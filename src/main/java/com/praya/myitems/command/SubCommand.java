package com.praya.myitems.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    boolean onSubCommand(CommandSender sender, Command command, String label, String[] args);
}