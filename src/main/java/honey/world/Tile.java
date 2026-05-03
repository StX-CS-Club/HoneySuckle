package honey.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import honey.HoneySuckle;
import honey.rendering.Rendering;

/*
 * Tile.java *
 - Class for managing world tiles
 - Static json data
 */
public class Tile {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;
    private static final int FPS = HoneySuckle.FPS;

    // Static json data
    public static final Map<Integer, List<String>> tileTags = new HashMap<>();
    public static final Map<Integer, Map<String, Number>> tileAttributes = new HashMap<>();
    public static final Map<Integer, Map<String, String>> tileTextures = new HashMap<>();
    public static final Map<String, Integer> tileIntIds = new HashMap<>();
    public static final Map<Integer, String> tileStringIds = new HashMap<>();

    // Basic Tile Properties
    public final int id;
    public final int[] posIndex;

    // Specific Tile Properties
    public final List<String> tags;
    public final Map<String, Number> attributes;
    private final String anim;
    private final Map<String, String> texture;

    private final int glowColor;
    private final String color;
    private final String edgeColor;
    public final Color mapColor;
    public boolean rendered = false;
    private final BufferedImage staticTexture;
    private BufferedImage staticEdgeTexture;

    private final String variant;
    private final int maxFrames;
    private final int dip;
    private int frame = 0;

    // Tile Constructor
    public Tile(int id, int[] posIndex, World world) {
        this.id = id;
        this.posIndex = posIndex;

        tags = tileTags.get(id);
        attributes = tileAttributes.get(id);
        texture = tileTextures.get(id);
        anim = texture.getOrDefault("anim", "");

        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }
        color = getColor(world, "");
        edgeColor = getColor(world, "Edge");
        mapColor = Rendering.decodeColor(getMapColor(world));

        maxFrames = attributes.getOrDefault("animFrames", FPS).intValue();
        dip = attributes.getOrDefault("dip", 0).intValue();

        variant = getVariant();
        staticTexture = getTexture(variant);
    }

    public void render(Graphics2D g, World world, double[] screenPos) {
        // If tile has texture, load texture with grey-scaling
        if (anim.contains("_gif_")) {
            g.drawImage(getFrame(variant), (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE, null);
            frame = (frame + 1) % maxFrames;
        } else {
            if (staticTexture != null) {
                g.drawImage(staticTexture, (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE, null);
            } else {
                // Else, render basic rectangle
                g.setColor(Rendering.decodeColor(color));
                Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], TILE_SIZE, TILE_SIZE);
            }
        }

        if (posIndex[1] != 0 && dip != 0) {
            world.grid[posIndex[0]][posIndex[1] - 1].renderEdge(g, world, screenPos, dip);
        }
    }

    public void renderEdge(Graphics2D g, World world, double[] screenPos, int dip) {
        dip -= this.dip;
        if(dip <= 0) return;

        if (staticEdgeTexture == null) {
            final String edgeTexture = texture.get("textureEdge");
            if (edgeTexture != null) {
                staticEdgeTexture = Rendering.texture(edgeTexture, edgeColor);
            }
        }

        if (staticEdgeTexture != null) {
            g.drawImage(staticEdgeTexture, (int) screenPos[0], (int) screenPos[1], (int) screenPos[0] + TILE_SIZE,
                    (int) (screenPos[1] + Math.round(TILE_SIZE / 16.0 * dip)), 0, 0, 16, dip, null);
        }

    }

    private String getColor(World world, String postfix) {
        // If tile has biome specific color, find color from biome
        final String natColorId = texture.get("natColor" + postfix);
        if (natColorId != null) {
            String natColor = world.biome.colorMap.get(natColorId);
            if (natColor != null) {
                return natColor;
            }
            // If tile has listed base color, set as color
        }
        return texture.getOrDefault("baseColor" + postfix, null);
    }

    private String getMapColor(World world) {
        final String mColor = texture.get("mapColor");
        if (mColor != null) {
            return mColor;
        }
        // If tile has biome specific color, find color from biome
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = world.biome.colorMap.get(natColorId);
            if (natColor != null) {
                return natColor;
            }
            // If tile has listed base color, set as color
        }
        return texture.getOrDefault("baseColor", "#ffffff");
    }

    private BufferedImage getTexture(String postfix) {
        String textureString = texture.get("texture");
        if (textureString != null) {
            return Rendering.texture(textureString + postfix, color);
        }
        return null;
    }

    private BufferedImage getFrame(String postfix) {
        String textureString = texture.get("gif");
        if (textureString != null) {
            return Rendering.renderGIF(textureString + postfix, color, frame / (double) maxFrames);
        }
        return null;
    }

    private String getVariant() {
        int textureCount = attributes.getOrDefault("variants", 1).intValue();
        if (textureCount > 1) {
            return "_" + ThreadLocalRandom.current().nextInt(1, textureCount + 1);
        }
        return "";
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", screenPos[0] + TILE_SIZE / 2,
                "posY", screenPos[1] + TILE_SIZE / 2,
                "radius", attributes.getOrDefault("lightRadius", 0),
                "color", glowColor,
                "glow", attributes.getOrDefault("glow", 0),
                "glowRadius", attributes.getOrDefault("glowRadius", 0)));
    }
}
