package net.arthurllew.mementobeta.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.arthurllew.mementobeta.MementoBeta;

import java.io.*;

public class Config {
    /**
     * Gson object.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Active config data.
     */
    public static Data DATA;

    /**
     * Inits config.
     */
    public static void initConfig() {
        // Config file
        File file = new File("config", MementoBeta.MODID + ".json");

        // If config doesn't exist
        if (!file.exists()) {
            // Init with defaults
            createDefaultConfig();

            // Try to save config
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(DATA, writer);
            }
            // On error: add log
            catch (Exception e) {
                MementoBeta.LOGGER.warn("Unable to save config file due to: ", e);
            }
        }
        // If config already exists
        else {
            // Try to read
            try (Reader reader = new FileReader(file)) {
                DATA = GSON.fromJson(reader, Data.class);
            }
            // On error: set to defaults and add log
            catch (Exception e) {
                MementoBeta.LOGGER.warn("Unable to read config file due to: ", e);
                createDefaultConfig();
            }
        }
    }

    /**
     * Inits config with defaults.
     */
    private static void createDefaultConfig() {
        DATA = new Data();
    }

    /**
     * Data holder.
     */
    public static class Data {
        /**
         * Whether season effects should be applied.
         */
        public boolean doSeasonEffects;

        /**
         * Default data constructor.
         */
        private Data() {
            this.doSeasonEffects = true;
        }
    }
}
