
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;

public class HoneySuckle extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

    public static final int tileSize = 40;
    public static final int fps = 40;
    public static final int[] size = new int[]{800, 600};

    public static boolean[] keyDown = new boolean[100];

    public static double[] mousePos = new double[2];
    public static int click = 0;

    public static double scroll = 0;

    public static Player player;

    public static Set<Map<String, Integer>> lights = new LinkedHashSet<>();

    public HoneySuckle() {
        setPreferredSize(new Dimension(size[0], size[1]));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("HoneySuckle");
        HoneySuckle panel = new HoneySuckle();

        try {
            Image iconImage = ImageIO.read(HoneySuckle.class.getResource("/assets/HoneySuckleIcon.png"));
            frame.setIconImage(iconImage);
        } catch (IOException e) {
        }

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Thread gameThread = new Thread(panel);
        gameThread.start();
    }
}
