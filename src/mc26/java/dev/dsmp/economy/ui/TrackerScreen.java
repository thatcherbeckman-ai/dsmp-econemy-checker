package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.loans.LoanService;
import dev.dsmp.economy.loans.LoanUi;
import dev.dsmp.economy.ui.AppearanceScreen;
import dev.dsmp.economy.ui.DashboardNavigation;
import dev.dsmp.economy.ui.FestiveUi;
import dev.dsmp.economy.ui.TextBoxesScreen;
import dev.dsmp.economy.ui.TrackerRenderer;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class TrackerScreen
extends Screen {
    private final EconomyTrackerClient mod;
    private final Ledger sample = Ledger.demo();
    private boolean preview = true;
    private int tab;
    private int dragging = -1;
    private double offsetX;
    private double offsetY;
    private String notice = "Drag a box to move it. Esc saves.";

    public TrackerScreen(EconomyTrackerClient mod) {
        super((Component)Component.literal((String)"Economy Tracker Editor"));
        this.mod = mod;
    }

    protected void init() {
        this.button(10, 28, 50, "Layout", () -> {
            this.tab = 0;
            this.rebuildWidgets();
        });
        this.button(64, 28, 56, "Trackers", () -> {
            this.tab = 1;
            this.rebuildWidgets();
        });
        this.button(124, 28, 56, "Widgets", () -> {
            this.tab = 2;
            this.rebuildWidgets();
        });
        this.button(this.width - 66, 28, 56, "Done", this::onClose);
        if (this.tab == 0) {
            this.button(10, 54, 64, "Scale -", () -> {
                this.mod.config.scale = Math.max(0.4f, this.mod.config.scale - 0.1f);
            });
            this.button(78, 54, 64, "Scale +", () -> {
                this.mod.config.scale = Math.min(1.5f, this.mod.config.scale + 0.1f);
            });
            this.button(146, 54, 90, "Reset layout", () -> {
                TrackerConfig defaults = new TrackerConfig();
                for (int box = 0; box < 3; ++box) {
                    this.mod.config.box((int)box).x = defaults.box((int)box).x;
                    this.mod.config.box((int)box).y = defaults.box((int)box).y;
                }
                this.mod.config.scale = defaults.scale;
            });
            this.button(240, 54, 64, "Glass", () -> {
                this.mod.config.opacity = this.mod.config.opacity >= 230 ? 80 : this.mod.config.opacity + 30;
            });
        } else if (this.tab == 1) {
            this.toggle(10, 54, 70, "Nets", 0);
            this.toggle(84, 54, 70, "Bets", 1);
            this.toggle(158, 54, 70, "Profit", 2);
        } else {
            this.button(10, 54, 110, "Rakeback: " + (this.mod.config.rakebackPanel.visible ? "on" : "off"), () -> {
                this.mod.config.rakebackPanel.visible = !this.mod.config.rakebackPanel.visible;
                this.rebuildWidgets();
            });
            this.button(124, 54, 110, "Balance: " + (this.mod.config.balancePanel.visible ? "on" : "off"), () -> {
                this.mod.config.balancePanel.visible = !this.mod.config.balancePanel.visible;
                this.rebuildWidgets();
            });
            this.button(238, 54, 64, "Read bal", () -> this.mod.syncBalance());
        }
        this.button(10, 78, 94, this.preview ? "Sample preview" : "Your records", () -> {
            this.preview = !this.preview;
            this.rebuildWidgets();
        });
        this.button(108, 78, 94, "Reload settings", () -> {
            this.mod.reloadConfig();
            this.notice = this.mod.status;
            this.rebuildWidgets();
        });
        if (this.tab == 1) {
            this.button(206, 78, 94, this.mod.config.scoreboardBalance ? "Read bal: on" : "Read bal: off", () -> {
                this.mod.config.scoreboardBalance = !this.mod.config.scoreboardBalance;
                this.rebuildWidgets();
            });
        }
        this.button(10, 102, 94, "Next view", () -> this.mod.viewCycle.next(this.mod.config.enabledViews(), System.nanoTime() / 1000000L));
        this.button(108, 102, 94, this.mod.config.autoCycle ? "Cycle: 8s" : "Cycle: paused", () -> {
            this.mod.config.autoCycle = !this.mod.config.autoCycle;
            this.rebuildWidgets();
        });
        this.button(206, 102, 94, this.mod.config.autoDetectPayments ? "Auto bets: on" : "Auto bets: off", () -> {
            this.mod.config.autoDetectPayments = !this.mod.config.autoDetectPayments;
            this.rebuildWidgets();
        });
        this.addLoanToggle();
        this.addAppearanceButton();
        this.addTextBoxesButton();
    }

    private void toggle(int x, int y, int width, String name, int panel) {
        this.button(x, y, width, name + (this.mod.config.panels[panel].visible ? ": on" : ": off"), () -> {
            this.mod.config.panels[panel].visible = !this.mod.config.panels[panel].visible;
            this.rebuildWidgets();
        });
    }

    private void button(int x, int y, int width, String label, Runnable action) {
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)label), button -> action.run()).bounds(x, y, width, 20).build());
    }

