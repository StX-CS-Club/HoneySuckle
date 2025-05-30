
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Buold.java *
 - Class for managing player's building in world
 - Contains static json data
 */
public class Build {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;
    private static final int HUD_SIZE = HoneySuckle.HUD_SIZE;

    private static final int[] gameSize = new int[]{GAME_WIDTH, GAME_HEIGHT};

    //Static json data
    public static final Map<Integer, List<Map<String, Integer>>> blueprintMats = new HashMap<>();
    public static final Map<Integer, Map<String, List<Integer>>> blueprintParams = new HashMap<>();
    public static final Map<Integer, Map<String, String>> blueprintTextures = new HashMap<>();

    //Build Constructor
    public Build(Set<Integer> blueprints) {
        //Assigns values to properties
        this.blueprints.addAll(blueprints);
    }

    //Build properties
    private final Set<Integer> blueprints = new LinkedHashSet<>(Arrays.asList(-1));
    private int blueprintIndex = 0;

    //Index of cursor tile compared to player
    public double[] cursor = new double[2];

    //Update Build
    public void update(Player player, World world, InputHandler input) {
        for (int i = 0; i < 2; i++) {
            double mouseDiff = input.mousePos[i]
                    - (gameSize[i] / 2.0
                    + Math.floor(player.pos[i] / TILE_SIZE) * TILE_SIZE
                    - world.camera[i]);
            if (mouseDiff < 0) {
                cursor[i] = -1;
            } else if (mouseDiff > TILE_SIZE) {
                cursor[i] = 1;
            } else {
                cursor[i] = 0;
            }
        }
    }

    //Render Build
    public void render(Graphics2D g, World world, Player player) {
        //Tile position of player, with cursor pos added
        int[] index = new int[]{
            (int) Math.floor(player.pos[0] / TILE_SIZE + cursor[0]),
            (int) Math.floor(player.pos[1] / TILE_SIZE + cursor[1])
        };

        //Color of tile
        Color color = Color.red;

        //If can place on tile, be cyan
        if (checkCanPlace(world, player, (int) blueprints.toArray()[blueprintIndex])) {
            color = Color.cyan;
        }

        //Import camera from world
        double[] camera = World.worlds.get(World.level).camera;

        //Render Build Tile
        g.setColor(new Color(0, 0, 0, 0));
        Rendering.borderRect(g, 1, color,
                (int) (GAME_WIDTH / 2.0 + index[0] * TILE_SIZE - camera[0]),
                (int) (GAME_HEIGHT / 2.0 + index[1] * TILE_SIZE - camera[1]),
                TILE_SIZE, TILE_SIZE);
    }

    //Render Build UI
    public void renderUi(Graphics2D g, World world, Player player) {
        //Slot size
        double xMargin = 0;

        //If player is in bottom left corner, render in bottom right
        if (player.screenPos[0] < HUD_SIZE * 3 + TILE_SIZE && player.screenPos[1] > GAME_HEIGHT - HUD_SIZE * 25.0 / 12 - TILE_SIZE) {
            xMargin = GAME_WIDTH - HUD_SIZE * 13.0 / 12;
        }

        //Verification color
        String textureColor = "#ff0000";
        //If have materials, display green verification
        if (hasMaterials(player, (int) blueprints.toArray()[blueprintIndex])) {
            textureColor = "#00ff00";
        }
        //Render blueprint Scroll
        g.drawImage(Rendering.texture("hud/recipe", textureColor), (int) (xMargin + HUD_SIZE / 12.0), (int) (GAME_HEIGHT - HUD_SIZE * 25.0 / 12), HUD_SIZE, HUD_SIZE, null);

        //Render blueprint Item
        Map<String, String> texture = blueprintTextures.get((int) blueprints.toArray()[blueprintIndex]);
        if (texture != null) {
            if (texture.get("texture") != null) {
                g.drawImage(Rendering.texture(texture.get("texture"), "#ffffff"), (int) (xMargin + HUD_SIZE * 5 / 24), (int) (GAME_HEIGHT - HUD_SIZE * 47.0 / 24), HUD_SIZE * 3 / 4, HUD_SIZE * 3 / 4, null);
            }
        }
    }

    //Change selected blueprint based on scroll wheel
    public void scrollBar(double mouseScroll) {
        if (Math.abs(mouseScroll) >= InputHandler.criticalMouseScroll) {
            blueprintIndex += Math.signum(mouseScroll);
            if (blueprintIndex < 0) {
                blueprintIndex = blueprints.size() - 1;
            }
            if (blueprintIndex >= blueprints.size()) {
                blueprintIndex = 0;
            }
        }
    }

    //Build something in the world
    public void build(World world, Player player) {
        //Current selected blueprint
        int blueprintKey = (int) blueprints.toArray()[blueprintIndex];
        //Material data
        List<Map<String, Integer>> blueprint = blueprintMats.get(blueprintKey);

        //Position to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / TILE_SIZE) + cursor[0]),
            (int) (Math.floor(player.pos[1] / TILE_SIZE) + cursor[1])
        };

        //Checks if can place on tile
        if (checkCanPlace(world, player, blueprintKey)) {
            //Places tile
            world.objGrid[index[0]][index[1]] = new WorldObject(blueprintKey, index);
            //Removes materials
            for (Map<String, Integer> material : blueprint) {
                String item = Inventory.itemStringId.get(readMat(material, "item", 0).intValue());
                player.inventory.items.put(item, player.inventory.getMaterial(item) - readMat(material, "count", 1).intValue());
            }
        }
    }

    //Checks if blueprint can be built
    private boolean checkCanPlace(World world, Player player, int blueprintKey) {
        //Unique tags of blueprint
        List<String> tags = WorldObject.objTags.get(blueprintKey);

        //Position trying to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / TILE_SIZE) + cursor[0]),
            (int) (Math.floor(player.pos[1] / TILE_SIZE) + cursor[1])
        };

        //Checks if player has materials
        if (!hasMaterials(player, blueprintKey)) {
            return false;
        }

        //Checks to ensure player can always place on selected tile
        if (tags.contains("placeAway") && cursor[0] == 0 && cursor[1] == 0) {
            return false;
        }

        if (index[0] < 0 || index[0] >= world.size[0] || index[1] < 0 || index[1] >= world.size[1]) {
            return false;
        }

        //If object doesnt exist, only check tile
        if (world.objGrid[index[0]][index[1]] == null) {
            return blueprintParams.get(blueprintKey).get("tile").contains(world.grid[index[0]][index[1]].id);
        }
        //Check object and tile
        return (blueprintParams.get(blueprintKey).get("tile").contains(world.grid[index[0]][index[1]].id)
                && blueprintParams.get(blueprintKey).get("obj").contains(world.objGrid[index[0]][index[1]].id));
    }

    //Check to see if player has materials
    private boolean hasMaterials(Player player, int blueprintKey) {
        //Material data
        List<Map<String, Integer>> blueprint = blueprintMats.get(blueprintKey);

        //Go through all materials needed
        for (Map<String, Integer> material : blueprint) {
            //If don't have, return false
            String matStringId = Inventory.itemStringId.get(readMat(material, "item", 0).intValue());
            if (player.inventory.getMaterial(matStringId)
                    < readMat(material, "count", 1).intValue()) {
                return false;
            }
        }
        //If makes it past check, return true
        return true;
    }

    private Number readMat(Map<String, Integer> mats, String value, Number defaultValue) {
        Number result = mats.get(value);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }
}
