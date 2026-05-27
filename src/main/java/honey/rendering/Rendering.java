package honey.rendering;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import honey.HoneySuckle;

/*
 * Rendering.java *
 - Class for running static methods involving more complex rendering
 - Static lists of already rendered shit
 */
public final class Rendering {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    private static final int LIGHT_SCALE = 8;

    //Save data -> Save CPU
    public static final Map<String, Map<String, BufferedImage>> textures = new HashMap<>();
    public static final Map<String, Map<String, List<BufferedImage>>> gifFrames = new HashMap<>();
    private static final Map<String, Color> colorCache = new HashMap<>();
    // outer key: texturePath:textureColor, inner key: overlayColor:alpha
    private static final Map<String, Map<String, BufferedImage>> overlayCache = new HashMap<>();

    //Reusable light map images — cleared each frame instead of reallocated
    private static final BufferedImage lightImage = new BufferedImage(GAME_WIDTH / LIGHT_SCALE, GAME_HEIGHT / LIGHT_SCALE, BufferedImage.TYPE_INT_ARGB);
    private static final Graphics2D light2d = lightImage.createGraphics();
    private static final BufferedImage glowImage = new BufferedImage(GAME_WIDTH / LIGHT_SCALE, GAME_HEIGHT / LIGHT_SCALE, BufferedImage.TYPE_INT_ARGB);
    private static final Graphics2D glow2d = glowImage.createGraphics();

    //Fonts
    public static final Map<String, Font> fonts = new HashMap<>();

    public static String imageKey(String primary, String secondary) {
        return primary + ":" + secondary;
    }

    public static int frameCount(String path, int frameWidth, int frameHeight) {
        Map<String, List<BufferedImage>> gifMap = gifFrames.get(path);
        if (gifMap != null && !gifMap.isEmpty()) {
            return gifMap.values().iterator().next().size();
        }

        registerGIF(path, null, frameWidth, frameHeight);
        gifMap = gifFrames.get(path);
        if (gifMap != null && !gifMap.isEmpty()) {
            return gifMap.values().iterator().next().size();
        }
        return 0;
    }

    public static void registerImage(String texture, String color) {
        final String path = "sprites/" + texture;
        if (textures.computeIfAbsent(path, k -> new HashMap<>()).containsKey(color)) return;
        final URL url = HoneySuckle.class.getResource("/images/" + path + ".png");
        if (url == null) return;
        try {
            final BufferedImage base = ImageIO.read(url);
            textures.get(path).put(null, base);
            textures.get(path).put(color, replaceGradient(base, color));
        } catch (IOException e) { }
    }

