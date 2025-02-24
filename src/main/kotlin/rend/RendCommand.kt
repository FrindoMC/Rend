package rend

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.NumberInvalidException
import net.minecraft.command.WrongUsageException
import rend.RendConfig.config
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
            "eqdelay" -> {
                if (args.size < 2) throw WrongUsageException(getCommandUsage(sender))
                val delayValue = args[1].toIntOrNull() ?: throw NumberInvalidException("Invalid number: ${args[1]}")
                handleEQDelay(delayValue)
            }
            "face" -> handleFace()
            else -> throw WrongUsageException(getCommandUsage(sender))
        }
    }

    private fun handleToggle() {
        config.isEnabled = !config.isEnabled
        modMessage("Rend toggle: ${config.isEnabled}")
        RendConfig.save()
    }

    private fun handleDelay(value: Int) {
        config.leftClickDelay = value
        modMessage("Left click delay is $value ticks.")
        RendConfig.save()
    }

    private fun handleEQDelay(value: Int) {
        config.eqDelay = value
        modMessage("Equipment menu delay is $value ticks.")
        RendConfig.save()
    }

    private fun handleFace() {
        config.autoFaceKuudra = !config.autoFaceKuudra
        modMessage("Auto face kuuudra: ${config.autoFaceKuudra}")
        RendConfig.save()
    }
}