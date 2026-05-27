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
    private static final double TILE_UNIT = TILE_SIZE / 16.0;

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
    private final Color colorDecoded;
    private final Color edgeColorDecoded;
    public final Color mapColor;
    public boolean rendered = false;
    private final BufferedImage staticTexture;
    private final BufferedImage staticEdgeTexture;

    private final String variant;
    private final int maxFrames;
    private final int dip;
    public final int dipPixels;
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
        color = getColor(world);
        edgeColor = getEdgeColor(world);
        colorDecoded = Rendering.decodeColor(color);
        edgeColorDecoded = Rendering.decodeColor(edgeColor);
        mapColor = Rendering.decodeColor(getMapColor(world));

        maxFrames = attributes.getOrDefault("animFrames", FPS).intValue();
        dip = attributes.getOrDefault("dip", 0).intValue();
        dipPixels = (int) Math.round(TILE_UNIT * dip);

        variant = getVariant();
        staticTexture = getTexture(variant);
        staticEdgeTexture = getEdgeTexture();
    }

    public void render(Graphics2D g, World world, double[] screenPos) {
        final int screenX = (int) screenPos[0];
        final int screenY = (int) screenPos[1] + dipPixels;

        if (posIndex[1] == 0 && dip != 0) {
            renderAt(g, screenX, screenY - TILE_SIZE);
        }

        if (anim.contains("_gif_")) {
            g.drawImage(getFrame(variant), screenX, screenY, TILE_SIZE, TILE_SIZE, null);
            frame = (frame + 1) % maxFrames;
        } else {
            renderAt(g, screenX, screenY);
        }

        if (posIndex[1] != 0 && dip != 0) {
            world.grid[posIndex[0]][posIndex[1] - 1].renderEdge(g, world, new double[]{screenPos[0], screenPos[1]}, dip);
        }
    }

    private void renderAt(Graphics2D g, int x, int y) {
        if (staticTexture != null) {
            g.drawImage(staticTexture, x, y, TILE_SIZE, TILE_SIZE, null);
        } else if (colorDecoded != null) {
            g.setColor(colorDecoded);
            Rendering.borderRect(g, 2, Color.black, x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    public void renderEdge(Graphics2D g, World world, double[] screenPos, int dip) {
        dip -= this.dip;
        if (dip <= 0) return;

        final int screenX = (int) screenPos[0];
        final int screenY = (int) screenPos[1] + dipPixels;
        final int edgeHeight = (int) Math.round(TILE_UNIT * dip);

        if (staticEdgeTexture != null) {
            g.drawImage(staticEdgeTexture, screenX, screenY, screenX + TILE_SIZE, screenY + edgeHeight, 0, 0, 16, dip, null);
        } else if (edgeColorDecoded != null) {
            g.setColor(edgeColorDecoded);
            g.fillRect(screenX, screenY, TILE_SIZE, edgeHeight);
            Rendering.borderOutline(g, 2, Color.black, screenX, screenY, TILE_SIZE, edgeHeight);
        }
    }

    private String getColor(World world) {
        final String natColorId = texture.get("natColor");
        if (natColorId != null) {
            final String natColor = world.biome.textureMap.get(natColorId);
            if (natColor != null) return natColor;
        }
        return texture.getOrDefault("baseColor", null);
    }

    private String getEdgeColor(World world) {
        final String natColorId = texture.get("natColorEdge");
        if (natColorId != null) {
            final String natColor = world.biome.textureMap.get(natColorId);
            if (natColor != null) return natColor;
        }
        final String baseColorEdge = texture.get("baseColorEdge");
        if (baseColorEdge != null) return baseColorEdge;
        return color;
    }

    private String getMapColor(World world) {
        final String mColor = texture.get("mapColor");
        if (mColor != null) {
            return mColor;
        }
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = world.biome.textureMap.get(natColorId);
            if (natColor != null) {
                return natColor;
            }
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

    private BufferedImage getEdgeTexture() {
        final String edgeTexture = texture.get("textureEdge");
        if (edgeTexture != null) {
            return Rendering.texture(edgeTexture, edgeColor);
        }
        return null;
    }

    private BufferedImage getFrame(String postfix) {
        String textureString = texture.get("gif");
        if (textureString != null) {
            final int frameSize = attributes.getOrDefault("frameSize", 16).intValue();
            return Rendering.renderGIF(textureString + postfix, color, frame / (double) maxFrames, frameSize, frameSize);
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
