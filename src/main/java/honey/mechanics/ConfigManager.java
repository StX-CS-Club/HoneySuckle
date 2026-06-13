package honey.mechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.player.Player;
import honey.player.armory.Ammo;
import honey.player.armory.Armor;
import honey.player.armory.Armory;
import honey.player.armory.Attack;
import honey.player.armory.Effect;
import honey.player.armory.Weapon;
import honey.player.build.Blueprint;
import honey.player.build.Build;
import honey.player.inventory.Craft;
import honey.player.inventory.Inventory;
import honey.player.inventory.KeyItem;
import honey.rendering.Menu;
import honey.rendering.Rendering;
import honey.rendering.Splash;
import honey.world.Biome;
import honey.world.Brain;
import honey.world.Entity;
import honey.world.Navigator;
import honey.world.Projectile;
import honey.world.Tile;
import honey.world.World;
import honey.world.WorldObject;

/*
 * ConfigManager.java *
 - Interprets config map from FileManager
 - Holds all config values as final fields
 - Distributes itself to all classes that need it
 */
public class ConfigManager {

    public final int fps;
    public final int gameWidth;
    public final int gameHeight;
    public final int tileSize;
    public final int hudSize;
    public final int cameraDelay;
    public final int renderDistance;
    public final int lightScale;
    public final int slashFrameSize;
    public final int stabFrameWidth;
    public final int stabFrameHeight;
    public final double criticalMouseScroll;
    public final String startingBiome;
    public final List<String> playerTags;
    public final List<String> startingBlueprints;
    public final List<String> startingRecipes;
    public final List<String> startingWeapons;
    public final String startingArmor;
    public final List<String> recipeOrder;

    public ConfigManager(Map<String, Object> data) {
        fps               = getInt(data,    "fps",                30);
        gameWidth         = getInt(data,    "gameWidth",          640);
        gameHeight        = getInt(data,    "gameHeight",         480);
        tileSize          = getInt(data,    "tileSize",           32);
        hudSize           = getInt(data,    "hudSize",            64);
        cameraDelay       = getInt(data,    "cameraDelay",        6);
        renderDistance    = getInt(data,    "renderDistance",     2);
        lightScale        = getInt(data,    "lightScale",         8);
        slashFrameSize    = getInt(data,    "slashFrameSize",     8);
        stabFrameWidth    = getInt(data,    "stabFrameWidth",     4);
        stabFrameHeight   = getInt(data,    "stabFrameHeight",    16);
        criticalMouseScroll = getDouble(data, "criticalMouseScroll", 0.25);
        startingBiome     = getString(data, "startingBiome",      "wetlands");
        playerTags        = getStringList(data, "playerTags", new ArrayList<>());
        startingBlueprints = getStringList(data, "startingBlueprints", new ArrayList<>());
        startingRecipes   = getStringList(data, "startingRecipes", new ArrayList<>());
        startingWeapons   = getStringList(data, "startingWeapons", new ArrayList<>());
        startingArmor     = getString(data, "startingArmor", "leather");
        recipeOrder = getStringList(data, "recipeOrder", new ArrayList<>());
    }

    public void distribute() {
        HoneySuckle.config  = this;
        Player.config       = this;
        World.config        = this;
        Entity.config       = this;
        Brain.config        = this;
        Projectile.config   = this;
        Tile.config         = this;
        Biome.config        = this;
        WorldObject.config  = this;
        Navigator.config    = this;
        Rendering.config    = this;
        Menu.config         = this;
        Splash.config       = this;
        Attack.config       = this;
        Weapon.config       = this;
        Armor.config        = this;
        Armory.config       = this;
        KeyItem.config      = this;
        Ammo.config         = this;
        Craft.config        = this;
        Inventory.config    = this;
        Blueprint.config    = this;
        Build.config        = this;
        Effect.config       = this;
        InputHandler.config = this;
        AssetManager.config = this;
    }

    private static int getInt(Map<String, Object> data, String key, int defaultValue) {
        Object val = data.get(key);
        return val instanceof Number ? ((Number) val).intValue() : defaultValue;
    }

    private static double getDouble(Map<String, Object> data, String key, double defaultValue) {
        Object val = data.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : defaultValue;
    }

    private static String getString(Map<String, Object> data, String key, String defaultValue) {
        final Object val = data.get(key);
        return val instanceof String ? (String) val : defaultValue;
    }

    @SuppressWarnings("unchecked")
	private static List<String> getStringList(Map<String, Object> data, String key, List<String> defaultValue) {
        final Object val = data.get(key);
        if (val instanceof List) {
            final List<?> list = (List<?>) val;
            if (!list.isEmpty() && list.get(0) instanceof String) {
                return (List<String>) list;
            }
        }
        return defaultValue;
    }
}
