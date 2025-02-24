package rend.utils

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ChatComponentText
import net.minecraft.util.StringUtils
import rend.Rend.stopMacro
import rend.RendMain.Companion.mc
import rend.mixin.MinecraftAccessor
import rend.utils.InventoryUtils.findItem

object Utils {
    fun modMessage(message: String) {
        mc.thePlayer?.addChatMessage(ChatComponentText(message)) ?: return
    }

    fun String.stripControlCodes(): String {
        return StringUtils.stripControlCodes(this)
    }

    fun EntityPlayerSP.rotate(yaw: Float, pitch: Float) {
        this.rotationYaw = yaw
        this.rotationYawHead = yaw
        this.rotationPitch = pitch
    }

    fun swapToIndex(index: Int) {
        KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].keyCode)
    }

    fun swapToItem(name: String) {
        findItem(name, ignoreCase = true, inInv = false, mode = 1)?.let { swapToIndex(it) } ?: return stopMacro()
    }

    fun clickSlot(slot: Int){
        mc.thePlayer?.openContainer?.let {
            if (it is ContainerChest) mc.playerController?.windowClick(it.windowId, slot, 2, 3, mc.thePlayer)
        }
    }

    fun EntityLivingBase?.getSBMaxHealth(): Float {
        return this?.getEntityAttribute(SharedMonsterAttributes.maxHealth)?.baseValue?.toFloat() ?: 0f
    }

    var kuudraEntity: EntityMagmaCube? = null
    fun LocateKuudra() {
        kuudraEntity = mc.theWorld?.loadedEntityList
            ?.filterIsInstance<EntityMagmaCube>()
            ?.firstOrNull { it.slimeSize == 30 && it.getSBMaxHealth() == 100000f }
    }

    fun FaceKuudra() {
        val kuudra = kuudraEntity ?: return
        val kuudraPos = kuudra.positionVector

        val yaw = when {
            kuudraPos.xCoord < -128 -> 90  // RIGHT
            kuudraPos.xCoord > -72 -> -90 // LEFT
            kuudraPos.zCoord > -84 -> 0   // FRONT
            kuudraPos.zCoord < -132 -> 180 // BACK
            else -> return
        }

        mc.thePlayer.rotate(yaw.toFloat(), 0.0f)
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, true)
        modMessage("facing kuudra $yaw")
    }

    fun leftClick() {
        (mc as MinecraftAccessor).invokeClickMouse()
    }

    fun rightClick() {
        (mc as MinecraftAccessor).invokeRightClickMouse()
    }
}