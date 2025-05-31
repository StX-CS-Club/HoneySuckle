
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * WorldObject.java *
 - Class for handling world objects
 - Static json data
 */
public class WorldObject {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static json dats
    public static final Map<Integer, List<String>> objTags = new HashMap<>();
    public static final Map<Integer, Map<String, Number>> objValues = new HashMap<>();
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
    public Map<String, Number> values;
    public Map<String, String> texture;
    public List<Map<String, Number>> loot;

    private final int glowColor;

    //WorldObject Contructor
    public WorldObject(int id, int[] posIndex) {
        this.id = id;
        this.posIndex = posIndex;
        //Interprets entity tags and attributes
        tags = objTags.get(id);
        if (tags.contains("destructable")) {
            durability = objValues.get(id).get("durability").doubleValue();
        }
        values = objValues.get(id);
        texture = objTextures.get(id);
        loot = objLoot.get(id);

        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }
    }

    //Render WorldObject
    public void render(Graphics2D g, World world, double[] screenPos) {
        //Default color of pure white
        String color = "#ffffff";
        //If object has biome specific color, find color from biome
        if (texture.get("natColor") != null) {
            if (Biome.biomeColorMap.get(world.biome).get(texture.get("natColor")) != null) {
                color = Biome.biomeColorMap.get(world.biome).get(texture.get("natColor"));
            }
        }
        //If object has texture, render it
        if (texture.get("texture") != null) {
            String objTexture = texture.get("texture");
            g.drawImage(Rendering.texture(objTexture, color), (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE, null);
        } else {//If object has listed baseColor, set as color
            if (texture.get("baseColor") != null && color.equals("#ffffff")) {
                color = texture.get("baseColor");
            }
            //Else render basic rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE);
        }
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", (int) screenPos[0] + TILE_SIZE / 2,
                "posY", (int) screenPos[1] + TILE_SIZE / 2,
                "radius", TILE_SIZE * (int) readValue("light"),
                "color", glowColor,
                "glow", (int) readValue("glow")
        ));
    }

    public Number readValue(String value) {
        if (values.get(value) != null) {
            return values.get(value);
        }
        return 0;
    }

    //Damages object; returns true if broken
    public boolean damage(double damage) {
        //If can't break, go fuck off
        if (!tags.contains("destructable")) {
            return false;
        }
        //Damages object
        durability -= damage;
        //If still there, return false
        if (durability > 0) {
            return false;
        }
        //If broken, remove object and return true
        World.worlds.get(World.level).objGrid[posIndex[0]][posIndex[1]] = null;
        return true;
    }
}
