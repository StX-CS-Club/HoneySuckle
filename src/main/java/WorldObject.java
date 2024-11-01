import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldObject {
    public static Map<Integer, List<String>> objTags = new HashMap<>();
    public static Map<Integer, Map<String, Integer>> objValues = new HashMap<>();
    public static Map<Integer, Map<String, Integer>> objLoot = new HashMap<>();
    public static Map<Integer, Map<String, String>> objTextures = new HashMap<>();

    public int id;

    public WorldObject(int id){
        this.id = id;
    }
}
