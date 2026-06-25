package cz.wolverstone.agonia.packbranding.client.config;

import cz.wolverstone.agonia.packbranding.PackBranding;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared helpers for reading PackBranding config files, all located under
 * {@code config/packbranding/}.
 *
 * <p>Handles creating the directory, writing default content on first run and
 * reading existing files, so the individual config classes only deal with
 * parsing.
 */
public final class ConfigFile {
    public static final String CONFIG_FOLDER = "packbranding";

    private ConfigFile() {
    }

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FOLDER);
    }

    public static Path resolve(String fileName) {
        return getConfigDir().resolve(fileName);
    }

    /**
     * Ensures the config directory exists, returning {@code false} if it could
     * not be created.
     */
    public static boolean ensureDir() {
        Path dir = getConfigDir();
        if (Files.exists(dir)) {
            return true;
        }
        try {
            Files.createDirectories(dir);
            PackBranding.LOGGER.info("Created config directory: {}", dir);
            return true;
        } catch (IOException e) {
            PackBranding.LOGGER.error("Failed to create config directory: {}", dir, e);
            return false;
        }
    }

    /**
     * Writes the given default content to the file if it does not exist yet.
     */
    public static void writeDefaultIfMissing(Path file, String defaultContent) {
        if (Files.exists(file)) {
            return;
        }
        try {
            Files.writeString(file, defaultContent);
            PackBranding.LOGGER.info("Created default config: {}", file);
        } catch (IOException e) {
            PackBranding.LOGGER.error("Failed to write default config: {}", file, e);
        }
    }

    public static Reader newReader(Path file) throws IOException {
        return Files.newBufferedReader(file);
    }

    public static String readString(Path file) throws IOException {
        return Files.readString(file);
    }

    public static boolean exists(Path file) {
        return Files.exists(file);
    }
}
