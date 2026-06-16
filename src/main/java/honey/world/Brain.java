
package honey.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import honey.HoneySuckle;
import honey.mechanics.ConfigManager;
import honey.player.Player;

/* 
 * Brain.java *
 -Handles methods specific to entity types
 */
public class Brain {

    public static ConfigManager config;

    public static final Map<String, Map<String, Map<String, Object>>> entityBrain = new HashMap<>();

    private final Entity entity;
    private final World world;
    private final Map<String, Map<String, Object>> brainMap;
    private final Map<String, Boolean> states = new HashMap<>();

    // Attack Brain Maps
    private final Map<String, Object> lungeAttack;
    private final Map<String, Object> shootAttack;
    private final Map<String, Object> contactAttack;
    private final Map<String, Object> summonBehavior;
    private final Map<String, Object> flee;
    private final Map<String, Object> chase;
    private final Map<String, Object> track;

    public double chaseAngle = 180;
    public double trackAngle = 180;
    public double immunity = 0;

    public final Map<String, Object> death;

    public Brain(Entity entity, World world) {
        this.entity = entity;
        this.world = world;
        brainMap = entityBrain.get(entity.type);

        lungeAttack = registerBrain("lunge");
        shootAttack = registerBrain("shoot");
        contactAttack = registerBrain("contact");
        summonBehavior = registerBrain("summon");
        flee = registerBrain("flee");
        chase = registerBrain("chase");
        track = registerBrain("track");

        death = registerBrain("death");
    }

    private static double distSq(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return dx * dx + dy * dy;
    }

