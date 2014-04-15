/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author rsmitty
 */
public class FileBrowser {
    
    public ArrayList<String> GetFileNames(String directoryPath){
        File f = new File(directoryPath);
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
        return names;
    }
    
}
