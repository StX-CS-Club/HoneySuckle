
package honey;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import honey.mechanics.FileManager;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Menu;
import honey.rendering.Rendering;
import honey.world.Biome;
import honey.world.Entity;
import honey.world.World;

/* 
 * HoneySuckle.java *
 - Main Class
 -Static variables and constants
 - Creates window and canvas, centerlizes rendering and updating
 */
//Main class, extends JPanel for graphics, implements runnable and listeners
public class HoneySuckle extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    //Static variables referenced throughout code
    public static final int FPS = 30;
    public static final int GAME_WIDTH = 640;
    public static final int GAME_HEIGHT = 480;
    public static final int TILE_SIZE = 32;
    public static final int HUD_SIZE = 64;

    //Static variable referencing inputs
    private static final InputHandler inputHandler = new InputHandler();

    //Main player
    public static Player player;
    public static Menu menu = new Menu(Menu.MenuType.MAIN_MENU);

    public static boolean play = false;

    //Public set of light data used in lighting system
    public static Set<Map<String, Number>> lights = new LinkedHashSet<>();
    public static Set<Entity> healthBars = new LinkedHashSet<>();

    //Main Method
    public static void main(String[] args) {
        //Creates the window
        JFrame frame = new JFrame("HoneySuckle");
        HoneySuckle panel = new HoneySuckle();

        //Trys to set window icon as logo
        URL iconUrl = HoneySuckle.class.getResource("/images/HoneySuckleIcon.png");
        try {
            Image iconImage = ImageIO.read(iconUrl);
            frame.setIconImage(iconImage);
        } catch (IOException e) {
            if (iconUrl == null) {
                System.out.println("HoneySuckle ERROR: Could not find icon.");
            } else {
                System.out.println("HoneySuckle ERROR: Failed to import icon.");
            }
        }

        //Appends rendering to window
        frame.add(panel);
        //Adds resize listener to allow resizing rendering canvas
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Starts game loop
        Thread gameThread = new Thread(panel);
        gameThread.start();
    }

    //Game Constructor
    public HoneySuckle() {
        //Creates window
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        //Adds event listeners to window
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        //Fetches all data from json files into appropriate hashmaps

        FileManager.readJsonData();
        FileManager.registerFont();
        FileManager.preloadImages();
    }

    public static void stop() {
        play = false;
        World.worlds.clear();
        World.level = 0;
        Player.players.clear();
        healthBars.clear();
        lights.clear();
    }

    public static void start() {
        //Creates world 1
        World world = new World("hive");
        //Creates main player in reference to world 1
        player = new Player(new double[]{TILE_SIZE * (world.start[0] + 0.5), TILE_SIZE * (world.start[1] + 0.5)},
                (int) (TILE_SIZE * 0.75), Arrays.asList("leader"));
        play = true;
    }

    //Render
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Converts Graphics to Graphics2D for more methods
        final BufferedImage internalFrame = new BufferedImage(
                GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB
        );
        final Graphics2D g2d = (Graphics2D) internalFrame.getGraphics();

        if (play) {
            //Resets lights every frame
            lights.clear();

            //Renders World
            final World world = World.worlds.get(World.level);
            world.render(g2d);
            //Renders Players
            for (Player renderPlayer : Player.players) {
                renderPlayer.render(g2d);
            }

            final Biome biome = World.worlds.get(World.level).biome;
            //Renders fog, is present
            if (biome.tags.contains("fog")) {
                final Color fogColor = Rendering.decodeColor(biome.textureMap.get("fogColor"));
                Rendering.renderLight(g2d, fogColor, lights);
            }
            
            biome.renderOverlay(g2d);

            //Creates red overlay when at low health
            Rendering.colorFade(g2d, Color.red, 1 - player.health);

            int healthBarIndex = 0;
            for (Entity entity : healthBars) {
                entity.renderHealthBar(g2d, healthBarIndex);
                healthBarIndex++;
            }

            if (player.health > 0) {
                //Renders crafting and weapon ui
                if(world.navigator.isOpen){
                    world.navigator.renderUi(g2d);
                } else if (player.inventory.isOpen) {
                    player.inventory.renderUi(g2d);
                } else {
                    player.build.renderUi(g2d, World.worlds.get(World.level));
                    player.armory.renderUi(g2d, !healthBars.isEmpty());
                }
            }
        }

        if (!menu.complete) {
            menu.render(g2d);
        }

        scaleGraphics(g, internalFrame);
        //Disposes of Graphics and Graphics2D
        g2d.dispose();
        g.dispose();
    }

    public void scaleGraphics(Graphics g, BufferedImage frame) {
        double width = getWidth();
        double height = getHeight();

        double scaleX = width / (double) GAME_WIDTH;
        double scaleY = height / (double) GAME_HEIGHT;

        double scale = Math.min(scaleX, scaleY);

        int offsetX = (int) Math.floor((width - GAME_WIDTH * scale) / 2);
        int offsetY = (int) Math.floor((height - GAME_HEIGHT * scale) / 2);

        int gameWidth = (int) Math.floor(GAME_WIDTH * scale);
        int gameHeight = (int) Math.floor(GAME_HEIGHT * scale);
        // Apply scaling and translation
        g.drawImage(frame, offsetX, offsetY, gameWidth, gameHeight, null);

        g.setColor(Color.BLACK);

        g.fillRect(0, 0, offsetX + 1, gameHeight + 1);
        g.fillRect(gameWidth + offsetX, 0, offsetX + 1, gameHeight + 1);

        g.fillRect(0, 0, gameWidth + 1, offsetY + 1);
        g.fillRect(0, gameHeight + offsetY, gameWidth + 1, offsetY + 1);

        inputHandler.setScale(scale, offsetX, offsetY);
    }

    //Update
    public void update() {
        inputHandler.update();

        if (!menu.complete) {
            menu.update(inputHandler);
        }
        if (play) {
            //Updates all players
            for (Player updatePlayer : Player.players) {
                updatePlayer.update(inputHandler);
            }
            //Updates current world
            World.worlds.get(World.level).update(inputHandler);
        }
    }

    //Run Loop
    @Override
    public void run() {
        //Infinite loop
        while (true) {
            //Update
            update();
            repaint();
            try {
                //Sleep for appropriate time to mantain FPS and allow CPU to chill
                Thread.sleep((int) (1000.0 / FPS));
            } catch (InterruptedException e) {
                System.out.println("HoneySuckle ERROR: Failed to delay loop.");
            }
        }
    }

    //Adds/removes keys down to keyDown variable
    @Override
    public void keyPressed(KeyEvent e) {
        inputHandler.keyDown[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        inputHandler.keyDown[e.getKeyCode()] = false;
    }

    //Tracks mouse movements and events
    @Override
    public void mouseMoved(MouseEvent e) {
        Point pos = e.getPoint();
        inputHandler.setMousePosition(pos.x, pos.y);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point pos = e.getPoint();
        inputHandler.setMousePosition(pos.x, pos.y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        inputHandler.click[e.getButton()] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        inputHandler.click[e.getButton()] = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        inputHandler.mouseScroll += e.getPreciseWheelRotation() * 1;
    }

    //Unused methods implemented
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
