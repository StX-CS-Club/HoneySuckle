package honey.world;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import honey.mechanics.ConfigManager;
import honey.mechanics.MapReader;
import honey.rendering.Rendering;

public class Structure {

    public record EntitySpawn(String entityId, double prob, double maxProb, double levelProb, double[] pos) {

    }

    public record ChestSpawn(int id, double prob, double maxProb, double levelProb, int[] pos, List<Map<String, Object>> lootEntries) {

    }

    public record StructureData(int[] core, int[] size, int[][] tileMap, int[][] objMap,
            List<EntitySpawn> entities, List<ChestSpawn> chests, List<StructureJoint> joints) {

    }

    public record StructureJoint(int[] pos, List<Map<String, Object>> segments, int rotation) {

    }

    public static ConfigManager config;

    public static final Map<String, Integer> structureIntId = new HashMap<>();
    public static final Map<Integer, String> structureStringId = new HashMap<>();
    public static final Map<String, String> structureName = new HashMap<>();
    public static final Map<String, Map<String, Object>> structureGeneration = new HashMap<>();
    public static final Map<String, Map<String, String>> structureTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> structureAttributes = new HashMap<>();
    public static final Map<String, StructureData> structureData = new HashMap<>();

    private final StructureData sd;
    private final int[] index;
    public final double[] pos;
    public final int[] size;
    public final String type;
    private final int rotation;

    private final Map<String, String> texture;
    private final Map<String, Number> attributes;

    public final BufferedImage mapTexture;

    public Structure(String type, int[] index) {
        sd = structureData.get(type);

        this.index = index;
        this.type = type;

        pos = getPos();
        size = sd.size();
        rotation = 0;

        texture = structureTextures.get(type);
        attributes = structureAttributes.get(type);

        mapTexture = getMapTexture();
    }

    public Structure(String type, int[] index, int rotation) {
        sd = structureData.get(type);

        this.index = index;
        this.type = type;

        pos = getPos();
        if (rotation % 2 == 0) {
            size = sd.size();
        } else {
            size = new int[]{sd.size()[1], sd.size()[0]};
        }
        this.rotation = rotation;

        texture = structureTextures.get(type);
        attributes = structureAttributes.get(type);

        mapTexture = getMapTexture();
    }

    public void generate(World world, Tile[][] result, WorldObject[][] objResult, List<Entity> entityResult, boolean[][] structureResult) {
        if (pos.length != 0) {
            if (pos[0] < world.size[0] && pos[1] < world.size[1]) {
                world.structureGrid[(int) pos[0]][(int) pos[1]] = this;
            }
        }

        for (int x = index[0]; x < index[0] + size[0]; x++) {
            if (x >= 0 && x < structureResult.length) {
                for (int y = index[1]; y < index[1] + size[1]; y++) {
                    if (y >= 0 && y < structureResult[0].length) {
                        structureResult[x][y] = true;
                    }
                }
            }
        }

        final int[][] tileMap = sd.tileMap();
        for (int y = 0; y < tileMap.length; y++) {
            for (int x = 0; x < tileMap[y].length; x++) {
                final int[] tilePos = rotateIndex(index, size, rotation, x, y);
                if (tilePos[0] < result.length && tilePos[1] < result[0].length) {
                    if (tileMap[y][x] == -1) {
                        continue;
                    }
                    result[tilePos[0]][tilePos[1]] = new Tile(tileMap[y][x], tilePos, world);
                } else {
                    break;
                }
            }
        }

        final int[][] objMap = sd.objMap();
        for (int y = 0; y < objMap.length; y++) {
            for (int x = 0; x < objMap[y].length; x++) {
                final int[] objPos = rotateIndex(index, size, rotation, x, y);
                if (objPos[0] < objResult.length && objPos[1] < objResult[0].length) {
                    if (objMap[y][x] == -1) {
                        continue;
                    }
                    if (objMap[y][x] != 0) {
                        objResult[objPos[0]][objPos[1]] = new WorldObject(objMap[y][x], objPos, world);
                    } else {
                        objResult[objPos[0]][objPos[1]] = null;
                    }
                } else {
                    break;
                }
            }
        }

        for (EntitySpawn spawn : sd.entities()) {
            final double spawnProb = Math.min(spawn.prob() + spawn.levelProb() * World.level, spawn.maxProb());
            if (ThreadLocalRandom.current().nextDouble() <= spawnProb) {
                final double[] entityPos = rotatePos(index, size, rotation, spawn.pos()[0], spawn.pos()[1]);
                entityResult.add(new Entity(spawn.entityId(), new double[]{entityPos[0] * config.tileSize, entityPos[1] * config.tileSize}, world));
            }
        }

        for (ChestSpawn chestSpawn : sd.chests()) {
            final double chestProb = Math.min(chestSpawn.prob() + chestSpawn.levelProb() * World.level, chestSpawn.maxProb());
            if (ThreadLocalRandom.current().nextDouble() <= chestProb) {
                final int[] chestPos = rotateIndex(index, size, rotation, chestSpawn.pos()[0], chestSpawn.pos()[1]);
                if (chestPos[0] > -1 && chestPos[0] < objResult.length
                        && chestPos[1] > -1 && chestPos[1] < objResult[0].length) {
                    final WorldObject chest = new WorldObject(chestSpawn.id(), chestPos, world);
                    final List<Map<String, Object>> lootEntries = chestSpawn.lootEntries();
                    final double chestSeed = ThreadLocalRandom.current().nextDouble();
                    final double defaultProb = defaultProb(lootEntries);
                    double chestProgress = 0;
                    for (Map<String, Object> lootEntry : lootEntries) {
                        double entryProb = MapReader.getNumberOrDefault(lootEntry, "prob", defaultProb).doubleValue();
                        entryProb += MapReader.getNumberOrDefault(lootEntry, "levelProb", 0).doubleValue() * World.level;
                        entryProb = Math.min(entryProb, MapReader.getNumberOrDefault(lootEntry, "maxProb", 1.0).doubleValue());
                        final double lootProb = entryProb + chestProgress;
                        if (lootProb >= chestSeed) {
                            final Object rawLoot = lootEntry.get("loot");
                            if (rawLoot instanceof List<?>) {
                                chest.setLoot((List<Map<String, Number>>) rawLoot);
                            }
                            break;
                        }
                        chestProgress = lootProb;
                    }
                    objResult[chestPos[0]][chestPos[1]] = chest;
                }
            }
        }

        for (StructureJoint joint : sd.joints()) {
            final double defaultProb = defaultProb(joint.segments());
            final double jointSeed = ThreadLocalRandom.current().nextDouble();
            double jointProgress = 0;
            for (Map<String, Object> segment : joint.segments()) {
                final double prob = MapReader.getNumberOrDefault(segment, "prob", defaultProb).doubleValue() + jointProgress;
                if (jointSeed <= prob) {
                    final String structureId = structureStringId.get(MapReader.getNumberOrDefault(segment, "id", 0).intValue());
                    final int structureRotation = Math.floorMod(rotation + joint.rotation(), 4);
                    final int[] jointPos = rotateIndex(index, size, rotation, joint.pos()[0], joint.pos()[1]);
                    final int[] structurePos = getIndexFromCore(structureId, jointPos, structureRotation);
                    if (canGenerate(structureResult, structurePos, structureId, structureRotation)) {
                        final Structure structure = new Structure(structureId, structurePos, structureRotation);
                        structure.generate(world, result, objResult, entityResult, structureResult);
                        break;
                    }
                }
                jointProgress = prob;
            }
        }
    }

