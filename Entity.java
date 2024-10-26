
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

public class Entity {

    public static Map<String, Map<String, Double>> attributes = Map.of(
            "slime", Map.of(
                    "size", 0.33,
                    "health", 0.5,
                    "view", 7.5
            ));

    public String type;
    public double health;
    public double size;
    public double[] pos = new double[2];
    public double[] vel = new double[2];

    public int ticks = 0;

    public Entity(String type, double[] pos) {
        this.type = type;
        this.health = attributes.get(type).get("health");
        this.size = attributes.get(type).get("size") * HoneySuckle.tileSize;
        this.pos = pos;
    }

    public Entity(String string, double d, double e) {
        //TODO Auto-generated constructor stub
    }

    public void render(Graphics2D g, double[] camera) {
        double[] screenPos = new double[]{
            HoneySuckle.size[0] / 2 + pos[0] - camera[0] - size / 2,
            HoneySuckle.size[1] / 2 + pos[1] - camera[1] - size / 2
        };

        Map<String, String> textureMap = Brain.textures.get(type);
        String color = "#000000";

        if (textureMap.get("natColor") != null) {
            String biome = World.worlds.get(World.level).biome;
            if (Biome.biomeColorMap.get(biome).get(textureMap.get("natColor")) != null) {
                color = Biome.biomeColorMap.get(biome).get(textureMap.get("natColor"));
            }
        } else {
            if (textureMap.get("baseColor") != null) {
                color = textureMap.get("baseColor");
            }
        }
        if (textureMap.get("texture") != null) {
            String texture = textureMap.get("texture");
            g.drawImage(Rendering.texture(texture, color), (int) screenPos[0], (int) screenPos[1], (int) size, (int) size, null);
        } else {
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size);
        }
    }

    public void update() {
        ticks++;
        Brain.update(this);
    }
}