    public static void registerGIF(String path, String color, int frameWidth, int frameHeight) {
        if (gifFrames.computeIfAbsent(path, k -> new HashMap<>()).containsKey(color)) return;
        final URL url = HoneySuckle.class.getResource("/images/animations/" + path + ".png");
        if (url == null) return;
        try {
            final BufferedImage sheet = ImageIO.read(url);
            final int numCols = sheet.getWidth() / frameWidth;
            final int numRows = sheet.getHeight() / frameHeight;
            final List<BufferedImage> frames = new ArrayList<>();
            final BufferedImage colored = replaceGradient(sheet, color);
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    frames.add(colored.getSubimage(col * frameWidth, row * frameHeight, frameWidth, frameHeight));
                }
            }
            gifFrames.get(path).put(color, frames);
        } catch (IOException e) { }
    }

    //Render sprite
    public static BufferedImage image(String texture) {
        return image(texture, null);
    }

    public static BufferedImage image(String texture, String color) {
        //If already rendered, return
        final Map<String, BufferedImage> textureMap = textures.get(texture);
        if (textureMap != null) {
            final BufferedImage result = textureMap.get(color);
            if (result != null) {
                return result;
            }
        } else {
            textures.put(texture, new HashMap<>());
        }

        BufferedImage baseImage = textures.get(texture).get(null);
        if (baseImage == null) {
            try {
                baseImage = ImageIO.read(HoneySuckle.class.getResource("/images/" + texture + ".png"));
                textures.get(texture).put(null, baseImage);
            } catch (IOException e) {
                System.out.println("HoneySuckle ERROR: Could not find sprite: " + texture);
                return null;
            }
        }
        //Gray scale texture
        final BufferedImage result = replaceGradient(baseImage, color);
        textures.get(texture).put(color, result);
        return result;
    }

    public static BufferedImage texture(String texture) {
        return texture(texture, null);
    }

    //Render sprite
    public static BufferedImage texture(String texture, String color) {
        return image("sprites/" + texture, color);
    }

    //Grey scale image
    public static BufferedImage replaceGradient(BufferedImage image, String color) {
        final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        result.getGraphics().drawImage(image, 0, 0, null);
        if (color == null) {
            return result;
        }
        //Shade to replace with
        final Color shade = decodeColor(color);
        final int shadeR = shade.getRed();
        final int shadeG = shade.getGreen();
        final int shadeB = shade.getBlue();
        //Go through ALL pixels of image
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                final int argb = image.getRGB(x, y);
                final int a = (argb >>> 24) & 0xFF;
                final int r = (argb >> 16) & 0xFF;
                final int g = (argb >> 8) & 0xFF;
                final int b = argb & 0xFF;
                //If shade of grey...
                if (r == g && r == b && r != 0) {
                    //Set grey as percentage of shade
                    final int nr = r * shadeR / 255;
                    final int ng = r * shadeG / 255;
                    final int nb = r * shadeB / 255;
                    result.setRGB(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
                }
            }
        }
        return result;
    }

    //Render Light
    public static void renderLight(Graphics2D g, Color fogColor, Set<Map<String, Number>> lights) {
        //Clear reusable light map images
        light2d.setComposite(AlphaComposite.Clear);
        light2d.fillRect(0, 0, GAME_WIDTH / LIGHT_SCALE, GAME_HEIGHT / LIGHT_SCALE);
        light2d.setComposite(AlphaComposite.SrcOver);

        glow2d.setComposite(AlphaComposite.Clear);
        glow2d.fillRect(0, 0, GAME_WIDTH / LIGHT_SCALE, GAME_HEIGHT / LIGHT_SCALE);
        glow2d.setComposite(AlphaComposite.SrcOver);

        //Render all lights on light map as radial gradients
        for (Map<String, Number> light : lights) {
            final Point2D center = new Point2D.Float(light.get("posX").floatValue() / LIGHT_SCALE, light.get("posY").floatValue() / LIGHT_SCALE);
            final int radius = (int) Math.floor(light.get("radius").doubleValue() * TILE_SIZE / LIGHT_SCALE);
            if (radius != 0) {
                final Ellipse2D circle = new Ellipse2D.Double(
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
            }
            if (light.get("glow") != null && light.get("color") != null) {
                final Color glowColor = new Color(light.get("color").intValue());
                final int glowValue = light.get("glow").intValue();
                final int glowRadius = (int) Math.floor(light.get("glowRadius").doubleValue() * TILE_SIZE / LIGHT_SCALE);
                if (glowRadius != 0) {
                    final Ellipse2D circle = new Ellipse2D.Double(
                            center.getX() - radius,
                            center.getY() - radius,
                            radius * 2, radius * 2);
                    glow2d.setPaint(new RadialGradientPaint(
                            center,
                            glowRadius,
                            new float[]{0f, 1f},
                            new Color[]{new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), glowValue),
                                new Color(0, 0, 0, 0)}
                    ));
                    glow2d.fill(circle);
                }
            }
        }
        //Input light map into translator and render
        drawLight(g, lightImage, fogColor);
        g.drawImage(glowImage, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
    }

    //Translate lightmap into light mask
    public static void drawLight(Graphics2D g, BufferedImage original, Color blendColor) {

        //Light map data
        final int width = original.getWidth();
        final int height = original.getHeight();

        final BufferedImage transparent = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final int br = blendColor.getRed();
        final int bg = blendColor.getGreen();
        final int bb = blendColor.getBlue();
        //Go through all light map pixels...
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //Replace light map black px with light color opacity
                final int alpha = (original.getRGB(x, y) >>> 24) & 0xFF;
                transparent.setRGB(x, y, ((255 - alpha) << 24) | (br << 16) | (bg << 8) | bb);
            }
        }
        //Draw light mask
        g.drawImage(transparent, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
    }

    //Render screen color fade
    public static void colorFade(Graphics2D g, Color color, double percent) {
        //Ensures percent within range
        if (percent < 0.1) {
            percent = 0;
        }
        if (percent > 1) {
            percent = 1;
        }
        //Set paint to radial gradient
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(HoneySuckle.GAME_WIDTH / 2, GAME_HEIGHT / 2),
                GAME_WIDTH / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * percent))}
        ));

        //Render color fase
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
    }

    //Render frame from PNG sprite sheet (left-to-right, top-to-bottom) under animations/
    public static BufferedImage renderGIF(String path, String color, double frame, int frameWidth, int frameHeight) {
        final Map<String, List<BufferedImage>> gifMap = gifFrames.get(path);
        if (gifMap != null) {
            final List<BufferedImage> frames = gifMap.get(color);
            if (frames != null) {
                return frames.get((int) Math.floor(frame * frames.size()));
            }
        } else {
            gifFrames.put(path, new HashMap<>());
        }

        registerGIF(path, color, frameWidth, frameHeight);
        final List<BufferedImage> frames = gifFrames.get(path).get(color);
        return frames.get((int) Math.floor(frame * frames.size()));
    }

    public static BufferedImage renderGIF(String path, String color, int frame, int frameWidth, int frameHeight) {
        final Map<String, List<BufferedImage>> gifMap = gifFrames.get(path);
        if (gifMap != null) {
            final List<BufferedImage> frames = gifMap.get(color);
            if (frames != null) {
                return frames.get(Math.min(frame, frames.size() - 1));
            }
        } else {
            gifFrames.put(path, new HashMap<>());
        }

        registerGIF(path, color, frameWidth, frameHeight);
        final List<BufferedImage> frames = gifFrames.get(path).get(color);
        return frames.get(Math.min(frame, frames.size() - 1));
    }

    //Apply opacity overlay to image, cached by base image identity and overlay parameters
    public static BufferedImage applyOverlay(String texturePath, String textureColor, String overlayColor, int alpha) {
        final String imageKey = imageKey(texturePath, textureColor);
        final String overlayKey = imageKey(overlayColor, String.valueOf(alpha));

        final Map<String, BufferedImage> imageOverlays = overlayCache.computeIfAbsent(imageKey, k -> new HashMap<>());
        final BufferedImage cached = imageOverlays.get(overlayKey);
        if (cached != null) {
            return cached;
        }

        final BufferedImage source = texture(texturePath, textureColor);
        if (source == null) {
            return null;
        }

        final int width = source.getWidth();
        final int height = source.getHeight();

        final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = result.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        final Color c = decodeColor(overlayColor);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        g.fillRect(0, 0, width, height);
        g.dispose();

        imageOverlays.put(overlayKey, result);
        return result;
    }

    public static BufferedImage applyOverlay(BufferedImage source, String imageKey, String overlayColor, int alpha) {
        final String overlayKey = imageKey(overlayColor, String.valueOf(alpha));

        final Map<String, BufferedImage> imageOverlays = overlayCache.computeIfAbsent(imageKey, k -> new HashMap<>());
        final BufferedImage cached = imageOverlays.get(overlayKey);
        if (cached != null) {
            return cached;
        }

        if (source == null) {
            return null;
        }

        final int width = source.getWidth();
        final int height = source.getHeight();

        final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = result.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        final Color c = decodeColor(overlayColor);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        g.fillRect(0, 0, width, height);
        g.dispose();

        imageOverlays.put(overlayKey, result);
        return result;
    }

    //Render rectange with border
    public static void borderRect(Graphics2D g, int border, Color color, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
        borderOutline(g, border, color, x, y, width, height);
    }

    //Draw only the outline of a rectangle (no fill)
    public static void borderOutline(Graphics2D g, int border, Color color, int x, int y, int width, int height) {
        g.setStroke(new BasicStroke(border));
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

    public static void centeredText(Graphics2D g, String text, int x, int y) {
        final int fontOffset = g.getFontMetrics().stringWidth(text) / 2;

        g.drawString(text, x - fontOffset, y);
    }

    public static void centeredText(Graphics2D g, String text, int x, int y, int width, int maxFontSize) {
        int fontOffset = 0;

        // Sets the font size to fit within box
        for (int f = maxFontSize; f > 0; f--) {
            g.setFont(new Font("VT323 Regular", Font.PLAIN, f));
            final int fontSize = g.getFontMetrics().stringWidth(text);
            if (fontSize < width) {
                fontOffset = fontSize / 2;
                break;
            }
        }

        g.drawString(text, x - fontOffset, y);
    }

    public static BufferedImage scroll(int length) {
        final BufferedImage result = new BufferedImage(length * 4 + 8, 32, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = result.createGraphics();

        g.drawImage(Rendering.texture("ui/scroll/scroll_end", null), 0, 0, 4, 32, null);
        for (int i = 0; i < length; i++) {
            g.drawImage(Rendering.texture("ui/scroll/scroll_middle", null), 4 + i * 4, 0, 4, 32, null);
        }
        g.drawImage(Rendering.texture("ui/scroll/scroll_end", null), 4 + length * 4, 0, 4, 32, null);

        return result;
    }

    public static BufferedImage rotateImage(BufferedImage image, double degrees) {
        // Convert angle to radians
        final double angle = Math.toRadians(degrees);

        final int w = image.getWidth();
        final int h = image.getHeight();

        // Calculate new dimensions after rotation
        final double sin = Math.abs(Math.sin(angle));
        final double cos = Math.abs(Math.cos(angle));
        final int newW = (int) Math.floor(w * cos + h * sin);
        final int newH = (int) Math.floor(h * cos + w * sin);

        // Create a new image with the new dimensions and an alpha channel
        final BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = rotated.createGraphics();

        // Maintain image quality
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Create an AffineTransform to rotate around the center
        AffineTransform at = new AffineTransform();
        at.translate(newW / 2.0, newH / 2.0); // Move origin to center
        at.rotate(angle);
        at.translate(-w / 2.0, -h / 2.0); // Move image back

        // Draw the original image with the transform
        g.drawImage(image, at, null);
        g.dispose();

        return rotated;
    }

    public static Color decodeColor(String colorId) {
        if (colorId == null) {
            return null;
        }
        return colorCache.computeIfAbsent(colorId, Color::decode);
    }

    public static Color decodeColor(String colorId, int alpha) {
        if (colorId == null) {
            return null;
        }
        final Color c = decodeColor(colorId);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static void imageFactor(BufferedImage image, Graphics2D g, int x, int y, int w, int h, double factor) {
        g.drawImage(image, (int) (x - w * (factor - 1) / 2), (int) (y - h * (factor - 1) / 2), (int) (w * factor), (int) (h * factor), null);
    }
}
