package dev.dsmp.economy.design;

public final class AccessOptions {
    public int textScale=100;public boolean highContrast=false,reducedMotion=false;
    public AccessOptions copy(){var n=new AccessOptions();n.textScale=textScale;n.highContrast=highContrast;n.reducedMotion=reducedMotion;return n;}
    public void validate(){if(textScale<100||textScale>130)throw new IllegalArgumentException("Menu text must be 100–130%");}
}
