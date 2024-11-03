import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldObject {
    public static Map<Integer, List<String>> objTags = new HashMap<>();
    public static Map<Integer, Map<String, Double>> objValues = new HashMap<>();
    public static Map<Integer, Map<String, Integer>> objLoot = new HashMap<>();
    public static Map<Integer, Map<String, String>> objTextures = new HashMap<>();

    public int id;
    public int[] posIndex;

    public double durability;
    public List<String> tags;
    public Map<String, Double> values;
    public Map<String, String> texture;
    public Map<String, Integer> loot;

    public WorldObject(int id, int[] posIndex){
        this.id = id;
        this.posIndex = posIndex;
        tags = objTags.get(id);
        if(tags.contains("destructable")){
        durability = objValues.get(id).get("durability");
        }
        values = objValues.get(id);
        texture = objTextures.get(id);
        loot = objLoot.get(id);
    }

    public boolean damage(double damage){
        if(!tags.contains("destructable")){
            return false;
        }
        durability -= damage;
        if(durability > 0){
            return false;
        }
        World.worlds.get(World.level).objGrid[posIndex[0]][posIndex[1]] = null;
        return true;
    }
}
