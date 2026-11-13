package dev.dsmp.economy.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public final class RakebackCommands {
    private RakebackCommands() {}
    public static <S> void attach(LiteralArgumentBuilder<S> root, RakebackResetAction action) {
        root.then(LiteralArgumentBuilder.<S>literal("rakeback")
                .then(LiteralArgumentBuilder.<S>literal("resetall")
                        .executes(context -> action.resetAllRakeback())));
    }
    public static String resultMessage(boolean saved, String status) {
        return saved ? "All rakeback reset to zero; balances, Profit and Player Nets retained." : status;
    }
}
