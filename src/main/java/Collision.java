import java.awt.geom.Point2D;

public class Collision {
    public static double[] pointToArray(Point2D.Double point){
        return new double[]{point.x, point.y};
    }

    public static Point2D.Double addAtAngle(Point2D.Double pos, double distance, double angle){
        return new Point2D.Double(
            pos.x - distance * Math.sin(Math.toRadians(-angle)),
            pos.y - distance * Math.cos(Math.toRadians(-angle))
        );
    }

    public static boolean isBoxOverlap(Point2D.Double origin, Point2D.Double size, double angle, Point2D.Double point, Point2D.Double pointSize) {
        // Convert sizes to half-widths for easier calculations
        Point2D.Double halfSize = new Point2D.Double(size.x / 2, size.y / 2);
        Point2D.Double halfPointSize = new Point2D.Double(pointSize.x / 2, pointSize.y / 2);
        
        // Find the corners of the rotated box
        Point2D.Double[] originBoxCorners = getRotatedBoxCorners(origin, halfSize, angle);
        
        // Find the corners of the non-rotated box
        Point2D.Double[] pointBoxCorners = getBoxCorners(point, halfPointSize);
        
        // Check for overlap using SAT (Separating Axis Theorem)
        return checkSAT(originBoxCorners, pointBoxCorners);
    }

    private static Point2D.Double[] getRotatedBoxCorners(Point2D.Double center, Point2D.Double halfSize, double angle) {
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        
        return new Point2D.Double[]{
            new Point2D.Double(center.x + cos * halfSize.x - sin * halfSize.y, center.y + sin * halfSize.x + cos * halfSize.y),
            new Point2D.Double(center.x - cos * halfSize.x - sin * halfSize.y, center.y - sin * halfSize.x + cos * halfSize.y),
            new Point2D.Double(center.x - cos * halfSize.x + sin * halfSize.y, center.y - sin * halfSize.x - cos * halfSize.y),
            new Point2D.Double(center.x + cos * halfSize.x + sin * halfSize.y, center.y + sin * halfSize.x - cos * halfSize.y)
        };
    }

    private static Point2D.Double[] getBoxCorners(Point2D.Double center, Point2D.Double halfSize) {
        return new Point2D.Double[]{
            new Point2D.Double(center.x + halfSize.x, center.y + halfSize.y),
            new Point2D.Double(center.x - halfSize.x, center.y + halfSize.y),
            new Point2D.Double(center.x - halfSize.x, center.y - halfSize.y),
            new Point2D.Double(center.x + halfSize.x, center.y - halfSize.y)
        };
    }

    private static boolean checkSAT(Point2D.Double[] box1, Point2D.Double[] box2) {
        for (int i = 0; i < box1.length; i++) {
            // Get the current edge of box1
            Point2D.Double edge = new Point2D.Double(
                box1[(i + 1) % box1.length].x - box1[i].x,
                box1[(i + 1) % box1.length].y - box1[i].y
            );
            
            // Get the axis perpendicular to the edge
            Point2D.Double axis = new Point2D.Double(-edge.y, edge.x);
            
            // Project both boxes onto the axis
            double[] projection1 = projectOntoAxis(box1, axis);
            double[] projection2 = projectOntoAxis(box2, axis);
            
            // Check if there is no overlap
            if (projection1[1] < projection2[0] || projection2[1] < projection1[0]) {
                return false;
            }
        }
        
        // Repeat the same for box2 edges
        for (int i = 0; i < box2.length; i++) {
            Point2D.Double edge = new Point2D.Double(
                box2[(i + 1) % box2.length].x - box2[i].x,
                box2[(i + 1) % box2.length].y - box2[i].y
            );
            
            Point2D.Double axis = new Point2D.Double(-edge.y, edge.x);
            
            double[] projection1 = projectOntoAxis(box1, axis);
            double[] projection2 = projectOntoAxis(box2, axis);
            
            if (projection1[1] < projection2[0] || projection2[1] < projection1[0]) {
                return false;
            }
        }
        
        return true; // Overlap detected
    }

    private static double[] projectOntoAxis(Point2D.Double[] box, Point2D.Double axis) {
        double min = dotProduct(box[0], axis);
        double max = min;
        
        for (int i = 1; i < box.length; i++) {
            double projection = dotProduct(box[i], axis);
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }
        
        return new double[]{min, max};
    }

    private static double dotProduct(Point2D.Double point1, Point2D.Double point2) {
        return point1.x * point2.x + point1.y * point2.y;
    }
}
