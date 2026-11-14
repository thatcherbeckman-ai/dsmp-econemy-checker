/*
 * Decompiled with CFR 0.152.
 */
package dev.dsmp.economy.core;

public final class ViewCycle {
    public static final long INTERVAL_MS = 8000L;
    public static final long FADE_MS = 400L;
    public static final int[] ORDER = new int[]{2, 0, 1, 3};
    private int page = 2;
    private boolean started;
    private boolean automatic = true;
    private long startedAt;
    private int previous = -1;
    private long transitionAt;

    public int current(boolean[] enabled, boolean auto, long now) {
        int count = ViewCycle.count(enabled);
        if (count == 0) {
            this.started = false;
            this.previous = -1;
            return -1;
        }
        if (!this.started || !enabled[this.page]) {
            if (!enabled[this.page]) {
                this.page = ViewCycle.following(enabled, this.page);
            }
            this.startedAt = now;
            this.started = true;
            this.automatic = auto;
            this.previous = -1;
        } else if (this.automatic != auto || now < this.startedAt) {
            this.startedAt = now;
            this.automatic = auto;
            if (now < this.transitionAt) {
                this.previous = -1;
            }
        }
        if (auto && count > 1) {
            long elapsed = now - this.startedAt;
            long steps = elapsed / 8000L;
            int old = this.page;
            int i = 0;
            while ((long)i < steps % (long)count) {
                this.page = ViewCycle.following(enabled, this.page);
                ++i;
            }
            this.startedAt = now - elapsed % 8000L;
            if (this.page != old) {
                this.previous = old;
                this.transitionAt = this.startedAt;
            }
        }
        return this.page;
    }

    public int next(boolean[] enabled, long now) {
        if (this.current(enabled, false, now) < 0) {
            return -1;
        }
        int next = ViewCycle.following(enabled, this.page);
        if (this.page != next) {
            this.previous = this.page;
            this.transitionAt = now;
            this.page = next;
        }
        this.startedAt = now;
        return this.page;
    }

    public Frame frame(boolean[] enabled, boolean auto, long now) {
        int current = this.current(enabled, auto, now);
        if (current < 0 || this.previous < 0 || !enabled[this.previous]) {
            return new Frame(current, -1, 1.0);
        }
        double t = Math.clamp((double)(now - this.transitionAt) / 400.0, 0.0, 1.0);
        if (t >= 1.0) {
            this.previous = -1;
            return new Frame(current, -1, 1.0);
        }
        return new Frame(current, this.previous, t * t * (3.0 - 2.0 * t));
    }

    public double progress(boolean[] enabled, boolean auto, long now) {
        if (!this.started || ViewCycle.count(enabled) == 0) {
            return 0.0;
        }
        if (!auto || ViewCycle.count(enabled) == 1) {
            return 1.0;
        }
        return Math.clamp((double)(now - this.startedAt) / 8000.0, 0.0, 1.0);
    }

    private static int count(boolean[] enabled) {
        if (enabled == null || enabled.length != 4) {
            throw new IllegalArgumentException("Expected four view toggles");
        }
        int count = 0;
        for (boolean value : enabled) {
            if (!value) continue;
            ++count;
        }
        return count;
    }

    private static int following(boolean[] enabled, int page) {
        int index = 0;
        while (ORDER[index] != page) {
            ++index;
        }
        for (int offset = 1; offset <= ORDER.length; ++offset) {
            int candidate = ORDER[(index + offset) % ORDER.length];
            if (!enabled[candidate]) continue;
            return candidate;
        }
        return -1;
    }

    public record Frame(int page, int previous, double blend) {
    }
}

