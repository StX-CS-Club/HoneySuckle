package honey.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import honey.mechanics.MapReader;
import honey.player.armory.Ammo;
import honey.player.armory.Armor;
import honey.player.armory.Weapon;
import honey.player.build.Blueprint;
import honey.player.inventory.Craft;
import honey.player.inventory.Inventory;
import honey.player.inventory.Item;
import honey.player.inventory.KeyItem;

// Processes a recursive loot list: rolls each entry's own prob/count/countProb, grants it (or spawns an
// entity for "type": "entity"), and — only once that entry's own condition is met — recurses into its
// optional nested "loot" child list. Owns every probability roll for loot (moved out of World/Inventory).
public class LootDistributer {

    private final World world;
    private final Inventory inventory;
    private final double[] spawnPos;

    public LootDistributer(World world, Inventory inventory, double[] spawnPos) {
        this.world = world;
        this.inventory = inventory;
        this.spawnPos = spawnPos;
    }

    public void process(List<Map<String, Object>> loot) {
        for (Map<String, Object> entry : loot) {
            final String type = MapReader.getOrDefault(entry, "type", "item");
            final boolean met = "entity".equals(type) ? processEntity(entry) : processGrant(entry, type);
            if (met) {
                process(MapReader.getOrDefault(entry, "loot", new ArrayList<>()));
            }
        }
    }

    private boolean processEntity(Map<String, Object> entry) {
        final double prob = MapReader.getNumberOrDefault(entry, "prob", 1).doubleValue();
        if (Math.random() >= prob) {
            return false;
        }

        int count = MapReader.getNumberOrDefault(entry, "count", 1).intValue();
        final double countProb = MapReader.getNumberOrDefault(entry, "countProb", 0).doubleValue();
        while (ThreadLocalRandom.current().nextDouble() <= countProb) {
            count++;
        }

        final String entityType = MapReader.getOrDefault(entry, "id", "");
        if (!entityType.isEmpty()) {
            final double burst = MapReader.getNumberOrDefault(entry, "burst", 0).doubleValue();
            for (int i = 0; i < count; i++) {
                final double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
                final double scatter = World.config.tileSize * 0.3;
                final double[] pos = {
                    spawnPos[0] + Math.cos(angle) * scatter,
                    spawnPos[1] + Math.sin(angle) * scatter
                };
                final Entity spawned = new Entity(entityType, pos, world);
                if (burst > 0) {
                    final double burstAngle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
                    spawned.vel[0] = Math.cos(burstAngle) * burst * World.config.tileSize;
                    spawned.vel[1] = Math.sin(burstAngle) * burst * World.config.tileSize;
                }
                spawned.brain.immunity = 3.0;
                world.entities.add(spawned);
            }
        }

        return true;
    }

    private boolean processGrant(Map<String, Object> entry, String type) {
        final double prob = MapReader.getNumberOrDefault(entry, "prob", 1).doubleValue();
        if (Math.random() >= prob) {
            return false;
        }

        if (inventory != null) {
            int count = MapReader.getNumberOrDefault(entry, "count", 1).intValue();
            final double countProb = MapReader.getNumberOrDefault(entry, "countProb", 0).doubleValue();
            while (ThreadLocalRandom.current().nextDouble() <= countProb) {
                count++;
            }

            if (count != 0) {
                final String stringId = MapReader.getOrDefault(entry, "id", "");
                final Integer intId = resolveId(type, stringId);
                if (intId != null) {
                    final Map<String, Number> resolved = new HashMap<>(MapReader.castMap(entry, Number.class));
                    resolved.put("id", intId);
                    resolved.put("type", resolveType(type));
                    inventory.incrementItem(resolved, count);
                }
            }
        }

        return true;
    }

    private static Integer resolveId(String type, String stringId) {
        return switch (type) {
            case "weapon" -> Weapon.weaponIntId.get(stringId);
            case "armor" -> Armor.armorIntId.get(stringId);
            case "ammo" -> Ammo.ammoIntId.get(stringId);
            case "keyItem" -> KeyItem.keyIntId.get(stringId);
            case "recipe" -> Craft.recipeIntId.get(stringId);
            case "blueprint" -> Blueprint.blueprintIntId.get(stringId);
            default -> Item.itemIntId.get(stringId);
        };
    }

    private static int resolveType(String type) {
        return switch (type) {
            case "weapon" -> 1;
            case "armor" -> 2;
            case "ammo" -> 3;
            case "keyItem" -> 4;
            case "recipe" -> 5;
            case "blueprint" -> 6;
            default -> 0;
        };
    }
}
