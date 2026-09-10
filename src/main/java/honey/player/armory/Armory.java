package honey.player.armory;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import honey.mechanics.ConfigManager;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.player.inventory.Inventory;
import honey.player.inventory.KeyItem;
import honey.rendering.Rendering;

public class Armory {

    public static ConfigManager config;

    //Basic armory components
    public int weaponIndex = 0;
    public final Weapon[] weapons = new Weapon[3];
    public Armor armor;

    public KeyItem hotKey = null;

    public final List<Effect> effects = new ArrayList<>();

    private double weaponScroll = 0;
    private int weaponHover = -2;
    public Weapon weaponSelect = null;

    //Hover/select state for the gameplay HUD weapon slots (mouse selection, distinct from the open weapon menu's weaponHover)
    private int hudWeaponHover = -1;

    //Hover/press state for the gameplay HUD hotkey tile (mouse selection)
    private boolean hotKeyHovered = false;
    private boolean hotKeyPressed = false;

    private int trashHoverIndex = -1;
    private int trashFrames = 0;

    private double armorScroll = 0;
    private int armorAnim = 0;
    private int armorHover = -1;

    private final Player player;

    //Armory Constructor
    public Armory(Player player, Weapon[] weapons, Armor armor) {
        this.player = player;
        //Provides default equipment
        for (int i = 0; i < 3; i++) {
            if (weapons.length > i) {
                this.weapons[i] = weapons[i];
            }
        }
        this.armor = armor;
    }

    //Update Armory
    public void updateControls(InputHandler inputHandler) {
        //Mouse hover/click over the HUD weapon slots; consumes the click so it doesn't also fire the old weapon
        updateWeaponHud(inputHandler);
        //Mouse hover/click over the HUD hotkey tile
        updateHotKeyHud(inputHandler);

        //If left click and selected weapon exists, attack (unless that click just selected a HUD weapon slot)
        if (hudWeaponHover == -1 && weapons[weaponIndex] != null) {
            weapons[weaponIndex].updateControls(inputHandler, player);
        }
        //Selects weapon from number key
        for (int i = 0; i < 3; i++) {
            if (inputHandler.keyDown(KeyEvent.VK_1 + i)) {
                weaponIndex = i;
                break;
            }
        }

        if (hotKey != null) {
            if (inputHandler.clickPressed(InputHandler.BUTTON4) || inputHandler.keyPressed(KeyEvent.VK_Q) || inputHandler.keyPressed(KeyEvent.VK_ALT)) {
                hotKey.use(player);
            }
        }
    }

    //Hit-tests the mouse against the gameplay HUD weapon slots (same idea as the open weapon menu's hover/click
    //select, but against the fixed HUD row instead of the scrollable inventory list) and switches the active
    //weapon on click
    private void updateWeaponHud(InputHandler input) {
        hudWeaponHover = -1;

        final int weaponX = weaponHudMoveBottom() ? config.gameWidth - config.hudSize * 3 : 0;
        final int slotY = config.gameHeight - config.hudSize;

        if (input.mousePos[1] >= slotY && input.mousePos[1] < slotY + config.hudSize) {
            final double highlight = input.mousePos[0] - weaponX;
            if (highlight >= 0 && highlight < config.hudSize * 3) {
                hudWeaponHover = (int) (highlight / config.hudSize);
            }
        }

        if (hudWeaponHover != -1 && input.clickPressed(MouseEvent.BUTTON1)) {
            weaponIndex = hudWeaponHover;
        }
    }

    private boolean weaponHudMoveBottom() {
        return player.screenPos[0] < config.hudSize * 3 + config.tileSize && player.screenPos[1] > config.gameHeight - config.hudSize * 25 / 12 - config.tileSize;
    }

    //Hit-tests the mouse against the HUD hotkey tile (same idea as updateWeaponHud) and activates it on click
    private void updateHotKeyHud(InputHandler input) {
        hotKeyHovered = false;
        hotKeyPressed = false;
        if (hotKey == null) {
            return;
        }

        final int hotX = hotKeyMoveTop() ? config.hudSize / 8 : config.gameWidth - config.hudSize * 9 / 8;
        final int hotY = config.hudSize / 8;

        if (input.mousePos[0] >= hotX && input.mousePos[0] < hotX + config.hudSize
                && input.mousePos[1] >= hotY && input.mousePos[1] < hotY + config.hudSize) {
            hotKeyHovered = true;
            hotKeyPressed = input.clickDown(MouseEvent.BUTTON1);
            if (input.clickPressed(MouseEvent.BUTTON1)) {
                hotKey.use(player);
            }
        }
    }

