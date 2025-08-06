
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
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
    public final List<Ammo> ammo = new ArrayList<>();

    private final List<Splash> splashes = new ArrayList<>();

    private final Player player;

    public int ideaFrames = 0;
    public String ideaColor = "#ffff00";

    public boolean isOpen = false;
    private int page = 1;
    private final String[] iconColors = new String[5];

    private double itemScroll = 0;

    private double ammoScroll = 0;
    private int ammoHover = -1;
    public Ammo ammoSelect;

    //Inventory Constructor
    public Inventory(
            Player player,
            List<Weapon> weapons,
            List<Armor> armors,
            List<Item> items,
            List<Ammo> ammo) {
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
            this.ammo.addAll(ammo);
        }

        Arrays.fill(iconColors, "#888888");
    }

    public void update(InputHandler input) {
        if (input.keyPressed(69)) {
            isOpen = !isOpen;
            player.armory.weaponSelect = null;
            ammoSelect = null;
            setPage(1);
        }
        if (input.keyPressed(82)) {
            isOpen = !isOpen;
            player.armory.weaponSelect = null;
            ammoSelect = null;
            setPage(0);
        }
        if (isOpen) {
            if (input.clickPressed(1)) {
                if (Math.abs(GAME_HEIGHT * 9 / 10 - input.mousePos[1]) <= 25) {
                    for (int i = 0; i < 5; i++) {
                        if (Math.abs(GAME_WIDTH / 2 - 120 + i * 60 - input.mousePos[0]) <= 25) {
                            setPage(i);
                            break;
                        }
                    }
                }
            }

            switch (page) {
                case 0 -> {
                    player.craft.update(input);
                }
                case 1 -> {
                    itemScroll = Math.clamp(itemScroll + input.mouseScroll, 0, items.size() - 1);
                }
                case 2 -> {
                    player.armory.updateWeaponMenu(input);
                }
                case 3 -> {
                    if (ammoSelect != null) {
                        if (player.armory.updateAmmoSelect(input)) {
                            ammoSelect = null;
                        }
                    } else {
                        ammoScroll = Math.clamp(ammoScroll + input.mouseScroll, 0, ammo.size() - 1);
                        ammoHover = -1;
                        if (Math.abs(input.mousePos[1] - GAME_HEIGHT / 2) <= 50) {
                            double ammoHighlight = (input.mousePos[0] - (GAME_WIDTH / 2 - 50));
                            if (ammoSelect == null) {
                                ammoHighlight += ammoScroll * 110;
                            }
                            if (ammoHighlight % 110 <= 100) {
                                ammoHover = (int) Math.floor(ammoHighlight / 110);
                            }
                        }
                        if (input.clickPressed(1)) {
                            if (ammoHover > -1 && ammoHover < ammo.size()) {
                                Ammo invAmmo = ammo.get(ammoHover);
                                for (Weapon weapon : player.armory.weapons) {
                                    if (weapon.correctAmmo(invAmmo.types)) {
                                        ammoSelect = invAmmo;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                case 4 -> {
                    player.armory.updateArmorMenu(input);
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

    public void renderSplashes(Graphics2D g, double[] screenPos) {
        for (int i = 0; i < Math.min(splashes.size(), 3); i++) {
            Splash splash = splashes.get(i);

            if (!splash.render(g, (int) screenPos[0], (int) (screenPos[1] - 25 * i - TILE_SIZE * 1.25))) {
                splashes.remove(splash);
            }
        }
    }

    public void renderUi(Graphics2D g) {
        g.setColor(new Color(64, 64, 64, 192));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        if (player.armory.weaponSelect != null) {
            player.armory.renderWeaponSelect(g);
        } else if (ammoSelect != null) {
            player.armory.renderAmmoSelect(g);
        } else {
            // Renders Items
            switch (page) {
                case 0 -> {
                    player.craft.renderUi(g);
                }
                case 1 -> {
                    for (int i = 0; i < items.size(); i++) {
                        final int offset = 110 * i - ((int) Math.floor(itemScroll * 110));
                        items.get(i).renderUiTile(g, GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50);
                    }

                    // Displays empty slot when items left to be collected
                    if (items.size() < Item.itemNames.size()) {
                        g.drawImage(Rendering.texture("hud/slots/item", "#ffffff"), GAME_WIDTH / 2 - 50 + 110 * items.size() - ((int) Math.floor(itemScroll * 110)), GAME_HEIGHT / 2 - 50, 100, 100, null);
                    }
                }
                case 2 -> {
                    player.armory.renderWeaponMenu(g);
                }
                case 3 -> {
                    for (int i = 0; i < ammo.size(); i++) {
                        final int offset = 110 * i - ((int) Math.floor(ammoScroll * 110));
                        if (ammoHover == i) {
                            ammo.get(i).renderUiTile(g, GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50, 1.1);
                        } else {
                            ammo.get(i).renderUiTile(g, GAME_WIDTH / 2 - 50 + offset, GAME_HEIGHT / 2 - 50, 1);
                        }
                    }

                    if (ammoHover > -1 && ammoHover < ammo.size()) {
                        ammo.get(ammoHover).renderScroll(g);
                    }
                }
                case 4 -> {
                    player.armory.renderArmorMenu(g);
                }
            }

            g.drawImage(Rendering.texture("hud/icons/craft", iconColors[0]), GAME_WIDTH / 2 - 145, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/icons/items", iconColors[1]), GAME_WIDTH / 2 - 85, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/icons/weapons", iconColors[2]), GAME_WIDTH / 2 - 25, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/icons/ammo", iconColors[3]), GAME_WIDTH / 2 + 35, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
            g.drawImage(Rendering.texture("hud/icons/armors", iconColors[4]), GAME_WIDTH / 2 + 95, GAME_HEIGHT * 9 / 10 - 25, 50, 50, null);
        }
    }

    public void incrementItem(Map<String, Number> itemData, boolean gain) {
        if (Math.random() < itemData.getOrDefault("prob", 1).doubleValue()) {
            int id = itemData.getOrDefault("id", 0).intValue();
            int count = itemData.getOrDefault("count", 1).intValue();

            String stringId = null;
            switch (itemData.getOrDefault("type", 0).intValue()) {
                case 0 -> {
                    stringId = Item.itemStringId.get(id);

                    Item item = getItem(stringId);

                    if (!gain) {
                        count *= -1;
                    } else { 
                        unlockRecipes(Item.itemBlueprintUnlocks.get(stringId), Item.itemRecipeUnlocks.get(stringId));
                    }

                    if (item == null) {
                        item = new Item(stringId, count);
                        items.add(item);
                        iconColors[1] = "#00ffaa";
                    } else {
                        item.count += count;
                    }
                }
                case 1 -> {
                    stringId = Weapon.weaponStringId.get(id);

                    if (gain) {
                        for (int i = 0; i < count; i++) {
                            weapons.add(new Weapon(stringId));
                            weapons.getLast().setAmmo(ammo);
                            iconColors[2] = "#00ffaa";
                        }
                        unlockRecipes(Weapon.weaponBlueprintUnlocks.get(stringId), Weapon.weaponRecipeUnlocks.get(stringId));
                    } else {
                        int removed = 0;
                        for (int i = weapons.size() - 1; i > -1; i--) {
                            Weapon weapon = weapons.get(i);
                            if (weapon.type.equals(stringId)) {
                                weapons.remove(weapon);
                                for (int e = 0; e < player.armory.weapons.length; e++) {
                                    if (player.armory.weapons[e] == weapon) {
                                        player.armory.weapons[e] = null;
                                    }
                                }
                                removed++;
                                if (removed == count) {
                                    break;
                                }
                            }
                        }
                    }
                }
                case 2 -> {
                    stringId = Armor.armorStringId.get(id);

                    if (gain) {
                        for (int i = 0; i < count; i++) {
                            armors.add(new Armor(stringId));
                            iconColors[4] = "#00ffaa";
                        }
                        unlockRecipes(Armor.armorBlueprintUnlocks.get(stringId), Armor.armorRecipeUnlocks.get(stringId));
                    } else {
                        int removed = 0;
                        for (int i = armors.size() - 1; i > -1; i--) {
                            Armor armor = armors.get(i);
                            if (armor.type.equals(stringId)) {
                                armors.remove(armor);
                                if (armor == player.armory.armor) {
                                    player.armory.armor = null;
                                }
                                removed++;
                                if (removed == count) {
                                    break;
                                }
                            }
                        }
                    }
                }
                case 3 -> {
                    stringId = Ammo.ammoStringId.get(id);

                    Ammo invAmmo = getAmmo(stringId);

                    if (!gain) {
                        count *= -1;
                    } else {
                        unlockRecipes(Ammo.ammoBlueprintUnlocks.get(stringId), Ammo.ammoRecipeUnlocks.get(stringId));
                    }

                    if (invAmmo == null) {
                        invAmmo = new Ammo(stringId, count);
                        ammo.add(invAmmo);
                        iconColors[3] = "#00ffaa";
                    } else {
                        invAmmo.count += count;
                    }
                }
            }

            if (gain) {
                Splash splash = getSplash(stringId);
                if (splash == null) {
                    splash = new Splash(itemData);
                    splashes.add(splash);
                } else {
                    splash.count += count;
                    splash.resetSplash();
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
                    if (weapon.type.equals(weaponId)) {
                        inventoryCount++;
                    }
                }
                return inventoryCount >= count;
            }
            case 2 -> {
                final String armorId = Armor.armorStringId.get(id);
                int inventoryCount = 0;
                for (Armor armor : armors) {
                    if (armor.type.equals(armorId)) {
                        inventoryCount++;
                    }
                }
                return inventoryCount >= count;
            }
            case 3 -> {
                return getAmmoCount(Ammo.ammoStringId.get(id)) >= count;
            }
        }
        return false;
    }

    public void unlockRecipes(List<String> blueprintUnlocks, List<String> recipeUnlocks) {
        for (String blueprint : blueprintUnlocks) {
            if (!player.build.blueprints.contains(blueprint)) {
                player.build.blueprints.add(blueprint);
                ideaFrames = 80;
                ideaColor = "#ddff00";
            }
        }

        for (String recipe : recipeUnlocks) {
            if (!player.craft.recipes.contains(recipe)) {
                player.craft.recipes.add(recipe);
                ideaFrames = 80;
                ideaColor = "#ffcc00";
                iconColors[0] = "#00ffaa";
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

    private Ammo getAmmo(String ammoId) {
        for (Ammo invAmmo : ammo) {
            if (invAmmo.type.equals(ammoId)) {
                return invAmmo;
            }
        }
        return null;
    }

    private Splash getSplash(String itemId) {
        for (Splash splash : splashes) {
            if (splash.id.equals(itemId)) {
                return splash;
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

    public int getAmmoCount(String ammoId) {
        for (Ammo invAmmo : ammo) {
            if (invAmmo.type.equals(ammoId)) {
                return invAmmo.count;
            }
        }
        return 0;
    }
}
