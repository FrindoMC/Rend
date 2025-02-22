package rend

import kotlinx.coroutines.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import rend.utils.GuiEvent
import rend.utils.Utils
import kotlin.coroutines.EmptyCoroutineContext

@Mod(
    modid = "rend",
    name = "Rend",
    version = "0.1",
    useMetadata = true,
    clientSideOnly = true
)
class RendMain {
    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        RendConfig.load()
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        listOf(
            this,
            Utils,
            Rend
        ).forEach(MinecraftForge.EVENT_BUS::register)

        ClientCommandHandler.instance.registerCommand(RendCommand)
        ClientRegistry.registerKeyBinding(rendKeybind)
    }

    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) = scope.launch {
        if (event.gui !is GuiChest) return@launch
        val container = (event.gui as GuiChest).inventorySlots

        if (container !is ContainerChest) return@launch

        val deferred = waitUntilLastItem(container)
        try { deferred.await() } catch (_: Exception) { return@launch }

        val chestName = container.lowerChestInventory.displayName.unformattedText

        MinecraftForge.EVENT_BUS.post(GuiEvent.Loaded(container, chestName))
    }

    suspend fun waitUntilLastItem(container: ContainerChest) = coroutineScope {
        val deferredResult = CompletableDeferred<Unit>()
        val startTime = System.currentTimeMillis()

        fun check() {
            if (System.currentTimeMillis() - startTime > 1000) {
                deferredResult.completeExceptionally(Exception("Promise rejected"))
                return
            } else if (container.inventory[container.inventory.size - 37] != null) {
                deferredResult.complete(Unit)
            } else {
                launch {
                    delay(10)
                    check()
                }
            }
        }

        launch {
            check()
        }

        deferredResult
    }

    companion object {
        val mc: Minecraft = Minecraft.getMinecraft()
        var rendKeybind = KeyBinding("STOP REND", Keyboard.KEY_NONE, "Rend")
    }
}
