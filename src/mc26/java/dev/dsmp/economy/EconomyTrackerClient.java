package dev.dsmp.economy;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.dsmp.economy.LocalStore;
import dev.dsmp.economy.SidebarBalance;
import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.appearance.RenderTheme;
import dev.dsmp.economy.auction.AuctionService;
import dev.dsmp.economy.commands.AuctionAction;
import dev.dsmp.economy.commands.LoanAction;
import dev.dsmp.economy.commands.LoanCommands;
import dev.dsmp.economy.commands.RakebackCommands;
import dev.dsmp.economy.commands.RakebackResetAction;
import dev.dsmp.economy.core.BalanceReader;
import dev.dsmp.economy.core.BalanceReading;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.core.MessageParser;
import dev.dsmp.economy.core.Money;
import dev.dsmp.economy.core.PaymentDetector;
import dev.dsmp.economy.core.RakebackReset;
import dev.dsmp.economy.core.ServerMatcher;
import dev.dsmp.economy.core.ViewCycle;
import dev.dsmp.economy.core.WalletTracker;
import dev.dsmp.economy.loans.LoanService;
import dev.dsmp.economy.ui.AuctionHologram;
import dev.dsmp.economy.ui.DashboardScreen;
import dev.dsmp.economy.ui.TrackerRenderer;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EconomyTrackerClient
implements ClientModInitializer,
RakebackResetAction,
LoanAction,
AuctionAction {
    private static final Logger LOG = LoggerFactory.getLogger((String)"EconomyTracker");
    public final LocalStore store = new LocalStore(FabricLoader.getInstance().getConfigDir().resolve("economy-tracker"));
    public TrackerConfig config = new TrackerConfig();
    public Ledger ledger = new Ledger();
    public final ViewCycle viewCycle = new ViewCycle();
    private final PaymentDetector payments = new PaymentDetector();
    public final WalletTracker wallet = new WalletTracker();
    private MessageParser parser = new MessageParser("", "");
    private String scope;
    private boolean configWritable = true;
    private boolean dataWritable = true;
    private int sampleTicks;
    public String status = "Configure server message patterns, or record manually";

    public void onInitializeClient() {
        this.reloadConfig();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> this.openLedger(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            dev.dsmp.economy.ui.AuctionItems.invalidate();
            this.saveLedger();
            this.scope = null;
            AuctionService.close();
            AuctionHologram.clear();
            LoanService.close();
            this.ledger = new Ledger();
            this.payments.clear();
            this.wallet.clear();
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (this.scope != null && this.dataWritable) {
                boolean changed;
                long now = System.currentTimeMillis();
                String localName = Minecraft.getInstance().getUser().getName();
                boolean bl = changed = this.config.scoreboardBalance && this.wallet.receiveReply(message.getString(), localName, this.ledger, now);
                if (!overlay) {
                    PaymentDetector.Detection payment = this.payments.receiveDetailed(message.getString(), this.ledger, now, (long)this.config.paymentSettlementSeconds * 1000L, localName, this.config.autoDetectPayments);
                    changed |= payment.betChanged();
                    if (this.config.scoreboardBalance && payment.walletDelta() != null) {
                        changed |= this.wallet.payment(payment.walletDelta(), this.ledger, now);
                    }
                    if (!payment.suppressesCustom() && this.config.trackingEnabled) {
                        this.accept(message.getString());
                    }
                }
                if (changed) {
                    this.saveLedger();
                }
            }
        });
        ClientSendMessageEvents.COMMAND.register(command -> {
            if (this.scope != null && this.dataWritable) {
                long now = System.currentTimeMillis();
                this.payments.observeCommand(command, now);
                this.wallet.observeCommand(command, Minecraft.getInstance().getUser().getName(), now);
            }
        });
        HudElementRegistry.attachElementBefore((Identifier)VanillaHudElements.CHAT, (Identifier)Identifier.fromNamespaceAndPath((String)"economy_tracker", (String)"trackers"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && this.scope != null && !client.gui.hud.isHidden() && !RenderTheme.isEditor(client.gui.screen())) {
                TrackerRenderer.drawAll(graphics, this.config, this.ledger, this.viewCycle, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), false, false, this.wallet.estimated());
            }
        });
        KeyMapping key = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.economy_tracker.edit", InputConstants.Type.KEYBOARD, InputConstants.KEY_O, KeyMapping.Category.register((Identifier)Identifier.fromNamespaceAndPath((String)"economy_tracker", (String)"controls"))));
        KeyMapping nextView = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.economy_tracker.next_view", InputConstants.Type.KEYBOARD, InputConstants.KEY_P, key.getCategory()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AuctionService.tick(System.currentTimeMillis());
            AuctionHologram.tick(Minecraft.getInstance());
            while (key.consumeClick()) {
                if (client.gui.screen() != null) continue;
                client.gui.setScreen((Screen)new DashboardScreen(this));
            }
            while (nextView.consumeClick()) {
                if (client.gui.screen() != null || client.level == null) continue;
                this.viewCycle.next(this.config.enabledViews(), System.nanoTime() / 1000000L);
            }
            if (++this.sampleTicks >= 20) {
                this.sampleTicks = 0;
                if (this.scope != null && this.dataWritable && this.config.scoreboardBalance) {
                    long now = System.currentTimeMillis();
                    Optional<BalanceReading> balance = SidebarBalance.reading((Minecraft)client);
                    if (balance.isPresent() && this.wallet.sidebar(balance.get(), this.ledger, now)) {
                        this.saveLedger();
                    }
                }
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> root = ClientCommands.literal((String)"economy").executes(context -> {
                Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen((Screen)new DashboardScreen(this)));
                return 1;
            });
            root.then(ClientCommands.literal((String)"reload").executes(context -> {
                this.reloadConfig();
                this.openLedger(Minecraft.getInstance());
                this.say(this.status);
                return 1;
            }));
            root.then((((ClientCommands.literal((String)"auto").executes(context -> {
                this.say("Automatic bets: " + (this.config.autoDetectPayments ? "ON" : "OFF") + "; balance reader: " + (this.config.scoreboardBalance ? "ON" : "OFF") + "; no-payout wait: " + this.config.paymentSettlementSeconds + " seconds.");
                return 1;
            })).then(ClientCommands.literal((String)"on").executes(context -> {
                this.config.autoDetectPayments = true;
                this.config.scoreboardBalance = true;
                this.say(this.saveConfig() ? "Automatic stakes/payouts and scoreboard balance enabled." : this.status);
                return 1;
            }))).then(ClientCommands.literal((String)"off").executes(context -> {
                this.config.autoDetectPayments = false;
                this.payments.clear();
                this.say(this.saveConfig() ? "Automatic bet capture paused; balance setting unchanged." : this.status);
                return 1;
            }))).then(ClientCommands.literal((String)"wait").then(ClientCommands.argument((String)"seconds", IntegerArgumentType.integer((int)5, (int)600)).executes(context -> {
                this.config.paymentSettlementSeconds = IntegerArgumentType.getInteger((CommandContext)context, (String)"seconds");
                this.say((String)(this.saveConfig() ? "Unpaid stakes settle after " + this.config.paymentSettlementSeconds + " seconds." : this.status));
                return 1;
            }))));
            root.then(ClientCommands.literal((String)"bet").then(ClientCommands.argument((String)"player", StringArgumentType.word()).then(ClientCommands.argument((String)"playerNet", StringArgumentType.word()).executes(context -> this.runLocal(() -> {
                long delta = Money.parse(StringArgumentType.getString((CommandContext)context, (String)"playerNet"));
                this.ledger.recordBet(new Ledger.Bet(UUID.randomUUID().toString(), StringArgumentType.getString((CommandContext)context, (String)"player"), delta, System.currentTimeMillis()));
                this.say(this.saveLedger() ? "Manual bet recorded locally (positive = player ahead)." : this.status);
            })))));
            root.then(ClientCommands.literal((String)"profit").then(ClientCommands.literal((String)"reset").executes(context -> this.runLocal(() -> {
                this.ledger.resetProfit();
                this.say(this.saveLedger() ? "Profit and displayed In/Out reset to zero. Balance, player totals, graph, bets and rakeback retained." : this.status);
            }))));
            root.then(ClientCommands.literal((String)"graph").then(ClientCommands.literal((String)"reset").executes(context -> this.runLocal(() -> {
                this.ledger.resetGraph();
                this.say(this.saveLedger() ? "Graph cleared. New graph activity starts at zero; overall Profit is retained." : this.status);
            }))));
            root.then(ClientCommands.literal((String)"player").then(ClientCommands.literal((String)"reset").then(ClientCommands.argument((String)"player", StringArgumentType.word()).executes(context -> this.runLocal(() -> {
                String player = StringArgumentType.getString((CommandContext)context, (String)"player");
                if (!this.ledger.resetPlayerNet(player)) {
                    this.say("No tracked total for " + player);
                    return;
                }
                this.say((String)(this.saveLedger() ? "Displayed win/loss total reset for " + player + "; balance, overall Profit, bets and rakeback are retained." : this.status));
            })))));
            root.then((((ClientCommands.literal((String)"balance").then(ClientCommands.literal((String)"sync").executes(context -> this.syncBalance()))).then(ClientCommands.literal((String)"debug").executes(context -> {
                this.say("Balance reading: " + (this.wallet.estimated() ? "estimated or unknown" : "exact") + "; active server: " + (this.scope != null));
                this.say("Last balance reply: " + this.wallet.lastReply());
                for (BalanceReader.Row row : SidebarBalance.rows(Minecraft.getInstance())) {
                    this.say("Sidebar: " + BalanceReader.readable(row.left()) + " | " + BalanceReader.readable(row.right()));
                }
                return 1;
            }))).then(ClientCommands.literal((String)"reset").executes(context -> this.runLocal(() -> {
                if (!this.ledger.resetBalanceBaseline()) {
                    this.say("No known balance to use as a starting point.");
                    return;
                }
                this.say(this.saveLedger() ? "Balance change reset to zero; current balance and history retained." : this.status);
            })))).then(ClientCommands.argument((String)"amount", StringArgumentType.word()).executes(context -> this.runLocal(() -> {
                this.wallet.manualBalance(Money.parse(StringArgumentType.getString((CommandContext)context, (String)"amount")), this.ledger, System.currentTimeMillis());
                this.say(this.saveLedger() ? "Balance recorded locally." : this.status);
            }))));
            root.then((((ClientCommands.literal((String)"rakeback").executes(context -> {
                this.say("Commands: /economy rakeback reset NAME | resetall | set NAME AMOUNT | show NAME. Goal: $1B net losses since reset.");
                return 1;
            })).then(ClientCommands.literal((String)"reset").then(ClientCommands.argument((String)"player", StringArgumentType.word()).executes(context -> this.runLocal(() -> {
                String player = StringArgumentType.getString((CommandContext)context, (String)"player");
                if (!this.ledger.resetRakeback(player)) {
                    this.say("No rakeback record for " + player);
                    return;
                }
                this.say((String)(this.saveLedger() ? "Rakeback reset for " + player + "; Player Nets and Profit retained." : this.status));
            }))))).then(ClientCommands.literal((String)"set").then(ClientCommands.argument((String)"player", StringArgumentType.word()).then(ClientCommands.argument((String)"loss", StringArgumentType.word()).executes(context -> this.runLocal(() -> {
                String player = StringArgumentType.getString((CommandContext)context, (String)"player");
                long loss = Money.parse(StringArgumentType.getString((CommandContext)context, (String)"loss"));
                this.ledger.setRakebackLoss(player, loss);
                this.say((String)(this.saveLedger() ? "Rakeback progress for " + player + ": " + Money.full(loss, false) + " / $1B" : this.status));
            })))))).then(ClientCommands.literal((String)"show").then(ClientCommands.argument((String)"player", StringArgumentType.word()).executes(context -> this.runLocal(() -> {
                Optional<Ledger.PlayerLoss> progress = this.ledger.rakeback(StringArgumentType.getString((CommandContext)context, (String)"player"));
                this.say(progress.map(p -> p.player() + ": " + Money.full(p.cents(), false) + " / $1B" + (p.eligible() ? " (goal reached)" : "")).orElse("No rakeback record for that player"));
            })))));
            LiteralArgumentBuilder literalArgumentBuilder = root;
            RakebackCommands.attach(literalArgumentBuilder, this);
            LoanCommands.attach(literalArgumentBuilder, this);
            dispatcher.register(literalArgumentBuilder);
        });
    }

    private int runLocal(Runnable command) {
        try {
            this.requireScope();
            command.run();
            return 1;
        }
        catch (ArithmeticException | IllegalArgumentException | IllegalStateException ex) {
            this.say("Command could not be completed: " + ex.getMessage());
            return 0;
        }
    }

    public int syncBalance() {
        return this.runLocal(() -> {
            this.config.scoreboardBalance = true;
            if (!this.saveConfig()) {
                this.say(this.status);
                return;
            }
            long now = System.currentTimeMillis();
            Optional<BalanceReading> balance = SidebarBalance.reading(Minecraft.getInstance());
            if (balance.isPresent() && this.wallet.sidebar(balance.get(), this.ledger, now)) {
                this.saveLedger();
            }
            this.say("Balance reader enabled. Reading sidebar and payment messages; no server commands sent. Type /bal yourself for an exact balance.");
        });
    }

    public void reloadConfig() {
        try {
            TrackerConfig loaded = this.store.loadConfig();
            this.parser = new MessageParser(loaded.betPattern, loaded.balancePattern);
            this.config = loaded;
            this.configWritable = true;
            this.payments.clear();
            this.status = this.config.autoDetectPayments ? "Automatic payment-based bet capture enabled; wait " + this.config.paymentSettlementSeconds + "s" : (this.config.trackingEnabled ? "Custom message tracking enabled" : "Automatic bet capture paused");
        }
        catch (Exception ex) {
            this.configWritable = false;
            this.config.trackingEnabled = false;
            this.config.autoDetectPayments = false;
            this.status = "Settings unreadable; original file preserved. Fix settings.json and reload.";
            LOG.error("Cannot load tracker settings; refusing to overwrite file", (Throwable)ex);
        }
    }

    public boolean saveConfig() {
        if (!this.configWritable) {
            return false;
        }
        try {
            this.store.saveConfig(this.config);
            return true;
        }
        catch (Exception ex) {
            this.status = "Could not save tracker settings";
            LOG.error(this.status, (Throwable)ex);
            return false;
        }
    }

    private void openLedger(Minecraft client) {
        dev.dsmp.economy.ui.AuctionItems.invalidate();
        if (this.scope != null) {
            this.saveLedger();
        }
        this.scope = null;
        AuctionService.close();
        AuctionHologram.clear();
        LoanService.close();
        this.payments.clear();
        this.wallet.clear();
        this.ledger = new Ledger();
        ServerData server = client.getCurrentServer();
        if (server == null || !ServerMatcher.matches(this.config.serverAddress, server.ip)) {
            LoanService.open(this.scope, this.store.loanDirectory());
            AuctionService.open(this.scope, this.store.auctionDirectory());
            return;
        }
        this.scope = server.ip.toLowerCase(Locale.ROOT) + "|" + String.valueOf(client.getUser().getProfileId());
        AuctionService.close();
        AuctionHologram.clear();
        LoanService.close();
        try {
            this.ledger = this.store.loadLedger(this.scope);
            this.dataWritable = true;
        }
        catch (Exception ex) {
            this.dataWritable = false;
            this.status = "Ledger unreadable; tracking paused to preserve the file";
            LOG.error(this.status, (Throwable)ex);
        }
        LoanService.open(this.scope, this.store.loanDirectory());
        AuctionService.open(this.scope, this.store.auctionDirectory());
    }

    private void requireScope() {
        if (this.scope == null) {
            throw new IllegalStateException("Join the server configured in settings.json first");
        }
        if (!this.dataWritable) {
            throw new IllegalStateException("Fix the existing ledger file before recording");
        }
    }

    private boolean saveLedger() {
        if (this.scope == null || !this.dataWritable) {
            return false;
        }
        try {
            this.store.saveLedger(this.scope, this.ledger);
            return true;
        }
        catch (Exception ex) {
            this.status = "Ledger save failed; current data remains in memory";
            LOG.error(this.status, (Throwable)ex);
            return false;
        }
    }

    private void accept(String line) {
        try {
            Optional<Long> balance;
            boolean changed = false;
            Optional<MessageParser.ParsedBet> bet = this.parser.bet(line);
            if (bet.isPresent()) {
                MessageParser.ParsedBet result = bet.get();
                changed = this.ledger.recordBet(new Ledger.Bet(result.id(), result.player(), result.playerDelta(), System.currentTimeMillis()));
            }
            if ((balance = this.parser.balance(line)).isPresent()) {
                this.ledger.recordBalance(balance.get(), System.currentTimeMillis());
                changed = true;
            }
            if (changed) {
                this.saveLedger();
            }
        }
        catch (ArithmeticException | IllegalArgumentException ex) {
            this.status = "Ignored a result with invalid or out-of-range amounts";
        }
    }

    private void say(String text) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.literal("[thatchacats econemy checker] "+text));
        }
    }

    @Override
    public int resetAllRakeback() {
        return this.runLocal(() -> {
            RakebackReset.all(this.ledger);
            this.say(RakebackCommands.resultMessage(this.saveLedger(), this.status));
        });
    }

    @Override
    public int loanCommand(String string, String string2, String string3) {
        return this.runLocal(() -> this.say(LoanService.execute(string, string2, string3)));
    }

    @Override
    public int auctionAction(Runnable runnable) {
        return this.runLocal(runnable);
    }
}
