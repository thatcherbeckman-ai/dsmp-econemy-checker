package dev.dsmp.economy.shaders;

import java.lang.reflect.*;

/** Optional binding to Iris's public v0 API. No Iris implementation or classes are bundled. */
public final class IrisBridge {
    public enum Availability { ABSENT, READY, UNAVAILABLE }
    public record State(boolean enabled,boolean rendering){}
    public static final class Unavailable extends Exception {public Unavailable(){super("Iris controls are unavailable. Check the Iris and Minecraft versions.");}}
    private final Availability availability;private final String version;
    private final Object api,config;private final Method open,enabled,rendering,apply;
    private IrisBridge(Availability availability,String version,Object api,Object config,Method open,Method enabled,Method rendering,Method apply){this.availability=availability;this.version=version;this.api=api;this.config=config;this.open=open;this.enabled=enabled;this.rendering=rendering;this.apply=apply;}
    public static IrisBridge discover(boolean installed,String version,ClassLoader loader){
        if(!installed)return unavailable(Availability.ABSENT,"");
        try{return fromApiTypes(version,Class.forName("net.irisshaders.iris.api.v0.IrisApi",false,loader),Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig",false,loader));}
        catch(ReflectiveOperationException|LinkageError|RuntimeException ex){return unavailable(Availability.UNAVAILABLE,version);}
    }
    /** Class injection also permits isolated contract tests without shipping Iris or invoking its renderer. */
    public static IrisBridge fromApiTypes(String version,Class<?> apiType,Class<?> configType){
        try{
            Method singleton=apiType.getMethod("getInstance");if(!Modifier.isStatic(singleton.getModifiers()))throw new IllegalArgumentException();
            Object api=singleton.invoke(null);if(!apiType.isInstance(api))throw new IllegalArgumentException();
            Object config=apiType.getMethod("getConfig").invoke(api);if(!configType.isInstance(config))throw new IllegalArgumentException();
            Method open=apiType.getMethod("openMainIrisScreenObj",Object.class),enabled=configType.getMethod("areShadersEnabled"),rendering=apiType.getMethod("isShaderPackInUse"),apply=configType.getMethod("setShadersEnabledAndApply",boolean.class);
            if(enabled.getReturnType()!=boolean.class||rendering.getReturnType()!=boolean.class||apply.getReturnType()!=void.class)throw new IllegalArgumentException();
            return new IrisBridge(Availability.READY,clean(version),api,config,open,enabled,rendering,apply);
        }catch(ReflectiveOperationException|LinkageError|RuntimeException ex){return unavailable(Availability.UNAVAILABLE,version);}
    }
    private static IrisBridge unavailable(Availability kind,String version){return new IrisBridge(kind,clean(version),null,null,null,null,null,null);}
    private static String clean(String text){return text==null?"unknown":text.replaceAll("[\\p{Cc}\\p{Cf}§]","").codePoints().limit(80).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();}
    public Availability availability(){return availability;}
    public String version(){return version;}
    public boolean ready(){return availability==Availability.READY;}
    public State state()throws Unavailable{
        if(!ready())throw new Unavailable();
        try{return new State((boolean)enabled.invoke(config),(boolean)rendering.invoke(api));}
        catch(ReflectiveOperationException|LinkageError|RuntimeException ex){throw new Unavailable();}
    }
    public void setEnabled(boolean value)throws Unavailable{
        if(!ready())throw new Unavailable();
        try{if((boolean)enabled.invoke(config)!=value)apply.invoke(config,value);}
        catch(ReflectiveOperationException|LinkageError|RuntimeException ex){throw new Unavailable();}
    }
    public Object screen(Object parent)throws Unavailable{
        if(!ready())throw new Unavailable();
        try{Object result=open.invoke(api,parent);if(result==null)throw new IllegalArgumentException();return result;}
        catch(ReflectiveOperationException|LinkageError|RuntimeException ex){throw new Unavailable();}
    }
}
