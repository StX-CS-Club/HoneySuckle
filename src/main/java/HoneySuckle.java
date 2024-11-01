import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

public class HoneySuckle extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

    public static final int tileSize = 40;
    public static final int fps = 40;
    public static int[] size = new int[]{800, 600};

    public static boolean[] keyDown = new boolean[100];

    public static double[] mousePos = new double[2];
    public static int click = 0;

    public static double scroll = 0;

    public static Player player;

    public static Set<Map<String, Integer>> lights = new LinkedHashSet<>();

    @SuppressWarnings({ "unchecked" })
    public HoneySuckle() throws URISyntaxException {
        setPreferredSize(new Dimension(size[0], size[1]));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
            };
            Map<String, Object> objData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/obj.json").toURI()), mapType);
            for(String key : objData.keySet()){
                int intKey = Integer.parseInt(key);
                Map<String, Object> obj = (Map<String, Object>) objData.get(key);
                WorldObject.objLoot.put(intKey, (Map<String, Integer>) obj.get("loot"));
                WorldObject.objTextures.put(intKey, (Map<String, String>) obj.get("texture"));
                WorldObject.objValues.put(intKey, (Map<String, Integer>) obj.get("values"));
                WorldObject.objTags.put(intKey, (List<String>) obj.get("tags"));
            }

            Map<String, Object> tileData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/tile.json").toURI()), mapType);
            for(String key : tileData.keySet()){
                int intKey = Integer.parseInt(key);
                Map<String, Object> tile = (Map<String, Object>) tileData.get(key);
                Tile.tileTextures.put(intKey, (Map<String, String>) tile.get("texture"));
                Tile.tileValues.put(intKey, (Map<String, Integer>) tile.get("values"));
                Tile.tileTags.put(intKey, (List<String>) tile.get("tags"));
            }

            Map<String, Object> recipeData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/recipe.json").toURI()), mapType);
            for(String key : recipeData.keySet()){
                int intKey = Integer.parseInt(key);
                Map<String, Object> recipe = (Map<String, Object>) recipeData.get(key);
                Craft.recipeMats.put(intKey, (Map<String, Integer>) recipe.get("mats"));
                Craft.recipeParams.put(intKey, (Map<String, List<Integer>>) recipe.get("params"));
            }

            Map<String, Object> biomeData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/biome.json").toURI()), mapType);
            for(String key : biomeData.keySet()){
                Map<String, Object> biome = (Map<String, Object>) biomeData.get(key);
                Biome.biomeColorMap.put(key, (Map<String, String>) biome.get("colorMap"));
                Biome.biomeTags.put(key, (List<String>) biome.get("tags"));
            }

            Map<String, Object> entityData = objectMapper.readValue(new File(HoneySuckle.class.getResource("jsonData/entity.json").toURI()), mapType);
            for(String key : entityData.keySet()){
                Map<String, Object> entity = (Map<String, Object>) entityData.get(key);
                Entity.entityAttributes.put(key, (Map<String, Double>) entity.get("attributes"));
                Entity.entityTextures.put(key, (Map<String, String>) entity.get("texture"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        World world = new World();
        player = new Player(new double[]{HoneySuckle.tileSize * (world.start + 0.5), HoneySuckle.tileSize * (world.size[1] - 0.5)},
                tileSize / 2, Arrays.asList("leader"));
    }

    //Render
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        lights = new LinkedHashSet<>();

        Graphics2D g2d = (Graphics2D) g;

        World.worlds.get(World.level).render(g2d);
        for (Player renderPlayer : Player.players) {
            renderPlayer.render(g2d, mousePos);
        }

        String biome = World.worlds.get(World.level).biome;
        if (Biome.biomeTags.get(biome).contains("fog")) {
            Color fogColor = Color.decode(Biome.biomeColorMap.get(biome).get("fogColor"));
            Rendering.renderLight(g2d, fogColor, lights);
        }
        
        Rendering.colorFade(g2d, Color.red, 1-player.health);

        g2d.dispose();
    }

    public void update() {
        for (Player updatePlayer : Player.players) {
            updatePlayer.update(keyDown, mousePos, click, scroll);
        }
        World.worlds.get(World.level).update();

        click = 0;
        if (Math.abs(scroll) > 2) {
            scroll = Math.signum(scroll) * 2;
        }
        scroll += -Math.signum(scroll) * 0.8;

        repaint();
    }

    @Override
    public void run() {
        while (true) {
            update();
            try {
                Thread.sleep((int) (1000 / fps));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        JFrame frame = new JFrame("HoneySuckle");
        HoneySuckle panel = new HoneySuckle();

        try {
            Image iconImage = ImageIO.read(HoneySuckle.class.getResource("images/HoneySuckleIcon.png"));
            frame.setIconImage(iconImage);
        } catch (IOException e) {
        }

        frame.add(panel);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension d = frame.getSize();
                size = new int[]{d.width, d.height - tileSize};
                panel.setSize(d);
            }
        });
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Thread gameThread = new Thread(panel);
        gameThread.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyDown[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyDown[e.getKeyCode()] = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point pos = e.getPoint();
        mousePos = new double[]{pos.x, pos.y};
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        click = e.getButton();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scroll += e.getPreciseWheelRotation() * 1;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
