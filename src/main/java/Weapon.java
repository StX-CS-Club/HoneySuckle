
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class Weapon {

    public static final Map<String, Map<String, Double>> weaponAttributes = new HashMap<>();
    public static final Map<String, String> weaponTypes = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponTextures = new HashMap<>();
    public static final Map<String, String> weaponProj = new HashMap<>();

    private int attackFrame;
    private int coolDown;

    private final String type;
    private final Map<String, Double> attributes;
    public final Map<String, String> texture;
    public final String weapon;

    public Weapon(String weapon) {
        type = weaponTypes.get(weapon);
        attributes = weaponAttributes.get(weapon);
        attackFrame = attributes.get("frames").intValue();
        texture = weaponTextures.get(weapon);
        this.weapon = weapon;
    }

    public void attack(Player player) {
        switch (type) {
            case "blade" -> {
                if (coolDown <= 0) {
                    attackFrame = 0;
                    coolDown = attributes.get("cooldown").intValue();
                }
            }
            case "gun" -> {
                if (coolDown <= 0) {
                    attackFrame = 0;
                    coolDown = attributes.get("cooldown").intValue();
                    World.worlds.get(World.level).projectiles.add(new Projectile(weaponProj.get(weapon), player.pos, player.rotation, player));
                }
            }
            case "shield" -> {
                if (coolDown <= 0) {
                    attackFrame = 0;
                    coolDown = attributes.get("cooldown").intValue();
                }
            }
        }
    }

    public void update(Player player) {
        World world = World.worlds.get(World.level);
        switch (type) {
            case "blade" -> {
                coolDown -= 1;
                if (attackFrame < attributes.get("frames")) {
                    attackFrame++;
                    double size = HoneySuckle.tileSize * attributes.get("size");
                    for (Entity entity : world.renderEntities) {
                        if (Collision.isBoxOverlap(
                                Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                                new Point2D.Double(size / 2, size / 2),
                                player.rotation,
                                new Point2D.Double(entity.pos[0], entity.pos[1]),
                                new Point2D.Double(entity.size, entity.size))) {
                            if (entity.damage(attributes.get("damage"))) {
                                Map<String, Integer> loot = Entity.entityLoot.get(entity.type);
                                for (String material : loot.keySet()) {
                                    player.crafting.materials.put(material, player.crafting.getOrDefault(material) + loot.get(material));
                                }
                            }
                        }
                    }
                    int sizeTiles = (int) Math.floor(size / HoneySuckle.tileSize) + 1;
                    int[] posIndex = new int[]{
                        (int) Math.floor(player.pos[0] / HoneySuckle.tileSize),
                        (int) Math.floor(player.pos[1] / HoneySuckle.tileSize)
                    };
                    for (int x = posIndex[0] - sizeTiles; x < posIndex[0] + sizeTiles; x++) {
                        for (int y = posIndex[1] - sizeTiles; y < posIndex[1] + sizeTiles; y++) {
                            if (x >= 0 && x < world.size[0] && y >= 0 && y < world.size[1]) {
                                if (world.objGrid[x][y] != null) {
                                    if (Collision.isBoxOverlap(
                                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                                            new Point2D.Double(size, size),
                                            player.rotation,
                                            new Point2D.Double((x + 0.5) * HoneySuckle.tileSize, (y + 0.5) * HoneySuckle.tileSize),
                                            new Point2D.Double(HoneySuckle.tileSize, HoneySuckle.tileSize))) {
                                        WorldObject obj = world.objGrid[x][y];
                                        if (world.objGrid[x][y].damage(attributes.get("damage"))) {
                                            Map<String, Integer> loot = obj.loot;
                                            for (String material : loot.keySet()) {
                                                player.crafting.materials.put(material, player.crafting.getOrDefault(material) + loot.get(material));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case "gun" -> {
                coolDown -= 1;
                if (attackFrame < attributes.get("frames")) {
                    attackFrame++;
                }
            }
            case "shield" -> {
                coolDown -= 1;
                if (attackFrame < attributes.get("frames")) {
                    attackFrame++;
                }
                double size = HoneySuckle.tileSize * attributes.get("size");
                for (Entity entity : world.renderEntities) {
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                            new Point2D.Double(size, size),
                            player.rotation,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        if (attackFrame < attributes.get("frames") || coolDown <= 0) {
                            double[] direction = new double[]{
                                Math.signum(Math.sin(Math.toRadians(player.rotation))),
                                -Math.signum(Math.cos(Math.toRadians(player.rotation)))
                            };
                            double parryCoef = attributes.get("parry") * attributes.get("frames") / attackFrame;
                            if (attackFrame == attributes.get("frames")) {
                                parryCoef = 1;
                            }
                            double[] parryBonus = new double[]{
                                attributes.get("parry") / attributes.get("frames") * HoneySuckle.tileSize * (attributes.get("frames") - attackFrame) * Math.abs(Math.sin(Math.toRadians(player.rotation))),
                                attributes.get("parry") / attributes.get("frames") * HoneySuckle.tileSize * (attributes.get("frames") - attackFrame) * Math.abs(Math.cos(Math.toRadians(player.rotation)))
                            };
                            entity.vel[0] = (Math.abs(entity.vel[0]) * attributes.get("bounce") * parryCoef + parryBonus[0]) * direction[0] / entity.weight;
                            entity.vel[1] = (Math.abs(entity.vel[1]) * attributes.get("bounce") * parryCoef + parryBonus[1]) * direction[1] / entity.weight;
                        }
                    }
                }
                for (Projectile projectile : world.renderProjectiles) {
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size * 2, player.rotation),
                            new Point2D.Double(size, size),
                            player.rotation,
                            new Point2D.Double(projectile.pos[0], projectile.pos[1]),
                            new Point2D.Double(projectile.size, projectile.size))) {
                        if (attackFrame < attributes.get("frames")) {
                            projectile.alterVel(Collision.pointToArray(
                                    Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size * 2 + projectile.size, player.rotation)
                            ), player.rotation, 2, player);
                        } else if (coolDown <= 0) {
                            player.vel[0] += projectile.vel[0] / 2 * projectile.weight / attributes.get("bounce");
                            player.vel[1] += projectile.vel[1] / 2 * projectile.weight / attributes.get("bounce");
                            world.projectiles.remove(projectile);
                        }
                    }
                }
            }
        }
    }

    public void render(Graphics2D g, Player player) {
        switch (type) {
            case "blade" -> {
                if (attackFrame < attributes.get("frames")) {
                    double[] screenPos = new double[]{
                        HoneySuckle.size[0] / 2 + player.pos[0] - World.worlds.get(World.level).camera[0] - attributes.get("size") * HoneySuckle.tileSize / 2,
                        HoneySuckle.size[1] / 2 + player.pos[1] - World.worlds.get(World.level).camera[1] - attributes.get("size") * HoneySuckle.tileSize - player.size / 2
                    };
                    g.drawImage(
                            Rendering.replaceGradient(Rendering.renderGIF("images/gifs/slash.gif", ((double) attackFrame) / attributes.get("frames")), texture.get("bladeColor")),
                            (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.tileSize * attributes.get("size")), (int) (HoneySuckle.tileSize * attributes.get("size")), null);
                } else {
                    double size = attributes.get("size");

                    double[] screenPos = new double[]{
                        HoneySuckle.size[0] / 2 + player.pos[0] - World.worlds.get(World.level).camera[0] - attributes.get("size") * HoneySuckle.tileSize/2 + player.size,
                        HoneySuckle.size[1] / 2 + player.pos[1] - World.worlds.get(World.level).camera[1] - attributes.get("size") * HoneySuckle.tileSize / 2 - HoneySuckle.tileSize/4
                    };

                    g.drawImage(
                            Rendering.texture(texture.get("texture"), "#ffffff"),
                            (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.tileSize * size), (int) (HoneySuckle.tileSize * size), null);
                }
            }
            case "gun" -> {
                double size = attributes.get("size");

                double[] screenPos = new double[]{
                    HoneySuckle.size[0] / 2 + player.pos[0] - World.worlds.get(World.level).camera[0] - size * HoneySuckle.tileSize / 2,
                    HoneySuckle.size[1] / 2 + player.pos[1] - World.worlds.get(World.level).camera[1] - size * HoneySuckle.tileSize - player.size / 2
                };
                g.drawImage(
                        Rendering.texture(texture.get("texture"), "#ffffff"),
                        (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.tileSize * size), (int) (HoneySuckle.tileSize * size), null);
            }
            case "shield" -> {
                double size = attributes.get("size") * attributes.get("parry") / (attackFrame / attributes.get("frames"));

                double[] screenPos = new double[]{
                    HoneySuckle.size[0] / 2 + player.pos[0] - World.worlds.get(World.level).camera[0] - size * HoneySuckle.tileSize / 2,
                    HoneySuckle.size[1] / 2 + player.pos[1] - World.worlds.get(World.level).camera[1] - size * HoneySuckle.tileSize - player.size / 2
                };
                g.drawImage(
                        Rendering.texture(texture.get("texture"), "#ffffff"),
                        (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.tileSize * size), (int) (HoneySuckle.tileSize * size), null);
            }
        }
    }
}
