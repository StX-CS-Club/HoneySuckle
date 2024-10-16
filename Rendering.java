
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class Rendering {

    public static Map<String, Map<String, BufferedImage>> textures = new HashMap<>();

    public static BufferedImage texture(String texture, String color) {
        if (textures.get(texture) != null) {
            if (textures.get(texture).get(color) != null) {
                return textures.get(texture).get(color);
            }
        } else {
            textures.put(texture, new HashMap<>());
        }
        BufferedImage result;
        try {
            result = ImageIO.read(HoneySuckle.class.getResource("/assets/sprites/" + texture + ".png"));
            if (result != null) {
                Color shade = Color.decode(color);
                for(int x = 0; x < result.getWidth(); x++){
                    for(int y = 0; y < result.getHeight(); y++){
                        Color px = new Color(result.getRGB(x, y), true);
                        if(px.getRed() == px.getGreen() && px.getRed() == px.getBlue() && px.getRed() != 0){
                            double ratio = ((double)px.getRed())/255;

                            System.out.println(ratio);
                            result.setRGB(x, y, new Color(
                                (int) (shade.getRed() * ratio),
                                (int) (shade.getGreen() * ratio),
                                (int) (shade.getBlue() * ratio)
                            ).getRGB());
                        }
                    }
                }
                textures.get(texture).put(color, result);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void renderLight(Graphics2D g, Color fogColor, Set<Map<String, Integer>> lights) {
        BufferedImage lightImage = new BufferedImage(HoneySuckle.size[0], HoneySuckle.size[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D light2d = lightImage.createGraphics();

        for (Map<String, Integer> light : lights) {
            Point2D center = new Point2D.Float(light.get("posX"), light.get("posY"));
            int radius = light.get("radius");
            Ellipse2D circle = new Ellipse2D.Double(
                    center.getX() - radius,
                    center.getY() - radius,
                    radius * 2, radius * 2);
            light2d.setPaint(new RadialGradientPaint(
                    center,
                    radius,
                    new float[]{0f, 1f},
                    new Color[]{Color.white, new Color(0, 0, 0, 0)}
            ));
            light2d.fill(circle);
            if (light.get("color") != null) {
                Color color = new Color(light.get("color"));
                g.setPaint(new RadialGradientPaint(
                        center,
                        radius,
                        new float[]{0f, 1f},
                        new Color[]{new Color(color.getRed(), color.getGreen(), color.getBlue(), 50), new Color(0, 0, 0, 0)}
                ));
                g.fill(circle);
            }
        }
        drawLight(g, lightImage, fogColor);
    }

    public static void drawLight(Graphics2D g, BufferedImage original, Color blendColor) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage transparent = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(original.getRGB(x, y), true);
                transparent.setRGB(x, y, new Color(blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), 255 - color.getAlpha()).getRGB());
            }
        }
        g.drawImage(transparent, 0, 0, null);
    }

    public static void colorFade(Graphics2D g, Color color, double percent) {
        if (percent < 0.1) {
            percent = 0;
        }
        if (percent > 1) {
            percent = 1;
        }
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(HoneySuckle.size[0] / 2, HoneySuckle.size[1] / 2),
                HoneySuckle.size[0] / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * percent))}
        ));

        g.fillRect(0, 0, HoneySuckle.size[0], HoneySuckle.size[1]);
    }

    public static void borderRect(Graphics2D g, int border, Color color, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);

        g.setStroke(new BasicStroke(border));
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

}
