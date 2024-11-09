import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Tile.java *
 - Class for managing world tiles
 - Static json data
 */

public class Tile {
    //Static json data
    public static final Map<Integer, List<String>> tileTags = new HashMap<>();
    public static final Map<Integer, Map<String, Double>> tileValues = new HashMap<>();
    public static final Map<Integer, Map<String, String>> tileTextures = new HashMap<>();

    //Tile Constructor
    public Tile(int id, int[] posIndex){
        
    }
}
