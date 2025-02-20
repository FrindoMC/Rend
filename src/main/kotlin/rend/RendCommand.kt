package rend

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.NumberInvalidException
import net.minecraft.command.WrongUsageException
import rend.utils.Utils.modMessage

object RendCommand : CommandBase() {
    override fun getCommandName(): String = "rend"

    override fun getCommandUsage(sender: ICommandSender?): String = "/rend (toggle|delay)"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args.isNullOrEmpty()) {
            handleToggle()
            return
        }

        when (args[0]) {
            "toggle" -> handleToggle()
            "delay" -> {
                if (args.size < 2) throw WrongUsageException(getCommandUsage(sender))
                val delayValue = args[1].toIntOrNull() ?: throw NumberInvalidException("Invalid number: ${args[1]}")
                handleDelay(delayValue)
            }
            else -> throw WrongUsageException(getCommandUsage(sender))
        }
    }

    private fun handleToggle() {
        RendConfig.isEnabled = !RendConfig.isEnabled
        modMessage("Rend toggle: ${RendConfig.isEnabled}")
        RendConfig.save()
    }

    private fun handleDelay(value: Int) {
        RendConfig.leftClickDelay = value
        modMessage("Left click delay is $value ticks.")
        RendConfig.save()
    }
}