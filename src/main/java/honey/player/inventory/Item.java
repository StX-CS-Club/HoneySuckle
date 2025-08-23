package honey.player.inventory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.rendering.Rendering;

public class Item {

    //Static json data
    public static final Map<String, String> itemNames = new HashMap<>();
    public static final Map<String, Map<String, String>> itemTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> itemAttributes = new HashMap<>();
    public static final Map<String, List<String>> itemBlueprintUnlocks = new HashMap<>();
    public static final Map<String, List<String>> itemRecipeUnlocks = new HashMap<>();
    public static final Map<Integer, String> itemStringId = new HashMap<>();
    public static final Map<String, Integer> itemIntId = new HashMap<>();

    final String id;
    int count;

    private final String name;
    private final Map<String, String> texture;
    private final Map<String, Number> attributes;

    public Item(String id, int count) {
        this.id = id;
        this.count = count;

        name = itemNames.get(id);
        texture = itemTextures.get(id);
        attributes = itemAttributes.get(id);
    }

    public void renderUiTile(Graphics2D g, int x, int y) {
        String color = null;
        if (count > 0) {
            color = "#f5d39d";
        }

        g.drawImage(Rendering.texture("hud/slots/item", color), x, y, 100, 100, null);

        final String itemTexture = texture.get("texture");
        if (itemTexture != null) {
            g.drawImage(Rendering.texture(itemTexture, null), x + 25, y + 25, 50, 50, null);
        }

        final String label = name + " x" + count;

        g.setColor(new Color(224, 224, 224));

        // Draws the font
        Rendering.centeredText(g, label, x + 50, y + 100, 100, 24);
    }
}
