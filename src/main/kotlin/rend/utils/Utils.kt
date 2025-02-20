package rend.utils

import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.ChatComponentText
import net.minecraft.util.StringUtils
import rend.RendMain.Companion.mc
import rend.mixin.MinecraftAccessor

object Utils {
    fun modMessage(message: String) {
        mc.thePlayer?.addChatMessage(ChatComponentText(message)) ?: return
    }

    fun String.stripControlCodes(): String {
        return StringUtils.stripControlCodes(this)
    }

    fun swapToIndex(index: Int) {
        KeyBinding.onTick(mc.gameSettings.keyBindsHotbar[index].keyCode)
    }

    fun leftClick() {
        (mc as MinecraftAccessor).invokeClickMouse()
    }

    fun rightClick() {
        (mc as MinecraftAccessor).invokeRightClickMouse()
    }
}