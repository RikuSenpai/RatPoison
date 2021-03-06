package rat.poison.scripts.aim

import rat.poison.game.*
import rat.poison.game.entity.*
import rat.poison.game.entity.EntityType.Companion.ccsPlayer
import rat.poison.settings.*
import rat.poison.utils.*
import org.jire.arrowhead.keyPressed
import rat.poison.curSettings
import rat.poison.strToBool
import java.lang.Math.toRadians
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.sin

val target = AtomicLong(-1)
val perfect = AtomicBoolean() //Perfect Aim boolean check, only for path aim
var boneTrig = false

fun reset() {
	target.set(-1L)
	perfect.set(false)
}

fun findTarget(position: Angle, angle: Angle, allowPerfect: Boolean,
                        lockFOV: Int = curSettings["AIM_FOV"].toInt(), BONE: Int = curSettings["AIM_BONE"].toInt()): Player {
	var closestFOV = Double.MAX_VALUE
	var closestDelta = Double.MAX_VALUE
	var closestPlayer = -1L

	forEntities result@{
		val entity = it.entity
		if (entity <= 0 || entity == me || !entity.canShoot()) {
			return@result false
		}

		if (it.type != EntityType.CCSPlayer) {
			return@result false
		}

		if (BONE == -3) { //Knife bot bone
			for (i in 3..8) {
				val arr = calcTarget(closestDelta, entity, position, angle, lockFOV, entity.nearestBone())

				if (arr[0] != -1.0) {
					closestFOV = arr[0] as Double
					closestDelta = arr[1] as Double
					closestPlayer = arr[2] as Long
				}
			}
		} else if (BONE == -2) { //Trigger bot bone
			for (i in 3..8) {
				val arr = calcTarget(closestDelta, entity, position, angle, lockFOV, i)

				if (arr[0] != -1.0) {
					closestFOV = arr[0] as Double
					closestDelta = arr[1] as Double
					closestPlayer = arr[2] as Long
				}
			}
		} else {
			if (BONE == NEAREST_BONE) { //Nearest bone check
				val nB = entity.nearestBone()
				if (nB != -999) {
					val arr = calcTarget(closestDelta, entity, position, angle, lockFOV, entity.nearestBone())

					if (arr[0] != -1.0) {
						closestFOV = arr[0] as Double
						closestDelta = arr[1] as Double
						closestPlayer = arr[2] as Long
					}
				}
			} else {
				val arr = calcTarget(closestDelta, entity, position, angle, lockFOV, BONE)

				if (arr[0] != -1.0) {
					closestFOV = arr[0] as Double
					closestDelta = arr[1] as Double
					closestPlayer = arr[2] as Long
				}
			}
		}
		return@result false
	}

	if (closestDelta == Double.MAX_VALUE || closestDelta < 0 || closestPlayer < 0) return -1
	if (curSettings["PERFECT_AIM"].strToBool() && allowPerfect && closestFOV <= curSettings["PERFECT_AIM_FOV"].toInt() && randInt(100 + 1) <= curSettings["PERFECT_AIM_CHANCE"].toInt()) {
		perfect.set(true)
	}

	return closestPlayer
}

fun calcTarget(calcClosestDelta: Double, entity: Entity, position: Angle, angle: Angle, lockFOV: Int = curSettings["AIM_FOV"].toInt(), BONE: Int): MutableList<Any> {
	val retList = mutableListOf(-1.0, 0.0, 0L)

	val ePos: Angle = entity.bones(BONE)
	val distance = position.distanceTo(ePos)

	val dest = getCalculatedAngle(me, ePos)

	val pitchDiff = abs(angle.x - dest.x)
	var yawDiff = abs(angle.y - dest.y)

	if (yawDiff > 180f) {
		yawDiff = 360f - yawDiff
	}

	val fov = abs(sin(toRadians(yawDiff)) * distance)
	val delta = abs((sin(toRadians(pitchDiff)) + sin(toRadians(yawDiff))) * distance)

	if (delta <= lockFOV && delta <= calcClosestDelta) {
		retList[0] = fov
		retList[1] = delta
		retList[2] = entity
	}

	return retList.toMutableList()
}

fun Entity.inMyTeam() =
		!curSettings["TEAMMATES_ARE_ENEMIES"].strToBool() && if (DANGER_ZONE) {
			me.survivalTeam().let { it > -1 && it == this.survivalTeam() }
		} else me.team() == team()

fun Entity.canShoot() = (if (DANGER_ZONE) { true } else { spotted() }
		&& !dormant()
		&& !dead()
		&& !inMyTeam()
		&& !isProtected()
		&& !me.dead())

internal inline fun <R> aimScript(duration: Int, crossinline precheck: () -> Boolean,
								  crossinline doAim: (destinationAngle: Angle,
													  currentAngle: Angle, aimSpeed: Int) -> R) = every(duration) {
	try {
		if (!precheck()) return@every
		if (!curSettings["ENABLE_AIM"].strToBool()) return@every

		val meWep = me.weapon()
		val meWepEnt = me.weaponEntity()

		if (!meWepEnt.canFire() && !meWep.automatic && !meWep.pistol && !meWep.shotgun && !meWep.sniper && !meWep.smg) { //Aim after shoot
			reset()
			return@every
		}

		if (meWep.sniper && !me.isScoped() && curSettings["ENABLE_SCOPED_ONLY"].strToBool()) { //Scoped only
			reset()
			return@every
		}

		if (meWep.grenade || meWep.knife) {
			reset()
			return@every
		}

		val aim = curSettings["ACTIVATE_FROM_AIM_KEY"].strToBool() && keyPressed(AIM_KEY)
		val forceAim = keyPressed(curSettings["FORCE_AIM_KEY"].toInt()) || curSettings["FORCE_AIM_ALWAYS"].strToBool()
		val haveAmmo = meWepEnt.bullets() > 0

		val pressed = (aim || forceAim || boneTrig) && !MENUTOG && haveAmmo
		var currentTarget = target.get()

		if (!pressed) {
			reset()
			return@every
		}

		val currentAngle = clientState.angle()
		val position = me.position()

		if (currentTarget < 0) {
			currentTarget = findTarget(position, currentAngle, aim)
			if (currentTarget < 0) {
				reset()
				return@every
			}
			target.set(currentTarget)
		}

		val aB = curSettings["AIM_BONE"].toInt()

		var destBone = aB

		if (aB == -1) { //Nearest bone check
			val nearestBone = currentTarget.nearestBone()

			if (nearestBone != -999) {
				destBone = nearestBone
			} else {
				reset()
				return@every
			}
		}

		if (currentTarget == me || !currentTarget.canShoot()) {
			reset()
			Thread.sleep(500)
			return@every
		} else {
			val bonePosition = currentTarget.bones(destBone)

			val destinationAngle = getCalculatedAngle(me, bonePosition) //Rename to current angle

			if (!perfect.get()) {
				destinationAngle.finalize(currentAngle, (1.1 - curSettings["AIM_SMOOTHNESS"].toDouble() / 5))
			}

			val aimSpeed = curSettings["AIM_SPEED"].toInt()
			doAim(destinationAngle, currentAngle, aimSpeed)
		}
	} catch (e: Exception) { e.printStackTrace() }
}