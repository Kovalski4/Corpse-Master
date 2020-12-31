package org.kovalski.corpsemaster.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.kovalski.corpsemaster.Main;

public class MessageUtil {

    private final Main instance = Main.getInstance();
    private YamlConfig yamlConfig = instance.getYamlConfig();
    private ConfigurationSection messages = yamlConfig.getConfigurationSection("messages");

    public enum Messages{
        CONFIG_VERSION,
        PREFIX,
        MSG_RELOAD,
        ERROR_NO_PERMISSION

    }

    public String getMessage(Messages messageName){

        String message = " ";

        switch (messageName){
            case CONFIG_VERSION:
                message = yamlConfig.getString("config-version");
                break;
            case PREFIX:
                message = getPrefix();
                break;
            case MSG_RELOAD:
                message = messages.getString("reload");
                break;
            case ERROR_NO_PERMISSION:
                message = messages.getString("error_no_permission");
        }

        return message.replaceAll("%prefix%", getPrefix())
                .replaceAll("&", "§");
    }

    public String getPrefix(){
        String string =  yamlConfig.getString("prefix");
        if (string == null) return " ";
        else return string;
    }

    public void reload(){
        yamlConfig = instance.getYamlConfig();
        messages = yamlConfig.getConfigurationSection("messages");
    }

}
