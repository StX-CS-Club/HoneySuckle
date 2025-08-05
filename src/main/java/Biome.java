
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Biome.java *
 - Handles biome generation
 - Static attributes from json
 */
public class Biome {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static data from json
    public static final Map<String, Map<String, String>> biomeColorMap = new HashMap<>();
    public static final Map<String, Map<String, Object>> biomeGeneration = new HashMap<>();
    public static final Map<String, List<String>> biomeTags = new HashMap<>();
    public static final Map<String, Integer> biomeLevel = new HashMap<>();

    public static final Map<String, Integer> structureIntId = new HashMap<>();
    public static final Map<Integer, String> structureStringId = new HashMap<>();
    public static final Map<String, String> structureName = new HashMap<>();
    public static final Map<String, Map<String, Object>> structureGeneration = new HashMap<>();

    public final String type;

    public final List<String> tags;
    public final Map<String, String> colorMap;
    private final Map<String, Object> generation;

    public Biome() {
        if (World.level > 0) {
            type = randomizeBiome(World.worlds.getLast().biome.type, World.level);
        } else {
            type = "wetlands";
        }
        tags = biomeTags.get(type);
        colorMap = biomeColorMap.get(type);
        generation = biomeGeneration.get(type);
    }

    public static String randomizeBiome(String lastBiome, int level) {
        final List<String> biomes = new ArrayList<>();
        for (String biomeId : biomeLevel.keySet()) {
            if (biomeLevel.get(biomeId) <= level) {
                biomes.add(biomeId);
            }
        }
        String biome = biomes.get((int) Math.floor(Math.random() * biomes.size()));
        if (biome.equals(lastBiome)) {
            biome = biomes.get((int) Math.floor(Math.random() * biomes.size()));
        }

        return biome;
    }

