
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.util.List;

public class Player {
    public Player(int size, List<String> tags) {
        this.size = size;
        this.tags = tags;
    }

    public void render(Graphics g){
        g.setColor(Color.BLACK);

        double[] camera = World.worlds.get(World.level).camera;
        g.fillRect((int)(HoneySuckle.size[0]/2-HoneySuckle.tileSize/3+pos[0]-camera[0]), (int)(HoneySuckle.size[1]/2-HoneySuckle.tileSize/3+pos[1]-camera[1]), size, size);
    }

    public void update(boolean[] keyDown){
        double[] deltaPos = new double[2];
        double incriment = (double) size/10 * 60/HoneySuckle.fps;

        if(keyDown[16]){
            incriment *= 2;
        }

        if(keyDown[40] || keyDown[83]){
            deltaPos[1] += incriment;
        }

        if(keyDown[38] || keyDown[87]){
            deltaPos[1] -= incriment;
        }

        if(keyDown[37] || keyDown[65]){
            deltaPos[0] -= incriment;
        }

        if(keyDown[39] || keyDown[68]){
            deltaPos[0] += incriment;
        }
        pos[0] += deltaPos[0];
        pos[1] += deltaPos[1];

        if(tags.contains("leader")){
            World.worlds.get(World.level).camera[0] = pos[0];
            World.worlds.get(World.level).camera[1] = pos[1];

            double[] newCamera = World.worlds.get(World.level).camera;
            int[] screenSize = HoneySuckle.size;
            int[] worldSize = World.worlds.get(World.level).size;

            if(newCamera[0]-screenSize[0]/2 < 0){
                World.worlds.get(World.level).camera[0] = screenSize[0]/2;
            }
            if(newCamera[0]+screenSize[0]/2 > worldSize[0]*HoneySuckle.tileSize){
                World.worlds.get(World.level).camera[0] = worldSize[0]*HoneySuckle.tileSize-screenSize[0]/2;
            }
            if(newCamera[1]-screenSize[1]/2 < 0){
                World.worlds.get(World.level).camera[1] = screenSize[1]/2;
            }
            if(newCamera[1]+screenSize[1]/2 > worldSize[1]*HoneySuckle.tileSize){
                World.worlds.get(World.level).camera[1] = worldSize[1]*HoneySuckle.tileSize-screenSize[1]/2;
            }
        }
    }

    public double[] pos = {HoneySuckle.tileSize*25.5,HoneySuckle.tileSize*99.5};
    public double health = 1;
    private int size;
    public List<String> tags;
}