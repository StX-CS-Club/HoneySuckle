
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class World {

    public static List<World> worlds = new ArrayList<>();
    public static int level = 0;

    public World() {
        this.grid = generateLevel();
        camera = new double[]{(start+0.5)*HoneySuckle.tileSize,(size[1]*HoneySuckle.tileSize)-HoneySuckle.size[1] / 2};
        worlds.add(this);
    }

    public double[] camera = new double[2];

    private int[][] grid;
    public int[] size = new int[2];
    private int start;
    private String biome;

    int[][] generateLevel() {
        size = new int[]{51, 100};
        start = (size[0] - 1) / 2;

        biome = "wetlands";

        int[][] result = new int[size[0]][size[1]];

        result[start - 1][size[1] - 1] = 1;
        result[start][size[1] - 1] = 1;
        result[start + 1][size[1] - 1] = 1;

        result[start - 1][0] = 1;
        result[start][0] = 1;
        result[start + 1][0] = 1;

        for (int x = 0; x < start; x++) {
            for (int y = size[1] - 2; y > 0; y--) {
                int leftProb = 10 + 55 * result[start - x + 1][y] + 30 * result[start - x][y + 1];
                int rightProb = 10 + 55 * result[start + x - 1][y] + 30 * result[start - x][y + 1];
                if (Math.random() * 100 <= leftProb) {
                    result[start - x][y] = 1;
                }
                if (Math.random() * 100 <= rightProb) {
                    result[start + x][y] = 1;
                }
            }
        }
        return result;
    }

    public void render(Graphics g) {
        g.setColor(Color.decode("#0080ff"));
        g.fillRect(0, 0, HoneySuckle.size[0], HoneySuckle.size[1]);

        g.setColor(Color.decode(Biome.biomeData.get(biome).get("landColor")));
        int tileSize = HoneySuckle.tileSize;

        int[] cameraTile = new int[]{(int) Math.floor(camera[0] / tileSize), (int) Math.floor(camera[1] / tileSize)};
        int[] cameraOffset = new int[]{(HoneySuckle.size[0] / 2 / tileSize + 1), (HoneySuckle.size[1] / 2 / tileSize + 2)};
        
        for (int y = cameraTile[1] - cameraOffset[1]; y < cameraTile[1] + cameraOffset[1]; y++) {
            for (int x = cameraTile[0] - cameraOffset[0]; x < cameraTile[0] + cameraOffset[0]; x++) {
                if (y > 0 && y < grid[0].length && x > 0 && x < grid.length) {
                    if (grid[x][y] == 1) {
                        g.fillRect((int) (x * tileSize - camera[0] + HoneySuckle.size[0]/2), (int)(y * tileSize - camera[1] + HoneySuckle.size[1]/2), tileSize, tileSize);
                    }
                }
            }
        }
    }

    public void printLevel() {
        for (int y = 0; y < grid[0].length; y++) {
            String response = "";
            for (int x = 0; x < grid.length; x++) {
                if (grid[x][y] == 1) {
                    response = response + " ";
                } else {
                    response = response + "■";
                }
            }
            System.out.println(response);
        }
    }
}
