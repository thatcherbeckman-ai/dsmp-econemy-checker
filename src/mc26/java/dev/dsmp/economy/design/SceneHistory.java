package dev.dsmp.economy.design;

import dev.dsmp.economy.menubackground.MenuBackgroundOptions;
import java.util.*;

/** Bounded draft history; no history action writes a settings file. */
public final class SceneHistory {
    private final Deque<MenuBackgroundOptions> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    public boolean canUndo(){return !undo.isEmpty();}public boolean canRedo(){return !redo.isEmpty();}
    public void record(MenuBackgroundOptions before,MenuBackgroundOptions after){before.validate();after.validate();if(DesignLook.JSON.toJson(before).equals(DesignLook.JSON.toJson(after)))return;undo.addLast(before.copy());while(undo.size()>64)undo.removeFirst();redo.clear();}
    public MenuBackgroundOptions undo(MenuBackgroundOptions current){if(undo.isEmpty())return current.copy();redo.addLast(current.copy());return undo.removeLast().copy();}
    public MenuBackgroundOptions redo(MenuBackgroundOptions current){if(redo.isEmpty())return current.copy();undo.addLast(current.copy());return redo.removeLast().copy();}
}
