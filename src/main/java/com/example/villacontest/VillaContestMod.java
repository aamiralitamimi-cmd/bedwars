package com.example.villacontest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VillaContestMod implements ModInitializer {
    public static final String MOD_ID = "villa_contest";
    private static final int ROUND_MINUTES = 25;
    private static final int ROUNDS = 4;

    private static final List<String> MISSIONS = List.of(
            "خانه مدرن", "ویلای ساحلی", "خانه جنگلی", "خانه کوهستانی",
            "خانه آینده‌نگر", "خانه کوچک و دنج", "ویلای لوکس", "خانه با استخر"
    );

    private static boolean running = false;
    private static int currentRound = 0;
    private static String currentMission = "-";
    private static long roundEndTick = 0;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("villa")
                        .then(CommandManager.literal("start").executes(ctx -> start(ctx.getSource().getServer())))
                        .then(CommandManager.literal("stop").executes(ctx -> stop(ctx.getSource().getServer())))
                        .then(CommandManager.literal("status").executes(ctx -> status(ctx.getSource().getServer())))
                        .then(CommandManager.literal("mission").executes(ctx -> mission(ctx.getSource().getServer())))
                        .then(CommandManager.literal("reset").executes(ctx -> reset(ctx.getSource().getServer())))
                        .then(CommandManager.literal("round")
                                .then(CommandManager.literal("1").executes(ctx -> forceRound(ctx.getSource().getServer(), 1)))
                                .then(CommandManager.literal("2").executes(ctx -> forceRound(ctx.getSource().getServer(), 2)))
                                .then(CommandManager.literal("3").executes(ctx -> forceRound(ctx.getSource().getServer(), 3)))
                                .then(CommandManager.literal("4").executes(ctx -> forceRound(ctx.getSource().getServer(), 4)))
                        )));
    }

    private static int start(MinecraftServer server) {
        running = true;
        currentRound = 1;
        chooseMission();
        roundEndTick = server.getTicks() + ROUND_MINUTES * 60L * 20L;
        applyRules(server);
        broadcast(server, "§6§lVILLA CONTEST §fشروع شد! §eRound 1/4§f - مأموریت: §b" + currentMission);
        return 1;
    }

    private static int stop(MinecraftServer server) {
        running = false;
        broadcast(server, "§c§lVILLA CONTEST §fمتوقف شد.");
        return 1;
    }

    private static int status(MinecraftServer server) {
        String state = running ? "§aدر حال اجرا" : "§cمتوقف";
        server.getCommandSource().sendFeedback(() -> Text.literal("§6Villa Contest: " + state + " §f| Round: " + currentRound + "/" + ROUNDS + " §f| Mission: " + currentMission), false);
        return 1;
    }

    private static int mission(MinecraftServer server) {
        chooseMission();
        broadcast(server, "§eمأموریت جدید: §b" + currentMission);
        return 1;
    }

    private static int reset(MinecraftServer server) {
        running = false;
        currentRound = 0;
        currentMission = "-";
        roundEndTick = 0;
        broadcast(server, "§7Villa Contest ریست شد.");
        return 1;
    }

    private static int forceRound(MinecraftServer server, int round) {
        running = true;
        currentRound = round;
        chooseMission();
        roundEndTick = server.getTicks() + ROUND_MINUTES * 60L * 20L;
        applyRules(server);
        broadcast(server, "§6Round " + round + "/4 §fشروع شد — مأموریت: §b" + currentMission);
        return 1;
    }

    private static void chooseMission() {
        currentMission = MISSIONS.get(ThreadLocalRandom.current().nextInt(MISSIONS.size()));
    }

    private static void applyRules(MinecraftServer server) {
        server.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerManager().broadcast(Text.literal(message), false);
    }
}
