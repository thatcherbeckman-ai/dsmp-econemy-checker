package dev.dsmp.economy.shaders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IrisBridgeTest {
    public interface Api {
        static Api getInstance(){return current;}
        Config getConfig();boolean isShaderPackInUse();Object openMainIrisScreenObj(Object parent);
    }
    public interface Config {boolean areShadersEnabled();void setShadersEnabledAndApply(boolean enabled);}
    private static Api current;
    private static final class ConfigImpl implements Config {
        boolean enabled;int changes;boolean fail;
        public boolean areShadersEnabled(){if(fail)throw new IllegalStateException("private implementation detail");return enabled;}
        public void setShadersEnabledAndApply(boolean value){if(fail)throw new IllegalStateException("private implementation detail");enabled=value;changes++;}
    }
    private static final class ApiImpl implements Api {
        final ConfigImpl config=new ConfigImpl();Object parent,screen=new Object();boolean fail,rendering;
        public Config getConfig(){return config;}public boolean isShaderPackInUse(){if(fail)throw new IllegalStateException("private implementation detail");return rendering;}
        public Object openMainIrisScreenObj(Object parent){if(fail)throw new IllegalStateException("private implementation detail");this.parent=parent;return screen;}
    }
    private static IrisBridge bridge(ApiImpl api){current=api;return IrisBridge.fromApiTypes("API test",Api.class,Config.class);}
    @Test void missingIrisDoesNotTryToLoadAnyIrisClasses(){
        var loader=new ClassLoader(){@Override public Class<?> loadClass(String name){throw new AssertionError("Unexpected optional class load: "+name);}};
        var bridge=IrisBridge.discover(false,"",loader);assertEquals(IrisBridge.Availability.ABSENT,bridge.availability());assertFalse(bridge.ready());assertThrows(IrisBridge.Unavailable.class,bridge::state);assertThrows(IrisBridge.Unavailable.class,()->bridge.setEnabled(true));assertThrows(IrisBridge.Unavailable.class,()->bridge.screen(null));
    }
    @Test void installedButMissingOrBrokenApiIsOptionalAndSafe(){
        var missing=IrisBridge.discover(true,"unknown",new ClassLoader(null){});assertEquals(IrisBridge.Availability.UNAVAILABLE,missing.availability());
        var broken=IrisBridge.discover(true,"broken",new ClassLoader(){@Override public Class<?> loadClass(String name){throw new NoClassDefFoundError("optional dependency");}});assertEquals(IrisBridge.Availability.UNAVAILABLE,broken.availability());assertFalse(broken.ready());
    }
    @Test void stateSeparatesEnabledConfigurationFromActualRendering()throws Exception{
        var api=new ApiImpl();var bridge=bridge(api);assertTrue(bridge.ready());assertEquals(new IrisBridge.State(false,false),bridge.state());api.config.enabled=true;assertEquals(new IrisBridge.State(true,false),bridge.state());api.rendering=true;assertEquals(new IrisBridge.State(true,true),bridge.state());assertEquals(0,api.config.changes);
    }
    @Test void publicConfigAppliesOnlyExplicitStateChangesEvenForPrivateImplementations()throws Exception{
        var api=new ApiImpl();var bridge=bridge(api);bridge.setEnabled(false);assertEquals(0,api.config.changes);bridge.setEnabled(true);assertTrue(api.config.enabled);assertEquals(1,api.config.changes);bridge.setEnabled(true);assertEquals(1,api.config.changes);bridge.setEnabled(false);assertFalse(api.config.enabled);assertEquals(2,api.config.changes);
    }
    @Test void screenFactoryKeepsParentIdentityAndRejectsNull()throws Exception{
        var api=new ApiImpl();var bridge=bridge(api);Object parent=new Object();assertSame(api.screen,bridge.screen(parent));assertSame(parent,api.parent);api.screen=null;assertThrows(IrisBridge.Unavailable.class,()->bridge.screen(parent));
    }
    @Test void pluginErrorsBecomeSafeDiagnosticsWithoutImplementationDetails()throws Exception{
        var api=new ApiImpl();var bridge=bridge(api);api.fail=true;var stateError=assertThrows(IrisBridge.Unavailable.class,bridge::state);assertFalse(stateError.getMessage().contains("private implementation"));assertThrows(IrisBridge.Unavailable.class,()->bridge.screen(null));api.config.fail=true;assertThrows(IrisBridge.Unavailable.class,()->bridge.setEnabled(true));
    }
    public static final class NullSingleton {public static Object getInstance(){return null;}}
    public static final class NoSingleton {}
    @Test void unsupportedContractsAreRejectedAndVersionLabelsAreBounded(){
        assertFalse(IrisBridge.fromApiTypes("broken",NullSingleton.class,Config.class).ready());assertFalse(IrisBridge.fromApiTypes("broken",NoSingleton.class,Config.class).ready());var api=new ApiImpl();current=api;var version=IrisBridge.fromApiTypes("§aTest\n\u202e"+"x".repeat(200),Api.class,Config.class).version();assertTrue(version.codePointCount(0,version.length())<=80);assertFalse(version.contains("§"));assertFalse(version.contains("\n"));assertFalse(version.contains("\u202e"));
    }
}
