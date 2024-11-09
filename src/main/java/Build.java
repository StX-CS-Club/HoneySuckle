
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

    //Static json data
    public static final Map<Integer, Map<String, Integer>> recipeMats = new HashMap<>();
    public static final Map<Integer, Map<String, List<Integer>>> recipeParams = new HashMap<>();
    public static final Map<Integer, Map<String, String>> recipeTextures = new HashMap<>();

    //Build Constructor
    public Build(Set<Integer> recipes) {
        //Assigns values to properties
        this.recipes.addAll(recipes);
    }

    //Build properties
    private final Set<Integer> recipes = new LinkedHashSet<>(Arrays.asList(-1));
    private int recipeIndex = 0;

    //Index of cursor tile compared to player
    public double[] cursor = new double[2];

    //Update Build
    public void update(Player player, World world, double[] mousePos){
        for (int i = 0; i < 2; i++) {
            double mouseDiff = mousePos[i]
                    - (HoneySuckle.size[i] / 2
                    + Math.floor(player.pos[i] / HoneySuckle.tileSize) * HoneySuckle.tileSize
                    - world.camera[i]);
            if (mouseDiff < 0) {
                cursor[i] = -1;
            } else if (mouseDiff > HoneySuckle.tileSize) {
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
            (int) Math.floor(player.pos[0] / HoneySuckle.tileSize + cursor[0]),
            (int) Math.floor(player.pos[1] / HoneySuckle.tileSize + cursor[1])
        };

        //Color of tile
        Color color = Color.red;

        //If can place on tile, be cyan
        if (checkCanPlace(world, player, (int) recipes.toArray()[recipeIndex])) {
            color = Color.cyan;
        }

        //Import camera from world
        double[] camera = World.worlds.get(World.level).camera;

        //Render Build Tile
        g.setColor(new Color(0, 0, 0, 0));
        Rendering.borderRect(g, 1, color,
                (int) (HoneySuckle.size[0] / 2 + index[0] * HoneySuckle.tileSize - camera[0]),
                (int) (HoneySuckle.size[1] / 2 + index[1] * HoneySuckle.tileSize - camera[1]),
                HoneySuckle.tileSize, HoneySuckle.tileSize);
    }

    //Render Build UI
    public void renderUi(Graphics2D g, World world, Player player) {
        //Slot size
        double size = HoneySuckle.size[0] / 12;
        double xMargin = 0;

        //If player is in bottom left corner, render in bottom right
        if (player.screenPos[0] < size * 3 + HoneySuckle.tileSize && player.screenPos[1] > HoneySuckle.size[1] - size * 25 / 12 - HoneySuckle.tileSize) {
            xMargin = HoneySuckle.size[0] - size * 13 / 12;
        }

        //Verification color
        String textureColor = "#ff0000";
        //If have materials, display green verification
        if (hasMaterials(player, (int) recipes.toArray()[recipeIndex])) {
            textureColor = "#00ff00";
        }
        //Render Recipe Scroll
        g.drawImage(Rendering.texture("hud/recipe", textureColor), (int) (xMargin + size / 12), (int) (HoneySuckle.size[1] - size * 25 / 12), (int) size, (int) size, null);
        
        //Render Recipe Item
        Map<String, String> texture = recipeTextures.get((int) recipes.toArray()[recipeIndex]);
        if (texture != null) {
            if (texture.get("texture") != null) {            
                g.drawImage(Rendering.texture(texture.get("texture"), "#ffffff"), (int) (xMargin + size*5/24), (int) (HoneySuckle.size[1] - size * 47 / 24), (int) size*3/4, (int) size*3/4, null);
            }
        }
    }

    //Change selected recipe based on scroll wheel
    public void scrollBar(double scroll) {
        if (Math.abs(scroll) >= 1) {
            recipeIndex += Math.signum(scroll);
            if (recipeIndex < 0) {
                recipeIndex = recipes.size() - 1;
            }
            if (recipeIndex >= recipes.size()) {
                recipeIndex = 0;
            }
        }
    }

    //Build something in the world
    public void build(World world, Player player) {
        //Current selected recipe
        int recipeKey = (int) recipes.toArray()[recipeIndex];
        Map<String, Integer> recipe = recipeMats.get(recipeKey);
        Set<String> requiredMat = recipe.keySet();

        //Position to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / HoneySuckle.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / HoneySuckle.tileSize) + cursor[1])
        };

        //Checks if can place on tile
        if (checkCanPlace(world, player, recipeKey)) {
            //Places tile
            world.objGrid[index[0]][index[1]] = new WorldObject(recipeKey, index);
            //Removes materials
            for (String material : requiredMat) {
                player.inventory.items.put(material, getOrDefault(player.inventory.items, material) - recipe.get(material));
            }
        }
    }

    //Checks if recipe can be built
    private boolean checkCanPlace(World world, Player player, int recipeKey) {
        //Unique tags of recipe
        List<String> Tags = WorldObject.objTags.get(recipeKey);

        //Position trying to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / HoneySuckle.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / HoneySuckle.tileSize) + cursor[1])
        };
  
        //Checks if player has materials
        if(!hasMaterials(player, recipeKey)){
            return false;
        }

        //Checks to ensure player can always place on selected tile
        if (Tags.contains("placeAway") && cursor[0] == 0 && cursor[1] == 0) {
            return false;
        }

        if (index[0] < 0 || index[0] >= world.size[0] || index[1] < 0 || index[1] >= world.size[1]) {
            return false;
        }

        //If object doesnt exist, only check tile
        if (world.objGrid[index[0]][index[1]] == null) {
            return recipeParams.get(recipeKey).get("tile").contains(world.grid[index[0]][index[1]]);
        }
        //Check object and tile
        return (recipeParams.get(recipeKey).get("tile").contains(world.grid[index[0]][index[1]])
                && recipeParams.get(recipeKey).get("obj").contains(world.objGrid[index[0]][index[1]].id));
    }
 
    //Check to see if player has materials
    private boolean hasMaterials(Player player, int recipeKey){ 
        //Material data
        Map<String, Integer> recipe = recipeMats.get(recipeKey);
        Set<String> requiredMat = recipe.keySet();

        //Go through all materials needed
        for (String material : requiredMat) {
            //If don't have, return false
            if (getOrDefault(player.inventory.items, material) < recipe.get(material)) {
                return false;
            }
        }
        //If makes it past check, return true
        return true;
    }

    //Null safe way of reading materials map
    public int getOrDefault(Map<String, Integer> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return 0;
        }
    }
}
