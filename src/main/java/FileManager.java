
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

class FileManager {

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
            Map<String, Object> objData = readJsonDirectory(FileManager.class.getResource("jsonData/objects").toURI());
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
            Map<String, Object> tileData = readJsonDirectory(FileManager.class.getResource("jsonData/tiles").toURI());
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
            Map<String, Object> blueprintData = readJsonDirectory(FileManager.class.getResource("jsonData/blueprints").toURI());
            for (String key : blueprintData.keySet()) {
                Map<String, Object> blueprint = (Map<String, Object>) blueprintData.get(key);
                Build.blueprintMats.put(key, (List<Map<String, Number>>) blueprint.getOrDefault("mats", new ArrayList<>()));
                Build.blueprintParams.put(key, (Map<String, List<Number>>) blueprint.getOrDefault("params", new HashMap<>()));
                Build.blueprintTextures.put(key, (Map<String, String>) blueprint.getOrDefault("texture", new HashMap<>()));
                Build.blueprintProducts.put(key, (Integer) blueprint.getOrDefault("product", 0));
                Build.blueprintTags.put(key, (List<String>) blueprint.getOrDefault("tags", new ArrayList<>()));
            }

            //Maps recipe data
            Map<String, Object> recipeData = readJsonDirectory(FileManager.class.getResource("jsonData/recipes").toURI());
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
            Map<String, Object> itemData = readJsonDirectory(FileManager.class.getResource("jsonData/items").toURI());
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

            //Maps biome data
            Map<String, Object> biomeData = readJsonDirectory(FileManager.class.getResource("jsonData/biomes").toURI());
            for (String key : biomeData.keySet()) {
                Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biomeColorMap.put(key, (Map<String, String>) biome.getOrDefault("colorMap", new HashMap<>()));
                Biome.biomeTags.put(key, (List<String>) biome.getOrDefault("tags", new ArrayList<>()));
                Biome.biomeGeneration.put(key, (Map<String, Object>) biome.getOrDefault("generation", new HashMap<>()));
                Biome.biomeLevel.put(key, (Integer) biome.getOrDefault("level", 1));
            }

            //Maps structure data
            Map<String, Object> structureData = readJsonDirectory(FileManager.class.getResource("jsonData/structures").toURI());
            for (String key : structureData.keySet()) {
                final Map<String, Object> structure = (Map<String, Object>) structureData.get(key);
                Biome.structureName.put(key, (String) structure.getOrDefault("name", key));
                Biome.structureGeneration.put(key, (Map<String, Object>) structure.getOrDefault("generation", new HashMap<>()));

                final int id = (int) structure.get("id");
                Biome.structureIntId.put(key, id);
                Biome.structureStringId.put(id, key);
            }

            //Maps entity data
            Map<String, Object> entityData = readJsonDirectory(FileManager.class.getResource("jsonData/entities").toURI());
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
            Map<String, Object> weaponData = readJsonDirectory(FileManager.class.getResource("jsonData/weapons").toURI());
            for (String key : weaponData.keySet()) {
                Map<String, Object> weapon = (Map<String, Object>) weaponData.get(key);
                Weapon.weaponAttributes.put(key, (Map<String, Number>) weapon.getOrDefault("attributes", new HashMap<>()));
                Weapon.weaponStats.put(key, (Map<String, String>) weapon.getOrDefault("stats", new HashMap<>()));
                Weapon.weaponBehaviors.put(key, (Map<String, Map<String, Object>>) weapon.getOrDefault("behavior", new HashMap<>()));
                Weapon.weaponTags.put(key, (List<String>) weapon.getOrDefault("tags", new ArrayList<>()));
                Weapon.weaponTextures.put(key, (Map<String, String>) weapon.getOrDefault("texture", new HashMap<>()));
                Weapon.weaponNames.put(key, (String) weapon.getOrDefault("name", key));

                final int id = (int) weapon.get("id");
                Weapon.weaponIntId.put(key, id);
                Weapon.weaponStringId.put(id, key);
            }

            //Maps armor data
            Map<String, Object> armorData = readJsonDirectory(FileManager.class.getResource("jsonData/armor").toURI());
            for (String key : armorData.keySet()) {
                Map<String, Object> armor = (Map<String, Object>) armorData.get(key);
                Armor.armorTextures.put(key, (Map<String, String>) armor.getOrDefault("texture", new HashMap<>()));
                Armor.armorAttributes.put(key, (Map<String, Number>) armor.getOrDefault("attributes", new HashMap<>()));
                Armor.armorStats.put(key, (Map<String, String>) armor.getOrDefault("stats", new HashMap<>()));
                Armor.armorNames.put(key, (String) armor.getOrDefault("name", key));

                final int id = (int) armor.get("id");
                Armor.armorIntId.put(key, id);
                Armor.armorStrignId.put(id, key);
            }

            //Maps Projectile data
            Map<String, Object> projData = readJsonDirectory(FileManager.class.getResource("jsonData/projectiles").toURI());
            for (String key : projData.keySet()) {
                Map<String, Object> proj = (Map<String, Object>) projData.get(key);
                Projectile.projAttributes.put(key, (Map<String, Number>) proj.getOrDefault("attributes", new HashMap<>()));
                Projectile.projTextures.put(key, (Map<String, String>) proj.getOrDefault("texture", new HashMap<>()));
                Projectile.projTags.put(key, (List<String>) proj.getOrDefault("tags", new ArrayList<>()));

                final int id = (int) proj.get("id");
                Projectile.projIntId.put(key, id);
                Projectile.projStringId.put(id, key);
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

    public static void registerFont() {
        try {
            final File fontFile = new File(FileManager.class.getResource("fonts/VT323/VT323-Regular.ttf").toURI());

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
