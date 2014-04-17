/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.DirectoryStream;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author rsmitty
 */
public class FileBrowser {
    
    public Map<Path,Path> GetFileNames(Path directoryPath) {
        
        Map<Path,Path> returnList = new HashMap();
        
        try {
            DirectoryStream<Path> dirList = Files.newDirectoryStream(directoryPath);
            for (Path path : dirList) {
                returnList.put(path.getFileName(),path.toAbsolutePath());
            }
        } catch (IOException e) {
            //access denied - should never happen since we begin on root dir
            e.printStackTrace();
        }
        return returnList;
    }
    
    public Path GetParent(Path directoryPath){
        return directoryPath.getParent();
    }
    
    public Boolean CheckValidPath(Path directoryPath){
        if (Files.isDirectory(directoryPath) && Files.isReadable(directoryPath)) {
            return true;
        }
        return false;
    }
    
    public long getSize(Path startPath) throws IOException {
        final AtomicLong size = new AtomicLong(0);
        System.out.println("checking " + startPath.toString());
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                //access is denied - continue
                return FileVisitResult.CONTINUE;
            }
        });

        return size.get();
    }
}
