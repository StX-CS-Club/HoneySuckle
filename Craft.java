
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

public class Craft {

    public Craft(Map<String, Integer> materials) {
        this.materials.putAll(materials);
    }

    private Map<String, Integer> materials = new HashMap<>();

    public double[] cursor = new double[2];

    public void render(Graphics2D g, double[] pos) {
        g.setColor(new Color(0, 0, 0, 0));

        double[] camera = World.worlds.get(World.level).camera;

        HoneySuckle.borderRect(g, 2, Color.red,
                (int) (HoneySuckle.size[0] / 2
                + Math.floor(pos[0] / HoneySuckle.tileSize + cursor[0]) * HoneySuckle.tileSize
                - camera[0]),
                (int) (HoneySuckle.size[1] / 2
                + Math.floor(pos[1] / HoneySuckle.tileSize + cursor[1]) * HoneySuckle.tileSize
                - camera[1]),
                HoneySuckle.tileSize, HoneySuckle.tileSize);
    }

    public void destroy(World world, Player player){
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / HoneySuckle.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / HoneySuckle.tileSize) + cursor[1])
        };
        if (index[0] >= 0 && index[0] < world.size[0] && index[1] >= 0 && index[1] < world.size[1]) {
            if(world.objGrid[index[0]][index[1]] == 1 || world.objGrid[index[0]][index[1]] == -1){
                world.objGrid[index[0]][index[1]] = 0;
                materials.put("wood", getOrDefault("wood") + 1);
                System.out.println(materials.get("wood"));
            }
        }
    }

    public void build(World world, Player player){
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / HoneySuckle.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / HoneySuckle.tileSize) + cursor[1])
        };
        if (index[0] >= 0 && index[0] < world.size[0] && index[1] >= 0 && index[1] < world.size[1]) {
            if(world.grid[index[0]][index[1]] == 1 && getOrDefault("wood") > 0){
                world.objGrid[index[0]][index[1]] = -1;
                materials.put("wood", getOrDefault("wood") - 1);
                System.out.println(materials.get("wood"));
            }
        }
    }

    private int getOrDefault(String key){
        if(materials.containsKey(key)){
            return materials.get(key);
        } else {
            return 0;
        }
    }
}
