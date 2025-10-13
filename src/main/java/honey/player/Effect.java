package honey.player;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.mechanics.MapReader;
import honey.rendering.Rendering;

public class Effect {

    private static final int FPS = HoneySuckle.FPS;
    private static final int HUD_SIZE = HoneySuckle.HUD_SIZE;

    public static final Map<String, Map<String, String>> effectTextures = new HashMap<>();
    public static final Map<String, List<String>> effectTags = new HashMap<>();
    public static final Map<String, Map<String, Number>> effectModifiers = new HashMap<>();
    public static final Map<String, String> effectNames = new HashMap<>();
    public static final Map<String, Integer> effectIntId = new HashMap<>();
    public static final Map<Integer, String> effectStringId = new HashMap<>();

    private final String type;
    private final long duration;
    private final double amplifier;

    private final Map<String, String> texture;
    private final Map<String, Number> modifiers;
    private final List<String> tags;

    private long frame = 0;
    public boolean active = true;

    public Effect(String type, long duration, double amplifier) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;

        texture = effectTextures.get(type);
        modifiers = effectModifiers.get(type);
        tags = effectTags.get(type);
    }

    public Effect(Map<String, Object> effect) {
        type = MapReader.getOrDefault(effect, "effect", "speed");

        texture = effectTextures.get(type);
        tags = effectTags.get(type);

        this.modifiers = MapReader.putAllIfAbsent(
                MapReader.castMap(effect, Number.class),
                effectModifiers.get(type));

        duration = modifiers.getOrDefault("duration", 10).longValue() * FPS;
        amplifier = modifiers.getOrDefault("amplifier", 1).doubleValue();
    }

    public Effect(String type, Map<String, Number> modifiers) {
        texture = effectTextures.get(type);
        tags = effectTags.get(type);

        MapReader.putAllIfAbsent(modifiers, effectModifiers.get(type));
        this.modifiers = modifiers;

        this.type = type;
        duration = modifiers.getOrDefault("duration", 10).longValue() * FPS;
        amplifier = modifiers.getOrDefault("amplifier", 1).doubleValue();
    }

    public void update() {
        if (frame < duration) {
            frame++;
        } else {
            active = false;
        }
    }

    public void modify(Map<String, Number> attributes) {
        if (active) {
            for (String key : modifiers.keySet()) {
                double modifier = 1 + modifiers.get(key).doubleValue() * amplifier;
                attributes.put(key, modifier * MapReader.getOrGet(attributes, Player.playerDefaultAttributes, key).doubleValue());
            }
        }
    }

    public boolean renderUi(Graphics2D g, int x, int y) {
        String textureString = texture.get("texture");
        if (textureString != null) {
            g.drawImage(Rendering.texture(textureString, "#ffffff"), x, y, HUD_SIZE / 2, HUD_SIZE / 2, null);

            if (duration > 0) {
                g.setColor(new Color(255, 255, 255, 96));

                int size = (int) Math.round(frame / (double) duration * HUD_SIZE / 2.0);
                g.fillRect(x, y + size, HUD_SIZE / 2, HUD_SIZE / 2 - size);
            }

            return true;
        }

        return false;
    }
}
