
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* 
 * World.java *
 - Class used for managing world
 - Manages tiles and objects
 - Manages entities and projectiles
 - Update and Render Methods
 */
public class World {

    //Static variables
    public static List<World> worlds = new ArrayList<>();
    public static int level = 0;
    private static final int tileSize = HoneySuckle.tileSize;

    //World Constructor
    public World() {
        //Pseudo-Randomized Biome
        randomizeBiome();
        //Makes repetition less likely
        if (!worlds.isEmpty()) {
            if (worlds.get(level - 1).biome.equals(biome)) {
                randomizeBiome();
            }
        }
        //Generates the world based on teh biome
        Biome.biomeGeneration(this);
        //Sets camera position
        camera = new double[]{(start + 0.5) * tileSize, (size[1] * tileSize) - HoneySuckle.size[1] / 2};
        //Adds world to static list of worlds
        worlds.add(this);
    }

    private void randomizeBiome() {
        //Runs progressive randomization
        if (level < Biome.biomes.length) {
            biome = Biome.biomes[(int) Math.floor(Math.random() * level)];
        } else {
            biome = Biome.biomes[(int) Math.floor(Math.random() * Biome.biomes.length)];
        }
    }

    //Camera position for rendering
    public double[] camera = new double[2];

    //World Make-up
    public int[][] grid;
    public WorldObject[][] objGrid;
    public List<Entity> entities = new ArrayList<>();
    public List<Projectile> projectiles = new ArrayList<>();

    //Updating entities
    public List<Entity> renderEntities = new ArrayList<>();
    public List<Projectile> renderProjectiles = new ArrayList<>();

