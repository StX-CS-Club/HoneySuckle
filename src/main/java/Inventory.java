
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Inventory.java *
 - Class for managing player inventories
 */
public class Inventory {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;

    //Static json data
    public static final Map<String, String> itemNames = new HashMap<>();
    public static final Map<String, Map<String, String>> itemTextures = new HashMap<>();
    public static final Map<String, List<String>> itemRecipeUnlocks = new HashMap<>();
    public static final Map<Integer, String> itemStringId = new HashMap<>();
    public static final Map<String, Integer> itemIntId = new HashMap<>();

    //Inventory data
    public final List<Weapon> weapons = new ArrayList<>();
    public final List<Armor> armors = new ArrayList<>();
    public final Map<String, Integer> items = new HashMap<>();
    public final Map<String, Integer> ammo = new HashMap<>();

    private final Player player;

    public boolean isOpen = false;
    private double itemScroll = 0;
    private double weaponScroll = 0;

    //Inventory Constructor
    public Inventory(
            Player player,
            List<Weapon> weapons,
            List<Armor> armors,
            Map<String, Integer> items,
            Map<String, Integer> ammo) {
        this.player = player;
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

    public int getMaterial(String material) {
        if (items.get(material) == null) {
            return 0;
        }
        return items.get(material);
    }

    public void update(InputHandler input) {
        if (input.keyPressed(69)) {
            isOpen = !isOpen;
        }
        if (isOpen) {
            /*
            if (input.mousePos[1] < GAME_HEIGHT / 2 - 60) {
                weaponScroll += input.mouseScroll;
                if (weaponScroll < 0) {
                    weaponScroll = 0;
                } else if (weaponScroll > weapons.size() - 1) {
                    weaponScroll = weapons.size() - 1;
                }
            } else {*/
            itemScroll += input.mouseScroll;
            if (itemScroll < 0) {
                itemScroll = 0;
            } else if (itemScroll > items.size() - 1) {
                itemScroll = items.size() - 1;
            }
            //}
        }
    }

    public void renderUi(Graphics2D g) {
        g.setColor(new Color(64, 64, 64, 192));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        /* 
        // Renders Weapons
        for (int i = 0; i < weapons.size(); i++) {
            final Weapon weapon = weapons.get(i);
            final String weaponTexture = weapon.texture.get("itemTexture");

            String color = weapon.texture.get("rarityColor");
            if(color == null){
                color = "#ffffff";
            }

            final int offset = 110 * i - ((int) Math.floor(weaponScroll * 110));
            g.drawImage(Rendering.texture("hud/weapon_slot", color), GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 160, 100, 100, null);

            if (weaponTexture != null) {
                g.drawImage(Rendering.texture(weaponTexture, "#ffffff"), GAME_WIDTH / 2 - 38 + offset, GAME_HEIGHT / 2 - 148, 75, 75, null);
            }
        }
         */
        // Renders Items
        for (int i = 0; i < items.size(); i++) {
            final String itemId = (String) items.keySet().toArray()[i];

            String color = "#ffffff";
            if (items.get(itemId) > 0) {
                color = "#f5d39d";
            }

            final int offset = 110 * i - ((int) Math.floor(itemScroll * 110));
            g.drawImage(Rendering.texture("hud/slot", color), GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50, 100, 100, null);

            final Map<String, String> texture = itemTextures.get(itemId);

            final String itemTexture = texture.get("texture");
            if (itemTexture != null) {
                g.drawImage(Rendering.texture(itemTexture, "#ffffff"), GAME_WIDTH / 2 - 25 + offset, GAME_HEIGHT / 2 - 25, 50, 50, null);
            }

            final String label = itemNames.get(itemId) + " x" + items.get(itemId);

            int fontOffset = 0;

            // Sets the font size to fit within box
            for (int f = 24; f > 0; f--) {
                g.setFont(new Font("VT323 Regular", Font.PLAIN, f));
                if (g.getFontMetrics().stringWidth(label) < 100) {
                    fontOffset = g.getFontMetrics().stringWidth(label) / 2;
                    break;
                }
            }

            g.setColor(new Color(224, 224, 224));

            // Draws the font
            g.drawString(label, GAME_WIDTH / 2 + offset - fontOffset, GAME_HEIGHT / 2 + 60);
        }

        // Displays empty slot when items left to be collected
        if (items.size() < itemNames.size()) {
            g.drawImage(Rendering.texture("hud/slot", "#ffffff"), GAME_WIDTH / 2 - 50 + 110 * items.size() - ((int) Math.floor(itemScroll * 110)), GAME_HEIGHT / 2 - 50, 100, 100, null);
        }
    }

    public void addItem(Map<String, Number> item) {
        if (Math.random() < readLoot(item, "prob", 1).doubleValue()) {
            final String itemId = itemStringId.get(readLoot(item, "item", 0).intValue());
            items.put(itemId, getMaterial(itemId) + readLoot(item, "count", 1).intValue());

            final List<String> recipeUnlocks = itemRecipeUnlocks.get(itemId);
            if (recipeUnlocks != null) {
                for (String recipe : recipeUnlocks) {
                    player.build.blueprints.add(recipe);
                }
            }
        }
    }

    private Number readLoot(Map<String, Number> loot, String value, Number defaultValue) {
        Number result = loot.get(value);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }
}
