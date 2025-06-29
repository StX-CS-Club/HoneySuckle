
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/*
 * WorldObject.java *
 - Class for handling world objects
 - Static json data
 */
public class WorldObject {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static json dats
    public static final Map<Integer, List<String>> objTags = new HashMap<>();
    public static final Map<Integer, Map<String, Number>> objAttributes = new HashMap<>();
    public static final Map<Integer, List<Map<String, Number>>> objLoot = new HashMap<>();
    public static final Map<Integer, Map<String, String>> objTextures = new HashMap<>();
    public static final Map<String, Integer> objIntIds = new HashMap<>();
    public static final Map<Integer, String> objStringIds = new HashMap<>();

    //Basic object attributes
    public int id;
    public int[] posIndex;

    //Specific object attributes
    public double durability;
    public List<String> tags;
    public Map<String, Number> attributes;
    public Map<String, String> texture;
    public List<Map<String, Number>> loot;

    private double frameDamage = 0;

    private final int glowColor;
    private final String color;
    private final BufferedImage staticTexture;

    //WorldObject Contructor
    public WorldObject(int id, int[] posIndex, World world) {
        this.id = id;
        this.posIndex = posIndex;
        //Interprets entity tags and attributes
        tags = objTags.get(id);
        attributes = objAttributes.get(id);
        texture = objTextures.get(id);
        loot = objLoot.get(id);

        if (tags.contains("destructable")) {
            durability = attributes.getOrDefault("durability", 1).doubleValue();
        }

        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }
        color = getColor(world);
        staticTexture = getTexture();
    }

    //Render WorldObject
    public void render(Graphics2D g, World world, double[] screenPos) {
        //If object has texture, render it
        int size = TILE_SIZE;
        double[] pos = screenPos.clone();
        if(frameDamage != 0 && ThreadLocalRandom.current().nextBoolean()){
            size *= .8;
            pos[0] += ((double) TILE_SIZE)/10;
            pos[1] += ((double) TILE_SIZE)/10;
        }

        if (staticTexture != null) {
            g.drawImage(staticTexture, (int) pos[0], (int) pos[1], size, size, null);
        } else {
            //Else render basic rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) pos[0], (int) pos[1], size, size);
        }
        frameDamage -= 0.1;
        frameDamage = Math.clamp(frameDamage, 0, 1);
    }

    private String getColor(World world) {
        //If tile has biome specific color, find color from biome
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = world.biome.colorMap.get(natColorId);
            if (natColor != null) {
                return natColor;
            }
            //If tile has listed base color, set as color
        } 
        String baseColor = texture.get("baseColor");
        if (baseColor != null) {
            return baseColor;
        }
        return "#ffffff";
    }

    private BufferedImage getTexture(){
        String textureString = texture.get("texture");
        if (textureString != null) {
            return Rendering.texture(textureString, color);
        }
        return null;
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", screenPos[0] + TILE_SIZE / 2,
                "posY", screenPos[1] + TILE_SIZE / 2,
                "radius", attributes.get("lightRadius"),
                "color", glowColor,
                "glow", attributes.getOrDefault("glow", 0),
                "glowRadius", attributes.get("glowRadius")
        ));
    }

    //Damages object; returns true if broken
    public boolean damage(double damage) {
        //If can't break, go fuck off
        if (!tags.contains("destructable")) {
            return false;
        }
        //Damages object
        durability -= damage;
        frameDamage = damage;
        //If still there, return false
        if (durability > 0) {
            return false;
        }
        //If broken, remove object and return true
        World.worlds.get(World.level).objGrid[posIndex[0]][posIndex[1]] = null;
        return true;
    }
}
