package com.example.aurix.command;

import com.example.aurix.config.AurixConfig;
import com.example.aurix.memory.MemoryFileStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public final class AurixCommand {
    private AurixCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                AurixConfig config,
                                MemoryFileStore memoryStore,
                                Runnable reloadAction) {
        dispatcher.register(ClientCommandManager.literal("aurix")
                .then(ClientCommandManager.literal("agent")
                        .then(ClientCommandManager.literal("on")
                                .executes(ctx -> {
                                    config.enabled = true;
                                    config.save();
                                    send(ctx.getSource(), "agent 已開啟");
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("off")
                                .executes(ctx -> {
                                    config.enabled = false;
                                    config.save();
                                    send(ctx.getSource(), "agent 已關閉");
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("reload")
                                .executes(ctx -> {
                                    reloadAction.run();
                                    send(ctx.getSource(), "memory 與 global memory 已重新載入");
                                    return 1;
                                })))
                .then(ClientCommandManager.literal("memory")
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String player = StringArgumentType.getString(ctx, "player");
                                                    String value = StringArgumentType.getString(ctx, "value");
                                                    try {
                                                        int id = memoryStore.addPlayerMemory(player, value);
                                                        reloadAction.run();
                                                        send(ctx.getSource(), "已新增玩家記憶：" + player + " / " + id + ".txt");
                                                        return 1;
                                                    } catch (Exception e) {
                                                        send(ctx.getSource(), "新增玩家記憶失敗：" + e.getMessage());
                                                        return 0;
                                                    }
                                                }))))
                        .then(ClientCommandManager.literal("add_global")
                                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String value = StringArgumentType.getString(ctx, "value");
                                                    try {
                                                        memoryStore.addGlobalMemory(name, value);
                                                        reloadAction.run();
                                                        send(ctx.getSource(), "已新增全域記憶：" + name + ".txt");
                                                        return 1;
                                                    } catch (Exception e) {
                                                        send(ctx.getSource(), "新增全域記憶失敗：" + e.getMessage());
                                                        return 0;
                                                    }
                                                }))))
                        .then(ClientCommandManager.literal("delete")
                                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("id", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    String player = StringArgumentType.getString(ctx, "player");
                                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                                    try {
                                                        memoryStore.deletePlayerMemory(player, id);
                                                        reloadAction.run();
                                                        send(ctx.getSource(), "已刪除玩家記憶：" + player + " / " + id + ".txt");
                                                        return 1;
                                                    } catch (Exception e) {
                                                        send(ctx.getSource(), "刪除玩家記憶失敗：" + e.getMessage());
                                                        return 0;
                                                    }
                                                }))))
                        .then(ClientCommandManager.literal("delete_global")
                                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            try {
                                                memoryStore.deleteGlobalMemory(name);
                                                reloadAction.run();
                                                send(ctx.getSource(), "已刪除全域記憶：" + name + ".txt");
                                                return 1;
                                            } catch (Exception e) {
                                                send(ctx.getSource(), "刪除全域記憶失敗：" + e.getMessage());
                                                return 0;
                                            }
                                        })))))
        ;
    }

    private static void send(FabricClientCommandSource source, String message) {
        source.sendFeedback(Text.literal("[ Aurix ] " + message));
    }
}
