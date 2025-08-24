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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

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

    //Fonts
    public static final Map<String, Font> fonts = new HashMap<>();

    //Render sprite
    public static BufferedImage image(String texture) {
        //If already rendered, return
        Map<String, BufferedImage> textureMap = textures.get(texture);
        if (textureMap != null) {
            BufferedImage result = textureMap.get(null);
            if (result != null) {
                return result;
            }
        } else {
            textures.put(texture, new HashMap<>());
        }
        //Result image
        BufferedImage result;
        try {
            //Gray scale texture
            result = ImageIO.read(HoneySuckle.class.getResource("/images/" + texture + ".png"));
            textures.get(texture).put(null, result);
            return result;
        } catch (IOException e) {
            System.out.println("HoneySuckle ERROR: Could not find sprite: " + texture);
        }
        //If error, return null
        return null;
    }

    //Render sprite
    public static BufferedImage texture(String texture, String color) {
        //If already rendered, return
        Map<String, BufferedImage> textureMap = textures.get(texture);
        if (textureMap != null) {
            BufferedImage result = textureMap.get(color);
            if (result != null) {
                return result;
            }
        } else {
            textures.put(texture, new HashMap<>());
        }
        //Result image
        BufferedImage result;
        try {
            //Gray scale texture
            result = replaceGradient(ImageIO.read(HoneySuckle.class.getResource("/images/sprites/" + texture + ".png")), color);
            textures.get(texture).put(color, result);
            return result;
        } catch (IOException e) {
            System.out.println("HoneySuckle ERROR: Could not find sprite: " + texture);
        }
        //If error, return null
        return null;
    }

    //Grey scale image
    public static BufferedImage replaceGradient(BufferedImage image, String color) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        result.getGraphics().drawImage(image, 0, 0, null);
        if (color == null) {
            return result;
        }
        //Shade to replace with
        Color shade = Color.decode(color);
        //Go through ALL pixels of image
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                //Individual pixel
                Color px = new Color(image.getRGB(x, y), true);
                //If shade of grey...
                if (px.getRed() == px.getGreen() && px.getRed() == px.getBlue() && px.getRed() != 0) {
                    //Set grey as percentage of shade
                    double ratio = (px.getRed()) / 255.0;
                    result.setRGB(x, y, new Color(
                            (int) (shade.getRed() * ratio),
                            (int) (shade.getGreen() * ratio),
                            (int) (shade.getBlue() * ratio)
                    ).getRGB());
                }
            }
        }
        return result;
    }

    //Render Light
    public static void renderLight(Graphics2D g, Color fogColor, Set<Map<String, Number>> lights) {
        //Create empty light map
        BufferedImage lightImage = new BufferedImage(GAME_WIDTH / LIGHT_SCALE, GAME_HEIGHT / LIGHT_SCALE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D light2d = lightImage.createGraphics();

        BufferedImage glowImage = new BufferedImage(GAME_WIDTH / LIGHT_SCALE, GAME_HEIGHT / LIGHT_SCALE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D glow2d = glowImage.createGraphics();

        //Render all lights on light map as radial gradients
        for (Map<String, Number> light : lights) {
            final Point2D center = new Point2D.Float(light.get("posX").floatValue() / LIGHT_SCALE, light.get("posY").floatValue() / LIGHT_SCALE);
            final int radius = (int) Math.floor(light.get("radius").doubleValue() * TILE_SIZE / LIGHT_SCALE);
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
            if (light.get("glow") != null && light.get("color") != null) {
                Color glowColor = new Color(light.get("color").intValue());
                int glowValue = light.get("glow").intValue();
                final int glowRadius = (int) Math.floor(light.get("glowRadius").doubleValue() * TILE_SIZE / LIGHT_SCALE);
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
        //Input light map into translator and render
        drawLight(g, lightImage, fogColor);
        g.drawImage(glowImage, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
    }

    //Translate lightmap into light mask
    public static void drawLight(Graphics2D g, BufferedImage original, Color blendColor) {

        //Light map data
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage transparent = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //Go through all light map pixels...
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //Replace light map black px with light color opacity
                Color color = new Color(original.getRGB(x, y), true);
                transparent.setRGB(x, y, new Color(blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), 255 - color.getAlpha()).getRGB());
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

    //Render frames of gif
    public static BufferedImage renderGIF(String path, String color, double frame) {
        //If already rendered, return
        Map<String, List<BufferedImage>> gifMap = gifFrames.get(path);
        if (gifMap != null) {
            List<BufferedImage> frames = gifMap.get(color);
            if (frames != null) {
                return frames.get((int) Math.floor(frame * frames.size()));
            }
        } else {
            gifFrames.put(path, new HashMap<>());
        }

        //Current frames of gif
        List<BufferedImage> frames = gifFrames.get(path).get(color);
        //If frames not found...
        if (frames == null) {
            //Break down frames of gif into list of frames
            frames = new ArrayList<>();
            File gifFile;
            try {
                gifFile = new File(HoneySuckle.class.getResource("/images/gifs/" + path + ".gif").toURI());
                ImageInputStream input = ImageIO.createImageInputStream(gifFile);
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(input);

                    int numFrames = reader.getNumImages(true);
                    for (int i = 0; i < numFrames; i++) {
                        BufferedImage gifFrame = replaceGradient(reader.read(i), color);
                        frames.add(gifFrame);
                    }
                    reader.dispose();
                }
                gifFrames.get(path).put(color, frames);
            } catch (URISyntaxException e) {
                System.out.println("HoneySuckle ERROR: Could not find gif: " + path);
            } catch (IOException e) {
                System.out.println("HoneySuckle ERROR: Failed to interpret GIF at: " + path);
            }
        }
        //Return appropriate frame
        return frames.get((int) Math.floor(frame * frames.size()));
    }

    //Apply opacity overlay to image
    public static BufferedImage applyOverlay(BufferedImage image, String color, int alpha) {
        //Width of image
        int width = image.getWidth();
        int height = image.getHeight();

        //Create overlay image
        BufferedImage overlayedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = overlayedImage.createGraphics();

        //Draw base image
        g.drawImage(image, 0, 0, null);

        //Apply opacity overlay
        g.setComposite(AlphaComposite.SrcAtop);
        Color c = Color.decode(color);
        g.setColor(new Color(c.getRed(), c.getBlue(), c.getGreen(), alpha));
        g.fillRect(0, 0, width, height);

        //Dispose of Graphics, return image
        g.dispose();
        return overlayedImage;
    }

    //Render rectange with border
    public static void borderRect(Graphics2D g, int border, Color color, int x, int y, int width, int height) {
        //Fill base rect
        g.fillRect(x, y, width, height);

        //Stroke outline of rect
        g.setStroke(new BasicStroke(border));
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

    public static void centeredText(Graphics2D g, String text, int x, int y) {
        int fontOffset = g.getFontMetrics().stringWidth(text) / 2;

        g.drawString(text, x - fontOffset, y);
    }

    public static void centeredText(Graphics2D g, String text, int x, int y, int width, int maxFontSize) {
        int fontOffset = 0;

        // Sets the font size to fit within box
        for (int f = maxFontSize; f > 0; f--) {
            g.setFont(new Font("VT323 Regular", Font.PLAIN, f));
            int fontSize = g.getFontMetrics().stringWidth(text);
            if (fontSize < width) {
                fontOffset = fontSize / 2;
                break;
            }
        }

        g.drawString(text, x - fontOffset, y);
    }

    public static BufferedImage scroll(int length) {
        BufferedImage result = new BufferedImage(length * 4 + 8, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        g.drawImage(Rendering.texture("hud/scroll/scroll_end", null), 0, 0, 4, 32, null);
        for (int i = 0; i < length; i++) {
            g.drawImage(Rendering.texture("hud/scroll/scroll_middle", null), 4 + i * 4, 0, 4, 32, null);
        }
        g.drawImage(Rendering.texture("hud/scroll/scroll_end", null), 4 + length * 4, 0, 4, 32, null);

        return result;
    }

    public static BufferedImage rotateImage(BufferedImage image, double degrees) {
        // Convert angle to radians
        double angle = Math.toRadians(degrees);

        int w = image.getWidth();
        int h = image.getHeight();

        // Calculate new dimensions after rotation
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        // Create a new image with the new dimensions and an alpha channel
        BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rotated.createGraphics();

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

    public static Color decodeColor(String colorId, int alpha) {
        if (colorId == null) {
            return null;
        }
        final Color color = Color.decode(colorId);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
