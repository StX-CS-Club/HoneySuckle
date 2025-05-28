
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* 
 * Entity.java *
 - Class for managing individual entities
 - Static json data
 - References to Brain.java for specific methods
 */
public class Entity {

    //Satic data imported from json files
    public static final Map<String, Map<String, Double>> entityAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> entityTextures = new HashMap<>();
    public static final Map<String, List<Map<String, Integer>>> entityLoot = new HashMap<>();
    public static final Map<String, List<String>> entityTags = new HashMap<>();

    //Basic attributes
    public final String type;
    public double health;
    public final double size;
    public final double weight;

    //All attributes
    public final Map<String, Double> attributes;
    public final List<String> tags;
    public final List<Map<String, Integer>> loot;

    //Number of frames to render as red
    private int damageFrames = 0;

    //Position variables
    public double[] pos = new double[2];
    public double[] vel = new double[2];
    public int[] direction = new int[2];

    //Update ticks
    public int ticks = 0;

    //Entity Constructor
    public Entity(String type, double[] pos) {
        //Gets attributes and tags based on type
        this.type = type;
        attributes = entityAttributes.get(type);
        tags = entityTags.get(type);
        loot = entityLoot.get(type);

        //Interprets basic attributes
        health = attributes.get("health");
        size = attributes.get("size") * HoneySuckle.tileSize;
        this.pos = pos;
        weight = attributes.get("weight");
    }

    //Render Entity
    public void render(Graphics2D g, double[] camera) {
        //Position of entity on screen
        double[] screenPos = new double[]{
            HoneySuckle.size[0] / 2.0 + pos[0] - camera[0] - size / 2.0,
            HoneySuckle.size[1] / 2.0 + pos[1] - camera[1] - size / 2.0
        };

        //Texture data for entity
        Map<String, String> textureMap = entityTextures.get(type);
        //Default color of pure white
        String color = "#ffffff";

        //If entity has biome specific color, get color from biome
        if (textureMap.get("natColor") != null) {
            String biome = World.worlds.get(World.level).biome;
            if (Biome.biomeColorMap.get(biome).get(textureMap.get("natColor")) != null) {
                color = Biome.biomeColorMap.get(biome).get(textureMap.get("natColor"));
            }
            //If entity has specified baseColor, set as color
        }

        //If entity has texture, display
        if (textureMap.get("texture") != null) {
            String texture = textureMap.get("texture");
            //Add paremeters to stem file name, if applicable
            switch (textureMap.get("anim")) {
                case "horizontal" -> {
                    if (direction[0] < 0) {
                        texture = texture + "Left";
                    } else {
                        texture = texture + "Right";
                    }
                }
                case "fire" -> {
                    if (ticks < attributes.get("frames")) {
                        texture = texture + "Fire";
                    }
                }
            }
            //Find image
            BufferedImage image = Rendering.texture(texture, color);
            //Render red overlay when damaged
            if (damageFrames > 0) {
                image = Rendering.applyOverlay(image, "#ff0000");
            }
            damageFrames--;

            //Draw Texture
            g.drawImage(image, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size, null);
        } else {//If entity has specified baseColor, set as color
            if (textureMap.get("baseColor") != null && color.equals("#ffffff")) {
                color = textureMap.get("baseColor");
            }
            //Else render simple rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size);
        }
    }

    //Update Entity
    public void update() {
        //Progress ticks
        ticks++;
        //Update entity through Brain
        Brain.update(this);
    }

    //Damage Entity; returns true if dead
    public boolean damage(double damage) {
        //Subtract damage 
        health -= damage;
        //Add damageFrames
        if (damageFrames < -5) {
            damageFrames = (int) (1.5 * Math.floor(damage) + 1);
        }
        //If not dead, return false
        if (health > 0) {
            return false;
        }
        //If dead, die, and return true
        Brain.die(this, World.worlds.get(World.level));
        return true;
    }

    public Number readLoot(int index, String value, Number defaultValue) {
        Number result = loot.get(index).get(value);
        if(result == null){
            return defaultValue;
        }
        return result;
    }
}
