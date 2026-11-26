package dev.dsmp.economy.design;

import com.google.gson.*;
import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.menubackground.MenuBackgroundOptions;
import dev.dsmp.economy.inventory.InventoryOptions;
import java.util.*;

/** Explicit cosmetic allowlist. Tracking preferences and recorded data never enter profiles. */
public final class DesignLook {
    public static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    public static final Set<String> FIELDS=Set.of("festiveBackground","animateSnow","menuAccent","textBoxes","appearances","panels","rakebackPanel","balancePanel","scale","opacity","autoCycle","loansVisible");
    public int version=1;public MenuBackgroundOptions background;public InventoryOptions inventory;public AccessOptions access;public JsonObject appearance;
    public static DesignLook capture(TrackerConfig config,MenuBackgroundOptions background,InventoryOptions inventory,AccessOptions access){
        var look=new DesignLook();look.background=background.copy();look.inventory=inventory;look.access=access.copy();look.appearance=new JsonObject();var json=JSON.toJsonTree(config).getAsJsonObject();for(String key:new TreeSet<>(FIELDS))look.appearance.add(key,json.get(key).deepCopy());look.validate();return look;
    }
    public TrackerConfig applyTo(TrackerConfig original){
        validateParts();var json=JSON.toJsonTree(original).getAsJsonObject();for(String key:FIELDS)json.add(key,appearance.get(key).deepCopy());var result=JSON.fromJson(json,TrackerConfig.class);result.validate();return result;
    }
    private void validateParts(){if(version!=1||background==null||inventory==null||access==null||appearance==null||!appearance.keySet().equals(FIELDS))throw new IllegalArgumentException("Invalid appearance profile");background.validate();access.validate();}
    public void validate(){validateParts();var json=JSON.toJsonTree(new TrackerConfig()).getAsJsonObject();for(String key:FIELDS)json.add(key,appearance.get(key).deepCopy());JSON.fromJson(json,TrackerConfig.class).validate();}
    public DesignLook copy(){var result=JSON.fromJson(JSON.toJson(this),DesignLook.class);result.validate();return result;}
}
