
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/* 
 * Brain.java *
 -Handles methods specific to entity types
 */
public class Brain {

    private static final int FPS = HoneySuckle.FPS;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    public static final Map<String, Map<String, Map<String, Object>>> entityBrain = new HashMap<>();

    private final Entity entity;
    private final World world;
    private final Map<String, Map<String, Object>> brainMap;
    private final Map<String, Boolean> states = new HashMap<>();

    // Attack Brain Maps
    private final Map<String, Object> lungeAttack;
    private final Map<String, Object> shootAttack;
    private final Map<String, Object> contactAttack;
    private final Map<String, Object> flee;
    private final Map<String, Object> chase;

    public double chaseAngle = 180;

    public final Map<String, Object> death;

    public Brain(Entity entity, World world) {
        this.entity = entity;
        this.world = world;
        brainMap = entityBrain.get(entity.type);

        lungeAttack = registerBrain("lunge");
        shootAttack = registerBrain("shoot");
        contactAttack = registerBrain("contact");
        flee = registerBrain("flee");
        chase = registerBrain("chase");

        death = registerBrain("death");
    }

    //Update Entity based on type
    public void update() {
        //Point2d of entity
        Point2D.Double entityPoint = Collision.arrayToPoint(entity.pos);

        //Main player and player Point2D
        Player player = HoneySuckle.player;
        Point2D.Double playerPoint = Collision.arrayToPoint(player.pos);

        //Goes through all players, and determines the closest player
        for (Player testPlayer : Player.players) {
            Point2D.Double testPoint = Collision.arrayToPoint(testPlayer.pos);
            if (entityPoint.distance(testPoint) < entityPoint.distance(playerPoint)) {
                player = testPlayer;
                playerPoint = Collision.arrayToPoint(player.pos);
            }
        }
        double[] playerDistance = new double[]{
            player.pos[0] - entity.pos[0],
            player.pos[1] - entity.pos[1]
        };
        double playerAbsDistance = Math.sqrt(playerDistance[0] * playerDistance[0] + playerDistance[1] * playerDistance[1]);
        chaseAngle = Math.toDegrees(Math.atan(-playerDistance[0] / playerDistance[1]));
        if (playerDistance[1] > 0) {
            chaseAngle += 180;
        }
        if (chaseAngle < 0) {
            chaseAngle += 360;
        }

        //Friction
        for (int i = 0; i < 2; i++) {
            entity.vel[i] /= 2;
            if (Math.abs(entity.vel[i]) <= 0.2) {
                entity.vel[i] = 0;
            }
        }

        final double healthBarDistance = entity.attributes.getOrDefault("healthBarDistance", 0).doubleValue() * TILE_SIZE;
        if (healthBarDistance != 0) {
            if (healthBarDistance >= playerAbsDistance) {
                HoneySuckle.healthBars.add(entity);
            } else {
                HoneySuckle.healthBars.remove(entity);
            }
        }

        boolean hesitate = false;
        if (!lungeAttack.isEmpty()) {
            final int cooldown = numberFromMap(lungeAttack, "cooldown", 40).intValue();
            final int hesitationTime = numberFromMap(lungeAttack, "hesitationTime", 0).intValue();
            final int frames = numberFromMap(lungeAttack, "frames", hesitationTime).intValue();
            final double range = numberFromMap(lungeAttack, "range", 10).doubleValue();
            final double length = numberFromMap(lungeAttack, "length", 1).doubleValue();

            if (playerAbsDistance <= range * TILE_SIZE) {
                long ticks = incrementTicks("lunge", 1);
                if (ticks >= cooldown) {
                    //If within range of view, do a little hop
                    double coefficient = TILE_SIZE * length / Math.max(1, playerAbsDistance);
                    entity.vel[0] += playerDistance[0] * coefficient;
                    entity.vel[1] += playerDistance[1] * coefficient;

                    ticks = 0;
                    entity.ticks.put("lunge", 0l);
                }
                if (ticks >= cooldown - hesitationTime) {
                    hesitate = true;
                }
                if (ticks >= cooldown - frames) {
                    states.put("lunging", true);
                } else {
                    states.put("lunging", false);
                }
            }
        }

        if (!shootAttack.isEmpty()) {
            double healthLost = entity.attributes.getOrDefault("health", 1).doubleValue() - entity.health;
            double panicSpeed = numberFromMap(shootAttack, "panicSpeed", 0).doubleValue();
            double speed = numberFromMap(shootAttack, "speed", 1).doubleValue();
            final double panicVel = numberFromMap(shootAttack, "panicVel", panicSpeed).doubleValue() * healthLost;
            final double vel = numberFromMap(shootAttack, "vel", speed).doubleValue() + panicVel;
            panicSpeed *= healthLost;
            speed += panicSpeed;
            final double cooldown = numberFromMap(shootAttack, "cooldown", 100).doubleValue();
            final int frames = numberFromMap(shootAttack, "frames", 10).intValue();
            final double range = numberFromMap(shootAttack, "range", 100).doubleValue();
            final String projectileId = Projectile.projStringId.get(numberFromMap(shootAttack, "proj", 3).intValue());

            if (playerAbsDistance <= range * TILE_SIZE) {
                long ticks = incrementTicks("shoot", 1);
                if (ticks * speed >= cooldown * FPS / 40.0 / speed) {
                    Projectile projectile = new Projectile(projectileId, entity.pos, entity.vel, chaseAngle, entity);
                    projectile.alterVel(entity.pos, entity.vel, chaseAngle, vel, entity);
                    world.projectiles.add(projectile);

                    entity.ticks.put("shoot", 0l);
                    ticks = 0;
                }

                if (ticks * speed >= cooldown * FPS / 40.0 / speed - frames) {
                    states.put("shooting", true);
                } else {
                    states.put("shooting", false);
                }
            }
        }

        if (!flee.isEmpty() && !hesitate) {
            final double range = numberFromMap(flee, "range", 5).doubleValue();
            final double speed = numberFromMap(flee, "speed", 0.1).doubleValue();
            final double hesitateRange = numberFromMap(flee, "hesitateRange", range).doubleValue();
            if (playerAbsDistance <= hesitateRange * TILE_SIZE) {
                hesitate = true;
            }
            if (playerAbsDistance <= range * TILE_SIZE) {
                entity.vel[0] -= TILE_SIZE * speed * -Math.cos(Math.toRadians(chaseAngle + 90));
                entity.vel[1] -= TILE_SIZE * speed * Math.sin(Math.toRadians(chaseAngle - 90));
            }
        }

        if (!chase.isEmpty() && !hesitate) {
            final double range = numberFromMap(chase, "range", 5).doubleValue();
            final double speed = numberFromMap(chase, "speed", 0.1).doubleValue();
            final double hesitateRange = numberFromMap(chase, "hesitateRange", 0).doubleValue();
            if (playerAbsDistance <= hesitateRange * TILE_SIZE) {
                hesitate = true;
            }
            if (playerAbsDistance <= range * TILE_SIZE && !hesitate) {
                states.put("chase", true);
                entity.vel[0] += TILE_SIZE * speed * -Math.cos(Math.toRadians(chaseAngle + 90));
                entity.vel[1] += TILE_SIZE * speed * Math.sin(Math.toRadians(chaseAngle - 90));
            } else {
                states.put("chase", false);
            }
        }

        if (hesitate) {
            states.put("hesitate", true);
        } else {
            states.put("hesitate", false);
        }

        //Update velocity and events based on pos
        entity.pos = world.bound(entity.pos, entity.vel, entity.tags, entity.size / 2.0);
        world.entityEvent(entity);
    }

