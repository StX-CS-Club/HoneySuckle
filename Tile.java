
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tile {

    public static Map<Integer, Map<String, String>> objTexture = Map.of(
            -4, Map.of(
                    "baseColor", "#ffb31c",
                    "natColor", "pumpkinColor",
                    "texture", "pumpkin"
            ),
            -3, Map.of(
                    "baseColor", "#ffc654",
                    "texture", "torch"
            ),
            -2, Map.of(
                    "baseColor", "#aa6608",
                    "texture", "raft"
            ),
            -1, Map.of(
                    "baseColor", "#893f00",
                    "texture", "wall"
            ),
            1, Map.of(
                    "natColor", "treeColor",
                    "texture", "tree"
            ),
            2, Map.of(
                    "natColor", "pumpkinColor",
                    "texture", "pumpkin"
            ),
            3, Map.of(
                    "natColor", "cactusColor",
                    "texture", "cactus"
            ),
            4, Map.of(
                    "natColor", "coalColor",
                    "texture", "ore"
            )
    );

    public static Map<Integer, Map<String, String>> tileTexture = Map.of(
            0, Map.of(
                    "natColor", "voidColor",
                    "texture", "water"
            ),
            1, Map.of(
                    "natColor", "landColor",
                    "texture", "land"
            ),
            2, Map.of(
                    "natColor", "mudColor",
                    "texture", "mud"
            ),
            3, Map.of(
                    "natColor", "sandColor",
                    "texture", "sand"
            ),
            4, Map.of(
                    "natColor", "iceColor",
                    "texture", "ice"
            )
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
