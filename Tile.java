
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tile {

    public static Map<Integer, String> objColor = Map.of(
            -1, "#893f00",
            -2, "#aa6608"
    );

    public static Map<Integer, String> natObjColor = Map.of(
        1, "treeColor",
        2, "pumpkinColor",
        3, "cactusColor"
    );

    public static Map<Integer, String> natTileColor = Map.of(
        1, "landColor",
        2, "mudColor",
        3, "sandColor"
    );

    public static Map<Integer, Map<String, Integer>> recipes = Map.of(
        -1, Map.of(
            "wood", 1
        ),
        -2, Map.of(
            "wood", 2
        )
    );

    public static Map<Integer, String> objName = Map.of(
        -1, "Wall",
        -2, "Raft"
    );

    public static Map<Integer, Map<String, Integer>> objLoot = Map.of(
        0, Map.of(),
        1, Map.of(
            "wood", 1
        ),
        2, Map.of(
            "pumpkin", 1
        ),
        3, Map.of(
            "cactus", 1
        ),
        -1, Map.of(
            "wood", 1
        ),
        -2, Map.of()
    );

    public static Map<Integer, Map<String, List<Integer>>> objParams = Map.of(
        -1, Map.of(
            "grid", new ArrayList<>(Arrays.asList(1,2,3)),
            "objGrid", new ArrayList<>(Arrays.asList(0))
        ),
        -2, Map.of(
            "grid", new ArrayList<>(Arrays.asList(0,2)),
            "objGrid", new ArrayList<>(Arrays.asList(0))
        )
    );

    public static Map<Integer, List<String>> tileProperties = Map.of(
        0, new ArrayList<>(Arrays.asList("damage")),
        1, new ArrayList<>(Arrays.asList("walkable")),
        2, new ArrayList<>(Arrays.asList("walkable", "slow")),
        3, new ArrayList<>(Arrays.asList("walkable", "slow"))
    );

    public static Map<Integer, List<String>> objProperties = Map.of(
        0, new ArrayList<>(),
        1, new ArrayList<>(Arrays.asList("obstruction", "destructable")),
        2, new ArrayList<>(Arrays.asList("obstruction", "destructable")),
        3, new ArrayList<>(Arrays.asList("obstruction", "hurts", "destructable")),
        -1, new ArrayList<>(Arrays.asList("obstruction", "destructable", "placeAway")),
        -2, new ArrayList<>(Arrays.asList("walkable", "safe", "destructable", "breakAway"))
    );
}
