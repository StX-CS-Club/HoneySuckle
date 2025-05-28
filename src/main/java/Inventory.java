
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.ItemSelectable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Inventory.java *
 - Class for managing player inventories
 */
public class Inventory {

    //Static json data
    public static final Map<Integer, String> itemNames = new HashMap<>();
    public static final Map<Integer, Map<String, String>> itemTextures = new HashMap<>();

    //Inventory data
    public final List<Weapon> weapons = new ArrayList<>();
    public final List<Armor> armors = new ArrayList<>();
    public final Map<Integer, Integer> items = new HashMap<>();
    public final Map<String, Integer> ammo = new HashMap<>();

    public boolean isOpen = false;
    private double itemScroll = 0;

    //Inventory Constructor
    public Inventory(
            List<Weapon> weapons,
            List<Armor> armors,
            Map<Integer, Integer> items,
            Map<String, Integer> ammo) {
        //If specified, adds shit to inventory
        if (weapons != null) {
            this.weapons.addAll(weapons);
        }
        if (armors != null) {
            this.armors.addAll(armors);
        }
        if (items != null) {
            this.items.putAll(items);
        }
        if (ammo != null) {
            this.ammo.putAll(ammo);
        }
    }

    public int getMaterial(int material) {
        if (items.get(material) == null) {
            return 0;
        }
        return items.get(material);
    }

    public void update(Input input) {
        if (input.keyPressed(69)) {
            isOpen = !isOpen;
        }
        if (isOpen) {
            itemScroll += input.mouseScroll;
            if (itemScroll < 0) {
                itemScroll = 0;
            } else if (itemScroll > items.size() - 1) {
                itemScroll = items.size() - 1;
            }
        }
    }

    public void renderUi(Graphics2D g) {
        if (isOpen) {
            final int[] size = HoneySuckle.size;
            g.setColor(new Color(64, 64, 64, 192));
            g.fillRect(0, 0, size[0], size[1]);

            for (int i = 0; i < items.size(); i++) {
                final int itemId = (int) items.keySet().toArray()[i];

                String color = "#ffffff";
                if (items.get(itemId) > 0) {
                    color = "#f5d39d";
                }

                final int offset = 110 * i - ((int) Math.floor(itemScroll * 110));
                g.drawImage(Rendering.texture("hud/slot", color), size[0] / 2 - 50 + offset, size[1] / 2 - 50, 100, 100, null);

                final Map<String, String> texture = itemTextures.get(itemId);

                final String itemTexture = texture.get("texture");
                if (itemTexture != null) {
                    g.drawImage(Rendering.texture(itemTexture, "#ffffff"), size[0] / 2 - 25 + offset, size[1] / 2 - 25, 50, 50, null);
                }

                final String label = itemNames.get(itemId) + " x" + items.get(itemId);

                int fontOffset = 0;

                // Sets the font size to fit within box
                for(int f = 24; f > 0; f--){
                    g.setFont(new Font("VT323 Regular", Font.PLAIN, f));
                    if(g.getFontMetrics().stringWidth(label) < 100){
                        fontOffset = g.getFontMetrics().stringWidth(label)/2;
                        break;
                    }
                }
                
                g.setColor(new Color(224, 224, 224));

                // Draws the font
                g.drawString(label, size[0] / 2 + offset - fontOffset, size[1] / 2 + 60);
            }
            
            // Displays empty slot when items left to be collected
            if(items.size() < itemNames.size()){
                g.drawImage(Rendering.texture("hud/slot", "#ffffff"), size[0] / 2 - 50 + 110 * items.size() - ((int) Math.floor(itemScroll * 110)), size[1] / 2 - 50, 100, 100, null);
            }
        }
    }
}
