/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
    public Map<String,String> GetFileNames(String directoryPath) {
        
        Map<String,String> returnList = new HashMap();
        
        long l = 0;
        try {
            DirectoryStream<Path> dirList = Files.newDirectoryStream(Paths.get(directoryPath));
            for (Path path : dirList) {
                if (Files.isDirectory(path)) {
                    l = getSize(path);
                }
                else {
                    l = Files.size(path);
                }
                System.out.println(path.toString() + " - " + l);
                //returnList.put(path.toAbsolutePath(), l);
                returnList.put(path.getFileName().toString(),path.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            System.out.println("access denied");
        }
        return returnList;
    }
    
    public String GetParent(String directoryPath){
        File f = new File(directoryPath);
        return f.getParent();
    }
    
    public Boolean CheckValidPath(String directoryPath){
        File f = new File(directoryPath);
        
        //necessary because this operation returns null on a restricted directory
        File[] fList = f.listFiles();
        if ( fList != null && f.isDirectory() ) {
            return true;
        }
        return false;
    }
    
    public long getSize(Path startPath) throws IOException {
        final AtomicLong size = new AtomicLong(0);

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