public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        FestiveUi.background(g, this.width, this.height, this.mod.config);
        g.fill(0, 0, this.width, this.height, 285937688);
        g.text(this.font, "ECONOMY / HUD EDITOR", 10, 10, -2974791, true);
        g.text(this.font, this.font.plainSubstrByWidth(this.notice, Math.max(10, this.width - 220)), 210, 10, -6313284, false);
        TrackerRenderer.drawAll((GuiGraphicsExtractor)g, this.mod.config, this.preview ? this.sample : this.mod.ledger, this.mod.viewCycle, this.width, this.height, true, this.preview, this.mod.wallet.estimated());
        if (LoanUi.viewsOff(IntStream.range(0, 3).noneMatch(i -> this.mod.config.panels[i].visible), this.mod.config)) {
            g.text(this.font, "Rotating views off. Enable them in Trackers.", 10, 140, -6313284, false);
        }
        if (this.dragging >= 0) {
            TrackerRenderer.Bounds box = TrackerRenderer.bounds(this.mod.config, this.dragging, this.width, this.height, true);
            g.fill(box.x(), box.y(), box.x() + box.width(), box.y() + 1, -2974791);
        }
        super.extractRenderState((GuiGraphicsExtractor)g, (int)mouseX, (int)mouseY, (float)delta);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() != com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT || event.y() < 160.0) {
            return false;
        }
        for (int widget = 2; widget >= 0; --widget) {
            TrackerRenderer.Bounds box = TrackerRenderer.bounds(this.mod.config, widget, this.width, this.height, true);
            if (!this.mod.config.boxVisible(widget) || !box.contains(event.x(), event.y())) continue;
            this.dragging = widget;
            this.offsetX = event.x() - (double)box.x();
            this.offsetY = event.y() - (double)box.y();
            return true;
        }
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging < 0) {
            return super.mouseDragged(event, dx, dy);
        }
        TrackerRenderer.Bounds box = TrackerRenderer.bounds(this.mod.config, this.dragging, this.width, this.height, true);
        this.mod.config.box((int)this.dragging).x = this.clamp((event.x() - this.offsetX) / (double)Math.max(1, this.width - box.width()));
        this.mod.config.box((int)this.dragging).y = this.clamp((event.y() - this.offsetY - 160.0) / (double)Math.max(1, this.height - 160 - box.height()));
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging >= 0) {
            this.dragging = -1;
            return true;
        }
        return super.mouseReleased(event);
    }

    private float clamp(double value) {
        return (float)Math.max(0.0, Math.min(1.0, value));
    }

    public void onClose() {
        if (this.mod.saveConfig()) {
            DashboardNavigation.open(this.mod);
        } else {
            if (this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("[Economy Tracker] Settings were not saved: "+this.mod.status));
            }
            DashboardNavigation.open(this.mod);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void addLoanToggle() {
        if (this.tab == 1) {
            this.button(232, 54, 70, LoanService.toggleLabel(this.mod.config.loansVisible), () -> {
                this.mod.config.loansVisible ^= true;
                this.rebuildWidgets();
            });
        }
    }

    private void addAppearanceButton() {
        this.button(184, 28, 62, "Style", this::openAppearance);
    }

    private void openAppearance() {
        Minecraft.getInstance().gui.setScreen((Screen)new AppearanceScreen(this.mod, this));
    }

    private void addTextBoxesButton() {
        if (this.tab == 2) {
            this.button(206, 78, 94, "Text boxes", this::openTextBoxes);
        }
    }

    private void openTextBoxes() {
        Minecraft.getInstance().gui.setScreen((Screen)new TextBoxesScreen(this.mod, this));
    }
}
