package honey.world;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import honey.mechanics.ConfigManager;
import honey.mechanics.MapReader;
import honey.rendering.Rendering;

/*
 * Biome.java *
 - Handles biome generation
 - Static attributes from json
 */
public class Biome {

    public record TileGenRule(int id, double prob, double[][] tileProb, double[][] sideProb,
            double[][] bottomProb, double[][] rangeProb) {}
    public record ObjGenRule(int id, double prob, double[][] tileProb, double[][] rangeProb) {}
    public record EntityGenRule(int id, double prob, double[][] tileProb, double[][] rangeProb,
            double levelProbPower) {}
    public record StructureGenRule(int id, double prob, int[][] pos, int[][] grid,
            double[][] tileProb, double[][] rangeProb, int[] offsetBR) {}
    public record BiomeGenData(int[] size, int[] start, int[][] startMap, int baseTile, int[] margin,
            List<TileGenRule> tiles, List<ObjGenRule> objects, List<EntityGenRule> entities,
            List<StructureGenRule> structures) {}

    public static ConfigManager config;

    // Static data from json
    public static final Map<String, Map<String, String>> biometextureMap = new HashMap<>();
    public static final Map<String, Map<String, Object>> biomeGeneration = new HashMap<>();
    public static final Map<String, BiomeGenData> biomeGenData = new HashMap<>();
    public static final Map<String, List<String>> biomeTags = new HashMap<>();
    public static final Map<String, Map<String, Number>> biomeAttributes = new HashMap<>();
    public static final Map<String, Integer> biomeLevel = new HashMap<>();

    private final World world;
    public final String type;
    public final BufferedImage overlayTexture;

    public final List<String> tags;
    public final Map<String, Number> attributes;
    public final Map<String, String> textureMap;

    public Biome(World world) {
        this.world = world;
        if (World.level > 0) {
            type = randomizeBiome(World.worlds.getLast().biome.type, World.level);
        } else {
            type = "wetlands";
        }
        tags = biomeTags.get(type);
        attributes = biomeAttributes.get(type);
        textureMap = biometextureMap.get(type);
        overlayTexture = getOverlayTexture();
    }

    public Biome(World world, String biomeId) {
        this.world = world;
        type = biomeId;
        tags = biomeTags.get(type);
        attributes = biomeAttributes.get(type);
        textureMap = biometextureMap.get(type);
        overlayTexture = getOverlayTexture();
    }

