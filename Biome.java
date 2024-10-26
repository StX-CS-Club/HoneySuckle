
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Biome {

    public static String[] biomes = new String[]{
        "wetlands",
        "field",
        "islands",
        "swamp",
        "peninsula",
        "darkForest",
        "tundra",
        "desert"
    };

    public static Map<String, Map<String, String>> biomeColorMap = Map.of(
            "wetlands", Map.of(
                    "landColor", "#0fc80f",
                    "voidColor", "#0080ff",
                    "treeColor", "#019501"
            ),
            "peninsula", Map.of(
                    "landColor", "#94ca53",
                    "voidColor", "#4382ff",
                    "treeColor", "#7eab47",
                    "coalColor", "#665555"
            ),
            "islands", Map.of(
                    "landColor", "#0fc854",
                    "voidColor", "#0080ff",
                    "treeColor", "#0a953e"
            ),
            "field", Map.of(
                    "landColor", "#00a800",
                    "voidColor", "#004aff",
                    "treeColor", "#006b00",
                    "coalColor", "#5d4646"
            ),
            "swamp", Map.of(
                    "landColor", "#58860b",
                    "voidColor", "#118fa1",
                    "treeColor", "#5e6b00",
                    "mudColor", "#6b4700"
            ),
            "darkForest", Map.of(
                    "landColor", "#306730",
                    "voidColor", "#001139",
                    "treeColor", "#042e00",
                    "pumpkinColor", "#d88700",
                    "fogColor", "#000000",
                    "coalColor", "#271b1b"
            ),
            "desert", Map.of(
                    "landColor", "#ffc436",
                    "sandColor", "#ffd264",
                    "voidColor", "#6fbbe9",
                    "treeColor", "#8cc043",
                    "cactusColor", "#47ab00",
                    "fogColor", "#ecba40"
            ),
            "tundra", Map.of(
                    "landColor", "#dffffb",
                    "voidColor", "#92c4ff",
                    "treeColor", "#009000",
                    "iceColor", "#cbeeff",
                    "coalColor", "#784d4d"
            )
    );

    public static Map<String, List<String>> biomeTags = Map.of(
            "wetlands", new ArrayList<>(),
            "peninsula", new ArrayList<>(),
            "islands", new ArrayList<>(),
            "field", new ArrayList<>(),
            "swamp", new ArrayList<>(),
            "darkForest", new ArrayList<>(Arrays.asList("fog")),
            "desert", new ArrayList<>(Arrays.asList("fog")),
            "tundra", new ArrayList<>(Arrays.asList("dangerousVoid"))
    );

    public static void biomeGeneration(World world) {
        switch (world.biome) {
            case "wetlands" -> {
                world.size = new int[]{51, 90};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                List<Entity> entityResult = new ArrayList<>();

                result[world.start - 1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start + 1][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = 0.1 + 0.55 * result[world.start - x + 1][y] + 0.3 * result[world.start - x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() <= 0.1) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() <= 0.02) {
                                    entityResult.add(new Entity("slime",
                                            new double[]{(world.start - x + 0.5) * HoneySuckle.tileSize,
                                                (y + 0.5) * HoneySuckle.tileSize
                                            }));
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.1 + 0.55
                                    * result[world.start + x - 1][y] + 0.3
                                    * result[world.start + x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.1) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.02) {
                                    entityResult.add(new Entity("slime",
                                            new double[]{(world.start + x + 0.5) * HoneySuckle.tileSize,
                                                (y + 0.5) * HoneySuckle.tileSize
                                            }));
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
                world.entities = entityResult;
            }
            case "peninsula" -> {
                world.size = new int[]{25, 75};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                List<Entity> entityResult = new ArrayList<>();

                result[world.start - 2][world.size[1] - 1] = 1;
                result[world.start - 1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start + 1][world.size[1] - 1] = 1;
                result[world.start + 2][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = 0.095 * result[world.start - x + 1][y] + 0.9025 * result[world.start - x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() <= 0.05) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() <= 0.01) {
                                    objResult[world.start - x][y] = 4;
                                } else if (Math.random() <= 0.05){
                                    entityResult.add(new Entity("slime",
                                            new double[]{(world.start - x + 0.5) * HoneySuckle.tileSize,
                                                (y + 0.5) * HoneySuckle.tileSize
                                            }));
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.095 * result[world.start + x - 1][y] + 0.9025 * result[world.start + x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.05) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.01) {
                                    objResult[world.start + x][y] = 4;
                                } else if (Math.random() <= 0.05){
                                    entityResult.add(new Entity("slime",
                                            new double[]{(world.start + x + 0.5) * HoneySuckle.tileSize,
                                                (y + 0.5) * HoneySuckle.tileSize
                                            }));
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
                world.entities = entityResult;
            }
            case "islands" -> {
                world.size = new int[]{75, 50};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                result[world.start][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = 0.1
                                    + 0.45 * result[world.start - x + 1][y]
                                    + 0.25 * result[world.start - x][y + 1];
                            if (Math.random()<= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() <= 0.25) {
                                    objResult[world.start - x][y] = 1;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.1
                                    + 0.45 * result[world.start + x - 1][y]
                                    + 0.25 * result[world.start + x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.25) {
                                    objResult[world.start + x][y] = 1;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            case "field" -> {
                world.size = new int[]{101, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                result[world.start - 2][world.size[1] - 1] = 1;
                result[world.start - 1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start + 1][world.size[1] - 1] = 1;
                result[world.start + 2][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = 0.1
                                    + 0.445 * result[world.start - x + 1][y]
                                    + 0.445 * result[world.start - x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() <= 0.01) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() <= 0.001) {
                                    objResult[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.1
                                    + 0.445 * result[world.start + x - 1][y]
                                    + 0.445 * result[world.start + x][y + 1];
                            if (x == 0) {
                                prob = 10
                                        + 90 * result[world.start + x][y + 1];
                            }
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.01) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.001) {
                                    objResult[world.start + x][y] = 4;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            case "swamp" -> {
                world.size = new int[]{75, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                result[world.start][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = 0.19 * result[world.start - x + 1][y]
                                    + 0.19 * result[world.start - x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 2;
                            } else {
                                prob = 0.1
                                        + conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1, 2)), 0.425)
                                        + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1, 2)), 0.435);
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.35) {
                                        objResult[world.start - x][y] = 1;
                                    }
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.2 * result[world.start + x - 1][y]
                                    + 0.2 * result[world.start + x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 2;
                            } else {
                                prob = 0.1
                                        + 0.45 * result[world.start + x - 1][y]
                                        + 0.25 * result[world.start + x][y + 1];
                                if (Math.random() <= prob) {
                                    result[world.start + x][y] = 1;
                                    if (Math.random() <= 0.35) {
                                        objResult[world.start + x][y] = 1;
                                    }
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            case "darkForest" -> {
                world.size = new int[]{101, 150};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                result[world.start - 2][world.size[1] - 1] = 1;
                result[world.start - 1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start + 1][world.size[1] - 1] = 1;
                result[world.start + 2][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = 0.1
                                    + 0.435 * result[world.start - x + 1][y]
                                    + 0.435 * result[world.start - x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() <= 0.1) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() <= 0.002) {
                                    objResult[world.start - x][y] = 2;
                                } else if (Math.random() <= 0.005) {
                                    objResult[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.1
                                    + 0.435 * result[world.start + x - 1][y]
                                    + 0.435 * result[world.start + x][y + 1];
                            if (x == 0) {
                                prob = 0.1
                                        + 0.9 * result[world.start + x][y + 1];
                            }
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.1) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.002) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.005) {
                                    objResult[world.start + x][y] = 4;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            case "desert" -> {
                world.size = new int[]{150, 125};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    if (world.start - x >= 0) {
                        result[world.start - x][world.size[1] - 1] = 1;
                    }
                    if (world.start + x < world.size[0]) {
                        result[world.start + x][world.size[1] - 1] = 1;
                    }
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            result[world.start - x][y] = 1;
                            double prob = 0.005
                                    + conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(0)), 0.45)
                                    + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(0)), 0.35);
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 0;
                            } else {
                                if (Math.random() <= 0.2) {
                                    result[world.start - x][y] = 3;
                                }
                                if (Math.random() <= 0.005) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() <= 0.02) {
                                    objResult[world.start - x][y] = 3;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            result[world.start + x][y] = 1;
                            double prob = 0.005
                                    + conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(0)), 0.45)
                                    + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(0)), 0.35);
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 0;
                            } else {
                                if (Math.random() <= 0.2) {
                                    result[world.start + x][y] = 3;
                                }
                                if (Math.random() <= 0.005) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.02) {
                                    objResult[world.start + x][y] = 3;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            case "tundra" -> {
                world.size = new int[]{75, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                int[][] objResult = new int[world.size[0]][world.size[1]];

                result[world.start - 1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start + 1][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1, 4)), 0.35)
                                    + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 0.35);
                            if (Math.random() <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() <= 0.05) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() <= 0.002) {
                                    objResult[world.start - x][y] = 4;
                                }
                            } else {
                                prob = 0.025
                                        + conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1, 4)), 0.4)
                                        + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 0.4);
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start - x < world.size[0]) {
                            double prob = conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(1, 4)), 0.35)
                                    + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 0.35);
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.05) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() <= 0.002) {
                                    objResult[world.start + x][y] = 4;
                                }
                            } else {
                                prob = 0.025
                                        + conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(1, 4)), 0.4)
                                        + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 0.4);
                                if (Math.random() <= prob) {
                                    result[world.start + x][y] = 4;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
        }
    }

    private static double conditionalProb(int pos, List<Integer> condition, double prob) {
        return condition.contains(pos) ? prob : 0;
    }
}
