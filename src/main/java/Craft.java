
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Craft {

    public static final Map<Integer, Map<String, Integer>> recipeMats = new HashMap<>();
    public static final Map<Integer, Map<String, List<Integer>>> recipeParams = new HashMap<>();
    public static final Map<Integer, Map<String, String>> recipeTextures = new HashMap<>();

    public Craft(Map<String, Integer> materials, Set<Integer> recipes) {
        this.materials.putAll(materials);
        this.recipes.addAll(recipes);
    }

    public final Map<String, Integer> materials = new HashMap<>();

    private final Set<Integer> recipes = new LinkedHashSet<>(Arrays.asList(-1));
    private int recipeIndex = 0;

    public double[] cursor = new double[2];

    public void renderTile(Graphics2D g, World world, Player player){
        int[] index = new int[]{
            (int) Math.floor(player.pos[0] / HoneySuckle.tileSize + cursor[0]),
            (int) Math.floor(player.pos[1] / HoneySuckle.tileSize + cursor[1])
        };

        Color color = Color.red;

        if (checkCanPlace(world, player, (int) recipes.toArray()[recipeIndex])) {
            color = Color.cyan;
        }

        double[] camera = World.worlds.get(World.level).camera;

        g.setColor(new Color(0, 0, 0, 0));
        Rendering.borderRect(g, 1, color,
                (int) (HoneySuckle.size[0] / 2 + index[0] * HoneySuckle.tileSize - camera[0]),
                (int) (HoneySuckle.size[1] / 2 + index[1] * HoneySuckle.tileSize - camera[1]),
                HoneySuckle.tileSize, HoneySuckle.tileSize);
    }

    public void render(Graphics2D g, World world, Player player) {
        double size = HoneySuckle.size[0] / 12;
        double xMargin = 0;
        if (player.screenPos[0] < size * 3 + HoneySuckle.tileSize && player.screenPos[1] > HoneySuckle.size[1] - size*25/12 - HoneySuckle.tileSize) {
            xMargin = HoneySuckle.size[0] - size * 13 / 12;
        }

        String textureColor = "#ff0000";
        if(hasMaterials(player, (int) recipes.toArray()[recipeIndex])){
            textureColor = "#00ff00";
        }
        g.drawImage(Rendering.texture("hud/recipe", textureColor), (int) (xMargin + size / 12), (int) (HoneySuckle.size[1] - size * 25 / 12), (int) size, (int) size, null);
        Map<String, String> texture = recipeTextures.get((int) recipes.toArray()[recipeIndex]);
        if (texture != null) {
            if (texture.get("texture") != null) {
                g.drawImage(Rendering.texture(texture.get("texture"), "#ffffff"), (int) (xMargin + size*5/24), (int) (HoneySuckle.size[1] - size * 47 / 24), (int) size*3/4, (int) size*3/4, null);
            }
        }
    }

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

    public void build(World world, Player player) {
        int recipeKey = (int) recipes.toArray()[recipeIndex];
        Map<String, Integer> recipe = recipeMats.get(recipeKey);
        Set<String> requiredMat = recipe.keySet();

        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / HoneySuckle.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / HoneySuckle.tileSize) + cursor[1])
        };

        if (checkCanPlace(world, player, recipeKey)) {
            world.objGrid[index[0]][index[1]] = new WorldObject(recipeKey, index);
            for (String material : requiredMat) {
                materials.put(material, getOrDefault(material) - recipe.get(material));
            }
        }
    }

    private boolean checkCanPlace(World world, Player player, int recipeKey) {
        List<String> Tags = WorldObject.objTags.get(recipeKey);
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / HoneySuckle.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / HoneySuckle.tileSize) + cursor[1])
        };

        if(!hasMaterials(player, recipeKey)){
            return false;
        }

        if (Tags.contains("placeAway") && cursor[0] == 0 && cursor[1] == 0) {
            return false;
        }

        if (index[0] < 0 || index[0] >= world.size[0] || index[1] < 0 || index[1] >= world.size[1]) {
            return false;
        }
        if (world.objGrid[index[0]][index[1]] == null) {
            return recipeParams.get(recipeKey).get("tile").contains(world.grid[index[0]][index[1]]);
        }
        return (recipeParams.get(recipeKey).get("tile").contains(world.grid[index[0]][index[1]])
                && recipeParams.get(recipeKey).get("obj").contains(world.objGrid[index[0]][index[1]].id));
    }

    private boolean hasMaterials(Player player, int recipeKey){ 
        Map<String, Integer> recipe = recipeMats.get(recipeKey);
        Set<String> requiredMat = recipe.keySet();
        for (String material : requiredMat) {
            if (getOrDefault(material) < recipe.get(material) && !player.tags.contains("god")) {
                return false;
            }
        }
        return true;
    }

    public int getOrDefault(String key) {
        if (materials.containsKey(key)) {
            return materials.get(key);
        } else {
            return 0;
        }
    }
}
