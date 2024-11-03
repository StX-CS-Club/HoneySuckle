
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

public class Weapon {

    public static Map<String, Map<String, Double>> weaponAttributes = new HashMap<>();
    public static Map<String, String> weaponTypes = new HashMap<>();
    public static Map<String, Map<String, String>> weaponTextures = new HashMap<>();

    private int attackFrame;
    private int coolDown;

    private final String type;
    private final Map<String, Double> attributes;
    private final Map<String, String> texture;

    public Weapon(String weapon) {
        type = weaponTypes.get(weapon);
        attributes = weaponAttributes.get(weapon);
        attackFrame = attributes.get("frames").intValue() + 1;
        texture = weaponTextures.get(weapon);
    }

    public void attack() {
        if (coolDown <= 0) {
            attackFrame = 0;
            coolDown = attributes.get("cooldown").intValue();
        }
    }

    public void update(Player player) {
        coolDown -= 1;
        if (attackFrame < attributes.get("frames")) {
            attackFrame++;

            World world = World.worlds.get(World.level);
            switch (type) {
                case "blade" -> {
                    double size = HoneySuckle.tileSize * attributes.get("size");
                    for (Entity entity : world.renderEntities) {
                        if (Math.abs(entity.pos[0] - player.pos[0]) <= size && Math.abs(entity.pos[1] - player.pos[1]) <= size) {
                            if (hitBox(player.pos, entity.pos, player.rotation, player.size, size)) {
                                if(entity.damage(attributes.get("damage"))){
                                    Map<String, Integer> loot = Entity.entityLoot.get(entity.type);
                                    for(String material : loot.keySet()){
                                        player.crafting.materials.put(material, player.crafting.getOrDefault(material) + loot.get(material));
                                    }
                                }
                            }
                        }
                    }
                    int sizeTiles = (int) Math.floor(size / HoneySuckle.tileSize) + 1;
                    int[] posIndex = new int[]{
                        (int) Math.floor(player.pos[0] / HoneySuckle.tileSize),
                        (int) Math.floor(player.pos[1] / HoneySuckle.tileSize)
                    };
                    for (int x = posIndex[0] - sizeTiles; x < posIndex[0] + sizeTiles; x++) {
                        for (int y = posIndex[1] - sizeTiles; y < posIndex[1] + sizeTiles; y++) {
                            if (x >= 0 && x < world.size[0] && y >= 0 && y < world.size[1]) {
                                if (world.objGrid[x][y] != null) {
                                    if (hitBox(player.pos, new double[]{(x + 0.5) * HoneySuckle.tileSize, (y + 0.5) * HoneySuckle.tileSize}, player.rotation, player.size, size)) {
                                        WorldObject obj = world.objGrid[x][y];
                                        if (world.objGrid[x][y].damage(attributes.get("damage"))) {
                                            Map<String, Integer> loot = obj.loot;
                                            for (String material : loot.keySet()) {
                                                player.crafting.materials.put(material, player.crafting.getOrDefault(material) + loot.get(material));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void render(Graphics2D g, Player player) {
        double[] screenPos = new double[]{
            HoneySuckle.size[0] / 2 + player.pos[0] - World.worlds.get(World.level).camera[0] - attributes.get("size")*HoneySuckle.tileSize/2+player.size/2,
            HoneySuckle.size[1] / 2 + player.pos[1] - World.worlds.get(World.level).camera[1] - attributes.get("size")*HoneySuckle.tileSize+player.size/2
        };
        if (attackFrame < attributes.get("frames")) {
            g.drawImage(
                Rendering.replaceGradient(Rendering.renderGIF("images/gifs/slash.gif", ((double) attackFrame) / attributes.get("frames")), texture.get("bladeColor")),
                (int) screenPos[0], (int) screenPos[1], HoneySuckle.tileSize * attributes.get("size").intValue(), HoneySuckle.tileSize * attributes.get("size").intValue(), null);
        }
    }

    private boolean hitBox(double[] origin, double[] point, double angle, double margin, double size) {
        double[] pos = new double[]{
            point[0] - origin[0] - margin,
            point[1] - origin[1] - margin
        };

        double sin = Math.sin(-angle);
        double cos = Math.cos(-angle);

        double[] rotatedPos = new double[]{
            pos[0] * cos - pos[1] * sin,
            pos[0] * sin + pos[1] * cos
        };

        return (rotatedPos[0] >= -size && rotatedPos[0] <= size && rotatedPos[1] >= -size && rotatedPos[1] <= size);
    }
}
