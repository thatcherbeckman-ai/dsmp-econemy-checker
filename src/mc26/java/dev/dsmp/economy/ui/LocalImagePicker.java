package dev.dsmp.economy.ui;

import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** SDL3's asynchronous native picker, using libraries already shipped with 26.3. */
final class LocalImagePicker {
    private record Request(Consumer<String> selected,Consumer<String> error){}
    private static final AtomicReference<Request> ACTIVE=new AtomicReference<>();
    // One callback/filter allocation lives as long as SDL itself, avoiding callbacks into freed memory.
    private static final java.nio.ByteBuffer NAME=MemoryUtil.memUTF8("PNG images"),PATTERN=MemoryUtil.memUTF8("png");
    private static final SDL_DialogFileFilter.Buffer FILTERS=SDL_DialogFileFilter.calloc(1);
    private static final SDL_DialogFileCallback CALLBACK=SDL_DialogFileCallback.create((userdata,files,filter)->{
        Request request=ACTIVE.getAndSet(null);if(request==null)return;
        if(files==0){Minecraft.getInstance().execute(()->request.error.accept("Could not open the image chooser. Use Folder and Refresh."));return;}
        long first=MemoryUtil.memGetAddress(files);String path=first==0?null:MemoryUtil.memUTF8(first);
        Minecraft.getInstance().execute(()->request.selected.accept(path));
    });
    static {FILTERS.get(0).name(NAME).pattern(PATTERN);}
    static void choose(Consumer<String> selected,Consumer<String> error){
        var request=new Request(selected,error);if(!ACTIVE.compareAndSet(null,request)){error.accept("The image chooser is already open.");return;}
        try{SDLDialog.SDL_ShowOpenFileDialog(CALLBACK,0,Minecraft.getInstance().getWindow().handle(),FILTERS,(CharSequence)null,false);}
        catch(RuntimeException|LinkageError ex){ACTIVE.compareAndSet(request,null);error.accept("Could not open image chooser. Use Folder and Refresh.");}
    }
}
