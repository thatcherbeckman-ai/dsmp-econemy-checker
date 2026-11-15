package dev.dsmp.economy.text;

import java.nio.file.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class TextImageStoreTest {
    private static byte[] png(int w,int h)throws Exception{var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,0x8033DDCC);var out=new ByteArrayOutputStream();ImageIO.write(image,"PNG",out);return out.toByteArray();}
    @Test void importingCopiesPngAndBoundsTheGpuPreviewWithoutAlteringSource(@TempDir Path dir)throws Exception{var source=dir.resolve("My icon.PNG");byte[] bytes=png(1024,512);Files.write(source,bytes);var store=new TextImageStore(dir.resolve("images"));String key=store.importPng(source);assertTrue(key.matches("file:[a-f0-9]{24}-My_icon\\.png"));assertArrayEquals(bytes,Files.readAllBytes(source));assertArrayEquals(bytes,Files.readAllBytes(store.directory().resolve(key.substring(5))));var loaded=store.load(key);assertEquals(256,loaded.width());assertEquals(128,loaded.height());assertEquals(key,store.importPng(source));assertEquals(1,store.list().size());}
    @Test void transparentSmallImagesKeepSizeAndAlpha(@TempDir Path dir)throws Exception{var source=dir.resolve("small.png");Files.write(source,png(24,12));var store=new TextImageStore(dir.resolve("images"));var image=store.load(store.importPng(source));assertEquals(24,image.width());assertEquals(12,image.height());assertEquals(0x80,ImageIO.read(new ByteArrayInputStream(image.bytes())).getRGB(0,0)>>>24);}
    @Test void invalidOversizedAndMissingImagesAreRejected(@TempDir Path dir)throws Exception{assertThrows(IOException.class,()->TextImageStore.normalized("not png".getBytes()));assertThrows(IOException.class,()->TextImageStore.normalized(new byte[TextImageStore.MAX_BYTES+1]));assertThrows(IOException.class,()->TextImageStore.normalized(png(2049,1)));var store=new TextImageStore(dir.resolve("images"));assertThrows(IOException.class,()->store.load("file:missing.png"));assertThrows(IOException.class,()->store.load("file:../outside.png"));assertThrows(IOException.class,()->store.load("builtin:cards"));}
    @Test void symlinksCannotEscapeTheImageFolder(@TempDir Path dir)throws Exception{var store=new TextImageStore(dir.resolve("images"));Files.createDirectories(store.directory());Path outside=dir.resolve("outside.png");Files.write(outside,png(8,8));Files.createSymbolicLink(store.directory().resolve("escape.png"),outside);assertTrue(store.list().isEmpty());assertThrows(IOException.class,()->store.load("file:escape.png"));assertTrue(Files.exists(outside));}
    @Test void imageLimitStillAllowsSelectingAnExistingImport(@TempDir Path dir)throws Exception{var source=dir.resolve("one.png");byte[] bytes=png(4,4);Files.write(source,bytes);var store=new TextImageStore(dir.resolve("images"));var key=store.importPng(source);for(int i=1;i<128;i++)Files.write(store.directory().resolve("icon_"+i+".png"),bytes);assertEquals(128,store.list().size());assertEquals(key,store.importPng(source));Path second=dir.resolve("two.png");Files.write(second,bytes);assertThrows(IOException.class,()->store.importPng(second));}
}
