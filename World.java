
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class World {

    public static List<World> worlds = new ArrayList<>();
    public static int level = 0;

    public World() {
        biome = Biome.biomes[(int) Math.floor(Math.random() * Biome.biomes.length)];
        if(World.worlds.isEmpty()){
            biome = "wetlands";
        }
        this.grid = Biome.biomeGeneration(this);
        camera = new double[]{(start + 0.5) * HoneySuckle.tileSize, (size[1] * HoneySuckle.tileSize) - HoneySuckle.size[1] / 2};
        worlds.add(this);
    }

    public double[] camera = new double[2];

    private int[][] grid;
    public int[] size = new int[2];
    public int start;
    public String biome;

    public double[] bound(double[] pos, double[] delta, double margin) {
        double[] newPos = new double[]{pos[0] + delta[0], pos[1] + delta[1]};
        if (margin <= 0) {
            margin = 0.01;
        }
        for (int i = 0; i < 2; i++) {
            if (newPos[i] < margin) {
                newPos[i] = margin;
            }
            if (newPos[i] > size[i] * HoneySuckle.tileSize - margin) {
                newPos[i] = size[i] * HoneySuckle.tileSize - margin;
            }
        }
        int[] posIndex = new int[]{(int) (Math.floor(pos[0] / HoneySuckle.tileSize)), (int) (Math.floor(pos[1] / HoneySuckle.tileSize))};
        int[] newPosIndex = new int[]{(int) (Math.floor(newPos[0] / HoneySuckle.tileSize)), (int) (Math.floor(newPos[1] / HoneySuckle.tileSize))};
        if (grid[posIndex[0]][posIndex[1]] == 1) {
            if (newPosIndex[0] > 0 && newPosIndex[0] < size[0]) {
                if (grid[newPosIndex[0]][posIndex[1]] != 1) {
                    newPos[0] = pos[0];
                }
            }
            if (newPosIndex[1] > 0 && newPosIndex[1] < size[1]) {
                if (grid[posIndex[0]][newPosIndex[1]] != 1) {
                    newPos[1] = pos[1];
                }
            }
        }
        return newPos;
    }

    public void posEvent(Player player){
        int[] posIndex = new int[]{(int) Math.floor(player.pos[0]/HoneySuckle.tileSize), (int) Math.floor(player.pos[1]/HoneySuckle.tileSize)};
        if(grid[posIndex[0]][posIndex[1]] == 0 && !player.tags.contains("god")){
            player.health -= 0.01*30/HoneySuckle.fps;
        }
        if(player.tags.contains("leader")){
            if(player.pos[1] <= player.size/2){
                World world = new World();
                player.pos = new double[]{HoneySuckle.tileSize * (world.start + 0.5), HoneySuckle.tileSize * (world.size[1]-0.5)};
                level++;
            }
        }
    }

    public void render(Graphics g) {
        g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get("voidColor")));
        g.fillRect(0, 0, HoneySuckle.size[0], HoneySuckle.size[1]);

        g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get("landColor")));
        int tileSize = HoneySuckle.tileSize;

        int[] cameraTile = new int[]{(int) Math.floor(camera[0] / tileSize), (int) Math.floor(camera[1] / tileSize)};
        int[] cameraOffset = new int[]{(HoneySuckle.size[0] / 2 / tileSize + 1), (HoneySuckle.size[1] / 2 / tileSize + 2)};

        for (int y = cameraTile[1] - cameraOffset[1]; y < cameraTile[1] + cameraOffset[1]; y++) {
            for (int x = cameraTile[0] - cameraOffset[0]; x < cameraTile[0] + cameraOffset[0]; x++) {
                if (y >= 0 && y < grid[0].length && x >= 0 && x < grid.length) {
                    if (grid[x][y] == 1) {
                        g.fillRect((int) (x * tileSize - camera[0] + HoneySuckle.size[0] / 2), (int) (y * tileSize - camera[1] + HoneySuckle.size[1] / 2), tileSize, tileSize);
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
