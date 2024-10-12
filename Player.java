
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player {

    public static List<Player> players = new ArrayList<>();

    public Player(double[] pos, int size, List<String> tags) {
        this.pos = pos;
        this.size = size;
        this.tags = tags;
        crafting = new Craft(
            Map.of(
                "wood", 3
            )
        );
        players.add(this);
    }

    public double[] pos;
    public double[] vel = new double[2];

    private Craft crafting;

    public double health = 1;
    private double stamina = 1;

    public int size;

    public List<String> tags;

    public void render(Graphics2D g) {
        if (health > 0) {
            double[] camera = World.worlds.get(World.level).camera;

            if (tags.contains("leader")) {
                crafting.render(g, pos);
            }

            g.setColor(Color.GRAY);

            HoneySuckle.borderRect(g, 2, Color.black,
                    (int) (HoneySuckle.size[0] / 2 - size / 2 + pos[0] - camera[0]),
                    (int) (HoneySuckle.size[1] / 2 - size / 2 + pos[1] - camera[1]),
                    size, size);
        }
    }

    public void update(boolean[] keyDown, double[] mousePos, int click) {
        for (int i = 0; i < 2; i++) {
            vel[i] /= 2;
            if (Math.abs(vel[i]) <= 0.2) {
                vel[i] = 0;
            }
        }
        double incriment = (double) size / 5 * 30 / HoneySuckle.fps * HoneySuckle.tileSize / 40;

        double[] camera = World.worlds.get(World.level).camera;

        if (tags.contains("god")) {
            incriment *= 4;
        }

        if (keyDown[16]) {
            if (stamina == 1) {
                vel[0] = 0;
                vel[1] = 0;
                incriment = HoneySuckle.tileSize * 1.25;
                stamina = 0;
            }
        } else {
            stamina += 0.1 * 30 / HoneySuckle.fps;
            if (stamina > 1) {
                stamina = 1;
            }
        }

        if (tags.contains("wasd")) {
            if ((keyDown[83] || keyDown[87]) && (keyDown[65] || keyDown[68])) {
                incriment /= 2;
            }

            if (keyDown[83]) {
                vel[1] += incriment;
            }

            if (keyDown[87]) {
                vel[1] -= incriment;
            }

            if (keyDown[65]) {
                vel[0] -= incriment;
            }

            if (keyDown[68]) {
                vel[0] += incriment;
            }
        }
        if (tags.contains("arrows")) {
            if ((keyDown[38] || keyDown[40]) && (keyDown[37] || keyDown[39])) {
                incriment /= 2;
            }

            if (keyDown[40]) {
                vel[1] += incriment;
            }

            if (keyDown[38]) {
                vel[1] -= incriment;
            }

            if (keyDown[37]) {
                vel[0] -= incriment;
            }

            if (keyDown[39]) {
                vel[0] += incriment;
            }
        }

        if (tags.contains("god")) {
            pos[0] += vel[0];
            pos[1] += vel[1];
        } else {
            pos = World.worlds.get(World.level).bound(pos, vel, size / 2);
        }

        if (tags.contains("mouse")) {
            for (int i = 0; i < 2; i++) {
                double mouseDiff = mousePos[i]
                        - (HoneySuckle.size[i] / 2
                        + Math.floor(pos[i] / HoneySuckle.tileSize) * HoneySuckle.tileSize
                        - camera[i]);
                if (mouseDiff < 0) {
                    crafting.cursor[i] = -1;
                } else if (mouseDiff > HoneySuckle.tileSize) {
                    crafting.cursor[i] = 1;
                } else {
                    crafting.cursor[i] = 0;
                }
            }

            if(click == 1){
                crafting.destroy(World.worlds.get(World.level), this);
            }

            if(click == 3){
                crafting.build(World.worlds.get(World.level), this);
            }
        }

        if (tags.contains("leader")) {
            World.worlds.get(World.level).camera[0] = pos[0];
            World.worlds.get(World.level).camera[1] = pos[1];

            double[] newCamera = World.worlds.get(World.level).camera;
            int[] screenSize = HoneySuckle.size;
            int[] worldSize = World.worlds.get(World.level).size;

            if (newCamera[0] - screenSize[0] / 2 < 0) {
                World.worlds.get(World.level).camera[0] = screenSize[0] / 2;
            }
            if (newCamera[0] + screenSize[0] / 2 > worldSize[0] * HoneySuckle.tileSize) {
                World.worlds.get(World.level).camera[0] = worldSize[0] * HoneySuckle.tileSize - screenSize[0] / 2;
            }
            if (newCamera[1] - screenSize[1] / 2 < 0) {
                World.worlds.get(World.level).camera[1] = screenSize[1] / 2;
            }
            if (newCamera[1] + screenSize[1] / 2 > worldSize[1] * HoneySuckle.tileSize) {
                World.worlds.get(World.level).camera[1] = worldSize[1] * HoneySuckle.tileSize - screenSize[1] / 2;
            }
        }
        World.worlds.get(World.level).posEvent(this);
    }
}
