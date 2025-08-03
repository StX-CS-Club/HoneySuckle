
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/*
 * Armor.java *
 - Class for managing general attributes of player and armor
 - COntains static json data
 */
public class Armor {

    //Static json data
    public static final Map<String, Map<String, String>> armorTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> armorAttributes = new HashMap<>();
    public static final Map<String, Integer> armorIntId = new HashMap<>();
    public static final Map<Integer, String> armorStrignId = new HashMap<>();

    //Basic Armor Attributes
    public final String type;
    public final Map<String, String> texture;
    public final Map<String, Number> attributes;

    private final BufferedImage staticTexture;

    //Armor Constructor
    public Armor(String type) {
        //Interprets armor type
        this.type = type;
        texture = armorTextures.get(type);
        attributes = armorAttributes.get(type);

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
            g.drawImage(Rendering.texture("hud/armor_slot", "#ffffff"), (int) (x-60*(factor-1)), (int) (y-60*(factor-1)), (int) (120 * factor), (int) (120 * factor), null);
        } else {
            g.drawImage(Rendering.texture("hud/armor_slot", "#666666"), (int) (x-60*(factor-1)), (int) (y-60*(factor-1)), (int) (120 * factor), (int) (120 * factor), null);
        }

        final String itemTexture = texture.get("item_texture");
        if (itemTexture != null) {
            g.drawImage(Rendering.texture(itemTexture, "#ffffff"), x + 10, y + 10, 100, 100, null);
        }
    }

    private BufferedImage getTexture() {
        String textureId = texture.get("texture");
        if (textureId != null) {
            return Rendering.texture(textureId, "#ffffff");
        }
        return null;
    }

    //Update Armor
    public void update(Player player) {
    }
}
