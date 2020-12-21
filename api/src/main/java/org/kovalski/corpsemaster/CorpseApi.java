package org.kovalski.corpsemaster;

import org.bukkit.plugin.Plugin;

public class CorpseApi extends CorpseHolder {

    private static Plugin instance;

    public void setPlugin (Plugin plugin){
        instance = plugin;
    }

    public Plugin getInstance(){
        return instance;
    }

    public void removeCorpse(ICorpse corpse){
        corpse.removeCorpse();
        removeCorpseCache(corpse);
        removeFakeBedCache(corpse.getBedLocation().getBlock());
    }
}
