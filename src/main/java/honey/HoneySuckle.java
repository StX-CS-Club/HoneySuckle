
package honey;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Taskbar;
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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import honey.mechanics.AssetManager;
import honey.mechanics.ConfigManager;
import honey.mechanics.DataManager;
import honey.mechanics.FileManager;
import honey.mechanics.GameRandom;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Menu;
import honey.world.Entity;
import honey.world.World;

//Main class, extends JPanel for graphics, implements runnable and listeners
public final class HoneySuckle extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public static ConfigManager config;

    //Static variable referencing inputs
    private static final InputHandler inputHandler = new InputHandler();

    //Main player
    public static Player player;
    public static Menu menu = new Menu(Menu.MenuType.MAIN_MENU);

    public static boolean running = false;
    public static boolean rendering = false;

    //Public set of entities with visible health bars
    public static Set<Entity> healthBars = new LinkedHashSet<>();

    //Main Method
    public static void main(String[] args) {
        config = FileManager.readConfig();
        config.distribute();

        //Creates the window
        final JFrame frame = new JFrame("HoneySuckle");
        final HoneySuckle panel = new HoneySuckle();
        panel.register();

        //Trys to set window icon as logo
        final URL iconUrl = HoneySuckle.class.getResource("/images/HoneySuckleIcon.png");
        try {
            final Image iconImage = ImageIO.read(iconUrl);
            frame.setIconImage(iconImage);
            //setIconImage only sets the title-bar icon; macOS ignores it and needs the Dock icon set separately
            if (Taskbar.isTaskbarSupported()) {
                final Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(iconImage);
                }
            }
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
        final Thread gameThread = new Thread(panel);
        gameThread.start();
    }

    //Game Constructor
    public HoneySuckle() {
        //Creates window
        setPreferredSize(new Dimension(config.gameWidth, config.gameHeight));
        setFocusable(true);
        requestFocusInWindow();

        //Fetches all data from json files into appropriate hashmaps
        DataManager.readJsonData();
        DataManager.formatBiomeGeneration();
        DataManager.formatStructureData();
        AssetManager.registerFont();
        AssetManager.preloadImages();
    }

    public void register() {
        //Adds event listeners to window
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    public static void stop() {
        running = false;
        rendering = false;
        World.worlds.clear();
        World.level = 0;
        World.pendingNextWorld = null;
        Player.players.clear();
        healthBars.clear();
    }

    public static void start() {
        GameRandom.newSeed();
        //Creates world 1
        final World world = new World(config.startingBiome);
        World.worlds.add(world);
        //Creates main player in reference to world 1
        player = new Player(new double[]{config.tileSize * (world.start[0] + 0.5), config.tileSize * (world.start[1] + 0.5)},
                (int) (config.tileSize * 0.75), config.playerTags, world);
        Player.players.add(player);
        running = true;
        rendering = true;
    }

    public static void play() {
        running = true;
        rendering = true;
    }

    public static void pause() {
        running = false;
    }

    public static void beginLevelTransition() {
        pause();
        final int targetLevel = World.level + 1;
        menu.setMenuType(Menu.MenuType.LOADING_MENU);
        new Thread(() -> {
            final World world = new World(targetLevel);
            World.pendingNextWorld = world;
        }).start();
    }

    //Render
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Converts Graphics to Graphics2D for more methods
        final BufferedImage internalFrame = new BufferedImage(
                config.gameWidth, config.gameHeight, BufferedImage.TYPE_INT_ARGB
        );
        final Graphics2D g2d = (Graphics2D) internalFrame.getGraphics();

        if (rendering) {
            player.renderPOV(g2d);
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
        final double width = getWidth();
        final double height = getHeight();

        final double scaleX = width / (double) config.gameWidth;
        final double scaleY = height / (double) config.gameHeight;

        final double scale = Math.min(scaleX, scaleY);

        final int offsetX = (int) Math.floor((width - config.gameWidth * scale) / 2);
        final int offsetY = (int) Math.floor((height - config.gameHeight * scale) / 2);

        final int gameWidth = (int) Math.floor(config.gameWidth * scale);
        final int gameHeight = (int) Math.floor(config.gameHeight * scale);
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

        if (inputHandler.keyPressed(KeyEvent.VK_ESCAPE)) {
            if (menu.menuType == Menu.MenuType.PAUSE_MENU && !menu.complete) {
                //Pause menu is currently showing -> resume
                play();
                menu.complete = true;
            } else if (running && menu.menuType != Menu.MenuType.GAME_OVER_MENU && menu.menuType != Menu.MenuType.RESTART_MENU) {
                //Actively playing (not mid game-over/restart sequence) -> open pause menu
                pause();
                menu.setMenuType(Menu.MenuType.PAUSE_MENU);
            }
        }

        if (!menu.complete) {
            menu.update(inputHandler);
        }
        if (running) {
            //Updates all players
            for (Player updatePlayer : Player.players) {
                updatePlayer.update(inputHandler);
            }
            //Updates current world
            World.getCurrentWorld().update(inputHandler);
        }
    }

    //Run Loop
    @Override
    public void run() {
        //Infinite loop
        while (true) {
            final long startTime = System.currentTimeMillis();
            //Update
            update();
            repaint();
            try {
                //Sleep for appropriate time to mantain FPS and allow CPU to chill
                Thread.sleep((int) Math.max(0, 1000.0 / config.fps - (System.currentTimeMillis() - startTime)));
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
