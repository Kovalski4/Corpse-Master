package org.kovalski.corpsemaster;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TrapDoor;

public class CorpseUtil {

    public Location getFixedLocation(Location location) {

        double y = location.getBlockY();
        World world = location.getWorld();
        double x = location.getX();
        double z = location.getZ();
        Location loc = new Location(world, x, y, z);
        Block block = loc.getBlock();

        while (true) {

            if (y < 0) {
                break;
            } else if (block.getType() == org.bukkit.Material.WATER) {

                if (block.getRelative(BlockFace.UP).getType() == org.bukkit.Material.AIR) {
                    y -= 0.1;
                    break;
                } else {
                    y++;
                }

            } else if (block.getType() == org.bukkit.Material.AIR) {
                y--;
            } else if (block.getType() == org.bukkit.Material.SNOW) {
                y -= 0.8;
                break;
            } else if (block.getBlockData() instanceof TrapDoor) {
                y -= 0.7;
                break;
            } else if (block.getBlockData() instanceof Slab) {
                Slab slab = (Slab) block.getBlockData();
                Slab.Type type = slab.getType();
                if (type == Slab.Type.BOTTOM) {
                    y -= 0.4;
                    break;
                }
            } else {
                break;
            }

            loc = new Location(world, x, y, z);
            block = loc.getBlock().getRelative(BlockFace.DOWN);

        }

        if (loc.getBlock().getType() != Material.AIR){
            y++;
        }

        return new Location(world, x, y, z, loc.getYaw(), loc.getPitch());
    }

}
