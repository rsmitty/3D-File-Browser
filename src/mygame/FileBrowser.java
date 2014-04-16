/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rsmitty
 */
public class FileBrowser {
    
    public Map<String,String> GetFileNames(String directoryPath) {
       
        Map<String,String> returnList = new HashMap();
    
        File directory = new File(directoryPath);
        try {
            File[] fList = directory.listFiles();
            for (File file : fList) {
                returnList.put(file.getName(),file.getAbsolutePath());
            }
            return returnList;
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public String GetParent(String directoryPath){
        File f = new File(directoryPath);
        return f.getParent();
    }
    
    public Boolean CheckValidPath(String directoryPath){
        File f = new File(directoryPath);
        return f.isDirectory();
    }
}
