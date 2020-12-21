package org.kovalski.corpsemaster.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.kovalski.corpsemaster.Main;

public class CorpseMasterCommand implements CommandExecutor {

    private Main instance = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (sender.hasPermission("corpsemaster.admin")){
            instance.getYamlConfig().reload();
            sender.sendMessage("§5[Corpse-Master] Configuration Reloaded");
            return true;

        }

        return false;
    }
}
