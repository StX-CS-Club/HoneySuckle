package honey.world;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final int[] renderOffset = new int[]{
        (int) GAME_WIDTH / TILE_SIZE / 2 + RENDER_DISTANCE,
        (int) GAME_HEIGHT / TILE_SIZE / 2 + RENDER_DISTANCE
    };

    //Static variables
    public static List<World> worlds = new ArrayList<>();
    public static int level = 0;

    //World Constructor
    public World() {
        //Pseudo-Randomized Biome
        biome = new Biome();
        //Generates the world based on the biome
        biome.generateWorld(this);
        //Sets camera position
        camera = new double[]{(start[0] + 0.5) * TILE_SIZE, (size[1] * TILE_SIZE) - GAME_HEIGHT / 2.0};
        navigator = new Navigator(this);
        //Adds world to static list of worlds
        worlds.add(this);
    }

    public World(String biomeId) {
        biome = new Biome(biomeId);
        biome.generateWorld(this);
        camera = new double[]{(start[0] + 0.5) * TILE_SIZE, (size[1] * TILE_SIZE) - GAME_HEIGHT / 2.0};
        navigator = new Navigator(this);
        worlds.add(this);
    }

    //Camera position for rendering
    public double[] camera = new double[2];

    //World Make-up
    public Tile[][] grid;
    public WorldObject[][] objGrid;
    public List<Entity> entities;
    public Structure[][] structureGrid;
    public List<Projectile> projectiles = new ArrayList<>();

    public final Navigator navigator;

    //Updating entities
    public List<Entity> renderEntities = new ArrayList<>();
    public List<Projectile> renderProjectiles = new ArrayList<>();

    //Spatial grid for entity collision queries (keyed by bx * size[1] + by)
    public final Map<Integer, List<Entity>> entityGrid = new HashMap<>();

    //World attributes
    public int[] size = new int[2];
    public int[] start = new int[2];
    public final Biome biome;

    //Bounds movement to boundaries of world, mutates pos in place
    public void bound(double[] pos, double[] delta, List<String> tags, double margin) {
        if (margin <= 0) margin = 0.01;

        //Save original tile position before applying delta
        final double origX = pos[0];
        final double origY = pos[1];
        final int posX = (int) Math.floor(origX / TILE_SIZE);
        final int posY = (int) Math.floor(origY / TILE_SIZE);

        //Apply delta in place and clamp to world bounds
        pos[0] += delta[0];
        pos[1] += delta[1];
        if (pos[0] < margin) pos[0] = margin;
        if (pos[0] > size[0] * TILE_SIZE - margin) pos[0] = size[0] * TILE_SIZE - margin;
        if (pos[1] < margin) pos[1] = margin;
        if (pos[1] > size[1] * TILE_SIZE - margin) pos[1] = size[1] * TILE_SIZE - margin;

        //Ensures you can walk on next tile
        final int newPosX = (int) Math.floor(pos[0] / TILE_SIZE);
        final int newPosY = (int) Math.floor(pos[1] / TILE_SIZE);
        if (checkTag(posX, posY, "walkable") && !checkTag(posX, posY, "slippery") && !tags.contains("flying")) {
            if (newPosX >= 0 && newPosX < size[0] && !checkTag(newPosX, posY, "walkable")) pos[0] = origX;
            if (newPosY >= 0 && newPosY < size[1] && !checkTag(posX, newPosY, "walkable")) pos[1] = origY;
        }

        //Ensures not touching any obstructions (unrolled per edge)
        final int mx0 = (int) Math.floor((pos[0] - margin) / TILE_SIZE);
        final int mx1 = (int) Math.floor((pos[0] + margin) / TILE_SIZE);
        final int my0 = (int) Math.floor((pos[1] - margin) / TILE_SIZE);
        final int my1 = (int) Math.floor((pos[1] + margin) / TILE_SIZE);
        if (delta[0] != 0) {
            if (mx0 >= 0 && mx0 < size[0] && checkTag(mx0, posY, "obstruction") && !checkTag(mx0, posY, "tunnel")) {
                pos[0] = (mx0 + 1) * TILE_SIZE + margin;
            }
            if (mx1 >= 0 && mx1 < size[0] && checkTag(mx1, posY, "obstruction") && !checkTag(mx1, posY, "tunnel")) {
                pos[0] = mx1 * TILE_SIZE - margin;
            }
        }
        if (delta[1] != 0) {
            if (my0 >= 0 && my0 < size[1] && checkTag(posX, my0, "obstruction") && !checkTag(posX, my0, "tunnel")) {
                pos[1] = (my0 + 1) * TILE_SIZE + margin;
            }
            if (my1 >= 0 && my1 < size[1] && checkTag(posX, my1, "obstruction") && !checkTag(posX, my1, "tunnel")) {
                pos[1] = my1 * TILE_SIZE - margin;
            }
        }

        //Final clamp
        if (pos[0] < margin) pos[0] = margin;
        if (pos[0] > size[0] * TILE_SIZE - margin) pos[0] = size[0] * TILE_SIZE - margin;
        if (pos[1] < margin) pos[1] = margin;
        if (pos[1] > size[1] * TILE_SIZE - margin) pos[1] = size[1] * TILE_SIZE - margin;
    }

    //Events based on player pos
    public void playerEvent(Player player) {
        //Checks if player is at end of world, then progresses
        if (player.pos[1] <= player.size / 2.0) {
            if (!biome.tags.contains("enemyLock") || entities.isEmpty()) {
                level++;
                World world = new World();
                player.pos = new double[]{TILE_SIZE * (world.start[0] + 0.5), TILE_SIZE * (world.size[1] - 0.5)};
                return;
            }
        }
        //Player Tile
        int[] posIndex = new int[]{(int) Math.floor(player.pos[0] / TILE_SIZE), (int) Math.floor(player.pos[1] / TILE_SIZE)};

        //Player margin from center
        double margin = player.size / 2.0 + 1;
        //Player touching tiles
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((player.pos[0] - margin) / TILE_SIZE)), (int) (Math.floor((player.pos[0] + margin) / TILE_SIZE))},
            {(int) (Math.floor((player.pos[1] - margin) / TILE_SIZE)), (int) (Math.floor((player.pos[1] + margin) / TILE_SIZE))}
        };

        //Checks if on damage tile
        if (checkAttribute(posIndex[0], posIndex[1], "damageness") && !checkTag(posIndex[0], posIndex[1], "safe")) {
            player.damage(getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS, false);
            if (biome.tags.contains("dangerousVoid")) {
                player.damage(0.01 * getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS, false);
            }
        }
        //Checks if on acel tile
        if (!checkTag(posIndex[0], posIndex[1], "safe") && getAttribute(posIndex[0], posIndex[1], "acel") != 0) {
            player.vel[0] *= getAttribute(posIndex[0], posIndex[1], "acel");
            player.vel[1] *= getAttribute(posIndex[0], posIndex[1], "acel");
        }
        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0]) {
                //Checks if touching hurty tile
                if (checkAttribute(marginIndex[0][i], posIndex[1], "hurtness")) {
                    player.damage(0.01 * getAttribute(marginIndex[0][i], posIndex[1], "hurtness") * 30.0 / FPS, true);
                }
            }
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                if (checkAttribute(posIndex[0], marginIndex[1][i], "hurtness")) {
                    player.damage(0.01 * getAttribute(posIndex[0], marginIndex[1][i], "hurtness") * 30.0 / FPS, true);
                }
            }
        }

        //Allows entities to interact with player
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

    //Events based on entity
    public void entityEvent(Entity entity) {
        //Entity tile
        int[] posIndex = new int[]{(int) Math.floor(entity.pos[0] / TILE_SIZE), (int) Math.floor(entity.pos[1] / TILE_SIZE)};

        //Entity margin from center
        double margin = entity.size / 2.0 + 1;
        //Entity touching tiles
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((entity.pos[0] - margin) / TILE_SIZE)), (int) (Math.floor((entity.pos[0] + margin) / TILE_SIZE))},
            {(int) (Math.floor((entity.pos[1] - margin) / TILE_SIZE)), (int) (Math.floor((entity.pos[1] + margin) / TILE_SIZE))}
        };

        //Checks if on damage tile
        if (!entity.tags.contains("flying")) {
            if (checkAttribute(posIndex[0], posIndex[1], "damageness") && !checkTag(posIndex[0], posIndex[1], "safe")) {
                entity.brain.damage(getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
                if (biome.tags.contains("dangerousVoid")) {
                    entity.brain.damage(0.01 * getAttribute(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
                }
            }
            //Checks if on acel tile
            if (!checkTag(posIndex[0], posIndex[1], "safe") && getAttribute(posIndex[0], posIndex[1], "acel") != 0) {
                entity.vel[0] *= getAttribute(posIndex[0], posIndex[1], "acel");
                entity.vel[1] *= getAttribute(posIndex[0], posIndex[1], "acel");
            }
        }

        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0]) {
                //Checks if touching hurty tile
                if (checkAttribute(marginIndex[0][i], posIndex[1], "hurts")) {
                    entity.brain.damage(0.01 * getAttribute(marginIndex[0][i], posIndex[1], "hurtness") * 30.0 / FPS);
                }
            }
            //Again...
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                if (checkAttribute(posIndex[0], marginIndex[1][i], "hurts")) {
                    entity.brain.damage(0.01 * getAttribute(posIndex[0], marginIndex[1][i], "hurtness") * 30.0 / FPS);
                }
            }
        }
    }

    //Checks if tile or obj has given tag
    public boolean checkTag(int x, int y, String tag) {
        if (objGrid[x][y] == null) {
            return grid[x][y].tags.contains(tag);
        }
        return grid[x][y].tags.contains(tag) || objGrid[x][y].tags.contains(tag);
    }

    //Gives sum of tile and obj value
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

    //Render World
    public void render(Graphics2D g) {
        //Fills background as voidColor
        g.setColor(Rendering.decodeColor(biome.colorMap.get("voidColor")));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        //Center tile on screen
        int[] cameraTile = new int[]{(int) Math.floor(camera[0] / TILE_SIZE), (int) Math.floor(camera[1] / TILE_SIZE)};

        final boolean fog = biome.tags.contains("fog");

        //Runs through all nearby (on screen) tiles to render
        for (int y = cameraTile[1] - renderOffset[1]; y < cameraTile[1] + renderOffset[1]; y++) {
            for (int x = cameraTile[0] - renderOffset[0]; x < cameraTile[0] + renderOffset[0]; x++) {
                if (y >= 0 && y < grid[0].length && x >= 0 && x < grid.length) {
                    //Position of tile on screen
                    double[] screenPos = new double[]{
                        (x * TILE_SIZE - camera[0] + GAME_WIDTH / 2.0),
                        (y * TILE_SIZE - camera[1] + GAME_HEIGHT / 2.0)
                    };
                    //Render tile
                    grid[x][y].render(g, this, screenPos);
                    //If object on grid, render object
                    if (objGrid[x][y] != null) {
                        objGrid[x][y].render(g, this, screenPos);
                    }
                    //If tile/object provides light, add light to HoneySuckle.lights
                    if (fog) {
                        if (grid[x][y].attributes.containsKey("lightRadius") || grid[x][y].attributes.containsKey("glowRadius")) {
                            grid[x][y].renderLight(screenPos);
                            if (navigator.started) {
                                grid[x][y].rendered = true;
                            }
                        }
                        if (objGrid[x][y] != null) {
                            if (objGrid[x][y].attributes.containsKey("lightRadius") || objGrid[x][y].attributes.containsKey("glowRadius")) {
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
        //Renders entities
        for (Entity entity : renderEntities) {
            entity.render(g, camera);
        }
        //Renders projectiles
        for (Projectile proj : renderProjectiles) {
            double[] screenPos = new double[]{
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

    //Updates world
    public void update(InputHandler input) {
        navigator.update(input);
        //Empties renderEntities, then adds nearby entities into renderEntities, and updates them
        renderEntities.clear();
        for (Entity entity : entities) {
            if (entity.tags.contains("alwaysRender") || Math.abs(entity.pos[0] - camera[0]) <= GAME_WIDTH * 3.0 / 4 && Math.abs(entity.pos[1] - camera[1]) <= GAME_HEIGHT * 3.0 / 4) {
                renderEntities.add(entity);
            }
        }
        for (Entity entity : renderEntities) {
            entity.update();
        }
        //Rebuilds entity collision grid from updated renderEntities positions
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
        //Empties renderProjectiles, then adds nearby projectiles into renderProjectiles, and updates them
        renderProjectiles.clear();
        for (Projectile projectile : projectiles) {
            if (Projectile.projTags.get(projectile.type).contains("alwaysRender") || Math.abs(projectile.pos[0] - camera[0]) <= GAME_WIDTH * 3.0 / 4 && Math.abs(projectile.pos[1] - camera[1]) <= GAME_HEIGHT * 3.0 / 4) {
                renderProjectiles.add(projectile);
            }
        }
        for (Projectile projectile : renderProjectiles) {
            projectile.update();
        }
    }
}
