package dev.dsmp.economy.design;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Named local profiles and a bounded, deduplicated history of automatic appearance backups. */
public final class DesignArchive {
    public record Entry(String id,String name,long created,DesignLook look){}
    public record Summary(String id,String name,long created,boolean valid){}
    private final Path root;private long lastCreated;
    public DesignArchive(Path root){this.root=root;}
    private Path folder(boolean backup)throws IOException{Path dir=root.resolve(backup?"backups":"profiles");if(Files.isSymbolicLink(root)||Files.isSymbolicLink(dir))throw new IOException("Design folders cannot be symbolic links");return dir;}
    private Path file(boolean backup,String id)throws IOException{if(id==null||!id.matches("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}"))throw new IOException("Invalid design ID");return folder(backup).resolve(id+".json");}
    public List<Summary> list(boolean backup)throws IOException{
        Path dir=folder(backup);if(!Files.exists(dir))return List.of();var rows=new ArrayList<Summary>();try(var files=Files.list(dir)){var paths=files.filter(p->p.getFileName().toString().endsWith(".json")).limit(129).toList();if(paths.size()>128)throw new IOException("Too many design files");for(Path path:paths){String name=path.getFileName().toString(),id=name.substring(0,name.length()-5);try{var value=load(backup,id);lastCreated=Math.max(lastCreated,value.created);rows.add(new Summary(id,value.name,value.created,true));}catch(IOException ex){rows.add(new Summary(id,"Unreadable file (retained)",0,false));}}}rows.sort(Comparator.comparingLong(Summary::created).reversed().thenComparing(Summary::id));return List.copyOf(rows);
    }
    public Entry load(boolean backup,String id)throws IOException{
        Path file=file(backup,id);if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>2_097_152)throw new IOException("Invalid design file");
        try{var value=DesignLook.JSON.fromJson(Files.readString(file),Entry.class);if(value==null||!id.equals(value.id)||value.created<0)throw new IllegalArgumentException("Invalid design record");checkName(value.name);value.look.validate();return new Entry(value.id,value.name,value.created,value.look.copy());}catch(RuntimeException ex){throw new IOException("Could not read design; original file retained",ex);}
    }
    public Entry saveProfile(String name,DesignLook look)throws IOException{
        checkName(name);look.validate();var list=list(false);String id=null;for(var item:list)if(item.name.equalsIgnoreCase(name)){id=item.id;break;}if(id==null&&list.size()>=24)throw new IOException("Profile limit reached (24)");return write(false,id==null?UUID.randomUUID().toString():id,name,look);
    }
    public void backup(String name,DesignLook look)throws IOException{
        look.validate();var list=list(true);if(!list.isEmpty()&&list.getFirst().valid&&DesignLook.JSON.toJson(load(true,list.getFirst().id).look).equals(DesignLook.JSON.toJson(look)))return;
        write(true,UUID.randomUUID().toString(),name,look);var updated=list(true);for(int i=12;i<updated.size();i++)if(updated.get(i).valid)Files.delete(file(true,updated.get(i).id));
    }
    private Entry write(boolean backup,String id,String name,DesignLook look)throws IOException{checkName(name);var entry=new Entry(id,name,lastCreated=Math.max(System.currentTimeMillis(),lastCreated+1),look.copy());String json=DesignLook.JSON.toJson(entry);if(json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>2_097_152)throw new IOException("Design exceeds the 2 MB limit");DesignFiles.write(file(backup,id),json);return entry;}
    public void deleteProfile(String id)throws IOException{load(false,id);Files.delete(file(false,id));}
    private static void checkName(String name){if(name==null||name.isBlank()||name.length()>40||name.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Use a profile name of 1–40 characters");}
}
