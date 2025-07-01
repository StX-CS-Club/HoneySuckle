
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
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
    private int page = 0;
    private final String[] iconColors = new String[2];

    private double itemScroll = 0;

    private double weaponScroll = 0;
    private int weaponHover = -1;
    private int weaponSelect = -1;

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

        Arrays.fill(iconColors, "#888888");
    }

    public void update(InputHandler input) {
        if (input.keyPressed(69)) {
            isOpen = !isOpen;
            weaponSelect = -1;
            setPage(0);
        }
        if (isOpen) {
            switch (page) {
                case 0 -> {
                    itemScroll = Math.clamp(itemScroll + input.mouseScroll, 0, items.size() - 1);
                }
                case 1 -> {
                    weaponScroll = Math.clamp(weaponScroll + input.mouseScroll, 0, weapons.size() - 1);
                    weaponHover = -1;
                    if (Math.abs(input.mousePos[1] - GAME_HEIGHT / 2) <= 50) {
                        double weaponHighlight = (input.mousePos[0] - (GAME_WIDTH / 2 - 50));
                        if (weaponSelect == -1) {
                            weaponHighlight += weaponScroll * 110;
                        }
                        if (weaponHighlight % 110 <= 100) {
                            weaponHover = (int) Math.floor(weaponHighlight / 110);
                        }
                    }
                }
            }

            if (input.clickPressed(1)) {
                if (weaponSelect != -1) {
                    if (Math.abs(weaponHover) <= 1) {
                        final Weapon weapon = weapons.get(weaponSelect);
                        for (int i = 0; i < 3; i++) {
                            if (weapon == player.armory.weapons[i]) {
                                player.armory.weapons[i] = null;
                                break;
                            }
                        }
                        player.armory.weapons[weaponHover + 1] = weapon;
                        weaponSelect = -1;
                    }
                } else if (page == 1 && weaponHover >= 0 && weaponHover < weapons.size()) {
                    weaponSelect = weaponHover;
                } else if (Math.abs(GAME_HEIGHT * 9 / 10 - input.mousePos[1]) <= 25) {
                    for (int i = 0; i < 2; i++) {
                        if (Math.abs(GAME_WIDTH / 2 - 35 + i * 70 - input.mousePos[0]) <= 25) {
                            setPage(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setPage(int newPage) {
        page = newPage;
        for (int i = 0; i < iconColors.length; i++) {
            if ("#ffffff".equals(iconColors[i])) {
                iconColors[i] = "#666666";
            }
        }
        iconColors[newPage] = "#ffffff";
    }

    public void renderItemSplashes(Graphics2D g, double[] screenPos) {
        for (int i = 0; i < Math.min(splashItems.size(), 3); i++) {
            Item item = splashItems.get(i);

            if (!item.renderSplash(g, (int) screenPos[0], (int) (screenPos[1] - 25 * i - TILE_SIZE * 1.25))) {
                splashItems.remove(item);
            }
        }
    }

    public void renderUi(Graphics2D g) {
        g.setColor(new Color(64, 64, 64, 192));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        if (weaponSelect != -1) {
            final Weapon weapon = weapons.get(weaponSelect);

            weapon.renderUiTile(g, GAME_WIDTH / 2 - 50, GAME_HEIGHT / 2 + 75, 1, player.armory.weapons);

            for (int i = 0; i < 3; i++) {
                Weapon iWeapon = player.armory.weapons[i];

                double factor = 1;
                if (weaponHover + 1 == i) {
                    factor = 1.1;
                }

                if (iWeapon != null) {
                    iWeapon.renderUiTile(g, GAME_WIDTH / 2 - 160 + i * 110, GAME_HEIGHT / 2 - 50, factor, player.armory.weapons);
                } else {
                    g.drawImage(Rendering.texture("hud/weapon_slot", "#ffffff"), (int) (GAME_WIDTH / 2 - 50 * factor + (i - 1) * 110), (int) (GAME_HEIGHT / 2 - 50 * factor), (int) (100 * factor), (int) (100 * factor), null);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("VT323 Regular", Font.PLAIN, 28));
                    Rendering.centeredText(g, Integer.toString(i + 1), (int) (GAME_WIDTH / 2 + 45 + (i - 1) * 110), (int) (GAME_HEIGHT / 2 + 50));
                }
            }
        } else {
            // Renders Items
            if (page == 0) {
                for (int i = 0; i < items.size(); i++) {

                    final int offset = 110 * i - ((int) Math.floor(itemScroll * 110));
                    items.get(i).renderUiTile(g, GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50);
                }

                // Displays empty slot when items left to be collected
                if (items.size() < Item.itemNames.size()) {
                    g.drawImage(Rendering.texture("hud/slot", "#ffffff"), GAME_WIDTH / 2 - 50 + 110 * items.size() - ((int) Math.floor(itemScroll * 110)), GAME_HEIGHT / 2 - 50, 100, 100, null);
                }
            } else if (page == 1) {
                // Renders Weapons
                for (int i = 0; i < weapons.size(); i++) {
                    final int offset = 110 * i - ((int) Math.floor(weaponScroll * 110));

                    if (i == weaponHover) {
                        weapons.get(i).renderUiTile(g, (int) (GAME_WIDTH / 2 - 50 + offset), (int) (GAME_HEIGHT / 2 - 50), 1.1, player.armory.weapons);
                    } else {
                        weapons.get(i).renderUiTile(g, (int) (GAME_WIDTH / 2 - 50 + offset), (int) (GAME_HEIGHT / 2 - 50), 1, player.armory.weapons);
                    }
                }

            }

            g.drawImage(Rendering.texture("hud/items", iconColors[0]), GAME_WIDTH / 2 - 60, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/weapons", iconColors[1]), GAME_WIDTH / 2 + 10, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
        }
    }

    public void incrementItem(Map<String, Number> itemData, boolean gain) {
        if (Math.random() < readLoot(itemData, "prob", 1).doubleValue()) {
            final String itemId = Item.itemStringId.get(readLoot(itemData, "item", 0).intValue());
            Item item = getItem(itemId);

            int itemCount = readLoot(itemData, "count", 1).intValue();
            if (!gain) {
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
