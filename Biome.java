
import java.util.Map;

public class Biome {

    public static String[] biomes = new String[]{
        "wetlands",
        "peninsula",
        "islands",
        "field"
    };

    public static Map<String, Map<String, String>> biomeColorMap = Map.of(
            "wetlands", Map.of(
                    "landColor", "#0fc80f",
                    "voidColor", "#0080ff"
            ),
            "peninsula", Map.of(
                    "landColor", "#94ca53",
                    "voidColor", "#4382ff"
            ),
            "islands", Map.of(
                    "landColor", "#0fc854",
                    "voidColor", "#0080ff"
            ),
            "field", Map.of(
                    "landColor", "#00a800",
                    "voidColor", "#004aff"
            )
    );

    public static int[][] biomeGeneration(World world) {
        switch (world.biome) {
            case "wetlands" -> {
                world.size = new int[]{51, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

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
                            int leftProb = 10 + 55 * result[world.start - x + 1][y] + 30 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= leftProb) {
                                result[world.start - x][y] = 1;
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            int rightProb = 10 + 55
                                    * result[world.start + x - 1][y] + 30
                                    * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= rightProb) {
                                result[world.start + x][y] = 1;
                            }
                        }
                    }
                }
                return result;
            }

            case "peninsula" -> {
                world.size = new int[]{25, 75};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

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
                            double leftProb = 9.5 * result[world.start - x + 1][y] + 90 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= leftProb) {
                                result[world.start - x][y] = 1;
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double rightProb = 9.5 * result[world.start + x - 1][y] + 90 * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= rightProb) {
                                result[world.start + x][y] = 1;
                            }
                        }
                    }
                }
                return result;
            }
            case "islands" -> {
                world.size = new int[]{75, 50};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                result[world.start][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double leftProb = 10
                                    + 45 * result[world.start - x + 1][y]
                                    + 25 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= leftProb) {
                                result[world.start - x][y] = 1;
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double rightProb = 10
                                    + 45 * result[world.start + x - 1][y]
                                    + 25 * result[world.start + x][y + 1];
                            if (Math.random() * 100 <= rightProb) {
                                result[world.start + x][y] = 1;
                            }
                        }
                    }
                }
                return result;
            }
            case "field" -> {
                world.size = new int[]{101, 100};
                world.start = (world.size[0] - 1) / 2;

                int[][] result = new int[world.size[0]][world.size[1]];

                result[world.start-2][world.size[1] - 1] = 1;
                result[world.start-1][world.size[1] - 1] = 1;
                result[world.start][world.size[1] - 1] = 1;
                result[world.start+1][world.size[1] - 1] = 1;
                result[world.start+2][world.size[1] - 1] = 1;

                int xmargin = world.start;
                if (world.size[0] - world.start > world.start) {
                    xmargin = world.size[0] - world.start;
                }
                for (int x = 0; x < xmargin; x++) {
                    for (int y = world.size[1] - 2; y > -1; y--) {
                        if (world.start - x >= 0) {
                            double leftProb = 10
                                    + 44.5 * result[world.start - x + 1][y]
                                    + 44.5 * result[world.start - x][y + 1];
                            if (Math.random() * 100 <= leftProb) {
                                result[world.start - x][y] = 1;
                            }
                        }
                        if (world.start + x < world.size[0]) {
                            double rightProb = 10
                                    + 44.5 * result[world.start + x - 1][y]
                                    + 44.5 * result[world.start + x][y + 1];
                                    if(x == 0){
                                        rightProb = 10
                                    + 90 * result[world.start + x][y + 1];
                                    }
                            if (Math.random() * 100 <= rightProb) {
                                result[world.start + x][y] = 1;
                            }
                        }
                    }
                }
                return result;
            }
            default -> {
                return null;
            }
        }
    }
}
