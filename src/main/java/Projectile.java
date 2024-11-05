
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projectile {

    public static final Map<String, List<String>> projTags = new HashMap<>();
    public static final Map<String, Map<String, Double>> projAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> projTextures = new HashMap<>();

    public double[] pos;
    private double[] vel;
    private final double angle;
    private final String type;
    private final Map<String, String> texture;
    private final Map<String, Double> attributes;
    private final List<String> tags;

    public Projectile(String type, double[] pos, double angle) {
        int quarter = (int) Math.floor(angle / 90);
        if (quarter > 3) {
            quarter = 0;
        }
        double sin = Math.sin(Math.toRadians(angle - quarter * 90)) * 10;
        double cos = Math.cos(Math.toRadians(angle - quarter * 90)) * 10;
        switch (quarter) {
            case 0 -> {
                vel = new double[]{sin, -cos};
            }
            case 1 -> {
                vel = new double[]{cos, sin};
            }
            case 2 -> {
                vel = new double[]{-sin, cos};
            }
            case 3 -> {
                vel = new double[]{-cos, -sin};
            }
        }

        this.pos = pos;
        this.type = type;
        this.angle = angle;
        texture = projTextures.get(type);
        attributes = projAttributes.get(type);
        tags = projTags.get(type);
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
                if (object.tags.contains("projObstruction")) {
                    world.projectiles.remove(this);
                }
            }
        }
        if (tags.contains("hurtEnemy")) {
            for (Entity entity : world.renderEntities) {
                if (Math.abs(entity.pos[0] - pos[0]) <= HoneySuckle.tileSize * attributes.get("size") / 2 && Math.abs(entity.pos[1] - pos[1]) <= HoneySuckle.tileSize * attributes.get("size") / 2) {
                    entity.damage(attributes.get("damage"));
                    world.projectiles.remove(this);
                    break;
                }
            }
        }
        if (tags.contains("hurtPlayer")) {
            for (Player player : Player.players) {
                if (Math.abs(player.pos[0] - pos[0]) <= HoneySuckle.tileSize * attributes.get("size") / 2 && Math.abs(player.pos[1] - pos[1]) <= HoneySuckle.tileSize * attributes.get("size") / 2) {
                    player.health -= attributes.get("damage");
                    world.projectiles.remove(this);
                    break;
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