    //Damage Entity; returns true if dead
    public boolean damage(double damage) {
        //Subtract damage 
        entity.health -= damage;
        //Add damageFrames
        if (entity.damageFrames < -5) {
            entity.damageFrames = (int) (1.5 * Math.floor(damage) + 1);
        }
        //If not dead, return false
        if (entity.health > 0) {
            return false;
        }
        //If dead, die, and return true
        die(World.worlds.get(World.level));
        return true;
    }

    //Kill Entity events
    public void die(World world) {
        if (!death.isEmpty()) {
            final int gateId = numberFromMap(death, "gateId", 0).intValue();
            if (gateId != 0) {
                boolean cleared = true;
                for (Entity worldEntity : world.entities) {
                    if (numberFromMap(worldEntity.brain.death, "gateId", 0).intValue() == gateId && worldEntity != entity) {
                        cleared = false;
                        break;
                    }
                }
                if (cleared) {
                    for (WorldObject[] objColumn : world.objGrid) {
                        for (int y = 0; y < objColumn.length; y++) {
                            if (objColumn[y] != null) {
                                if (objColumn[y].attributes.getOrDefault("deathGateId", 0).intValue() == gateId) {
                                    objColumn[y] = null;
                                }
                            }
                        }
                    }
                }
            }
        }
        world.entities.remove(entity);
        HoneySuckle.healthBars.remove(entity);
    }

    //Entity interacts with player
    public void event(Player player) {
        double[] playerDistance = new double[]{
            player.pos[0] - entity.pos[0],
            player.pos[1] - entity.pos[1]
        };

        if (!contactAttack.isEmpty()) {
            final double damage = numberFromMap(contactAttack, "damage", 0).doubleValue();
            final double bounce = numberFromMap(contactAttack, "bounce", 0).doubleValue();
            final double range = numberFromMap(contactAttack, "range", Math.ceil(entity.size / TILE_SIZE)).doubleValue();
            final int rate = (int) Math.floor(FPS / numberFromMap(contactAttack, "rate", 1).doubleValue());

            if (Math.sqrt(playerDistance[0] * playerDistance[0] + playerDistance[1] * playerDistance[1]) < TILE_SIZE * range) {
                long ticks = incrementTicks("contact", 1)-1;
                if (ticks % rate == 0) {
                    player.damage(damage, true);
                    entity.vel[0] *= -bounce;
                    entity.vel[1] *= -bounce;
                }
            } else {
                entity.ticks.put("contact", 0l);
            }

        }
    }

    public boolean checkState(String state) {
        if (states.get(state) != null) {
            return states.get(state);
        }
        return false;
    }

    private long incrementTicks(String key, int amount) {
        long ticks = entity.ticks.get(key);
        entity.ticks.put(key, ticks + amount);
        return ticks + amount;
    }

    private Map<String, Object> registerBrain(String brainType) {
        Map<String, Object> brain = brainMap.get(brainType);
        if (brain != null) {
            entity.ticks.put(brainType, 0l);
            return brain;
        }
        return new HashMap<>();
    }

    private static Number numberFromMap(Map<String, Object> map, String key, Number defaultValue) {
        if (map.get(key) instanceof Number) {
            return (Number) map.get(key);
        }
        return defaultValue;
    }
}
