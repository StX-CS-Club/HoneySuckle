package honey.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;

import honey.HoneySuckle;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Rendering;

public class Navigator {

    private static final int MAP_WIDTH = 32;
    private static final int MAP_PIXEL = 4;

    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    private final World world;
    private final int[] size;
    private final int mapHeight;
    private final int mapCount;

    private final int[] scrollPosX;
    private final int scrollPosY;

    private final int[] mapPos;

    public boolean isOpen = false;
    public boolean started = false;

    public final Set<Structure> icons = new LinkedHashSet<>();

    public Navigator(World world) {
        this.world = world;
        size = world.size;

        mapHeight = (int) Math.ceil((size[1] * MAP_PIXEL) * 16 / 14.0) + 1;

        mapCount = (int) Math.ceil(size[0] * MAP_PIXEL / (double) MAP_WIDTH);

        scrollPosY = GAME_HEIGHT / 2 - mapHeight / 2;
        scrollPosX = new int[]{
            GAME_WIDTH / 2 - mapCount * MAP_WIDTH / 2 - MAP_WIDTH,
            GAME_WIDTH / 2 + mapCount * MAP_WIDTH / 2,};

        mapPos = new int[]{
            GAME_WIDTH / 2 - size[0] * MAP_PIXEL / 2,
            GAME_HEIGHT / 2 - size[1] * MAP_PIXEL / 2,};
    }

    public void update(InputHandler input) {
        isOpen = input.keyDown(16) || input.clickDown(4) || input.clickDown(5);
    }

    public void renderUi(Graphics2D g) {
        g.setColor(new Color(64, 64, 64, 192));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        if (started) {
            g.drawImage(Rendering.texture("hud/scroll/map_end", null), scrollPosX[0], scrollPosY, MAP_WIDTH, mapHeight, null);
            for (int i = 0; i < mapCount; i++) {
                g.drawImage(Rendering.texture("hud/scroll/map_middle", null), scrollPosX[0] + i * MAP_WIDTH + MAP_WIDTH, scrollPosY, MAP_WIDTH, mapHeight, null);
            }
            g.drawImage(Rendering.texture("hud/scroll/map_end", null), scrollPosX[1], scrollPosY, MAP_WIDTH, mapHeight, null);
            BufferedImage mapImage = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < size[0]; x++) {
                for (int y = 0; y < size[1]; y++) {
                    WorldObject obj = world.objGrid[x][y];
                    if (obj != null) {
                        if (obj.rendered) {
                            mapImage.setRGB(x, y, obj.mapColor.getRGB());
                        }
                    } else {
                        Tile tile = world.grid[x][y];
                        if (tile.rendered) {
                            mapImage.setRGB(x, y, tile.mapColor.getRGB());
                        }
                    }
                }
            }

            g.drawImage(mapImage, mapPos[0], mapPos[1], MAP_PIXEL * size[0], MAP_PIXEL * size[1], null);

            for (Structure icon : icons) {
                if (icon.mapTexture != null) {
                    g.drawImage(icon.mapTexture, (int) (mapPos[0] + icon.pos[0] * MAP_PIXEL - 8), (int) (mapPos[1] + icon.pos[1] * MAP_PIXEL - 8), 16, 16, null);
                }
            }

            for (Player player : Player.players) {
                //Original rotation
                AffineTransform originalTransform = g.getTransform();

                final int[] playerMapPos = new int[]{
                    (int) (mapPos[0] + player.pos[0] / TILE_SIZE * MAP_PIXEL),
                    (int) (mapPos[1] + player.pos[1] / TILE_SIZE * MAP_PIXEL)
                };

                g.rotate(Math.toRadians(player.mapRotation), playerMapPos[0], playerMapPos[1]);

                g.drawImage(Rendering.texture("hud/map/player", null), playerMapPos[0] - 8, playerMapPos[1] - 8, 16, 16, null);

                g.setTransform(originalTransform);
            }
        } else {
            g.drawImage(Rendering.texture("hud/symbols/denied", "#999999"), GAME_WIDTH / 2 - 32, GAME_HEIGHT / 2 - 32, 64, 64, null);
        }
    }
}
