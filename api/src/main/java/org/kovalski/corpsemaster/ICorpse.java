package org.kovalski.corpsemaster;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface ICorpse {

    void spawnCorpse();

    void removeCorpse();

    void updateCorpse(Player player);

    void updateCorpseEveryone();

    void sendCorpsePacket(Player player);

    Chunk getChunk();

    Inventory getInventory();

    Location getBedLocation();

}