    //True when the player is in the top-right corner, in which case the hotkey tile (and effect icons) move to the
    //top-left - shared by renderUi() and updateHotKeyHud() so the clickable area always matches what's drawn
    private boolean hotKeyMoveTop() {
        return player.screenPos[0] > config.gameWidth * 3 / 4 && player.screenPos[1] < config.hudSize * 9 / 8 + config.tileSize;
    }

    public void updateWeapons() {
        for (int i = 0; i < weapons.length; i++) {
            if (weapons[i] != null) {
                weapons[i].passiveUpdate();
                if (i == weaponIndex) {
                    weapons[i].update(player);
                }
            }
        }
    }

    //Update Armory Armor
    public void updateArmor() {
        //If armor exists, update it
        if (armor != null) {
            armor.update(player);
        }
    }

    public void updateEffects() {
        for (int i = effects.size() - 1; i > -1; i--) {
            final Effect effect = effects.get(i);

            effect.update();
            if (!effect.active) {
                effects.remove(effect);
            }
        }
    }

    public void updateArmorMenu(InputHandler input) {
        armorScroll = Math.clamp(armorScroll + input.mouseScroll, 0, player.inventory.armors.size() - 1);
        armorHover = -1;
        if (Math.abs(input.mousePos[0] - config.gameWidth + 100) <= 60) {
            final double armorHighlight = (input.mousePos[1] - (config.gameHeight / 2 - 60)) + armorScroll * 130;
            if (armorHighlight % 130 <= 120) {
                armorHover = (int) Math.floor(armorHighlight / 130);
            }
        }
        if (input.clickPressed(MouseEvent.BUTTON1)) {
            if (armorHover > -1 && armorHover < player.inventory.armors.size()) {
                final Armor invArmor = player.inventory.armors.get(armorHover);
                if (invArmor == armor) {
                    armor = null;
                } else {
                    armor = invArmor;
                    armorAnim = 0;
                }
            }
        }
    }

    public void updateWeaponMenu(InputHandler input) {
        weaponScroll = Math.clamp(weaponScroll + input.mouseScroll, 0, Math.max(0, player.inventory.weapons.size() - 1));
        weaponHover = -2;
        if (Math.abs(input.mousePos[1] - config.gameHeight / 2) <= 50) {
            double weaponHighlight = (input.mousePos[0] - (config.gameWidth / 2 - 50));
            if (weaponSelect == null) {
                weaponHighlight += weaponScroll * 110;
            }
            if (weaponHighlight % 110 <= 100) {
                weaponHover = (int) Math.floor(weaponHighlight / 110);
            }
        }

        //Hold right-click on an owned weapon tile to delete it, with a rising safety-timer overlay (see renderWeaponMenu)
        if (weaponSelect == null && weaponHover > -1 && weaponHover < player.inventory.weapons.size() && input.clickDown(MouseEvent.BUTTON3)) {
            if (trashHoverIndex != weaponHover) {
                trashHoverIndex = weaponHover;
                trashFrames = 0;
            }
            trashFrames++;
            final Weapon held = player.inventory.weapons.get(trashHoverIndex);
            final int trashTotal = (int) Math.round(held.attributes.getOrDefault("trashTimer", 1).doubleValue() * config.fps);
            if (trashFrames >= trashTotal) {
                deleteWeapon(held);
                trashHoverIndex = -1;
                trashFrames = 0;
            }
        } else {
            trashHoverIndex = -1;
            trashFrames = 0;
        }

        if (input.clickPressed(MouseEvent.BUTTON1)) {
            if (weaponSelect != null) {
                if (Math.abs(weaponHover) <= 1) {
                    for (int i = 0; i < 3; i++) {
                        if (weaponSelect == weapons[i]) {
                            weapons[i] = weapons[weaponHover + 1];
                            break;
                        }
                    }
                    weapons[weaponHover + 1] = weaponSelect;
                    weaponSelect = null;
                }
            } else if (weaponHover >= 0 && weaponHover < player.inventory.weapons.size()) {
                weaponSelect = player.inventory.weapons.get(weaponHover);
            }
        }
    }

    //Removes a weapon from the owned-weapons list and clears it from any equipped slot that references it
    private void deleteWeapon(Weapon weapon) {
        player.inventory.weapons.remove(weapon);
        for (int i = 0; i < weapons.length; i++) {
            if (weapons[i] == weapon) {
                weapons[i] = null;
            }
        }
        if (weaponSelect == weapon) {
            weaponSelect = null;
        }
    }

