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
    
    /**
     * Checks whether or not a path is a directory and if it is accessible
     * 
     * @param directoryPath Path to validate
     * @return True if the path is a directory and if it is accessible to the filesystem
     */
    public Boolean CheckValidPath(Path directoryPath){
        if (Files.isDirectory(directoryPath) && Files.isReadable(directoryPath)) {
            return true;
        }
        return false;
    }
    
    /**
     * Get the size of the provided path
     * 
     * @param startPath Root path to gather file size for
     * @return  A long containing the size in bytes of the specified path. 
     *          If the specified path is a directory, recursion is used on all subfolders and files.
     *          If the specified path is inaccessible, returns a size of 0.
     * @throws IOException 
     */
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
    
    /**
     * Builds a map of directories their file sizes
     * 
     * @param directory A map of short and absolute directory names
     * @return A map of the absolute directory names and the file size of the directory in bytes
     */
    public Map<Path,Float> getSizeList(Map<Path,Path> directory) {
        Map<Path,Float> sizeList = new HashMap();
        
        for (Map.Entry<Path,Path> path : directory.entrySet()) {
            float size = 0.0f;
            try {
                size = getSize(path.getValue());
                size = convertSize(size, "mega");
            } catch (IOException e) {
                //size calculation failed
            }
            System.out.printf("%s %s: %.2fMb%n", "sizeof ", path.getValue(), size);
            sizeList.put(path.getValue(), size);
        }
        return sizeList;
    }
    
    /**
     * Scale all the file sizes so the largest is rendered as the biggest box
     * 
     * @param sizeList
     * @return The same list only with scaled filesizes
     */
    public Map<Path,Float> normalize(Map<Path,Float> sizeList) {
        float maxVal = 0;
        float newVal = 0;
        
        // find maximum size to use as scaling factor for other values
        for (Map.Entry<Path,Float> entry : sizeList.entrySet()) {
            if (entry.getValue() > maxVal) {
                maxVal = entry.getValue();
            }
        }
        for (Map.Entry<Path,Float> entry : sizeList.entrySet()) {
            newVal = entry.getValue();
            //box size can't be 0 or it won't appear in scene, so make it 0.5
            if (newVal == 0.0f) {
                newVal = 0.5f;
            }
            //since < 1 is close to zero, make it slightly bigger by adding 1.0
            else if (newVal > 0.0f && newVal <= 1.0f) {
                newVal += 1.0f;
            }
            //needs to be bigger than previous 2, but smaller than maximum
            else if (newVal > 1.0f && newVal < maxVal) {
                newVal /= maxVal;
                newVal += 2.0f;
            }
            //needs to be the biggest box
            //!!! this needs work - good for large sets, bad for small sets
            else {
                newVal /= maxVal;
                newVal += 3.0f;
            }
            sizeList.put(entry.getKey(), newVal);
        }
        return sizeList;
    }
    
    /**
     * Converts bytes to kilobytes, megabytes, or gigabytes based on your OS
     * 
     * @param l     Float you wish to convert
     * @param type  Desired converion type
     * @return      Desired conversion type of float value
     */
    public float convertSize(float l, String type) {
        // convert bytes to kilobytes, megabytes, or gigabytes
        float scale = 1024.0f;
        if      ("kilo".equals(type)) {return (l / scale);}
        else if ("mega".equals(type)) {return (l / scale) / scale;}
        else if ("giga".equals(type)) {return ((l / scale) / scale) / scale;}
        else {return 0;}
    }
    
    public void DeleteAFile(Path pathToDelete){
        File f = pathToDelete.toFile();
        f.delete();
    }
    
}
