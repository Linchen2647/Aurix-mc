package com.example.aurix;

import com.example.aurix.ai.NvidiaChatClient;
import com.example.aurix.brain.ConversationMemory;
import com.example.aurix.brain.ReplyPlanner;
import com.example.aurix.command.AurixCommand;
import com.example.aurix.config.AurixConfig;
import com.example.aurix.memory.MemoryFileStore;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AurixClientMod implements ClientModInitializer {
    private static AurixConfig config;
    private static final ConversationMemory MEMORY = new ConversationMemory();
    private static final ReplyPlanner PLANNER = new ReplyPlanner();
    private static final NvidiaChatClient AI = new NvidiaChatClient();
    private static final MemoryFileStore MEMORY_STORE = new MemoryFileStore();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static final AtomicBoolean SENDING_AURIX_MESSAGE = new AtomicBoolean(false);

    private boolean loadedMessageSent = false;

    @Override
    public void onInitializeClient() {
        config = AurixConfig.load();

        try {
            MEMORY_STORE.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClientReceiveMessageEvents.CHAT.register(this::onReceiveChat);
        ClientSendMessageEvents.CHAT.register(this::onSendChat);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                AurixCommand.register(dispatcher, config, MEMORY_STORE, this::reloadMemories));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !loadedMessageSent) {
                loadedMessageSent = true;
                sendSystemMessage("[ Aurix ] load complete!");
            }
        });
    }

    private void onReceiveChat(Text message,
                               SignedMessage signedMessage,
                               GameProfile sender,
                               MessageType.Parameters params,
                               Instant receptionTimestamp) {
        if (!config.enabled) return;

        String senderName = params != null && params.name() != null
                ? params.name().getString()
                : "Unknown";
        String content = message.getString();

        if (content == null || content.isBlank()) return;

        if (config.ignoreOwnMessages) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && senderName.equalsIgnoreCase(client.player.getName().getString())) {
                return;
            }
        }

        MEMORY.add(senderName, content, false, config.maxContextMessages);
        maybeReplyAsync();
    }

    private void onSendChat(String message) {
        if (!config.enabled) return;
        if (SENDING_AURIX_MESSAGE.get()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        String selfName = client.player != null ? client.player.getName().getString() : "Me";

        if (message != null && !message.isBlank()) {
            MEMORY.add(selfName, message, true, config.maxContextMessages);
            maybeReplyAsync();
        }
    }

    private void maybeReplyAsync() {
        if (BUSY.get()) return;

        List<ConversationMemory.ChatLine> recent = MEMORY.lastN(config.triggerWindowMessages);
        if (!PLANNER.shouldReply(config, recent)) {
            return;
        }

        if (!BUSY.compareAndSet(false, true)) {
            return;
        }

        EXECUTOR.submit(() -> {
            try {
                List<ConversationMemory.ChatLine> context = MEMORY.lastN(config.maxContextMessages);
                LinkedHashSet<String> participants = new LinkedHashSet<>();
                for (ConversationMemory.ChatLine line : context) {
                    if (!line.self()) {
                        participants.add(line.sender());
                    }
                }

                String reply = AI.generateReply(
                        config,
                        context,
                        MEMORY_STORE.getPlayerMemoriesForParticipants(participants),
                        MEMORY_STORE.getGlobalMemories()
                );

                if (reply == null || reply.isBlank()) {
                    return;
                }

                String finalMessage = config.chatPrefix + reply;

                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> {
                    if (client.player == null || client.getNetworkHandler() == null) {
                        return;
                    }

                    String msg = finalMessage.length() > 180 ? finalMessage.substring(0, 180) : finalMessage;
                    SENDING_AURIX_MESSAGE.set(true);
                    try {
                        client.getNetworkHandler().sendChatMessage(msg);
                    } finally {
                        SENDING_AURIX_MESSAGE.set(false);
                    }

                    MEMORY.add(config.botName, reply, true, config.maxContextMessages);
                    PLANNER.markReplied();
                });

            } catch (Exception e) {
                sendSystemMessage("[ Aurix ERROR ] " + e.getMessage());
            } finally {
                BUSY.set(false);
            }
        });
    }

    private void reloadMemories() {
        try {
            MEMORY_STORE.reload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSystemMessage(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.execute(() -> client.player.sendMessage(Text.of(msg), false));
        }
    }
}
