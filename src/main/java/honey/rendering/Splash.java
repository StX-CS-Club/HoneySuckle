
package honey.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import honey.HoneySuckle;
import honey.player.inventory.Ammo;
import honey.player.inventory.Armor;
import honey.player.inventory.Item;
import honey.player.inventory.KeyItem;
import honey.player.inventory.Weapon;

public class Splash {

    private static final int FPS = HoneySuckle.FPS;

    public final String id;
    private final Map<String, Number> attributes;
    private final Map<String, String> texture;
    private final String name;

    public int count;
    private int frames = 0;
    private final int maxFrames;
    private final int animFrames;

    final Color splashColor;
    private BufferedImage splashTexture;
    private String label;

    private final int[] size = new int[2];

    public Splash(Map<String, Number> itemData) {
        final int intId = itemData.get("id").intValue();
        final int type = itemData.getOrDefault("type", 0).intValue();
        count = itemData.getOrDefault("count", 1).intValue();
        switch (type) {
            case 0 -> {
                final String stringId = Item.itemStringId.get(intId);
                attributes = Item.itemAttributes.get(stringId);
                texture = Item.itemTextures.get(stringId);
                name = Item.itemNames.get(stringId);
                id = type+":"+stringId;
            }
            case 1 -> {
                final String stringId = Weapon.weaponStringId.get(intId);
                attributes = Weapon.weaponAttributes.get(stringId);
                texture = Weapon.weaponTextures.get(stringId);
                name = Weapon.weaponNames.get(stringId);
                id = type+":"+stringId;
            }
            case 2 -> {
                final String stringId = Armor.armorStringId.get(intId);
                attributes = Armor.armorAttributes.get(stringId);
                texture = Armor.armorTextures.get(stringId);
                name = Armor.armorNames.get(stringId);
                id = type+":"+stringId;
            }
            case 3 -> {
                final String stringId = Ammo.ammoStringId.get(intId);
                attributes = Ammo.ammoAttributes.get(stringId);
                texture = Ammo.ammoTextures.get(stringId);
                name = Ammo.ammoNames.get(stringId);
                id = type+":"+stringId;
            }
            case 4 -> {
                final String stringId = KeyItem.keyStringId.get(intId);
                attributes = KeyItem.keyAttributes.get(stringId);
                texture = KeyItem.keyTextures.get(stringId);
                name = KeyItem.keyNames.get(stringId);
                id = type+":"+stringId;
            }
            default -> {
                attributes = new HashMap<>();
                texture = new HashMap<>();
                name = "";
                id = null;
            }
        }
        maxFrames = attributes.getOrDefault("splashFrames", FPS).intValue();
        animFrames = Math.min(FPS, maxFrames) / 2;

        splashColor = Color.decode(texture.getOrDefault("splashColor", "#ffffff"));
        label = name + " x" + count;
        drawSplash();
    }

    public boolean render(Graphics2D g, int x, int y) {
        frames++;
        if (frames < maxFrames) {
            if (maxFrames - frames <= animFrames) {
                drawSplash();
            } else if (frames <= animFrames) {
                final double animSize = Math.min((double) frames / animFrames, 1);

                size[0] = (int) (150 * animSize);
                size[1] = (int) (32 * animSize);
            }

            g.drawImage(splashTexture, x - size[0] / 2, y - size[1] / 2, size[0], size[1], null);
            return true;
        }
        return false;
    }

    private void drawSplash() {
        int opacity = (int) Math.floor(255 * Math.min((double) (maxFrames - frames) / animFrames, 1));

        BufferedImage result = new BufferedImage(150, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        g.setColor(new Color(splashColor.getRed(), splashColor.getGreen(), splashColor.getBlue(), opacity));
        Rendering.centeredText(g, label, 75, 24, 150, 24);

        splashTexture = result;
    }

    public void resetSplash() {
        if (frames > animFrames) {
            frames = animFrames;
        }
        label = name + " x" + count;
        drawSplash();
    }
}
