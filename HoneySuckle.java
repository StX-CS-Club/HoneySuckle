
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

public class HoneySuckle extends JPanel implements Runnable, KeyListener {

    public static int tileSize = 40;
    public static int fps = 40;
    public static int[] size = new int[]{800, 600};

    public static boolean[] keyDown = new boolean[100];

    public HoneySuckle() {
        setPreferredSize(new Dimension(size[0], size[1]));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        World world = new World();
        new Player(new double[]{HoneySuckle.tileSize * (world.start + 0.5), HoneySuckle.tileSize * (world.size[1]-0.5)},
        tileSize*2/3, Arrays.asList("leader", "wasd"));
    }

    //Render
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        World.worlds.get(World.level).render(g2d);
        for(int i = 0; i < Player.players.size(); i++){
            Player.players.get(i).render(g2d);
        }
    }

    public static void borderRect(Graphics2D g, int border, int x, int y, int width, int height){
        g.fillRect(x, y, width, height);

        g.setStroke(new BasicStroke(border));
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }

    public void update() {
        for(int i = 0; i < Player.players.size(); i++){
            Player.players.get(i).update(keyDown);
        }

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
