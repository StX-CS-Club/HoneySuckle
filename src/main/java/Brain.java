public class Brain {

    public static void update(Entity entity) {
        switch (entity.type) {
            case "slime" -> {
                for (int i = 0; i < 2; i++) {
                    entity.vel[i] /= 2;
                    if (Math.abs(entity.vel[i]) <= 0.2) {
                        entity.vel[i] = 0;
                    }
                }
                Player player = HoneySuckle.player;
                if (entity.ticks % HoneySuckle.fps / 2 == 0) {
                    double[] distance = new double[]{
                        player.pos[0] - entity.pos[0],
                        player.pos[1] - entity.pos[1]
                    };
                    double magnitude = Math.sqrt(distance[0] * distance[0] + distance[1] * distance[1]);
                    if (magnitude <= Entity.entityAttributes.get(entity.type).get("view") * HoneySuckle.tileSize) {
                        if (magnitude == 0) {
                            magnitude = 1;
                        }
                        double coefficient = 15 / magnitude;
                        entity.vel[0] += distance[0] * coefficient;
                        entity.vel[1] += distance[1] * coefficient;
                        entity.direction[0] = (int) Math.signum(entity.vel[0]);
                    }
                }
                entity.pos = World.worlds.get(World.level).bound(entity.pos, entity.vel, entity.size / 2);
                World.worlds.get(World.level).entityEvent(entity);
            }
            case "dragon" -> {
                World world = World.worlds.get(World.level);
                Player player = HoneySuckle.player;
                if (entity.ticks > entity.attributes.get("cooldown")) {
                    entity.ticks = 0;
                }
                if (entity.ticks == entity.attributes.get("frames")) {
                    double[] distance = new double[]{
                        entity.pos[0] - player.pos[0],
                        entity.pos[1] - player.pos[1]
                    };
                    double angle = Math.toDegrees(Math.atan(distance[0] / -distance[1]));
                    if(distance[1] < 0){
                        angle += 180;
                    }
                    world.projectiles.add(new Projectile("fireball", entity.pos, angle, entity));
                }
                for (int i = 0; i < 2; i++) {
                    entity.vel[i] /= 5;
                    if (Math.abs(entity.vel[i]) <= 0.2) {
                        entity.vel[i] = 0;
                    }
                }
                entity.pos = world.bound(entity.pos, entity.vel, entity.size / 2);
                World.worlds.get(World.level).entityEvent(entity);
            }
        }
    }

    public static void die(Entity entity, World world){
        switch(entity.type){
            case "slime" -> {
                world.entities.remove(entity);
            }
            case "dragon" -> {
                world.entities.remove(entity);
                if(world.entities.isEmpty()){
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

    public static void event(Entity entity, Player player) {
        switch (entity.type) {
            case "slime" -> {
                if (checkTicks(entity, 2)) {
                    if (Math.sqrt(Math.pow(entity.pos[0] - player.pos[0], 2) + Math.pow(entity.pos[1] - player.pos[1], 2)) < HoneySuckle.tileSize) {
                        player.health -= 0.05;
                    }
                }
            }

        }
    }

    private static boolean checkTicks(Entity entity, int n) {
        for (int i = 0; i < n; i++) {
            if ((entity.ticks + i) % HoneySuckle.fps == 0) {
                return true;
            }
        }
        return false;
    }
}
