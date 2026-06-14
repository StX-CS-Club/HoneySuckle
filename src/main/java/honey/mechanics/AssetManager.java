package honey.mechanics;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import honey.player.armory.Ammo;
import honey.player.armory.Armor;
import honey.player.armory.Effect;
import honey.player.armory.Weapon;
import honey.player.inventory.Item;
import honey.player.inventory.KeyItem;
import honey.rendering.Rendering;
import honey.world.Biome;
import honey.world.Entity;
import honey.world.Projectile;
import honey.world.Structure;
import honey.world.Tile;
import honey.world.WorldObject;

public class AssetManager {

    public static ConfigManager config;

    public static void preloadImages() {
        // Entities: static and gif variants, respecting natColor per biome
        for (String entityKey : Entity.entityTextures.keySet()) {
            final Map<String, String> tex = Entity.entityTextures.get(entityKey);
            final String staticBase = tex.get("texture");
            final String gifBase = tex.get("gif");
            if (staticBase == null && gifBase == null) continue;
            final String anim = tex.getOrDefault("anim", "");
            final int frameSize = Entity.entityAttributes.get(entityKey).getOrDefault("frameSize", 16).intValue();
            final String natColorId = tex.get("natColor");
            if (natColorId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorId);
                    if (color != null) {
                        for (String suffix : entitySuffixes(anim)) {
                            if (staticBase != null) Rendering.registerImage(staticBase + suffix, color);
                            if (gifBase != null) Rendering.registerGIF(gifBase + suffix, color, frameSize, frameSize);
                        }
                    }
                }
            } else {
                final String color = tex.get("baseColor");
                for (String suffix : entitySuffixes(anim)) {
                    if (staticBase != null) Rendering.registerImage(staticBase + suffix, color);
                    if (gifBase != null) Rendering.registerGIF(gifBase + suffix, color, frameSize, frameSize);
                }
            }
        }

        // Objects: static and gif variants, respecting natColor per biome
        for (int objKey : WorldObject.objTextures.keySet()) {
            final Map<String, String> tex = WorldObject.objTextures.get(objKey);
            final String staticBase = tex.get("texture");
            final String gifBase = tex.get("gif");
            if (staticBase == null && gifBase == null) continue;
            final Map<String, Number> attrs = WorldObject.objAttributes.get(objKey);
            final String natColorId = tex.get("natColor");
            if (natColorId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorId);
                    if (color != null) {
                        final int frameSize = attrs.getOrDefault("frameSize", 16).intValue();
                        for (String postfix : objSuffixes(tex, attrs)) {
                            if (staticBase != null) Rendering.registerImage(staticBase + postfix, color);
                            if (gifBase != null) Rendering.registerGIF(gifBase + postfix, color, frameSize, frameSize);
                        }
                    }
                }
            } else {
                final String color = tex.get("baseColor");
                final int frameSize = attrs.getOrDefault("frameSize", 16).intValue();
                for (String postfix : objSuffixes(tex, attrs)) {
                    if (staticBase != null) Rendering.registerImage(staticBase + postfix, color);
                    if (gifBase != null) Rendering.registerGIF(gifBase + postfix, color, frameSize, frameSize);
                }
            }
        }

        // Projectiles
        for (Map<String, String> tex : Projectile.projTextures.values()) {
            final String base = tex.get("texture");
            if (base != null) Rendering.registerImage(base, null);
        }

        // Weapons: item icon + attack GIFs per swing/stab color
        for (String weaponKey : Weapon.weaponTextures.keySet()) {
            final Map<String, String> tex = Weapon.weaponTextures.get(weaponKey);
            final String itemTex = tex.get("itemTexture");
            if (itemTex != null) Rendering.registerImage(itemTex, "#e8f1ff");
            final String swingColor = tex.get("swingColor");
            if (swingColor != null) Rendering.registerGIF("attacks/slash", swingColor, config.slashFrameSize, config.slashFrameSize);
            final String stabColor = tex.get("stabColor");
            if (stabColor != null) Rendering.registerGIF("attacks/stab", stabColor, config.stabFrameWidth, config.stabFrameHeight);
        }

        // Armor front/back textures
        for (Map<String, String> tex : Armor.armorTextures.values()) {
            final String front = tex.get("front");
            final String back = tex.get("back");
            if (front != null) Rendering.registerImage(front, null);
            if (back != null) Rendering.registerImage(back, null);
        }

        // Items, ammo, key items, effects
        for (Map<String, String> tex : Item.itemTextures.values()) {
            final String t = tex.get("texture");
            if (t != null) Rendering.registerImage(t, null);
        }
        for (Map<String, String> tex : Ammo.ammoTextures.values()) {
            final String t = tex.get("itemTexture");
            if (t != null) Rendering.registerImage(t, null);
        }
        for (Map<String, String> tex : KeyItem.keyTextures.values()) {
            final String t = tex.get("texture");
            if (t != null) Rendering.registerImage(t, null);
        }
        for (Map<String, String> tex : Effect.effectTextures.values()) {
            final String t = tex.get("texture");
            if (t != null) Rendering.registerImage(t, null);
        }

        // Biome overlays
        for (Map<String, String> texMap : Biome.biometextureMap.values()) {
            final String overlay = texMap.get("overlayTexture");
            if (overlay != null) Rendering.registerImage(overlay, null);
        }

        // Tiles: edge textures only, respecting natColorEdge per biome
        for (Map<String, String> tex : Tile.tileTextures.values()) {
            final String edgeTex = tex.get("textureEdge");
            if (edgeTex == null) continue;
            final String natColorEdgeId = tex.get("natColorEdge");
            if (natColorEdgeId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorEdgeId);
                    if (color != null) Rendering.registerImage(edgeTex, color);
                }
            } else {
                Rendering.registerImage(edgeTex, tex.get("baseColorEdge"));
            }
        }
    }

    public static void registerFont() {
        try {
            final File fontFile = new File(AssetManager.class.getResource("/fonts/VT323/VT323-Regular.ttf").toURI());
            final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            final Font sizedFont = font.deriveFont(Font.PLAIN, 24f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(sizedFont);
        } catch (FontFormatException e) {
            System.out.println("AssetManager ERROR: Failed to format text font.");
        } catch (IOException e) {
            System.out.println("AssetManager ERROR: Failed to import text font file.");
        } catch (URISyntaxException e) {
            System.out.println("AssetManager ERROR: Could not find text font file.");
        }
    }

    public static void formatBiomeGeneration() {
        for (String biomeId : Biome.biomeGeneration.keySet()) {
            final Map<String, Object> gen = Biome.biomeGeneration.get(biomeId);

            int[] size = toIntArray(gen.get("size"), 51, 100);
            if (size.length < 2) size = new int[]{ 51, 100 };

            int[][] startMap = toIntMatrix(gen.get("startMap"));
            if (startMap.length == 0) startMap = new int[][]{{ 1 }};
            final int startHeight = startMap.length;

            final int defaultStartX = size[0] / 2;
            final int defaultStartY = size[1] - 1;
            int[] start = toIntArray(gen.get("start"), defaultStartX, defaultStartY);
            if (start.length < 2) start = new int[]{ defaultStartX, defaultStartY };

            final int baseTile = getInt(gen, "base", 0);

            final int defaultMarginX = size[0] / 2 + 1;
            final int defaultMarginY = size[1] - startHeight;
            int[] margin = toIntArray(gen.get("genSize"), defaultMarginX, defaultMarginY);
            if (margin.length < 2) margin = new int[]{ defaultMarginX, defaultMarginY };

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
                final int[][] grid = rawGrid != null ? toIntMatrix(rawGrid) : new int[][]{{ 0, 0, 0, 0 }};
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
        if (!(raw instanceof List<?>)) return defaults.clone();
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
        if (!(raw instanceof List<?>)) return defaults.clone();
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
        if (!(raw instanceof List<?> outer) || outer.isEmpty()) return new int[0][0];
        if (!(outer.get(0) instanceof List<?>)) return new int[0][0];
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
        if (!(raw instanceof List<?> outer) || outer.isEmpty()) return new double[0][0];
        if (!(outer.get(0) instanceof List<?>)) return new double[0][0];
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

    private static List<String> objSuffixes(Map<String, String> tex, Map<String, Number> attrs) {
        final String anim = tex.getOrDefault("anim", "");
        final int variants = attrs.getOrDefault("variants", 1).intValue();
        final List<String> result = new ArrayList<>();
        for (int v = variants > 1 ? 1 : 0; variants > 1 ? v <= variants : v < 1; v++) {
            final String variantSuffix = variants > 1 ? "_" + v : "";
            result.add(variantSuffix);
            if (anim.contains("_destroyed_")) result.add(variantSuffix + "_destroyed");
        }
        return result;
    }

    private static List<String> entitySuffixes(String anim) {
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

        final List<String> states = new ArrayList<>();
        if (anim.contains("_chase_"))    states.add("_chase");
        if (anim.contains("_hesitate_")) states.add("_hesitate");
        if (anim.contains("_shoot_"))    states.add("_shoot");
        if (anim.contains("_summon_"))   states.add("_summon");

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
}
