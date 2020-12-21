package org.kovalski.corpsemaster;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;

public abstract class FakeBedHolder {

    private static final HashMap<Block, Integer> fakeBedCache = new HashMap<>();

    public void removeFakeBedCache(Block block){
        fakeBedCache.replace(block, fakeBedCache.get(block)-1);
        if (fakeBedCache.get(block) < 1) {
            fakeBedCache.remove(block);
            for (Player player : Bukkit.getOnlinePlayers()){
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }
        }
    }

    public void cacheFakeBed(Block block){
        if (fakeBedCache.containsKey(block))
            fakeBedCache.replace(block, fakeBedCache.get(block)+1);
        else
            fakeBedCache.put(block, 1);
    }

}
