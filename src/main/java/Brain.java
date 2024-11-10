
import java.awt.geom.Point2D;

/* 
 * Brain.java *
 -Handles methods specific to entity types
 */
public class Brain {

    //Update Entity based on type
    public static void update(Entity entity) {
        //Current world
        World world = World.worlds.get(World.level);
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

        //Rule switch case; runs different update methods depending on entity type
        switch (entity.type) {
            case "slime" -> {
                //Friction
                for (int i = 0; i < 2; i++) {
                    entity.vel[i] /= 2;
                    if (Math.abs(entity.vel[i]) <= 0.2) {
                        entity.vel[i] = 0;
                    }
                }
                //If time to attack
                if (entity.ticks % HoneySuckle.fps / 2 == 0) {
                    double[] distance = new double[]{
                        player.pos[0] - entity.pos[0],
                        player.pos[1] - entity.pos[1]
                    };
                    //Magnitude of distance
                    double magnitude = Math.sqrt(distance[0] * distance[0] + distance[1] * distance[1]);
                    //If within range of view, do a little hop
                    if (magnitude <= Entity.entityAttributes.get(entity.type).get("view") * HoneySuckle.tileSize) {
                        if (magnitude == 0) {
                            magnitude = 1;
                        }
                        double coefficient = 15.0 / magnitude;
                        entity.vel[0] += distance[0] * coefficient;
                        entity.vel[1] += distance[1] * coefficient;
                        entity.direction[0] = (int) Math.signum(entity.vel[0]);
                    }
                }
                //Update velocity and events based on pos
                entity.pos = World.worlds.get(World.level).bound(entity.pos, entity.vel, entity.size / 2.0);
                World.worlds.get(World.level).entityEvent(entity);
            }
            case "dragon" -> {
                //Dragon is basically a gun; operated similar to the code for one
                double speed = 1.0 + 1.125 / 19 * (15 - entity.health);
                if (entity.ticks * speed > entity.attributes.get("cooldown") * HoneySuckle.fps / 40.0 / speed) {
                    entity.ticks = 0;
                }
                if (entity.ticks == Math.floor(entity.attributes.get("frames")/speed)) {
                    //Launches projectile at player is time to do so
                    double[] distance = new double[]{
                        entity.pos[0] - player.pos[0],
                        entity.pos[1] - player.pos[1]
                    };
                    double angle = Math.toDegrees(Math.atan(distance[0] / -distance[1]));
                    if (distance[1] < 0) {
                        angle += 180;
                    }
                    Projectile fireball = new Projectile("fireball", entity.pos, entity.vel, angle, entity);
                    fireball.alterVel(entity.pos, entity.vel, angle, speed, entity);
                    world.projectiles.add(fireball);
                }
                //Friction
                for (int i = 0; i < 2; i++) {
                    entity.vel[i] /= 5;
                    if (Math.abs(entity.vel[i]) <= 0.2) {
                        entity.vel[i] = 0;
                    }
                }
                //Update vel and events based on pos
                entity.pos = world.bound(entity.pos, entity.vel, entity.size / 2.0);
                World.worlds.get(World.level).entityEvent(entity);
            }
        }
    }

    //Kill Entity events
    public static void die(Entity entity, World world) {
        switch (entity.type) {
            case "slime" -> {
                //Just dies
                world.entities.remove(entity);
            }
            case "dragon" -> {
                //Dies, and removes all gates if last one
                world.entities.remove(entity);
                if (world.entities.isEmpty()) {
                    for (WorldObject[] objColumn : world.objGrid) {
                        for (int y = 0; y < objColumn.length; y++) {
                            if (objColumn[y] != null) {
                                if (objColumn[y].id == 5) {
                                    objColumn[y] = null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //Entity interacts with player
    public static void event(Entity entity, Player player) {
        switch (entity.type) {
            case "slime" -> {
                //If attacking, deals damage
                if (checkTicks(entity, 2)) {
                    if (Math.sqrt(Math.pow(entity.pos[0] - player.pos[0], 2) + Math.pow(entity.pos[1] - player.pos[1], 2)) < HoneySuckle.tileSize) {
                        player.damage(0.05);
                        entity.vel[0] *= -2;
                        entity.vel[1] *= -2;
                    }
                }
            }

        }
    }

    //If entity's ticks are less than n, return true
    private static boolean checkTicks(Entity entity, int n) {
        for (int i = 0; i < n; i++) {
            if ((entity.ticks + i) % HoneySuckle.fps == 0) {
                return true;
            }
        }
        return false;
    }
}
