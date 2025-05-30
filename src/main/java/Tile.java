
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Tile.java *
 - Class for managing world tiles
 - Static json data
 */
public class Tile {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static json data
    public static final Map<Integer, List<String>> tileTags = new HashMap<>();
    public static final Map<Integer, Map<String, Double>> tileValues = new HashMap<>();
    public static final Map<Integer, Map<String, String>> tileTextures = new HashMap<>();

    //Basic Tile Properties
    public final int id;
    public final int[] posIndex;

    //Specific Tile Properties
    public final List<String> tags;
    public final Map<String, Double> values;
    private final Map<String, String> texture;

    private final int glowColor;

    //Tile Constructor
    public Tile(int id, int[] posIndex) {
        this.id = id;
        this.posIndex = posIndex;

        tags = tileTags.get(id);
        values = tileValues.get(id);
        texture = tileTextures.get(id);

        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }
    }

    public void render(Graphics2D g, World world, double[] screenPos) {
        //Default color of pure white
        String color = "#ffffff";
        //If tile has biome specific color, find color from biome
        if (texture.get("natColor") != null) {
            if (Biome.biomeColorMap.get(world.biome).get(texture.get("natColor")) != null) {
                color = Biome.biomeColorMap.get(world.biome).get(texture.get("natColor"));
            }
            //If tile has listed base color, set as color
        } else if (texture.get("baseColor") != null) {
            color = texture.get("baseColor");
        }
        //If tile has texture, load texture with grey-scaling
        if (texture.get("texture") != null) {
            String tileTexture = texture.get("texture");
            g.drawImage(Rendering.texture(tileTexture, color), (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE, null);
        } else {
            //Else, render basic rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE);
        }
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", (int) screenPos[0] + TILE_SIZE / 2,
                "posY", (int) screenPos[1] + TILE_SIZE / 2,
                "radius", TILE_SIZE * (int) readValue("light"),
                "color", glowColor
        ));
    }

    public double readValue(String value) {
        if (values.get(value) != null) {
            return values.get(value);
        }
        return 0;
    }
}
