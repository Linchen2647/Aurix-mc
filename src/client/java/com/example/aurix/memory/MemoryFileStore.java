package com.example.aurix.memory;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryFileStore {
    private static final Pattern WINDOWS_ILLEGAL = Pattern.compile("[\\\\/:*?\"<>|]");

    private final Path baseDir = FabricLoader.getInstance().getConfigDir().resolve("aurix").resolve("memory");
    private final Path playersDir = baseDir.resolve("players");
    private final Path globalDir = baseDir.resolve("global");

    private final Map<String, List<String>> playerMemories = new LinkedHashMap<>();
    private final Map<String, String> globalMemories = new LinkedHashMap<>();

    public synchronized void initialize() throws IOException {
        ensureDirectories();
        reload();
    }

    public synchronized void ensureDirectories() throws IOException {
        Files.createDirectories(playersDir);
        Files.createDirectories(globalDir);
    }

    public synchronized void reload() throws IOException {
        ensureDirectories();
        playerMemories.clear();
        globalMemories.clear();

        if (Files.exists(playersDir)) {
            try (Stream<Path> playerPaths = Files.list(playersDir)) {
                for (Path playerPath : playerPaths.filter(Files::isDirectory).sorted().toList()) {
                    String player = playerPath.getFileName().toString();
                    List<String> memories = new ArrayList<>();

                    try (Stream<Path> files = Files.list(playerPath)) {
                        for (Path file : files
                                .filter(Files::isRegularFile)
                                .filter(path -> path.getFileName().toString().endsWith(".txt"))
                                .sorted(Comparator.comparingInt(this::extractNumericPrefix))
                                .toList()) {
                            String content = Files.readString(file, StandardCharsets.UTF_8).trim();
                            if (!content.isBlank()) {
                                memories.add(content);
                            }
                        }
                    }

                    if (!memories.isEmpty()) {
                        playerMemories.put(player, memories);
                    }
                }
            }
        }

        if (Files.exists(globalDir)) {
            try (Stream<Path> files = Files.list(globalDir)) {
                for (Path file : files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".txt"))
                        .sorted()
                        .toList()) {
                    String name = file.getFileName().toString();
                    name = name.substring(0, name.length() - 4);
                    String content = Files.readString(file, StandardCharsets.UTF_8).trim();
                    if (!content.isBlank()) {
                        globalMemories.put(name, content);
                    }
                }
            }
        }
    }

    public synchronized int addPlayerMemory(String playerName, String value) throws IOException {
        String safePlayer = sanitizeName(playerName);
        String safeValue = requireValue(value);
        Path playerDir = playersDir.resolve(safePlayer);
        Files.createDirectories(playerDir);

        int nextId = 1;
        try (Stream<Path> files = Files.list(playerDir)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                nextId = Math.max(nextId, extractNumericPrefix(file) + 1);
            }
        }

        Files.writeString(playerDir.resolve(nextId + ".txt"), safeValue, StandardCharsets.UTF_8);
        reload();
        return nextId;
    }

    public synchronized void addGlobalMemory(String name, String value) throws IOException {
        String safeName = sanitizeName(name);
        String safeValue = requireValue(value);
        Files.writeString(globalDir.resolve(safeName + ".txt"), safeValue, StandardCharsets.UTF_8);
        reload();
    }

    public synchronized void deletePlayerMemory(String playerName, int id) throws IOException {
        String safePlayer = sanitizeName(playerName);
        if (id <= 0) {
            throw new IllegalArgumentException("id must be >= 1");
        }

        Path target = playersDir.resolve(safePlayer).resolve(id + ".txt");
        if (!Files.exists(target)) {
            throw new IOException("memory file not found: " + id + ".txt");
        }

        Files.delete(target);
        reload();
    }

    public synchronized void deleteGlobalMemory(String name) throws IOException {
        String safeName = sanitizeName(name);
        Path target = globalDir.resolve(safeName + ".txt");
        if (!Files.exists(target)) {
            throw new IOException("global memory file not found: " + safeName + ".txt");
        }

        Files.delete(target);
        reload();
    }

    public synchronized List<String> getGlobalMemories() {
        return new ArrayList<>(globalMemories.values());
    }

    public synchronized List<String> getPlayerMemoriesForParticipants(Collection<String> participantNames) {
        List<String> result = new ArrayList<>();
        for (String name : participantNames) {
            String safeName = sanitizeName(name);
            List<String> memories = playerMemories.get(safeName);
            if (memories != null) {
                for (String memory : memories) {
                    result.add(safeName + ": " + memory);
                }
            }
        }
        return result;
    }

    public synchronized Path getBaseDir() {
        return baseDir;
    }

    public String sanitizeName(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("name is null");
        }

        String s = raw.trim();
        s = WINDOWS_ILLEGAL.matcher(s).replaceAll("_");
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("^[. ]+", "");
        s = s.replaceAll("[. ]+$", "");

        if (s.isBlank() || s.equals(".") || s.equals("..")) {
            throw new IllegalArgumentException("name is invalid");
        }

        return s;
    }

    private String requireValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value is empty");
        }
        return value.trim();
    }

    private int extractNumericPrefix(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".txt")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }

        try {
            return Integer.parseInt(fileName);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
