package honey.player.armory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.player.inventory.KeyItem;
import honey.rendering.Rendering;

/*
 * Armory.java *
 - Class for managing player's combat components (Armor, Weapons, etc.)
 */
public class Armory {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;
    private static final int HUD_SIZE = HoneySuckle.HUD_SIZE;

    //Basic armory components
    public int weaponIndex = 0;
    public Weapon[] weapons = new Weapon[3];
    public Armor armor;

    public KeyItem hotKey = null;

    public List<Effect> effects = new ArrayList<>();

    private double weaponScroll = 0;
    private int weaponHover = -2;
    public Weapon weaponSelect = null;

    private double armorScroll = 0;
    private int armorOffset = 0;
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
        //If left click and selected weapon exists, attack
        if (weapons[weaponIndex] != null) {
            weapons[weaponIndex].updateControls(inputHandler, player);
        }
        //Selects weapon from number key
        for (int i = 0; i < 3; i++) {
            if (inputHandler.keyDown(49 + i)) {
                weaponIndex = i;
                break;
            }
        }

        if (hotKey != null) {
            if (inputHandler.clickPressed(4) || inputHandler.keyPressed(81) || inputHandler.keyPressed(18)) {
                hotKey.use(player);
            }
        }
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
            Effect effect = effects.get(i);