    public boolean updateAmmoSelect(InputHandler input) {
        weaponHover = -2;
        if (Math.abs(input.mousePos[1] - config.gameHeight / 2) <= 50) {
            double weaponHighlight = (input.mousePos[0] - (config.gameWidth / 2 - 50));
            if (weaponHighlight % 110 <= 100) {
                weaponHover = (int) Math.floor(weaponHighlight / 110);
            }
        }

        if (weaponHover > -2 && weaponHover < 2) {
            if (!weapons[weaponHover + 1].correctAmmo(player.inventory.ammoSelect.types)) {
                weaponHover = -2;
            } else if (input.clickPressed(MouseEvent.BUTTON1)) {
                weapons[weaponHover + 1].ammo = player.inventory.ammoSelect;
                player.inventory.ammoSelect = null;
            }
        }

        return false;
    }

    //If armor exists, returns armor attributes
    public Map<String, Number> getAttributes() {
        final Map<String, Number> result = new HashMap<>();
        if (armor != null) {
            result.putAll(armor.attributes);
        }

        for (Effect effect : effects) {
            effect.modify(result);
        }

        return result;
    }

    //Updates selected weapon from scroll wheel
    public void scrollBar(double mouseScroll) {
        if (Math.abs(mouseScroll) >= config.criticalMouseScroll) {
            weaponIndex += Math.signum(mouseScroll);
            if (weaponIndex < 0) {
                weaponIndex = 2;
            }
            if (weaponIndex >= 3) {
                weaponIndex = 0;
            }
        }
    }

    //Render Armory
    public void render(Graphics2D g) {
        //Original rotation
        final AffineTransform originalTransform = g.getTransform();

        //Rotate to match player rotation
        g.rotate(Math.toRadians(player.rotation), player.screenPos[0], player.screenPos[1]);

        //If weapon exists, render it
        if (weapons[weaponIndex] != null) {
            weapons[weaponIndex].render(g, player);
        }

        //Resets rotation
        g.setTransform(originalTransform);
    }

    //Render Armory Armor
    public void renderArmor(Graphics2D g) {
        //If armor exists, render it
        if (armor != null) {
            armor.render(g, player);
        }
    }

