
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Item.java *
 - Class for managing player inventories
 */
public class Inventory {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Inventory data
    public final List<Weapon> weapons = new ArrayList<>();
    public final List<Armor> armors = new ArrayList<>();
    public final List<Item> items = new ArrayList<>();
    public final Map<String, Integer> ammo = new HashMap<>();

    private final List<Item> splashItems = new ArrayList<>();

    private final Player player;

    public boolean isOpen = false;
    private double itemScroll = 0;
    private double weaponScroll = 0;

    //Inventory Constructor
    public Inventory(
            Player player,
            List<Weapon> weapons,
            List<Armor> armors,
            List<Item> items,
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
            this.items.addAll(items);
        }
        if (ammo != null) {
            this.ammo.putAll(ammo);
        }
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

    public void renderItemSplashes(Graphics2D g, double[] screenPos) {
        for (int i = 0; i < Math.min(splashItems.size(), 3); i++) {
            Item item = splashItems.get(i);

            if (!item.renderSplash(g, (int) screenPos[0], (int) (screenPos[1] - 25 * i - TILE_SIZE*1.25))) {
                splashItems.remove(item);
            }
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

            final int offset = 110 * i - ((int) Math.floor(itemScroll * 110));
            items.get(i).renderUiTile(g, GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50);
        }

        // Displays empty slot when items left to be collected
        if (items.size() < Item.itemNames.size()) {
            g.drawImage(Rendering.texture("hud/slot", "#ffffff"), GAME_WIDTH / 2 - 50 + 110 * items.size() - ((int) Math.floor(itemScroll * 110)), GAME_HEIGHT / 2 - 50, 100, 100, null);
        }
    }

    public void incrementItem(Map<String, Number> itemData, boolean gain) {
        if (Math.random() < readLoot(itemData, "prob", 1).doubleValue()) {
            final String itemId = Item.itemStringId.get(readLoot(itemData, "item", 0).intValue());
            Item item = getItem(itemId);

            int itemCount = readLoot(itemData, "count", 1).intValue();
            if(!gain){
                itemCount *= -1;
            }
            if (item == null) {
                item = new Item(itemId, itemCount);
                items.add(item);
            } else {
                item.count += itemCount;

                final List<String> recipeUnlocks = Item.itemRecipeUnlocks.get(itemId);
                for (String recipe : recipeUnlocks) {
                    player.build.blueprints.add(recipe);
                }
            }

            if (gain) {
                Item splashItem = getSplashItem(itemId);
                if (splashItem == null) {
                    splashItem = new Item(itemId, itemCount);
                    splashItems.add(splashItem);
                } else {
                    splashItem.count += itemCount;
                    splashItem.resetSplashFrame();
                }
            }
        }
    }

    private Item getItem(String itemId) {
        for (Item item : items) {
            if (item.id.equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    private Item getSplashItem(String itemId) {
        for (Item item : splashItems) {
            if (item.id.equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public int getItemCount(String itemId) {
        for (Item item : items) {
            if (item.id.equals(itemId)) {
                return item.count;
            }
        }
        return 0;
    }

    private Number readLoot(Map<String, Number> loot, String value, Number defaultValue) {
        Number result = loot.get(value);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }
}
