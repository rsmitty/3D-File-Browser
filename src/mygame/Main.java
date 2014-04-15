package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.Map;

public class Main extends SimpleApplication {

    private Node shootables;
    private Map<String,String> fileHash;
    private String currentPath;
    private FileBrowser fb = new FileBrowser();
    
     public static void main(String[] args){
        Main app = new Main();
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
        
      //Setup listeners for mouse buttons  
      initKeys();
      
      //Determine OS for root dir
      if ( System.getProperty("os.name").contains("Windows")){
          currentPath = "C:\\";
      }
      else{
          currentPath = "/";
      }
      
      //Create UI - Crosshairs, Boxes, and Labels
      SetupUI(currentPath);
      
      
      }
    
    private void SetupUI(String directory){
      
      //Reset cam as UI is reset.
      cam.setLocation(new Vector3f(0,0,10));
      
      //Speed up camera
      flyCam.setMoveSpeed(3);
      
      MakeCrosshairs();
      
      //Holds all the boxes so they're clickable
      shootables = new Node("Shootables");
      rootNode.attachChild(shootables);
      
      //Get all files in current directory
      fileHash = fb.GetFileNames(directory);
      
      //Lay boxes out in rows of 5
      int xaxis = 0;
      int zaxis = 0;
      
      for (Map.Entry<String, String> file :fileHash.entrySet()) {
        String filename = file.getKey();
        
        if (xaxis > 20){
          xaxis = 0;
          zaxis -= 5;
        }
          
        MakeABox(filename, xaxis,1,zaxis);
        MakeALabel(filename,xaxis,1,zaxis);  
           
        xaxis += 5;
      }
    }
    
    private void MakeABox(String filename, int x, int y, int z){
        Box box = new Box(1,1,1);
        Geometry boxGeo = new Geometry("Box", box);
        boxGeo.setLocalTranslation(new Vector3f(x,y,z));
        boxGeo.setName(filename);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.randomColor());
        boxGeo.setMaterial(mat);
        shootables.attachChild(boxGeo);
    }
    
    private void MakeALabel(String filename, int x, int y, int z){
        float moveX = (float) x - 0.5f;
        float moveZ = (float) z + 1.02f;
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText helloText = new BitmapText(guiFont, false);
        helloText.setSize(0.3f);
        helloText.setText(filename);
        helloText.setLocalTranslation(moveX, 2, moveZ);
        rootNode.attachChild(helloText);
    }
    
    private void MakeCrosshairs(){
      guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
      BitmapText cross = new BitmapText(guiFont, false);
      cross.setSize(guiFont.getCharSet().getRenderedSize() * 2);
      cross.setText("+");
      cross.setLocalTranslation((settings.getWidth() / 2) , (settings.getHeight() / 2)  , 0);
      guiNode.attachChild(cross);
    }
    
    /** Declaring the "Shoot" action and mapping to its triggers. */
    public void initKeys() {
      inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
      inputManager.addMapping("Back", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
      inputManager.addListener(shootListener, "Shoot");
      inputManager.addListener(shootListener, "Back");
    }
    /** Defining the "Shoot" action: Determine what was hit and how to respond. */
    private ActionListener shootListener = new ActionListener() {
      public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("Shoot") && !keyPressed) {

          CollisionResults results = new CollisionResults();
          Ray ray = new Ray(cam.getLocation(), cam.getDirection());
          shootables.collideWith(ray, results);

          if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            String closestPath = fileHash.get(closest.getGeometry().getName());
            
            if(fb.CheckValidPath(closestPath)){
              rootNode.detachAllChildren();
              currentPath = closestPath;
              SetupUI(currentPath);
            }
          }
        }
        
        else if (name.equals("Back") && !keyPressed){
            String parentPath = fb.GetParent(currentPath);
            
            if(parentPath != null){
            currentPath = fb.GetParent(currentPath);
            rootNode.detachAllChildren();
            SetupUI(currentPath);
            }
        }
      }
    };
    
    
    
}
