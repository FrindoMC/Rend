package rend

import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import rend.RendConfig.config
import rend.RendMain.Companion.mc
import rend.utils.ClickEvent
import rend.utils.GuiEvent
import rend.utils.InventoryUtils.findItemInContainer
import rend.utils.InventoryUtils.isHolding
import rend.utils.PacketReceivedEvent
import rend.utils.Utils.FaceKuudra
import rend.utils.Utils.LocateKuudra
import rend.utils.Utils.clickSlot
import rend.utils.Utils.leftClick
import rend.utils.Utils.modMessage
import rend.utils.Utils.rightClick
import rend.utils.Utils.stripControlCodes
import rend.utils.Utils.swapToItem
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule
import kotlin.math.floor

object Rend {
    private val actionQueue: Queue<ActionTask> = LinkedList()
    data class ActionTask(var ticksLeft: Int, val action: () -> Unit) // MutablePairs wouldn't let me change value???
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private var isRunning = false
    private var isWaitingGui = false
    private var isWaitingSound = false
    private var hitCount = 0

    private var hasTriggered = false
    private var inKuudra = false

    @SubscribeEvent
    fun onRightClick(e: ClickEvent.RightClickEvent) {
        if (mc.thePlayer.isHolding("Bonemerang", ignoreCase = true, mode = 1) && !isRunning && config.isEnabled) startMacro()
    }

    @SubscribeEvent (priority = EventPriority.HIGHEST)
    fun onPacketReceive(e: PacketReceivedEvent) {
        when (val packet = e.packet) {
            is S29PacketSoundEffect -> {
                if (!mc.thePlayer.isHolding("terminator", "last", ignoreCase = true, mode = 1) || packet.soundName != "tile.piston.out" || !isWaitingSound) return
                if (++hitCount >= 2) {
                    modMessage("§2BackBone Hit while holding §4§l${mc.thePlayer.heldItem?.displayName?.stripControlCodes()} §2and wearing §4§l${mc.thePlayer.inventory.armorInventory[3]?.displayName?.stripControlCodes()}.")
                    isWaitingSound = false
                }
            }

            is S08PacketPlayerPosLook -> {
                if (floor(packet.x) ==  -102.0 && floor(packet.y) == 6.0 && floor(packet.z) == -106.0 && inKuudra && !hasTriggered && config.autoFaceKuudra) {
                    hasTriggered = true
                    Timer().schedule(30) {
                        FaceKuudra()
                    }
                }
            }

            else -> return
        }
    }

    @SubscribeEvent
    fun onGuiOpen(e: GuiEvent.Loaded) {
        if (e.name == "Your Equipment and Stats" && isWaitingGui) {
            isWaitingGui = false

            Thread({
                for (i in 0..100) {
                    if (hitCount >= 2) {
                        secondQueue()
                        return@Thread
                    }
                    Thread.sleep(20)
                }
                modMessage("Couldn't detect back bone within time stopping macro..")
                stopMacro()
            }, "HitCountChecker").start()
        }
    }

    @SubscribeEvent
    fun onKeyBind(e: InputEvent.KeyInputEvent) {
        if (RendMain.rendKeybind.isPressed && isRunning) {
            stopMacro()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load) {
        stopMacro()
        hasTriggered = false
        inKuudra = false
    }

    private fun secondQueue() {
        actionQueue.addAll(listOf(
            ActionTask(2) { findItemInContainer("Warden")?.let { clickSlot(it) } ?: return@ActionTask stopMacro(); hitCount = 0 },
            ActionTask(1) { mc.thePlayer.closeScreen() },
            ActionTask(1) { swapToItem("End Stone") },
            ActionTask(3) { rightClick() },
            ActionTask(2) { swapToItem("Bonemerang") },
            ActionTask(config.leftClickDelay) { leftClick() },
            ActionTask(6) { swapToItem("Terminator"); isRunning = false }
        ))
    }

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (config.autoFaceKuudra) LocateKuudra()
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

    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        val message = e.message.unformattedText

        if (message.startsWith("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!")) inKuudra = true
    }

    private fun startMacro() {
        isRunning = true
        isWaitingSound = true
        hitCount = 0

        actionQueue.addAll(listOf(
            ActionTask(0) { swapToItem("Blade of the Volcano") },
            ActionTask(config.eqDelay) { mc.thePlayer.sendChatMessage("/eq"); isWaitingGui = true }
        ))

        val startTime = System.currentTimeMillis()
        scheduler.schedule({
            if (isRunning && System.currentTimeMillis() - startTime >= 6000) {
                stopMacro()
            }
        }, 6000, TimeUnit.MILLISECONDS)
    }

    fun stopMacro() {
        if (!isRunning) return
        isRunning = false
        isWaitingGui = false
        isWaitingSound = false
        hitCount = 0
        actionQueue.clear()

        modMessage("Stopped macro")
    }
}