    //Generates biome based on given type
    public void generateWorld(World world) {
        // Interprets size and start values
        world.size = intArray(listFromMap(generation, "size", new Number[]{51, 100}).toArray(Number[]::new), 101);
        world.start = intArray(listFromMap(generation, "start", new Number[]{world.size[0] / 2, world.size[1] - 1}).toArray(Number[]::new), 51);

        Tile[][] result = new Tile[world.size[0]][world.size[1]];

        WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

        List<Entity> entityResult = new ArrayList<>();

        // Generates base tiles
        int baseTileId = numberFromMap(generation, "base", 0).intValue();
        for (int x = 0; x < world.size[0]; x++) {
            for (int y = 0; y < world.size[1]; y++) {
                result[x][y] = new Tile(baseTileId, new int[]{x, y}, world);
            }
        }

        // Generates start tiles
        final int[][] start = int2dArray(array2dFromList(listFromMap(generation, "startMap", new Number[][]{new Number[]{1}})), 1);

        final int[] startSize = new int[]{start[0].length, start.length};
        final int startSide = (int) Math.floor((startSize[0] - 1) / 2.0);

        for (int i = 0; i < startSize[0]; i++) {
            for (int e = 0; e < startSize[1]; e++) {
                final int[] pos = new int[]{world.start[0] - startSide + i, world.start[1] - startSize[1] + 1 + e};
                result[pos[0]][pos[1]] = new Tile(start[e][i], pos, world);
            }
        }

        // Generates world
        final int[] margin = intArray(listFromMap(generation, "genSize", new Number[]{Math.ceil(world.size[0] / 2) + 1, world.size[1] - startSize[1]}).toArray(Number[]::new), 0);

        // Generation Rules
        final List<Map<String, Object>> tileGenRules = listFromMap(generation, "tiles");
        final List<Map<String, Object>> objGenRules = listFromMap(generation, "objects");
        final List<Map<String, Object>> entityGenRules = listFromMap(generation, "entities");

        final boolean structuresFirst = tags.contains("structuresFirst");
        if(structuresFirst){
            generateStructures(world, result, objResult, entityResult);
        }

        final boolean watery = tags.contains("watery");

        
        for (int x = 0; x < margin[0]; x++) {
            for (int y = world.start[1] - startSize[1]; y > Math.max(world.start[1] - margin[1] - 1, -1); y--) {
                for (int i = 1; i > -2; i -= 2) {
                    if (x != 0 || i != -1) {
                        final int[] pos = new int[]{world.start[0] - x * i, y};

                        if (pos[0] >= 0 && pos[0] < world.size[0]) {
                            // Generates tiles
                            for (Map<String, Object> tileGenRule : tileGenRules) {
                                if (watery && x == 0 && result[pos[0]][y + 1].id != 0) {
                                    break;
                                }

                                double prob = numberFromMap(tileGenRule, "prob", 0).doubleValue();

                                final Number[][] tileProbs = array2dFromList(listFromMap(tileGenRule, "tileProb", new Number[0][]));
                                for (Number[] tileProb : tileProbs) {
                                    prob += conditionalProb(result[pos[0]][y], tileProb[0].intValue(), tileProb[1].doubleValue());
                                }

                                final Number[][] sideProbs = array2dFromList(listFromMap(tileGenRule, "sideProb", new Number[0][]));
                                for (Number[] sideProb : sideProbs) {
                                    prob += conditionalProb(result[pos[0] + i][y], sideProb[0].intValue(), sideProb[1].doubleValue());
                                }

                                final Number[][] bottomProbs = array2dFromList(listFromMap(tileGenRule, "bottomProb", new Number[0][]));
                                for (Number[] bottomProb : bottomProbs) {
                                    prob += conditionalProb(result[world.start[0] - i * x][y + 1], bottomProb[0].intValue(), bottomProb[1].doubleValue());
                                }

                                final Number[][] rangeProbs = array2dFromList(listFromMap(tileGenRule, "rangeProb", new Number[0][]));
                                for (Number[] rangeProb : rangeProbs) {
                                    if (pos[0] >= rangeProb[0].intValue() && pos[0] <= rangeProb[1].intValue()) {
                                        prob += rangeProb[2].doubleValue();
                                    }
                                }

                                if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                    result[pos[0]][y] = new Tile(numberFromMap(tileGenRule, "id", 1).intValue(), pos, world);
                                    break;
                                }
                            }

                            // Generates objects
                            for (Map<String, Object> objGenRule : objGenRules) {
                                double prob = numberFromMap(objGenRule, "prob", 0).doubleValue();

                                final Number[][] tileProbs = array2dFromList(listFromMap(objGenRule, "tileProb", new Number[0][]));
                                for (Number[] tileProb : tileProbs) {
                                    prob += conditionalProb(result[pos[0]][y], tileProb[0].intValue(), tileProb[1].doubleValue());
                                }

                                if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                    objResult[pos[0]][y] = new WorldObject(numberFromMap(objGenRule, "id", 1).intValue(), pos, world);
                                    break;
                                }
                            }

                            // Generates Entities
                            if (checkId(objResult[pos[0]][y], 0)) {
                                for (Map<String, Object> entityGenRule : entityGenRules) {
                                    double prob = numberFromMap(entityGenRule, "prob", 0).doubleValue();

                                    final Number[][] tileProbs = array2dFromList(listFromMap(entityGenRule, "tileProb", new Number[0][]));
                                    for (Number[] tileProb : tileProbs) {
                                        prob += conditionalProb(result[pos[0]][y], tileProb[0].intValue(), tileProb[1].doubleValue());
                                    }

                                    prob *= Math.pow(World.level, numberFromMap(entityGenRule, "levelProbPower", 0).doubleValue());

                                    if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                        final String entityId = Entity.entityStringId.get(numberFromMap(entityGenRule, "id", 0).intValue());
                                        entityResult.add(new Entity(entityId, new double[]{
                                            (pos[0] + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE
                                        }, world));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        
        if(!structuresFirst){
            generateStructures(world, result, objResult, entityResult);
        }

        world.grid = result;
        world.objGrid = objResult;
        world.entities = entityResult;
    }

    private void generateStructures(World world, Tile[][] result, WorldObject[][] objResult, List<Entity> entityResult){
        // Generates Structures
        final List<Map<String, Object>> structureGenRules = listFromMap(generation, "structures");
        for (Map<String, Object> structureGenRule : structureGenRules) {
            final String structureId = structureStringId.get(numberFromMap(structureGenRule, "id", 0).intValue());
            final int[] offsetBr = intArray(listFromMap(structureGenRule, "offsetBR", new Number[2]).toArray(Number[]::new), 0);
            final int[] offsetBl = intArray(listFromMap(structureGenRule, "offsetBR", new Number[2]).toArray(Number[]::new), 0);

            final int[][] setPositions = int2dArray(array2dFromList(listFromMap(structureGenRule, "pos", new Number[0][])), 0);
            for (int[] setPos : setPositions) {
                generateStructure(world, result, objResult, entityResult, setPos, structureId);
            }

            final int[][] grids = int2dArray(array2dFromList(listFromMap(structureGenRule, "grid", new Number[1][4])), 0);

            final double baseProb = numberFromMap(structureGenRule, "prob", 0).doubleValue();
            final Number[][] tileProbs = array2dFromList(listFromMap(structureGenRule, "tileProb", new Number[0][]));
            for (int[] grid : grids) {

                grid[0] = Math.max(grid[0], offsetBl[0]);
                grid[1] = Math.max(grid[1], offsetBl[1]);
                grid[2] = Math.max(grid[2], 1);
                grid[3] = Math.max(grid[3], 1);

                for (int x = grid[0]; x < world.size[0] - offsetBr[0]; x += grid[2]) {
                    for (int y = grid[1]; y < world.size[1] - offsetBr[1]; y += grid[3]) {
                        double prob = baseProb;

                        for (Number[] tileProb : tileProbs) {
                            if (result[x][y].id == tileProb[0].intValue()) {
                                prob += tileProb[1].doubleValue();
                            }
                        }

                        if (ThreadLocalRandom.current().nextDouble() <= prob) {
                            generateStructure(world, result, objResult, entityResult, new int[]{x, y}, structureId);
                        }
                    }
                }
            }

        }

    }

    private static void generateStructure(World world, Tile[][] result, WorldObject[][] objResult, List<Entity> entityResult, int[] pos, String id) {
        final Map<String, Object> generation = structureGeneration.get(id);

        final int[][] tileMap = int2dArray(array2dFromList(listFromMap(generation, "tileMap", new Number[0][0])), 0);
        if (tileMap.length > 0) {
            for (int y = 0; y < tileMap.length; y++) {
                for (int x = 0; x < tileMap[0].length; x++) {
                    final int[] tilePos = new int[]{pos[0] + x, pos[1] + y};
                    if (tilePos[0] < result.length && tilePos[1] < result[0].length) {
                        result[tilePos[0]][tilePos[1]] = new Tile(tileMap[y][x], tilePos, world);
                    } else {
                        break;
                    }
                }
            }
        }

        final int[][] objMap = int2dArray(array2dFromList(listFromMap(generation, "objMap", new Number[0][0])), 0);
        if (objMap.length > 0) {
            for (int y = 0; y < objMap.length; y++) {
                for (int x = 0; x < objMap[0].length; x++) {
                    final int[] objPos = new int[]{pos[0] + x, pos[1] + y};
                    if (objPos[0] < objResult.length && objPos[1] < objResult[0].length) {
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
        }

        final List<Map<String, Object>> entities = listFromMap(generation, "entities");
        for (Map<String, Object> entity : entities) {
            final String entityId = Entity.entityStringId.get(numberFromMap(entity, "id", 0).intValue());
            final double[] entityPos = doubleArray(listFromMap(entity, "pos", new Number[2]).toArray(Number[]::new), 0);
            Arrays.setAll(entityPos, i -> (entityPos[i] + pos[i]) * TILE_SIZE);
            entityResult.add(new Entity(entityId, entityPos, world));
        }
    }

    private static double conditionalProb(Tile tile, int condition, double prob) {
        if (checkId(tile, condition)) {
            return prob;
        }
        return 0;
    }

    private static boolean checkId(Tile tile, int id) {
        if (tile != null) {
            if (tile.id == id) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkId(WorldObject obj, int id) {
        if (obj != null) {
            if (obj.id == id) {
                return true;
            }
        } else if (id == 0) {
            return true;
        }
        return false;
    }

    private static List<Number> listFromMap(Map<String, Object> map, String key, Number[] defaultValue) {
        if (map.get(key) instanceof List<?>) {
            try {
                List<Number> result = (List<Number>) map.get(key);
                return result;
            } catch (Exception e) {
                System.out.println("HoneySuckle ERROR: Expected List<Number> under key '" + key + "'");
            }
        }
        return Arrays.asList(defaultValue);
    }

    private static List<List<Number>> listFromMap(Map<String, Object> map, String key, Number[][] defaultValue) {
        if (map.get(key) instanceof List<?>) {
            if (((List<Object>) map.get(key)).getFirst() instanceof List<?>) {
                try {
                    final List<List<Number>> result = (List<List<Number>>) map.get(key);
                    return result;
                } catch (Exception e) {
                    System.out.println("HoneySuckle ERROR: Expected List<List<Number>> under key '" + key + "'");
                }
            }
        }
        final List<List<Number>> result = new ArrayList<>();
        for (Number[] row : defaultValue) {
            result.add(Arrays.asList(row));
        }
        return result;
    }

    private static List<Map<String, Object>> listFromMap(Map<String, Object> map, String key) {
        if (map.get(key) instanceof List<?>) {
            try {
                List<Map<String, Object>> result = (List<Map<String, Object>>) map.get(key);
                return result;
            } catch (Exception e) {
                System.out.println("HoneySuckle ERROR: Expected List<Map<String, Object>> under key '" + key + "'");
            }
        }
        return new ArrayList<>();
    }

    private static Number numberFromMap(Map<String, Object> map, String key, Number defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return (Number) value;
        }
        return defaultValue;
    }

    private static Number[][] array2dFromList(List<List<Number>> list) {
        if (list.isEmpty()) {
            return new Number[0][0];
        }
        final int iLength = list.size();
        final int eLength = list.get(0).size();
        final Number[][] result = new Number[iLength][eLength];
        for (int i = 0; i < iLength; i++) {
            for (int e = 0; e < eLength; e++) {
                result[i][e] = list.get(i).get(e);
            }
        }
        return result;
    }

    private static int[] intArray(Number[] array, int defaultValue) {
        int[] result = new int[array.length];
        Arrays.setAll(result, i -> array[i] == null ? defaultValue : array[i].intValue());
        return result;
    }

    private static double[] doubleArray(Number[] array, double defaultValue){
        double[] result = new double[array.length];
        Arrays.setAll(result, i -> array[i] == null ? defaultValue : array[i].doubleValue());
        return result;
    }

    private static int[][] int2dArray(Number[][] array, int defaultValue) {
        if (array.length == 0) {
            return new int[0][0];
        }
        int[][] result = new int[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            for (int e = 0; e < array[0].length; e++) {
                Number value = array[i][e];
                if (value == null) {
                    result[i][e] = defaultValue;
                } else {
                    result[i][e] = value.intValue();
                }
            }
        }
        return result;
    }
}
