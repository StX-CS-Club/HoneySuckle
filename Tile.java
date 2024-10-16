
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tile {

    public static Map<Integer, String> objTexture = Map.of(
            1, "tree"
    );

    public static Map<Integer, String> tileTexture = Map.of(
        2, "mud"
    );

    public static Map<Integer, String> objColor = Map.of(
            -1, "#893f00",
            -2, "#aa6608",
            -3, "#ffc654",
            -4, "#ffb31c"
    );

    public static Map<Integer, String> natObjColor = Map.of(
            1, "treeColor",
            2, "pumpkinColor",
            3, "cactusColor",
            4, "coalColor"
    );

    public static Map<Integer, String> natTileColor = Map.of(
            1, "landColor",
            2, "mudColor",
            3, "sandColor",
            4, "iceColor"
    );

    public static Map<Integer, Map<String, Integer>> recipes = Map.of(
            -1, Map.of(
                    "wood", 1
            ),
            -2, Map.of(
                    "wood", 2
            ),
            -3, Map.of(
                    "wood", 1,
                    "coal", 1
            ),
            -4, Map.of(
                    "pumpkin", 1,
                    "coal", 1,
                    "wood", 1
            )
    );

    public static Map<Integer, String> objName = Map.of(
            -1, "Wall",
            -2, "Raft",
            -3, "Torch",
            -4, "jack-o-lantern"
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
            4, Map.of(
                    "coal", 2
            ),
            -1, Map.of(
                    "wood", 1
            ),
            -2, Map.of(),
            -3, Map.of(
                    "wood", 1
            ),
            -4, Map.of(
                    "pumpkin", 1
            )
    );

    public static Map<Integer, Map<String, List<Integer>>> objParams = Map.of(
            -1, Map.of(
                    "grid", new ArrayList<>(Arrays.asList(1, 2, 3, 4)),
                    "objGrid", new ArrayList<>(Arrays.asList(0))
            ),
            -2, Map.of(
                    "grid", new ArrayList<>(Arrays.asList(0, 2)),
                    "objGrid", new ArrayList<>(Arrays.asList(0))
            ),
            -3, Map.of(
                    "grid", new ArrayList<>(Arrays.asList(1, 2, 3)),
                    "objGrid", new ArrayList<>(Arrays.asList(0))
            ),
            -4, Map.of(
                    "grid", new ArrayList<>(Arrays.asList(1, 2, 3)),
                    "objGrid", new ArrayList<>(Arrays.asList(0))
            )
    );

    public static Map<Integer, List<String>> tileTags = Map.of(
            0, new ArrayList<>(Arrays.asList("damage")),
            1, new ArrayList<>(Arrays.asList("walkable")),
            2, new ArrayList<>(Arrays.asList("walkable", "slow")),
            3, new ArrayList<>(Arrays.asList("walkable", "slow")),
            4, new ArrayList<>(Arrays.asList("walkable", "slippery"))
    );

    public static Map<Integer, Map<String, Integer>> tileValues = Map.of(
            0, Map.of(
                    "damageness", 1
            ),
            1, Map.of(),
            2, Map.of(
                    "slowness", 2
            ),
            3, Map.of(
                    "slowness", 3
            ),
            4, Map.of(
                    "slippieness", 5
            )
    );

    public static Map<Integer, List<String>> objTags = Map.of(
            0, new ArrayList<>(),
            1, new ArrayList<>(Arrays.asList("obstruction", "destructable")),
            2, new ArrayList<>(Arrays.asList("obstruction", "destructable")),
            3, new ArrayList<>(Arrays.asList("obstruction", "hurts", "destructable")),
            4, new ArrayList<>(Arrays.asList("obstruction", "destructable")),
            -1, new ArrayList<>(Arrays.asList("obstruction", "destructable", "placeAway")),
            -2, new ArrayList<>(Arrays.asList("walkable", "safe", "destructable", "breakAway")),
            -3, new ArrayList<>(Arrays.asList("destructable", "light")),
            -4, new ArrayList<>(Arrays.asList("obstruction", "destructable", "light"))
    );

    public static Map<Integer, Map<String, Integer>> objValues = Map.of(
            0, Map.of(),
            1, Map.of(),
            2, Map.of(),
            3, Map.of(
                    "hurtness", 2
            ),
            4, Map.of(),
            -1, Map.of(),
            -2, Map.of(),
            -3, Map.of(
                    "light", 8
            ),
            -4, Map.of(
                    "light", 15
            )
    );
}
