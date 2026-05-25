package honey.world;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import honey.HoneySuckle;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Rendering;

/* 
 * World.java *
 - Class used for managing world
 - Manages tiles and objects
 - Manages entities and projectiles
 - Update and Render Methods
 */
public class World {

    private static final int FPS = HoneySuckle.FPS;
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;
    private static final int RENDER_DISTANCE = 2;
    private static final double TILE_EPSILON = 0.001;

    private static final int[] renderOffset = new int[] {
            (int) GAME_WIDTH / TILE_SIZE / 2 + RENDER_DISTANCE,
            (int) GAME_HEIGHT / TILE_SIZE / 2 + RENDER_DISTANCE
    };

    // Static variables
    public static List<World> worlds = new ArrayList<>();
    public static int level = 0;

    // World Constructor
    public World() {
        // Pseudo-Randomized Biome
        biome = new Biome(this);
        // Generates the world based on the biome
        biome.generateWorld();
        // Sets camera position
        camera = new double[] { (start[0] + 0.5) * TILE_SIZE, (size[1] * TILE_SIZE) - GAME_HEIGHT / 2.0 };
        navigator = new Navigator(this);
        // Adds world to static list of worlds
        worlds.add(this);
    }

    public World(String biomeId) {
        biome = new Biome(this, biomeId);
        biome.generateWorld();
        camera = new double[] { (start[0] + 0.5) * TILE_SIZE, (size[1] * TILE_SIZE) - GAME_HEIGHT / 2.0 };
        navigator = new Navigator(this);
        worlds.add(this);
    }

    // Camera position for rendering
    public double[] camera = new double[2];

    // World Make-up
    public Tile[][] grid;
    public WorldObject[][] objGrid;
    public List<Entity> entities;
    public Structure[][] structureGrid;
    public List<Projectile> projectiles = new ArrayList<>();

    public final Navigator navigator;

    // Updating entities
    public List<Entity> renderEntities = new ArrayList<>();
    public List<Projectile> renderProjectiles = new ArrayList<>();

    // Spatial grid for entity collision queries (keyed by bx * size[1] + by)
    public final Map<Integer, List<Entity>> entityGrid = new HashMap<>();

    // World attributes
    public int[] size = new int[2];
    public int[] start = new int[2];
    public final Biome biome;

    // Processes a loot list: type 7 spawns entities at spawnPos, all others go to inventory
    public void processLoot(List<Map<String, Number>> loot, double[] spawnPos, Player player) {
        for (Map<String, Number> entry : loot) {
            if (entry.getOrDefault("type", 0).intValue() == 7) {
                final double prob = entry.getOrDefault("prob", 1).doubleValue();
                if (Math.random() >= prob) continue;
                int count = entry.getOrDefault("count", 1).intValue();
                final double countProb = entry.getOrDefault("countProb", 0).doubleValue();
                while (ThreadLocalRandom.current().nextDouble() <= countProb) count++;
                final String entityType = Entity.entityStringId.get(entry.getOrDefault("id", 0).intValue());
                if (entityType != null) {
                    final double burst = entry.getOrDefault("burst", 0).doubleValue();
                    for (int i = 0; i < count; i++) {
                        final double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
                        final double scatter = TILE_SIZE * 0.3;
                        final double[] pos = {
                            spawnPos[0] + Math.cos(angle) * scatter,
                            spawnPos[1] + Math.sin(angle) * scatter
                        };
                        final Entity spawned = new Entity(entityType, pos, this);
                        if (burst > 0) {
                            final double burstAngle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
                            spawned.vel[0] = Math.cos(burstAngle) * burst * TILE_SIZE;
                            spawned.vel[1] = Math.sin(burstAngle) * burst * TILE_SIZE;
                        }
                        spawned.brain.immunity = 3.0;
                        entities.add(spawned);
                    }
                }
            } else if (player != null) {
                player.inventory.incrementItem(entry, true);
            }
        }
    }

