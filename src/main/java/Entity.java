
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Satic data imported from json files
    public static final Map<String, Map<String, Number>> entityAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> entityTextures = new HashMap<>();
    public static final Map<String, List<Map<String, Number>>> entityLoot = new HashMap<>();
    public static final Map<String, List<String>> entityTags = new HashMap<>();
    public static final Map<Integer, String> entityStringId = new HashMap<>();
    public static final Map<String, Integer> entityIntId = new HashMap<>();

    //Basic attributes
    public final String type;
    public double health;
    public final double size;
    public final double weight;

    public final Brain brain;

    //All attributes
    public final Map<String, Number> attributes;
    public final List<String> tags;
    public final List<Map<String, Number>> loot;
    private final Map<String, String> texture;

    //Number of frames to render as red
    public int damageFrames = 0;
    private final String color;
    private final String staticTextureId;
    private final String animation;

    //Position variables
    public double[] pos = new double[2];
    public double[] vel = new double[2];

    //Update ticks
    public int ticks = 0;

    //Entity Constructor
    public Entity(String type, double[] pos, World world) {
        //Gets attributes and tags based on type
        this.type = type;
        attributes = entityAttributes.getOrDefault(type, new HashMap<>());
        tags = entityTags.getOrDefault(type, new ArrayList<>());
        loot = entityLoot.getOrDefault(type, new ArrayList<>());
        texture = entityTextures.getOrDefault(type, new HashMap<>());

        //Interprets basic attributes
        health = attributes.getOrDefault("health", 1).doubleValue();
        size = attributes.getOrDefault("size", 1).doubleValue() * TILE_SIZE;
        weight = attributes.getOrDefault("weight", 1).doubleValue();
        this.pos = pos;

        brain = new Brain(this, world);
        color = getColor(world);
        staticTextureId = texture.get("texture");
        animation = texture.get("anim");;
    }

    //Render Entity
    public void render(Graphics2D g, double[] camera) {
        //Position of entity on screen
        double[] screenPos = new double[]{
            GAME_WIDTH / 2.0 + pos[0] - camera[0] - size / 2.0,
            GAME_HEIGHT / 2.0 + pos[1] - camera[1] - size / 2.0
        };

        //If entity has texture, display
        if (staticTextureId != null) {
            String textureId = staticTextureId;
            //Add paremeters to stem file name, if applicable
            if (animation != null) {
                if (animation.contains("_x_")) {
                    if (brain.chaseAngle > 180) {
                        textureId = textureId + "_left";
                    } else {
                        textureId = textureId + "_right";
                    }
                }
                if (animation.contains("_xy_")) {
                    if (brain.chaseAngle >= 315 || brain.chaseAngle < 45) {
                        textureId = textureId + "_up";
                    } else if (brain.chaseAngle < 135) {
                        textureId = textureId + "_right";
                    } else if (brain.chaseAngle < 225) {
                        textureId = textureId + "_down";
                    } else {
                        textureId = textureId + "_left";
                    }
                }
                if (animation.contains("_shoot_")) {
                    if (brain.checkState("shooting")) {
                        textureId = textureId + "_shoot";
                    }
                }

            }
            //Find image
            BufferedImage textureImage = Rendering.texture(textureId, color);
            //Render red overlay when damaged
            if (damageFrames > 0) {
                textureImage = Rendering.applyOverlay(textureImage, "#ff0000");
            }
            damageFrames--;

            //Draw Texture
            g.drawImage(textureImage, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size, null);
        } else {//If entity has specified baseColor, set as color
            //Else render simple rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], (int) size, (int) size);
        }
    }

    private String getColor(World world) {
        //If entity has biome specific color, get color from biome
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = Biome.biomeColorMap.get(world.biome).get(natColorId);
            if (natColor != null) {
                return natColor;
            }
            //If entity has specified baseColor, set as color
        }
        String baseColor = texture.get("baseColor");
        if (baseColor != null) {
            return baseColor;
        }
        return "#ffffff";
    }

    //Update Entity
    public void update() {
        //Progress ticks
        ticks++;
        //Update entity through Brain
        brain.update();
    }
}
