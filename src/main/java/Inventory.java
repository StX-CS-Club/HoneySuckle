
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

    public int ideaFrames = 0;
    public String ideaColor = "#ffff00";

    public boolean isOpen = false;
    private int page = 1;
    private final String[] iconColors = new String[3];

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
            setPage(1);
        }
        if (input.keyPressed(82)) {
            isOpen = !isOpen;
            weaponSelect = -1;
            setPage(0);
        }
        if (isOpen) {
            switch (page) {
                case 0 -> {
                    player.craft.update(player, input);
                }
                case 1 -> {
                    itemScroll = Math.clamp(itemScroll + input.mouseScroll, 0, items.size() - 1);
                }
                case 2 -> {
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
                } else if (page == 2 && weaponHover >= 0 && weaponHover < weapons.size()) {
                    weaponSelect = weaponHover;
                } else if (Math.abs(GAME_HEIGHT * 9 / 10 - input.mousePos[1]) <= 25) {
                    for (int i = 0; i < 3; i++) {
                        if (Math.abs(GAME_WIDTH / 2 - 60 + i * 60 - input.mousePos[0]) <= 25) {
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
            switch (page) {
                case 0 -> {
                    player.craft.renderUi(g, player);
                }
                case 1 -> {
                    for (int i = 0; i < items.size(); i++) {
                        final int offset = 110 * i - ((int) Math.floor(itemScroll * 110));
                        items.get(i).renderUiTile(g, GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50);
                    }

                    // Displays empty slot when items left to be collected
                    if (items.size() < Item.itemNames.size()) {
                        g.drawImage(Rendering.texture("hud/slot", "#ffffff"), GAME_WIDTH / 2 - 50 + 110 * items.size() - ((int) Math.floor(itemScroll * 110)), GAME_HEIGHT / 2 - 50, 100, 100, null);
                    }
                }
                case 2 -> {
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
            }

            g.drawImage(Rendering.texture("hud/craft", iconColors[0]), GAME_WIDTH / 2 - 85, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/items", iconColors[1]), GAME_WIDTH / 2 - 25, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/weapons", iconColors[2]), GAME_WIDTH / 2 + 35, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
        }
    }

    public void incrementItem(Map<String, Number> itemData, boolean gain) {
        if (Math.random() < itemData.getOrDefault("prob", 1).doubleValue()) {
            int id = itemData.getOrDefault("id", 0).intValue();
            int count = itemData.getOrDefault("count", 1).intValue();

            if (!gain) {
                count *= -1;
            }

            switch (itemData.getOrDefault("type", 0).intValue()) {
                case 0 -> {
                    String itemId = Item.itemStringId.get(id);

                    Item item = getItem(itemId);

                    if (item == null) {
                        item = new Item(itemId, count);
                        items.add(item);
                    } else {
                        item.count += count;
                    }

                    if (gain) {
                        final List<String> blueprintUnlocks = Item.itemBlueprintUnlocks.get(itemId);
                        for (String blueprint : blueprintUnlocks) {
                            if (!player.build.blueprints.contains(blueprint)) {
                                player.build.blueprints.add(blueprint);
                                ideaFrames = 80;
                                ideaColor = "#ddff00";
                            }
                        }

                        final List<String> recipeUnlocks = Item.itemRecipeUnlocks.get(itemId);
                        for (String recipe : recipeUnlocks) {
                            if (!player.craft.recipes.contains(recipe)) {
                                player.craft.recipes.add(recipe);
                                ideaFrames = 80;
                                ideaColor = "#ffcc00";
                            }
                        }

                        Item splashItem = getSplashItem(itemId);
                        if (splashItem == null) {
                            splashItem = new Item(itemId, count);
                            splashItems.add(splashItem);
                        } else {
                            splashItem.count += count;
                            splashItem.resetSplashFrame();
                        }
                    }
                }
                case 1 -> {
                    String weaponId = Weapon.weaponStringId.get(id);

                    if (gain) {
                        weapons.add(new Weapon(weaponId));
                    } else {
                        int removed = 0;
                        for (int i = weapons.size() - 1; i > -1; i--) {
                            Weapon weapon = weapons.get(i);
                            if (weapon.weapon.equals(weaponId)) {
                                weapons.remove(weapon);
                                removed++;
                                if (removed == count) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean hasMaterial(Map<String, Number> matData) {
        final int count = matData.getOrDefault("count", 1).intValue();
        final int id = matData.get("id").intValue();
        switch (matData.getOrDefault("type", 0).intValue()) {
            case 0 -> {
                return getItemCount(Item.itemStringId.get(id)) >= count;
            }
            case 1 -> {
                final String weaponId = Weapon.weaponStringId.get(id);
                int inventoryCount = 0;
                for (Weapon weapon : weapons) {
                    if (weapon.weapon.equals(weaponId)) {
                        inventoryCount++;
                    }
                }
                return inventoryCount >= count;
            }
        }
        return false;
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
}
