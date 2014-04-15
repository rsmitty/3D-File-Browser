package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import java.util.ArrayList;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

     public static void main(String[] args){
        Main app = new Main();
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
   
      FileBrowser fb = new FileBrowser();
      ArrayList<String> filenames = fb.GetFileNames("/");
      
      int xaxis = 0;
      int zaxis = 0;
      
      for(String filename: filenames){
          
        if (xaxis > 14){
          xaxis = 0;
          zaxis -= 3.5;
        }
          
        MakeABox(xaxis,1,zaxis);
        MakeALabel(filename,xaxis,1,zaxis);  
           
        xaxis += 3.5;    
      }

    }
    
    public void MakeABox(int x, int y, int z){
        Box box = new Box(1,1,1);
        Geometry blue = new Geometry("Box", box);
        blue.setLocalTranslation(new Vector3f(x,y,z));
        Material mat1 = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.randomColor());
        blue.setMaterial(mat1);
        rootNode.attachChild(blue); // put this node in the scene
    }
    
    public void MakeALabel(String filename, int x, int y, int z){
        float moveX = (float) x - 0.5f;
        float moveZ = (float) z + 1.1f;
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText helloText = new BitmapText(guiFont, false);
        helloText.setSize(0.3f);
        helloText.setText(filename);
        helloText.setLocalTranslation(moveX, 2, moveZ);
        rootNode.attachChild(helloText);
    }
    
}
