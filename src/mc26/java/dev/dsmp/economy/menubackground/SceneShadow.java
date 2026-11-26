package dev.dsmp.economy.menubackground;

/** World-space contact/cast shadow parameters shared by the cosmetic terrain renderers. */
public final class SceneShadow {
    private SceneShadow(){}
    public static double opacity(double x,double z,CharacterComposition options){
        double angle=Math.toRadians(options.lightDirection),dx=Math.cos(angle),dz=Math.sin(angle),soft=.09+options.shadowSoftness*.0032;
        double along=x*dx+z*dz,across=-x*dz+z*dx;
        double contact=.17*Math.exp(-(x*x+z*z)*15),cast=.20*Math.exp(-(across*across)/(soft*soft)-(along-.28)*(along-.28)/(.18+soft));return Math.min(.36,contact+cast);
    }
}
