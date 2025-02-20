package rend.utils

import net.minecraft.inventory.ContainerChest
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

open class ClickEvent : Event() {
    @Cancelable
    class LeftClickEvent : ClickEvent()

    @Cancelable
    class RightClickEvent : ClickEvent()

    @Cancelable
    class MiddleClickEvent : ClickEvent()
}

abstract class GuiEvent : Event() {
    class Loaded(val gui: ContainerChest, val name: String) : GuiEvent()
}

@Cancelable
class PacketReceivedEvent(val packet: Packet<*>) : Event()