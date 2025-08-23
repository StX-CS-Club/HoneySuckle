package honey.player.inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Rendering;

/*
 * Weapon.java *
 - Class for managing weapons
 - Contains static json data
 */
public class Weapon {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;

    //Static json data
    public static final Map<String, Map<String, Number>> weaponAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponStats = new HashMap<>();
    public static final Map<String, List<String>> weaponAmmo = new HashMap<>();
    public static final Map<String, Map<String, Map<String, Object>>> weaponBehaviors = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponTextures = new HashMap<>();
    public static final Map<String, List<String>> weaponTags = new HashMap<>();
    public static final Map<String, List<String>> weaponRecipeUnlocks = new HashMap<>();
    public static final Map<String, List<String>> weaponBlueprintUnlocks = new HashMap<>();
    public static final Map<String, String> weaponNames = new HashMap<>();
    public static final Map<String, Integer> weaponIntId = new HashMap<>();
    public static final Map<Integer, String> weaponStringId = new HashMap<>();

    //Weapon properties
    public final Map<String, Number> attributes;
    public final Map<String, String> texture;
    public final Map<String, String> stats;
    public final String type;
    private final String name;
    public final List<String> tags;

    public Ammo ammo = null;
    public List<String> ammoTypes;

    private final Attack attack;

    //Weapon constructor
    public Weapon(String type) {
        //Interprets weapon id
        attributes = weaponAttributes.get(type);
        stats = weaponStats.get(type);
        texture = weaponTextures.get(type);
        tags = weaponTags.get(type);
        this.type = type;
        name = weaponNames.get(type);

        attack = new Attack(this);

        ammoTypes = weaponAmmo.get(type);
    }

    public boolean correctAmmo(List<String> ammoTypeList) {
        for (String ammoType : ammoTypes) {
            if (ammoTypeList.contains(ammoType)) {
                return true;
            }
        }
        return false;
    }

    public void setAmmo(List<Ammo> ammoList) {
        for (Ammo invAmmo : ammoList) {
            if (correctAmmo(invAmmo.types)) {
                ammo = invAmmo;
                return;
            }
        }
    }

    //Attack with weapon
    public void updateControls(InputHandler input, Player player) {
        //Dependant on weapon type
        attack.updateControls(input, player);
    }

    public void passiveUpdate() {
        //Progress cooldown
        attack.passiveUpdate();
    }

    //Update Weapon
    public void update(Player player) {
        //Current world
        attack.update(player);
    }

    //Render Weapon
    public void render(Graphics2D g, Player player) {
        //Dependant on weapon type
        attack.render(g, player);
    }

    public void renderUiTile(Graphics2D g, int x, int y, double factor, Weapon[] weapons) {
        final String weaponTexture = texture.get("itemTexture");
        final String color = texture.get("rarityColor");

        g.drawImage(Rendering.texture("hud/slots/weapon", color), (int) (x - 50 * (factor - 1)), (int) (y - 50 * (factor - 1)), (int) (100 * factor), (int) (100 * factor), null);

        if (weaponTexture != null) {
            g.drawImage(Rendering.texture(weaponTexture, "#e8f1ff"), x + 0xc, y + 12, 75, 75, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 28));
        for (int w = 0; w < weapons.length; w++) {
            if (weapons[w] == this) {
                Rendering.centeredText(g, Integer.toString(w + 1), x + 95, y + 100);
            }
        }
    }

    public void renderScroll(Graphics2D g) {
        g.drawImage(Rendering.scroll(14), GAME_WIDTH / 2 - 192, 40, 384, 192, null);

        g.setColor(Color.decode(texture.getOrDefault("rarityColor", "#333333")));
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));

        Rendering.centeredText(g, name, GAME_WIDTH / 2, 88);

        g.setColor(Color.BLACK);
        String[] statKeys = stats.keySet().toArray(String[]::new);

        int columns = Math.ceilDiv(statKeys.length, 3);
        int width = 384 / columns;
        int x = GAME_WIDTH / 2 - 192 + 192 / columns;

        for (int i = 0; i < columns; i++) {
            for (int e = 0; e < 3; e++) {
                int index = i * 3 + e;
                if (index < statKeys.length) {
                    Rendering.centeredText(g, statKeys[index] + ": " + stats.get(statKeys[index]), x + width * i, 112 + 32 * e, width - 10, 24);
                }
            }
        }
    }
}
