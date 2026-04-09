package com.example.aurix.ai;

import com.example.aurix.brain.ConversationMemory;
import com.example.aurix.config.AurixConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class NvidiaChatClient {
    private static final Gson GSON = new Gson();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String generateReply(AurixConfig config,
                                List<ConversationMemory.ChatLine> lines,
                                List<String> playerMemories,
                                List<String> globalMemories) throws IOException, InterruptedException {
        if (config.apiKey == null || config.apiKey.isBlank()) {
            throw new IOException("apiKey is empty");
        }

        JsonArray messages = new JsonArray();

        String systemPrompt = String.format(
                "你是 Aurix\n" +
                "你現在在 Minecraft 聊天裡\n" +
                "你的說話風格要自然 短句 低存在感\n" +
                "不要每句都回\n" +
                "只有在適合插話時才說話\n" +
                "回覆要像玩家\n" +
                "盡量不要太多標點符號\n" +
                "不要長篇大論\n" +
                "一次最多一兩句\n" +
                "不要用表情符號\n" +
                "不要裝成全知\n" +
                "看不懂就簡短帶過\n" +
                "若別人叫你 Aurix 可以正常回應\n" +
                "你的輸出不要自己加前綴\n" +
                "你的輸出內容請控制在 %d 字以內\n",
                config.maxReplyChars
        );

        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", systemPrompt);
        messages.add(system);

        if (!globalMemories.isEmpty()) {
            JsonObject memoryMessage = new JsonObject();
            memoryMessage.addProperty("role", "system");
            memoryMessage.addProperty("content", "以下是全域記憶 請當作低調參考 不要逐條背誦\n- " + String.join("\n- ", globalMemories));
            messages.add(memoryMessage);
        }

        if (!playerMemories.isEmpty()) {
            JsonObject memoryMessage = new JsonObject();
            memoryMessage.addProperty("role", "system");
            memoryMessage.addProperty("content", "以下是玩家相關記憶 請只在適合時默默參考\n- " + String.join("\n- ", playerMemories));
            messages.add(memoryMessage);
        }

        for (ConversationMemory.ChatLine line : lines) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", "user");
            msg.addProperty("content", line.sender() + ": " + line.content());
            messages.add(msg);
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", config.model);
        body.add("messages", messages);
        body.addProperty("temperature", 0.7);
        body.addProperty("top_p", 0.9);
        body.addProperty("max_tokens", 80);

        JsonObject chatTemplateKwargs = new JsonObject();
        chatTemplateKwargs.addProperty("enable_thinking", false);
        body.add("chat_template_kwargs", chatTemplateKwargs);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.apiBaseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(40))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("NVIDIA API HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray choices = json.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        JsonObject first = choices.get(0).getAsJsonObject();
        JsonObject message = first.getAsJsonObject("message");
        if (message == null) {
            return null;
        }

        String content = null;
        if (message.has("content") && !message.get("content").isJsonNull()) {
            content = message.get("content").getAsString();
        }
        if ((content == null || content.isBlank()) && message.has("reasoning_content") && !message.get("reasoning_content").isJsonNull()) {
            content = message.get("reasoning_content").getAsString();
        }
        if (content == null || content.isBlank()) {
            return null;
        }

        return sanitize(content, config.maxReplyChars);
    }

    private String sanitize(String text, int maxChars) {
        if (text == null) return null;

        String s = text
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();

        s = s.replaceFirst("^\\[\\s*Aurix\\s*]\\s*", "");
        s = s.replaceFirst("^Aurix\\s*[:：]\\s*", "");

        if (s.length() > maxChars) {
            s = s.substring(0, maxChars).trim();
        }

        s = s.replaceAll("^[\\p{Punct}\\s]+", "").trim();
        if (s.isEmpty()) return null;

        return s;
    }
}
