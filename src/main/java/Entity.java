
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {

    public static final Map<String, Map<String, Double>> entityAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> entityTextures = new HashMap<>();
    public static final Map<String, Map<String, Integer>> entityLoot = new HashMap<>();
    public static final Map<String, List<String>> entityTags = new HashMap<>();

    public final String type;
    public double health;
    public final double size;
    public final double weight;

    public final Map<String, Double> attributes;
    public final List<String> tags;

    private int damageFrames = 0;

    public double[] pos = new double[2];
    public double[] vel = new double[2];
    public int[] direction = new int[2];

    public int ticks = 0;

    public Entity(String type, double[] pos) {
        this.type = type;
        attributes = entityAttributes.get(type);
        tags = entityTags.get(type);

        health = attributes.get("health");
        size = attributes.get("size") * HoneySuckle.tileSize;
        this.pos = pos;
        weight = attributes.get("weight");
    }

    public void render(Graphics2D g, double[] camera) {
        double[] screenPos = new double[]{
            HoneySuckle.size[0] / 2 + pos[0] - camera[0] - size / 2,
            HoneySuckle.size[1] / 2 + pos[1] - camera[1] - size / 2
        };

        Map<String, String> textureMap = entityTextures.get(type);
        String color = "#ffffff";

        if (textureMap.get("natColor") != null) {
            String biome = World.worlds.get(World.level).biome;
            if (Biome.biomeColorMap.get(biome).get(textureMap.get("natColor")) != null) {
                color = Biome.biomeColorMap.get(biome).get(textureMap.get("natColor"));
            }
        }

        if (textureMap.get("texture") != null) {
            String texture = textureMap.get("texture");
            switch(textureMap.get("anim")){
                case "horizontal" -> {
                    if(direction[0] < 0){
                        texture = texture+"Left";
                    } else {
                        texture = texture+"Right";
                    }
                }
                case "fire" -> {
                    if(ticks < attributes.get("frames")){
                        texture = texture+"Fire";
                    }
                }
            }
            BufferedImage image = Rendering.texture(texture, color);
            if(damageFrames > 0){
                image = Rendering.applyOverlay(image, "#ff0000");
                damageFrames--;
            }

            g.drawImage(image, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size, null);
        } else {
            if (textureMap.get("baseColor") != null) {
                color = textureMap.get("baseColor");
            }
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size);
        }
    }

    public void update() {
        ticks++;
        Brain.update(this);
    }

    public boolean damage(double damage) {
        health -= damage;
        damageFrames += (int) (1.5*Math.floor(damage)+1);
        if (health > 0) {
            return false;
        }
        Brain.die(this, World.worlds.get(World.level));
        return true;
    }
}
