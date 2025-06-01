
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

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
    private static final int RENDER_DISTANCE = HoneySuckle.RENDER_DISTANCE;

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
        if (level > 0) {
            biome = Biome.randomizeBiome(worlds.get(level - 1).biome, level);
        }
        //Generates the world based on the biome
        Biome.generateWorld(this);
        //Sets camera position
        camera = new double[]{(start[0] + 0.5) * TILE_SIZE, (size[1] * TILE_SIZE) - GAME_HEIGHT / 2.0};
        //Adds world to static list of worlds
        worlds.add(this);
    }

    //Camera position for rendering
    public double[] camera = new double[2];

    //World Make-up
    public Tile[][] grid;
    public WorldObject[][] objGrid;
    public List<Entity> entities = new ArrayList<>();
    public List<Projectile> projectiles = new ArrayList<>();

    //Updating entities
    public List<Entity> renderEntities = new ArrayList<>();
    public List<Projectile> renderProjectiles = new ArrayList<>();

    //World attributes
    public int[] size = new int[2];
    public int[] start = new int[2];
    public String biome = "wetlands";

    //Bounds movement to boundaries of world
    public double[] bound(double[] pos, double[] delta, double margin) {
        //Ensures position is within world
        double[] newPos = new double[]{pos[0] + delta[0], pos[1] + delta[1]};
        if (margin <= 0) {
            margin = 0.01;
        }
        for (int i = 0; i < 2; i++) {
            if (newPos[i] < margin) {
                newPos[i] = margin;
            }
            if (newPos[i] > size[i] * TILE_SIZE - margin) {
                newPos[i] = size[i] * TILE_SIZE - margin;
            }
        }
        //AKA Tile
        int[] posIndex = new int[]{(int) (Math.floor(pos[0] / TILE_SIZE)), (int) (Math.floor(pos[1] / TILE_SIZE))};
        int[] newPosIndex = new int[]{(int) (Math.floor(newPos[0] / TILE_SIZE)), (int) (Math.floor(newPos[1] / TILE_SIZE))};

        //Ensures you can walk on next tile
        if (checkTag(posIndex[0], posIndex[1], "walkable") && !checkTag(posIndex[0], posIndex[1], "slippery")) {
            if (newPosIndex[0] >= 0 && newPosIndex[0] < size[0]) {
                if (!checkTag(newPosIndex[0], posIndex[1], "walkable")) {
                    newPos[0] = pos[0];
                }
            }
            if (newPosIndex[1] >= 0 && newPosIndex[1] < size[1]) {
                if (!checkTag(posIndex[0], newPosIndex[1], "walkable")) {
                    newPos[1] = pos[1];
                }
            }
        }

        //AKA Tiles touched
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((newPos[0] - margin) / TILE_SIZE)), (int) (Math.floor((newPos[0] + margin) / TILE_SIZE))},
            {(int) (Math.floor((newPos[1] - margin) / TILE_SIZE)), (int) (Math.floor((newPos[1] + margin) / TILE_SIZE))}};
        //Ensures not touching any obstructions
        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0] && delta[0] != 0) {
                if (checkTag(marginIndex[0][i], posIndex[1], "obstruction")) {
                    newPos[0] = (marginIndex[0][i] + 0.5) * TILE_SIZE + (0.5 * TILE_SIZE + margin) * Math.pow(-1, i);
                }
            }
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1] && delta[1] != 0) {
                if (checkTag(posIndex[0], marginIndex[1][i], "obstruction")) {
                    newPos[1] = (marginIndex[1][i] + 0.5) * TILE_SIZE + (0.5 * TILE_SIZE + margin) * Math.pow(-1, i);
                }
            }
        }
        //Returns bounded pos
        return newPos;
    }

    //Events based on player pos
    public void playerEvent(Player player) {
        //Checks if player is at end of world, then progresses
        if (player.pos[1] <= player.size / 2.0) {
            if (!Biome.biomeTags.get(biome).contains("enemyLock") || entities.isEmpty()) {
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
        if (checkTag(posIndex[0], posIndex[1], "damage") && !checkTag(posIndex[0], posIndex[1], "safe")) {
            player.damage(checkValue(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
            if (Biome.biomeTags.get(biome).contains("dangerousVoid")) {
                player.damage(0.01 * checkValue(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
            }
        }
        //Checks if on acel tile
        if (!checkTag(posIndex[0], posIndex[1], "safe") && checkValue(posIndex[0], posIndex[1], "acel") != 0) {
            player.vel[0] *= checkValue(posIndex[0], posIndex[1], "acel");
            player.vel[1] *= checkValue(posIndex[0], posIndex[1], "acel");
        }
        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0]) {
                //Checks if touching hurty tile
                if (checkTag(marginIndex[0][i], posIndex[1], "hurts")) {
                    player.damage(0.01 * checkValue(marginIndex[0][i], posIndex[1], "hurtness") * 30.0 / FPS);
                }
            }
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                if (checkTag(posIndex[0], marginIndex[1][i], "hurts")) {
                    player.damage(0.01 * checkValue(posIndex[0], marginIndex[1][i], "hurtness") * 30.0 / FPS);
                }
            }
        }

        //Allows entities to interact with player
        for (Entity entity : renderEntities) {
            entity.brain.event(player);
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
        if (checkTag(posIndex[0], posIndex[1], "damage") && !checkTag(posIndex[0], posIndex[1], "safe")) {
            entity.brain.damage(checkValue(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
            if (Biome.biomeTags.get(biome).contains("dangerousVoid")) {
                entity.brain.damage(0.01 * checkValue(posIndex[0], posIndex[1], "damageness") * 30.0 / FPS);
            }
        }
        //Checks if on acel tile
        if (!checkTag(posIndex[0], posIndex[1], "safe") && checkValue(posIndex[0], posIndex[1], "acel") != 0) {
            entity.vel[0] *= checkValue(posIndex[0], posIndex[1], "acel");
            entity.vel[1] *= checkValue(posIndex[0], posIndex[1], "acel");
        }
        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0]) {
                //Checks if touching hurty tile
                if (checkTag(marginIndex[0][i], posIndex[1], "hurts")) {
                    entity.brain.damage(0.01 * checkValue(marginIndex[0][i], posIndex[1], "hurtness") * 30.0 / FPS);
                }
            }
            //Again...
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                if (checkTag(posIndex[0], marginIndex[1][i], "hurts")) {
                    entity.brain.damage(0.01 * checkValue(posIndex[0], marginIndex[1][i], "hurtness") * 30.0 / FPS);
                }
            }
        }
    }

    //Checks if tile or obj has given tag
    private boolean checkTag(int x, int y, String tag) {
        if (objGrid[x][y] == null) {
            return grid[x][y].tags.contains(tag);
        }
        return grid[x][y].tags.contains(tag) || objGrid[x][y].tags.contains(tag);
    }

    //Gives sum of tile and obj value
    private double checkValue(int x, int y, String value) {
        double result = 0;
        result += grid[x][y].attributes.getOrDefault(value, 0).doubleValue();
        if (objGrid[x][y] != null) {
            result += objGrid[x][y].attributes.getOrDefault(value, 0).doubleValue();
        }
        return result;
    }

    //Render World
    public void render(Graphics2D g) {
        //Fills background as voidColor
        g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get("voidColor")));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        //Center tile on screen
        int[] cameraTile = new int[]{(int) Math.floor(camera[0] / TILE_SIZE), (int) Math.floor(camera[1] / TILE_SIZE)};

        final boolean fog = Biome.biomeTags.get(biome).contains("fog");

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
                        if (objGrid[x][y] != null) {
                            if (objGrid[x][y].tags.contains("light")) {
                                objGrid[x][y].renderLight(screenPos);
                            }
                        }
                        if (grid[x][y].tags.contains("light")) {
                            grid[x][y].renderLight(screenPos);
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
        for (Projectile projectile : renderProjectiles) {
            projectile.render(g, camera);
        }
    }

    //Updates world
    public void update() {
        //Empties renderEntities, then adds nearby entities into renderEntities, and updates them
        renderEntities = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.tags.contains("alwaysRender") || Math.abs(entity.pos[0] - camera[0]) <= GAME_WIDTH * 3.0 / 4 && Math.abs(entity.pos[1] - camera[1]) <= GAME_HEIGHT * 3.0 / 4) {
                renderEntities.add(entity);
            }
        }
        for (Entity entity : renderEntities) {
            entity.update();
        }
        //Empties renderProjectiles, then adds nearby projectiles into renderProjectiles, and updates them
        renderProjectiles = new ArrayList<>();
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
