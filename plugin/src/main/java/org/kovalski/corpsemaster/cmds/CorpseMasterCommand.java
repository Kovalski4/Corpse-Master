package org.kovalski.corpsemaster.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.kovalski.corpsemaster.Main;
import org.kovalski.corpsemaster.utils.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class CorpseMasterCommand implements CommandExecutor, TabCompleter {

    private final Main instance = Main.getInstance();
    private final MessageUtil messageUtil = instance.getMessageUtil();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("corpsemaster.admin")){
            sender.sendMessage(messageUtil.getMessage(MessageUtil.Messages.ERROR_NO_PERMISSION));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
            instance.reloadConfig();
            sender.sendMessage(messageUtil.getMessage(MessageUtil.Messages.MSG_RELOAD));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
        }
        return completions;
    }
}
