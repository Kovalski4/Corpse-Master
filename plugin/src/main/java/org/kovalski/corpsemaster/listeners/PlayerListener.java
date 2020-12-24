package org.kovalski.corpsemaster.listeners;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.kovalski.corpsemaster.CorpseHolder;
import org.kovalski.corpsemaster.ICorpse;
import org.kovalski.corpsemaster.Main;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener extends CorpseHolder implements Listener {

    private final Main instance = Main.getInstance();

    @EventHandler
    public void chunkLoad(ChunkLoadEvent e){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.kovalski.corpsemaster.ICorpse c : getCorpseCache()){
                    if (c.getChunk().equals(e.getChunk())){
                        World world = c.getChunk().getWorld();
                        List<Player> players = new ArrayList<>(world.getPlayers());
                        for (Player player : players){
                            c.updateCorpse(player);
                        }
                    }
                }
            }
        }.runTaskLater(instance, 10L);
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent e){
        for (ICorpse c : getCorpseCache()){
            c.updateCorpse(e.getPlayer());
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        for (ICorpse c : getCorpseCache()){
            c.updateCorpse(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        for (ICorpse c : getCorpseCache()){
            c.updateCorpse(e.getPlayer());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        instance.createCorpse(e.getEntity().getLocation(), e.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        for (ICorpse c : getCorpseCache()){
            c.updateCorpse(e.getPlayer());
        }
    }

    /*
    @EventHandler
    public void onShift(EntityPoseChangeEvent e){
        if (e.getPose().equals(Pose.SNEAKING) && e.getEntity().isOp()){
            instance.createCorpse(e.getEntity().getLocation(), (Player) e.getEntity());
        }
    }

     */

}
