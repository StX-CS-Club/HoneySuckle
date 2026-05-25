package honey.player.armory;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import honey.HoneySuckle;
import honey.mechanics.Collision;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Rendering;
import honey.world.Entity;
import honey.world.Projectile;
import honey.world.World;
import honey.world.WorldObject;

public class Attack {

    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;

    private final Map<String, long[]> attackFrames = new HashMap<>();

    private final Map<String, Map<String, Object>> behavior;
    private final Map<String, Object> swingBehavior;
    private final Map<String, Object> shootBehavior;
    private final Map<String, Object> shieldBehavior;
    private final Map<String, Object> stabBehavior;

    public final boolean constClick;

    private final double size;

    private final Weapon weapon;

    public Attack(Weapon weapon) {
        this.weapon = weapon;

        behavior = Weapon.weaponBehaviors.get(weapon.type);

        swingBehavior = registerBehavior("swing");
        shootBehavior = registerBehavior("shoot");
        shieldBehavior = registerBehavior("shield");
        stabBehavior = registerBehavior("stab");

        size = weapon.attributes.getOrDefault("size", 1).doubleValue() * TILE_SIZE;

        constClick = weapon.tags.contains("constClick");
    }

    public void updateControls(InputHandler input, Player player) {
        final Map<String, long[]> staticAttackFrames = Map.copyOf(attackFrames);

        if (constClick && input.clickDown(1) || input.clickPressed(1)) {
            if (swingBehavior != null) {
                final int cooldown = numberFromMap(swingBehavior, "cooldown", 10).intValue();
                final String attackId = (String) swingBehavior.get("attackId");

                final long[] attackFrame = staticAttackFrames.get(attackId);
                if (attackFrame[0] >= cooldown || attackFrame[0] == -1) {
                    attackFrame[0] = 0;
                }
            }
            if (shootBehavior != null) {
                final int cooldown = numberFromMap(shootBehavior, "cooldown", 10).intValue();
                final String attackId = (String) shootBehavior.get("attackId");

                final long[] attackFrame = staticAttackFrames.get(attackId);
                if (attackFrame[0] >= cooldown || attackFrame[0] == -1) {
                    attackFrame[0] = 0;
                }
            }
            if (shieldBehavior != null) {
                final int cooldown = numberFromMap(shieldBehavior, "cooldown", 10).intValue();
                final String attackId = (String) shieldBehavior.get("attackId");

                final long[] attackFrame = staticAttackFrames.get(attackId);
                if (attackFrame[0] >= cooldown || attackFrame[0] == -1) {
                    attackFrame[0] = 0;
                }
            }
            if (stabBehavior != null) {
                final int cooldown = numberFromMap(stabBehavior, "cooldown", 10).intValue();
                final String attackId = (String) stabBehavior.get("attackId");

                final long[] attackFrame = staticAttackFrames.get(attackId);
                if (attackFrame[0] >= cooldown || attackFrame[0] == -1) {
                    attackFrame[0] = 0;
                }
            }
        }

    }

    public void passiveUpdate() {
        for (String key : attackFrames.keySet()) {
            final long attackFrame = attackFrames.get(key)[0];
            if (attackFrame != -1) {
                attackFrames.get(key)[0]++;
            }
        }
    }

