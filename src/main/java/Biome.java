
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

    //Progression of biomes
    public static String[] biomes = new String[]{
        "wetlands",
        "field",
        "islands",
        "swamp",
        "peninsula",
        "darkForest",
        "tundra",
        "desert",
        "bridge"
    };

    //Static data from json
    public static final Map<String, Map<String, String>> biomeColorMap = new HashMap<>();
    public static final Map<String, Map<String, Object>> biomeGeneration = new HashMap<>();
    public static final Map<String, List<String>> biomeTags = new HashMap<>();

    public static final Map<String, Integer> structureIntId = new HashMap<>();
    public static final Map<Integer, String> structureStringId = new HashMap<>();
    public static final Map<String, String> structureName = new HashMap<>();
    public static final Map<String, Map<String, Object>> structureGeneration = new HashMap<>();

    //Generates biome based on given type
    public static void biomeGeneration(World world) {
        Map<String, Object> generation = biomeGeneration.get(world.biome);

        // Interprets size and start values
        world.size = arrayFromList(listFromMap(generation, "size", new Number[]{51, 100}));
        world.start = arrayFromList(listFromMap(generation, "start", new Number[]{world.size[0] / 2, world.size[1] - 1}));

        Tile[][] result = new Tile[world.size[0]][world.size[1]];

        WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

        List<Entity> entityResult = new ArrayList<>();

        // Generates base tiles
        for (int x = 0; x < world.size[0]; x++) {
            for (int y = 0; y < world.size[1]; y++) {
                result[x][y] = new Tile(numberFromMap(generation, "base", 0).intValue(), new int[]{x, y});
            }
        }

        // Generates start tiles
        final int[][] start = array2dFromList(listFromMap(generation, "startMap", new Number[][]{new Number[]{1}}));

        final int[] startSize = new int[]{start[0].length, start.length};
        final double startSide = (startSize[0] - 1) / 2.0;

        for (int i = 0; i < startSize[0]; i++) {
            for (int e = 0; e < startSize[1]; e++) {
                final int[] pos = new int[]{world.start[0] - (int) Math.floor(startSide) + i, world.start[1] - startSize[1] + 1 + e};
                result[pos[0]][pos[1]] = new Tile(start[e][i], pos);
            }
        }

        // Generates world
        final int[] margin = arrayFromList(listFromMap(generation, "genSize", new Number[]{Math.ceil(world.size[0] / 2) + 1, world.size[1] - startSize[1]}));

        // Generation Rules
        final List<Map<String, Object>> tileGenRules = listFromMap(generation, "tiles");
        final List<Map<String, Object>> objGenRules = listFromMap(generation, "objects");
        final List<Map<String, Object>> entityGenRules = listFromMap(generation, "entities");

        final List<String> tags = biomeTags.get(world.biome);

        for (int x = 0; x < margin[0]; x++) {
            for (int y = world.start[1] - startSize[1]; y > Math.max(world.start[1] - margin[1] - 1, -1); y--) {
                for (int i = 1; i > -2; i -= 2) {
                    if (x != 0 || i != -1) {
                        final int[] pos = new int[]{world.start[0] - x * i, y};

                        if (pos[0] >= 0 && pos[0] < world.size[0]) {
                            // Generates tiles
                            for (Map<String, Object> tileGenRule : tileGenRules) {
                                if (tags.contains("watery") && x == 0 && result[pos[0]][y + 1].id != 0) {
                                    break;
                                }

                                double prob = numberFromMap(tileGenRule, "prob", 0).doubleValue();

                                final List<Number> sideProbs = listFromMap(tileGenRule, "sideProb", new Number[]{});
                                if (!sideProbs.isEmpty()) {
                                    for (int e = 0; e <= sideProbs.size() / 2; e += 2) {
                                        prob += conditionalProb(result[pos[0] + i][y], sideProbs.get(e).intValue(), sideProbs.get(e + 1).doubleValue());
                                    }
                                }

                                final List<Number> bottomProbs = listFromMap(tileGenRule, "bottomProb", new Number[]{});
                                if (!bottomProbs.isEmpty()) {
                                    for (int e = 0; e <= bottomProbs.size() / 2; e += 2) {
                                        prob += conditionalProb(result[world.start[0] - i * x][y + 1], bottomProbs.get(e).intValue(), bottomProbs.get(e + 1).doubleValue());
                                    }
                                }

                                final List<Number> rangeProbs = listFromMap(tileGenRule, "rangeProb", new Number[]{});
                                if (!rangeProbs.isEmpty()) {
                                    for (int e = 0; e <= rangeProbs.size() / 3; e += 3) {
                                        if (pos[0] >= rangeProbs.get(e).intValue() && pos[0] <= rangeProbs.get(e + 1).intValue()) {
                                            prob += rangeProbs.get(e + 2).intValue();
                                        }
                                    }
                                }

                                if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                    result[pos[0]][y] = new Tile(numberFromMap(tileGenRule, "id", 1).intValue(), pos);
                                    break;
                                }
                            }

                            // Generates objects
                            for (Map<String, Object> objGenRule : objGenRules) {
                                double prob = numberFromMap(objGenRule, "prob", 0).doubleValue();

                                final List<Number> tileProbs = listFromMap(objGenRule, "tileProb", new Number[]{});
                                for (int e = 0; e < tileProbs.size() / 2; e += 2) {
                                    prob += conditionalProb(result[pos[0]][y], tileProbs.get(e).intValue(), tileProbs.get(e + 1).doubleValue());
                                }

                                if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                    objResult[pos[0]][y] = new WorldObject(numberFromMap(objGenRule, "id", 1).intValue(), pos);
                                    break;
                                }
                            }

                            // Generates Entities
                            if (checkId(objResult[pos[0]][y], 0)) {
                                for (Map<String, Object> entityGenRule : entityGenRules) {
                                    double prob = numberFromMap(entityGenRule, "prob", 0).doubleValue();

                                    final List<Number> tileProbs = listFromMap(entityGenRule, "tileProb", new Number[]{});
                                    for (int e = 0; e < tileProbs.size() / 2; e += 2) {
                                        prob += conditionalProb(result[pos[0]][y], tileProbs.get(e).intValue(), tileProbs.get(e + 1).doubleValue());
                                    }

                                    prob *= Math.pow(World.level, numberFromMap(entityGenRule, "levelProbPower", 0).doubleValue());

                                    if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                        final String entityId = Entity.entityStringId.get(numberFromMap(entityGenRule, "id", 0).intValue());
                                        entityResult.add(new Entity(entityId, new double[]{
                                            (pos[0] + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE
                                        }));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Generates Structures
        final List<Map<String, Object>> structureGenRules = listFromMap(generation, "structures");
        for (Map<String, Object> structureGenRule : structureGenRules) {
            final String structureId = structureStringId.get(numberFromMap(structureGenRule, "id", 0).intValue());

            final int[][] setPositions = array2dFromList(listFromMap(structureGenRule, "pos", new Number[0][0]));
            for (int[] setPos : setPositions) {
                generateStructure(result, objResult, entityResult, setPos, structureId);
            }
        }

        world.grid = result;
        world.objGrid = objResult;
        world.entities = entityResult;
    }

    private static void generateStructure(Tile[][] result, WorldObject[][] objResult, List<Entity> entityResult, int[] pos, String id) {
        final Map<String, Object> generation = structureGeneration.get(id);

        final int[][] tileMap = array2dFromList(listFromMap(generation, "tileMap", new Number[0][0]));
        if (tileMap.length > 0) {
            for (int y = 0; y < tileMap.length; y++) {
                for (int x = 0; x < tileMap[0].length; x++) {
                    final int[] tilePos = new int[]{pos[0] + x, pos[1] + y};
                    result[tilePos[0]][tilePos[1]] = new Tile(tileMap[y][x], tilePos);
                }
            }
        }

        final int[][] objMap = array2dFromList(listFromMap(generation, "objMap", new Number[0][0]));
        if (objMap.length > 0) {
            for (int y = 0; y < objMap.length; y++) {
                for (int x = 0; x < objMap[0].length; x++) {
                    final int[] objPos = new int[]{pos[0] + x, pos[1] + y};
                    if (objMap[y][x] != 0) {
                        objResult[objPos[0]][objPos[1]] = new WorldObject(objMap[y][x], objPos);
                    } else {
                        objResult[objPos[0]][objPos[1]] = null;
                    }
                }
            }
        }

        final List<Map<String, Object>> entities = listFromMap(generation, "entities");
        for (Map<String, Object> entity : entities) {
            final String entityId = Entity.entityStringId.get(numberFromMap(entity, "id", 0).intValue());
            final List<Number> entityPosList = listFromMap(entity, "pos", new Number[2]);
            final double[] entityPos = new double[]{
                (entityPosList.get(0).doubleValue() + pos[0]) * TILE_SIZE,
                (entityPosList.get(1).doubleValue() + pos[1]) * TILE_SIZE
            };
            entityResult.add(new Entity(entityId, entityPos));
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
        if (map.get(key) instanceof Number) {
            return (Number) map.get(key);
        }
        return defaultValue;
    }

    private static int[] arrayFromList(List<Number> list) {
        final int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i).intValue();
        }
        return result;
    }

    private static int[][] array2dFromList(List<List<Number>> list) {
        if (list.isEmpty()) {
            return new int[0][0];
        }
        final int[][] result = new int[list.size()][list.getFirst().size()];
        for (int i = 0; i < result.length; i++) {
            for (int e = 0; e < result[0].length; e++) {
                result[i][e] = list.get(i).get(e).intValue();
            }
        }
        return result;
    }
}