    public void renderArmorMenu(Graphics2D g) {
        if (armor != null) {
            final Map<String, String> armorTexture = armor.texture;
            final int armorOffset = (int) (200 * Math.pow(0.75, armorAnim));

            final String back = armorTexture.get("backTexture");
            if (back != null) {
                g.drawImage(Rendering.texture(back, null), (int) config.gameWidth / 2 - 75, (int) config.gameHeight / 2 - 75 - armorOffset, 150, 150, null);
            }

            g.drawImage(Rendering.texture("player/front", null), (int) config.gameWidth / 2 - 75, (int) config.gameHeight / 2 - 75, 150, 150, null);
            final String playerTexture = armorTexture.get("playerTexture");
            if (playerTexture != null) {
                final float opacity = (float) Math.min(1, armorAnim / 10.0);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g.drawImage(Rendering.texture(playerTexture, null), (int) config.gameWidth / 2 - 75, (int) config.gameHeight / 2 - 75, 150, 150, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
            }

            final String front = armorTexture.get("frontTexture");
            if (front != null) {
                g.drawImage(Rendering.texture(front, null), (int) config.gameWidth / 2 - 75, (int) config.gameHeight / 2 - 75 - armorOffset, 150, 150, null);
            }
        } else {
            g.drawImage(Rendering.texture("player/front", null), (int) config.gameWidth / 2 - 75, (int) config.gameHeight / 2 - 75, 150, 150, null);
        }

        armorAnim++;

        for (int i = 0; i < player.inventory.armors.size(); i++) {
            final int offset = 130 * i - ((int) Math.floor(armorScroll * 130));

            if (i == armorHover) {
                Armor invArmor = player.inventory.armors.get(i);
                invArmor.renderUiTile(g, config.gameWidth - 170, (int) (config.gameHeight / 2 - 60 + offset), 1.1, invArmor == armor);
            } else {
                Armor invArmor = player.inventory.armors.get(i);
                invArmor.renderUiTile(g, config.gameWidth - 170, (int) (config.gameHeight / 2 - 60 + offset), 1, invArmor == armor);
            }
        }

        Armor scrollArmor = armor;
        if (armorHover > -1 && armorHover < player.inventory.armors.size()) {
            scrollArmor = player.inventory.armors.get(armorHover);
        }

        if (scrollArmor != null) {
            scrollArmor.renderScroll(g);
        }
    }

    public void renderWeaponMenu(Graphics2D g) {
        // Renders Weapons
        for (int i = 0; i < player.inventory.weapons.size(); i++) {
            final int offset = 110 * i - ((int) Math.floor(weaponScroll * 110));
            final int tileX = config.gameWidth / 2 - 50 + offset;
            final int tileY = config.gameHeight / 2 - 50;

            if (i == weaponHover) {
                player.inventory.weapons.get(i).renderUiTile(g, tileX, tileY, 1.1, weapons);
            } else {
                player.inventory.weapons.get(i).renderUiTile(g, tileX, tileY, 1, weapons);
            }

            //Rising red safety-timer overlay while this tile is being held for deletion
            if (i == trashHoverIndex && trashFrames > 0) {
                final Weapon held = player.inventory.weapons.get(i);
                final int trashTotal = (int) Math.round(held.attributes.getOrDefault("trashTimer", 1).doubleValue() * config.fps);
                final int filledHeight = (int) Math.min(100, 100.0 * trashFrames / trashTotal);
                g.setColor(new Color(255, 0, 0, 128));
                g.fillRect(tileX, tileY + 100 - filledHeight, 100, filledHeight);
            }
        }

        if (weaponHover > -1 && weaponHover < player.inventory.weapons.size()) {
            player.inventory.weapons.get(weaponHover).renderScroll(g);
        }
    }

    public void renderWeaponSelect(Graphics2D g) {
        weaponSelect.renderUiTile(g, config.gameWidth / 2 - 50, config.gameHeight / 2 + 75, 1, weapons);

        renderWeaponList(g);

        Weapon scrollWeapon = weaponSelect;
        if (weaponHover > -2 && weaponHover < 2) {
            if (weapons[weaponHover + 1] != null) {
                scrollWeapon = weapons[weaponHover + 1];
            }
        }

        scrollWeapon.renderScroll(g);
    }

    public void renderAmmoSelect(Graphics2D g) {
        player.inventory.ammoSelect.renderUiTile(g, config.gameWidth / 2 - 50, config.gameHeight / 2 + 75, 1);

        renderWeaponList(g);

        player.inventory.ammoSelect.renderScroll(g);

        for (int i = 0; i < 3; i++) {
            final Weapon iWeapon = weapons[i];

            if (iWeapon != null) {
                if (iWeapon.ammo != null) {
                    final String ammoTexture = iWeapon.ammo.texture.get("texture");
                    if (ammoTexture != null) {
                        g.drawImage(Rendering.texture("ui/slots/ammo", iWeapon.ammo.texture.get("rarityColor")), config.gameWidth / 2 - 126 + i * 110, config.gameHeight / 2 - 66, 32, 32, null);
                        g.drawImage(Rendering.texture(ammoTexture, null), config.gameWidth / 2 - 122 + i * 110, config.gameHeight / 2 - 62, 24, 24, null);
                    }
                }
            }
        }

        if (weaponHover > -2 && weaponHover < 2) {
            final Weapon weapon = weapons[weaponHover + 1];
            if (weapon != null) {
                if (weapon.ammo != null) {
                    weapon.ammo.renderScroll(g);
                }
            }
        }
    }

    private void renderWeaponList(Graphics2D g) {
        for (int i = 0; i < 3; i++) {
            final Weapon iWeapon = weapons[i];
            final double factor = weaponHover + 1 == i ? 1.1 : 1;

            if (iWeapon != null) {
                iWeapon.renderUiTile(g, config.gameWidth / 2 - 160 + i * 110, config.gameHeight / 2 - 50, factor, weapons);
            } else {
                Rendering.scale(Rendering.texture("ui/slots/weapon", null), g, config.gameWidth / 2 - 160 + i * 110, config.gameHeight / 2 - 50, 100, 100, factor);
                g.setColor(Color.WHITE);
                g.setFont(new Font("VT323 Regular", Font.PLAIN, 28));
                Rendering.centeredText(g, Integer.toString(i + 1), (int) (config.gameWidth / 2 + 45 + (i - 1) * 110), (int) (config.gameHeight / 2 + 50));
            }
        }
    }

    //Render Armory UI
    public void renderUi(Graphics2D g, boolean dense) {
        final boolean moveTop = hotKeyMoveTop();

        //If player in bottom left corner, display in right corner
        final int weaponX = weaponHudMoveBottom() ? config.gameWidth - config.hudSize * 3 : 0;
        final int slotY = config.gameHeight - config.hudSize;

        //Go through each slot
        for (int i = 0; i < 3; i++) {
            //Color of "flowers" of slot - equipped weapon lights up, a hovered (clickable) slot gets a softer highlight
            String color = "#000000";
            if (i == weaponIndex) {
                color = "#eeeeff";
            } else if (i == hudWeaponHover) {
                color = "#aaaaaa";
            }

            final int slotX = config.hudSize * i + weaponX;

            //Render slot
            g.drawImage(Rendering.texture("ui/hud/slot", color), slotX, slotY, config.hudSize, config.hudSize, null);
            final Weapon weapon = weapons[i];
            if (weapon != null) {
                //Render Weapon Item
                final Map<String, String> texture = weapon.texture;
                if (texture.get("itemTexture") != null) {
                    g.drawImage(Rendering.texture(texture.get("itemTexture"), null), slotX + config.hudSize / 8, config.gameHeight - config.hudSize * 7 / 8, config.hudSize * 3 / 4, config.hudSize * 3 / 4, null);
                }

                if (weapon.ammo != null || weapon.tags.contains("stackable")) {
                    final int count = weapon.ammo != null ? weapon.ammo.count : weapon.count;
                    Rendering.renderCounter(g, slotX, slotY, count, false);
                }
            }
        }

        int effectX = config.hudSize / 8;

        if (hotKey != null) {
            final double hotFactor = hotKeyPressed ? 0.8 : hotKeyHovered ? 1.1 : 1.0;
            if (moveTop) {
                hotKey.renderHotTile(g, config.hudSize / 8, config.hudSize / 8, hotFactor);
            } else {
                hotKey.renderHotTile(g, config.gameWidth - config.hudSize * 9 / 8, config.hudSize / 8, hotFactor);
            }
            effectX = config.hudSize * 5 / 4;
        }

        int offsetX = 0;
        int offsetY = config.hudSize / 8;
        for (Effect effect : effects) {
            if (effect.hasRender()) {
                if (moveTop) {
                    effect.renderUi(g, offsetX + effectX, offsetY);
                } else {
                    effect.renderUi(g, config.gameWidth - config.hudSize / 2 - offsetX - effectX, offsetY);
                }

                offsetX += config.hudSize * 5 / 8;

                if (dense && offsetX + effectX + config.hudSize / 2 > config.gameWidth / 4) {
                    offsetX = 0;
                    offsetY += config.hudSize * 5 / 8;
                }
            }
        }
    }

    public Map<String, Object> toJson() {
        //LinkedHashMap, not Map.of: empty armor/hotKey slots are null, and Map.of rejects null values
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("weapons", Arrays.stream(weapons).map(weapon -> weapon != null ? weapon.type : null).toList());
        json.put("armor", armor != null ? armor.type : null);
        json.put("hotKey", hotKey != null ? hotKey.id : null);
        json.put("effects", effects.stream().map(Effect::toJson).toList());
        return json;
    }

    //Equipped weapons/armor are looked up by type from the already-reconstructed inventory rather than built fresh,
    //preserving the same shared-instance relationship the normal Player constructor sets up
    @SuppressWarnings("unchecked")
    public static Armory fromJson(Player player, Map<String, Object> json, Inventory inventory) {
        final List<String> weaponTypes = (List<String>) json.get("weapons");
        final Weapon[] weapons = new Weapon[3];
        for (int i = 0; i < weaponTypes.size(); i++) {
            final String type = weaponTypes.get(i);
            if (type != null) {
                weapons[i] = findByType(inventory.weapons, w -> w.type, type);
            }
        }

        final String armorType = (String) json.get("armor");
        final Armor armor = armorType != null ? findByType(inventory.armors, a -> a.type, armorType) : null;

        final Armory armory = new Armory(player, weapons, armor);

        final String hotKeyId = (String) json.get("hotKey");
        if (hotKeyId != null) {
            armory.hotKey = findByType(inventory.keyItems, k -> k.id, hotKeyId);
        }

        for (Map<String, Object> effectJson : (List<Map<String, Object>>) json.get("effects")) {
            armory.effects.add(new Effect(effectJson));
        }

        return armory;
    }

    private static <T> T findByType(List<T> pool, Function<T, String> typeOf, String type) {
        for (T candidate : pool) {
            if (typeOf.apply(candidate).equals(type)) {
                return candidate;
            }
        }
        return null;
    }
}
