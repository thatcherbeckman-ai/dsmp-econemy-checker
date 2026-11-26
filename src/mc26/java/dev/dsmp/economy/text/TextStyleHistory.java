package dev.dsmp.economy.text;
import java.util.*;
import java.util.function.Consumer;

/** Atomic edits and independent bounded Undo/Redo for one open style editor. */
public final class TextStyleHistory {
    private final TextBox box,original;private final Deque<TextBox> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    public TextStyleHistory(TextBox box){this.box=box;original=box.copy();}
    public void edit(Consumer<TextBox> change){var next=box.copy();next.typography=true;change.accept(next);next.validate();undo.addLast(box.copy());while(undo.size()>32)undo.removeFirst();redo.clear();box.restore(next);}
    public boolean canUndo(){return !undo.isEmpty();}public boolean canRedo(){return !redo.isEmpty();}
    public void undo(){move(undo,redo);}public void redo(){move(redo,undo);}
    private void move(Deque<TextBox> from,Deque<TextBox> to){if(from.isEmpty())return;to.addLast(box.copy());box.restore(from.removeLast());}
    public void cancel(){box.restore(original);undo.clear();redo.clear();}
}