    // Bounds movement to boundaries of world, mutates pos in place
    public void bound(double[] pos, double[] delta, List<String> tags, double margin) {
        if (margin <= 0)
            margin = 0.01;

        final boolean flying = tags.contains("flying");
        final String collAttr = flying ? "flightCollision" : "collision";

        final double origX = pos[0];
        final double origY = pos[1];
        final int posX = (int) Math.floor(origX / TILE_SIZE);
        final int posY = (int) Math.floor(origY / TILE_SIZE);
        final boolean onWalkable = checkTag(posX, posY, "walkable") && !checkTag(posX, posY, "slippery")
                && !flying;
        // X phase: apply X delta, resolve collisions against original Y span
        if (delta[0] != 0) {
            pos[0] += delta[0];
            if (pos[0] < margin)
                pos[0] = margin;
            if (pos[0] > size[0] * TILE_SIZE - margin)
                pos[0] = size[0] * TILE_SIZE - margin;

            final int newPosX = (int) Math.floor(pos[0] / TILE_SIZE);
            if (onWalkable && newPosX >= 0 && newPosX < size[0] && !checkTag(newPosX, posY, "walkable")) {
                pos[0] = origX;
            }
            final int txMin = (int) Math.floor((pos[0] - margin) / TILE_SIZE);
            final int txMax = (int) Math.floor((pos[0] + margin) / TILE_SIZE);
            final int tyMin = (int) Math.floor((origY - margin + TILE_EPSILON) / TILE_SIZE);
            final int tyMax = (int) Math.floor((origY + margin - TILE_EPSILON) / TILE_SIZE);
            if (delta[0] < 0) {
                double snapRight = Double.NEGATIVE_INFINITY;
                for (int tx = txMin; tx <= txMax; tx++) {
                    for (int ty = tyMin; ty <= tyMax; ty++) {
                        if (tx < 0 || tx >= size[0] || ty < 0 || ty >= size[1] || checkTag(tx, ty, "tunnel"))
                            continue;
                        final double c = getAttribute(tx, ty, collAttr);
                        if (c <= 0) continue;
                        final double tileLeft = (tx + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileRight = (tx + (1.0 + c) / 2.0) * TILE_SIZE;
                        final double tileTop = (ty + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileBottom = (ty + (1.0 + c) / 2.0) * TILE_SIZE;
                        if (pos[0] - margin < tileRight && pos[0] + margin > tileLeft
                                && origY - margin + TILE_EPSILON < tileBottom
                                && origY + margin - TILE_EPSILON > tileTop)
                            snapRight = Math.max(snapRight, tileRight);
                    }
                }
                if (snapRight != Double.NEGATIVE_INFINITY)
                    pos[0] = snapRight + margin;
            } else {
                double snapLeft = Double.POSITIVE_INFINITY;
                for (int tx = txMin; tx <= txMax; tx++) {
                    for (int ty = tyMin; ty <= tyMax; ty++) {
                        if (tx < 0 || tx >= size[0] || ty < 0 || ty >= size[1] || checkTag(tx, ty, "tunnel"))
                            continue;
                        final double c = getAttribute(tx, ty, collAttr);
                        if (c <= 0) continue;
                        final double tileLeft = (tx + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileRight = (tx + (1.0 + c) / 2.0) * TILE_SIZE;
                        final double tileTop = (ty + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileBottom = (ty + (1.0 + c) / 2.0) * TILE_SIZE;
                        if (pos[0] - margin < tileRight && pos[0] + margin > tileLeft
                                && origY - margin + TILE_EPSILON < tileBottom
                                && origY + margin - TILE_EPSILON > tileTop)
                            snapLeft = Math.min(snapLeft, tileLeft);
                    }
                }
                if (snapLeft != Double.POSITIVE_INFINITY)
                    pos[0] = snapLeft - margin;
            }
        }
        // Y phase: apply Y delta, resolve collisions against updated X span
        pos[1] += delta[1];
        if (pos[1] < margin)
            pos[1] = margin;
        if (pos[1] > size[1] * TILE_SIZE - margin)
            pos[1] = size[1] * TILE_SIZE - margin;
        if (delta[1] != 0) {
            final int curPosX = (int) Math.floor(pos[0] / TILE_SIZE);
            final int newPosY = (int) Math.floor(pos[1] / TILE_SIZE);
            if (onWalkable && newPosY >= 0 && newPosY < size[1] && !checkTag(curPosX, newPosY, "walkable")) {
                pos[1] = origY;
            }
            final int txMin = (int) Math.floor((pos[0] - margin + TILE_EPSILON) / TILE_SIZE);
            final int txMax = (int) Math.floor((pos[0] + margin - TILE_EPSILON) / TILE_SIZE);
            final int tyMin = (int) Math.floor((pos[1] - margin) / TILE_SIZE);
            final int tyMax = (int) Math.floor((pos[1] + margin) / TILE_SIZE);
            if (delta[1] < 0) {
                double snapBottom = Double.NEGATIVE_INFINITY;
                for (int tx = txMin; tx <= txMax; tx++) {
                    for (int ty = tyMin; ty <= tyMax; ty++) {
                        if (tx < 0 || tx >= size[0] || ty < 0 || ty >= size[1] || checkTag(tx, ty, "tunnel"))
                            continue;
                        final double c = getAttribute(tx, ty, collAttr);
                        if (c <= 0) continue;
                        final double tileLeft = (tx + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileRight = (tx + (1.0 + c) / 2.0) * TILE_SIZE;
                        final double tileTop = (ty + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileBottom = (ty + (1.0 + c) / 2.0) * TILE_SIZE;
                        if (pos[0] - margin + TILE_EPSILON < tileRight && pos[0] + margin - TILE_EPSILON > tileLeft
                                && pos[1] - margin < tileBottom && pos[1] + margin > tileTop)
                            snapBottom = Math.max(snapBottom, tileBottom);
                    }
                }
                if (snapBottom != Double.NEGATIVE_INFINITY)
                    pos[1] = snapBottom + margin;
            } else {
                double snapTop = Double.POSITIVE_INFINITY;
                for (int tx = txMin; tx <= txMax; tx++) {
                    for (int ty = tyMin; ty <= tyMax; ty++) {
                        if (tx < 0 || tx >= size[0] || ty < 0 || ty >= size[1] || checkTag(tx, ty, "tunnel"))
                            continue;
                        final double c = getAttribute(tx, ty, collAttr);
                        if (c <= 0) continue;
                        final double tileLeft = (tx + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileRight = (tx + (1.0 + c) / 2.0) * TILE_SIZE;
                        final double tileTop = (ty + (1.0 - c) / 2.0) * TILE_SIZE;
                        final double tileBottom = (ty + (1.0 + c) / 2.0) * TILE_SIZE;
                        if (pos[0] - margin + TILE_EPSILON < tileRight && pos[0] + margin - TILE_EPSILON > tileLeft
                                && pos[1] - margin < tileBottom && pos[1] + margin > tileTop)
                            snapTop = Math.min(snapTop, tileTop);
                    }
                }
                if (snapTop != Double.POSITIVE_INFINITY)
                    pos[1] = snapTop - margin;
            }
        } // Final clamp
        if (pos[0] < margin)
            pos[0] = margin;
        if (pos[0] > size[0] * TILE_SIZE - margin)
            pos[0] = size[0] * TILE_SIZE - margin;
        if (pos[1] < margin)
            pos[1] = margin;
        if (pos[1] > size[1] * TILE_SIZE - margin)
            pos[1] = size[1] * TILE_SIZE - margin;
    }

    // Events based on player pos
    public void playerEvent(Player player) {
        // Checks if player is at end of world, then progresses
        if (player.pos[1] <= player.size / 2.0) {
            if (!biome.tags.contains("enemyLock") || entities.isEmpty()) {
                level++;
                World world = new World();
                player.pos = new double[] { TILE_SIZE * (world.start[0] + 0.5), TILE_SIZE * (world.size[1] - 0.5) };
                return;
            }
        }
        // Player Tile
        int[] posIndex = new int[] { (int) Math.floor(player.pos[0] / TILE_SIZE),
                (int) Math.floor(player.pos[1] / TILE_SIZE) };

        // Player margin from center
        double margin = player.size / 2.0 + 1;
        // Player touching tiles
        int[][] marginIndex = new int[][] {
                { (int) (Math.floor((player.pos[0] - margin) / TILE_SIZE)),
                        (int) (Math.floor((player.pos[0] + margin) / TILE_SIZE)) },
                { (int) (Math.floor((player.pos[1] - margin) / TILE_SIZE)),
                        (int) (Math.floor((player.pos[1] + margin) / TILE_SIZE)) }
        };

        // Checks if on damage tile
        if (checkAttribute(posIndex[0], posIndex[1], "damageness") && !checkTag(posIndex[0], posIndex[1], "safe")) {
            player.damage(getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS, false);
            if (biome.tags.contains("dangerousVoid")) {
                player.damage(0.01 * getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS, false);
            }
        }
        // Checks if on acel tile
        if (!checkTag(posIndex[0], posIndex[1], "safe") && getAttribute(posIndex[0], posIndex[1], "acel") != 0) {
            player.vel[0] *= getAttribute(posIndex[0], posIndex[1], "acel");
            player.vel[1] *= getAttribute(posIndex[0], posIndex[1], "acel");
        }
        for (int tx = marginIndex[0][0]; tx <= marginIndex[0][1]; tx++) {
            for (int ty = marginIndex[1][0]; ty <= marginIndex[1][1]; ty++) {
                if (tx >= 0 && tx < size[0] && ty >= 0 && ty < size[1] && checkAttribute(tx, ty, "hurtness")) {
                    player.damage(0.01 * getAttribute(tx, ty, "hurtness") * 30.0 / FPS, false);
                }
            }
        }

        // Allows entities to interact with player
        for (Entity entity : renderEntities) {
            entity.brain.event(player);
        }

        // Expands map
        int[] mapRange = renderOffset.clone();
        if (biome.tags.contains("fog")) {
            int lightRadius = player.attributes.getOrDefault("lightRadius", 4).intValue();
            Arrays.fill(mapRange, lightRadius);
        }
        if (navigator.started) {
            for (int x = posIndex[0] - mapRange[0]; x < posIndex[0] + mapRange[0]; x++) {
                if (x > -1 && x < size[0]) {
                    for (int y = posIndex[1] - mapRange[1]; y < posIndex[1] + mapRange[1]; y++) {
                        if (y > -1 && y < size[1]) {
                            grid[x][y].rendered = true;

                            if (objGrid[x][y] != null) {
                                objGrid[x][y].rendered = true;
                            }
                        }
                    }
                }
            }
        }

        if (navigator.started) {
            for (int x = posIndex[0] - 10; x < posIndex[0] + 10; x++) {
                if (x > -1 && x < size[0]) {
                    for (int y = posIndex[1] - 10; y < posIndex[1] + 10; y++) {
                        if (y > -1 && y < size[1]) {
                            if (structureGrid[x][y] != null) {
                                if (structureGrid[x][y].withinRange(posIndex)) {
                                    navigator.icons.add(structureGrid[x][y]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Events based on entity
    public void entityEvent(Entity entity) {
        // Entity tile
        int[] posIndex = new int[] { (int) Math.floor(entity.pos[0] / TILE_SIZE),
                (int) Math.floor(entity.pos[1] / TILE_SIZE) };

        // Entity margin from center
        double margin = entity.size / 2.0 + 1;
        // Entity touching tiles
        int[][] marginIndex = new int[][] {
                { (int) (Math.floor((entity.pos[0] - margin) / TILE_SIZE)),
                        (int) (Math.floor((entity.pos[0] + margin) / TILE_SIZE)) },
                { (int) (Math.floor((entity.pos[1] - margin) / TILE_SIZE)),
                        (int) (Math.floor((entity.pos[1] + margin) / TILE_SIZE)) }
        };

        // Checks if on damage tile
        if (!entity.tags.contains("flying")) {
            if (checkAttribute(posIndex[0], posIndex[1], "damageness") && !checkTag(posIndex[0], posIndex[1], "safe")) {
                entity.brain.damage(getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
                if (biome.tags.contains("dangerousVoid")) {
                    entity.brain.damage(0.01 * getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
                }
            }
            // Checks if on acel tile
            if (!checkTag(posIndex[0], posIndex[1], "safe") && getAttribute(posIndex[0], posIndex[1], "acel") != 0) {
                entity.vel[0] *= getAttribute(posIndex[0], posIndex[1], "acel");
                entity.vel[1] *= getAttribute(posIndex[0], posIndex[1], "acel");
            }
        }

        for (int tx = marginIndex[0][0]; tx <= marginIndex[0][1]; tx++) {
            for (int ty = marginIndex[1][0]; ty <= marginIndex[1][1]; ty++) {
                if (tx >= 0 && tx < size[0] && ty >= 0 && ty < size[1] && checkAttribute(tx, ty, "hurtness")) {
                    entity.brain.damage(0.01 * getAttribute(tx, ty, "hurtness") * 30.0 / FPS);
                }
            }
        }
    }

    // Checks if tile or obj has given tag
    public boolean checkTag(int x, int y, String tag) {
        if (objGrid[x][y] == null) {
            return grid[x][y].tags.contains(tag);
        }
        return grid[x][y].tags.contains(tag) || objGrid[x][y].tags.contains(tag);
    }

    // Gives sum of tile and obj value
    public double getAttribute(int x, int y, String value) {
        double result = 0;
        result += grid[x][y].attributes.getOrDefault(value, 0).doubleValue();
        if (objGrid[x][y] != null) {
            result += objGrid[x][y].attributes.getOrDefault(value, 0).doubleValue();
        }
        return result;
    }

    private boolean checkAttribute(int x, int y, String value) {
        if (objGrid[x][y] != null) {
            return objGrid[x][y].attributes.containsKey(value) || grid[x][y].attributes.containsKey(value);
        }
        return grid[x][y].attributes.containsKey(value);
    }

    // Render World
    public void render(Graphics2D g) {
        // Fills background as voidColor
        g.setColor(Rendering.decodeColor(biome.textureMap.get("voidColor")));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Center tile on screen
        int[] cameraTile = new int[] { (int) Math.floor(camera[0] / TILE_SIZE),
                (int) Math.floor(camera[1] / TILE_SIZE) };

        final boolean fog = biome.tags.contains("fog");

        // Runs through all nearby (on screen) tiles to render
        for (int y = cameraTile[1] - renderOffset[1]; y < cameraTile[1] + renderOffset[1]; y++) {
            for (int x = cameraTile[0] - renderOffset[0]; x < cameraTile[0] + renderOffset[0]; x++) {
                if (y >= 0 && y < grid[0].length && x >= 0 && x < grid.length) {
                    // Position of tile on screen
                    double[] screenPos = new double[] {
                            (x * TILE_SIZE - camera[0] + GAME_WIDTH / 2.0),
                            (y * TILE_SIZE - camera[1] + GAME_HEIGHT / 2.0)
                    };
                    // Render tile
                    grid[x][y].render(g, this, screenPos);
                    // If object on grid, render object
                    if (objGrid[x][y] != null) {
                        objGrid[x][y].render(g, this, screenPos);
                    }
                    // If tile/object provides light, add light to HoneySuckle.lights
                    if (fog) {
                        if (grid[x][y].attributes.containsKey("lightRadius")
                                || grid[x][y].attributes.containsKey("glowRadius")) {
                            grid[x][y].renderLight(screenPos);
                            if (navigator.started) {
                                grid[x][y].rendered = true;
                            }
                        }
                        if (objGrid[x][y] != null) {
                            if (objGrid[x][y].attributes.containsKey("lightRadius")
                                    || objGrid[x][y].attributes.containsKey("glowRadius")) {
                                objGrid[x][y].renderLight(screenPos);

                                if (navigator.started) {
                                    objGrid[x][y].rendered = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Renders entities
        for (Entity entity : renderEntities) {
            entity.render(g, camera);
        }
        // Renders projectiles
        for (Projectile proj : renderProjectiles) {
            double[] screenPos = new double[] {
                    GAME_WIDTH / 2.0 + proj.pos[0] - camera[0],
                    GAME_HEIGHT / 2.0 + proj.pos[1] - camera[1]
            };

            proj.render(g, screenPos);
            if (fog) {
                if (proj.attributes.containsKey("lightRadius") || proj.attributes.containsKey("glowRadius")) {
                    proj.renderLight(screenPos);
                }
            }
        }
    }

    // Updates world
    public void update(InputHandler input) {
        navigator.update(input);
        // Empties renderEntities, then adds nearby entities into renderEntities, and
        // updates them
        renderEntities.clear();
        for (Entity entity : entities) {
            if (entity.tags.contains("alwaysRender") || Math.abs(entity.pos[0] - camera[0]) <= GAME_WIDTH * 3.0 / 4
                    && Math.abs(entity.pos[1] - camera[1]) <= GAME_HEIGHT * 3.0 / 4) {
                renderEntities.add(entity);
            }
        }
        for (Entity entity : renderEntities) {
            entity.update();
        }
        // Rebuilds entity collision grid from updated renderEntities positions
        entityGrid.clear();
        for (Entity entity : renderEntities) {
            final int x0 = Math.max(0, (int) Math.floor((entity.pos[0] - entity.size / 2.0) / TILE_SIZE));
            final int x1 = Math.min(size[0] - 1, (int) Math.floor((entity.pos[0] + entity.size / 2.0) / TILE_SIZE));
            final int y0 = Math.max(0, (int) Math.floor((entity.pos[1] - entity.size / 2.0) / TILE_SIZE));
            final int y1 = Math.min(size[1] - 1, (int) Math.floor((entity.pos[1] + entity.size / 2.0) / TILE_SIZE));
            for (int bx = x0; bx <= x1; bx++) {
                for (int by = y0; by <= y1; by++) {
                    entityGrid.computeIfAbsent(bx * size[1] + by, k -> new ArrayList<>()).add(entity);
                }
            }
        }
        // Empties renderProjectiles, then adds nearby projectiles into
        // renderProjectiles, and updates them
        renderProjectiles.clear();
        for (Projectile projectile : projectiles) {
            if (Projectile.projTags.get(projectile.type).contains("alwaysRender")
                    || Math.abs(projectile.pos[0] - camera[0]) <= GAME_WIDTH * 3.0 / 4
                            && Math.abs(projectile.pos[1] - camera[1]) <= GAME_HEIGHT * 3.0 / 4) {
                renderProjectiles.add(projectile);
            }
        }
        for (Projectile projectile : renderProjectiles) {
            projectile.update();
        }
    }
}
