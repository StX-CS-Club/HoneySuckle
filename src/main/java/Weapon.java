
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Weapon.java *
 - Class for managing weapons
 - Contains static json data
 */
public class Weapon {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static json data
    public static final Map<String, Map<String, Double>> weaponAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponStats = new HashMap<>();
    public static final Map<String, String> weaponTypes = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponTextures = new HashMap<>();
    public static final Map<String, String> weaponProj = new HashMap<>();
    public static final Map<String, List<String>> weaponTags = new HashMap<>();

    //Timing
    private int attackFrame;
    private int coolDown;

    //Weapon properties
    private final String type;
    private final Map<String, Double> attributes;
    public final Map<String, String> texture;
    public final String weapon;
    public final boolean constClick;
    public final List<String> tags;

    //Weapon constructor
    public Weapon(String weapon) {
        //Interprets weapon id
        type = weaponTypes.get(weapon);
        attributes = weaponAttributes.get(weapon);
        attackFrame = attributes.get("frames").intValue();
        texture = weaponTextures.get(weapon);
        tags = weaponTags.get(weapon);
        constClick = tags.contains("constClick");
        this.weapon = weapon;
    }

    //Attack with weapon
    public void attack(Player player) {
        //Dependant on weapon type
        switch (type) {
            case "blade" -> {
                //If cooldown over, set attack frame to 0 and reset cooldown
                if (coolDown <= 0) {
                    attackFrame = 0;
                    coolDown = attributes.get("cooldown").intValue();
                }
            }
            case "gun" -> {
                //If cooldown over, shoot projectile and reset cooldown
                if (coolDown <= 0) {
                    attackFrame = 0;
                    coolDown = attributes.get("cooldown").intValue();
                    World.worlds.get(World.level).projectiles.add(new Projectile(weaponProj.get(weapon), player.pos, player.vel, player.rotation, player));
                }
            }
            case "shield" -> {
                //If cooldown over, set attack frame to 0 and reset cooldown
                if (coolDown <= 0) {
                    attackFrame = 0;
                    coolDown = attributes.get("cooldown").intValue();
                }
            }
        }
    }

