package honey.world;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.rendering.Rendering;

public class Structure {

    public record EntitySpawn(String entityId, double prob, double[] pos) {}
    public record ChestSpawn(int id, double prob, int[] pos, List<Map<String, Object>> lootEntries) {}
    public record StructureData(int[] core, int[] size, int[][] tileMap, int[][] objMap,
            List<EntitySpawn> entities, List<ChestSpawn> chests) {}

    public static final Map<String, Integer> structureIntId = new HashMap<>();
    public static final Map<Integer, String> structureStringId = new HashMap<>();
    public static final Map<String, String> structureName = new HashMap<>();
    public static final Map<String, Map<String, Object>> structureGeneration = new HashMap<>();
    public static final Map<String, Map<String, String>> structureTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> structureAttributes = new HashMap<>();
    public static final Map<String, StructureData> structureData = new HashMap<>();

    public final double[] pos;
    public final String type;

    private final Map<String, String> texture;
    private final Map<String, Number> attributes;

    public final BufferedImage mapTexture;

    public Structure(String type, double[] pos) {
        this.pos = pos;
        this.type = type;

        texture = structureTextures.get(type);
        attributes = structureAttributes.get(type);

        mapTexture = getMapTexture();
    }

    private BufferedImage getMapTexture() {
        final String textureId = texture.get("mapTexture");
        if (textureId != null) {
            return Rendering.texture(textureId, null);
        }
        return null;
    }

    public boolean withinRange(int[] playerPos) {
        final int range = attributes.getOrDefault("mapRange", 1).intValue();
        return Math.abs(playerPos[0] - pos[0]) <= range && Math.abs(playerPos[1] - pos[1]) <= range;
    }
}
