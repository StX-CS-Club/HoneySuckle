
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

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
        count = itemData.getOrDefault("count", 1).intValue();
        switch (itemData.getOrDefault("type", 0).intValue()) {
            case 0 -> {
                id = Item.itemStringId.get(intId);
                attributes = Item.itemAttributes.get(id);
                texture = Item.itemTextures.get(id);
                name = Item.itemNames.get(id);
            }
            case 1 -> {
                this.id = Weapon.weaponStringId.get(intId);
                attributes = Weapon.weaponAttributes.get(id);
                texture = Weapon.weaponTextures.get(id);
                name = Weapon.weaponNames.get(id);
            }
            case 2 -> {
                this.id = Armor.armorStringId.get(intId);
                attributes = Armor.armorAttributes.get(id);
                texture = Armor.armorTextures.get(id);
                name = Armor.armorNames.get(id);
            }
            case 3 -> {
                this.id = Ammo.ammoStringId.get(intId);
                attributes = Ammo.ammoAttributes.get(id);
                texture = Ammo.ammoTextures.get(id);
                name = Ammo.ammoNames.get(id);
            }
            default -> {
                attributes = new HashMap<>();
                texture = new HashMap<>();
                name = "";
                this.id = null;
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
