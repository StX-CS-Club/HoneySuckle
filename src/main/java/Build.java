
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
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
    public static final Map<String, List<Map<String, Number>>> blueprintMats = new HashMap<>();
    public static final Map<String, Map<String, List<Number>>> blueprintParams = new HashMap<>();
    public static final Map<String, Map<String, String>> blueprintTextures = new HashMap<>();
    public static final Map<String, Integer> blueprintProducts = new HashMap<>();
    public static final Map<String, List<String>> blueprintTags = new HashMap<>();

    //Build Constructor
    public Build(Set<String> blueprints) {
        //Assigns values to properties
        this.blueprints.addAll(blueprints);
    }

    //Build properties
    public final Set<String> blueprints = new LinkedHashSet<>(Arrays.asList("wall"));
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
        if (checkCanPlace(world, player, (String) blueprints.toArray()[blueprintIndex])) {
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
        if (player.screenPos[0] < HUD_SIZE * 3 + TILE_SIZE && player.screenPos[1] > GAME_HEIGHT - HUD_SIZE * 25 / 12 - TILE_SIZE) {
            xMargin = GAME_WIDTH - HUD_SIZE * 3;
        }

        //Verification color
        String textureColor = "#ff0000";
        //If have materials, display green verification
        if (hasMaterials(player, (String) blueprints.toArray()[blueprintIndex])) {
            textureColor = "#00ff00";
        }
        //Render blueprint Scroll
        g.drawImage(Rendering.texture("hud/recipe", textureColor), (int) (xMargin + HUD_SIZE / 12.0), (int) (GAME_HEIGHT - HUD_SIZE * 25.0 / 12), HUD_SIZE, HUD_SIZE, null);

        //Render blueprint Item
        String blueprintKey = (String) blueprints.toArray()[blueprintIndex];
        Map<String, String> texture = blueprintTextures.get(blueprintKey);
        if (texture != null) {
            if (texture.get("texture") != null) {
                g.drawImage(Rendering.texture(texture.get("texture"), "#ffffff"), (int) (xMargin + HUD_SIZE * 5 / 24), (int) (GAME_HEIGHT - HUD_SIZE * 47.0 / 24), HUD_SIZE * 3 / 4, HUD_SIZE * 3 / 4, null);
            }
        }

        List<Map<String, Number>> blueprint = blueprintMats.get(blueprintKey);
        for (int i = 0; i < blueprint.size(); i++) {
            Map<String, Number> mat = blueprint.get(i);
            String itemId = Item.itemStringId.get(mat.get("item").intValue());
            String name = Item.itemNames.get(itemId);
            int itemCount = mat.getOrDefault("count", 1).intValue();

            g.setFont(new Font("Dialog", Font.PLAIN, 16));
            if (player.inventory.getItemCount(itemId) >= itemCount) {
                g.setColor(Color.GREEN);
                g.drawString("✓ " + name + " x" + itemCount, (int) (xMargin + HUD_SIZE * 7 / 6), (int) (GAME_HEIGHT - HUD_SIZE * 13 / 12 - i * 18));
            } else {
                g.setColor(Color.RED);
                g.drawString("✕ " + name + " x" + itemCount, (int) (xMargin + HUD_SIZE * 7 / 6), (int) (GAME_HEIGHT - HUD_SIZE * 13 / 12 - i * 18));
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
        String blueprintKey = (String) blueprints.toArray()[blueprintIndex];
        //Material data
        List<Map<String, Number>> blueprint = blueprintMats.get(blueprintKey);

        //Position to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / TILE_SIZE) + cursor[0]),
            (int) (Math.floor(player.pos[1] / TILE_SIZE) + cursor[1])
        };

        //Checks if can place on tile
        if (checkCanPlace(world, player, blueprintKey)) {
            //Places tile
            world.objGrid[index[0]][index[1]] = new WorldObject(blueprintProducts.get(blueprintKey), index, world);
            //Removes materials
            for (Map<String, Number> material : blueprint) {
                player.inventory.incrementItem(material, false);
            }
        }
    }

    //Checks if blueprint can be built
    private boolean checkCanPlace(World world, Player player, String blueprintKey) {
        //Unique tags of blueprint
        List<String> tags = blueprintTags.get(blueprintKey);

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

        final List<Number> tileList = blueprintParams.get(blueprintKey).getOrDefault("tile", new ArrayList<>());
        final List<Number> objList = blueprintParams.get(blueprintKey).getOrDefault("obj", List.of(0));
        //If object doesnt exist, only check tile
        if (world.objGrid[index[0]][index[1]] == null) {
            return tileList.contains(world.grid[index[0]][index[1]].id) && objList.contains(0);
        }
        //Check object and tile
        return (tileList.contains(world.grid[index[0]][index[1]].id)
                && objList.contains(world.objGrid[index[0]][index[1]].id));
    }

    //Check to see if player has materials
    private boolean hasMaterials(Player player, String blueprintKey) {
        //Material data
        List<Map<String, Number>> blueprint = blueprintMats.get(blueprintKey);

        //Go through all materials needed
        for (Map<String, Number> material : blueprint) {
            //If don't have, return false
            String matStringId = Item.itemStringId.get(readMat(material, "item", 0).intValue());
            if (player.inventory.getItemCount(matStringId)
                    < readMat(material, "count", 1).intValue()) {
                return false;
            }
        }
        //If makes it past check, return true
        return true;
    }

    private Number readMat(Map<String, Number> mats, String value, Number defaultValue) {
        Number result = mats.get(value);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }
}
