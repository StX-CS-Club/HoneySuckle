
package honey.rendering;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import honey.mechanics.ConfigManager;
import honey.player.armory.Ammo;
import honey.player.armory.Armor;
import honey.player.armory.Weapon;
import honey.player.build.Blueprint;
import honey.player.inventory.Craft;
import honey.player.inventory.Item;
import honey.player.inventory.KeyItem;

public class Splash {

    public static ConfigManager config;

    public static final String ITEM_SYMBOL = "◆";
    public static final String WEAPON_SYMBOL = "⚔";
    public static final String ARMOR_SYMBOL = "⛨";
    public static final String AMMO_SYMBOL = "➤";
    public static final String KEY_ITEM_SYMBOL = "⚷";
    public static final String RECIPE_SYMBOL = "✎";
    public static final String BLUEPRINT_SYMBOL = "▦";

    // The symbol isn't in "VT323 Regular" (the pixel font every other splash character uses), so it's
    // drawn separately with this fallback font and placed to the left of the VT323 text instead of being
    // concatenated into one string.
    private static final String SYMBOL_FONT_NAME = Font.SANS_SERIF;
    private static final int SYMBOL_GAP = 4;

    public final String id;
    private final Map<String, Number> attributes;
    private final Map<String, String> texture;
    private final String name;
    private final String symbol;

    public int count;
    private int frames = 0;
    private final int maxFrames;
    private final int animFrames;

    final Color splashColor;
    private BufferedImage splashTexture;
    private String text;

    private final int[] size = new int[2];

    public Splash(Map<String, Number> itemData, int count) {
        final int intId = itemData.get("id").intValue();
        final int type = itemData.getOrDefault("type", 0).intValue();
        this.count = count;
        switch (type) {
            case 0 -> {
                final String stringId = Item.itemStringId.get(intId);
                attributes = Item.itemAttributes.get(stringId);
                texture = Item.itemTextures.get(stringId);
                name = Item.itemNames.get(stringId);
                symbol = ITEM_SYMBOL;
                id = type+":"+stringId;
            }
            case 1 -> {
                final String stringId = Weapon.weaponStringId.get(intId);
                attributes = Weapon.weaponAttributes.get(stringId);
                texture = Weapon.weaponTextures.get(stringId);
                name = Weapon.weaponNames.get(stringId);
                symbol = WEAPON_SYMBOL;
                id = type+":"+stringId;
            }
            case 2 -> {
                final String stringId = Armor.armorStringId.get(intId);
                attributes = Armor.armorAttributes.get(stringId);
                texture = Armor.armorTextures.get(stringId);
                name = Armor.armorNames.get(stringId);
                symbol = ARMOR_SYMBOL;
                id = type+":"+stringId;
            }
            case 3 -> {
                final String stringId = Ammo.ammoStringId.get(intId);
                attributes = Ammo.ammoAttributes.get(stringId);
                texture = Ammo.ammoTextures.get(stringId);
                name = Ammo.ammoNames.get(stringId);
                symbol = AMMO_SYMBOL;
                id = type+":"+stringId;
            }
            case 4 -> {
                final String stringId = KeyItem.keyStringId.get(intId);
                attributes = KeyItem.keyAttributes.get(stringId);
                texture = KeyItem.keyTextures.get(stringId);
                name = KeyItem.keyNames.get(stringId);
                symbol = KEY_ITEM_SYMBOL;
                id = type+":"+stringId;
            }
            case 5 -> {
                final String stringId = Craft.recipeStringId.get(intId);
                attributes = Craft.recipeAttributes.get(stringId);
                texture = Craft.recipeTextures.get(stringId);
                name = Craft.recipeNames.get(stringId);
                symbol = RECIPE_SYMBOL;
                id = type+":"+stringId;
            }
            case 6 -> {
                final String stringId = Blueprint.blueprintStringId.get(intId);
                attributes = new HashMap<>();
                texture = Blueprint.blueprintTextures.get(stringId);
                name = Blueprint.blueprintNames.get(stringId);
                symbol = BLUEPRINT_SYMBOL;
                id = type+":"+stringId;
            }
            default -> {
                attributes = new HashMap<>();
                texture = new HashMap<>();
                name = "";
                symbol = "";
                id = null;
            }
        }
        maxFrames = attributes.getOrDefault("splashFrames", config.fps).intValue();
        animFrames = Math.min(config.fps, maxFrames) / 2;

        splashColor = Rendering.decodeColor(texture.getOrDefault("splashColor", "#ffffff"));
        text = count + "x " + name;
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
        final int opacity = (int) Math.floor(255 * Math.min((double) (maxFrames - frames) / animFrames, 1));

        final BufferedImage result = new BufferedImage(150, 32, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = result.createGraphics();

        g.setColor(new Color(splashColor.getRed(), splashColor.getGreen(), splashColor.getBlue(), opacity));

        final boolean hasSymbol = !symbol.isEmpty();

        // Shrink both fonts together (same size each step) until the symbol + gap + text fit the 150px box -
        // mirrors Rendering.centeredText's shrink-to-fit loop, but sized against the combined width of two fonts.
        Font textFont = new Font("VT323 Regular", Font.PLAIN, 24);
        Font symbolFont = new Font(SYMBOL_FONT_NAME, Font.PLAIN, 24);
        int textWidth = 0;
        int symbolWidth = 0;
        for (int f = 24; f > 0; f--) {
            textFont = new Font("VT323 Regular", Font.PLAIN, f);
            symbolFont = new Font(SYMBOL_FONT_NAME, Font.PLAIN, f);
            textWidth = g.getFontMetrics(textFont).stringWidth(text);
            symbolWidth = hasSymbol ? g.getFontMetrics(symbolFont).stringWidth(symbol) : 0;
            final int gap = hasSymbol ? SYMBOL_GAP : 0;
            if (textWidth + symbolWidth + gap < 150) {
                break;
            }
        }

        final int gap = hasSymbol ? SYMBOL_GAP : 0;
        int drawX = 75 - (symbolWidth + gap + textWidth) / 2;

        if (hasSymbol) {
            g.setFont(symbolFont);
            g.drawString(symbol, drawX, 24);
            drawX += symbolWidth + gap;
        }

        g.setFont(textFont);
        g.drawString(text, drawX, 24);

        splashTexture = result;
    }

    public void resetSplash() {
        if (frames > animFrames) {
            frames = animFrames;
        }
        text = count + "x " + name;
        drawSplash();
    }
}
