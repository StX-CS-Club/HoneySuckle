
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.util.List;

public class Player {

    public Player(double[] pos, int size, List<String> tags) {
        this.pos = pos;
        this.size = size;
        this.tags = tags;
    }

    public double[] pos;
    public double[] vel = new double[2];
    public double health = 1;
    private double stamina = 1;
    public int size;
    public List<String> tags;

    public void render(Graphics g) {
        if (health > 0) {
            g.setColor(Color.GRAY);

            double[] camera = World.worlds.get(World.level).camera;
            g.fillRect((int) (HoneySuckle.size[0] / 2 - size / 2 + pos[0] - camera[0]), (int) (HoneySuckle.size[1] / 2 - size / 2 + pos[1] - camera[1]), size, size);
        }
    }

    public void update(boolean[] keyDown) {
        for (int i = 0; i < 2; i++) {
            vel[i] /= 2;
            if (Math.abs(vel[i]) <= 0.2) {
                vel[i] = 0;
            }
        }
        double incriment = (double) size / 10 * 30 / HoneySuckle.fps;

        if ((keyDown[83] || keyDown[87]) && (keyDown[65] || keyDown[68])) {
            incriment /= 2;
        }

        if(tags.contains("god")){
            incriment *= 2;
        }

        if (keyDown[16]) {
            stamina -= 0.3 * 30 / HoneySuckle.fps;
            if (stamina > 0) {
                incriment *= 2;
            }
        } else {
            if (stamina < 0) {
                stamina = 0;
            }
            stamina += 0.2 * 30 / HoneySuckle.fps;
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
        
        if (tags.contains("god")) {
            pos[0] += vel[0];
            pos[1] += vel[1];
        } else {
            pos = World.worlds.get(World.level).bound(pos, vel, size / 2);
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
