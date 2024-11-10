
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
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

/*
 * Rendering.java *
 - Class for running static methods involving more complex rendering
 - Static lists of already rendered shit
 */

public class Rendering {

    //Save data -> Save CPU
    public static Map<String, Map<String, BufferedImage>> textures = new HashMap<>();
    public static Map<String, List<BufferedImage>> gifFrames = new HashMap<>();

    //Render sprite
    public static BufferedImage texture(String texture, String color) {
        //If already rendered, return
        if (textures.get(texture) != null) {
            if (textures.get(texture).get(color) != null) {
                return textures.get(texture).get(color);
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
            System.out.println("HoneySuckle ERROR: Could not find sprite: "+texture);
        }
        //If error, return null
        return null;
    }

    //Grey scale image
    public static BufferedImage replaceGradient(BufferedImage image, String color) {
        //Shade to replace with
        Color shade = Color.decode(color);
        //Go through ALL pixels of image
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                //Individual pixel
                Color px = new Color(image.getRGB(x, y), true);
                //If shade of grey...
                if (px.getRed() == px.getGreen() && px.getRed() == px.getBlue() && px.getRed() != 0) {
                    //Set grey as percentage of shade
                    double ratio = (px.getRed()) / 255.0;
                    image.setRGB(x, y, new Color(
                            (int) (shade.getRed() * ratio),
                            (int) (shade.getGreen() * ratio),
                            (int) (shade.getBlue() * ratio)
                    ).getRGB());
                }
            }
        }
        return image;
    }

    //Render Light
    public static void renderLight(Graphics2D g, Color fogColor, Set<Map<String, Integer>> lights) {
        //Create empty light map
        BufferedImage lightImage = new BufferedImage(HoneySuckle.size[0], HoneySuckle.size[1], BufferedImage.TYPE_INT_ARGB);
        Graphics2D light2d = lightImage.createGraphics();

        //Render all lights on light map as radial gradients
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
        //Input light map into translator and render
        drawLight(g, lightImage, fogColor);
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
        g.drawImage(transparent, 0, 0, null);
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
                new Point2D.Float(HoneySuckle.size[0] / 2, HoneySuckle.size[1] / 2),
                HoneySuckle.size[0] / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * percent))}
        ));

        //Render color fase
        g.fillRect(0, 0, HoneySuckle.size[0], HoneySuckle.size[1]);
    }

    //Render frames of gif
    public static BufferedImage renderGIF(String path, double frame) {
        //Current frames of gif
        List<BufferedImage> frames = gifFrames.get(path);
        //If frames not found...
        if (frames == null) {
            //Break down frames of gif into list of frames
            frames = new ArrayList<>();
            File gifFile;
            try {
                gifFile = new File(Rendering.class.getResource(path).toURI());
                ImageInputStream input = ImageIO.createImageInputStream(gifFile);
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(input);

                    int numFrames = reader.getNumImages(true);
                    for (int i = 0; i < numFrames; i++) {
                        BufferedImage gifFrame = reader.read(i);
                        frames.add(gifFrame);
                    }
                    reader.dispose();
                }
                gifFrames.put(path, frames);
            } catch (URISyntaxException e) {
                System.out.println("HoneySuckle ERROR: Could not find gif: "+path);
            } catch (IOException e) {
                System.out.println("HoneySuckle ERROR: Failed to interpret GIF at: "+path);
            }
        }
        //Return appropriate frame
        return frames.get((int) Math.floor(frame * frames.size()));
    }

    //Apply opacity overlay to image
    public static BufferedImage applyOverlay(BufferedImage image, String color) {
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
        g.setColor(new Color(c.getRed(), c.getBlue(), c.getGreen(), 128));
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
}
