
package honey.world;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.rendering.Rendering;

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
    private static final int FPS = HoneySuckle.FPS;

    //Satic data imported from json files
    public static final Map<String, Map<String, Number>> entityAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> entityTextures = new HashMap<>();
    public static final Map<String, List<Map<String, Number>>> entityLoot = new HashMap<>();
    public static final Map<String, List<String>> entityTags = new HashMap<>();
    public static final Map<String, String> entityNames = new HashMap<>();
    public static final Map<Integer, String> entityStringId = new HashMap<>();
    public static final Map<String, Integer> entityIntId = new HashMap<>();

    //Basic attributes
    public final String type;
    public double health;
    public final double size;
    public final double weight;
    public final String name;

    public final Brain brain;

    //All attributes
    public final Map<String, Number> attributes;
    public final List<String> tags;
    public final List<Map<String, Number>> loot;
    private final Map<String, String> texture;

    //Number of frames to render as red
    public int damageFrames = 0;
    private int healthBarFrames = 0;
    private final String color;
    private final Color colorDecoded;
    private final String staticTextureId;
    private final String animTextureId;
    private final String animation;
    private final Map<String, long[]> animFrames = new HashMap<>();
    private final int maxFrames;
    private int frame = 0;

    //Position variables
    public double[] pos = new double[2];
    public double[] vel = new double[2];

    //Update ticks
    public final Map<String, long[]> ticks = new HashMap<>();

    //Entity Constructor
    public Entity(String type, double[] pos, World world) {
        //Gets attributes and tags based on type
        this.type = type;
        attributes = entityAttributes.get(type);
        tags = entityTags.get(type);
        loot = entityLoot.get(type);
        texture = entityTextures.get(type);
        name = entityNames.get(type);

        ticks.put("base", new long[]{0});

        //Interprets basic attributes
        health = attributes.getOrDefault("health", 1).doubleValue();
        size = attributes.getOrDefault("size", 1).doubleValue() * TILE_SIZE;
        weight = attributes.getOrDefault("weight", 1).doubleValue();
        this.pos = pos.clone();

        brain = new Brain(this, world);
        color = getColor(world);
        colorDecoded = Rendering.decodeColor(color);
        staticTextureId = texture.get("texture");
        animTextureId = texture.get("gif");
        animation = texture.get("anim");
        maxFrames = attributes.getOrDefault("animFrames", FPS).intValue();
    }

    //Render Entity
    public void render(Graphics2D g, double[] camera) {
        //Position of entity on screen
        double[] screenPos = new double[]{
            GAME_WIDTH / 2.0 + pos[0] - camera[0] - size / 2.0,
            GAME_HEIGHT / 2.0 + pos[1] - camera[1] - size / 2.0
        };
        double[] screenSize = new double[]{size, size};

        //If entity has texture, display
        if (staticTextureId != null || animTextureId != null) {
            String baseId = animTextureId != null ? animTextureId : staticTextureId;
            StringBuilder textureId = new StringBuilder(baseId);
            //Add parameters to stem file name, if applicable
            if (animation != null) {
                if (animation.contains("_x_")) {
                    if (brain.trackAngle > 180) {
                        textureId.append("_left");
                    } else {
                        textureId.append("_right");
                    }
                }
                if (animation.contains("_y_")) {
                    if (brain.trackAngle >= 270 || brain.trackAngle < 90) {
                        textureId.append("_up");
                    } else {
                        textureId.append("_down");
                    }
                }
                if (animation.contains("_xy_")) {
                    if (brain.trackAngle >= 315 || brain.trackAngle < 45) {
                        textureId.append("_up");
                    } else if (brain.trackAngle < 135) {
                        textureId.append("_right");
                    } else if (brain.trackAngle < 225) {
                        textureId.append("_down");
                    } else {
                        textureId.append("_left");
                    }
                }
                if (animation.contains("_chase_")) {
                    if (brain.checkState("chase")) {
                        textureId.append("_chase");
                    }
                }
                if (animation.contains("_hesitate_")) {
                    if (brain.checkState("hesitate")) {
                        textureId.append("_hesitate");
                    }
                }
                if (animation.contains("_shoot_")) {
                    if (brain.checkState("shooting")) {
                        textureId.append("_shoot");
                    }
                }
                if (animation.contains("_lunge_")) {
                    if (brain.checkState("lunging")) {
                        screenPos[1] += screenSize[1] * .375;
                        screenSize[1] *= 0.75;
                    }
                }
                if (animation.contains("_hover_")) {
                    screenPos[1] += Math.sin(incrementFrames("hover", 1) / 10.0)*size/8;
                }
            }

            //Rotate to face locked target
            AffineTransform originalTransform = null;
            if (animation != null && animation.contains("_face_")) {
                double cx = screenPos[0] + screenSize[0] / 2.0;
                double cy = screenPos[1] + screenSize[1] / 2.0;
                originalTransform = g.getTransform();
                g.rotate(Math.toRadians(brain.trackAngle), cx, cy);
            }

            //Draw animated sprite sheet or static texture
            final String textureIdStr = textureId.toString();
            final BufferedImage textureImage;
            if (animTextureId != null && animation != null && animation.contains("_gif_")) {
                textureImage = Rendering.renderGIF(textureIdStr, color, frame / (double) maxFrames);
                frame = (frame + 1) % maxFrames;
            } else {
                textureImage = damageFrames > 0
                        ? Rendering.applyOverlay(textureIdStr, color, "#ff0000", 128)
                        : Rendering.texture(textureIdStr, color);
            }
            damageFrames--;

            g.drawImage(textureImage, (int) screenPos[0], (int) screenPos[1], (int) screenSize[0], (int) screenSize[1], null);

            if (originalTransform != null) {
                g.setTransform(originalTransform);
            }
        } else {
            g.setColor(colorDecoded);
            Rendering.borderRect(g, 2, Color.black, (int) screenPos[0], (int) screenPos[1], (int) screenSize[0], (int) screenSize[1]);
        }
    }

    public void renderHealthBar(Graphics2D g, int index) {
        if (index < 3) {
            healthBarFrames++;
            final float opacity = Math.min(healthBarFrames * 3, 255) / 255f;
            Composite originalComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            g.setColor(Color.DARK_GRAY);
            g.fillRect(GAME_WIDTH / 4, 25 + index * 35, GAME_WIDTH / 2, 10);

            final double barWidth = (GAME_WIDTH / 2) * (health / attributes.get("health").doubleValue());

            g.setColor(Rendering.decodeColor(texture.getOrDefault("healthBarColor", "#c4021f")));
            g.fillRect(GAME_WIDTH / 4, 25 + index * 35, (int) Math.ceil(barWidth), 10);

            final String symbol = texture.getOrDefault("healthBarSymbol", "=");
            final String label = (symbol + " " + name + " " + symbol).toUpperCase();

            g.setFont(new Font("VT323 Regular", Font.PLAIN, 24));
            g.setColor(Color.WHITE);

            Rendering.centeredText(g, label, GAME_WIDTH / 2, 30 + index * 35);

            g.setComposite(originalComposite);
        }

    }

    private String getColor(World world) {
        //If entity has biome specific color, get color from biome
        String natColorId = texture.get("natColor");
        if (natColorId != null) {
            String natColor = world.biome.textureMap.get(natColorId);
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
        ticks.get("base")[0]++;
        //Update entity through Brain
        brain.update();
    }

    private long incrementFrames(String key, int amount) {
        final long[] frames = animFrames.get(key);
        if (frames != null) {
            frames[0] += amount;
            return frames[0];
        }
        animFrames.put(key, new long[]{amount});
        return amount;
    }
}
