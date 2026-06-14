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
import honey.mechanics.ConfigManager;
import honey.player.Player;
import honey.rendering.Rendering;

/*
 * Projectile.java *
 - Class for managing projectiles
 - Contains static json data
 */
public class Projectile {

    public static ConfigManager config;

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
    private int trail;
    public int bounces;

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

        baseSpeed = attributes.getOrDefault("speed", 0.25).doubleValue() * config.tileSize;

        double sin = Math.sin(Math.toRadians(-angle)) * -baseSpeed;
        double cos = Math.cos(Math.toRadians(-angle)) * -baseSpeed;
        vel = new double[]{sin, cos};
        vel[0] += currentVel[0];
        vel[1] += currentVel[1];

        hits = attributes.getOrDefault("hits", 1).intValue();
        frames = attributes.getOrDefault("frames", -1).intValue();

        this.pos = pos.clone();
        this.type = type;
        this.angle = angle;
        this.source = source;
        weight = attributes.getOrDefault("weight", 1.0).doubleValue();
        size = attributes.getOrDefault("size", 0.5).doubleValue() * config.tileSize;
        circumradius = size / 2.0 * Math.sqrt(2);
        damage = attributes.getOrDefault("damage", 0.25).doubleValue();
        trail = attributes.getOrDefault("trailCount", 0).intValue();
        bounces = attributes.getOrDefault("bounceCount", -1).intValue();

