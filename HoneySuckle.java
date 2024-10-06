
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

public class HoneySuckle extends JPanel implements Runnable, KeyListener {

    public static int tileSize = 40;
    public static int fps = 40;
    public static int[] size = new int[]{800, 600};

    private Player player;

    public static boolean[] keyDown = new boolean[100];

    public HoneySuckle() {
        setPreferredSize(new Dimension(size[0], size[1]));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        World world = new World();
        player = new Player(new double[]{HoneySuckle.tileSize * (world.start + 0.5), HoneySuckle.tileSize * (world.size[1]-0.5)},
        tileSize*2/3, Arrays.asList("leader"));
    }

    //Render
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        World.worlds.get(World.level).render(g);
        player.render(g);
    }

    public void update() {
        player.update(keyDown);

        repaint();
    }

    @Override
    public void run() {
        while (true) {
            update();
            try {
                Thread.sleep((int)(1000/fps));
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
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HoneySuckle");
        HoneySuckle panel = new HoneySuckle();

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Thread gameThread = new Thread(panel);
        gameThread.start();
    }
}
