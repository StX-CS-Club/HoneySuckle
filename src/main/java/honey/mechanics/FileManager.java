package honey.mechanics;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileManager {

    public static ConfigManager config;

    public static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
    };

    public static String getFilePath(String dialog, String directory, String fileType, String extension, boolean newFile) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialog);
        if(directory != null) {
            fileChooser.setCurrentDirectory(new File(directory));
        }
        if(extension != null) {
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(fileType, extension));
        }


        if (newFile) {
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = fileChooser.getSelectedFile();
                final String filePath = selectedFile.getAbsolutePath();
                if (!filePath.endsWith("." + extension)) {
                    return filePath + "." + extension;
                }
                return selectedFile.getAbsolutePath();
            }
        } else {
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = fileChooser.getSelectedFile();
                return selectedFile.getAbsolutePath();
            }
        }
        return null;
    }


    public static String getApplicationDirectory() {
        final String os = System.getProperty("os.name").toLowerCase();
        final String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return ensureDirectoryExists(userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + config.applicationDataDirectory);
        } else if (os.contains("mac")) {
            return ensureDirectoryExists(userHome + File.separator + "Library" + File.separator + "Application Support" + File.separator + config.applicationDataDirectory);
        } else {
            return ensureDirectoryExists(userHome + File.separator + "." + config.applicationDataDirectory);
        }
    }

    public static String ensureDirectoryExists(String directoryPath) {
        new File(directoryPath).mkdirs();
        return directoryPath;
    }

    public static ConfigManager readConfig() {
        try {
            final URL url = FileManager.class.getResource("/jsonData/config.json");
            if (url != null) {
                final Map<String, Object> data = objectMapper.readValue(new File(url.toURI()), mapType);
                return new ConfigManager(data);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("DataManager INFO: Using default config values.");
        }
        return new ConfigManager(new HashMap<>());
    }

    public static Map<String, Object> readJsonDirectory(URI directory) throws IOException {
        final Map<String, Object> result = new HashMap<>();
        final File directoryFile = new File(directory);
        for (File file : directoryFile.listFiles()) {
            if (file.isDirectory()) {
                result.putAll(readJsonDirectory(file.toURI()));
            } else {
                if (file.toURI().toString().contains(".json")) {
                    result.putAll(objectMapper.readValue(file, mapType));
                }
            }
        }
        return result;
    }
}