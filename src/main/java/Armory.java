
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

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

    private double armorScroll = 0;
    private int armorOffset = 0;
    private int armorHover = -1;

    //Armory Constructor
    public Armory(Weapon[] weapons, Armor armor) {
        //Provides default equipment
        for (int i = 0; i < 3; i++) {
            if (weapons.length > i) {
                this.weapons[i] = weapons[i];
            }
        }
        this.armor = armor;
    }

    //Update Armory
    public void updateControls(InputHandler inputHandler, Player player) {
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
    }

    public void updateWeapons(Player player) {
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
    public void updateArmor(Player player) {
        //If armor exists, update it
        if (armor != null) {
            armor.update(player);
        }
    }

    public void updateArmorMenu(Player player, InputHandler input) {
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

    //If armor exists, returns armor attributes
    public Map<String, Number> getAttributes() {
        if (armor != null) {
            return armor.attributes;
        }
        //Default "Naked" attributes
        return new HashMap<>();
    }

    //Updates selected weapon from scroll wheel
    public void scrollBar(double mouseScroll) {
        if (Math.abs(mouseScroll) >= InputHandler.criticalMouseScroll) {
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
    public void render(Graphics2D g, Player player) {
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
    public void renderArmor(Graphics2D g, Player player) {
        //If armor exists, render it
        if (armor != null) {
            armor.render(g, player);
        }
    }

    public void renderArmorMenu(Graphics2D g, Player player) {
        if (armor != null) {
            Map<String, String> armorTexture = armor.texture;

            String back = armorTexture.get("back_texture");
            if (back != null) {
                g.drawImage(Rendering.texture(back, "#ffffff"), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75 - armorOffset, 150, 150, null);
            }

            g.drawImage(Rendering.texture("player/front", "#ffffff"), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75, 150, 150, null);

            String front = armorTexture.get("front_texture");
            if (front != null) {
                g.drawImage(Rendering.texture(front, "#ffffff"), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75 - armorOffset, 150, 150, null);
            }
        } else {
            g.drawImage(Rendering.texture("player/front", "#ffffff"), (int) GAME_WIDTH / 2 - 75, (int) GAME_HEIGHT / 2 - 75, 150, 150, null);
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

    //Render Armory UI
    public void renderUi(Graphics2D g, Player player) {
        //Size of slot
        double xMargin = 0;

        //If player in bottom left corner, display in right corner
        if (player.screenPos[0] < HUD_SIZE * 3 + TILE_SIZE && player.screenPos[1] > GAME_HEIGHT - HUD_SIZE * 25 / 12 - TILE_SIZE) {
            xMargin = GAME_WIDTH - HUD_SIZE * 3;
        }

        //Go through each slot
        for (int i = 0; i < 3; i++) {
            //Color of "flowers" of slot
            String color = "#000000";
            if (i == weaponIndex) {
                color = "#eeeeff";
            }

            //Render slot
            g.drawImage(Rendering.texture("hud/icon", color), (int) (HUD_SIZE * i + xMargin), GAME_HEIGHT - HUD_SIZE, HUD_SIZE, HUD_SIZE, null);
            if (weapons[i] != null) {
                //Render Weapon Item
                Map<String, String> texture = weapons[i].texture;
                if (texture.get("itemTexture") != null) {
                    g.drawImage(Rendering.texture(texture.get("itemTexture"), "#ffffff"), (int) (HUD_SIZE * i + HUD_SIZE / 8 + xMargin), GAME_HEIGHT - HUD_SIZE * 7 / 8, HUD_SIZE * 3 / 4, HUD_SIZE * 3 / 4, null);
                }
            }
        }
    }
}