    //Update Weapon
    public void update(Player player) {
        //Current world
        World world = World.worlds.get(World.level);

        //Dependant on weapon type
        switch (type) {
            case "blade" -> {
                //Progress cooldown
                coolDown -= 1;
                //If in attack frames
                if (attackFrame < attributes.get("frames")) {
                    attackFrame++;
                    double size = TILE_SIZE * attributes.get("size");
                    //Check all entities
                    for (Entity entity : world.renderEntities) {
                        //If collision overlap of entity and blade
                        if (Collision.isBoxOverlap(
                                Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                                new Point2D.Double(size / 2, size / 2),
                                player.rotation,
                                new Point2D.Double(entity.pos[0], entity.pos[1]),
                                new Point2D.Double(entity.size, entity.size))) {
                            //Damage entity, if dead, add materials
                            if (entity.damage(attributes.get("damage"))) {
                                for (int i = 0; i < entity.loot.size(); i++) {
                                    if (Math.random() < entity.readLoot(i, "prob", 1).doubleValue()) {
                                        final String item = Inventory.itemStringId.get(entity.readLoot(i, "item", 0).intValue());
                                        player.inventory.items.put(item, player.inventory.getMaterial(item) + entity.readLoot(i, "count", 1).intValue());
                                    }
                                }
                            }
                        }
                    }
                    //Player tile
                    int sizeTiles = (int) Math.floor(size / TILE_SIZE) + 1;
                    int[] posIndex = new int[]{
                        (int) Math.floor(player.pos[0] / TILE_SIZE),
                        (int) Math.floor(player.pos[1] / TILE_SIZE)
                    };
                    //Check tiles
                    for (int x = posIndex[0] - sizeTiles; x < posIndex[0] + sizeTiles; x++) {
                        for (int y = posIndex[1] - sizeTiles; y < posIndex[1] + sizeTiles; y++) {
                            if (x >= 0 && x < world.size[0] && y >= 0 && y < world.size[1]) {
                                if (world.objGrid[x][y] != null) {
                                    //If collision overlap of tile and blade
                                    if (Collision.isBoxOverlap(
                                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                                            new Point2D.Double(size, size),
                                            player.rotation,
                                            new Point2D.Double((x + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE),
                                            new Point2D.Double(HoneySuckle.TILE_SIZE, TILE_SIZE))) {
                                        WorldObject obj = world.objGrid[x][y];
                                        //Damage object, and if broken add materials
                                        if (world.objGrid[x][y].damage(attributes.get("damage"))) {
                                            for (int i = 0; i < obj.loot.size(); i++) {
                                                if (Math.random() < obj.readLoot(i, "prob", 1).doubleValue()) {
                                                    final String item = Inventory.itemStringId.get(obj.readLoot(i, "item", 0).intValue());
                                                    player.inventory.items.put(item, player.inventory.getMaterial(item) + obj.readLoot(i, "count", 1).intValue());
                                                }
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
                //Progress cooldown
                coolDown -= 1;
                if (attackFrame < attributes.get("frames")) {
                    attackFrame++;
                }
            }
            case "shield" -> {
                //Progress cooldown
                coolDown -= 1;
                if (attackFrame < attributes.get("frames")) {
                    attackFrame++;
                }
                double size = TILE_SIZE * attributes.get("size");
                //Check all entities
                for (Entity entity : world.renderEntities) {
                    //If collision overlap of shield and entity
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                            new Point2D.Double(size, size),
                            player.rotation,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        if (attackFrame < attributes.get("frames") || coolDown <= 0) {
                            //Block tuah! Shield that thang!

                            //Direction shield is facing
                            double[] direction = new double[]{
                                Math.signum(Math.sin(Math.toRadians(player.rotation))),
                                -Math.signum(Math.cos(Math.toRadians(player.rotation)))
                            };
                            //Stength of parry
                            double parryCoef = attributes.get("parry") * attributes.get("frames") / attackFrame;
                            if (attackFrame == attributes.get("frames")) {
                                parryCoef = 1;
                            }
                            //Bonus of parry
                            double[] parryBonus = new double[]{
                                attributes.get("parry") / attributes.get("frames") * TILE_SIZE * (attributes.get("frames") - attackFrame) * Math.abs(Math.sin(Math.toRadians(player.rotation))),
                                attributes.get("parry") / attributes.get("frames") * TILE_SIZE * (attributes.get("frames") - attackFrame) * Math.abs(Math.cos(Math.toRadians(player.rotation)))
                            };
                            //Yeet
                            entity.vel[0] = (Math.abs(entity.vel[0]) * attributes.get("bounce") * parryCoef + parryBonus[0]) * direction[0] / entity.weight;
                            entity.vel[1] = (Math.abs(entity.vel[1]) * attributes.get("bounce") * parryCoef + parryBonus[1]) * direction[1] / entity.weight;
                        }
                    }
                }
                //Check projectiles
                for (Projectile projectile : world.renderProjectiles) {
                    //If collision overlap between shield and proj
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                            new Point2D.Double(size, size),
                            player.rotation,
                            new Point2D.Double(projectile.pos[0], projectile.pos[1]),
                            new Point2D.Double(projectile.size, projectile.size))) {
                        //If in parry time
                        if (attackFrame < attributes.get("frames")) {
                            //Send projectile towards shield direction at double velocity
                            projectile.alterVel(Collision.pointToArray(
                                    Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size * 2 + projectile.size, player.rotation)
                            ), player.vel, player.rotation, 1.5, player);
                        } else if (coolDown <= 0) {
                            //Delete projectile, knock back player
                            player.vel[0] += projectile.vel[0] / 2 * projectile.weight / attributes.get("bounce");
                            player.vel[1] += projectile.vel[1] / 2 * projectile.weight / attributes.get("bounce");
                            world.projectiles.remove(projectile);
                        }
                    }
                }
            }
        }
    }

    //Render Weapon
    public void render(Graphics2D g, Player player) {
        //Dependant on weapon type
        switch (type) {
            case "blade" -> {
                //If slashing...
                if (attackFrame < attributes.get("frames")) {
                    //Position of slash on screen
                    double[] screenPos = new double[]{
                        GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] - attributes.get("size") * TILE_SIZE / 2.0,
                        GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - attributes.get("size") * TILE_SIZE - player.size / 2.0
                    };
                    //Render slash
                    g.drawImage(
                            Rendering.replaceGradient(Rendering.renderGIF("images/gifs/slash.gif", ((double) attackFrame) / attributes.get("frames")), texture.get("bladeColor")),
                            (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.TILE_SIZE * attributes.get("size")), (int) (HoneySuckle.TILE_SIZE * attributes.get("size")), null);
                } else {
                    //Size of blase
                    double size = attributes.get("size");

                    //Position of blade on screen
                    double[] screenPos = new double[]{
                        GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] + player.size / 2.0,
                        GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - attributes.get("size") * TILE_SIZE / 2.0 - TILE_SIZE / 4.0
                    };

                    //Render blase
                    g.drawImage(
                            Rendering.texture(texture.get("texture"), "#ffffff"),
                            (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.TILE_SIZE * size), (int) (HoneySuckle.TILE_SIZE * size), null);
                }
            }
            case "gun" -> {
                double size = attributes.get("size");

                //Position of gun on screen
                double[] screenPos = new double[]{
                    GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] - size * TILE_SIZE / 2.0,
                    GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - size * TILE_SIZE - player.size / 2.0
                };

                //Render gun
                g.drawImage(
                        Rendering.texture(texture.get("texture"), "#ffffff"),
                        (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.TILE_SIZE * size), (int) (HoneySuckle.TILE_SIZE * size), null);
            }
            case "shield" -> {
                //Size dependant on parry animation
                double size = attributes.get("size") * attributes.get("parry") / (attackFrame / attributes.get("frames"));

                //Position of shield on screen
                double[] screenPos = new double[]{
                    GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] - size * TILE_SIZE / 2.0,
                    GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - size * TILE_SIZE - player.size / 2.0
                };

                //Render Shield
                g.drawImage(
                        Rendering.texture(texture.get("texture"), "#ffffff"),
                        (int) screenPos[0], (int) screenPos[1], (int) (HoneySuckle.TILE_SIZE * size), (int) (HoneySuckle.TILE_SIZE * size), null);
            }
        }
    }
}
