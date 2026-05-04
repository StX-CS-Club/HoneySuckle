package honey.world;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import honey.HoneySuckle;
import honey.mechanics.Collision;
import honey.player.Player;
import honey.rendering.Rendering;

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
    public static final Map<String, List<Map<String, Number>>> projSplinters = new HashMap<>();
    public static final Map<String, Integer> projIntId = new HashMap<>();
    public static final Map<Integer, String> projStringId = new HashMap<>();

    //Projectile properties
    public double[] pos;
    public double[] vel;
    private int hits;
    private int frames;
    public final double size;
    private final double baseSpeed;
    public double angle;
    private double damage;
    public final double weight;
    public final String type;

    //Source of projectile
    public Object source;

    //Specific projectile attributes
    private final Map<String, String> texture;
    public final Map<String, Number> attributes;
    private final String anim;
    private final List<String> tags;
    private final List<Map<String, Number>> splinters;
    private final int glowColor;

    private BufferedImage staticTexture;

    private boolean flaming = false;

    private final Set<Entity> hitEntities = new HashSet<>();
    private final double circumradius;

    //Projectile Constructor
    public Projectile(String type, double[] pos, double[] currentVel, double angle, Object source) {
        //Interprets projectile type
        texture = projTextures.get(type);
        anim = texture.getOrDefault("anim", "");
        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }

        attributes = projAttributes.get(type);
        tags = projTags.get(type);
        splinters = projSplinters.get(type);

        baseSpeed = attributes.getOrDefault("speed", 0.25).doubleValue() * TILE_SIZE;

        //Trig variables
        double sin = Math.sin(Math.toRadians(-angle)) * -baseSpeed;
        double cos = Math.cos(Math.toRadians(-angle)) * -baseSpeed;

        //Calculate velocity based on angle and magnitude
        vel = new double[]{sin, cos};

        vel[0] += currentVel[0];
        vel[1] += currentVel[1];

        hits = attributes.getOrDefault("hits", 1).intValue();
        frames = attributes.getOrDefault("frames", -1).intValue();

        //Assign values to properties
        this.pos = pos.clone();
        this.type = type;
        this.angle = angle;
        this.source = source;
        weight = attributes.getOrDefault("weight", 1.0).doubleValue();
        size = attributes.getOrDefault("size", 0.5).doubleValue() * HoneySuckle.TILE_SIZE;
        circumradius = size / 2.0 * Math.sqrt(2);
        damage = attributes.getOrDefault("damage", 0.25).doubleValue();

        staticTexture = getTexture(getPostfix());
    }

    public Projectile(Map<String, Number> attributes, double[] pos, double[] currentVel, double angle, Object source) {
        type = projStringId.get(attributes.get("proj").intValue());

        //Interprets projectile type
        texture = projTextures.get(type);
        anim = texture.getOrDefault("anim", "");
        String glowColorString = texture.get("glowColor");
        if (glowColorString != null) {
            glowColor = Integer.parseInt(glowColorString.substring(1), 16);
        } else {
            glowColor = 0;
        }

        tags = projTags.get(type);
        splinters = projSplinters.get(type);

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

        hits = attributes.getOrDefault("hits", 1).intValue();
        frames = attributes.getOrDefault("frames", -1).intValue();

        //Assign values to properties
        this.pos = pos.clone();
        this.angle = angle;
        this.source = source;
        weight = attributes.getOrDefault("weight", 1.0).doubleValue();
        size = attributes.getOrDefault("size", 0.5).doubleValue() * HoneySuckle.TILE_SIZE;
        circumradius = size / 2.0 * Math.sqrt(2);
        damage = attributes.getOrDefault("damage", 0.25).doubleValue();

        staticTexture = getTexture(getPostfix());
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
        if (!tags.contains("stationary")) {
            pos[0] += vel[0];
            pos[1] += vel[1];
        }
        //Current world data
        World world = World.worlds.get(World.level);
        int[] posIndex = new int[]{
            (int) Math.floor(pos[0] / TILE_SIZE),
            (int) Math.floor(pos[1] / TILE_SIZE)
        };

        frames--;
        if (frames == 0) {
            hits = 1;
            destroy(world);
        }

        //If projectile is outa here, remove it
        if (posIndex[0] < 0 || posIndex[0] >= world.size[0] || posIndex[1] < 0 || posIndex[1] >= world.size[1]) {
            destroy(world);
        } else {
            //World object of projectile
            for (int x = (int) Math.floor(posIndex[0] - size); x < posIndex[0] + size; x++) {
                if (x > -1 && x < world.size[0]) {
                    for (int y = (int) Math.floor(posIndex[1] - size); y < posIndex[1] + size; y++) {
                        if (y > -1 && y < world.size[1]) {
                            if (Collision.isBoxOverlap(
                                    new Point2D.Double(pos[0], pos[1]),
                                    new Point2D.Double(size, size),
                                    angle,
                                    new Point2D.Double(TILE_SIZE * (x + 0.5), TILE_SIZE * (y + 0.5)),
                                    new Point2D.Double(TILE_SIZE * 0.5, TILE_SIZE * 0.5))) {
                                WorldObject object = world.objGrid[x][y];
                                if (object != null) {
                                    final double oc = object.attributes.getOrDefault("projCollision", 0).doubleValue();
                                    if (oc > 0 && Collision.isBoxOverlap(
                                            new Point2D.Double(pos[0], pos[1]),
                                            new Point2D.Double(size, size),
                                            angle,
                                            new Point2D.Double(TILE_SIZE * (x + 0.5), TILE_SIZE * (y + 0.5)),
                                            new Point2D.Double(TILE_SIZE * oc * 0.5, TILE_SIZE * oc * 0.5))) {
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
                                                if (destroy(world)) {
                                                    return;
                                                }
                                            }
                                        } else {
                                            //If object blocks projectile, remove it
                                            if (destroy(world)) {
                                                return;
                                            }
                                        }
                                    }
                                    if (object.tags.contains("flameProj") && !flaming) {
                                        damage *= attributes.getOrDefault("flame", 1.5).doubleValue();
                                        flaming = true;
                                        staticTexture = getTexture(getPostfix());
                                    }
                                }

                                Tile tile = world.grid[posIndex[0]][posIndex[1]];
                                if (tile.attributes.getOrDefault("projCollision", 0).doubleValue() > 0) {
                                    if (destroy(world)) {
                                        return;
                                    }
                                }
                                if (tile.tags.contains("flameProj") && !flaming) {
                                    damage *= attributes.getOrDefault("flame", 1.5).doubleValue();
                                    flaming = true;
                                    staticTexture = getTexture(getPostfix());
                                }
                            }
                        }
                    }
                }
            }
        }
        //If can hurt entity, query collision grid for nearby candidates
        if (tags.contains("hurtEntity")) {
            final int px0 = Math.max(0, (int) Math.floor((pos[0] - circumradius) / TILE_SIZE));
            final int px1 = Math.min(world.size[0] - 1, (int) Math.floor((pos[0] + circumradius) / TILE_SIZE));
            final int py0 = Math.max(0, (int) Math.floor((pos[1] - circumradius) / TILE_SIZE));
            final int py1 = Math.min(world.size[1] - 1, (int) Math.floor((pos[1] + circumradius) / TILE_SIZE));
            for (int bx = px0; bx <= px1; bx++) {
                for (int by = py0; by <= py1; by++) {
                    final List<Entity> bucket = world.entityGrid.get(bx * world.size[1] + by);
                    if (bucket == null) continue;
                    for (Entity entity : bucket) {
                        if (hitEntities.contains(entity)) continue;
                        //If can hurt source or is not source...
                        if (source != entity || tags.contains("hurtSource")) {
                            //Check collision
                            if (Collision.isBoxOverlap(
                                    new Point2D.Double(pos[0], pos[1]),
                                    new Point2D.Double(size, size),
                                    angle,
                                    new Point2D.Double(entity.pos[0], entity.pos[1]),
                                    new Point2D.Double(entity.size, entity.size))) {
                                hitEntities.add(entity);
                                //damage entity, and remove self
                                if (entity.brain.damage(damage)) {
                                    if (source instanceof Player) {
                                        final Player player = (Player) source;
                                        for (Map<String, Number> loot : entity.loot) {
                                            player.inventory.incrementItem(loot, true);
                                        }
                                    }
                                }
                                if (destroy(world)) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        //If can hurt player, check players to hurt
        if (tags.contains("hurtPlayer")) {
            for (Player player : Player.players) {
                //If can hurt source or is not source...
                if (source != player || tags.contains("hurtSource")) {
                    if (Collision.isBoxOverlap(
                            new Point2D.Double(pos[0], pos[1]),
                            new Point2D.Double(size, size),
                            angle,
                            new Point2D.Double(player.pos[0], player.pos[1]),
                            new Point2D.Double(player.size, player.size))) {
                        //damage player, and remove self
                        player.damage(damage, true);
                        if (destroy(world)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    //Render projectile
    public void render(Graphics2D g, double[] screenPos) {
        //Original rotation
        AffineTransform originalTransform = g.getTransform();

        //Rotate based off angle
        g.rotate(Math.toRadians(angle), screenPos[0], screenPos[1]);

        //Render projectiles
        g.drawImage(staticTexture, (int) (screenPos[0] - size / 2.0), (int) (screenPos[1] - size / 2.0), (int) size, (int) size, null);

        //Reset rotation
        g.setTransform(originalTransform);
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", screenPos[0] + TILE_SIZE / 2,
                "posY", screenPos[1] + TILE_SIZE / 2,
                "radius", attributes.getOrDefault("lightRadius", 0),
                "color", glowColor,
                "glow", attributes.getOrDefault("glow", 0),
                "glowRadius", attributes.getOrDefault("glowRadius", 0)
        ));
    }

    private BufferedImage getTexture(String postfix) {
        return Rendering.texture(texture.get("texture") + postfix, null);
    }

    private String getPostfix() {
        StringBuilder postfix = new StringBuilder();

        if (anim.contains("_flame_")) {
            if (flaming) {
                postfix.append("_flame");
            }
        }

        return postfix.toString();
    }

    private boolean destroy(World world) {
        hits--;
        if (hits == 0) {
            world.projectiles.remove(this);

            for (Map<String, Number> splinter : splinters) {
                world.projectiles.add(new Projectile(splinter, pos, vel, angle, source));
            }
            return true;
        }
        return false;
    }
}
