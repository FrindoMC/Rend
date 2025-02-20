package rend.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor("timer")  // Accessor for the 'timer' field
    Timer getTimer();

    @Invoker("clickMouse")  // Invoker for the 'clickMouse' method
    void invokeClickMouse();

    @Invoker("rightClickMouse")  // Invoker for the 'rightClickMouse' method
    void invokeRightClickMouse();
}
