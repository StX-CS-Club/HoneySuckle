
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Projectile.java *
 - Class for managing projectiles
 - Contains static json data
 */
public class Projectile {
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static json data
    public static final Map<String, List<String>> projTags = new HashMap<>();
    public static final Map<String, Map<String, Double>> projAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> projTextures = new HashMap<>();

    //Projectile properties
    public double[] pos;
    public double[] vel;
    public final double size;
    public double angle;
    public final double weight;
    public final String type;

    //Source of projectile
    public Object source;

    //Specific projectile attributes
    private final Map<String, String> texture;
    private final Map<String, Double> attributes;
    private final List<String> tags;

    //Projectile Constructor
    public Projectile(String type, double[] pos, double[] currentVel, double angle, Object source) {
        //Interprets projectile type
        texture = projTextures.get(type);
        attributes = projAttributes.get(type);
        tags = projTags.get(type);

        //Trig variables
        double sin = Math.sin(Math.toRadians(-angle)) * -attributes.get("speed");
        double cos = Math.cos(Math.toRadians(-angle)) * -attributes.get("speed");

        //Calculate velocity based on angle and magnitude
        vel = new double[]{sin, cos};

        vel[0] += currentVel[0];
        vel[1] += currentVel[1];

        //Assign values to properties
        this.pos = pos;
        this.type = type;
        this.angle = angle;
        this.source = source;
        weight = attributes.get("weight");
        size = attributes.get("size")*HoneySuckle.TILE_SIZE;
    }

    //Change velocity of projectile
    public void alterVel(double[] pos, double[] currentVel, double angle, double velCoef, Object source) {
        //Trig variables
        double sin = Math.sin(Math.toRadians(-angle)) * velCoef * -attributes.get("speed");
        double cos = Math.cos(Math.toRadians(-angle)) * velCoef * -attributes.get("speed");

        //Reset shit
        vel = new double[]{sin, cos};
        vel[0] += currentVel[0];
        vel[1] += currentVel[1];
        this.angle = angle;
        this.source = source;
    }

    //Update Projectile
    public void update() {
        //Move Projectile
        pos[0] += vel[0];
        pos[1] += vel[1];
        //Current world data
        World world = World.worlds.get(World.level);
        int[] posIndex = new int[]{
            (int) Math.floor(pos[0] /TILE_SIZE),
            (int) Math.floor(pos[1] /TILE_SIZE)
        };

        //If projectile is outa here, remove it
        if (posIndex[0] < 0 || posIndex[0] >= world.size[0] || posIndex[1] < 0 || posIndex[1] >= world.size[1]) {
            world.projectiles.remove(this);
        } else {
            //World object of projectile
            WorldObject object = world.objGrid[posIndex[0]][posIndex[1]];
            if (object != null) {
                //If can damage tile, apply damage
                if (tags.contains("damageTile")) {
                    //If failed to destroy object, remove projectile
                    if (!object.damage(attributes.get("damage"))) {
                        if (object.tags.contains("projObstruction")) {
                            world.projectiles.remove(this);
                        }
                    }
                } else {
                    //If object blocks projectile, remove it
                    if (object.tags.contains("projObstruction")) {
                        world.projectiles.remove(this);
                    }
                }
            }
        }
        //If can hurt entity, check entities to hurt
        if (tags.contains("hurtEntity")) {
            for (Entity entity : world.renderEntities) {
                //If can hurt source or is not source...
                if (source != entity || tags.contains("hurtSelf")) {
                    //Check collision
                    if (Collision.isBoxOverlap(
                            new Point2D.Double(pos[0], pos[1]),
                            new Point2D.Double(size, size),
                            angle,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        //damage entity, and remove self
                        if (entity.damage(attributes.get("damage"))) {
                            if(source instanceof Player){
                                final Player player = (Player) source;
                                for (Map<String, Number> loot : entity.loot) {
                                    player.inventory.addItem(loot);
                                }
                            }
                        }
                        world.projectiles.remove(this);
                        break;
                    }
                }
            }
        }
        //If can hurt player, check players to hurt
        if (tags.contains("hurtPlayer")) {
            for (Player player : Player.players) {
                //If can hurt source or is not source...
                if (source != player || tags.contains("hurtSelf")) {
                    if (Collision.isBoxOverlap(
                            new Point2D.Double(pos[0], pos[1]),
                            new Point2D.Double(size, size),
                            angle,
                            new Point2D.Double(player.pos[0], player.pos[1]),
                            new Point2D.Double(player.size, player.size))) {
                        //damage player, and remove self
                        player.damage(attributes.get("damage"));
                        world.projectiles.remove(this);
                        break;
                    }
                }
            }
        }
    }

    //Render projectile
    public void render(Graphics2D g, double[] camera) {
        //Original rotation
        AffineTransform originalTransform = g.getTransform();

        //Position of proj on screen
        double[] screenPos = new double[]{
           GAME_WIDTH / 2.0 + pos[0] - camera[0],
           GAME_HEIGHT / 2.0 + pos[1] - camera[1]
        };

        //Rotate based off angle
        g.rotate(Math.toRadians(angle), screenPos[0], screenPos[1]);

        //Render projectiles
        g.drawImage(Rendering.texture(texture.get("texture"), "#ffffff"), (int) (screenPos[0] - size / 2.0), (int) (screenPos[1] - size / 2.0), (int) size, (int) size, null);
        
        //Reset rotation
        g.setTransform(originalTransform);
    }
}
