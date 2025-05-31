
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
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Satic data imported from json files
    public static final Map<String, Map<String, Double>> entityAttributes = new HashMap<>();
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
    public final Map<String, Double> attributes;
    public final List<String> tags;
    public final List<Map<String, Number>> loot;

    //Number of frames to render as red
    public int damageFrames = 0;

    //Position variables
    public double[] pos = new double[2];
    public double[] vel = new double[2];

    //Update ticks
    public int ticks = 0;

    //Entity Constructor
    public Entity(String type, double[] pos, World world) {
        //Gets attributes and tags based on type
        this.type = type;
        attributes = entityAttributes.get(type);
        tags = entityTags.get(type);
        loot = entityLoot.get(type);

        //Interprets basic attributes
        health = attributes.get("health");
        size = attributes.get("size") *TILE_SIZE;
        this.pos = pos;
        weight = attributes.get("weight");

        brain = new Brain(this, world);
    }

    //Render Entity
    public void render(Graphics2D g, double[] camera) {
        //Position of entity on screen
        double[] screenPos = new double[]{
           GAME_WIDTH / 2.0 + pos[0] - camera[0] - size / 2.0,
           GAME_HEIGHT / 2.0 + pos[1] - camera[1] - size / 2.0
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
            String animation = textureMap.get("anim");
            //Add paremeters to stem file name, if applicable
            if(animation != null){
                if(animation.contains("_x_")){
                    if (brain.chaseAngle > 180) {
                        texture = texture + "_left";
                    } else {
                        texture = texture + "_right";
                    }
                }
                if(animation.contains("_xy_")){
                    if(brain.chaseAngle >= 45 && brain.chaseAngle < 135){
                        texture = texture + "_right";
                    } else if(brain.chaseAngle < 225){
                        texture = texture + "_down";
                    } else if(brain.chaseAngle < 315){
                        texture = texture + "_left";
                    } else {
                        texture = texture + "_up";
                    }
                }
                if(animation.contains("_shoot_")){
                    if(brain.checkState("shooting")){
                        texture = texture + "_shoot";
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
        brain.update();
    }
}
