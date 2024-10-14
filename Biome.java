
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
                    "landColor", "#1c341c",
                    "voidColor", "#001139",
                    "treeColor", "#042e00",
                    "pumpkinColor", "#ab6b00",
                    "fogColor", "#000000",
                    "coalColor", "#271b1b"
            ),
            "desert", Map.of(
                    "landColor", "#ecba40",
                    "sandColor", "#efc356",
                    "voidColor", "#6fbbe9",
                    "treeColor", "#8cc043",
                    "cactusColor", "#47ab00",
                    "fogColor", "#ecba40"
            ),
            "tundra", Map.of(
                    "landColor", "#f0faff",
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
                            int prob = 10 + 55 * result[world.start - x + 1][y] + 30 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() * 100 <= 10) {
                                    objResult[world.start - x][y] = 1;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            int prob = 10 + 55
                                    * result[world.start + x - 1][y] + 30
                                    * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() * 100 <= 10) {
                                    objResult[world.start + x][y] = 1;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            case "peninsula" -> {
                world.size = new int[]{25, 75};
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
                            double prob = 9.5 * result[world.start - x + 1][y] + 90.25 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() * 100 <= 5) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() * 100 <= 1) {
                                    objResult[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 9.5 * result[world.start + x - 1][y] + 90.25 * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() * 100 <= 5) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() * 100 <= 1) {
                                    objResult[world.start - x][y] = 4;
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
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
                            double prob = 10
                                    + 45 * result[world.start - x + 1][y]
                                    + 25 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() * 100 <= 25) {
                                    objResult[world.start - x][y] = 1;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 10
                                    + 45 * result[world.start + x - 1][y]
                                    + 25 * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() * 100 <= 25) {
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
                            double prob = 10
                                    + 44.5 * result[world.start - x + 1][y]
                                    + 44.5 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() * 100 <= 1) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() * 100 <= 0.1) {
                                    objResult[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 10
                                    + 44.5 * result[world.start + x - 1][y]
                                    + 44.5 * result[world.start + x][y + 1];
                            if (x == 0) {
                                prob = 10
                                        + 90 * result[world.start + x][y + 1];
                            }
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() * 100 <= 1) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() * 100 <= 0.1) {
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
                            double prob = 19 * result[world.start - x + 1][y]
                                    + 19 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 2;
                            } else {
                                prob = 10
                                        + conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1, 2)), 42.5)
                                        + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1, 2)), 43.5);
                                if (Math.random() * 100 <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() * 100 <= 35) {
                                        objResult[world.start - x][y] = 1;
                                    }
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 20 * result[world.start + x - 1][y]
                                    + 20 * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 2;
                            } else {
                                prob = 10
                                        + 45 * result[world.start + x - 1][y]
                                        + 25 * result[world.start + x][y + 1];
                                if (Math.random() * 100 <= prob) {
                                    result[world.start + x][y] = 1;
                                    if (Math.random() * 100 <= 35) {
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
                            double prob = 10
                                    + 43.5 * result[world.start - x + 1][y]
                                    + 43.5 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 1;
                                if (Math.random() * 100 <= 10) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() * 100 <= 0.2) {
                                    objResult[world.start - x][y] = 2;
                                } else if (Math.random() * 100 <= 0.5) {
                                    objResult[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 10
                                    + 43.5 * result[world.start + x - 1][y]
                                    + 43.5 * result[world.start + x][y + 1];
                            if (x == 0) {
                                prob = 10
                                        + 90 * result[world.start + x][y + 1];
                            }
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() * 100 <= 10) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() * 100 <= 0.2) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() * 100 <= 0.5) {
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
                            double prob = 0.5
                                    + conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(0)), 45)
                                    + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(0)), 35);
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 0;
                            } else {
                                if (Math.random() * 100 <= 20) {
                                    result[world.start - x][y] = 3;
                                }
                                if (Math.random() * 100 <= 0.5) {
                                    objResult[world.start - x][y] = 1;
                                } else if (Math.random() * 100 <= 2) {
                                    objResult[world.start - x][y] = 3;
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            result[world.start + x][y] = 1;
                            double prob = 0.5
                                    + conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(0)), 45)
                                    + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(0)), 35);
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 0;
                            } else {
                                if (Math.random() * 100 <= 20) {
                                    result[world.start + x][y] = 3;
                                }
                                if (Math.random() * 100 <= 0.5) {
                                    objResult[world.start + x][y] = 1;
                                } else if (Math.random() * 100 <= 2) {
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

                result[world.start-1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start+1][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double prob = conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1,4)), 35) 
                                    + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1,4)), 35);
                            if (Math.random() * 100 <= prob) {
                                result[world.start - x][y] = 1;
                                    if (Math.random() * 100 <= 5) {
                                        objResult[world.start - x][y] = 1;
                                    } else if (Math.random() * 100 <= 0.2) {
                                        objResult[world.start - x][y] = 4;
                                    }
                            } else {
                                prob = 2.5
                                        + conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1, 4)), 40)
                                        + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 40);
                                if (Math.random() * 100 <= prob) {
                                    result[world.start - x][y] = 4;
                                }
                            }
                        }
                        if (world.start - x < world.size[0]) {
                            double prob = conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(1,4)), 35) 
                                    + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(1,4)), 35);
                            if (Math.random() * 100 <= prob) {
                                result[world.start + x][y] = 1;
                                    if (Math.random() * 100 <= 5) {
                                        objResult[world.start + x][y] = 1;
                                    } else if (Math.random() * 100 <= 0.2) {
                                        objResult[world.start + x][y] = 4;
                                    }
                            } else {
                                prob = 2.5
                                        + conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(1, 4)), 40)
                                        + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 40);
                                if (Math.random() * 100 <= prob) {
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
