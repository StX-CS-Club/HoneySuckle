
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
                int intKey = Integer.parseInt(key);
                Map<String, Object> obj = (Map<String, Object>) objData.get(key);
                WorldObject.objLoot.put(intKey, (List<Map<String, Number>>) obj.get("loot"));
                WorldObject.objTextures.put(intKey, (Map<String, String>) obj.get("texture"));
                WorldObject.objValues.put(intKey, (Map<String, Double>) obj.get("values"));
                WorldObject.objTags.put(intKey, (List<String>) obj.get("tags"));
            }

            //Maps tile data
            Map<String, Object> tileData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/tile.json").toURI()), mapType);
            for (String key : tileData.keySet()) {
                int intKey = Integer.parseInt(key);
                Map<String, Object> tile = (Map<String, Object>) tileData.get(key);
                Tile.tileTextures.put(intKey, (Map<String, String>) tile.get("texture"));
                Tile.tileValues.put(intKey, (Map<String, Double>) tile.get("values"));
                Tile.tileTags.put(intKey, (List<String>) tile.get("tags"));
            }

            //Maps recipe data
            Map<String, Object> blueprintData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/blueprint.json").toURI()), mapType);
            for (String key : blueprintData.keySet()) {
                int intKey = Integer.parseInt(key);
                Map<String, Object> recipe = (Map<String, Object>) blueprintData.get(key);
                Build.blueprintMats.put(intKey, (List<Map<String, Integer>>) recipe.get("mats"));
                Build.blueprintParams.put(intKey, (Map<String, List<Integer>>) recipe.get("params"));
                Build.blueprintTextures.put(intKey, (Map<String, String>) recipe.get("texture"));
            }

            //Maps item data
            Map<String, Object> itemData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/item.json").toURI()), mapType);
            for (String key : itemData.keySet()) {
                final Map<String, Object> item = (Map<String, Object>) itemData.get(key);
                Inventory.itemNames.put(key, (String) item.get("name"));
                Inventory.itemTextures.put(key, (Map<String, String>) item.get("texture"));

                final int id = (int) item.get("id");
                Inventory.itemIntId.put(key, id);
                Inventory.itemStringId.put(id, key);
            }

            //Maps biome data
            Map<String, Object> biomeData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/biome.json").toURI()), mapType);
            for (String key : biomeData.keySet()) {
                Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biomeColorMap.put(key, (Map<String, String>) biome.get("colorMap"));
                Biome.biomeTags.put(key, (List<String>) biome.get("tags"));
                Biome.biomeGeneration.put(key, (Map<String, Object>) biome.get("generation"));
            }

            //Maps structure data
            Map<String, Object> structureData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/structure.json").toURI()), mapType);
            for (String key : structureData.keySet()) {
                final Map<String, Object> structure = (Map<String, Object>) structureData.get(key);
                Biome.structureName.put(key, (String) structure.get("name"));
                Biome.structureGeneration.put(key, (Map<String, Object>) structure.get("generation"));

                final int id = (int) structure.get("id");
                Biome.structureIntId.put(key, id);
                Biome.structureStringId.put(id, key);
            }

            //Maps entity data
            Map<String, Object> entityData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/entity.json").toURI()), mapType);
            for (String key : entityData.keySet()) {
                Map<String, Object> entity = (Map<String, Object>) entityData.get(key);
                Entity.entityAttributes.put(key, (Map<String, Double>) entity.get("attributes"));
                Entity.entityTextures.put(key, (Map<String, String>) entity.get("texture"));
                Entity.entityLoot.put(key, (List<Map<String, Integer>>) entity.get("loot"));
                Entity.entityTags.put(key, (List<String>) entity.get("tags"));

                final int id = (int) entity.get("id");
                Entity.entityIntId.put(key, id);
                Entity.entityStringId.put(id, key);
            }

            //Maps weapon data
            Map<String, Object> weaponData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/weapon.json").toURI()), mapType);
            for (String key : weaponData.keySet()) {
                Map<String, Object> weapon = (Map<String, Object>) weaponData.get(key);
                Weapon.weaponAttributes.put(key, (Map<String, Double>) weapon.get("attributes"));
                Weapon.weaponStats.put(key, (Map<String, String>) weapon.get("stats"));
                Weapon.weaponTypes.put(key, (String) weapon.get("type"));
                Weapon.weaponProj.put(key, (String) weapon.get("projectile"));
                Weapon.weaponTags.put(key, (List<String>) weapon.get("tags"));
                Weapon.weaponTextures.put(key, (Map<String, String>) weapon.get("texture"));
            }

            //Maps armor data
            Map<String, Object> armorData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/armor.json").toURI()), mapType);
            for (String key : armorData.keySet()) {
                Map<String, Object> armor = (Map<String, Object>) armorData.get(key);
                Armor.armorTextures.put(key, (Map<String, String>) armor.get("texture"));
                Armor.armorAttributes.put(key, (Map<String, Double>) armor.get("attributes"));
            }

            //Maps Projectile data
            Map<String, Object> projData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/projectile.json").toURI()), mapType);
            for (String key : projData.keySet()) {
                Map<String, Object> proj = (Map<String, Object>) projData.get(key);
                Projectile.projAttributes.put(key, (Map<String, Double>) proj.get("attributes"));
                Projectile.projTextures.put(key, (Map<String, String>) proj.get("texture"));
                Projectile.projTags.put(key, (List<String>) proj.get("tags"));
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
