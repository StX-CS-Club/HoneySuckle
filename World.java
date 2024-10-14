
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class World {

    public static List<World> worlds = new ArrayList<>();
    public static int level = 0;
    private final int tileSize = HoneySuckle.tileSize;

    public World() {
        biome = Biome.biomes[(int) Math.floor(Math.random() * Biome.biomes.length)];
        if (level < Biome.biomes.length) {
            biome = Biome.biomes[(int) Math.floor(Math.random() * level)];
        }
        Biome.biomeGeneration(this);
        camera = new double[]{(start + 0.5) * tileSize, (size[1] * tileSize) - HoneySuckle.size[1] / 2};
        worlds.add(this);
    }

    public double[] camera = new double[2];

    public int[][] grid;
    public int[][] objGrid;
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
            if (newPos[i] > size[i] * tileSize - margin) {
                newPos[i] = size[i] * tileSize - margin;
            }
        }
        int[] posIndex = new int[]{(int) (Math.floor(pos[0] / tileSize)), (int) (Math.floor(pos[1] / tileSize))};
        int[] newPosIndex = new int[]{(int) (Math.floor(newPos[0] / tileSize)), (int) (Math.floor(newPos[1] / tileSize))};
        if (checkTile(posIndex[0], posIndex[1], "walkable") && !checkTile(posIndex[0], posIndex[1], "slippery")) {
            if (newPosIndex[0] >= 0 && newPosIndex[0] < size[0]) {
                if (!checkTile(newPosIndex[0], posIndex[1], "walkable")) {
                    newPos[0] = pos[0];
                }
            }
            if (newPosIndex[1] >= 0 && newPosIndex[1] < size[1]) {
                if (!checkTile(posIndex[0], newPosIndex[1], "walkable")) {
                    newPos[1] = pos[1];
                }
            }
        }
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((newPos[0] - margin) / tileSize)), (int) (Math.floor((newPos[0] + margin) / tileSize))},
            {(int) (Math.floor((newPos[1] - margin) / tileSize)), (int) (Math.floor((newPos[1] + margin) / tileSize))},};
        for (int i = 0; i < 2; i++) {
            if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0] && delta[0] != 0) {
                if (checkTile(marginIndex[0][i], posIndex[1], "obstruction")) {
                    newPos[0] = (marginIndex[0][i] + 0.5) * tileSize + (0.5 * tileSize + margin) * Math.pow(-1, i);
                }
            }
            if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1] && delta[1] != 0) {
                if (checkTile(posIndex[0], marginIndex[1][i], "obstruction")) {
                    newPos[1] = (marginIndex[1][i] + 0.5) * tileSize + (0.5 * tileSize + margin) * Math.pow(-1, i);
                }
            }
        }
        return newPos;
    }

    public void posEvent(Player player) {
        if (player.tags.contains("leader")) {
            if (player.pos[1] <= player.size / 2) {
                level++;
                World world = new World();
                player.pos = new double[]{tileSize * (world.start + 0.5), tileSize * (world.size[1] - 0.5)};
                return;
            }
        }
        int[] posIndex = new int[]{(int) Math.floor(player.pos[0] / tileSize), (int) Math.floor(player.pos[1] / tileSize)};

        double margin = player.size / 2 + 1;
        int[][] marginIndex = new int[][]{
            {(int) (Math.floor((player.pos[0] - margin) / tileSize)), (int) (Math.floor((player.pos[0] + margin) / tileSize))},
            {(int) (Math.floor((player.pos[1] - margin) / tileSize)), (int) (Math.floor((player.pos[1] + margin) / tileSize))}
        };

        if (!player.tags.contains("god")) {
            if (checkTile(posIndex[0], posIndex[1], "damage") && !checkTile(posIndex[0], posIndex[1], "safe")) {
                player.health -= 0.01 * 30 / HoneySuckle.fps;
                if (Biome.biomeTags.get(biome).contains("dangerousVoid")) {
                    player.health -= 0.01 * 30 / HoneySuckle.fps;
                }
            }
            if (checkTile(posIndex[0], posIndex[1], "slow") && !checkTile(posIndex[0], posIndex[1], "safe")) {
                player.vel[0] /= 3;
                player.vel[1] /= 3;
            }
            if (checkTile(posIndex[0], posIndex[1], "slippery") && !checkTile(posIndex[0], posIndex[1], "safe")) {
                player.vel[0] *= 1.625;
                player.vel[1] *= 1.625;
            }
            for (int i = 0; i < 2; i++) {
                if (marginIndex[0][i] >= 0 && marginIndex[0][i] < size[0]) {
                    if (checkTile(marginIndex[0][i], posIndex[1], "hurts")) {
                        player.health -= 0.02 * 30 / HoneySuckle.fps;
                    }
                }
                if (marginIndex[1][i] >= 0 && marginIndex[1][i] < size[1]) {
                    if (checkTile(posIndex[0], marginIndex[1][i], "hurts")) {
                        player.health -= 0.02 * 30 / HoneySuckle.fps;
                    }
                }
            }
        }
    }

    private boolean checkTile(int x, int y, String tag) {
        return Tile.tileProperties.get(grid[x][y]).contains(tag) || Tile.objProperties.get(objGrid[x][y]).contains(tag);
    }

    public void render(Graphics2D g) {
        g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get("voidColor")));
        g.fillRect(0, 0, HoneySuckle.size[0], HoneySuckle.size[1]);

        int[] cameraTile = new int[]{(int) Math.floor(camera[0] / tileSize), (int) Math.floor(camera[1] / tileSize)};
        int[] cameraOffset = new int[]{(HoneySuckle.size[0] / 2 / tileSize + 1), (HoneySuckle.size[1] / 2 / tileSize + 2)};

        for (int y = cameraTile[1] - cameraOffset[1]; y < cameraTile[1] + cameraOffset[1]; y++) {
            for (int x = cameraTile[0] - cameraOffset[0]; x < cameraTile[0] + cameraOffset[0]; x++) {
                if (y >= 0 && y < grid[0].length && x >= 0 && x < grid.length) {
                    if (grid[x][y] > 0) {
                        g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get(Tile.natTileColor.get(grid[x][y]))));
                        HoneySuckle.borderRect(g, 2, Color.black, (int) (x * tileSize - camera[0] + HoneySuckle.size[0] / 2), (int) (y * tileSize - camera[1] + HoneySuckle.size[1] / 2), tileSize, tileSize);
                    }
                    if (objGrid[x][y] != 0) {
                        if (objGrid[x][y] > 0) {
                            g.setColor(Color.decode(Biome.biomeColorMap.get(biome).get(Tile.natObjColor.get(objGrid[x][y]))));
                        }
                        if (objGrid[x][y] < 0) {
                            g.setColor(Color.decode(Tile.objColor.get(objGrid[x][y])));
                        }
                        HoneySuckle.borderRect(g, 2, Color.black, (int) (x * tileSize - camera[0] + HoneySuckle.size[0] / 2), (int) (y * tileSize - camera[1] + HoneySuckle.size[1] / 2), tileSize, tileSize);
                    }
                }
            }
        }
    }

    public void printLevel() {
        for (int y = 0; y < grid[0].length; y++) {
            String response = "";
            for (int[] grid1 : grid) {
                if (grid1[y] == 1) {
                    response = response + " ";
                } else {
                    response = response + "■";
                }
            }
            System.out.println(response);
        }
    }
}
