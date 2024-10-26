
import java.util.Map;


public class Brain {
    public static Map<String, Map<String, String>> textures = Map.of(
        "slime", Map.of(
            "baseColor", "#71cf01"
        )
    );

    public static void update(Entity entity) {
        switch (entity.type) {
            case "slime" -> {
                if (entity.health <= 0) {
                    World.worlds.get(World.level).entities.remove(entity);
                }
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
                    if (magnitude <= Entity.attributes.get(entity.type).get("view") * HoneySuckle.tileSize) {
                        if (magnitude == 0) {
                            magnitude = 1;
                        }
                        double coefficient = 15 / magnitude;
                        entity.vel[0] += distance[0] * coefficient;
                        entity.vel[1] += distance[1] * coefficient;
                    }
                }
                entity.pos = World.worlds.get(World.level).bound(entity.pos, entity.vel, entity.size / 2);
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
