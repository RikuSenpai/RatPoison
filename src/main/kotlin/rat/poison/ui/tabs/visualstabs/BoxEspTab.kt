package rat.poison.ui.tabs.visualstabs

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.tabbedpane.Tab
import rat.poison.curSettings
import rat.poison.ui.changed
import rat.poison.ui.tabs.boxEspTab
import rat.poison.ui.uiHelpers.VisCheckBoxCustom
import rat.poison.ui.uiHelpers.VisColorPickerCustom

class BoxEspTab : Tab(false, false) {
    private val table = VisTable()

    //Init labels/sliders/boxes that show values here
    val boxEsp = VisCheckBoxCustom("Box", "ENABLE_BOX_ESP")
    val boxEspDetails = VisCheckBoxCustom("Box Details", "BOX_ESP_DETAILS")
    val boxEspHealth = VisCheckBoxCustom("Health", "BOX_ESP_HEALTH")
    val boxEspHealthPos = VisSelectBox<String>()
    val boxEspArmor = VisCheckBoxCustom("Armor", "BOX_ESP_ARMOR")
    val boxEspArmorPos = VisSelectBox<String>()
    val boxEspName = VisCheckBoxCustom("Name", "BOX_ESP_NAME")
    val boxEspNamePos = VisSelectBox<String>()
    val boxEspWeapon = VisCheckBoxCustom("Weapon", "BOX_ESP_WEAPON")
    val boxEspWeaponPos = VisSelectBox<String>()

    val showTeam = VisCheckBoxCustom(" ", "BOX_SHOW_TEAM")
    val boxTeamColor = VisColorPickerCustom("Teammates", "BOX_TEAM_COLOR")

    val showEnemies = VisCheckBoxCustom(" ", "BOX_SHOW_ENEMIES")
    val boxEnemyColor = VisColorPickerCustom("Enemies", "BOX_ENEMY_COLOR")

    val showDefusers = VisCheckBoxCustom(" ", "BOX_SHOW_DEFUSERS")
    val boxDefuserColor = VisColorPickerCustom("Defusers", "BOX_DEFUSER_COLOR")

    init {
        //Create Box ESP Health Pos Selector
        boxEspHealthPos.setItems("Left", "Right")
        boxEspHealthPos.selected = when (curSettings["BOX_ESP_HEALTH_POS"].replace("\"", "")) {
            "L" -> "Left"
            else -> "Right"
        }
        boxEspHealthPos.changed { _, _ ->
            curSettings["BOX_ESP_HEALTH_POS"] = boxEspHealthPos.selected.first()
            true
        }

        //Create Box ESP Armor Pos Selector
        boxEspArmorPos.setItems("Left", "Right")
        boxEspArmorPos.selected = when (curSettings["BOX_ESP_ARMOR_POS"].replace("\"", "")) {
            "L" -> "Left"
            else -> "Right"
        }
        boxEspArmorPos.changed { _, _ ->
            curSettings["BOX_ESP_ARMOR_POS"] = boxEspArmorPos.selected.first()
            true
        }

        //Create Box ESP Name Pos Selector
        boxEspNamePos.setItems("Top", "Bottom")
        boxEspNamePos.selected = when (curSettings["BOX_ESP_NAME_POS"].replace("\"", "")) {
            "T" -> "Top"
            else -> "Bottom"
        }
        boxEspNamePos.changed { _, _ ->
            curSettings["BOX_ESP_NAME_POS"] = boxEspNamePos.selected.first()
            true
        }

        //Create Box ESP Weapon Pos Selector
        boxEspWeaponPos.setItems("Top", "Bottom")
        boxEspWeaponPos.selected = when (curSettings["BOX_ESP_WEAPON_POS"].replace("\"", "")) {
            "T" -> "Top"
            else -> "Bottom"
        }
        boxEspWeaponPos.changed { _, _ ->
            curSettings["BOX_ESP_WEAPON_POS"] = boxEspWeaponPos.selected.first()
            true
        }

        table.padLeft(25F)
        table.padRight(25F)

        table.add(boxEsp).left().row()
        table.add(boxEspDetails).left().row()
        table.add(boxEspHealth).left()
        table.add(boxEspHealthPos).left().row()
        table.add(boxEspArmor).left()
        table.add(boxEspArmorPos).left().row()
        table.add(boxEspName).left()
        table.add(boxEspNamePos).left().row()
        table.add(boxEspWeapon).left()
        table.add(boxEspWeaponPos).left().row()

        var tmpTable = VisTable()
        tmpTable.add(showTeam)
        tmpTable.add(boxTeamColor).width(175F - showTeam.width).padRight(50F)

        table.add(tmpTable).left()

        tmpTable = VisTable()
        tmpTable.add(showEnemies)
        tmpTable.add(boxEnemyColor).width(175F - showEnemies.width).padRight(50F)

        table.add(tmpTable).left().row()

        tmpTable = VisTable()
        tmpTable.add(showDefusers)
        tmpTable.add(boxDefuserColor).width(175F - showEnemies.width).padRight(50F)

        table.add(tmpTable).left().row()
    }

    override fun getContentTable(): Table? {
        return table
    }

    override fun getTabTitle(): String? {
        return "Box"
    }
}

fun boxEspTabUpdate() {
    boxEspTab.apply {
        boxEsp.update()
        boxEspDetails.update()
        boxEspHealth.update()
        boxEspArmor.update()
        boxEspName.update()
        boxEspWeapon.update()
        showTeam.update()
        showEnemies.update()
        boxTeamColor.update()
        boxEnemyColor.update()
        boxDefuserColor.update()
    }
}