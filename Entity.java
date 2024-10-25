import java.util.Map;
import java.awt.Color;
import java.awt.Graphics2D;

public class Entity {
    private Map<String, Map<String, Number>> attributes = Map.of(
            "slime", Map.of(
                    "damage", 0.01,
                    "speed", 3,
                    "attackSpeed", 1000,
                    "size", HoneySuckle.tileSize / 3.0,
                    "health", 0.5,
                    "view", 5
                    ));

    private String type;
    private double health;
    private double size;
    private double[] pos = new double[2];
    private double[] vel = new double[2];

    public Entity(String type, double[] pos) {
        this.type = type;
        this.health = attributes.get(type).get("health").doubleValue();
        this.size = attributes.get(type).get("size").doubleValue();
        this.pos = pos;
    }

    public void render(Graphics2D g, double[] camera) {
        double[] screenPos = new double[] {
                HoneySuckle.size[0] / 2 + pos[0] - camera[0] - size / 2,
                HoneySuckle.size[1] / 2 + pos[1] - camera[1] - size / 2
        };

        g.setColor(Color.green);
        Rendering.borderRect(g, 1, Color.BLACK, (int) screenPos[0], (int) screenPos[1],
                attributes.get(type).get("size").intValue(), (int) size);
    }

    public void update(){
        if (health <= 0) {
            World.worlds.get(World.level).entities.remove(this);
        }
        for (int i = 0; i < 2; i++) {
            vel[i] /= 2;
            if (Math.abs(vel[i]) <= 0.2) {
                vel[i] = 0;
            }
        }
        Player player = HoneySuckle.player;
        double[] distance = new double[]{
                player.pos[0] - pos[0],
                player.pos[1] - pos[1]
        };
        double magnitude = Math.sqrt(distance[0] * distance[0] + distance[1] * distance[1]);
        if(magnitude <= attributes.get(type).get("view").doubleValue() * HoneySuckle.tileSize){
        if(magnitude == 0){
            magnitude = 1;
        }
        double coefficient = attributes.get(type).get("speed").doubleValue() / magnitude;
        vel[0] += distance[0] * coefficient;
        vel[1] += distance[1] * coefficient;
        pos = World.worlds.get(World.level).bound(pos, vel, size / 2);
        }
    }
}