    public void renderOverlay(Graphics2D g) {
        if (overlayTexture != null) {
            final double opacitySpeed = attributes.getOrDefault("overlaySpeed", 0).doubleValue();
            final double oX = config.gameWidth - ((world.camera[0] * opacitySpeed) % (config.gameWidth));
            final double oY = config.gameHeight - ((world.camera[1] * opacitySpeed) % (config.gameHeight));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, attributes.getOrDefault("overlayOpacity", 0.1).floatValue()));
            g.drawImage(overlayTexture, (int) oX, (int) oY, config.gameWidth, config.gameHeight, null);
            g.drawImage(overlayTexture, (int) oX - config.gameWidth, (int) oY, config.gameWidth, config.gameHeight, null);
            g.drawImage(overlayTexture, (int) oX, (int) oY - config.gameHeight, config.gameWidth, config.gameHeight, null);
            g.drawImage(overlayTexture, (int) oX - config.gameWidth, (int) oY - config.gameHeight, config.gameWidth, config.gameHeight, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private BufferedImage getOverlayTexture() {
        final String texture = textureMap.get("overlayTexture");
        if (texture != null) {
            return Rendering.texture(texture);
        }
        return null;
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

    public void generateWorld() {
        final BiomeGenData genData = biomeGenData.get(type);

        world.size = genData.size().clone();
        world.start = genData.start().clone();

        Tile[][] result = new Tile[world.size[0]][world.size[1]];
        WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];
        List<Entity> entityResult = new ArrayList<>();
        world.structureGrid = new Structure[world.size[0]][world.size[1]];
        final boolean[][] structureResult = new boolean[world.size[0]][world.size[1]];

        // Generate base tiles
        for (int x = 0; x < world.size[0]; x++) {
            for (int y = 0; y < world.size[1]; y++) {
                result[x][y] = new Tile(genData.baseTile(), new int[]{ x, y }, world);
            }
        }

        // Generate start tiles
        final int[][] startMap = genData.startMap();
        final int startWidth = startMap.length > 0 ? startMap[0].length : 0;
        final int startHeight = startMap.length;
        final int startSide = (int) Math.floor((startWidth - 1) / 2.0);

        for (int i = 0; i < startWidth; i++) {
            for (int e = 0; e < startHeight; e++) {
                final int[] pos = new int[]{ world.start[0] - startSide + i, world.start[1] - startHeight + 1 + e };
                result[pos[0]][pos[1]] = new Tile(startMap[e][i], pos, world);
            }
        }

        final int[] margin = genData.margin();

        final boolean structuresFirst = tags.contains("structuresFirst");
        if (structuresFirst) {
            generateStructures(world, genData, result, objResult, entityResult, structureResult);
        }

        final boolean watery = tags.contains("watery");

        for (int x = 0; x < margin[0]; x++) {
            for (int y = world.start[1] - startHeight; y > Math.max(world.start[1] - margin[1] - 1, -1); y--) {
                for (int i = 1; i > -2; i -= 2) {
                    if (x != 0 || i != -1) {
                        final int[] pos = new int[]{ world.start[0] - x * i, y };

                        if (pos[0] >= 0 && pos[0] < world.size[0]) {
                            // Generate tiles
                            for (TileGenRule rule : genData.tiles()) {
                                if (watery && x == 0 && result[pos[0]][y + 1].id != 0) break;

                                double prob = rule.prob();
                                for (double[] tp : rule.tileProb()) {
                                    prob += conditionalProb(result[pos[0]][y], (int) tp[0], tp[1]);
                                }
                                for (double[] sp : rule.sideProb()) {
                                    prob += conditionalProb(result[pos[0] + i][y], (int) sp[0], sp[1]);
                                }
                                for (double[] bp : rule.bottomProb()) {
                                    prob += conditionalProb(result[world.start[0] - i * x][y + 1], (int) bp[0], bp[1]);
                                }
                                for (double[] rp : rule.rangeProb()) {
                                    if (pos[0] >= (int) rp[0] && y >= (int) rp[1]
                                            && pos[0] <= (int) rp[2] && y <= (int) rp[3]) {
                                        prob += rp[4];
                                    }
                                }
                                if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                    result[pos[0]][y] = new Tile(rule.id(), pos, world);
                                    break;
                                }
                            }

                            // Generate objects
                            for (ObjGenRule rule : genData.objects()) {
                                double prob = rule.prob();
                                for (double[] tp : rule.tileProb()) {
                                    prob += conditionalProb(result[pos[0]][y], (int) tp[0], tp[1]);
                                }
                                for (double[] rp : rule.rangeProb()) {
                                    if (pos[0] >= (int) rp[0] && y >= (int) rp[1]
                                            && pos[0] <= (int) rp[2] && y <= (int) rp[3]) {
                                        prob += rp[4];
                                    }
                                }
                                if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                    objResult[pos[0]][y] = new WorldObject(rule.id(), pos, world);
                                    break;
                                }
                            }

                            // Generate entities
                            if (checkId(objResult[pos[0]][y], 0)) {
                                for (EntityGenRule rule : genData.entities()) {
                                    double prob = rule.prob();
                                    for (double[] tp : rule.tileProb()) {
                                        prob += conditionalProb(result[pos[0]][y], (int) tp[0], tp[1]);
                                    }
                                    for (double[] rp : rule.rangeProb()) {
                                        if (pos[0] >= (int) rp[0] && y >= (int) rp[1]
                                                && pos[0] <= (int) rp[2] && y <= (int) rp[3]) {
                                            prob += rp[4];
                                        }
                                    }
                                    prob *= Math.pow(World.level, rule.levelProbPower());
                                    if (ThreadLocalRandom.current().nextDouble() <= prob) {
                                        final String entityId = Entity.entityStringId.get(rule.id());
                                        entityResult.add(new Entity(entityId, new double[]{
                                                (pos[0] + 0.5) * config.tileSize, (y + 0.5) * config.tileSize
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

        if (!structuresFirst) {
            generateStructures(world, genData, result, objResult, entityResult, structureResult);
        }

        world.grid = result;
        world.objGrid = objResult;
        world.entities = entityResult;
    }

    private void generateStructures(World world, BiomeGenData genData, Tile[][] result,
            WorldObject[][] objResult, List<Entity> entityResult, boolean[][] structureResult) {
        for (StructureGenRule rule : genData.structures()) {
            final String structureId = Structure.structureStringId.get(rule.id());
            final int[] size = Structure.structureData.get(structureId).size();
            final int[] offsetBR = rule.offsetBR();

            for (int[] setPos : rule.pos()) {
                generateStructure(world, result, objResult, entityResult, structureResult, setPos, structureId);
            }

            for (int[] rawGrid : rule.grid()) {
                final int[] g = rawGrid.clone();
                g[0] = Math.max(g[0], offsetBR[0]);
                g[1] = Math.max(g[1], offsetBR[1]);
                g[2] = Math.max(g[2], 1);
                g[3] = Math.max(g[3], 1);

                for (int x = g[0]; x < world.size[0] - offsetBR[0]; x += g[2]) {
                    for (int y = g[1]; y < world.size[1] - offsetBR[1]; y += g[3]) {
                        double prob = rule.prob();
                        for (double[] tp : rule.tileProb()) {
                            if (result[x][y].id == (int) tp[0]) prob += tp[1];
                        }
                        for (double[] rp : rule.rangeProb()) {
                            if (x >= (int) rp[0] && y >= (int) rp[1]
                                    && x <= (int) rp[2] && y <= (int) rp[3]) {
                                prob += rp[4];
                            }
                        }
                        if (ThreadLocalRandom.current().nextDouble() <= prob) {
                            final int[] pos = new int[]{ x, y };
                            if (structureCanGenerate(structureResult, pos, size)) {
                                generateStructure(world, result, objResult, entityResult, structureResult, pos, structureId);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void generateStructure(World world, Tile[][] result, WorldObject[][] objResult,
            List<Entity> entityResult, boolean[][] structureResult, int[] pos, String id) {
        final Structure.StructureData sd = Structure.structureData.get(id);

        if (sd.core() != null) {
            final double[] corePos = new double[]{
                    sd.core()[0] + pos[0] + 0.5,
                    sd.core()[1] + pos[1] + 0.5
            };
            if (corePos[0] < world.size[0] && corePos[1] < world.size[1]) {
                world.structureGrid[(int) corePos[0]][(int) corePos[1]] = new Structure(id, corePos);
            }
        }

        final int[] size = sd.size();
        for (int x = pos[0]; x < pos[0] + size[0]; x++) {
            if (x >= 0 && x < structureResult.length) {
                for (int y = pos[1]; y < pos[1] + size[1]; y++) {
                    if (y >= 0 && y < structureResult[0].length) {
                        structureResult[x][y] = true;
                    }
                }
            }
        }

        final int[][] tileMap = sd.tileMap();
        for (int y = 0; y < tileMap.length; y++) {
            for (int x = 0; x < tileMap[y].length; x++) {
                final int[] tilePos = new int[]{ pos[0] + x, pos[1] + y };
                if (tilePos[0] < result.length && tilePos[1] < result[0].length) {
                    if (tileMap[y][x] == -1) continue;
                    result[tilePos[0]][tilePos[1]] = new Tile(tileMap[y][x], tilePos, world);
                } else {
                    break;
                }
            }
        }

        final int[][] objMap = sd.objMap();
        for (int y = 0; y < objMap.length; y++) {
            for (int x = 0; x < objMap[y].length; x++) {
                final int[] objPos = new int[]{ pos[0] + x, pos[1] + y };
                if (objPos[0] < objResult.length && objPos[1] < objResult[0].length) {
                    if (objMap[y][x] == -1) continue;
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

        for (Structure.EntitySpawn spawn : sd.entities()) {
            if (ThreadLocalRandom.current().nextDouble() <= spawn.prob()) {
                final double[] entityPos = new double[]{
                        (spawn.pos()[0] + pos[0]) * config.tileSize,
                        (spawn.pos()[1] + pos[1]) * config.tileSize
                };
                entityResult.add(new Entity(spawn.entityId(), entityPos, world));
            }
        }

        for (Structure.ChestSpawn chestSpawn : sd.chests()) {
            if (ThreadLocalRandom.current().nextDouble() <= chestSpawn.prob()) {
                final int[] chestPos = new int[]{ chestSpawn.pos()[0] + pos[0], chestSpawn.pos()[1] + pos[1] };
                if (chestPos[0] > -1 && chestPos[0] < objResult.length
                        && chestPos[1] > -1 && chestPos[1] < objResult[0].length) {
                    final WorldObject chest = new WorldObject(chestSpawn.id(), chestPos, world);
                    final List<Map<String, Object>> lootEntries = chestSpawn.lootEntries();
                    final double chestSeed = ThreadLocalRandom.current().nextDouble();
                    final double defaultProb = defaultProb(lootEntries);
                    double chestProgress = 0;
                    for (Map<String, Object> lootEntry : lootEntries) {
                        final double lootProb = MapReader.getNumberOrDefault(lootEntry, "prob", defaultProb)
                                .doubleValue() + chestProgress;
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
    }

    private static double defaultProb(List<Map<String, Object>> maps) {
        int count = maps.size();
        double probSum = 0;
        for (Map<String, Object> map : maps) {
            int prob = MapReader.getNumberOrDefault(map, "prob", 0).intValue();
            if (prob != 0) {
                probSum += prob;
                count--;
            }
        }
        return (1 - probSum) / (double) count;
    }

    private boolean structureCanGenerate(boolean[][] structureGrid, int[] pos, int[] size) {
        for (int x = pos[0]; x < pos[0] + size[0]; x++) {
            if (x >= 0 && x < structureGrid.length) {
                for (int y = pos[1]; y < pos[1] + size[1]; y++) {
                    if (y >= 0 && y < structureGrid[0].length) {
                        if (structureGrid[x][y]) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
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
}
