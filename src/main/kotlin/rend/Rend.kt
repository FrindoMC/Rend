package rend

import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import rend.RendMain.Companion.mc
import rend.utils.ClickEvent
import rend.utils.GuiEvent
import rend.utils.InventoryUtils.findItem
import rend.utils.InventoryUtils.findItemInContainer
import rend.utils.InventoryUtils.isHolding
import rend.utils.PacketReceivedEvent
import rend.utils.Utils.leftClick
import rend.utils.Utils.modMessage
import rend.utils.Utils.rightClick
import rend.utils.Utils.stripControlCodes
import rend.utils.Utils.swapToIndex
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object Rend {
    private val actionQueue: Queue<ActionTask> = LinkedList()
    data class ActionTask(var ticksLeft: Int, val action: () -> Unit) // MutablePairs wouldn't let me change value???
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private var isRunning = false
    private var isWaitingGui = false
    private var isWaitingSound = false
    private var hitCount = 0

    @SubscribeEvent
    fun onRightClick(e: ClickEvent.RightClickEvent) {
        if (mc.thePlayer.isHolding("Bonemerang", ignoreCase = true, mode = 1) && !isRunning && RendConfig.isEnabled) startMacro()
    }

    @SubscribeEvent
    fun onPacketReceive(e: PacketReceivedEvent) {
        val packet = e.packet
        if (packet !is S29PacketSoundEffect || mc.thePlayer.isHolding("terminator", "last", ignoreCase = true, mode = 1)) return
        if (packet.soundName == "tile.piston.out" && isWaitingSound) {
            if (hitCount++ >= 2) {
                modMessage("§2BackBone Hit while holding §4§l${mc.thePlayer.heldItem?.displayName?.stripControlCodes()} §2and wearing §4§l${mc.thePlayer.inventory.armorInventory[3]?.displayName?.stripControlCodes()}.")
                isWaitingSound = false
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(e: GuiEvent.Loaded) {
        if (e.name == "Your Equipment and Stats" && isWaitingGui && hitCount == 2) {
            isWaitingGui = false
            actionQueue.addAll(listOf(
                ActionTask(3) { findItemInContainer("Warden")?.let { clickSlot(it) } ?: return@ActionTask stopMacro(); hitCount = 0 },
                ActionTask(1) { mc.thePlayer.closeScreen() },
                ActionTask(1) { swapToItem("End Stone") },
                ActionTask(3) { rightClick() },
                ActionTask(2) { swapToItem("Bonemerang") },
                ActionTask(RendConfig.leftClickDelay) { leftClick() },
                ActionTask(6) { swapToItem("Terminator"); isRunning = false }
            ))
        }
    }

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) processQueue()
    }

    private fun processQueue() {
        actionQueue.peek()?.let { task ->
            if (--task.ticksLeft <= 0) {
                task.action()
                actionQueue.poll()
            }
        }
    }

    private fun startMacro() {
        isRunning = true
        isWaitingSound = true
        hitCount = 0

        actionQueue.addAll(listOf(
            ActionTask(0) { swapToItem("Blade of the Volcano") },
            ActionTask(3) { mc.thePlayer.sendChatMessage("/eq"); isWaitingGui = true }
        ))

        val startTime = System.currentTimeMillis()
        scheduler.schedule({
            if (isRunning && System.currentTimeMillis() - startTime >= 6000) {
                stopMacro()
            }
        }, 6000, TimeUnit.MILLISECONDS)
    }

    private fun stopMacro() {
        isRunning = false
        isWaitingGui = false
        isWaitingSound = false
        hitCount = 0
        actionQueue.clear()

        modMessage("Stopped macro")
    }

    private fun swapToItem(name: String) {
        findItem(name, ignoreCase = true, inInv = false, mode = 1)?.let { swapToIndex(it) } ?: return stopMacro()
    }

    private fun clickSlot(slot: Int){
        mc.thePlayer?.openContainer?.let {
            if (it is ContainerChest) mc.playerController?.windowClick(it.windowId, slot, 2, 3, mc.thePlayer)
        }
    }

//    override fun onKeyBind() {
//        stopMacro()
//    }
}