    public void update(Player player) {
        final World world = World.worlds.get(World.level);

        final Map<String, long[]> staticAttackFrames = Map.copyOf(attackFrames);

        if (shootBehavior != null) {
            final int frames = numberFromMap(shootBehavior, "frames", 5).intValue();
            final String attackId = (String) shootBehavior.get("attackId");
            if (staticAttackFrames.get(attackId)[0] == frames) {
                final int bullets = numberFromMap(shootBehavior, "bulletCount", 1).intValue();
                if (weapon.ammo != null) {
                    final int ammoUsed = numberFromMap(shootBehavior, "ammoCount", bullets).intValue();
                    if (weapon.ammo.count >= ammoUsed) {
                        final double spread = Math.toDegrees(numberFromMap(shootBehavior, "spread", 0).doubleValue());
                        for (int i = 0; i < bullets; i++) {
                            world.projectiles.add(new Projectile(weapon.ammo.mergeAttributes(weapon.type, shootBehavior), player.pos.clone(), player.vel, player.rotation - (bullets - 1) * spread / 2 + spread * i, player));
                        }
                        weapon.ammo.count -= ammoUsed;
                    }
                }
            }
        }

        if (swingBehavior != null) {
            final int frames = numberFromMap(swingBehavior, "frames", 5).intValue();
            final String attackId = (String) swingBehavior.get("attackId");

            final long attackFrame = staticAttackFrames.get(attackId)[0];
            if (attackFrame <= frames && attackFrame >= 0) {
                final double swingSize = TILE_SIZE * numberFromMap(swingBehavior, "size", size / TILE_SIZE).doubleValue();
                final double damage = numberFromMap(swingBehavior, "damage", 0.1).doubleValue();
                final double objDamage = numberFromMap(swingBehavior, "objDamage", 1).doubleValue();

                //Check all entities
                for (Entity entity : world.renderEntities) {
                    //If collision overlap of entity and blade
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), swingSize / 2, player.rotation),
                            new Point2D.Double(swingSize, swingSize),
                            player.rotation,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        //Damage entity, if dead, add materials
                        if (entity.brain.damage(damage)) {
                            world.processLoot(entity.loot, entity.pos.clone(), player);
                        }
                    }
                }
                //Player tile
                int sizeTiles = (int) Math.floor(swingSize / TILE_SIZE) + 1;
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
                                        Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), swingSize / 2, player.rotation),
                                        new Point2D.Double(swingSize, swingSize),
                                        player.rotation,
                                        new Point2D.Double((x + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE),
                                        new Point2D.Double(HoneySuckle.TILE_SIZE, TILE_SIZE))) {
                                    WorldObject obj = world.objGrid[x][y];
                                    //Damage object, and if broken add materials
                                    if (world.objGrid[x][y].damage(damage * objDamage)) {
                                        world.processLoot(obj.loot, new double[]{(x + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE}, player);
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }

        if (stabBehavior != null) {
            final int frames = numberFromMap(stabBehavior, "frames", 5).intValue();
            final String attackId = (String) stabBehavior.get("attackId");

            final long attackFrame = staticAttackFrames.get(attackId)[0];
            if (attackFrame <= frames && attackFrame >= 0) {
                final double stabSize = TILE_SIZE * numberFromMap(stabBehavior, "size", size / TILE_SIZE).doubleValue();
                final double damage = numberFromMap(stabBehavior, "damage", 0.5).doubleValue();
                final double objDamage = numberFromMap(stabBehavior, "objDamage", 1).doubleValue();

                final double progress = Math.min(attackFrame / (frames * 0.67), 1.0);

                //Check all entities
                for (Entity entity : world.renderEntities) {
                    //If collision overlap of entity and blade
                    if (Collision.isBoxOverlap(
                            Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size / 2 + stabSize * progress, player.rotation),
                            new Point2D.Double(stabSize / 2, stabSize * 2 * progress),
                            player.rotation,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        //Damage entity, if dead, add materials
                        if (entity.brain.damage(damage)) {
                            world.processLoot(entity.loot, entity.pos.clone(), player);
                        }
                        attackFrames.get(attackId)[0] = -1;
                        break;
                    }
                }
                //Player tile
                int sizeTiles = (int) Math.floor(stabSize * 2 / TILE_SIZE) + 1;
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
                                        Collision.addAtAngle(new Point2D.Double(player.pos[0], player.pos[1]), player.size / 2 + stabSize * progress, player.rotation),
                                        new Point2D.Double(stabSize / 2, stabSize * 2 * progress),
                                        player.rotation,
                                        new Point2D.Double((x + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE),
                                        new Point2D.Double(HoneySuckle.TILE_SIZE, TILE_SIZE))) {
                                    WorldObject obj = world.objGrid[x][y];
                                    //Damage object, and if broken add materials
                                    if (world.objGrid[x][y].damage(damage * objDamage)) {
                                        world.processLoot(obj.loot, new double[]{(x + 0.5) * TILE_SIZE, (y + 0.5) * TILE_SIZE}, player);
                                    }
                                    attackFrames.get(attackId)[0] = -1;
                                    break;
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

            final long attackFrame = staticAttackFrames.get(attackId)[0];

            if ((attackFrame < frames || attackFrame >= cooldown) && attackFrame >= -1) {
                final double shieldSize = numberFromMap(shieldBehavior, "size", size / TILE_SIZE).doubleValue() * TILE_SIZE;
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
                            new Point2D.Double(shieldSize, shieldSize),
                            player.rotation,
                            new Point2D.Double(entity.pos[0], entity.pos[1]),
                            new Point2D.Double(entity.size, entity.size))) {
                        //Block tuah! Shield that thang!

                        //Stength of parry
                        double parryCoef = parry * frames / attackFrame;
                        if (attackFrame == frames) {
                            parryCoef = 1;
                        }
                        long frameDifference = (frames - attackFrame);
                        if (frameDifference < 0 || attackFrame == -1) {
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
                            new Point2D.Double(shieldSize, shieldSize),
                            player.rotation,
                            new Point2D.Double(projectile.pos[0], projectile.pos[1]),
                            new Point2D.Double(projectile.size, projectile.size))) {
                        //If in parry time
                        world.projectiles.remove(projectile);
                        if (attackFrame >= cooldown || attackFrame == -1) {
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
        
        final Map<String, long[]> staticAttackFrames = Map.copyOf(attackFrames);

        String textureId = weapon.texture.get("texture");
        World world = World.worlds.get(World.level);
        String overlayColor = null;

        double screenSize = size;
        double[] screenPos = new double[]{
            GAME_WIDTH / 2.0 + player.pos[0] - world.camera[0],
            GAME_HEIGHT / 2.0 + player.pos[1] - world.camera[1]
        };

        if (animation != null) {
            if (animation.contains("_swing_") && swingBehavior != null) {
                final String attackId = (String) swingBehavior.get("attackId");
                final int frames = numberFromMap(swingBehavior, "frames", 5).intValue();
                final long attackFrame = staticAttackFrames.get(attackId)[0];
                if (attackFrame < frames && attackFrame >= 0) {
                    final double swingSize = TILE_SIZE * numberFromMap(swingBehavior, "size", screenSize / TILE_SIZE).doubleValue();
                    //Position of slash on screen
                    double[] swingScreenPos = new double[]{
                        GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] - swingSize / 2.0,
                        GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - swingSize - player.size / 2.0
                    };
                    //Render slash
                    g.drawImage(
                            Rendering.renderGIF("attacks/slash", weapon.texture.get("swingColor"), ((double) attackFrame) / frames),
                            (int) swingScreenPos[0], (int) swingScreenPos[1], (int) swingSize, (int) swingSize, null);
                    return;
                }
            }

            if (animation.contains("_stab_") && stabBehavior != null) {
                final String attackId = (String) stabBehavior.get("attackId");
                final int frames = numberFromMap(stabBehavior, "frames", 5).intValue();
                final long attackFrame = staticAttackFrames.get(attackId)[0];
                if (attackFrame < frames && attackFrame >= 0) {
                    final double stabSize = TILE_SIZE * numberFromMap(stabBehavior, "size", screenSize / TILE_SIZE).doubleValue();
                    //Position of slash on screen
                    double[] swingScreenPos = new double[]{
                        GAME_WIDTH / 2.0 + player.pos[0] - World.worlds.get(World.level).camera[0] - stabSize / 4.0,
                        GAME_HEIGHT / 2.0 + player.pos[1] - World.worlds.get(World.level).camera[1] - stabSize * 2 - player.size / 2.0
                    };
                    //Render slash
                    g.drawImage(
                            Rendering.renderGIF("attacks/stab", weapon.texture.get("swingColor"), ((double) attackFrame) / frames),
                            (int) swingScreenPos[0], (int) swingScreenPos[1], (int) stabSize / 2, (int) stabSize * 2, null);
                    return;
                }
            }

            if (animation.contains("_shoot_") && shootBehavior != null) {
                final String attackId = (String) shootBehavior.get("attackId");
                final int frames = numberFromMap(shootBehavior, "frames", 5).intValue();
                final long attackFrame = staticAttackFrames.get(attackId)[0];
                if (attackFrame < frames && attackFrame >= 0) {
                    textureId = textureId + "_shoot";
                }
            }

            if (animation.contains("_parry_") && shieldBehavior != null) {
                final double parry = numberFromMap(shieldBehavior, "parry", 1).doubleValue();
                final int frames = numberFromMap(shieldBehavior, "frames", 10).intValue();
                final int cooldown = numberFromMap(shieldBehavior, "cooldown", 20).intValue();
                final String attackId = (String) shieldBehavior.get("attackId");

                final long attackFrame = staticAttackFrames.get(attackId)[0];

                if (attackFrame > 0) {
                    double sizeFactor = parry * frames / attackFrame;
                    if (sizeFactor >= 1) {
                        screenSize *= sizeFactor;
                    } else {
                        if (attackFrame >= frames && attackFrame < cooldown) {
                            overlayColor = "#888888";
                        }
                    }
                }
            }
        }

        final BufferedImage textureImage = overlayColor != null
                ? Rendering.applyOverlay(textureId, null, overlayColor, 192)
                : Rendering.texture(textureId, null);
        switch (weapon.texture.getOrDefault("type", "front")) {
            case "side" -> {
                screenPos[0] += player.size / 2.0;
                screenPos[1] -= screenSize / 2.0 + TILE_SIZE / 4.0;

                g.drawImage(textureImage,
                        (int) screenPos[0], (int) screenPos[1], (int) screenSize, (int) screenSize, null);
            }
            case "front" -> {
                screenPos[0] -= screenSize / 2.0;
                screenPos[1] -= screenSize + player.size / 2.0;

                g.drawImage(textureImage,
                        (int) screenPos[0], (int) screenPos[1], (int) screenSize, (int) screenSize, null);
            }
        }
    }

    private Map<String, Object> registerBehavior(String behaviorType) {
        final Map<String, Object> behaviorEntry = behavior.get(behaviorType);
        if (behaviorEntry != null) {
            behaviorEntry.putIfAbsent("attackId", "base");
            String attackId = (String) behaviorEntry.get("attackId");
            attackFrames.put(attackId, new long[]{-1});
        }
        return behaviorEntry;
    }

    private static Number numberFromMap(Map<String, Object> map, String key, Number defaultValue) {
        if (map.get(key) instanceof Number) {
            return (Number) map.get(key);
        }
        return defaultValue;
    }
}
