package honey.player.armory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.rendering.Rendering;

public class Ammo {
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int HUD_SIZE = HoneySuckle.HUD_SIZE;

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
        Map<String, Number> result = mergedAttributes.getOrDefault(weaponType, new HashMap<>());

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
        g.drawImage(Rendering.texture("ui/slots/ammo", color), (int) (x - 50 * (factor - 1)), (int) (y - 50 * (factor - 1)), (int) (100 * factor), (int) (100 * factor), null);

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
        Rendering.centeredText(g, label, x + 50, y + 100, (int) (100*factor), (int) (24 * factor));
    }
    
    public void renderScroll(Graphics2D g) {
        final int scale = (int) Math.floor(2.5 * HUD_SIZE / 32);
        final int renderedW = (14 * 4 + 8) * scale;
        final int renderedH = 32 * scale;
        final int scrollX = GAME_WIDTH / 2 - renderedW / 2;
        final int scrollTop = 20;
        g.drawImage(Rendering.scroll(14), scrollX, scrollTop, renderedW, renderedH, null);

        g.setColor(Rendering.decodeColor(texture.getOrDefault("rarityColor", "#333333")));
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));

        Rendering.centeredText(g, name, GAME_WIDTH / 2, scrollTop + 8 * scale);

        g.setColor(Color.BLACK);
        Rendering.centeredText(g, "x" + count, GAME_WIDTH / 2, scrollTop + renderedH - 22);

        String[] statKeys = stats.keySet().toArray(String[]::new);

        int columns = Math.ceilDiv(statKeys.length, 3);
        int width = renderedW / columns;
        int x = scrollX + renderedW / 2 / columns;

        for (int i = 0; i < columns; i++) {
            for (int e = 0; e < 3; e++) {
                int index = i * 3 + e;
                if (index < statKeys.length) {
                    Rendering.centeredText(g, statKeys[index] + ": " + stats.get(statKeys[index]), x + width * i, scrollTop + 12 * scale + 32 * e, width - 10, 24);
                }
            }
        }
    }

    private Map<String, Number> numberMap(Map<String, Object> map) {
        Map<String, Number> result = new HashMap<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Number) {
                result.put(key, (Number) value);
            }
        }
        return result;
    }

    private double mergeAttribute(String key, Map<String, Number> attribute1, Map<String, Number> attribute2) {
        return attribute1.getOrDefault(key, 1).doubleValue() * attribute2.getOrDefault(key, 1).doubleValue();
    }
}
