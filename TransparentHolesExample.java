import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class TransparentHolesExample extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the white rectangle
        g2d.setColor(Color.WHITE);
        g2d.fillRect(50, 50, 300, 200);

        // Draw the black rectangle
        g2d.setColor(Color.BLACK);
        g2d.fillRect(50, 50, 300, 200);

        // Create holes in the black rectangle
        // Set the composite to create transparent holes
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1f));
        
        // Draw the shapes to cut out holes
        g2d.fill(new Ellipse2D.Double(70, 70, 100, 100)); // Example hole
        g2d.fill(new Ellipse2D.Double(200, 120, 100, 100)); // Example hole
        
        // Reset the composite to draw normally
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Create and draw the gradient
        GradientPaint gradient = new GradientPaint(50, 50, new Color(0, 0, 0, 0), 
                                                    150, 50, new Color(0, 0, 0, 255), true);
        g2d.setPaint(gradient);
        g2d.fillRect(50, 50, 300, 200); // Fill the black rectangle again with gradient
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Transparent Holes Example");
        TransparentHolesExample panel = new TransparentHolesExample();
        frame.add(panel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
