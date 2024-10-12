
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

public class HoneySuckle extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {

    public static final int tileSize = 40;
    public static final int fps = 50;
    public static final int[] size = new int[]{800, 600};

    public static boolean[] keyDown = new boolean[100];

    public static double[] mousePos = new double[2];
    public static int click = 0;

    public HoneySuckle() {
        setPreferredSize(new Dimension(size[0], size[1]));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        World world = new World();
        new Player(new double[]{HoneySuckle.tileSize * (world.start + 0.5), HoneySuckle.tileSize * (world.size[1]-0.5)},
        tileSize*2/3, Arrays.asList("leader", "wasd", "mouse"));
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

    public static void borderRect(Graphics2D g, int border, Color color, int x, int y, int width, int height){
        g.fillRect(x, y, width, height);

        g.setStroke(new BasicStroke(border));
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

    public void update() {
        for(int i = 0; i < Player.players.size(); i++){
            Player.players.get(i).update(keyDown, mousePos, click);
        }

        click = 0;

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
    public void mouseMoved(MouseEvent e){
        Point pos = e.getPoint();
        mousePos = new double[]{pos.x, pos.y};
    }

    @Override
    public void mouseClicked(MouseEvent e){
        click = e.getButton();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e){}

    @Override 
    public void mousePressed(MouseEvent e){}

    @Override
    public void mouseReleased(MouseEvent e){}

    @Override
    public void mouseEntered(MouseEvent e){}

    @Override
    public void mouseExited(MouseEvent e){}

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
