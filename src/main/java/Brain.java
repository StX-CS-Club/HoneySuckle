
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

    public static final Map<String, Map<String, Object>> entityBrain = new HashMap<>();

    private final Entity entity;
    private final World world;
    private final Map<String, Object> brainMap;
    private final Map<String, Boolean> states = new HashMap<>();

    // Attack Brain Maps
    private final Map<String, Number> lungeAttack;
    private final Map<String, Number> shootAttack;
    private final Map<String, Number> contactAttack;
    private final Map<String, Number> flee;
    private final Map<String, Number> chase;

    public double chaseAngle = 180;

    public final Map<String, Number> death;

    public Brain(Entity entity, World world) {
        this.entity = entity;
        this.world = world;
        brainMap = entityBrain.get(entity.type);

        lungeAttack = mapFromMap(brainMap, "lunge");
        shootAttack = mapFromMap(brainMap, "shoot");
        contactAttack = mapFromMap(brainMap, "contact");
        flee = mapFromMap(brainMap, "flee");
        chase = mapFromMap(brainMap, "chase");

        death = mapFromMap(brainMap, "death");
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
        if(chaseAngle < 0){
            chaseAngle += 360;
        }

        //Friction
        for (int i = 0; i < 2; i++) {
            entity.vel[i] /= 2;
            if (Math.abs(entity.vel[i]) <= 0.2) {
                entity.vel[i] = 0;
            }
        }

        if (!lungeAttack.isEmpty()) {
            final int rate = readMap(lungeAttack, "rate", 1).intValue();
            final double view = readMap(lungeAttack, "view", 10).doubleValue();
            final double length = readMap(lungeAttack, "length", 1).doubleValue();

            if (entity.ticks % FPS / rate == 0) {
                //If within range of view, do a little hop
                if (playerAbsDistance <= view * TILE_SIZE) {
                    double coefficient = TILE_SIZE * length / Math.max(1, playerAbsDistance);
                    entity.vel[0] += playerDistance[0] * coefficient;
                    entity.vel[1] += playerDistance[1] * coefficient;
                }
            }
        }

        if (!shootAttack.isEmpty()) {
            double healthLost = entity.attributes.get("health") - entity.health;
            double panicSpeed = readMap(shootAttack, "panicSpeed", 0).doubleValue();
            double speed = readMap(shootAttack, "speed", 1).doubleValue();
            final double panicVel = readMap(shootAttack, "panicVel", panicSpeed).doubleValue() * healthLost;
            final double vel = readMap(shootAttack, "vel", speed).doubleValue() + panicVel;
            panicSpeed *= healthLost;
            speed += panicSpeed;
            final double cooldown = readMap(shootAttack, "cooldown", 100).doubleValue();
            final int frames = readMap(shootAttack, "frames", 10).intValue();
            final double range = readMap(shootAttack, "range", 100).doubleValue();
            final String projectileId = Projectile.projStringId.get(readMap(shootAttack, "proj", 1).intValue());

            if (playerAbsDistance <= range * TILE_SIZE) {
                if(entity.ticks < frames){
                    states.put("shooting", true);
                } else {
                    states.put("shooting", false);
                }
                if (entity.ticks * speed > cooldown * FPS / 40.0 / speed) {
                    entity.ticks = 0;
                }
                if (entity.ticks == Math.floor(frames / speed)) {
                    Projectile projectile = new Projectile(projectileId, entity.pos, entity.vel, chaseAngle, entity);
                    projectile.alterVel(entity.pos, entity.vel, chaseAngle, vel, entity);
                    world.projectiles.add(projectile);
                }
            }
        }

        boolean fleeing = false;
        if (!flee.isEmpty()) {
            final double range = readMap(flee, "range", 5).doubleValue();
            final double speed = readMap(flee, "speed", 0.1).doubleValue();
            final double hesitateRange = readMap(flee, "hesitateRange", range).doubleValue();
            if(playerAbsDistance <= hesitateRange * TILE_SIZE){
                fleeing = true;
            }
            if (playerAbsDistance <= range * TILE_SIZE) {
                entity.vel[0] -= TILE_SIZE*speed*-Math.cos(Math.toRadians(chaseAngle+90));
                entity.vel[1] -= TILE_SIZE*speed*Math.sin(Math.toRadians(chaseAngle-90));
            }
        }

        if (!chase.isEmpty() && !fleeing) {
            final double range = readMap(chase, "range", 5).doubleValue();
            final double speed = readMap(chase, "speed", 0.1).doubleValue();
            if (playerAbsDistance <= range * TILE_SIZE) {
                entity.vel[0] += TILE_SIZE*speed*-Math.cos(Math.toRadians(chaseAngle+90));
                entity.vel[1] += TILE_SIZE*speed*Math.sin(Math.toRadians(chaseAngle-90));
            }
        }

        //Update velocity and events based on pos
        entity.pos = world.bound(entity.pos, entity.vel, entity.size / 2.0);
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
            System.out.println("y");
            final int gateId = readMap(death, "gateId", 0).intValue();
            if (gateId != 0) {
                System.out.print("yo");
                boolean cleared = true;
                for (Entity worldEntity : world.entities) {
                    if (readMap(worldEntity.brain.death, "gateId", 0).intValue() == gateId && worldEntity != entity) {
                        cleared = false;
                        break;
                    }
                }
                if (cleared) {
                    System.out.println("yoo");
                    for (WorldObject[] objColumn : world.objGrid) {
                        for (int y = 0; y < objColumn.length; y++) {
                            if (objColumn[y] != null) {
                                if (objColumn[y].readValue("deathGateId").intValue() == gateId) {
                                    objColumn[y] = null;
                                }
                            }
                        }
                    }
                }
            }
        }
        world.entities.remove(entity);
    }

    //Entity interacts with player
    public void event(Player player) {
        double[] playerDistance = new double[]{
            player.pos[0] - entity.pos[0],
            player.pos[1] - entity.pos[1]
        };

        if (!contactAttack.isEmpty()) {
            final double damage = readMap(contactAttack, "damage", 0).doubleValue();
            final double bounce = readMap(contactAttack, "bounce", 0).doubleValue();
            final double range = readMap(contactAttack, "range", 1).doubleValue();
            final int rate = readMap(contactAttack, "rate", 1).intValue();

            if (checkTicks(entity, rate)) {
                if (Math.sqrt(playerDistance[0] * playerDistance[0] + playerDistance[1] * playerDistance[1]) < TILE_SIZE * range) {
                    player.damage(damage);
                    entity.vel[0] *= -bounce;
                    entity.vel[1] *= -bounce;
                }
            }

        }
    }

    public boolean checkState(String state){
        if(states.get(state) != null){
            return states.get(state);
        }
        return false;
    }

    //If entity's ticks are less than n, return true
    private static boolean checkTicks(Entity entity, int n) {
        for (int i = 0; i < n; i++) {
            if ((entity.ticks + i) % FPS == 0) {
                return true;
            }
        }
        return false;
    }

    private static <T> Map<String, T> mapFromMap(Map<String, Object> map, String key) {
        if (map.get(key) instanceof Map<?, ?>) {
            try {
                Map<String, T> result = (Map<String, T>) map.get(key);
                return result;
            } catch (Exception e) {
                System.out.println("HoneySuckle ERROR: Expected Map<String, Object> under key '" + key + "'");
            }
        }
        return new HashMap<>();
    }

    private static <T> T readMap(Map<String, T> map, String key, T defaultValue) {
        if (map.get(key) != null) {
            return map.get(key);
        }
        return defaultValue;
    }
}
