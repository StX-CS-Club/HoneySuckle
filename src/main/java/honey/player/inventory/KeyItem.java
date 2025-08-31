package honey.player.inventory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.rendering.Rendering;
import honey.world.World;

public class KeyItem {

    //Static json data
    public static final Map<String, String> keyNames = new HashMap<>();
    public static final Map<String, Map<String, String>> keyTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> keyAttributes = new HashMap<>();
    public static final Map<String, List<String>> keyBlueprintUnlocks = new HashMap<>();
    public static final Map<String, List<String>> keyRecipeUnlocks = new HashMap<>();
    public static final Map<String, Map<String, Map<String, Object>>> keyUtilities = new HashMap<>();
    public static final Map<Integer, String> keyStringId = new HashMap<>();
    public static final Map<String, Integer> keyIntId = new HashMap<>();

    final String id;
    int count;

    private final String name;
    private final Map<String, String> texture;
    private final Map<String, Number> attributes;
    private final Map<String, Map<String, Object>> utilities;

    private final Map<String, Integer> utilUses = new HashMap<>();

    private final Map<String, Object> mapUtility;

    public KeyItem(String id, int count) {
        this.id = id;
        this.count = count;

        name = keyNames.get(id);
        texture = keyTextures.get(id);
        attributes = keyAttributes.get(id);
        utilities = keyUtilities.get(id);

        mapUtility = registerUtility("map");
    }

    public void renderUiTile(Graphics2D g, int x, int y, double factor) {
        String color = null;
        if (count > 0) {
            color = "#f5d39d";
        }

        g.drawImage(Rendering.texture("hud/slots/key_item", color), (int) (x - 50 * (factor - 1)), (int) (y - 50 * (factor - 1)), (int) (100 * factor), (int) (100 * factor), null);

        final String itemTexture = texture.get("texture");
        if (itemTexture != null) {
            g.drawImage(Rendering.texture(itemTexture, null), x + 15, y + 15, 70, 70, null);
        }

        final String label = name + " x" + count;

        g.setColor(new Color(224, 224, 224));

        // Draws the font
        Rendering.centeredText(g, label, x + 50, y + 115, 100, 24);
    }

    public void update() {
        boolean reset = !utilUses.isEmpty();

        for (Integer use : utilUses.values()) {
            if (use != 0) {
                reset = false;
                break;
            }
        }

        if (reset) {
            resetUtil();
            count--;
        }
    }

    public void use() {
        final World world = World.worlds.get(World.level);
        final Map<String, Integer> staticUtilUses = Map.copyOf(utilUses);

        if (count > 0) {
            if (mapUtility != null) {
                String utilId = (String) mapUtility.get("utilId");
                int uses = staticUtilUses.get(utilId);

                if (uses != 0 && !world.navigator.started) {
                    world.navigator.started = true;
                    uses--;
                    utilUses.put(utilId, uses);
                }
            }
        }
    }

    private Map<String, Object> registerUtility(String utility) {
        final Map<String, Object> utilEntry = utilities.get(utility);
        if (utilEntry != null) {
            utilEntry.putIfAbsent("utilId", "base");
            String utilId = (String) utilEntry.get("utilId");

            int uses = Math.max(utilUses.getOrDefault(utilId, 1), (Integer) utilEntry.getOrDefault("uses", 1));
            utilUses.put(utilId, uses);
        }
        return utilEntry;
    }

    private void resetUtil() {
        utilUses.clear();

        registerUtility("map");
    }
}
