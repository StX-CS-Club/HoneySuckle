
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Biome.java *
 - Handles biome generation
 - Static attributes from json
 */

public class Biome {

    //Progression of biomes
    public static String[] biomes = new String[]{
        "wetlands",
        "field",
        "islands",
        "swamp",
        "peninsula",
        "darkForest",
        "tundra",
        "desert",
        "bridge"
    };

    //Static data from json
    public static final Map<String, Map<String, String>> biomeColorMap = new HashMap<>();

    public static final Map<String, List<String>> biomeTags = new HashMap<>();

    //Generates biome based on given type
    public static void biomeGeneration(World world) {
        switch (world.biome) {
            /* 
             * Generates land based on tile below and towards the center of the world
             * If land, chance to spawn tree or slime
             */
            case "wetlands" -> {
                world.size = new int[]{51, 90};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

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
                        if (x != 0) {
                            if (world.start - x >= 0) {
                                double prob = 0.1 + 0.55 * result[world.start - x + 1][y] + 0.3 * result[world.start - x][y + 1];
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.1) {
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.02) {
                                        entityResult.add(new Entity("slime",
                                                new double[]{(world.start - x + 0.5) * HoneySuckle.tileSize,
                                                    (y + 0.5) * HoneySuckle.tileSize
                                                }));
                                    }
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
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
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
            /*
             * Generates land based mostly on tile below, very small impact of tile towards center of the world
             * If land, chance to spawn coal, tree, or slime
             */
            case "peninsula" -> {
                world.size = new int[]{25, 75};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

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
                        if (x != 0) {
                            if (world.start - x >= 0) {
                                double prob = 0.095 * result[world.start - x + 1][y] + 0.90375 * result[world.start - x][y + 1];
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.05) {
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.01) {
                                        objResult[world.start - x][y] = new WorldObject(4, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.01*World.level) {
                                        entityResult.add(new Entity("slime",
                                                new double[]{(world.start - x + 0.5) * HoneySuckle.tileSize,
                                                    (y + 0.5) * HoneySuckle.tileSize
                                                }));
                                    }
                                }
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double prob = 0.095 * result[world.start + x - 1][y] + 0.90375 * result[world.start + x][y + 1];
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.05) {
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.01) {
                                    objResult[world.start + x][y] = new WorldObject(4, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.01*World.level) {
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
            /*
             * Generates land based on tile below and towards the center of the worlds, but far less common
             * If land, chance to spawn tree
             */
            case "islands" -> {
                world.size = new int[]{75, 50};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

                result[world.start][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (x != 0) {
                            if (world.start - x >= 0) {
                                double prob = 0.1
                                        + 0.45 * result[world.start - x + 1][y]
                                        + 0.25 * result[world.start - x][y + 1];
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.25) {
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    }
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
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            /*
             * Generates land based on tile below and towards center, but very common
             * If land, chance to spawn tree or coal
             */
            case "field" -> {
                world.size = new int[]{101, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

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
                        if (x != 0) {
                            if (world.start - x >= 0) {
                                double prob = 0.1
                                        + 0.445 * result[world.start - x + 1][y]
                                        + 0.445 * result[world.start - x][y + 1];
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.01) {
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.001) {
                                        objResult[world.start - x][y] = new WorldObject(4, new int[]{world.start - x, y});
                                    }
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
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.001) {
                                    objResult[world.start + x][y] = new WorldObject(4, new int[]{world.start + x, y});
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            /*
             * Generates mud based on tile below and towards center
             * If not mud, chance to generate land based on tile below and towards center
             * If land, chance to spawn tree
             */
            case "swamp" -> {
                world.size = new int[]{75, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

                result[world.start][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (x != 0) {
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
                                            objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                        }
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
                                        objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                    }
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            /*
             * Generates land based on tiel below and towards center
             * If land, chance to spawn tree, coal, or very rarely a pumpkin
             */
            case "darkForest" -> {
                world.size = new int[]{101, 150};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

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
                        if (x != 0) {
                            if (world.start - x >= 0) {
                                double prob = 0.1
                                        + 0.435 * result[world.start - x + 1][y]
                                        + 0.435 * result[world.start - x][y + 1];
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.1) {
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.002) {
                                        objResult[world.start - x][y] = new WorldObject(2, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.005) {
                                        objResult[world.start - x][y] = new WorldObject(4, new int[]{world.start - x, y});
                                    }
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
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.002) {
                                    objResult[world.start + x][y] = new WorldObject(2, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.005) {
                                    objResult[world.start + x][y] = new WorldObject(4, new int[]{world.start + x, y});
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            /*
             * Generates land based on tile below or towards center
             * If land, chance to become sand
             * If land, chance to spawn tree or cactus
             */
            case "desert" -> {
                world.size = new int[]{150, 125};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

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
                        if (x != 0) {
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
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.02) {
                                        objResult[world.start - x][y] = new WorldObject(3, new int[]{world.start - x, y});
                                    }
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
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.02) {
                                    objResult[world.start + x][y] = new WorldObject(3, new int[]{world.start + x, y});
                                }
                            }
                        }
                    }
                }
                world.grid = result;
                world.objGrid = objResult;
            }
            /*
             * Generates land based on tile below and towards center
             * If not land, chance to spawn ice based on tile below and towards center
             * If land, chance to spawn tree or coal
             */
            case "tundra" -> {
                world.size = new int[]{75, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

                result[world.start - 1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start + 1][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (x != 0) {
                            if (world.start - x >= 0) {
                                double prob = conditionalProb(result[world.start - x + 1][y], new ArrayList<>(Arrays.asList(1, 4)), 0.35)
                                        + conditionalProb(result[world.start - x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 0.35);
                                if (Math.random() <= prob) {
                                    result[world.start - x][y] = 1;
                                    if (Math.random() <= 0.05) {
                                        objResult[world.start - x][y] = new WorldObject(1, new int[]{world.start - x, y});
                                    } else if (Math.random() <= 0.002) {
                                        objResult[world.start - x][y] = new WorldObject(4, new int[]{world.start - x, y});
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
                        }
                        if (world.start - x < world.size[0]) {
                            double prob = conditionalProb(result[world.start + x - 1][y], new ArrayList<>(Arrays.asList(1, 4)), 0.35)
                                    + conditionalProb(result[world.start + x][y + 1], new ArrayList<>(Arrays.asList(1, 4)), 0.35);
                            if (Math.random() <= prob) {
                                result[world.start + x][y] = 1;
                                if (Math.random() <= 0.05) {
                                    objResult[world.start + x][y] = new WorldObject(1, new int[]{world.start + x, y});
                                } else if (Math.random() <= 0.002) {
                                    objResult[world.start + x][y] = new WorldObject(4, new int[]{world.start + x, y});
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
            /*
             * Generates straight line of bricks, with circle of land in the center
             * Spawns dragon in center, with entityGates blocking progression
             */
            case "bridge" -> {
                world.size = new int[]{51, 150};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];
                WorldObject[][] objResult = new WorldObject[world.size[0]][world.size[1]];

                for(int y = world.size[1]-1; y > -1; y--){
                    for(int x = 23; x < 28; x++){
                        result[x][y] = 5;
                    }
                }
                for (int y = 82; y > 68; y--) {
                    int xSize = 86-y;
                    if(y < 73){
                        xSize = y-65;
                    } else if(y < 78){
                        xSize = 8;
                    }
                    for(int x = 0; x < xSize; x++){
                        if(x == xSize-1){
                            result[world.start - x][y] = 5;
                            result[world.start + x][y] = 5;
                        } else {
                        result[world.start - x][y] = 1;
                        result[world.start + x][y] = 1;
                        }
                    }
                }
                for(int x = 23; x < 28; x++){
                    objResult[x][67] = new WorldObject(5, new int[]{x, 67});
                }
                world.entities.add(new Entity(
                    "dragon", new double[]{
                        25.5*HoneySuckle.tileSize, 76*HoneySuckle.tileSize
                    }
                ));
                
                world.grid = result;
                world.objGrid = objResult;
            }
        }
    }

    //Returns given probability if tile id is in provides list, else 0
    private static double conditionalProb(int pos, List<Integer> condition, double prob) {
        return condition.contains(pos) ? prob : 0;
    }
}
