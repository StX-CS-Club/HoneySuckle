
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/* 
 * HoneySuckle.java *
 - Main Class
 -Static variables and constants
 - Creates window and canvas, centerlizes rendering and updating
 */
//Main class, extends JPanel for graphics, implements runnable and listeners
public class HoneySuckle extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    //Static variables referenced throughout code
    public static int tileSize = 40;
    public static final int fps = 30;
    public static int[] size = new int[]{800, 600};

    //Static variable referencing inputs
    private static final Input input = new Input();

    //Main player
    public static Player player;

    //Public set of light data used in lighting system
    public static Set<Map<String, Integer>> lights = new LinkedHashSet<>();

    //Main Method
    public static void main(String[] args) {
        //Creates the windo
        JFrame frame = new JFrame("HoneySuckle");
        HoneySuckle panel = new HoneySuckle();

        //Trys to set window icon as logo
        URL iconUrl = HoneySuckle.class.getResource("images/HoneySuckleIcon.png");
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

        // Load the font from a file
        URL fontUrl = HoneySuckle.class.getResource("fonts/VT323/VT323-Regular.ttf");
        try {
            File fontFile = new File(fontUrl.toURI());
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(Font.PLAIN, 24);

            // Register the font with the graphics environment
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

            //Register font variable to be referenced later
            Rendering.fonts.put("VT323", font);
        } catch (IOException e) {
            System.out.println("HoneySuckle ERROR: Failed to import text font.");
        } catch (URISyntaxException e1) {
            System.out.println("HoneySuckle ERROR: Could not find text font.");
        } catch (FontFormatException e1) {
            System.out.println("HoneySuckle ERROR: Failed to load text font.");
        }

        //Appends rendering to window
        frame.add(panel);
        //Adds resize listener to allow resizing rendering canvas
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension d = frame.getSize();
                size = new int[]{d.width - 14, d.height - 37};
                panel.setSize(d);
            }
        });
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
        setPreferredSize(new Dimension(size[0], size[1]));
        setFocusable(true);
        requestFocusInWindow();
        //Adds event listeners to window
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        //Fetches all data from json files into appropriate hashmaps
        getJson();

        //Creates world 1
        World world = new World();
        //Creates main player in reference to world 1
        player = new Player(new double[]{HoneySuckle.tileSize * (world.start + 0.5), HoneySuckle.tileSize * (world.size[1] - 0.5)},
                (int) (tileSize * 0.75), Arrays.asList("leader"));
    }

    //Render
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //Resets lights every frame
        lights = new LinkedHashSet<>();

        //Converts Graphics to Graphics2D for more methods
        Graphics2D g2d = (Graphics2D) g;

        //Renders World
        World.worlds.get(World.level).render(g2d);
        //Renders Players
        for (Player renderPlayer : Player.players) {
            renderPlayer.render(g2d);
        }

        String biome = World.worlds.get(World.level).biome;
        //Renders fog, is present
        if (Biome.biomeTags.get(biome).contains("fog")) {
            Color fogColor = Color.decode(Biome.biomeColorMap.get(biome).get("fogColor"));
            Rendering.renderLight(g2d, fogColor, lights);
        }

        //Creates red overlay when at low health
        Rendering.colorFade(g2d, Color.red, 1 - player.health);

        //Renders crafting and weapon ui
        player.build.renderUi(g2d, World.worlds.get(World.level), player);
        player.armory.renderUi(g2d, player);
        player.inventory.renderUi(g2d);

        //Disposes of Graphics and Graphics2D
        g.dispose();
        g2d.dispose();
    }

    //Update
    public void update() {
        input.update();

        //Updates all players
        for (Player updatePlayer : Player.players) {
            updatePlayer.update(input);
        }
        //Updates current world
        World.worlds.get(World.level).update();

        //Renders
        repaint();
    }

    //Run Loop
    @Override
    public void run() {
        //Infinite loop
        while (true) {
            //Update
            update();
            try {
                //Sleep for appropriate time to mantain FPS and allow CPU to chill
                Thread.sleep((int) (1000.0 / fps));
            } catch (InterruptedException e) {
                System.out.println("HoneySuckle ERROR: Failed to delay loop.");
            }
        }
    }

    //Adds/removes keys down to keyDown variable
    @Override
    public void keyPressed(KeyEvent e) {
        input.keyDown[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        input.keyDown[e.getKeyCode()] = false;
    }

    //Tracks mouse movements and events
    @Override
    public void mouseMoved(MouseEvent e) {
        Point pos = e.getPoint();
        input.mousePos[0] = pos.x;
        input.mousePos[1] = pos.y;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point pos = e.getPoint();
        input.mousePos[0] = pos.x;
        input.mousePos[1] = pos.y;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        input.click[e.getButton()] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        input.click[e.getButton()] = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        input.mouseScroll += e.getPreciseWheelRotation() * 1;
    }

    //Fetches data from json files and maps as hashmaps
    @SuppressWarnings("unchecked")
    private void getJson() {
        try {
            //Object used for referencing json files
            ObjectMapper objectMapper = new ObjectMapper();
            //Type reference of json files
            TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
            };

            //Maps object data
            Map<String, Object> objData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/object.json").toURI()), mapType);
            for (String key : objData.keySet()) {
                int intKey = Integer.parseInt(key);
                Map<String, Object> obj = (Map<String, Object>) objData.get(key);
                WorldObject.objLoot.put(intKey, (List<Map<String, Number>>) obj.get("loot"));
                WorldObject.objTextures.put(intKey, (Map<String, String>) obj.get("texture"));
                WorldObject.objValues.put(intKey, (Map<String, Double>) obj.get("values"));
                WorldObject.objTags.put(intKey, (List<String>) obj.get("tags"));
            }

            //Maps tile data
            Map<String, Object> tileData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/tile.json").toURI()), mapType);
            for (String key : tileData.keySet()) {
                int intKey = Integer.parseInt(key);
                Map<String, Object> tile = (Map<String, Object>) tileData.get(key);
                Tile.tileTextures.put(intKey, (Map<String, String>) tile.get("texture"));
                Tile.tileValues.put(intKey, (Map<String, Double>) tile.get("values"));
                Tile.tileTags.put(intKey, (List<String>) tile.get("tags"));
            }

            //Maps recipe data
            Map<String, Object> blueprintData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/blueprint.json").toURI()), mapType);
            for (String key : blueprintData.keySet()) {
                int intKey = Integer.parseInt(key);
                Map<String, Object> recipe = (Map<String, Object>) blueprintData.get(key);
                Build.blueprintMats.put(intKey, (List<Map<String, Integer>>) recipe.get("mats"));
                Build.blueprintParams.put(intKey, (Map<String, List<Integer>>) recipe.get("params"));
                Build.blueprintTextures.put(intKey, (Map<String, String>) recipe.get("texture"));
            }

            //Maps biome data
            Map<String, Object> biomeData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/biome.json").toURI()), mapType);
            for (String key : biomeData.keySet()) {
                Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biomeColorMap.put(key, (Map<String, String>) biome.get("colorMap"));
                Biome.biomeTags.put(key, (List<String>) biome.get("tags"));
            }

            //Maps entity data
            Map<String, Object> entityData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/entity.json").toURI()), mapType);
            for (String key : entityData.keySet()) {
                Map<String, Object> entity = (Map<String, Object>) entityData.get(key);
                Entity.entityAttributes.put(key, (Map<String, Double>) entity.get("attributes"));
                Entity.entityTextures.put(key, (Map<String, String>) entity.get("texture"));
                Entity.entityLoot.put(key, (List<Map<String, Integer>>) entity.get("loot"));
                Entity.entityTags.put(key, (List<String>) entity.get("tags"));
            }

            //Maps weapon data
            Map<String, Object> weaponData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/weapon.json").toURI()), mapType);
            for (String key : weaponData.keySet()) {
                Map<String, Object> weapon = (Map<String, Object>) weaponData.get(key);
                Weapon.weaponAttributes.put(key, (Map<String, Double>) weapon.get("attributes"));
                Weapon.weaponTypes.put(key, (String) weapon.get("type"));
                Weapon.weaponProj.put(key, (String) weapon.get("projectile"));
                Weapon.weaponClick.put(key, (boolean) weapon.get("constClick"));
                Weapon.weaponTextures.put(key, (Map<String, String>) weapon.get("texture"));
            }

            //Maps armor data
            Map<String, Object> armorData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/armor.json").toURI()), mapType);
            for (String key : armorData.keySet()) {
                Map<String, Object> armor = (Map<String, Object>) armorData.get(key);
                Armor.armorTextures.put(key, (Map<String, String>) armor.get("texture"));
                Armor.armorAttributes.put(key, (Map<String, Double>) armor.get("attributes"));
            }

            //Maps Projectile data
            Map<String, Object> projData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/projectile.json").toURI()), mapType);
            for (String key : projData.keySet()) {
                Map<String, Object> proj = (Map<String, Object>) projData.get(key);
                Projectile.projAttributes.put(key, (Map<String, Double>) proj.get("attributes"));
                Projectile.projTextures.put(key, (Map<String, String>) proj.get("texture"));
                Projectile.projTags.put(key, (List<String>) proj.get("tags"));
            }
        } catch (IOException e) {
            System.out.println("HoneySuckle ERROR: Failed to import json files.");
        } catch (URISyntaxException e) {
            System.out.println("HoneySuckle ERROR: Could not find json files.");
        }
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
