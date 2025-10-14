package honey.player.inventory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.HoneySuckle;
import honey.mechanics.MapReader;
import honey.player.Player;
import honey.player.armory.Effect;
import honey.rendering.Rendering;
import honey.world.World;

public class KeyItem {

    private static final int HUD_SIZE = HoneySuckle.HUD_SIZE;
    
    //Static json data
    public static final Map<String, String> keyNames = new HashMap<>();
    public static final Map<String, Map<String, String>> keyTextures = new HashMap<>();
    public static final Map<String, Map<String, Number>> keyAttributes = new HashMap<>();
    public static final Map<String, List<String>> keyBlueprintUnlocks = new HashMap<>();
    public static final Map<String, List<String>> keyRecipeUnlocks = new HashMap<>();
    public static final Map<String, Map<String, Map<String, Object>>> keyUtilities = new HashMap<>();
    public static final Map<Integer, String> keyStringId = new HashMap<>();
    public static final Map<String, Integer> keyIntId = new HashMap<>();

    final String id;
    int count;

    private final String name;
    private final Map<String, String> texture;
    private final Map<String, Number> attributes;
    private final Map<String, Map<String, Object>> utilities;

    private final Map<String, Integer> defaultUtilUses = new HashMap<>();

    private final Map<String, Integer> utilUses = new HashMap<>();
    private final Map<String, Long> utilFrames = new HashMap<>();
    private final Map<String, Integer> utilAnimFrames = new HashMap<>();
    private int useFrames = 0;

    private final Map<String, Object> mapUtility;
    private final Map<String, Object> potionUtility;

    public KeyItem(String id, int count) {
        this.id = id;
        this.count = count;

        name = keyNames.get(id);
        texture = keyTextures.get(id);
        attributes = keyAttributes.get(id);
        utilities = keyUtilities.get(id);

        mapUtility = registerUtility("map");
        potionUtility = registerUtility("potion");

        setUtil();
    }

    public void renderUiTile(Graphics2D g, int x, int y, double factor) {
        String color = null;
        if (count > 0) {
            color = "#f5d39d";
        }

        Rendering.imageFactor(Rendering.texture("ui/slots/key_item", color), g, x, y, 100, 100, factor);

        boolean ready = utilAnimFrames.isEmpty();
        if (!ready) {
            g.setColor(new Color(128, 128, 128, 128 / utilAnimFrames.size()));
            for (String key : utilAnimFrames.keySet()) {
                final int animFrames = utilAnimFrames.get(key);
                if (animFrames > 0) {
                    final int size = (int) Math.ceil(utilFrames.get(key) / (double) animFrames * 100);
                    if (size > 0 && size <= 100) {
                        g.fillRect(x, y + size, 100, 100 - size);
                    } else {
                        ready = true;
                    }
                }
            }
        }

        final String itemTexture = texture.get("texture");
        if (itemTexture != null) {
            if (useFrames > 0) {
                Rendering.imageFactor(Rendering.texture(itemTexture, null), g, x + 15, y + 15, 70, 70, 0.8);
                useFrames--;
            } else {
                g.drawImage(Rendering.texture(itemTexture, null), x + 15, y + 15, 70, 70, null);
            }
        }

        final String label = name + " x" + count;

        if (ready) {
            g.setColor(new Color(224, 224, 224));
        } else {
            g.setColor(new Color(128, 128, 128));
        }

        // Draws the font
        Rendering.centeredText(g, label, x + 50, y + 115, 100, 24);
    }

