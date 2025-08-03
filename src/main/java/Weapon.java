
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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
    public static final Map<String, Map<String, Number>> weaponAttributes = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponStats = new HashMap<>();
    public static final Map<String, Map<String, Map<String, Object>>> weaponBehaviors = new HashMap<>();
    public static final Map<String, Map<String, String>> weaponTextures = new HashMap<>();
    public static final Map<String, List<String>> weaponTags = new HashMap<>();
    public static final Map<String, String> weaponNames = new HashMap<>();
    public static final Map<String, Integer> weaponIntId = new HashMap<>();
    public static final Map<Integer, String> weaponStringId = new HashMap<>();

    //Weapon properties
    private final Map<String, Number> attributes;
    private final Map<String, Map<String, Object>> behavior;
    public final Map<String, String> texture;
    public final Map<String, String> stats;
    public final String weapon;
    private final String name;
    public final boolean constClick;
    public final List<String> tags;

    private final Map<String, Integer> attackFrames = new HashMap<>();

    private final Map<String, Object> swingBehavior;
    private final Map<String, Object> shootBehavior;
    private final Map<String, Object> shieldBehavior;

    //Weapon constructor
    public Weapon(String weapon) {
        //Interprets weapon id
        attributes = weaponAttributes.get(weapon);
        stats = weaponStats.get(weapon);
        behavior = weaponBehaviors.get(weapon);
        texture = weaponTextures.get(weapon);
        tags = weaponTags.get(weapon);
        constClick = tags.contains("constClick");
        this.weapon = weapon;
        name = weaponNames.get(weapon);

        swingBehavior = registerBehavior("swing");
        shootBehavior = registerBehavior("shoot");
        shieldBehavior = registerBehavior("shield");
    }

    //Attack with weapon
    public void updateControls(InputHandler inputHandler, Player player) {
        //Dependant on weapon type

        if (constClick && inputHandler.clickDown(1) || inputHandler.clickPressed(1)) {
            if (swingBehavior != null) {
                final int cooldown = numberFromMap(swingBehavior, "cooldown", 10).intValue();
                final String attackId = (String) swingBehavior.get("attackId");

                if (attackFrames.get(attackId) >= cooldown) {
                    attackFrames.put(attackId, 0);
                }
            }
            if (shootBehavior != null) {
                final int cooldown = numberFromMap(shootBehavior, "cooldown", 10).intValue();
                final String attackId = (String) shootBehavior.get("attackId");

                if (attackFrames.get(attackId) >= cooldown) {
                    attackFrames.put(attackId, 0);
                }
            }
            if (shieldBehavior != null) {
                final int cooldown = numberFromMap(shieldBehavior, "cooldown", 10).intValue();
                final String attackId = (String) shieldBehavior.get("attackId");

                if (attackFrames.get(attackId) >= cooldown) {
                    attackFrames.put(attackId, 0);
                }
            }
        }
    }

    public void passiveUpdate() {
        //Progress cooldown
        for (String key : attackFrames.keySet()) {
            attackFrames.put(key, attackFrames.get(key) + 1);
        }
    }

    //Update Weapon
    public void update(Player player) {
        //Current world
        World world = World.worlds.get(World.level);

        if (shootBehavior != null) {
            final int frames = numberFromMap(shootBehavior, "frames", 5).intValue();
            final String attackId = (String) shootBehavior.get("attackId");
            if (attackFrames.get(attackId) == frames) {
                final int projId = numberFromMap(shootBehavior, "proj", 1).intValue();

                world.projectiles.add(new Projectile(Projectile.projStringId.get(projId), player.pos, player.vel, player.rotation, player));
            }
        }

        if (swingBehavior != null) {
            final int frames = numberFromMap(swingBehavior, "frames", 5).intValue();
            final String attackId = (String) swingBehavior.get("attackId");
            if (attackFrames.get(attackId) <= frames) {
                final double size = TILE_SIZE * numberFromMap(swingBehavior, "size", 1).doubleValue();
                final double damage = numberFromMap(swingBehavior, "damage", 0.1).doubleValue();

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
                        if (entity.brain.damage(damage)) {
                            for (Map<String, Number> loot : entity.loot) {
                                player.inventory.incrementItem(loot, true);
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
                                    if (world.objGrid[x][y].damage(damage)) {
                                        for (Map<String, Number> loot : obj.loot) {
                                            player.inventory.incrementItem(loot, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }

        if (shieldBehavior != null) {
            final int frames = numberFromMap(shieldBehavior, "frames", 10).intValue();
            final int cooldown = numberFromMap(shieldBehavior, "cooldown", 20).intValue();
            final String attackId = (String) shieldBehavior.get("attackId");

            final int attackFrame = attackFrames.get(attackId);

            if (attackFrame < frames || attackFrame >= cooldown) {
                final double size = numberFromMap(shieldBehavior, "size", 1).doubleValue() * TILE_SIZE;
                final double parry = numberFromMap(shieldBehavior, "parry", 1).doubleValue();
                final double bounce = numberFromMap(shieldBehavior, "bounce", 1).doubleValue();

                //Direction shield is facing
                double[] direction = new double[]{
                    Math.signum(Math.sin(Math.toRadians(player.rotation))),
                    -Math.signum(Math.cos(Math.toRadians(player.rotation)))
                };

                //Check all entities
                for (Entity entity : world.renderEntities) {
                    //If collision overlap of shield and entity
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size, player.rotation),
                            new Point2D.Double(size, size),
                            player.rotation,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        //Block tuah! Shield that thang!

                        //Stength of parry
                        double parryCoef = parry * frames / attackFrame;
                        if (attackFrame == frames) {
                            parryCoef = 1;
                        }
                        int frameDifference = (frames - attackFrame);
                        if (frameDifference < 0) {
                            frameDifference = 0;
                        }
                        //Bonus of parry
                        double[] parryBonus = new double[]{
                            parry / frames * TILE_SIZE * frameDifference * Math.abs(Math.sin(Math.toRadians(player.rotation))),
                            parry / frames * TILE_SIZE * frameDifference * Math.abs(Math.cos(Math.toRadians(player.rotation)))
                        };
                        //Yeet
                        entity.vel[0] = (Math.abs(entity.vel[0]) * bounce * parryCoef + parryBonus[0]) * direction[0] / entity.weight;
                        entity.vel[1] = (Math.abs(entity.vel[1]) * bounce * parryCoef + parryBonus[1]) * direction[1] / entity.weight;
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
                        world.projectiles.remove(projectile);
                        if (attackFrame >= cooldown) {
                            //Delete projectile, knock back player
                            player.vel[0] += projectile.vel[0] / 2 * projectile.weight / bounce;
                            player.vel[1] += projectile.vel[1] / 2 * projectile.weight / bounce;
                        } else {
                            projectile.alterVel(projectile.pos, player.vel, player.rotation, parry, player);
                            //Send projectile towards shield direction at double velocity
                            final int projId = Projectile.projIntId.get(projectile.type);
                            final String projString = Projectile.projStringId.get(projectile.attributes.getOrDefault("parryProj", projId).intValue());
                            Projectile newProjectile = new Projectile(projString,
                                    projectile.pos, projectile.vel, player.rotation, player);
                            world.projectiles.add(newProjectile);
                        }
                    }
                }
            }
        }
    }

    //Render Weapon
    public void render(Graphics2D g, Player player) {
        //Dependant on weapon type
        final String animation = texture.get("animation");

        String textureId = texture.get("texture");
        World world = World.worlds.get(World.level);
        double size = attributes.getOrDefault("size", 1).doubleValue() * TILE_SIZE;
        String overlayColor = null;

        double[] screenPos = new double[]{
            GAME_WIDTH / 2.0 + player.pos[0] - world.camera[0],
            GAME_HEIGHT / 2.0 + player.pos[1] - world.camera[1]
        };

        if (animation != null) {
            if (animation.contains("_swing_") && swingBehavior != null) {
                final String attackId = (String) swingBehavior.get("attackId");
                final int frames = numberFromMap(swingBehavior, "frames", 5).intValue();
                final int attackFrame = attackFrames.get(attackId);
                if (attackFrame < frames) {
                    final double swingSize = TILE_SIZE * numberFromMap(swingBehavior, "size", size / TILE_SIZE).doubleValue();
                    //Position of slash on screen
                    double[] swingScreenPos = new double[]{
                        GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] - swingSize / 2.0,
                        GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - swingSize - player.size / 2.0
                    };
                    //Render slash
                    g.drawImage(
                            Rendering.replaceGradient(Rendering.renderGIF("images/gifs/slash.gif", ((double) attackFrame) / frames), texture.get("bladeColor")),
                            (int) swingScreenPos[0], (int) swingScreenPos[1], (int) swingSize, (int) swingSize, null);
                    return;
                }
            }

            if (animation.contains("_shoot_") && shootBehavior != null) {
                final String attackId = (String) shootBehavior.get("attackId");
                final int frames = numberFromMap(shootBehavior, "frames", 5).intValue();
                final int attackFrame = attackFrames.get(attackId);
                if (attackFrame < frames) {
                    textureId = textureId + "_shoot";
                }
            }

            if (animation.contains("_parry_") && shieldBehavior != null) {
                final double parry = numberFromMap(shieldBehavior, "parry", 1).doubleValue();
                final int frames = numberFromMap(shieldBehavior, "frames", 10).intValue();
                final int cooldown = numberFromMap(shieldBehavior, "cooldown", 20).intValue();
                final String attackId = (String) shieldBehavior.get("attackId");

                final int attackFrame = attackFrames.get(attackId);

                double sizeFactor = parry * frames / attackFrame;
                if (sizeFactor >= 1) {
                    size *= sizeFactor;
                } else {
                    if (attackFrame >= frames && attackFrame < cooldown) {
                        overlayColor = "#888888";
                    }
                }
            }
        }

        BufferedImage textureImage = Rendering.texture(textureId, "#ffffff");
        if (overlayColor != null) {
            textureImage = Rendering.applyOverlay(textureImage, overlayColor, 192);
        }
        switch (texture.getOrDefault("type", "front")) {
            case "side" -> {
                screenPos[0] += player.size / 2.0;
                screenPos[1] -= size / 2.0 + TILE_SIZE / 4.0;

                g.drawImage(textureImage,
                        (int) screenPos[0], (int) screenPos[1], (int) size, (int) size, null);
            }
            case "front" -> {
                screenPos[0] -= size / 2.0;
                screenPos[1] -= size + player.size / 2.0;

                g.drawImage(textureImage,
                        (int) screenPos[0], (int) screenPos[1], (int) size, (int) size, null);
            }
        }
    }

    public void renderUiTile(Graphics2D g, int x, int y, double factor, Weapon[] weapons) {
        final String weaponTexture = texture.get("itemTexture");
        final String color = texture.get("rarityColor");

        g.drawImage(Rendering.texture("hud/weapon_slot", color), (int) (x - 50 * (factor - 1)), (int) (y - 50 * (factor - 1)), (int) (100 * factor), (int) (100 * factor), null);

        if (weaponTexture != null) {
            g.drawImage(Rendering.texture(weaponTexture, "#e8f1ff"), x + 0xc, y + 12, 75, 75, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 28));
        for (int w = 0; w < weapons.length; w++) {
            if (weapons[w] == this) {
                Rendering.centeredText(g, Integer.toString(w + 1), x + 95, y + 100);
            }
        }
    }

    public void renderScroll(Graphics2D g) {
        g.drawImage(Rendering.scroll(14), GAME_WIDTH / 2 - 192, 40, 384, 192, null);

        g.setColor(Color.decode(texture.getOrDefault("rarityColor", "#333333")));
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));

        Rendering.centeredText(g, name, GAME_WIDTH / 2, 88);

        g.setColor(Color.BLACK);
        String[] statKeys = stats.keySet().toArray(String[]::new);

        int columns = Math.ceilDiv(statKeys.length, 3);
        int width = 384 / columns;
        int x = GAME_WIDTH / 2 - 192 + 192 / columns;

        for (int i = 0; i < columns; i++) {
            for(int e = 0; e < 3; e++){
                int index = i*3+e;
                if(index < statKeys.length){
                    Rendering.centeredText(g, statKeys[index] + ": " + stats.get(statKeys[index]), x + width * i, 112 + 32*e, width-10, 24);
                }
            }
        }
    }

    private Map<String, Object> registerBehavior(String behaviorType) {
        Map<String, Object> behaviorEntry = behavior.get(behaviorType);
        if (behaviorEntry != null) {
            behaviorEntry.putIfAbsent("attackId", "base");
            String attackId = (String) behaviorEntry.get("attackId");
            attackFrames.put(attackId, 0);
            attackFrames.put(attackId, 0);
            return behaviorEntry;
        }
        return null;
    }

    private static Number numberFromMap(Map<String, Object> map, String key, Number defaultValue) {
        if (map.get(key) instanceof Number) {
            return (Number) map.get(key);
        }
        return defaultValue;
    }
}
