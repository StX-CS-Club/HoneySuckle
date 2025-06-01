
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class FileManager {

    //Fetches data from json files and maps as hashmaps
    @SuppressWarnings("unchecked")
    public static void readJsonData() {
        try {
            //Object used for referencing json files
            ObjectMapper objectMapper = new ObjectMapper();
            //Type reference of json files
            TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
            };

            //Maps object data
            Map<String, Object> objData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/object.json").toURI()), mapType);
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
            Map<String, Object> tileData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/tile.json").toURI()), mapType);
            for (String key : tileData.keySet()) {
                Map<String, Object> tile = (Map<String, Object>) tileData.get(key);
                int intKey = (Integer) tile.get("id");
                Tile.tileTextures.put(intKey, (Map<String, String>) tile.getOrDefault("texture", new HashMap<>()));
                Tile.tileAttributes.put(intKey, (Map<String, Number>) tile.getOrDefault("attributes", new HashMap<>()));
                Tile.tileTags.put(intKey, (List<String>) tile.getOrDefault("tags", new ArrayList<>()));
                Tile.tileIntIds.put(key, intKey);
                Tile.tileStringIds.put(intKey, key);
            }

            //Maps recipe data
            Map<String, Object> blueprintData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/blueprint.json").toURI()), mapType);
            for (String key : blueprintData.keySet()) {
                Map<String, Object> recipe = (Map<String, Object>) blueprintData.get(key);
                Build.blueprintMats.put(key, (List<Map<String, Integer>>) recipe.getOrDefault("mats", new ArrayList<>()));
                Build.blueprintParams.put(key, (Map<String, List<Integer>>) recipe.getOrDefault("params", new HashMap<>()));
                Build.blueprintTextures.put(key, (Map<String, String>) recipe.getOrDefault("texture", new HashMap<>()));
                Build.blueprintProducts.put(key, (Integer) recipe.getOrDefault("product", 0));
            }

            //Maps item data
            Map<String, Object> itemData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/item.json").toURI()), mapType);
            for (String key : itemData.keySet()) {
                final Map<String, Object> item = (Map<String, Object>) itemData.get(key);
                Inventory.itemNames.put(key, (String) item.getOrDefault("name", ""));
                Inventory.itemTextures.put(key, (Map<String, String>) item.getOrDefault("texture", new HashMap<>()));
                Inventory.itemRecipeUnlocks.put(key, (List<String>) item.getOrDefault("recipeUnlocks", new ArrayList<>()));

                final int id = (int) item.get("id");
                Inventory.itemIntId.put(key, id);
                Inventory.itemStringId.put(id, key);
            }

            //Maps biome data
            Map<String, Object> biomeData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/biome.json").toURI()), mapType);
            for (String key : biomeData.keySet()) {
                Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biomeColorMap.put(key, (Map<String, String>) biome.getOrDefault("colorMap", new HashMap<>()));
                Biome.biomeTags.put(key, (List<String>) biome.getOrDefault("tags", new ArrayList<>()));
                Biome.biomeGeneration.put(key, (Map<String, Object>) biome.getOrDefault("generation", new HashMap<>()));
                Biome.biomeLevel.put(key, (Integer) biome.getOrDefault("level", 1));
            }

            //Maps structure data
            Map<String, Object> structureData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/structure.json").toURI()), mapType);
            for (String key : structureData.keySet()) {
                final Map<String, Object> structure = (Map<String, Object>) structureData.get(key);
                Biome.structureName.put(key, (String) structure.getOrDefault("name", new HashMap<>()));
                Biome.structureGeneration.put(key, (Map<String, Object>) structure.getOrDefault("generation", new HashMap<>()));

                final int id = (int) structure.get("id");
                Biome.structureIntId.put(key, id);
                Biome.structureStringId.put(id, key);
            }

            //Maps entity data
            Map<String, Object> entityData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/entity.json").toURI()), mapType);
            for (String key : entityData.keySet()) {
                Map<String, Object> entity = (Map<String, Object>) entityData.get(key);
                Entity.entityAttributes.put(key, (Map<String, Number>) entity.getOrDefault("attributes", new HashMap<>()));
                Entity.entityTextures.put(key, (Map<String, String>) entity.getOrDefault("texture", new HashMap<>()));
                Entity.entityLoot.put(key, (List<Map<String, Number>>) entity.getOrDefault("loot", new ArrayList<>()));
                Entity.entityTags.put(key, (List<String>) entity.getOrDefault("tags", new ArrayList<>()));
                Brain.entityBrain.put(key, (Map<String, Object>) entity.getOrDefault("brain", new HashMap<>()));

                final int id = (int) entity.get("id");
                Entity.entityIntId.put(key, id);
                Entity.entityStringId.put(id, key);
            }

            //Maps weapon data
            Map<String, Object> weaponData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/weapon.json").toURI()), mapType);
            for (String key : weaponData.keySet()) {
                Map<String, Object> weapon = (Map<String, Object>) weaponData.get(key);
                Weapon.weaponAttributes.put(key, (Map<String, Double>) weapon.getOrDefault("attributes", new HashMap<>()));
                Weapon.weaponStats.put(key, (Map<String, String>) weapon.getOrDefault("stats", new HashMap<>()));
                Weapon.weaponTypes.put(key, (String) weapon.getOrDefault("type", "blade"));
                Weapon.weaponProj.put(key, (String) weapon.getOrDefault("projectile", "arrow"));
                Weapon.weaponTags.put(key, (List<String>) weapon.getOrDefault("tags", new ArrayList<>()));
                Weapon.weaponTextures.put(key, (Map<String, String>) weapon.getOrDefault("texture", new HashMap<>()));
            }

            //Maps armor data
            Map<String, Object> armorData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/armor.json").toURI()), mapType);
            for (String key : armorData.keySet()) {
                Map<String, Object> armor = (Map<String, Object>) armorData.get(key);
                Armor.armorTextures.put(key, (Map<String, String>) armor.getOrDefault("texture", new HashMap<>()));
                Armor.armorAttributes.put(key, (Map<String, Double>) armor.getOrDefault("attributes", new HashMap<>()));
            }

            //Maps Projectile data
            Map<String, Object> projData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/projectile.json").toURI()), mapType);
            for (String key : projData.keySet()) {
                Map<String, Object> proj = (Map<String, Object>) projData.get(key);
                Projectile.projAttributes.put(key, (Map<String, Double>) proj.getOrDefault("attributes", new HashMap<>()));
                Projectile.projTextures.put(key, (Map<String, String>) proj.getOrDefault("texture", new HashMap<>()));
                Projectile.projTags.put(key, (List<String>) proj.getOrDefault("tags", new ArrayList<>()));

                final int id = (int) proj.get("id");
                Projectile.projIntId.put(key, id);
                Projectile.projStringId.put(id, key);
            }
        } catch (IOException e) {
            System.out.println("HoneySuckle ERROR: Failed to import json files.");
        } catch (URISyntaxException e) {
            System.out.println("HoneySuckle ERROR: Could not find json files.");
        }
    }

    public static void registerFont() {
        try {
            final File fontFile = new File(HoneySuckle.class.getResource("fonts/VT323/VT323-Regular.ttf").toURI());

            final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            final Font sizedFont = font.deriveFont(Font.PLAIN, 24f);

            // Registers font globally under "VT323 Regular"
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(sizedFont);
        } catch (FontFormatException e) {
            System.out.println("HoneySuckle ERROR: Failed to format text font.");
        } catch (IOException e) {
            System.out.println("HoneySuckle ERROR: Failed to import text font file.");
        } catch (URISyntaxException e) {
            System.out.println("HoneySuckle ERROR: Could not find text font file.");
        }
    }
}