    private double[] getPos() {
        if (sd.core() != null) {
            return new double[]{
                sd.core()[0] + index[0] + 0.5,
                sd.core()[1] + index[1] + 0.5
            };
        }
        return new double[0];
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

    public static boolean canGenerate(boolean[][] structureGrid, int[] pos, String structureId, int rotation) {
        final StructureData sd = structureData.get(structureId);
        final int[] size = rotation % 2 == 0 ? sd.size() : new int[]{sd.size()[1], sd.size()[0]};
        if (pos[0] < 0 || pos[0] + size[0] > structureGrid.length
                || pos[1] < 0 || pos[1] + size[1] > structureGrid[0].length) {
            return false;
        }
        for (int x = pos[0]; x < pos[0] + size[0]; x++) {
            for (int y = pos[1]; y < pos[1] + size[1]; y++) {
                if (structureGrid[x][y]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int[] getIndexFromCore(String structureId, int[] coreIndex, int rotation) {
        final StructureData sd = structureData.get(structureId);
        final int[] core = sd.core();
        final int[] size = sd.size();
        switch (rotation) {
            case 0 -> {
                return new int[]{
                    coreIndex[0] - core[0], coreIndex[1] - core[1]
                };
            }
            case 1 -> {
                return new int[]{
                    coreIndex[0] + core[1] - size[1] + 1, coreIndex[1] - core[0]
                };
            }
            case 2 -> {
                return new int[]{
                    coreIndex[0] + core[0] - size[0] + 1, coreIndex[1] + core[1] - size[1] + 1
                };
            }
            case 3 -> {
                return new int[]{
                    coreIndex[0] - core[1], coreIndex[1] + core[0] - size[0] + 1
                };
            }
        }
        return new int[]{};
    }

    private static double defaultProb(List<Map<String, Object>> maps) {
        int count = maps.size();
        double probSum = 0;
        for (Map<String, Object> map : maps) {
            final double prob = MapReader.getNumberOrDefault(map, "prob", 0).doubleValue();
            if (prob != 0) {
                probSum += prob;
                count--;
            }
        }
        return count == 0 ? 0 : (1 - probSum) / count;
    }

    private static int[] rotateIndex(int[] index, int[] size, int rotation, int x, int y) {
        final double[] result = rotatePos(index, size, rotation, x, y);
        return new int[]{(int) result[0], (int) result[1]};
    }

    private static double[] rotatePos(int[] index, int[] size, int rotation, double x, double y) {
        switch (rotation) {
            case 0 -> {
                return new double[]{index[0] + x, index[1] + y};
            }
            case 1 -> {
                return new double[]{index[0] + size[0] - 1 - y, index[1] + x};
            }
            case 2 -> {
                return new double[]{index[0] + size[0] - 1 - x, index[1] + size[1] - 1 - y};
            }
            case 3 -> {
                return new double[]{index[0] + y, index[1] + size[1] - 1 - x};
            }
        }
        return new double[]{};
    }
}
