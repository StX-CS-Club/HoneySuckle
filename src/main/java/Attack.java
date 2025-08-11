
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Attack {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;

    private final Map<String, Integer> attackFrames = new HashMap<>();

    private final Map<String, Map<String, Object>> behavior;
    private final Map<String, Object> swingBehavior;
    private final Map<String, Object> shootBehavior;
    private final Map<String, Object> shieldBehavior;

    public final boolean constClick;

    private final Weapon weapon;

    public Attack(Weapon weapon) {
        this.weapon = weapon;

        behavior = Weapon.weaponBehaviors.get(weapon.type);

        swingBehavior = registerBehavior("swing");
        shootBehavior = registerBehavior("shoot");
        shieldBehavior = registerBehavior("shield");

        constClick = weapon.tags.contains("constClick");
    }

    public void updateControls(InputHandler input, Player player) {
        if (constClick && input.clickDown(1) || input.clickPressed(1)) {
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
        for (String key : attackFrames.keySet()) {
            attackFrames.put(key, attackFrames.get(key) + 1);
        }
    }

    public void update(Player player) {
        World world = World.worlds.get(World.level);

        if (shootBehavior != null) {
            final int frames = numberFromMap(shootBehavior, "frames", 5).intValue();
            final String attackId = (String) shootBehavior.get("attackId");
            if (attackFrames.get(attackId) == frames) {
                final int bullets = numberFromMap(shootBehavior, "bulletCount", 1).intValue();
                if (weapon.ammo != null) {
                    final int ammoUsed = numberFromMap(shootBehavior, "ammoCount", bullets).intValue();
                    if (weapon.ammo.count >= ammoUsed) {
                        final double spread = Math.toDegrees(numberFromMap(shootBehavior, "spread", 0).doubleValue());
                        for (int i = 0; i < bullets; i++) {
                            world.projectiles.add(new Projectile(weapon.ammo.mergeAttributes(weapon.type, shootBehavior), player.pos.clone(), player.vel, player.rotation-(bullets - 1)*spread/2+spread*i, player));
                        }
                        weapon.ammo.count -= ammoUsed;
                    }
                }
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

    public void render(Graphics2D g, Player player) {
        final String animation = weapon.texture.get("animation");

        String textureId = weapon.texture.get("texture");
        World world = World.worlds.get(World.level);
        double size = weapon.attributes.getOrDefault("size", 1).doubleValue() * TILE_SIZE;
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
                            Rendering.renderGIF("images/gifs/slash.gif", weapon.texture.get("swingColor"), ((double) attackFrame) / frames),
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
        switch (weapon.texture.getOrDefault("type", "front")) {
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
