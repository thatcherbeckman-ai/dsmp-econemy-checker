package dev.dsmp.economy.loans;
import dev.dsmp.economy.TrackerConfig;
public final class LoanUi {
    private LoanUi() {}
    public static boolean viewsOff(boolean originalViewsOff, TrackerConfig config) {
        return originalViewsOff && !config.enabledViews()[3];
    }
}
