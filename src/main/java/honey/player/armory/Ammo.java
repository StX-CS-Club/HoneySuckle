package honey.player.armory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.mechanics.ConfigManager;
import honey.rendering.Rendering;

public class Ammo {

    public static ConfigManager config;

    public static final Map<String, String> ammoNames = new HashMap<>();
    public static final Map<String, Map<String, Number>> ammoAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> ammoTextures = new HashMap<>();
    public static final Map<String, Map<String, String>> ammoStats = new HashMap<>();
    public static final Map<String, List<String>> ammoTypes = new HashMap<>();
    public static final Map<String, List<String>> ammoRecipeUnlocks = new HashMap<>();
    public static final Map<String, List<String>> ammoBlueprintUnlocks = new HashMap<>();
    public static final Map<String, Integer> ammoIntId = new HashMap<>();
    public static final Map<Integer, String> ammoStringId = new HashMap<>();

    public final String type;
    public int count;
    public final List<String> types;
    private final Map<String, Number> attributes;
    public final Map<String, String> texture;
    private final Map<String, String> stats;
    private final String name;

    private final Map<String, Map<String, Number>> mergedAttributes = new HashMap<>();

    public Ammo(String type, int count) {
        this.type = type;
        this.count = count;

        name = ammoNames.get(type);
        types = ammoTypes.get(type);
        attributes = ammoAttributes.get(type);
        texture = ammoTextures.get(type);
        stats = ammoStats.get(type);
    }

    public Map<String, Number> mergeAttributes(String weaponType, Map<String, Object> weaponBehavior) {
        final Map<String, Number> result = mergedAttributes.getOrDefault(weaponType, new HashMap<>());

        if (result.isEmpty()) {
            Map<String, Number> weaponAttributes = numberMap(weaponBehavior);
            result.put("proj", weaponAttributes.getOrDefault("proj", attributes.get("proj")));
            result.put("damage", mergeAttribute("damage", attributes, weaponAttributes));
            result.put("speed", mergeAttribute("speed", attributes, weaponAttributes));
            result.put("weight", mergeAttribute("weight", attributes, weaponAttributes));
            result.put("size", mergeAttribute("size", attributes, weaponAttributes));

            mergedAttributes.put(weaponType, result);
        }

        return result;
    }

    public void renderUiTile(Graphics2D g, int x, int y, double factor) {
        final String color = texture.get("rarityColor");
        Rendering.scale(Rendering.texture("ui/slots/ammo", color), g, x, y, 100, 100, factor);

        final String itemTexture = texture.get("texture");
        if (itemTexture != null) {
            g.drawImage(Rendering.texture(itemTexture, null), x + 15, y + 20, 70, 70, null);
        }

        final String label = name + " x" + count;

        if (count > 0) {
            g.setColor(new Color(224, 224, 224));
        } else {
            g.setColor(Color.RED);
        }

        // Draws the font
        Rendering.centeredText(g, label, x + 50, y + 100, (int) (100 * factor), (int) (24 * factor));
    }

    public void renderScroll(Graphics2D g) {
        final int scale = (int) Math.floor(2.5 * config.hudSize / 32);
        final int renderedW = (14 * 4 + 8) * scale;
        final int renderedH = 32 * scale;
        final int scrollX = config.gameWidth / 2 - renderedW / 2;
        final int scrollTop = 20;
        g.drawImage(Rendering.scroll(14), scrollX, scrollTop, renderedW, renderedH, null);

        g.setColor(Rendering.decodeColor(texture.getOrDefault("rarityColor", "#333333")));
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));

        Rendering.centeredText(g, name, config.gameWidth / 2, scrollTop + 8 * scale);

        if (count > 0) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(Color.RED);
        }
        Rendering.centeredText(g, "x" + count, config.gameWidth / 2, scrollTop + renderedH - 14);

        final String[] statKeys = stats.keySet().toArray(String[]::new);

        final int columns = Math.ceilDiv(statKeys.length, 3);
        final int width = renderedW / columns;
        final int x = scrollX + renderedW / 2 / columns;

        for (int i = 0; i < columns; i++) {
            for (int e = 0; e < 3; e++) {
                final int index = i * 3 + e;
                if (index < statKeys.length) {
                    Rendering.centeredText(g, statKeys[index] + ": " + stats.get(statKeys[index]), x + width * i, scrollTop + 12 * scale + 32 * e, width - 10, 24);
                }
            }
        }
    }

    private Map<String, Number> numberMap(Map<String, Object> map) {
        final Map<String, Number> result = new HashMap<>();
        for (String key : map.keySet()) {
            final Object value = map.get(key);
            if (value instanceof Number number) {
                result.put(key, number);
            }
        }
        return result;
    }

    private double mergeAttribute(String key, Map<String, Number> attribute1, Map<String, Number> attribute2) {
        return attribute1.getOrDefault(key, 1).doubleValue() * attribute2.getOrDefault(key, 1).doubleValue();
    }

    public Map<String, Object> toJson() {
        return Map.of(
            "type", type,
            "count", count
        );
    }

    public static Ammo fromJson(Map<String, Object> json) {
        return new Ammo((String) json.get("type"), ((Number) json.get("count")).intValue());
    }
}