            effect.update();
            if (!effect.active) {
                effects.remove(effect);
            }
        }
    }

    public void updateArmorMenu(InputHandler input) {
        armorScroll = Math.clamp(armorScroll + input.mouseScroll, 0, player.inventory.armors.size() - 1);
        armorHover = -1;
        if (Math.abs(input.mousePos[0] - GAME_WIDTH + 100) <= 60) {
            double armorHighlight = (input.mousePos[1] - (GAME_HEIGHT / 2 - 60)) + armorScroll * 130;
            if (armorHighlight % 130 <= 120) {
                armorHover = (int) Math.floor(armorHighlight / 130);
            }
        }
        if (input.clickPressed(1)) {
            if (armorHover > -1 && armorHover < player.inventory.armors.size()) {
                Armor invArmor = player.inventory.armors.get(armorHover);
                if (invArmor == armor) {
                    armor = null;
                } else {
                    armor = invArmor;
                    armorOffset = 200;
                }
            }
        }
    }

    public void updateWeaponMenu(InputHandler input) {
        weaponScroll = Math.clamp(weaponScroll + input.mouseScroll, 0, player.inventory.weapons.size() - 1);
        weaponHover = -2;
        if (Math.abs(input.mousePos[1] - GAME_HEIGHT / 2) <= 50) {
            double weaponHighlight = (input.mousePos[0] - (GAME_WIDTH / 2 - 50));
            if (weaponSelect == null) {
                weaponHighlight += weaponScroll * 110;
            }
            if (weaponHighlight % 110 <= 100) {
                weaponHover = (int) Math.floor(weaponHighlight / 110);
            }
        }
        if (input.clickPressed(1)) {
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

    public boolean updateAmmoSelect(InputHandler input) {
        weaponHover = -2;
        if (Math.abs(input.mousePos[1] - GAME_HEIGHT / 2) <= 50) {
            double weaponHighlight = (input.mousePos[0] - (GAME_WIDTH / 2 - 50));
            if (weaponHighlight % 110 <= 100) {
                weaponHover = (int) Math.floor(weaponHighlight / 110 + weaponScroll * 110);
            }
        }

        if (weaponHover > -2 && weaponHover < 2) {
            if (!weapons[weaponHover + 1].correctAmmo(player.inventory.ammoSelect.types)) {
                weaponHover = -2;
            } else if (input.clickPressed(1)) {
                weapons[weaponHover + 1].ammo = player.inventory.ammoSelect;
                player.inventory.ammoSelect = null;
            }
        }

        return false;
    }

    //If armor exists, returns armor attributes
    public Map<String, Number> getAttributes() {
        Map<String, Number> result = new HashMap<>();
        if (armor != null) {
            result.putAll(armor.attributes);
        }

        for (Effect effect : effects) {
            effect.modify(result);
        }

        //Default "Naked" attributes
        return result;
    }

    //Updates selected weapon from scroll wheel
    public void scrollBar(double mouseScroll) {
        if (Math.abs(mouseScroll) >= InputHandler.CRITICAL_MOUSE_SCROLL) {
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
        AffineTransform originalTransform = g.getTransform();

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
            Map<String, String> armorTexture = armor.texture;

            String back = armorTexture.get("backTexture");
            if (back != null) {
                g.drawImage(Rendering.texture(back, null), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75 - armorOffset, 150, 150, null);
            }

            g.drawImage(Rendering.texture("player/front", null), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75, 150, 150, null);

            String front = armorTexture.get("frontTexture");
            if (front != null) {
                g.drawImage(Rendering.texture(front, null), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75 - armorOffset, 150, 150, null);
            }
        } else {
            g.drawImage(Rendering.texture("player/front", null), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75, 150, 150, null);
        }

        armorOffset *= 0.75;

        for (int i = 0; i < player.inventory.armors.size(); i++) {
            final int offset = 130 * i - ((int) Math.floor(armorScroll * 130));

            if (i == armorHover) {
                Armor invArmor = player.inventory.armors.get(i);
                invArmor.renderUiTile(g, GAME_WIDTH - 170, (int) (GAME_HEIGHT / 2 - 60 + offset), 1.1, invArmor == armor);
            } else {
                Armor invArmor = player.inventory.armors.get(i);
                invArmor.renderUiTile(g, GAME_WIDTH - 170, (int) (GAME_HEIGHT / 2 - 60 + offset), 1, invArmor == armor);
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

            if (i == weaponHover) {
                player.inventory.weapons.get(i).renderUiTile(g, (int) (GAME_WIDTH / 2 - 50 + offset), (int) (GAME_HEIGHT / 2 - 50), 1.1, weapons);
            } else {
                player.inventory.weapons.get(i).renderUiTile(g, (int) (GAME_WIDTH / 2 - 50 + offset), (int) (GAME_HEIGHT / 2 - 50), 1, weapons);
            }
        }

        if (weaponHover > -1 && weaponHover < player.inventory.weapons.size()) {
            player.inventory.weapons.get(weaponHover).renderScroll(g);
        }
    }

    public void renderWeaponSelect(Graphics2D g) {
        weaponSelect.renderUiTile(g, GAME_WIDTH / 2 - 50, GAME_HEIGHT / 2 + 75, 1, weapons);

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
        player.inventory.ammoSelect.renderUiTile(g, GAME_WIDTH / 2 - 50, GAME_HEIGHT / 2 + 75, 1);

        renderWeaponList(g);

        player.inventory.ammoSelect.renderScroll(g);

        for (int i = 0; i < 3; i++) {
            Weapon iWeapon = weapons[i];

            if (iWeapon != null) {
                if (iWeapon.ammo != null) {
                    String ammoTexture = iWeapon.ammo.texture.get("texture");
                    if (ammoTexture != null) {
                        g.drawImage(Rendering.texture("ui/slots/ammo", iWeapon.ammo.texture.get("rarityColor")), GAME_WIDTH / 2 - 126 + i * 110, GAME_HEIGHT / 2 - 66, 32, 32, null);
                        g.drawImage(Rendering.texture(ammoTexture, null), GAME_WIDTH / 2 - 122 + i * 110, GAME_HEIGHT / 2 - 62, 24, 24, null);
                    }
                }
            }
        }

        if (weaponHover > -2 && weaponHover < 2) {
            Weapon weapon = weapons[weaponHover + 1];
            if (weapon != null) {
                if (weapon.ammo != null) {
                    weapon.ammo.renderScroll(g);
                }
            }
        }
    }

    private void renderWeaponList(Graphics2D g) {
        for (int i = 0; i < 3; i++) {
            Weapon iWeapon = weapons[i];

            double factor = 1;
            if (weaponHover + 1 == i) {
                factor = 1.1;
            }

            if (iWeapon != null) {
                iWeapon.renderUiTile(g, GAME_WIDTH / 2 - 160 + i * 110, GAME_HEIGHT / 2 - 50, factor, weapons);
            } else {
                g.drawImage(Rendering.texture("ui/slots/weapon", null), (int) (GAME_WIDTH / 2 - 50 * factor + (i - 1) * 110), (int) (GAME_HEIGHT / 2 - 50 * factor), (int) (100 * factor), (int) (100 * factor), null);
                g.setColor(Color.WHITE);
                g.setFont(new Font("VT323 Regular", Font.PLAIN, 28));
                Rendering.centeredText(g, Integer.toString(i + 1), (int) (GAME_WIDTH / 2 + 45 + (i - 1) * 110), (int) (GAME_HEIGHT / 2 + 50));
            }
        }
    }

    //Render Armory UI
    public void renderUi(Graphics2D g, boolean dense) {
        boolean moveBottom = player.screenPos[0] < HUD_SIZE * 3 + TILE_SIZE && player.screenPos[1] > GAME_HEIGHT - HUD_SIZE * 25 / 12 - TILE_SIZE;
        boolean moveTop = player.screenPos[0] > GAME_WIDTH * 3 / 4 && player.screenPos[1] < HUD_SIZE * 9 / 8 + TILE_SIZE;

        int weaponX = 0;

        //If player in bottom left corner, display in right corner
        if (moveBottom) {
            weaponX = GAME_WIDTH - HUD_SIZE * 3;
        }

        //Go through each slot
        for (int i = 0; i < 3; i++) {
            //Color of "flowers" of slot
            String color = "#000000";
            if (i == weaponIndex) {
                color = "#eeeeff";
            }

            //Render slot
            g.drawImage(Rendering.texture("ui/hud/slot", color), HUD_SIZE * i + weaponX, GAME_HEIGHT - HUD_SIZE, HUD_SIZE, HUD_SIZE, null);
            if (weapons[i] != null) {
                //Render Weapon Item
                Map<String, String> texture = weapons[i].texture;
                if (texture.get("itemTexture") != null) {
                    g.drawImage(Rendering.texture(texture.get("itemTexture"), null), HUD_SIZE * i + HUD_SIZE / 8 + weaponX, GAME_HEIGHT - HUD_SIZE * 7 / 8, HUD_SIZE * 3 / 4, HUD_SIZE * 3 / 4, null);
                }

                if (weapons[i].ammo != null) {
                    g.drawImage(Rendering.texture("ui/hud/counter", null), HUD_SIZE * i + weaponX, GAME_HEIGHT - HUD_SIZE * 5 / 16, HUD_SIZE / 2, HUD_SIZE / 4, null);
                    if (weapons[i].ammo.count > 0) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.RED);
                    }
                    Rendering.centeredText(g, Integer.toString(weapons[i].ammo.count), HUD_SIZE * i + weaponX + HUD_SIZE / 4, GAME_HEIGHT - HUD_SIZE * 1 / 8, HUD_SIZE / 2, 20);
                }
            }
        }

        int effectX = HUD_SIZE / 8;

        if (hotKey != null) {
            if (moveTop) {
                hotKey.renderHotTile(g, HUD_SIZE / 8, HUD_SIZE / 8, 1.0);
            } else {
                hotKey.renderHotTile(g, GAME_WIDTH - HUD_SIZE * 9 / 8, HUD_SIZE / 8, 1.0);
            }
            effectX = HUD_SIZE * 5 / 4;
        }

        int offsetX = 0;
        int offsetY = HUD_SIZE / 8;
        for (Effect effect : effects) {
            if (effect.hasRender()) {
                if (moveTop) {
                    effect.renderUi(g, offsetX + effectX, offsetY);
                } else {
                    effect.renderUi(g, GAME_WIDTH - HUD_SIZE / 2 - offsetX - effectX, offsetY);
                }

                offsetX += HUD_SIZE * 5 / 8;

                if (dense && offsetX + effectX + HUD_SIZE / 2 > GAME_WIDTH / 4) {
                    offsetX = 0;
                    offsetY += HUD_SIZE * 5 / 8;
                }
            }
        }
    }
}
