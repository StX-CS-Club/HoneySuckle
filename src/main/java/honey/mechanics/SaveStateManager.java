package honey.mechanics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import honey.HoneySuckle;
import honey.player.Player;
import honey.world.World;

public class SaveStateManager {

    public static ConfigManager config;

    private static final ObjectMapper objectMapper = DataManager.objectMapper;

    public static boolean saveGame() {
        final String filePath = FileManager.getFilePath("Save Game", saveFileDirectory(), "HoneySuckle Save File", config.saveFileExtension, true);
        if (filePath == null) {
            return false;
        }
        return saveToFile(filePath);
    }

    private static boolean saveToFile(String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath); GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            objectMapper.writeValue(gos, getSaveJson());
            return true;
        } catch (IOException e) {
            System.out.println("HONEYSUCKLE ERROR: Failed to save game state to " + filePath);
            return false;
        }
    }

    public static void loadGame() {
        final String filePath = FileManager.getFilePath("Load Game", saveFileDirectory(), "HoneySuckle Save File", config.saveFileExtension, false);
        if (filePath == null) {
            return;
        }

        loadFromFile(filePath);
    }

    @SuppressWarnings("unchecked")
    private static void loadFromFile(String filePath) {
        try (final FileInputStream fis = new FileInputStream(filePath); final GZIPInputStream gis = new GZIPInputStream(fis)) {
            final Map<String, Object> saveData = objectMapper.readValue(gis, Map.class);

            HoneySuckle.stop();

            final Object seedJson = (Number) saveData.get("seed");
            if (seedJson instanceof Number seedNumber) {
                GameRandom.seed(seedNumber.longValue());
            }

            World.level = ((Number) saveData.get("level")).intValue();
            for (int i = 0; i < World.level; i++) {
                World.worlds.add(null);
            }
            final World world = new World((Map<String, Object>) saveData.get("world"));

            final Map<String, Object> playerJson = (Map<String, Object>) saveData.get("player");
            final List<Number> posJson = (List<Number>) playerJson.get("pos");
            final double[] pos = {posJson.get(0).doubleValue(), posJson.get(1).doubleValue()};

            HoneySuckle.player = new Player(pos, playerJson);
            world.camera[0] = pos[0];
            world.camera[1] = pos[1];

            HoneySuckle.play();
        } catch (IOException e) {
            System.out.println("HONEYSUCKLE ERROR: Failed to load game state from " + filePath);
        }
    }

    private static Map<String, Object> getSaveJson() {
        return Map.of(
                "version", config.version,
                "seed", GameRandom.seed(),
                "level", World.level,
                "player", HoneySuckle.player.toJson(),
                "world", World.worlds.get(World.level).toJson()
        );
    }

    private static String saveFileDirectory() {
        return FileManager.getApplicationDirectory() + File.separator + config.saveFileDirectory;
    }
}
