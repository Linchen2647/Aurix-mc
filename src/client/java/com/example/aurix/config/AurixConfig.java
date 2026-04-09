package com.example.aurix.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AurixConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("aurix-client.json");

    public boolean enabled = true;
    public String apiBaseUrl = "https://integrate.api.nvidia.com/v1";
    public String apiKey = "";
    public String model = "meta/llama-3.1-8b-instruct";

    public String botName = "Aurix";
    public String chatPrefix = "[ Aurix ] ";

    public int minSecondsBetweenReplies = 35;
    public int maxContextMessages = 30;
    public int triggerWindowMessages = 8;
    public int minMessagesBeforeOrganicReply = 4;
    public double organicReplyChance = 0.18;
    public int maxReplyChars = 70;

    public boolean replyWhenMentioned = true;
    public boolean ignoreOwnMessages = true;
    public boolean logDebug = true;

    public static AurixConfig load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                AurixConfig config = new AurixConfig();
                config.save();
                return config;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                AurixConfig loaded = GSON.fromJson(reader, AurixConfig.class);
                if (loaded == null) {
                    loaded = new AurixConfig();
                }
                loaded.save();
                return loaded;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AurixConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