    //Update Entity based on type
    public void update() {
        final Set<String> tickedThisFrame = new HashSet<>();
        //Find closest player using squared distance (no sqrt, no Point2D allocation)
        Player player = HoneySuckle.player;
        double bestDistSq = distSq(entity.pos, player.pos);

        for (Player testPlayer : Player.players) {
            double testDistSq = distSq(entity.pos, testPlayer.pos);
            if (testDistSq < bestDistSq) {
                player = testPlayer;
                bestDistSq = testDistSq;
            }
        }
        double[] playerDistance = new double[]{
            player.pos[0] - entity.pos[0],
            player.pos[1] - entity.pos[1]
        };
        double playerAbsDistance = Math.sqrt(playerDistance[0] * playerDistance[0] + playerDistance[1] * playerDistance[1]);
        chaseAngle = getAngle(player.pos);
        if (track.isEmpty()) {
            trackAngle = chaseAngle;
        }

        immunity = Math.max(immunity - entity.attributes.getOrDefault("immunityDegen", 0.15).doubleValue(), 0);

        //Friction
        for (int i = 0; i < 2; i++) {
            entity.vel[i] /= 2;
            if (Math.abs(entity.vel[i]) <= 0.2) {
                entity.vel[i] = 0;
            }
        }

        final double healthBarDistance = entity.attributes.getOrDefault("healthBarDistance", 0).doubleValue() * config.tileSize;
        if (healthBarDistance != 0) {
            if (healthBarDistance >= playerAbsDistance) {
                HoneySuckle.healthBars.add(entity);
            } else {
                HoneySuckle.healthBars.remove(entity);
            }
        }

        boolean hesitate = false;
        if (!lungeAttack.isEmpty()) {
            final String behaviorId = stringFromMap(lungeAttack, "tickId", "lunge");
            final int cooldown = numberFromMap(lungeAttack, "cooldown", 40).intValue();
            final int hesitationTime = numberFromMap(lungeAttack, "hesitationTime", 0).intValue();
            final int frames = numberFromMap(lungeAttack, "frames", hesitationTime).intValue();
            final double prob = numberFromMap(lungeAttack, "prob", 1).doubleValue();
            final boolean resetOnFail = booleanFromMap(lungeAttack, "resetOnFail", true);
            final double range = numberFromMap(lungeAttack, "range", 10).doubleValue();
            final double length = numberFromMap(lungeAttack, "length", 1).doubleValue();

            if (playerAbsDistance <= range * config.tileSize) {
                long ticks = tickedThisFrame.add(behaviorId) ? incrementTicks(behaviorId, 1) : entity.ticks.get(behaviorId)[0];
                if (ticks >= cooldown) {
                    if (checkState("lunging")) {
                        //If within range of view, do a little hop
                        double coefficient = config.tileSize * length / Math.max(1, playerAbsDistance);
                        entity.vel[0] += playerDistance[0] * coefficient;
                        entity.vel[1] += playerDistance[1] * coefficient;
                    }
                    ticks = 0;
                    entity.ticks.put(behaviorId, new long[]{0});
                    states.put("lunging", false);
                }
                if (ticks >= cooldown - hesitationTime) {
                    hesitate = true;
                }
                if (ticks >= cooldown - frames) {
                    if (ticks == cooldown - frames) {
                        if (ThreadLocalRandom.current().nextDouble() <= prob) {
                            states.put("lunging", true);
                        } else if (resetOnFail) {
                            entity.ticks.put(behaviorId, new long[]{0});
                        }
                    }
                } else {
                    states.put("lunging", false);
                }
            }
        }

        if (!shootAttack.isEmpty()) {
            final String behaviorId = stringFromMap(shootAttack, "tickId", "shoot");
            final double range = numberFromMap(shootAttack, "range", 100).doubleValue();

            if (playerAbsDistance <= range * config.tileSize) {
                final double healthLost = entity.attributes.getOrDefault("health", 1).doubleValue() - entity.health;
                final double panicSpeed = numberFromMap(shootAttack, "panicSpeed", 0).doubleValue();
                final double speed = numberFromMap(shootAttack, "speed", 1).doubleValue() + panicSpeed * healthLost;
                final int cooldown = (int) Math.round(numberFromMap(shootAttack, "cooldown", 100).doubleValue() * config.fps / 40.0 / speed);
                final double prob = numberFromMap(shootAttack, "prob", 1).doubleValue();
                final boolean resetOnFail = booleanFromMap(shootAttack, "resetOnFail", true);
                final int frames = numberFromMap(shootAttack, "frames", 10).intValue();

                long ticks = tickedThisFrame.add(behaviorId) ? incrementTicks(behaviorId, 1) : entity.ticks.get(behaviorId)[0];

                if (checkState("shooting") && ticks >= cooldown) {
                    final String projectileId = Projectile.projStringId.get(numberFromMap(shootAttack, "proj", 3).intValue());

                    final double panicVel = numberFromMap(shootAttack, "panicVel", panicSpeed).doubleValue() * healthLost;
                    final double vel = numberFromMap(shootAttack, "vel", speed).doubleValue() + panicVel;

                    final double prediction = numberFromMap(shootAttack, "prediction", 0).doubleValue();
                    double angle = chaseAngle;
                    if (prediction != 0) {
                        angle = getAngle(new double[]{
                            player.pos[0] + player.vel[0] * prediction,
                            player.pos[1] + player.vel[1] * prediction
                        });
                    }

                    Projectile projectile = new Projectile(projectileId, entity.pos, entity.vel, angle, entity);
                    projectile.alterVel(entity.pos, entity.vel, angle, vel, entity);
                    world.projectiles.add(projectile);

                    entity.ticks.put(behaviorId, new long[]{0});
                    states.put("shooting", false);
                    ticks = 0;
                }

                if (ticks >= cooldown - frames) {
                    if (ticks == cooldown - frames) {
                        if (ThreadLocalRandom.current().nextDouble() <= prob) {
                            states.put("shooting", true);
                        } else if (resetOnFail) {
                            entity.ticks.put(behaviorId, new long[]{0});
                        }
                    }
                } else {
                    states.put("shooting", false);
                }
            }
        }

        if (!summonBehavior.isEmpty()) {
            final String behaviorId = stringFromMap(summonBehavior, "tickId", "summon");
            final double range = numberFromMap(summonBehavior, "range", 10).doubleValue();

            if (playerAbsDistance <= range * config.tileSize) {
                final int cooldown = numberFromMap(summonBehavior, "cooldown", 200).intValue();
                final int frames = numberFromMap(summonBehavior, "frames", 10).intValue();
                final double prob = numberFromMap(summonBehavior, "prob", 1).doubleValue();
                final boolean resetOnFail = booleanFromMap(summonBehavior, "resetOnFail", true);
                final int count = numberFromMap(summonBehavior, "count", 1).intValue();
                final double countProb = numberFromMap(summonBehavior, "countProb", 0).doubleValue();
                final double speed = numberFromMap(summonBehavior, "speed", 0.1).doubleValue();
                final String entityType = Entity.entityStringId.get(numberFromMap(summonBehavior, "entity", 0).intValue());

                long ticks = tickedThisFrame.add(behaviorId) ? incrementTicks(behaviorId, 1) : entity.ticks.get(behaviorId)[0];

                if (checkState("summoning") && ticks >= cooldown && entityType != null) {
                    int total = count;
                    if (ThreadLocalRandom.current().nextDouble() <= countProb) {
                        total++;
                    }
                    for (int i = 0; i < total; i++) {
                        final double angle = ThreadLocalRandom.current().nextDouble() * 360;
                        final Entity summon = new Entity(entityType, entity.pos, world);
                        summon.vel[0] = config.tileSize * speed * -Math.cos(Math.toRadians(angle + 90));
                        summon.vel[1] = config.tileSize * speed * Math.sin(Math.toRadians(angle - 90));
                        world.entities.add(summon);
                    }
                    entity.ticks.put(behaviorId, new long[]{0});
                    states.put("summoning", false);
                    ticks = 0;
                }

                if (ticks >= cooldown - frames) {
                    if (ticks == cooldown - frames) {
                        if (ThreadLocalRandom.current().nextDouble() <= prob) {
                            states.put("summoning", true);
                        } else if (resetOnFail) {
                            entity.ticks.put(behaviorId, new long[]{0});
                        }
                    }
                } else {
                    states.put("summoning", false);
                }
            }
        }

        if (!flee.isEmpty() && !hesitate) {
            final double range = numberFromMap(flee, "range", 5).doubleValue();
            final double speed = numberFromMap(flee, "speed", 0.1).doubleValue();
            final double hesitateRange = numberFromMap(flee, "hesitateRange", range).doubleValue();
            if (playerAbsDistance <= hesitateRange * config.tileSize) {
                hesitate = true;
            }
            if (playerAbsDistance <= range * config.tileSize) {
                entity.vel[0] -= config.tileSize * speed * -Math.cos(Math.toRadians(chaseAngle + 90));
                entity.vel[1] -= config.tileSize * speed * Math.sin(Math.toRadians(chaseAngle - 90));
            }
        }

        if (!chase.isEmpty() && !hesitate) {
            final double range = numberFromMap(chase, "range", 5).doubleValue();
            final double speed = numberFromMap(chase, "speed", 0.1).doubleValue();
            final double hesitateRange = numberFromMap(chase, "hesitateRange", 0).doubleValue();
            if (playerAbsDistance <= hesitateRange * config.tileSize) {
                hesitate = true;
            }
            if (playerAbsDistance <= range * config.tileSize && !hesitate) {
                states.put("chase", true);
                entity.vel[0] += config.tileSize * speed * -Math.cos(Math.toRadians(chaseAngle + 90));
                entity.vel[1] += config.tileSize * speed * Math.sin(Math.toRadians(chaseAngle - 90));
            } else {
                states.put("chase", false);
            }
        }

        if (!track.isEmpty()) {
            final double range = numberFromMap(track, "range", 10).doubleValue();
            final double speed = numberFromMap(track, "speed", 0.1).doubleValue();
            final double hesitateRange = numberFromMap(track, "hesitateRange", 0).doubleValue();
            final double turnRate = numberFromMap(track, "turnRate", 3).doubleValue();

            if (hesitateRange > 0 && playerAbsDistance <= hesitateRange * config.tileSize) {
                hesitate = true;
            }

            if (playerAbsDistance <= range * config.tileSize) {
                // Turn toward chaseAngle at max turnRate degrees per tick
                double diff = chaseAngle - trackAngle;
                while (diff > 180) diff -= 360;
                while (diff < -180) diff += 360;
                trackAngle += Math.abs(diff) <= turnRate ? diff : Math.signum(diff) * turnRate;
                trackAngle = ((trackAngle % 360) + 360) % 360;

                if (!hesitate) {
                    entity.vel[0] += config.tileSize * speed * -Math.cos(Math.toRadians(trackAngle + 90));
                    entity.vel[1] += config.tileSize * speed * Math.sin(Math.toRadians(trackAngle - 90));
                    states.put("chase", true);
                } else {
                    states.put("chase", false);
                }
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
        world.bound(entity.pos, entity.vel, entity.tags, entity.size / 2.0);
        world.entityEvent(entity);
    }

    //Damage Entity; returns true if dead
    public boolean damage(double damage) {
        damage /= Math.pow(entity.attributes.getOrDefault("immunity", 2).doubleValue(), immunity);
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
            final String behaviorId = stringFromMap(contactAttack, "tickId", "contact");
            final double damage = numberFromMap(contactAttack, "damage", 0).doubleValue();
            final double bounce = numberFromMap(contactAttack, "bounce", 0).doubleValue();
            final double range = numberFromMap(contactAttack, "range", Math.ceil(entity.size / config.tileSize)).doubleValue();
            final int rate = (int) Math.floor(config.fps / numberFromMap(contactAttack, "rate", 1).doubleValue());
            final double prob = numberFromMap(contactAttack, "prob", 1).doubleValue();

            if (Math.sqrt(playerDistance[0] * playerDistance[0] + playerDistance[1] * playerDistance[1]) < config.tileSize * range) {
                long ticks = incrementTicks(behaviorId, 1) - 1;
                if (ticks % rate == 0 && ThreadLocalRandom.current().nextDouble() <= prob) {
                    player.damage(damage, true);
                    entity.vel[0] *= -bounce;
                    entity.vel[1] *= -bounce;
                }
            } else {
                entity.ticks.put(behaviorId, new long[]{0});
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
        final long ticks = entity.ticks.get(key)[0];
        entity.ticks.put(key, new long[]{ticks + amount});
        return ticks + amount;
    }

    private Map<String, Object> registerBrain(String brainType) {
        Map<String, Object> brain = brainMap.get(brainType);
        if (brain != null) {
            String behaviorId = stringFromMap(brain, "tickId", brainType);
            if (!entity.ticks.containsKey(behaviorId)) {
                entity.ticks.put(behaviorId, new long[]{0});
            }
            return brain;
        }
        return new HashMap<>();
    }

    private static Number numberFromMap(Map<String, Object> map, String key, Number defaultValue) {
        if (map.get(key) instanceof Number number) {
            return number;
        }
        return defaultValue;
    }

    private static String stringFromMap(Map<String, Object> map, String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue).toString();
    }

    private static boolean booleanFromMap(Map<String, Object> map, String key, boolean defaultValue) {
        Object val = map.get(key);
        if (val instanceof Boolean aBoolean) return aBoolean;
        return defaultValue;
    }

    private double getAngle(double[] targetPos) {
        double[] distance = new double[]{
            targetPos[0] - entity.pos[0],
            targetPos[1] - entity.pos[1]
        };
        double result = Math.toDegrees(Math.atan(-distance[0] / distance[1]));
        if (distance[1] > 0) {
            result += 180;
        }
        if (result < 0) {
            result += 360;
        }

        return result;
    }
}
