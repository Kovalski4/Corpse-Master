package org.kovalski.corpsemaster;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class NMSCorpse_v1_15_R1 implements ICorpse {

    private MinecraftServer nmsServer;
    private WorldServer nmsWorld;
    private GameProfile gameProfile;
    private EntityPlayer corpse;
    private Player owner;

    private PacketPlayOutBlockChange fakeBedPacket;
    private PacketPlayOutPlayerInfo playerInfoAdd;
    private PacketPlayOutNamedEntitySpawn namedEntitySpawn;
    private PacketPlayOutEntityHeadRotation headRotation;
    private PacketPlayOutEntityDestroy destroy;
    private PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;
    private PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;
    private Inventory inventory;
    private org.bukkit.Chunk chunk;
    private Location bedLoc;
    private Boolean hookEquipment = false;

    public NMSCorpse_v1_15_R1(Location location, Player player, boolean hookEquipment){
        this.nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        this.nmsWorld = ((CraftWorld)Bukkit.getWorld(player.getWorld().getName())).getHandle();
        this.gameProfile = getGameProfile(player);
        this.bedLoc = player.getLocation().clone().toVector().setY(0.0D).toLocation(player.getWorld());
        this.fakeBedPacket = new PacketPlayOutBlockChange(fakeBed(getDirection(location.clone().getYaw())), toBlockPosition(bedLoc));
        this.hookEquipment = hookEquipment;
        this.owner = player;
        this.inventory = player.getInventory();
    }

    @Override
    public void spawnCorpse() {
        this.corpse = null;
        this.corpse = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        setMetadata();
        this.corpse.setPositionRotation(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(), owner.getLocation().getYaw(), owner.getEyeLocation().getPitch());
        this.movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(corpse.getId(), (short) 0,(short)2,(short)0,(byte)0,(byte)0, true);
        this.playerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, corpse);
        this.namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(corpse);
        this.headRotation = new PacketPlayOutEntityHeadRotation(corpse, (byte)MathHelper.d(corpse.getHeadRotation() * 256.0F / 360.0F));
        this.packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(corpse.getId(), corpse.getDataWatcher(), true);
        this.destroy = new PacketPlayOutEntityDestroy(corpse.getId());
        this.chunk = owner.getWorld().getChunkAt(bedLoc);
        updateCorpseEveryone();
    }

    @Override
    public void removeCorpse() {
        for (Player p : Bukkit.getOnlinePlayers()){
            removeCorpsePacket(p);
        }
    }

    @Override
    public void updateCorpse(Player player) {
        removeCorpsePacket(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                sendCorpsePacket(player);
            }
        }.runTaskLater(new CorpseApi().getInstance(), 10L);
    }

    @Override
    public org.bukkit.Chunk getChunk() {
        return this.chunk;
    }

    @Override
    public void updateCorpseEveryone() {
        for (Player player : Bukkit.getOnlinePlayers()){
            updateCorpse(player);
        }
    }

    @Override
    public void sendCorpsePacket(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(playerInfoAdd);
        connection.sendPacket(namedEntitySpawn);
        connection.sendPacket(headRotation);
        connection.sendPacket(packetPlayOutEntityMetadata);
        connection.sendPacket(fakeBedPacket);
        connection.sendPacket(movePacket);
        if (hookEquipment) sendEquipmentPacket(player);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Location getBedLocation() {
        return bedLoc;
    }

    public void sendEquipmentPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        net.minecraft.server.v1_15_R1.ItemStack helmet = CraftItemStack.asNMSCopy(owner.getInventory().getHelmet());
        net.minecraft.server.v1_15_R1.ItemStack chestplate = CraftItemStack.asNMSCopy(owner.getInventory().getChestplate());
        net.minecraft.server.v1_15_R1.ItemStack leggings = CraftItemStack.asNMSCopy(owner.getInventory().getLeggings());
        net.minecraft.server.v1_15_R1.ItemStack boots = CraftItemStack.asNMSCopy(owner.getInventory().getBoots());

        playerConnection.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), EnumItemSlot.HEAD, helmet));
        playerConnection.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), EnumItemSlot.CHEST, chestplate));
        playerConnection.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), EnumItemSlot.LEGS, leggings));
        playerConnection.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), EnumItemSlot.FEET, boots));

    }

    public void removeCorpsePacket(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(destroy);
    }

    private void setMetadata(){
        corpse.getDataWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.values()[EntityPose.SLEEPING.ordinal()]);
        corpse.getDataWatcher().set(DataWatcherRegistry.a.a(16), (byte)127);
        setBedPosition();
    }

    public GameProfile getGameProfile(Player player) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        return p.getProfile();
    }

    static IBlockAccess fakeBed(EnumDirection direction){
        return new IBlockAccess() {
            @Override
            public TileEntity getTileEntity(BlockPosition blockPosition) {
                return null;
            }

            @Override
            public IBlockData getType(BlockPosition blockPosition) {
                return Blocks.WHITE_BED.getBlockData().set(BlockBed.PART, BlockPropertyBedPart.HEAD).set(BlockBed.FACING, direction);
            }

            @Override
            public Fluid getFluid(BlockPosition blockPosition) {
                return null;
            }
        };
    }

    public void setBedPosition() {
        corpse.getDataWatcher().set(DataWatcherRegistry.m.a(13), Optional.of(toBlockPosition(this.bedLoc)));
    }

    static BlockPosition toBlockPosition(Location location){
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    static float transform(float rawYaw){
        rawYaw = rawYaw < 0.0F ? 360.0F + rawYaw : rawYaw;
        rawYaw = rawYaw % 360.0F;
        return rawYaw;
    }

    static EnumDirection getDirection(float f) {
        f = transform(f);
        EnumDirection a = null;
        if (f >= 315.0F || f <= 45.0F) {
            a = EnumDirection.NORTH;
        }

        if (f >= 45.0F && f <= 135.0F) {
            a = EnumDirection.EAST;
        }

        if (f >= 135.0F && f <= 225.0F) {
            a = EnumDirection.SOUTH;
        }

        if (f >= 225.0F && f <= 315.0F) {
            a = EnumDirection.WEST;
        }

        return a;
    }
}
