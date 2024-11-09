
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

    //Static json dats
    public static final Map<Integer, List<String>> objTags = new HashMap<>();
    public static final Map<Integer, Map<String, Double>> objValues = new HashMap<>();
    public static final Map<Integer, Map<String, Integer>> objLoot = new HashMap<>();
    public static final Map<Integer, Map<String, String>> objTextures = new HashMap<>();

    //Basic object attributes
    public int id;
    public int[] posIndex;

    //Specific object attributes
    public double durability;
    public List<String> tags;
    public Map<String, Double> values;
    public Map<String, String> texture;
    public Map<String, Integer> loot;

    //WorldObject Contructor
    public WorldObject(int id, int[] posIndex) {
        this.id = id;
        this.posIndex = posIndex;
        //Interprets entity tags and attributes
        tags = objTags.get(id);
        if (tags.contains("destructable")) {
            durability = objValues.get(id).get("durability");
        }
        values = objValues.get(id);
        texture = objTextures.get(id);
        loot = objLoot.get(id);
    }

    //Render WorldObject
    public void render(Graphics2D g, World world, double[] screenPos, int size) {
        //Default color of pure white
        String color = "#ffffff";
        //If object has biome specific color, find color from biome
        if (texture.get("natColor") != null) {
            if (Biome.biomeColorMap.get(world.biome).get(texture.get("natColor")) != null) {
                color = Biome.biomeColorMap.get(world.biome).get(texture.get("natColor"));
            }
            //If object has listed baseColor, set as color
        } else if (texture.get("baseColor") != null) {
            color = texture.get("baseColor");
        }
        //If object has texture, render it
        if (texture.get("texture") != null) {
            String objTexture = texture.get("texture");
            g.drawImage(Rendering.texture(objTexture, color), (int) screenPos[0], (int) screenPos[1], size, size, null);
        } else {
            //Else render basic rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], size, size);
        }
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