    //World attributes
    public int[] size = new int[2];
    public int start;
    public String biome;

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
            if (newPos[i] > size[i] * tileSize - margin) {
                newPos[i] = size[i] * tileSize - margin;
            }
        }
        //AKA Tile
        int[] posIndex = new int[]{(int) (Math.floor(pos[0] / tileSize)), (int) (Math.floor(pos[1] / tileSize))};
        int[] newPosIndex = new int[]{(int) (Math.floor(newPos[0] / tileSize)), (int) (Math.floor(newPos[1] / tileSize))};

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
            {(int) (Math.floor((newPos[0] - margin) / tileSize)), (int) (Math.floor((newPos[0] + margin) / tileSize))},
            {(int) (Math.floor((newPos[1] - margin) / tileSize)), (int) (Math.floor((newPos[1] + margin) / tileSize))}};
        //Ensures not touching any obstructions
        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0] && delta[0] != 0) {
                if (checkTag(marginIndex[0][i], posIndex[1], "obstruction")) {
                    newPos[0] = (marginIndex[0][i] + 0.5) * tileSize + (0.5 * tileSize + margin) * Math.pow(-1, i);
                }
            }
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1] && delta[1] != 0) {
                if (checkTag(posIndex[0], marginIndex[1][i], "obstruction")) {
                    newPos[1] = (marginIndex[1][i] + 0.5) * tileSize + (0.5 * tileSize + margin) * Math.pow(-1, i);
                }
            }
        }
        //Returns bounded pos
        return newPos;
    }

    //Events based on player pos
    public void playerEvent(Player player) {
        //Checks if player is at end of world, then progresses
        if (player.pos[1] <= player.size / 2) {
            if (!Biome.biomeTags.get(biome).contains("enemyLock") || entities.isEmpty()) {
                level++;
                World world = new World();
                player.pos = new double[]{tileSize * (world.start + 0.5), tileSize * (world.size[1] - 0.5)};
                return;
            }
        }
        //Player Tile
        int[] posIndex = new int[]{(int) Math.floor(player.pos[0] / tileSize), (int) Math.floor(player.pos[1] / tileSize)};

        //Player margin from center
        double margin = player.size / 2 + 1;
        //Player touching tiles
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((player.pos[0] - margin) / tileSize)), (int) (Math.floor((player.pos[0] + margin) / tileSize))},
            {(int) (Math.floor((player.pos[1] - margin) / tileSize)), (int) (Math.floor((player.pos[1] + margin) / tileSize))}
        };

        //Checks if on damage tile
        if (checkTag(posIndex[0], posIndex[1], "damage") && !checkTag(posIndex[0], posIndex[1], "safe")) {
            player.damage(checkValue(posIndex[0], posIndex[1], "damageness") * 30 / HoneySuckle.fps);
            if (Biome.biomeTags.get(biome).contains("dangerousVoid")) {
                player.damage(0.01 * checkValue(posIndex[0], posIndex[1], "damageness") * 30 / HoneySuckle.fps);
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
                    player.damage(0.01 * checkValue(marginIndex[0][i], posIndex[1], "hurtness") * 30 / HoneySuckle.fps);
                }
            }
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                if (checkTag(posIndex[0], marginIndex[1][i], "hurts")) {
                    player.damage(0.01 * checkValue(posIndex[0], marginIndex[1][i], "hurtness") * 30 / HoneySuckle.fps);
                }
            }
        }

        //Allows entities to interact with player
        for (Entity entity : renderEntities) {
            Brain.event(entity, player);
        }
    }

    //Events based on entity
    public void entityEvent(Entity entity) {
        //Entity tile
        int[] posIndex = new int[]{(int) Math.floor(entity.pos[0] / tileSize), (int) Math.floor(entity.pos[1] / tileSize)};

        //Entity margin from center
        double margin = entity.size / 2 + 1;
        //Entity touching tiles
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((entity.pos[0] - margin) / tileSize)), (int) (Math.floor((entity.pos[0] + margin) / tileSize))},
            {(int) (Math.floor((entity.pos[1] - margin) / tileSize)), (int) (Math.floor((entity.pos[1] + margin) / tileSize))}
        };

        //Checks if on damage tile
        if (checkTag(posIndex[0], posIndex[1], "damage") && !checkTag(posIndex[0], posIndex[1], "safe")) {
            entity.damage(checkValue(posIndex[0], posIndex[1], "damageness") * 30 / HoneySuckle.fps);
            if (Biome.biomeTags.get(biome).contains("dangerousVoid")) {
                entity.damage(0.01 * checkValue(posIndex[0], posIndex[1], "damageness") * 30 / HoneySuckle.fps);
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
                    entity.damage(0.01 * checkValue(marginIndex[0][i], posIndex[1], "hurtness") * 30 / HoneySuckle.fps);
                }
            }
            //Again...
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                if (checkTag(posIndex[0], marginIndex[1][i], "hurts")) {
                    entity.damage(0.01 * checkValue(posIndex[0], marginIndex[1][i], "hurtness") * 30 / HoneySuckle.fps);
                }
            }
        }
    }

    //Checks if tile or obj has given tag
    private boolean checkTag(int x, int y, String tag) {
        if (objGrid[x][y] == null) {
            return Tile.tileTags.get(grid[x][y]).contains(tag);
        }
        return Tile.tileTags.get(grid[x][y]).contains(tag) || objGrid[x][y].tags.contains(tag);
    }

    //Gives sum of tile and obj value
    private double checkValue(int x, int y, String value) {
        double result = 0;
        if (Tile.tileValues.get(grid[x][y]).get(value) != null) {
            result += Tile.tileValues.get(grid[x][y]).get(value);
        }
        if (objGrid[x][y] != null) {
            if (objGrid[x][y].values.get(value) != null) {
                result += objGrid[x][y].values.get(value);
            }
        }
        return result;
    }

    //Render World
    public void render(Graphics2D g) {
        //Fills background as voidColor
        g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get("voidColor")));
        g.fillRect(0, 0, HoneySuckle.size[0], HoneySuckle.size[1]);

        //Center tile on screen
        int[] cameraTile = new int[]{(int) Math.floor(camera[0] / tileSize), (int) Math.floor(camera[1] / tileSize)};
        //Size of screen in tiles
        int[] cameraOffset = new int[]{(HoneySuckle.size[0] / 2 / tileSize + 5), (HoneySuckle.size[1] / 2 / tileSize + 5)};

        //Runs through all nearby (on screen) tiles to render
        for (int y = cameraTile[1] - cameraOffset[1]; y < cameraTile[1] + cameraOffset[1]; y++) {
            for (int x = cameraTile[0] - cameraOffset[0]; x < cameraTile[0] + cameraOffset[0]; x++) {
                if (y >= 0 && y < grid[0].length && x >= 0 && x < grid.length) {
                    //Position of tile on screen
                    double[] screenPos = new double[]{
                        (x * tileSize - camera[0] + HoneySuckle.size[0] / 2),
                        (y * tileSize - camera[1] + HoneySuckle.size[1] / 2)
                    };
                    //Texture data for tile
                    Map<String, String> textureMap = Tile.tileTextures.get(grid[x][y]);
                    //Default color of pure white
                    String color = "#ffffff";
                    //If tile has biome specific color, find color from biome
                    if (textureMap.get("natColor") != null) {
                        if (Biome.biomeColorMap.get(biome).get(textureMap.get("natColor")) != null) {
                            color = Biome.biomeColorMap.get(biome).get(textureMap.get("natColor"));
                        }
                        //If tile has listed base color, set as color
                    } else if (textureMap.get("baseColor") != null) {
                        color = textureMap.get("baseColor");
                    }
                    //If tile has texture, load texture with grey-scaling
                    if (textureMap.get("texture") != null) {
                        String texture = textureMap.get("texture");
                        g.drawImage(Rendering.texture(texture, color), (int) screenPos[0], (int) screenPos[1], tileSize, tileSize, null);
                    } else {
                        //Else, render basic rectangle
                        g.setColor(Color.decode(color));
                        Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], tileSize, tileSize);
                    }
                    //If object on grid, render object
                    if (objGrid[x][y] != null) {
                        objGrid[x][y].render(g, this, screenPos, tileSize);
                    }
                    //If tile/object provides light, add light to HoneySuckle.lights
                    if (checkTag(x, y, "light")) {
                        HoneySuckle.lights.add(Map.of(
                                "posX", (int) screenPos[0] + tileSize / 2,
                                "posY", (int) screenPos[1] + tileSize / 2,
                                "radius", HoneySuckle.tileSize * (int) checkValue(x, y, "light"),
                                "color", (255 << 16) | (140 << 8)
                        ));
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
            if (entity.tags.contains("alwaysRender") || Math.abs(entity.pos[0] - camera[0]) <= HoneySuckle.size[0] * 3 / 4 && Math.abs(entity.pos[1] - camera[1]) <= HoneySuckle.size[1] * 3 / 4) {
                renderEntities.add(entity);
            }
        }
        for (Entity entity : renderEntities) {
            entity.update();
        }
        //Empties renderProjectiles, then adds nearby projectiles into renderProjectiles, and updates them
        renderProjectiles = new ArrayList<>();
        for (Projectile projectile : projectiles) {
            if (Projectile.projTags.get(projectile.type).contains("alwaysRender") || Math.abs(projectile.pos[0] - camera[0]) <= HoneySuckle.size[0] * 3 / 4 && Math.abs(projectile.pos[1] - camera[1]) <= HoneySuckle.size[1] * 3 / 4) {
                renderProjectiles.add(projectile);
            }
        }
        for (Projectile projectile : renderProjectiles) {
            projectile.update();
        }
    }
}
