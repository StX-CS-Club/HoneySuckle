package honey.mechanics;

import java.io.File;

import javax.swing.JFileChooser;

public class FileManager {

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
            return ensureDirectoryExists(userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "HoneySuckle");
        } else if (os.contains("mac")) {
            return ensureDirectoryExists(userHome + File.separator + "Library" + File.separator + "Application Support" + File.separator + "HoneySuckle");
        } else {
            return ensureDirectoryExists(userHome + File.separator + ".honeysuckle");
        }
    }

    public static String ensureDirectoryExists(String directoryPath) {
        new File(directoryPath).mkdirs();
        return directoryPath;
    }
}
