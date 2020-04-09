package me.kosinkadink.performantplants.util;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReflectionHelper {

    private static Map<String, Class<?>> classMap = new HashMap<>();
    private static Map<String, Method> methodMap = new HashMap<>();
    private static Map<String, Constructor<?>> constructorMap = new HashMap<>();

    static {
        String version = getVersion();
        try {
            // classes
            classMap.put("CraftMetaSkull", Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftMetaSkull", version)));
            classMap.put("CraftWorld", Class.forName(String.format("org.bukkit.craftbukkit.%s.CraftWorld", version)));
            classMap.put("WorldServer", Class.forName(String.format("net.minecraft.server.%s.WorldServer", version)));
            classMap.put("BlockPosition", Class.forName(String.format("net.minecraft.server.%s.BlockPosition", version)));
            classMap.put("GameProfile", Class.forName("com.mojang.authlib.GameProfile"));
            classMap.put("TileEntitySkull", Class.forName(String.format("net.minecraft.server.%s.TileEntitySkull", version)));
            // methods
            methodMap.put("CraftWorld.getHandle", classMap.get("CraftWorld").getMethod("getHandle"));
            methodMap.put("TileEntitySkull.setGameProfile", classMap.get("TileEntitySkull").getMethod("setGameProfile", classMap.get("GameProfile")));
            methodMap.put("CraftMetaSkull.setProfile", classMap.get("CraftMetaSkull").getDeclaredMethod("setProfile", classMap.get("GameProfile")));
            methodMap.put("WorldServer.getTileEntity", classMap.get("WorldServer").getMethod("getTileEntity", classMap.get("BlockPosition")));
            // constructors
            constructorMap.put("BlockPosition", classMap.get("BlockPosition").getConstructor(int.class, int.class, int.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSkullTexture(ItemStack itemStack, String encodedUrl) {
        if (encodedUrl == null || encodedUrl.isEmpty()) {
            return;
        }
        // if not type player head, do nothing
        if (itemStack.getType() != Material.PLAYER_HEAD) {
            return;
        }
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        try {
            Method setProfile = methodMap.get("CraftMetaSkull.setProfile");
            setProfile.setAccessible(true);
            setProfile.invoke(skullMeta, createProfile(encodedUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // set item meta of item stack
        itemStack.setItemMeta(skullMeta);
    }

    public static void setSkullTexture(Block block, String encodedUrl) {
        // if not a player head, do nothing
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
            return;
        }
        // if no encodedUrl provided, do nothing
        if (encodedUrl == null || encodedUrl.isEmpty()) {
            return;
        }
        try {
            GameProfile profile = createProfile(encodedUrl);
            Object handle = methodMap.get("CraftWorld.getHandle").invoke(block.getWorld());
            Object tileEntity = methodMap.get("WorldServer.getTileEntity").invoke(handle,
                    constructorMap.get("BlockPosition").newInstance(block.getX(), block.getY(), block.getZ()));
            // if no tile entity found, do nothing
            if (tileEntity == null) {
                return;
            }
            methodMap.get("TileEntitySkull.setGameProfile").invoke(tileEntity, profile);
            block.getState().update(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GameProfile createProfile(String encodedUrl) {
        // create UUID from bytes so that GameProfile will have common UUID for common encodedUrls (items are stackable)
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(encodedUrl.getBytes()), null);
        profile.getProperties().put("textures", new Property("textures", encodedUrl));
        return profile;
    }

    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}
