
import java.awt.Graphics2D;
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
    public static final Map<String, Map<String, Double>> armorAttributes = new HashMap<>();

    //Basic Armor Attributes
    private final String type;
    private final Map<String, String> texture;
    public final Map<String, Double> attributes;

    //Armor Constructor
    public Armor(String type) {
        //Interprets armor type
        this.type = type;
        texture = armorTextures.get(type);
        attributes = armorAttributes.get(type);
    }

    //Render Armor
    public void render(Graphics2D g, Player player) {
        //If armor texture exists, render it; already rotated to fit player
        if (texture.get("texture") != null) {
            g.drawImage(
                    Rendering.texture(texture.get("texture"), "#ffffff"),
                    (int) (player.screenPos[0] - player.size / 2),
                    (int) (player.screenPos[1] - player.size / 2),
                    player.size, player.size, null
            );
        }
    }

    //Update Armor
    public void update(Player player){}
}