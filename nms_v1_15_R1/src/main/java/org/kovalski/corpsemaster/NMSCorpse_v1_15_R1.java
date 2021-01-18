package org.kovalski.corpsemaster;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NMSCorpse_v1_15_R1 implements ICorpse {

    private List<Pair<EnumItemSlot, ItemStack>> armorContents = new ArrayList<>();
    private org.bukkit.Chunk chunk;
    private boolean hookEquipment;
    private Inventory inventory;
    private Location bedLoc;

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

    public NMSCorpse_v1_15_R1(Location location, Player player, boolean hookEquipment){
        this.nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        this.nmsWorld = ((CraftWorld)Bukkit.getWorld(player.getWorld().getName())).getHandle();
        this.gameProfile = getGameProfile(player);
        this.bedLoc = player.getLocation().clone().toVector().setY(0.0D).toLocation(player.getWorld());
        this.fakeBedPacket = new PacketPlayOutBlockChange(fakeBed(getDirection(location.clone().getYaw())), toBlockPosition(bedLoc));
        this.inventory = player.getInventory();
        this.owner = player;
        if (hookEquipment) {
            setArmorContents();
            this.inventory = player.getInventory();
            this.hookEquipment = true;
        }
    }

    @Override
    public void spawnCorpse() {
        this.corpse = null;
        this.corpse = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        setMetadata();
        setLocation();
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

    public void sendEquipmentPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        for(Pair<EnumItemSlot, ItemStack> pair : armorContents){
            playerConnection.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), pair.getFirst(), pair.getSecond()));
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
        }.runTaskLater(new CorpseApi().getInstance(), 20L);
    }

    @Override
    public void updateCorpseEveryone() {
        for (Player player : Bukkit.getOnlinePlayers()){
            updateCorpse(player);
        }
    }

    @Override
    public void removeCorpse() {
        for (Player p : Bukkit.getOnlinePlayers()){
            removeCorpsePacket(p);
        }
    }

    public void removeCorpsePacket(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(destroy);
    }

    @Override
    public org.bukkit.Chunk getChunk() {
        return this.chunk;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public GameProfile getGameProfile(Player player) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        return p.getProfile();
    }

    @Override
    public Location getBedLocation() {
        return bedLoc;
    }

    private void setMetadata(){
        corpse.getDataWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.values()[EntityPose.SLEEPING.ordinal()]);
        corpse.getDataWatcher().set(DataWatcherRegistry.a.a(16), (byte)127);
        setBedPosition();
    }

    private void setArmorContents(){
        ItemStack helmet = CraftItemStack.asNMSCopy(owner.getInventory().getHelmet());
        ItemStack chestplate = CraftItemStack.asNMSCopy(owner.getInventory().getChestplate());
        ItemStack leggings = CraftItemStack.asNMSCopy(owner.getInventory().getLeggings());
        ItemStack boots = CraftItemStack.asNMSCopy(owner.getInventory().getBoots());

        Pair<EnumItemSlot, ItemStack> pair = new Pair<>(EnumItemSlot.HEAD, helmet);
        Pair<EnumItemSlot, ItemStack> pair2 = new Pair<>(EnumItemSlot.CHEST, chestplate);
        Pair<EnumItemSlot, ItemStack> pair3 = new Pair<>(EnumItemSlot.LEGS, leggings);
        Pair<EnumItemSlot, ItemStack> pair4 = new Pair<>(EnumItemSlot.FEET, boots);

        armorContents.add(pair);
        armorContents.add(pair2);
        armorContents.add(pair3);
        armorContents.add(pair4);
    }

    public void setLocation(){

        Location location = new CorpseUtil().getFixedLocation(owner.getLocation());
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        this.corpse.setPositionRotation(
                x
                , y
                , z
                , yaw
                , pitch);
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