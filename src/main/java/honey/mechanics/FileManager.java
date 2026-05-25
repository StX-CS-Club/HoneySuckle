package honey.mechanics;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import honey.player.Player;
import honey.player.armory.Ammo;
import honey.rendering.Rendering;
import honey.player.armory.Armor;
import honey.player.armory.Effect;
import honey.player.armory.Weapon;
import honey.player.build.Blueprint;
import honey.player.inventory.Craft;
import honey.player.inventory.Item;
import honey.player.inventory.KeyItem;
import honey.world.Biome;
import honey.world.Brain;
import honey.world.Entity;
import honey.world.Projectile;
import honey.world.Structure;
import honey.world.Tile;
import honey.world.WorldObject;

public class FileManager {

    //Object used for referencing json files
    private static final ObjectMapper objectMapper = new ObjectMapper();
    //Type reference of json files
    private static final TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
    };

    //Fetches data from json files and maps as hashmaps
    @SuppressWarnings("unchecked")
    public static void readJsonData() {
        try {
            //Maps object data
            Map<String, Object> objData = readJsonDirectory(FileManager.class.getResource("/jsonData/objects").toURI());
            for (String key : objData.keySet()) {
                Map<String, Object> obj = (Map<String, Object>) objData.get(key);
                int intKey = (Integer) obj.get("id");
                WorldObject.objLoot.put(intKey, (List<Map<String, Number>>) obj.getOrDefault("loot", new ArrayList<>()));
                WorldObject.objTextures.put(intKey, (Map<String, String>) obj.getOrDefault("texture", new HashMap<>()));
                WorldObject.objAttributes.put(intKey, (Map<String, Number>) obj.getOrDefault("attributes", new HashMap<>()));
                WorldObject.objTags.put(intKey, (List<String>) obj.getOrDefault("tags", new ArrayList<>()));
                WorldObject.objIntIds.put(key, intKey);
                WorldObject.objStringIds.put(intKey, key);
            }

            //Maps tile data
            Map<String, Object> tileData = readJsonDirectory(FileManager.class.getResource("/jsonData/tiles").toURI());
            for (String key : tileData.keySet()) {
                Map<String, Object> tile = (Map<String, Object>) tileData.get(key);
                int intKey = (Integer) tile.get("id");
                Tile.tileTextures.put(intKey, (Map<String, String>) tile.getOrDefault("texture", new HashMap<>()));
                Tile.tileAttributes.put(intKey, (Map<String, Number>) tile.getOrDefault("attributes", new HashMap<>()));
                Tile.tileTags.put(intKey, (List<String>) tile.getOrDefault("tags", new ArrayList<>()));
                Tile.tileIntIds.put(key, intKey);
                Tile.tileStringIds.put(intKey, key);
            }

            //Maps blueprint data
            Map<String, Object> blueprintData = readJsonDirectory(FileManager.class.getResource("/jsonData/blueprints").toURI());
            for (String key : blueprintData.keySet()) {
                Map<String, Object> blueprint = (Map<String, Object>) blueprintData.get(key);
                Blueprint.blueprintMats.put(key, (List<Map<String, Number>>) blueprint.getOrDefault("mats", new ArrayList<>()));
                Blueprint.blueprintParams.put(key, (Map<String, List<Number>>) blueprint.getOrDefault("params", new HashMap<>()));
                Blueprint.blueprintTextures.put(key, (Map<String, String>) blueprint.getOrDefault("texture", new HashMap<>()));
                Blueprint.blueprintProducts.put(key, (Integer) blueprint.getOrDefault("product", 0));
                Blueprint.blueprintTags.put(key, (List<String>) blueprint.getOrDefault("tags", new ArrayList<>()));
            }

            //Maps recipe data
            Map<String, Object> recipeData = readJsonDirectory(FileManager.class.getResource("/jsonData/recipes").toURI());
            for (String key : recipeData.keySet()) {
                Map<String, Object> recipe = (Map<String, Object>) recipeData.get(key);
                Craft.recipeMats.put(key, (List<Map<String, Number>>) recipe.getOrDefault("mats", new ArrayList<>()));
                Craft.recipeAttributes.put(key, (Map<String, Number>) recipe.getOrDefault("attributes", new HashMap<>()));
                Craft.recipeTextures.put(key, (Map<String, String>) recipe.getOrDefault("texture", new HashMap<>()));
                Craft.recipeTypes.put(key, (String) recipe.getOrDefault("type", "item"));
                Craft.recipeNames.put(key, (String) recipe.getOrDefault("name", key));
                Craft.recipeProducts.put(key, (List<Map<String, Number>>) recipe.getOrDefault("products", new ArrayList<>()));
            }

            //Maps item data
            Map<String, Object> itemData = readJsonDirectory(FileManager.class.getResource("/jsonData/items").toURI());
            for (String key : itemData.keySet()) {
                final Map<String, Object> item = (Map<String, Object>) itemData.get(key);
                Item.itemNames.put(key, (String) item.getOrDefault("name", key));
                Item.itemTextures.put(key, (Map<String, String>) item.getOrDefault("texture", new HashMap<>()));
                Item.itemBlueprintUnlocks.put(key, (List<String>) item.getOrDefault("blueprintUnlocks", new ArrayList<>()));
                Item.itemRecipeUnlocks.put(key, (List<String>) item.getOrDefault("recipeUnlocks", new ArrayList<>()));
                Item.itemAttributes.put(key, (Map<String, Number>) item.getOrDefault("attributes", new HashMap<>()));

                final int id = (int) item.get("id");
                Item.itemIntId.put(key, id);
                Item.itemStringId.put(id, key);
            }
            

            //Maps item data
            Map<String, Object> keyData = readJsonDirectory(FileManager.class.getResource("/jsonData/key_items").toURI());
            for (String key : keyData.keySet()) {
                final Map<String, Object> keyItem = (Map<String, Object>) keyData.get(key);
                KeyItem.keyNames.put(key, (String) keyItem.getOrDefault("name", key));
                KeyItem.keyTextures.put(key, (Map<String, String>) keyItem.getOrDefault("texture", new HashMap<>()));
                KeyItem.keyUtilities.put(key, (Map<String, Map<String, Object>>) keyItem.getOrDefault("utilities", new HashMap<>()));
                KeyItem.keyBlueprintUnlocks.put(key, (List<String>) keyItem.getOrDefault("blueprintUnlocks", new ArrayList<>()));
                KeyItem.keyRecipeUnlocks.put(key, (List<String>) keyItem.getOrDefault("recipeUnlocks", new ArrayList<>()));
                KeyItem.keyAttributes.put(key, (Map<String, Number>) keyItem.getOrDefault("attributes", new HashMap<>()));

                final int id = (int) keyItem.get("id");
                KeyItem.keyIntId.put(key, id);
                KeyItem.keyStringId.put(id, key);
            }

            //Maps biome data
            Map<String, Object> biomeData = readJsonDirectory(FileManager.class.getResource("/jsonData/biomes").toURI());
            for (String key : biomeData.keySet()) {
                Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biometextureMap.put(key, (Map<String, String>) biome.getOrDefault("textureMap", new HashMap<>()));
                Biome.biomeTags.put(key, (List<String>) biome.getOrDefault("tags", new ArrayList<>()));
                Biome.biomeAttributes.put(key, (Map<String, Number>) biome.getOrDefault("attributes", new HashMap<>()));
                Biome.biomeGeneration.put(key, (Map<String, Object>) biome.getOrDefault("generation", new HashMap<>()));
                Biome.biomeLevel.put(key, (Integer) biome.getOrDefault("level", 1));
            }

            //Maps structure data
            Map<String, Object> structureData = readJsonDirectory(FileManager.class.getResource("/jsonData/structures").toURI());
            for (String key : structureData.keySet()) {
                final Map<String, Object> structure = (Map<String, Object>) structureData.get(key);
                Structure.structureName.put(key, (String) structure.getOrDefault("name", key));
                Structure.structureGeneration.put(key, (Map<String, Object>) structure.getOrDefault("generation", new HashMap<>()));
                Structure.structureTextures.put(key, (Map<String, String>) structure.getOrDefault("texture", new HashMap<>()));
                Structure.structureAttributes.put(key, (Map<String, Number>) structure.getOrDefault("attributes", new HashMap<>()));

                final int id = (int) structure.get("id");
                Structure.structureIntId.put(key, id);
                Structure.structureStringId.put(id, key);
            }

            //Maps entity data
            Map<String, Object> entityData = readJsonDirectory(FileManager.class.getResource("/jsonData/entities").toURI());
            for (String key : entityData.keySet()) {
                Map<String, Object> entity = (Map<String, Object>) entityData.get(key);
                Entity.entityAttributes.put(key, (Map<String, Number>) entity.getOrDefault("attributes", new HashMap<>()));
                Entity.entityTextures.put(key, (Map<String, String>) entity.getOrDefault("texture", new HashMap<>()));
                Entity.entityLoot.put(key, (List<Map<String, Number>>) entity.getOrDefault("loot", new ArrayList<>()));
                Entity.entityTags.put(key, (List<String>) entity.getOrDefault("tags", new ArrayList<>()));
                Entity.entityNames.put(key, (String) entity.getOrDefault("name", key));
                Brain.entityBrain.put(key, (Map<String, Map<String, Object>>) entity.getOrDefault("brain", new HashMap<>()));

                final int id = (int) entity.get("id");
                Entity.entityIntId.put(key, id);
                Entity.entityStringId.put(id, key);
            }

            //Maps weapon data
            Map<String, Object> weaponData = readJsonDirectory(FileManager.class.getResource("/jsonData/weapons").toURI());
            for (String key : weaponData.keySet()) {
                Map<String, Object> weapon = (Map<String, Object>) weaponData.get(key);
                Weapon.weaponAttributes.put(key, (Map<String, Number>) weapon.getOrDefault("attributes", new HashMap<>()));
                Weapon.weaponAmmo.put(key, (List<String>) weapon.getOrDefault("ammo", new ArrayList<>()));
                Weapon.weaponStats.put(key, (Map<String, String>) weapon.getOrDefault("stats", new HashMap<>()));
                Weapon.weaponBehaviors.put(key, (Map<String, Map<String, Object>>) weapon.getOrDefault("behavior", new HashMap<>()));
                Weapon.weaponTags.put(key, (List<String>) weapon.getOrDefault("tags", new ArrayList<>()));
                Weapon.weaponRecipeUnlocks.put(key, (List<String>) weapon.getOrDefault("recipeUnlocks", new ArrayList<>()));
                Weapon.weaponBlueprintUnlocks.put(key, (List<String>) weapon.getOrDefault("blueprintUnlocks", new ArrayList<>()));
                Weapon.weaponTextures.put(key, (Map<String, String>) weapon.getOrDefault("texture", new HashMap<>()));
                Weapon.weaponNames.put(key, (String) weapon.getOrDefault("name", key));

                final int id = (int) weapon.get("id");
                Weapon.weaponIntId.put(key, id);
                Weapon.weaponStringId.put(id, key);
            }

            //Maps ammo data
            Map<String, Object> ammoData = readJsonDirectory(FileManager.class.getResource("/jsonData/ammo").toURI());
            for (String key : ammoData.keySet()) {
                Map<String, Object> ammo = (Map<String, Object>) ammoData.get(key);

                Ammo.ammoAttributes.put(key, (Map<String, Number>) ammo.getOrDefault("attributes", new HashMap<>()));
                Ammo.ammoNames.put(key, (String) ammo.getOrDefault("name", key));
                Ammo.ammoStats.put(key, (Map<String, String>) ammo.getOrDefault("stats", new HashMap<>()));
                Ammo.ammoTextures.put(key, (Map<String, String>) ammo.getOrDefault("texture", new HashMap<>()));
                Ammo.ammoTypes.put(key, (List<String>) ammo.getOrDefault("types", new ArrayList<>()));
                Ammo.ammoRecipeUnlocks.put(key, (List<String>) ammo.getOrDefault("recipeUnlocks", new ArrayList<>()));
                Ammo.ammoBlueprintUnlocks.put(key, (List<String>) ammo.getOrDefault("blueprintUnlocks", new ArrayList<>()));

                final int id = (int) ammo.get("id");
                Ammo.ammoIntId.put(key, id);
                Ammo.ammoStringId.put(id, key);
            }

            //Maps armor data
            Map<String, Object> armorData = readJsonDirectory(FileManager.class.getResource("/jsonData/armor").toURI());
            for (String key : armorData.keySet()) {
                Map<String, Object> armor = (Map<String, Object>) armorData.get(key);
                Armor.armorTextures.put(key, (Map<String, String>) armor.getOrDefault("texture", new HashMap<>()));
                Armor.armorAttributes.put(key, (Map<String, Number>) armor.getOrDefault("attributes", new HashMap<>()));
                Armor.armorRecipeUnlocks.put(key, (List<String>) armor.getOrDefault("recipeUnlocks", new ArrayList<>()));
                Armor.armorBlueprintUnlocks.put(key, (List<String>) armor.getOrDefault("blueprintUnlocks", new ArrayList<>()));
                Armor.armorStats.put(key, (Map<String, String>) armor.getOrDefault("stats", new HashMap<>()));
                Armor.armorNames.put(key, (String) armor.getOrDefault("name", key));

                final int id = (int) armor.get("id");
                Armor.armorIntId.put(key, id);
                Armor.armorStringId.put(id, key);
            }
            Player.playerDefaultAttributes.putAll(Armor.armorAttributes.get("naked"));

            //Maps Projectile data
            Map<String, Object> projData = readJsonDirectory(FileManager.class.getResource("/jsonData/projectiles").toURI());
            for (String key : projData.keySet()) {
                Map<String, Object> proj = (Map<String, Object>) projData.get(key);
                Projectile.projAttributes.put(key, (Map<String, Number>) proj.getOrDefault("attributes", new HashMap<>()));
                Projectile.projTextures.put(key, (Map<String, String>) proj.getOrDefault("texture", new HashMap<>()));
                Projectile.projSplinters.put(key, (List<Map<String, Number>>) proj.getOrDefault("splinters", new ArrayList<>()));
                Projectile.projTags.put(key, (List<String>) proj.getOrDefault("tags", new ArrayList<>()));

                final int id = (int) proj.get("id");
                Projectile.projIntId.put(key, id);
                Projectile.projStringId.put(id, key);
            }

            //Maps Effect data
            Map<String, Object> effectData = readJsonDirectory(FileManager.class.getResource("/jsonData/effects").toURI());
            for (String key : effectData.keySet()) {
                Map<String, Object> effect = (Map<String, Object>) effectData.get(key);
                Effect.effectNames.put(key, (String) effect.getOrDefault("name", key));
                Effect.effectTextures.put(key, (Map<String, String>) effect.getOrDefault("texture", new HashMap<>()));
                Effect.effectModifiers.put(key, (Map<String, Number>) effect.getOrDefault("modifiers", new HashMap<>()));
                Effect.effectTags.put(key, (List<String>) effect.getOrDefault("tags", new ArrayList<>()));

                final int id = (int) effect.get("id");
                Effect.effectIntId.put(key, id);
                Effect.effectStringId.put(id, key);
            }
        } catch (IOException e) {
            System.out.println("FileManager ERROR: Failed to import json files.");
        } catch (URISyntaxException e) {
            System.out.println("FileManager ERROR: Could not find json files.");
        }
    }

    private static Map<String, Object> readJsonDirectory(URI directory) throws IOException {
        Map<String, Object> result = new HashMap<>();
        File directoryFile = new File(directory);
        for (File file : directoryFile.listFiles()) {
            if (file.isDirectory()) {
                result.putAll(readJsonDirectory(file.toURI()));
            } else {
                if (file.toURI().toString().contains(".json")) {
                    result.putAll(objectMapper.readValue(file, mapType));
                }
            }
        }
        return result;
    }

    public static void preloadImages() {
        // Entities: all animation variants, respecting natColor per biome
        for (Map<String, String> tex : Entity.entityTextures.values()) {
            String base = tex.get("texture");
            if (base == null) continue;
            String anim = tex.getOrDefault("anim", "");
            String natColorId = tex.get("natColor");
            if (natColorId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    String color = biomeColors.get(natColorId);
                    if (color != null) {
                        for (String suffix : animImageSuffixes(anim)) {
                            Rendering.texture(base + suffix, color);
                        }
                    }
                }
            } else {
                String color = tex.get("baseColor");
                for (String suffix : animImageSuffixes(anim)) {
                    Rendering.texture(base + suffix, color);
                }
            }
        }

        // Projectiles
        for (Map<String, String> tex : Projectile.projTextures.values()) {
            String base = tex.get("texture");
            if (base != null) Rendering.texture(base, null);
        }

        // Weapons: item icon + attack GIFs per swing color
        for (Map<String, String> tex : Weapon.weaponTextures.values()) {
            String itemTex = tex.get("itemTexture");
            if (itemTex != null) Rendering.texture(itemTex, "#e8f1ff");
            String swingColor = tex.get("swingColor");
            if (swingColor != null) {
                Rendering.renderGIF("attacks/slash", swingColor, 0.0);
                Rendering.renderGIF("attacks/stab", swingColor, 0.0);
            }
        }

        // Armor front/back textures
        for (Map<String, String> tex : Armor.armorTextures.values()) {
            String front = tex.get("front");
            String back = tex.get("back");
            if (front != null) Rendering.texture(front, null);
            if (back != null) Rendering.texture(back, null);
        }

        // Items, ammo, key items, effects
        for (Map<String, String> tex : Item.itemTextures.values()) {
            String t = tex.get("texture");
            if (t != null) Rendering.texture(t, null);
        }
        for (Map<String, String> tex : Ammo.ammoTextures.values()) {
            String t = tex.get("itemTexture");
            if (t != null) Rendering.texture(t, null);
        }
        for (Map<String, String> tex : KeyItem.keyTextures.values()) {
            String t = tex.get("texture");
            if (t != null) Rendering.texture(t, null);
        }
        for (Map<String, String> tex : Effect.effectTextures.values()) {
            String t = tex.get("texture");
            if (t != null) Rendering.texture(t, null);
        }

        // Tiles: edge textures only, respecting natColorEdge per biome
        for (Map<String, String> tex : Tile.tileTextures.values()) {
            final String edgeTex = tex.get("textureEdge");
            if (edgeTex == null) continue;
            final String natColorEdgeId = tex.get("natColorEdge");
            if (natColorEdgeId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorEdgeId);
                    if (color != null) Rendering.texture(edgeTex, color);
                }
            } else {
                Rendering.texture(edgeTex, tex.get("baseColorEdge"));
            }
        }
    }

    // Returns all possible image filename suffix combinations for a given animation string,
    // matching the order Entity.render() appends them (direction, then each state in sequence).
    // Uses a power set of states so multi-state combos like _chase_shoot are included.
    private static List<String> animImageSuffixes(String anim) {
        List<String> dirs;
        if (anim.contains("_xy_")) {
            dirs = List.of("_up", "_right", "_down", "_left");
        } else if (anim.contains("_x_")) {
            dirs = List.of("_left", "_right");
        } else if (anim.contains("_y_")) {
            dirs = List.of("_up", "_down");
        } else {
            dirs = List.of("");
        }

        // States in the same order Entity.render() appends them
        final List<String> states = new ArrayList<>();
        if (anim.contains("_chase_"))    states.add("_chase");
        if (anim.contains("_hesitate_")) states.add("_hesitate");
        if (anim.contains("_shoot_"))    states.add("_shoot");

        // Power set of state combos (2^n entries, including the empty "no state" combo)
        final int n = states.size();
        final List<String> stateCombos = new ArrayList<>(1 << n);
        for (int mask = 0; mask < (1 << n); mask++) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) sb.append(states.get(i));
            }
            stateCombos.add(sb.toString());
        }

        final List<String> result = new ArrayList<>(dirs.size() * stateCombos.size());
        for (String dir : dirs) {
            for (String stateCombo : stateCombos) {
                result.add(dir + stateCombo);
            }
        }
        return result;
    }

    public static void registerFont() {
        try {
            final File fontFile = new File(FileManager.class.getResource("/fonts/VT323/VT323-Regular.ttf").toURI());

            final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            final Font sizedFont = font.deriveFont(Font.PLAIN, 24f);

            // Registers font globally under "VT323 Regular"
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(sizedFont);
        } catch (FontFormatException e) {
            System.out.println("FileManager ERROR: Failed to format text font.");
        } catch (IOException e) {
            System.out.println("FileManager ERROR: Failed to import text font file.");
        } catch (URISyntaxException e) {
            System.out.println("FileManager ERROR: Could not find text font file.");
        }
    }
}
