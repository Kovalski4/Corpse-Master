package org.kovalski.corpsemaster;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.kovalski.corpsemaster.cmds.CorpseMasterCommand;
import org.kovalski.corpsemaster.listeners.PlayerListener;
import org.kovalski.corpsemaster.utils.MessageUtil;
import org.kovalski.corpsemaster.utils.UpdateChecker;
import org.kovalski.corpsemaster.utils.YamlConfig;

import java.io.File;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    private static Main instance;
    private MessageUtil messageUtil;
    private YamlConfig yamlConfig;
    private CorpseApi corpseApi;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        loadListeners();
        loadConfig();
        loadbStats();
        loadCommands();
        checkUpdate();
        corpseApi = new CorpseApi();
        corpseApi.setPlugin(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @SuppressWarnings("ConstantConditions")
    private void loadCommands(){
        getCommand("corpsemaster").setExecutor(new CorpseMasterCommand());
        getCommand("corpsemaster").setTabCompleter(new CorpseMasterCommand());
    }

    private void loadConfig(){
        yamlConfig = new YamlConfig(new File(this.getDataFolder(), "config.yml"), "config.yml");
        messageUtil = new MessageUtil();
    }

    private void loadListeners(){
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
    }

    @SuppressWarnings("unused")
    private void loadbStats(){
        int pluginId = 9560; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);
    }

    private void checkUpdate(){
        Logger logger = this.getLogger();
        new UpdateChecker(this, 86444).getVersion(version -> {
            DefaultArtifactVersion spigotVer = new DefaultArtifactVersion(version);
            DefaultArtifactVersion currentVer = new DefaultArtifactVersion(this.getDescription().getVersion());
            if (currentVer.getIncrementalVersion() < spigotVer.getIncrementalVersion()) {
                logger.info("Checking for updates...");
                logger.info("You are running outdated version of CorpseMaster (v"+currentVer+")");
                logger.info("Get latest version at: https://www.spigotmc.org/resources/86444/");
            }else{
                logger.info("Checking for updates...");
                logger.info("You are running latest version of CorpseMaster (v"+currentVer+")");
            }
        });
    }

    public void reloadConfig(){
        getYamlConfig().reload();
        getMessageUtil().reload();
    }

    public static Main getInstance() {
        return instance;
    }

    public void createCorpse(Location location, Player player) {

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        boolean hookEquipment = getYamlConfig().getBoolean("show-armor");
        ICorpse corpse;

        switch (version) {
            case "v1_16_R3":
                corpse = new NMSCorpse_v1_16_R3(location, player, hookEquipment);
                break;
            case "v1_16_R2":
                corpse = new NMSCorpse_v1_16_R2(location, player, hookEquipment);
                break;
            case "v1_16_R1":
                corpse = new NMSCorpse_v1_16_R1(location, player, hookEquipment);
                break;
            case "v1_15_R1":
                corpse = new NMSCorpse_v1_15_R1(location, player, hookEquipment);
                break;
            default:
                return;
        }

        corpse.spawnCorpse();
        getCorpseApi().cacheCorpse(corpse);
        getCorpseApi().cacheFakeBed(corpse.getBedLocation().getBlock());

        new BukkitRunnable() {
            @Override
            public void run() {
                getCorpseApi().removeCorpse(corpse);
            }
        }.runTaskLater(instance, 20L*yamlConfig.getInt("corpse-time"));

    }

    public YamlConfig getYamlConfig() {
        return yamlConfig;
    }

    public CorpseApi getCorpseApi() {
        return corpseApi;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}
