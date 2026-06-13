package honey.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import honey.HoneySuckle;
import honey.mechanics.ConfigManager;
import honey.rendering.Rendering;

/*
 * WorldObject.java *
 - Class for handling world objects
 - Static json data
 */
public class WorldObject {

    public static ConfigManager config;

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
    private final String anim;
    public List<Map<String, Number>> loot;

    private double frameDamage = 0;

    private final int glowColor;
    private final String color;
    private final Color colorDecoded;
    private final Color overlayColor;
    public final Color mapColor;
    public boolean rendered = false;
    private BufferedImage staticTexture;

    private final String variant;
    private final int maxFrames;
    private int frame = 0;

    //WorldObject Contructor
    public WorldObject(int id, int[] posIndex, World world) {
        this.id = id;
        this.posIndex = posIndex;
        //Interprets entity tags and attributes
        tags = objTags.get(id);
        attributes = objAttributes.get(id);
        texture = objTextures.get(id);
        anim = texture.getOrDefault("anim", "");
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
        colorDecoded = Rendering.decodeColor(color);
        overlayColor = Rendering.decodeColor(texture.get("overlayColor"), 16);
        mapColor = Rendering.decodeColor(getMapColor(world));

        maxFrames = attributes.getOrDefault("animFrames", config.fps).intValue();

        variant = getVariant();
        staticTexture = getTexture(getPostfix());

    }

    public void setLoot(List<Map<String, Number>> newLoot) {
        newLoot.addAll(loot);
        loot = newLoot;
    }

    //Render WorldObject
    public void render(Graphics2D g, World world, double[] screenPos) {
        //If object has texture, render it
        int size = config.tileSize;
        int posX = (int) screenPos[0];
        int posY = (int) screenPos[1];

        if (overlayColor != null) {
            g.setColor(overlayColor);
            g.fillRect(posX, posY, size, size);
        }

        if (frameDamage != 0 && ThreadLocalRandom.current().nextBoolean()) {
            size *= .8;
            posX += config.tileSize / 10.0;
            posY += config.tileSize / 10.0;
        }

        if (tags.contains("sink")) {
            posY += world.grid[posIndex[0]][posIndex[1]].dipPixels;
        }

        if (anim.contains("_gif_")) {
            g.drawImage(getFrame(getPostfix()), posX, posY, size, size, null);
            frame = (frame + 1) % maxFrames;
        } else {
            if (staticTexture != null) {
                g.drawImage(staticTexture, posX, posY, size, size, null);
            } else {
                g.setColor(colorDecoded);
                Rendering.borderRect(g, 2, Color.black, posX, posY, size, size);
            }
        }

        frameDamage = Math.clamp(frameDamage - 0.1, 0, 1);
    }

    private String getColor(World world) {
        //If tile has biome specific color, find color from biome
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = world.biome.textureMap.get(natColorId);
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

    private String getMapColor(World world) {
        final String mColor = texture.get("mapColor");
        if(mColor != null){
            return mColor;
        }
        //If tile has biome specific color, find color from biome
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = world.biome.textureMap.get(natColorId);
            if (natColor != null) {
                return natColor;
            }
            //If tile has listed base color, set as color
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

    private String getPostfix() {
        StringBuilder postfix = new StringBuilder(variant);

        if(anim.contains("_destroyed_")){
            if(durability <= 0){
                postfix.append("_destroyed");
            }
        }

        return postfix.toString();
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", screenPos[0] + config.tileSize / 2,
                "posY", screenPos[1] + config.tileSize / 2,
                "radius", attributes.getOrDefault("lightRadius", 0),
                "color", glowColor,
                "glow", attributes.getOrDefault("glow", 0),
                "glowRadius", attributes.getOrDefault("glowRadius", 0)
        ));
    }

    //Damages object; returns true if broken
    public boolean damage(double damage) {
        //If can't break, go fuck off
        if (!tags.contains("destructable")) {
            return false;
        }
        //Damages object
        if (durability <= 0) {
            destroy();
            return false;
        } else {
            durability -= damage;
            frameDamage = damage;
        }
        //If still there, return false
        if (durability > 0) {
            return false;
        }
        //If broken, remove object and return true
        destroy();
        return true;
    }

    private void destroy() {
        if (anim.contains("_destroyed_")) {
            staticTexture = getTexture(getPostfix());
        } else {
            World.worlds.get(World.level).objGrid[posIndex[0]][posIndex[1]] = null;
        }
    }
}