        staticTexture = getTexture(getPostfix());
    }

    public Projectile(Map<String, Number> attributes, double[] pos, double[] currentVel, double angle, Object source) {
        type = projStringId.get(attributes.get("proj").intValue());

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

        baseSpeed = attributes.getOrDefault("speed", 0.25).doubleValue() * config.tileSize;

        double sin = Math.sin(Math.toRadians(-angle)) * -baseSpeed;
        double cos = Math.cos(Math.toRadians(-angle)) * -baseSpeed;
        vel = new double[]{sin, cos};
        vel[0] += currentVel[0];
        vel[1] += currentVel[1];

        hits = attributes.getOrDefault("hits", 1).intValue();
        frames = attributes.getOrDefault("frames", -1).intValue();

        this.pos = pos.clone();
        this.angle = angle;
        this.source = source;
        weight = attributes.getOrDefault("weight", 1.0).doubleValue();
        size = attributes.getOrDefault("size", 0.5).doubleValue() * config.tileSize;
        circumradius = size / 2.0 * Math.sqrt(2);
        damage = attributes.getOrDefault("damage", 0.25).doubleValue();
        trail = attributes.getOrDefault("trailCount", 0).intValue();
        bounces = attributes.getOrDefault("bounceCount", -1).intValue();

        staticTexture = getTexture(getPostfix());
    }

    //Change velocity of projectile
    public void alterVel(double[] pos, double[] currentVel, double angle, double velCoef, Object source) {
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
        final World world = World.worlds.get(World.level);
        final int trailCount = attributes.getOrDefault("trailCount", 0).intValue();
        final int steps = attributes.getOrDefault("steps", 1).intValue();

        frames--;
        if (frames == 0) {
            destroy(world);
            return;
        }

        if (!tags.contains("stationary")) {
            final double[] newPos = pos.clone();

            for (int step = 0; step < steps; step++) {
                newPos[0] += vel[0] / steps;
                newPos[1] += vel[1] / steps;
                if (checkCollision(world, newPos)) break;
            }

            if (!world.projectiles.contains(this)) return;

            if (trailCount == 0) {
                pos[0] = newPos[0];
                pos[1] = newPos[1];
            } else {
                if (trail == trailCount) {
                    Projectile child = new Projectile(type, new double[]{pos[0] + vel[0], pos[1] + vel[1]}, new double[2], angle, source);
                    child.bounces = this.bounces;
                    world.projectiles.add(child);
                }
                trail--;
                if (trail == 0) {
                    world.projectiles.remove(this);
                }
            }
        }
    }

    //Render projectile
    public void render(Graphics2D g, double[] screenPos) {
        AffineTransform originalTransform = g.getTransform();
        g.rotate(Math.toRadians(angle), screenPos[0], screenPos[1]);
        g.drawImage(staticTexture, (int) (screenPos[0] - size / 2.0), (int) (screenPos[1] - size / 2.0), (int) size, (int) size, null);
        g.setTransform(originalTransform);
    }

    public void renderLight(double[] screenPos) {
        HoneySuckle.lights.add(Map.of(
                "posX", screenPos[0] + config.tileSize / 2,
                "posY", screenPos[1] + config.tileSize / 2,
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

    //Checks all collision at a given position, returns true if something was hit
    private boolean checkCollision(World world, double[] checkPos) {
        int[] posIndex = new int[]{
            (int) Math.floor(checkPos[0] / config.tileSize),
            (int) Math.floor(checkPos[1] / config.tileSize)
        };

        if (posIndex[0] < 0 || posIndex[0] >= world.size[0] || posIndex[1] < 0 || posIndex[1] >= world.size[1]) {
            destroy(world);
            return true;
        }

        //World objects and tiles
        for (int x = (int) Math.floor(posIndex[0] - size); x < posIndex[0] + size; x++) {
            if (x > -1 && x < world.size[0]) {
                for (int y = (int) Math.floor(posIndex[1] - size); y < posIndex[1] + size; y++) {
                    if (y > -1 && y < world.size[1]) {
                        if (Collision.isBoxOverlap(
                                new Point2D.Double(checkPos[0], checkPos[1]),
                                new Point2D.Double(size, size),
                                angle,
                                new Point2D.Double(config.tileSize * (x + 0.5), config.tileSize * (y + 0.5)),
                                new Point2D.Double(config.tileSize * 0.5, config.tileSize * 0.5))) {
                            final WorldObject object = world.objGrid[x][y];
                            if (object != null) {
                                final double oc = object.attributes.getOrDefault("projCollision", 0).doubleValue();
                                if (oc > 0 && Collision.isBoxOverlap(
                                        new Point2D.Double(checkPos[0], checkPos[1]),
                                        new Point2D.Double(size, size),
                                        angle,
                                        new Point2D.Double(config.tileSize * (x + 0.5), config.tileSize * (y + 0.5)),
                                        new Point2D.Double(config.tileSize * oc * 0.5, config.tileSize * oc * 0.5))) {
                                    if (tags.contains("damageTile")) {
                                        if (object.damage(damage)) {
                                            //Object destroyed — projectile passes through
                                            world.processLoot(object.loot, new double[]{config.tileSize * (x + 0.5), config.tileSize * (y + 0.5)}, source instanceof Player ? (Player) source : null);
                                        } else {
                                            //Object survived — bounce or stop
                                            if (bounces > 0) reflectOff(x, y, checkPos, world);
                                            else destroy(world);
                                            return true;
                                        }
                                    } else {
                                        if (bounces > 0) reflectOff(x, y, checkPos, world);
                                        else destroy(world);
                                        return true;
                                    }
                                }
                                if (object.tags.contains("flameProj") && !flaming) {
                                    damage *= attributes.getOrDefault("flame", 1.5).doubleValue();
                                    flaming = true;
                                    staticTexture = getTexture(getPostfix());
                                }
                            }

                            final Tile tile = world.grid[posIndex[0]][posIndex[1]];
                            if (tile.attributes.getOrDefault("projCollision", 0).doubleValue() > 0) {
                                if (bounces > 0) reflectOff(posIndex[0], posIndex[1], checkPos, world);
                                else destroy(world);
                                return true;
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

        //Entities
        if (tags.contains("hurtEntity") || tags.contains("hurtBoss")) {
            final int px0 = Math.max(0, (int) Math.floor((checkPos[0] - circumradius) / config.tileSize));
            final int px1 = Math.min(world.size[0] - 1, (int) Math.floor((checkPos[0] + circumradius) / config.tileSize));
            final int py0 = Math.max(0, (int) Math.floor((checkPos[1] - circumradius) / config.tileSize));
            final int py1 = Math.min(world.size[1] - 1, (int) Math.floor((checkPos[1] + circumradius) / config.tileSize));
            for (int bx = px0; bx <= px1; bx++) {
                for (int by = py0; by <= py1; by++) {
                    final List<Entity> bucket = world.entityGrid.get(bx * world.size[1] + by);
                    if (bucket == null) continue;
                    for (Entity entity : bucket) {
                        if (hitEntities.contains(entity)) continue;
                        if (!tags.contains("hurtEntity") && !entity.tags.contains("boss")) continue;
                        if (source != entity || tags.contains("hurtSource")) {
                            if (Collision.isBoxOverlap(
                                    new Point2D.Double(checkPos[0], checkPos[1]),
                                    new Point2D.Double(size, size),
                                    angle,
                                    new Point2D.Double(entity.pos[0], entity.pos[1]),
                                    new Point2D.Double(entity.size, entity.size))) {
                                hitEntities.add(entity);
                                if (entity.brain.damage(damage)) {
                                    world.processLoot(entity.loot, entity.pos.clone(), source instanceof Player ? (Player) source : null);
                                }
                                if (destroy(world)) return true;
                            }
                        }
                    }
                }
            }
        }

        //Players
        if (tags.contains("hurtPlayer")) {
            for (Player player : Player.players) {
                if (source != player || tags.contains("hurtSource")) {
                    if (Collision.isBoxOverlap(
                            new Point2D.Double(checkPos[0], checkPos[1]),
                            new Point2D.Double(size, size),
                            angle,
                            new Point2D.Double(player.pos[0], player.pos[1]),
                            new Point2D.Double(player.size, player.size))) {
                        player.damage(damage, true);
                        if (destroy(world)) return true;
                    }
                }
            }
        }

        return false;
    }

    private void reflectOff(int tx, int ty, double[] checkPos, World world) {
        final int sx = (int) Math.signum(vel[0]);
        final int sy = (int) Math.signum(vel[1]);

        final boolean openX = !hasProjCollision(world, tx - sx, ty);
        final boolean openY = !hasProjCollision(world, tx, ty - sy);

        if (openX && !openY) {
            vel[0] = -vel[0];
        } else if (!openX && openY) {
            vel[1] = -vel[1];
        } else {
            // Single tile or true corner — fall back to distance-to-center
            final double tcx = (tx + 0.5) * config.tileSize;
            final double tcy = (ty + 0.5) * config.tileSize;
            if (Math.abs(checkPos[0] - tcx) > Math.abs(checkPos[1] - tcy)) {
                vel[0] = -vel[0];
            } else {
                vel[1] = -vel[1];
            }
        }
        angle = Math.toDegrees(Math.atan2(vel[0], -vel[1]));
        hitEntities.clear();
        bounces--;
    }

    private boolean hasProjCollision(World world, int tx, int ty) {
        if (tx < 0 || tx >= world.size[0] || ty < 0 || ty >= world.size[1]) return true;
        WorldObject obj = world.objGrid[tx][ty];
        if (obj != null && obj.attributes.getOrDefault("projCollision", 0).doubleValue() >= 1) return true;
        return world.grid[tx][ty].attributes.getOrDefault("projCollision", 0).doubleValue() >= 1;
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
