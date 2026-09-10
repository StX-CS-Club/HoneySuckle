package honey.mechanics;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.player.Player;
import honey.player.armory.Ammo;
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

public class DataManager {

    @SuppressWarnings("unchecked")
    public static void readJsonData() {
        try {
            //Maps object data
            final Map<String, Object> objData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/objects").toURI());
            for (String key : objData.keySet()) {
                final Map<String, Object> obj = (Map<String, Object>) objData.get(key);
                final int intKey = (Integer) obj.get("id");
                WorldObject.objLoot.put(intKey, (List<Map<String, Number>>) obj.getOrDefault("loot", new ArrayList<>()));
                WorldObject.objTextures.put(intKey, (Map<String, String>) obj.getOrDefault("texture", new HashMap<>()));
                WorldObject.objAttributes.put(intKey, (Map<String, Number>) obj.getOrDefault("attributes", new HashMap<>()));
                WorldObject.objTags.put(intKey, (List<String>) obj.getOrDefault("tags", new ArrayList<>()));
                WorldObject.objIntIds.put(key, intKey);
                WorldObject.objStringIds.put(intKey, key);
            }

            //Maps tile data
            final Map<String, Object> tileData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/tiles").toURI());
            for (String key : tileData.keySet()) {
                final Map<String, Object> tile = (Map<String, Object>) tileData.get(key);
                final int intKey = (Integer) tile.get("id");
                Tile.tileTextures.put(intKey, (Map<String, String>) tile.getOrDefault("texture", new HashMap<>()));
                Tile.tileAttributes.put(intKey, (Map<String, Number>) tile.getOrDefault("attributes", new HashMap<>()));
                Tile.tileTags.put(intKey, (List<String>) tile.getOrDefault("tags", new ArrayList<>()));
                Tile.tileIntIds.put(key, intKey);
                Tile.tileStringIds.put(intKey, key);
            }

            //Maps blueprint data
            final Map<String, Object> blueprintData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/blueprints").toURI());
            for (String key : blueprintData.keySet()) {
                final Map<String, Object> blueprint = (Map<String, Object>) blueprintData.get(key);
                Blueprint.blueprintMats.put(key, (List<Map<String, Number>>) blueprint.getOrDefault("mats", new ArrayList<>()));
                Blueprint.blueprintParams.put(key, (Map<String, List<Number>>) blueprint.getOrDefault("params", new HashMap<>()));
                Blueprint.blueprintTextures.put(key, (Map<String, String>) blueprint.getOrDefault("texture", new HashMap<>()));
                Blueprint.blueprintProducts.put(key, (Integer) blueprint.getOrDefault("product", 0));
                Blueprint.blueprintTags.put(key, (List<String>) blueprint.getOrDefault("tags", new ArrayList<>()));
            }

            //Maps recipe data
            final Map<String, Object> recipeData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/recipes").toURI());
            for (String key : recipeData.keySet()) {
                final Map<String, Object> recipe = (Map<String, Object>) recipeData.get(key);
                Craft.recipeMats.put(key, (List<Map<String, Number>>) recipe.getOrDefault("mats", new ArrayList<>()));
                Craft.recipeAttributes.put(key, (Map<String, Number>) recipe.getOrDefault("attributes", new HashMap<>()));
                Craft.recipeTextures.put(key, (Map<String, String>) recipe.getOrDefault("texture", new HashMap<>()));
                Craft.recipeTypes.put(key, (String) recipe.getOrDefault("type", "item"));
                Craft.recipeNames.put(key, (String) recipe.getOrDefault("name", key));
                Craft.recipeProducts.put(key, (List<Map<String, Number>>) recipe.getOrDefault("products", new ArrayList<>()));
            }

            //Maps item data
            final Map<String, Object> itemData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/items").toURI());
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

            //Maps key item data
            final Map<String, Object> keyData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/key_items").toURI());
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
            final Map<String, Object> biomeData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/biomes").toURI());
            for (String key : biomeData.keySet()) {
                final Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biometextureMap.put(key, (Map<String, String>) biome.getOrDefault("textureMap", new HashMap<>()));
                Biome.biomeTags.put(key, (List<String>) biome.getOrDefault("tags", new ArrayList<>()));
                Biome.biomeAttributes.put(key, (Map<String, Number>) biome.getOrDefault("attributes", new HashMap<>()));
                Biome.biomeGeneration.put(key, (Map<String, Object>) biome.getOrDefault("generation", new HashMap<>()));
                Biome.biomeLevel.put(key, (Integer) biome.getOrDefault("level", 1));
            }

            //Maps structure data
            final Map<String, Object> structureData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/structures").toURI());
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
            final Map<String, Object> entityData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/entities").toURI());
            for (String key : entityData.keySet()) {
                final Map<String, Object> entity = (Map<String, Object>) entityData.get(key);
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
            final Map<String, Object> weaponData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/weapons").toURI());
            for (String key : weaponData.keySet()) {
                final Map<String, Object> weapon = (Map<String, Object>) weaponData.get(key);
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
            final Map<String, Object> ammoData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/ammo").toURI());
            for (String key : ammoData.keySet()) {
                final Map<String, Object> ammo = (Map<String, Object>) ammoData.get(key);
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
            final Map<String, Object> armorData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/armor").toURI());
            for (String key : armorData.keySet()) {
                final Map<String, Object> armor = (Map<String, Object>) armorData.get(key);
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
            final Map<String, Object> projData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/projectiles").toURI());
            for (String key : projData.keySet()) {
                final Map<String, Object> proj = (Map<String, Object>) projData.get(key);
                Projectile.projAttributes.put(key, (Map<String, Number>) proj.getOrDefault("attributes", new HashMap<>()));
                Projectile.projTextures.put(key, (Map<String, String>) proj.getOrDefault("texture", new HashMap<>()));
                Projectile.projSplinters.put(key, (List<Map<String, Number>>) proj.getOrDefault("splinters", new ArrayList<>()));
                Projectile.projTags.put(key, (List<String>) proj.getOrDefault("tags", new ArrayList<>()));

                final int id = (int) proj.get("id");
                Projectile.projIntId.put(key, id);
                Projectile.projStringId.put(id, key);
            }

            //Maps Effect data
            final Map<String, Object> effectData = FileManager.readJsonDirectory(DataManager.class.getResource("/jsonData/effects").toURI());
            for (String key : effectData.keySet()) {
                final Map<String, Object> effect = (Map<String, Object>) effectData.get(key);
                Effect.effectNames.put(key, (String) effect.getOrDefault("name", key));
                Effect.effectTextures.put(key, (Map<String, String>) effect.getOrDefault("texture", new HashMap<>()));
                Effect.effectModifiers.put(key, (Map<String, Number>) effect.getOrDefault("modifiers", new HashMap<>()));
                Effect.effectTags.put(key, (List<String>) effect.getOrDefault("tags", new ArrayList<>()));

                final int id = (int) effect.get("id");
                Effect.effectIntId.put(key, id);
                Effect.effectStringId.put(id, key);
            }
        } catch (IOException e) {
            System.out.println("DataManager ERROR: Failed to import json files.");
        } catch (URISyntaxException e) {
            System.out.println("DataManager ERROR: Could not find json files.");
        }
    }

    public static void formatBiomeGeneration() {
        for (String biomeId : Biome.biomeGeneration.keySet()) {
            final Map<String, Object> gen = Biome.biomeGeneration.get(biomeId);

            int[] size = toIntArray(gen.get("size"), 51, 100);
            if (size.length < 2) {
                size = new int[]{51, 100};
            }

            int[][] startMap = toIntMatrix(gen.get("startMap"));
            if (startMap.length == 0) {
                startMap = new int[][]{{1}};
            }
            final int startHeight = startMap.length;

            final int defaultStartX = size[0] / 2;
            final int defaultStartY = size[1] - 1;
            int[] start = toIntArray(gen.get("start"), defaultStartX, defaultStartY);
            if (start.length < 2) {
                start = new int[]{defaultStartX, defaultStartY};
            }

            final int baseTile = getInt(gen, "base", 0);

            final int defaultMarginX = size[0] / 2 + 1;
            final int defaultMarginY = size[1] - startHeight;
            int[] margin = toIntArray(gen.get("genSize"), defaultMarginX, defaultMarginY);
            if (margin.length < 2) {
                margin = new int[]{defaultMarginX, defaultMarginY};
            }

            final List<Biome.TileGenRule> tileRules = new ArrayList<>();
            for (Map<String, Object> tile : getListOfMaps(gen, "tiles")) {
                tileRules.add(new Biome.TileGenRule(
                        getInt(tile, "id", 1),
                        getDouble(tile, "prob", 0),
                        getDouble(tile, "maxProb", 1.0),
                        getDouble(tile, "levelProb", 0),
                        toDoubleMatrix(tile.get("tileProb")),
                        toDoubleMatrix(tile.get("sideProb")),
                        toDoubleMatrix(tile.get("bottomProb")),
                        toDoubleMatrix(tile.get("rangeProb"))
                ));
            }

            final List<Biome.ObjGenRule> objRules = new ArrayList<>();
            for (Map<String, Object> obj : getListOfMaps(gen, "objects")) {
                objRules.add(new Biome.ObjGenRule(
                        getInt(obj, "id", 1),
                        getDouble(obj, "prob", 0),
                        getDouble(obj, "maxProb", 1.0),
                        getDouble(obj, "levelProb", 0),
                        toDoubleMatrix(obj.get("tileProb")),
                        toDoubleMatrix(obj.get("rangeProb"))
                ));
            }

            final List<Biome.EntityGenRule> entityRules = new ArrayList<>();
            for (Map<String, Object> entity : getListOfMaps(gen, "entities")) {
                entityRules.add(new Biome.EntityGenRule(
                        getInt(entity, "id", 0),
                        getDouble(entity, "prob", 0),
                        getDouble(entity, "maxProb", 1.0),
                        toDoubleMatrix(entity.get("tileProb")),
                        toDoubleMatrix(entity.get("rangeProb")),
                        getDouble(entity, "levelProb", 0)
                ));
            }

            final List<Biome.StructureGenRule> structureRules = new ArrayList<>();
            for (Map<String, Object> structure : getListOfMaps(gen, "structures")) {
                final Object rawGrid = structure.get("grid");
                final int[][] grid = rawGrid != null ? toIntMatrix(rawGrid) : new int[][]{{0, 0, 0, 0}};
                structureRules.add(new Biome.StructureGenRule(
                        getInt(structure, "id", 0),
                        getDouble(structure, "prob", 0),
                        getDouble(structure, "maxProb", 1.0),
                        getDouble(structure, "levelProb", 0),
                        toIntMatrix(structure.get("pos")),
                        grid,
                        toDoubleMatrix(structure.get("tileProb")),
                        toDoubleMatrix(structure.get("rangeProb")),
                        toIntArray(structure.get("offsetBR"), 0, 0)
                ));
            }

            Biome.biomeGenData.put(biomeId, new Biome.BiomeGenData(
                    size, start, startMap, baseTile, margin,
                    tileRules, objRules, entityRules, structureRules
            ));
        }
    }

    public static void formatStructureData() {
        for (String structureId : Structure.structureGeneration.keySet()) {
            final Map<String, Object> gen = Structure.structureGeneration.get(structureId);

            int[] core = null;
            final Object rawCore = gen.get("core");
            if (rawCore instanceof List<?> list && !list.isEmpty()) {
                core = toIntArray(rawCore, 0, 0);
            }

            final int[] size = toIntArray(gen.get("size"), 0, 0);
            final int[][] tileMap = toIntMatrix(gen.get("tileMap"));
            final int[][] objMap = toIntMatrix(gen.get("objMap"));

            final List<Structure.EntitySpawn> entitySpawns = new ArrayList<>();
            for (Map<String, Object> entity : getListOfMaps(gen, "entities")) {
                final String entityId = Entity.entityStringId.get(getInt(entity, "id", 0));
                entitySpawns.add(new Structure.EntitySpawn(
                        entityId,
                        getDouble(entity, "prob", 1),
                        getDouble(entity, "maxProb", 1.0),
                        getDouble(entity, "levelProb", 0),
                        toDoubleArray(entity.get("pos"), 0, 0)
                ));
            }

            final List<Structure.ChestSpawn> chestSpawns = new ArrayList<>();
            for (Map<String, Object> chest : getListOfMaps(gen, "chests")) {
                chestSpawns.add(new Structure.ChestSpawn(
                        getInt(chest, "id", 16),
                        getDouble(chest, "prob", 1),
                        getDouble(chest, "maxProb", 1.0),
                        getDouble(chest, "levelProb", 0),
                        toIntArray(chest.get("pos"), 0, 0),
                        getListOfMaps(chest, "lootEntries")
                ));
            }

            final List<Structure.StructureJoint> joints = new ArrayList<>();
            for (Map<String, Object> joint : getListOfMaps(gen, "joints")) {
                joints.add(new Structure.StructureJoint(
                        toIntArray(joint.get("pos"), 0, 0),
                        getListOfMaps(joint, "segments"),
                        getInt(joint, "rotation", 0)
                ));
            }

            Structure.structureData.put(structureId, new Structure.StructureData(
                    core, size, tileMap, objMap, entitySpawns, chestSpawns, joints
            ));
        }
    }

    private static int getInt(Map<String, Object> map, String key, int def) {
        final Object v = map.get(key);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    private static double getDouble(Map<String, Object> map, String key, double def) {
        final Object v = map.get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : def;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getListOfMaps(Map<String, Object> map, String key) {
        final Object val = map.get(key);
        return val instanceof List<?> ? (List<Map<String, Object>>) val : new ArrayList<>();
    }

    private static int[] toIntArray(Object raw, int... defaults) {
        if (!(raw instanceof List<?>)) {
            return defaults.clone();
        }
        final List<?> list = (List<?>) raw;
        final int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            final Object elem = list.get(i);
            result[i] = elem instanceof Number ? ((Number) elem).intValue()
                    : (defaults.length > i ? defaults[i] : 0);
        }
        return result;
    }

    private static double[] toDoubleArray(Object raw, double... defaults) {
        if (!(raw instanceof List<?>)) {
            return defaults.clone();
        }
        final List<?> list = (List<?>) raw;
        final double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            final Object elem = list.get(i);
            result[i] = elem instanceof Number ? ((Number) elem).doubleValue()
                    : (defaults.length > i ? defaults[i] : 0);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static int[][] toIntMatrix(Object raw) {
        if (!(raw instanceof List<?> outer) || outer.isEmpty()) {
            return new int[0][0];
        }
        if (!(outer.get(0) instanceof List<?>)) {
            return new int[0][0];
        }
        final List<List<?>> list = (List<List<?>>) raw;
        final int[][] result = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            final List<?> row = list.get(i);
            result[i] = new int[row.size()];
            for (int j = 0; j < row.size(); j++) {
                final Object elem = row.get(j);
                result[i][j] = elem instanceof Number ? ((Number) elem).intValue() : 0;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static double[][] toDoubleMatrix(Object raw) {
        if (!(raw instanceof List<?> outer) || outer.isEmpty()) {
            return new double[0][0];
        }
        if (!(outer.get(0) instanceof List<?>)) {
            return new double[0][0];
        }
        final List<List<?>> list = (List<List<?>>) raw;
        final double[][] result = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            final List<?> row = list.get(i);
            result[i] = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {
                final Object elem = row.get(j);
                result[i][j] = elem instanceof Number ? ((Number) elem).doubleValue() : 0;
            }
        }
        return result;
    }
}
