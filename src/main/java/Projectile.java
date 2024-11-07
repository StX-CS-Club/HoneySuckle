
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projectile {

    public static final Map<String, List<String>> projTags = new HashMap<>();
    public static final Map<String, Map<String, Double>> projAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> projTextures = new HashMap<>();

    public double[] pos;
    public double[] vel;
    public final double size;
    public double angle;
    public final double weight;
    public final String type;

    public Object source;

    private final Map<String, String> texture;
    private final Map<String, Double> attributes;
    private final List<String> tags;

    public Projectile(String type, double[] pos, double angle, Object source) {
        texture = projTextures.get(type);
        attributes = projAttributes.get(type);
        tags = projTags.get(type);

        double sin = Math.sin(Math.toRadians(-angle)) * -attributes.get("speed");
        double cos = Math.cos(Math.toRadians(-angle)) * -attributes.get("speed");

        vel = new double[]{sin, cos};

        this.pos = pos;
        this.type = type;
        this.angle = angle;
        this.source = source;
        weight = attributes.get("weight");
        size = attributes.get("size");
    }

    public void alterVel(double[] pos, double angle, double velCoef, Object source) {
        double sin = Math.sin(Math.toRadians(-angle)) * velCoef * -attributes.get("speed");
        double cos = Math.cos(Math.toRadians(-angle)) * velCoef * -attributes.get("speed");

        vel = new double[]{sin, cos};

        this.angle = angle;
        this.source = source;
    }

    public void update() {
        pos[0] += vel[0];
        pos[1] += vel[1];
        World world = World.worlds.get(World.level);
        int[] posIndex = new int[]{
            (int) Math.floor(pos[0] / HoneySuckle.tileSize),
            (int) Math.floor(pos[1] / HoneySuckle.tileSize)
        };
        if (posIndex[0] < 0 || posIndex[0] >= world.size[0] || posIndex[1] < 0 || posIndex[1] >= world.size[1]) {
            world.projectiles.remove(this);
        } else {
            WorldObject object = world.objGrid[posIndex[0]][posIndex[1]];
            if (object != null) {
                if (tags.contains("damageTile")) {
                    if (!object.damage(attributes.get("damage"))) {
                        if (object.tags.contains("projObstruction")) {
                            world.projectiles.remove(this);
                        }
                    }
                } else {
                    if (object.tags.contains("projObstruction")) {
                        world.projectiles.remove(this);
                    }
                }
            }
        }
        if (tags.contains("hurtEntity")) {
            for (Entity entity : world.renderEntities) {
                if (source != entity || tags.contains("hurtSelf")) {
                    if (Collision.isBoxOverlap(
                            new Point2D.Double(pos[0], pos[1]),
                            new Point2D.Double(size, size),
                            angle,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        entity.damage(attributes.get("damage"));
                        world.projectiles.remove(this);
                        break;
                    }
                }
            }
        }
        if (tags.contains("hurtPlayer")) {
            for (Player player : Player.players) {
                if (source != player || tags.contains("hurtSelf")) {
                    if (Math.abs(player.pos[0] - pos[0]) <= HoneySuckle.tileSize * size / 2 && Math.abs(player.pos[1] - pos[1]) <= HoneySuckle.tileSize * size / 2) {
                        player.health -= attributes.get("damage");
                        world.projectiles.remove(this);
                        break;
                    }
                }
            }
        }
    }

    public void render(Graphics2D g, double[] camera) {
        AffineTransform originalTransform = g.getTransform();
        double[] screenPos = new double[]{
            HoneySuckle.size[0] / 2 + pos[0] - camera[0],
            HoneySuckle.size[1] / 2 + pos[1] - camera[1]
        };
        double size = attributes.get("size") * HoneySuckle.tileSize;

        g.rotate(Math.toRadians(angle), screenPos[0], screenPos[1]);
        g.drawImage(Rendering.texture(texture.get("texture"), "#ffffff"), (int) (screenPos[0] - size / 2), (int) (screenPos[1] - size / 2), (int) size, (int) size, null);
        g.setTransform(originalTransform);
    }
}
