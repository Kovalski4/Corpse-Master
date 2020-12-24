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
        MSG_RELOAD

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
