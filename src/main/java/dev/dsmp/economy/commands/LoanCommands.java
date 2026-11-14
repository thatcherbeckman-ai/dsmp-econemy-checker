package dev.dsmp.economy.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public final class LoanCommands {
    private LoanCommands() {}
    public static <S> void attach(LiteralArgumentBuilder<S> root, LoanAction action) {
        var loan = LiteralArgumentBuilder.<S>literal("loan").executes(c -> action.loanCommand("list", null, null));
        for (String operation : new String[]{"set", "add", "paid"}) {
            loan.then(LiteralArgumentBuilder.<S>literal(operation)
                    .then(RequiredArgumentBuilder.<S, String>argument("player", StringArgumentType.word())
                            .then(RequiredArgumentBuilder.<S, String>argument("amount", StringArgumentType.word())
                                    .executes(c -> action.loanCommand(operation, StringArgumentType.getString(c, "player"), StringArgumentType.getString(c, "amount"))))));
        }
        loan.then(LiteralArgumentBuilder.<S>literal("remove")
                .then(RequiredArgumentBuilder.<S, String>argument("player", StringArgumentType.word())
                        .executes(c -> action.loanCommand("remove", StringArgumentType.getString(c, "player"), null))));
        loan.then(LiteralArgumentBuilder.<S>literal("list").executes(c -> action.loanCommand("list", null, null)));
        loan.then(LiteralArgumentBuilder.<S>literal("page")
                .then(RequiredArgumentBuilder.<S, Integer>argument("number", IntegerArgumentType.integer(1))
                        .executes(c -> action.loanCommand("page", null, Integer.toString(IntegerArgumentType.getInteger(c, "number"))))));
        var noLoan = LiteralArgumentBuilder.<S>literal("noloan").executes(c -> action.loanCommand("noloan_list", null, null));
        for (String operation : new String[]{"add", "remove"}) {
            noLoan.then(LiteralArgumentBuilder.<S>literal(operation)
                    .then(RequiredArgumentBuilder.<S, String>argument("player", StringArgumentType.word())
                            .executes(c -> action.loanCommand("noloan_" + operation, StringArgumentType.getString(c, "player"), null))));
        }
        noLoan.then(LiteralArgumentBuilder.<S>literal("list").executes(c -> action.loanCommand("noloan_list", null, null))
                .then(RequiredArgumentBuilder.<S, Integer>argument("number", IntegerArgumentType.integer(1))
                        .executes(c -> action.loanCommand("noloan_list", null, Integer.toString(IntegerArgumentType.getInteger(c, "number"))))));
        loan.then(noLoan);
        root.then(loan);
    }
}