    public void renderHotTile(Graphics2D g, int x, int y, double factor) {
        String color = null;
        if (count > 0) {
            color = "#f5d39d";
        }

        Rendering.imageFactor(Rendering.texture("ui/hud/hotslot", color), g, x, y, HUD_SIZE, HUD_SIZE, factor);

        boolean ready = utilAnimFrames.isEmpty();
        if (!ready) {
            g.setColor(new Color(128, 128, 128, 128 / utilAnimFrames.size()));
            for (String key : utilAnimFrames.keySet()) {
                final int animFrames = utilAnimFrames.get(key);
                if (animFrames > 0) {
                    final int size = (int) Math.ceil(utilFrames.get(key) / (double) animFrames * HUD_SIZE);
                    if (size > 0 && size <= HUD_SIZE) {
                        g.fillRect(x, y + size, HUD_SIZE, HUD_SIZE - size);
                    } else {
                        ready = true;
                    }
                }
            }
        }

        final String itemTexture = texture.get("texture");
        if (itemTexture != null) {
            if (useFrames > 0) {
                Rendering.imageFactor(Rendering.texture(itemTexture, null), g, x + HUD_SIZE / 8, y + HUD_SIZE / 8, HUD_SIZE * 3 / 4, HUD_SIZE * 3 / 4, 0.8);
                useFrames--;
            } else {
                g.drawImage(Rendering.texture(itemTexture, null), x + HUD_SIZE / 8, y + HUD_SIZE / 8, HUD_SIZE * 3 / 4, HUD_SIZE * 3 / 4, null);
            }
        }
    }

    public void update() {
        for (String key : utilFrames.keySet()) {
            long frame = utilFrames.get(key);
            if (frame != -1) {
                utilFrames.put(key, frame + 1l);
            }
        }

        boolean reset = !utilUses.isEmpty();

        for (Integer use : utilUses.values()) {
            if (use != 0) {
                reset = false;
                break;
            }
        }

        if (reset) {
            setUtil();
            count--;
        }
    }

    public void use(Player player) {
        final World world = World.worlds.get(World.level);
        final Map<String, Integer> staticUtilUses = Map.copyOf(utilUses);

        if (count > 0) {
            if (mapUtility != null) {
                final String utilId = (String) mapUtility.get("utilId");
                int uses = staticUtilUses.get(utilId);

                if (uses != 0 && !world.navigator.started) {
                    world.navigator.started = true;
                    utilUses.put(utilId, uses - 1);
                    useFrames = MapReader.getNumberOrDefault(mapUtility, "useFrames", 1).intValue();
                }
            }

            if (potionUtility != null) {
                final String utilId = (String) potionUtility.get("utilId");
                int uses = staticUtilUses.get(utilId);
                long frames = utilFrames.get(utilId);

                final int cooldown = MapReader.getNumberOrDefault(potionUtility, "cooldown", 0).intValue();

                if (uses != 0 && (frames >= cooldown || frames == -1l)) {
                    utilUses.put(utilId, uses - 1);
                    utilFrames.put(utilId, 0l);
                    useFrames = MapReader.getNumberOrDefault(potionUtility, "useFrames", 1).intValue();

                    final List<Map<String, Object>> effects = MapReader.getOrDefault(potionUtility, "effects", new ArrayList<>());
                    for (Map<String, Object> effect : effects) {
                        player.armory.effects.add(new Effect(effect));
                    }
                }
            }
        }
    }

    private Map<String, Object> registerUtility(String utility) {
        final Map<String, Object> utilEntry = utilities.get(utility);
        if (utilEntry != null) {
            utilEntry.putIfAbsent("utilId", "base");
            String utilId = (String) utilEntry.get("utilId");

            final int uses = MapReader.getNumberOrDefault(utilEntry, "uses", 1).intValue();
            if (uses == -1) {
                defaultUtilUses.put(utilId, -1);
            } else {
                final int currentUses = defaultUtilUses.getOrDefault(utilId, 1);
                defaultUtilUses.put(utilId, Math.max(uses, currentUses));
            }

            final int cooldown = MapReader.getNumberOrDefault(utilEntry, "cooldown", 0).intValue();

            final int animFrames = MapReader.getNumberOrDefault(utilEntry, "animFrames", cooldown).intValue();
            final int currentAnimFrames = utilAnimFrames.getOrDefault(utilId, 0);
            if (currentAnimFrames == 0) {
                utilAnimFrames.put(utilId, animFrames);
            } else {
                utilAnimFrames.put(utilId, Math.min(animFrames, currentAnimFrames));
            }

            utilFrames.put(utilId, -1l);
        }
        return utilEntry;
    }

    private void setUtil() {
        utilUses.putAll(defaultUtilUses);
    }
}
