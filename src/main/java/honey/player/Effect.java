package honey.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Effect {

    public static final Map<String, Map<String, String>> effectTextures = new HashMap<>();
    public static final Map<String, List<String>> effectTags = new HashMap<>();
    public static final Map<String, Map<String, Number>> effectModifiers = new HashMap<>();
    public static final Map<String, String> effectNames = new HashMap<>();
    public static final Map<String, Integer> effectIntId = new HashMap<>();
    public static final Map<Integer, String> effectStringId = new HashMap<>();

    private final String type;
    private final int duration;
    private final int amplifier;

    private final Map<String, String> texture;
    private final Map<String, Number> modifiers;
    private final List<String> tags;

    public Effect(String type, int duration, int amplifier){
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;

        texture = effectTextures.get(type);
        modifiers = effectModifiers.get(type);
        tags = effectTags.get(type);
    }

    
}
