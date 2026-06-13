package honey.player.armory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.mechanics.ConfigManager;
import honey.player.Player;
import honey.rendering.Rendering;

/*
 * Armor.java *
 - Class for managing general attributes of player and armor
 - COntains static json data
 */
public class Armor {

    public static ConfigManager config;

    //Static json data
    public static final Map<String, Map<String, String>> armorTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> armorAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> armorStats = new HashMap<>();
    public static final Map<String, List<String>> armorRecipeUnlocks = new HashMap<>();
    public static final Map<String, List<String>> armorBlueprintUnlocks = new HashMap<>();
    public static final Map<String, String> armorNames = new HashMap<>();
    public static final Map<String, Integer> armorIntId = new HashMap<>();
    public static final Map<Integer, String> armorStringId = new HashMap<>();

    //Basic Armor Attributes
    public final String type;
    public final Map<String, String> texture;
    public final Map<String, Number> attributes;
    public final Map<String, String> stats;
    public final String name;

    private final BufferedImage staticTexture;

    //Armor Constructor
    public Armor(String type) {
        //Interprets armor type
        this.type = type;
        texture = armorTextures.get(type);
        attributes = armorAttributes.get(type);
        stats = armorStats.get(type);
        name = armorNames.get(type);

        staticTexture = getTexture();
    }

    //Render Armor
    public void render(Graphics2D g, Player player) {
        //If armor texture exists, render it; already rotated to fit player
        if (staticTexture != null) {
            g.drawImage(
                    staticTexture,
                    (int) (player.screenPos[0] - player.size / 2.0),
                    (int) (player.screenPos[1] - player.size / 2.0),
                    player.size, player.size, null
            );
        }
    }

    public void renderUiTile(Graphics2D g, int x, int y, double factor, boolean active) {
        if (active) {
            g.drawImage(Rendering.texture("ui/slots/armor", null), (int) (x - 60 * (factor - 1)), (int) (y - 60 * (factor - 1)), (int) (120 * factor), (int) (120 * factor), null);
        } else {
            g.drawImage(Rendering.texture("ui/slots/armor", "#666666"), (int) (x - 60 * (factor - 1)), (int) (y - 60 * (factor - 1)), (int) (120 * factor), (int) (120 * factor), null);
        }

        final String itemTexture = texture.get("itemTexture");
        if (itemTexture != null) {
            g.drawImage(Rendering.texture(itemTexture, null), x + 10, y + 10, 100, 100, null);
        }
    }

    public void renderScroll(Graphics2D g) {
        final int scale = (int) Math.floor(3.5 * config.hudSize / 32);
        final int scrollLength = stats.size() + 3;
        final int renderedW = 32 * scale;
        final int renderedH = scrollLength * 4 * scale;
        final int scrollY = (config.gameHeight - renderedH) / 2;
        final int scrollCenterX = 25 + renderedW / 2;
        g.drawImage(Rendering.rotateImage(Rendering.scroll(scrollLength), 90), 25, scrollY, renderedW, renderedH, null);

        g.setColor(Rendering.decodeColor(texture.getOrDefault("rarityColor", "#333333")));

        Rendering.centeredText(g, name, scrollCenterX, scrollY + 6 * scale, renderedW, 32);

        g.setFont(new Font("VT323 Regular", Font.PLAIN, 24));
        g.setColor(Color.BLACK);
        String[] statKeys = stats.keySet().toArray(String[]::new);
        for (int i = 0; i < statKeys.length; i++) {
            Rendering.centeredText(g, statKeys[i] + ": " + stats.get(statKeys[i]), scrollCenterX, scrollY + 10 * scale + 4 * scale * i);
        }
    }

    private BufferedImage getTexture() {
        String textureId = texture.get("texture");
        if (textureId != null) {
            return Rendering.texture(textureId, null);
        }
        return null;
    }

    //Update Armor
    public void update(Player player) {
    }
}
