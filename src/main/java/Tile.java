
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Tile.java *
 - Class for managing world tiles
 - Static json data
 */
public class Tile {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static json data
    public static final Map<Integer, List<String>> tileTags = new HashMap<>();
    public static final Map<Integer, Map<String, Number>> tileAttributes = new HashMap<>();
    public static final Map<Integer, Map<String, String>> tileTextures = new HashMap<>();
    public static final Map<String, Integer> tileIntIds = new HashMap<>();
    public static final Map<Integer, String> tileStringIds = new HashMap<>();

    //Basic Tile Properties
    public final int id;
    public final int[] posIndex;

    //Specific Tile Properties
    public final List<String> tags;
    public final Map<String, Number> attributes;
    private final Map<String, String> texture;

    private final int glowColor;
    private final String color;
    private final BufferedImage staticTexture;

    //Tile Constructor
    public Tile(int id, int[] posIndex, World world) {
        this.id = id;
        this.posIndex = posIndex;

        tags = tileTags.get(id);
        attributes = tileAttributes.get(id);
        texture = tileTextures.get(id);

        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }
        color = getColor(world);
        staticTexture = getTexture();
    }

    public void render(Graphics2D g, World world, double[] screenPos) {
        //If tile has texture, load texture with grey-scaling
        if (staticTexture != null) {
            g.drawImage(staticTexture, (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE, null);
        } else {
            //Else, render basic rectangle
            g.setColor(Color.decode(color));
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE);
        }
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
        return texture.getOrDefault("baseColor", null);
    }

    private BufferedImage getTexture() {
        String textureString = texture.get("texture");
        if (textureString != null) {
            int textureCount = attributes.getOrDefault("textures", 1).intValue();
            if (textureCount > 1) {
                textureString = textureString + "_" + ThreadLocalRandom.current().nextInt(1, textureCount + 1);
            }

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
}
