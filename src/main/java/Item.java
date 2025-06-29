
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {

    private static final int FPS = HoneySuckle.FPS;

    //Static json data
    public static final Map<String, String> itemNames = new HashMap<>();
    public static final Map<String, Map<String, String>> itemTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> itemAttributes = new HashMap<>();
    public static final Map<String, List<String>> itemRecipeUnlocks = new HashMap<>();
    public static final Map<Integer, String> itemStringId = new HashMap<>();
    public static final Map<String, Integer> itemIntId = new HashMap<>();

    final String id;
    int count;

    int splashFrame = 0;

    private final String name;
    private final Map<String, String> texture;
    private final Map<String, Number> attributes;

    public Item(String id, int count) {
        this.id = id;
        this.count = count;

        name = itemNames.get(id);
        texture = itemTextures.get(id);
        attributes = itemAttributes.get(id);
    }

    public void renderUiTile(Graphics2D g, int x, int y) {
        String color = "#ffffff";
        if (count > 0) {
            color = "#f5d39d";
        }

        g.drawImage(Rendering.texture("hud/slot", color), x, y, 100, 100, null);

        final String itemTexture = texture.get("texture");
        if (itemTexture != null) {
            g.drawImage(Rendering.texture(itemTexture, "#ffffff"), x + 25, y + 25, 50, 50, null);
        }

        final String label = name + " x" + count;

        g.setColor(new Color(224, 224, 224));

        // Draws the font
        Rendering.centeredText(g, label, x + 50, y + 100, 100, 24);
    }

    public boolean renderSplash(Graphics2D g, int x, int y) {
        int maxFrames = attributes.getOrDefault("splashFrames", FPS).intValue();
        splashFrame++;
        if (splashFrame < maxFrames) {
            int animFrames = Math.min(FPS, maxFrames) / 2;

            double size = Math.min((double) splashFrame / animFrames, 1);
            int opacity = (int) Math.floor(255 * Math.min((double) (maxFrames - splashFrame) / animFrames, 1));

            BufferedImage textImage = new BufferedImage(100, 24, BufferedImage.TYPE_INT_ARGB);
            Graphics2D textGraphics = textImage.createGraphics();

            Color baseColor = Color.decode(texture.getOrDefault("pickupColor", "#ffffff"));
            textGraphics.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), opacity));
            Rendering.centeredText(textGraphics, name + " x" + count, 50, 24, 100, 24);

            int width = (int) (100 * size);
            int height = (int) (24 * size);

            g.drawImage(textImage, x - width / 2, y - height / 2, width, height, null);
            return true;
        }
        return false;
    }

    public void resetSplashFrame() {
        int animFrames = Math.min(FPS, attributes.getOrDefault("splashFrames", FPS).intValue()) / 2;
        if (splashFrame > animFrames) {
            splashFrame = animFrames;
        }
    }
}
