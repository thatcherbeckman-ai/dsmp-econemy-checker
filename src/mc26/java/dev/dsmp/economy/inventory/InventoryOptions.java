package dev.dsmp.economy.inventory;

/** Immutable cosmetic preferences, deliberately separate from economy tracking/configuration. */
public record InventoryOptions(InventoryTheme theme,boolean animations,int speed,int intensity) {
    public static final String[] INTENSITIES={"Low","Medium","High"};
    public InventoryOptions {
        if(theme==null||speed<25||speed>200||intensity<0||intensity>2)throw new IllegalArgumentException("Invalid inventory theme settings");
    }
    public static InventoryOptions defaults(){return new InventoryOptions(InventoryTheme.VANILLA,true,100,1);}
    public InventoryOptions theme(InventoryTheme value){return new InventoryOptions(value,animations,speed,intensity);}
    public InventoryOptions animations(boolean value){return new InventoryOptions(theme,value,speed,intensity);}
    public InventoryOptions speed(int value){return new InventoryOptions(theme,animations,Math.clamp(value,25,200),intensity);}
    public InventoryOptions intensity(int value){return new InventoryOptions(theme,animations,speed,value);}
    public double time(double seconds){return animations&&Double.isFinite(seconds)?Math.max(0,seconds)%4096*speed/100d:0;}
}
