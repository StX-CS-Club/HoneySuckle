
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static final Map<String, Map<String, Number>> projAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> projTextures = new HashMap<>();
    public static final Map<String, Integer> projIntId = new HashMap<>();
    public static final Map<Integer, String> projStringId = new HashMap<>();

    //Projectile properties
    public double[] pos;
    public double[] vel;
    public final double size;
    private final double baseSpeed;
    public double angle;
    private final double damage;
    public final double weight;
    public final String type;

    //Source of projectile
    public Object source;

    //Specific projectile attributes
    private final Map<String, String> texture;
    public final Map<String, Number> attributes;
    private final List<String> tags;

    private final BufferedImage staticTexture;

    //Projectile Constructor
    public Projectile(String type, double[] pos, double[] currentVel, double angle, Object source) {
        //Interprets projectile type
        texture = projTextures.get(type);
        attributes = projAttributes.get(type);
        tags = projTags.get(type);

        baseSpeed = attributes.getOrDefault("speed", 0.25).doubleValue() * TILE_SIZE;

        //Trig variables
        double sin = Math.sin(Math.toRadians(-angle)) * -baseSpeed;
        double cos = Math.cos(Math.toRadians(-angle)) * -baseSpeed;

        //Calculate velocity based on angle and magnitude
        vel = new double[]{sin, cos};

        vel[0] += currentVel[0];
        vel[1] += currentVel[1];

        //Assign values to properties
        this.pos = pos;
        this.type = type;
        this.angle = angle;
        this.source = source;
        weight = attributes.getOrDefault("weight", 1.0).doubleValue();
        size = attributes.getOrDefault("size", 0.5).doubleValue() * HoneySuckle.TILE_SIZE;
        damage = attributes.getOrDefault("damage", 0.25).doubleValue();

        staticTexture = getTexture();
    }

    public Projectile(Map<String, Number> attributes, double[] pos, double[] currentVel, double angle, Object source) {
        type = projStringId.get(attributes.get("proj").intValue());

        //Interprets projectile type
        texture = projTextures.get(type);
        tags = projTags.get(type);

        Set<String> keys = attributes.keySet();
        Map<String, Number> defaultAttributes = projAttributes.get(type);
        for (String key : defaultAttributes.keySet()) {
            if (!keys.contains(key)) {
                attributes.put(key, defaultAttributes.get(key));
            }
        }
        this.attributes = attributes;

        baseSpeed = attributes.getOrDefault("speed", 0.25).doubleValue() * TILE_SIZE;

        //Trig variables
        double sin = Math.sin(Math.toRadians(-angle)) * -baseSpeed;
        double cos = Math.cos(Math.toRadians(-angle)) * -baseSpeed;

        //Calculate velocity based on angle and magnitude
        vel = new double[]{sin, cos};

        vel[0] += currentVel[0];
        vel[1] += currentVel[1];

        //Assign values to properties
        this.pos = pos;
        this.angle = angle;
        this.source = source;
        weight = attributes.getOrDefault("weight", 1.0).doubleValue();
        size = attributes.getOrDefault("size", 0.5).doubleValue() * HoneySuckle.TILE_SIZE;
        damage = attributes.getOrDefault("damage", 0.25).doubleValue();

        staticTexture = getTexture();
    }

    //Change velocity of projectile
    public void alterVel(double[] pos, double[] currentVel, double angle, double velCoef, Object source) {
        //Trig variables
        double sin = Math.sin(Math.toRadians(-angle)) * velCoef * -baseSpeed;
        double cos = Math.cos(Math.toRadians(-angle)) * velCoef * -baseSpeed;

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
            (int) Math.floor(pos[0] / TILE_SIZE),
            (int) Math.floor(pos[1] / TILE_SIZE)
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
                    if (object.damage(damage)) {
                            if (source instanceof Player) {
                                final Player player = (Player) source;
                                for (Map<String, Number> loot : object.loot) {
                                    player.inventory.incrementItem(loot, true);
                                }
                            }
                    } else {
                        if (object.tags.contains("projObstruction")) {
                            world.projectiles.remove(this);
                            return;
                        }
                    }
                } else {
                    //If object blocks projectile, remove it
                    if (object.tags.contains("projObstruction")) {
                        world.projectiles.remove(this);
                        return;
                    }
                }
            }

            Tile tile = world.grid[posIndex[0]][posIndex[1]];
            if (tile.tags.contains("projObstruction")) {
                world.projectiles.remove(this);
                return;
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
                        if (entity.brain.damage(damage)) {
                            if (source instanceof Player) {
                                final Player player = (Player) source;
                                for (Map<String, Number> loot : entity.loot) {
                                    player.inventory.incrementItem(loot, true);
                                }
                            }
                        }
                        world.projectiles.remove(this);
                        return;
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
                        player.damage(damage, true);
                        world.projectiles.remove(this);
                        return;
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
        g.drawImage(staticTexture, (int) (screenPos[0] - size / 2.0), (int) (screenPos[1] - size / 2.0), (int) size, (int) size, null);

        //Reset rotation
        g.setTransform(originalTransform);
    }

    private BufferedImage getTexture() {
        return Rendering.texture(texture.get("texture"), "#ffffff");
    }
}
