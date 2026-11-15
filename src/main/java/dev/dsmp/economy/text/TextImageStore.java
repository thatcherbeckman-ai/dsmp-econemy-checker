package dev.dsmp.economy.text;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

/** User-selected PNG copies only. Source files stay intact; GPU previews are bounded. */
public final class TextImageStore {
    public static final int MAX_BYTES=4*1024*1024,MAX_DIMENSION=2048,MAX_FILES=128,PREVIEW_SIZE=256;
    public record Png(byte[] bytes,int width,int height){}
    private final Path directory;
    public TextImageStore(Path directory){this.directory=directory.toAbsolutePath().normalize();}
    public Path directory(){return directory;}
    public List<String> list()throws IOException{
        if(!Files.isDirectory(directory))return List.of();
        try(var paths=Files.list(directory)){return paths.filter(p->Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)&&p.getFileName().toString().length()<=155&&p.getFileName().toString().matches("[a-zA-Z0-9_-][a-zA-Z0-9_.-]*\\.png")&&!p.getFileName().toString().contains("..")).map(p->"file:"+p.getFileName()).sorted().limit(MAX_FILES).toList();}
    }
    public String importPng(Path source)throws IOException{
        byte[] bytes=readBounded(source);normalized(bytes);String stem=source.getFileName().toString().replaceFirst("(?i)\\.png$","").replaceAll("[^A-Za-z0-9_-]","_");if(stem.length()>40)stem=stem.substring(0,40);if(stem.isBlank())stem="image";
        String hash;try{hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).substring(0,24);}catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
        String name=hash+"-"+stem+".png";Files.createDirectories(directory);Path destination=directory.resolve(name);
        if(Files.exists(destination,LinkOption.NOFOLLOW_LINKS)){if(Files.isSymbolicLink(destination)||!Arrays.equals(readBounded(destination),bytes))throw new IOException("Image destination conflicts with an existing file");return "file:"+name;}
        if(list().size()>=MAX_FILES)throw new IOException("Up to 128 imported images are supported");
        Path temporary=Files.createTempFile(directory,"import-",".tmp");try{Files.write(temporary,bytes);Files.move(temporary,destination);}finally{Files.deleteIfExists(temporary);}return "file:"+name;
    }
    public Png load(String key)throws IOException{
        var style=new LineStyle();style.image=key;try{style.validate();}catch(IllegalArgumentException ex){throw new IOException(ex.getMessage(),ex);}
        if(!key.startsWith("file:"))throw new IOException("Not a custom image");Path path=directory.resolve(key.substring(5));
        if(!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)||!path.toRealPath().getParent().equals(directory.toRealPath()))throw new IOException("Image is missing from the images folder");
        return normalized(readBounded(path));
    }
    private static byte[] readBounded(Path path)throws IOException{if(Files.size(path)>MAX_BYTES)throw new IOException("PNG must be at most 4 MB");try(var input=Files.newInputStream(path)){byte[] bytes=input.readNBytes(MAX_BYTES+1);if(bytes.length>MAX_BYTES)throw new IOException("PNG must be at most 4 MB");return bytes;}}
    public static Png normalized(byte[] bytes)throws IOException{
        if(bytes.length>MAX_BYTES||bytes.length<24||!Arrays.equals(Arrays.copyOf(bytes,8),new byte[]{(byte)137,80,78,71,13,10,26,10}))throw new IOException("Choose a PNG image (up to 4 MB)");
        try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException("Unreadable PNG image");var reader=readers.next();
            try{reader.setInput(input);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>MAX_DIMENSION||h>MAX_DIMENSION)throw new IOException("PNG dimensions must be 1 to 2048 pixels");BufferedImage image=reader.read(0);int nw=w,nh=h;
                if(Math.max(w,h)>PREVIEW_SIZE){float scale=PREVIEW_SIZE/(float)Math.max(w,h);nw=Math.max(1,Math.round(w*scale));nh=Math.max(1,Math.round(h*scale));var resized=new BufferedImage(nw,nh,BufferedImage.TYPE_INT_ARGB);var g=resized.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(image,0,0,nw,nh,null);}finally{g.dispose();}image=resized;}
                var output=new ByteArrayOutputStream();if(!ImageIO.write(image,"PNG",output))throw new IOException("PNG encoder unavailable");return new Png(output.toByteArray(),nw,nh);
            }finally{reader.dispose();}
        }catch(RuntimeException ex){throw new IOException("Unreadable PNG image",ex);}
    }
}
