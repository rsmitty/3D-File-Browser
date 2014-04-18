package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main extends SimpleApplication {
    
    //Holders for 3D Objects
    private Node shootables;
    private Node labels;
    
    //Holders for filenames and sizes
    private Map<Path,Path> fileHash;
    private Map<Path,Float> sizeHash;
    
    //Tracks what dir we're in
    private Path currentPath;
    
    //Access to FileBrowser.java utils
    private FileBrowser fb = new FileBrowser();
    
    //Used to request threads for updating sizes in the background
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private Future future = null;
    private Boolean timeToUpdate = false;
    
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
          currentPath = Paths.get("C:\\");
      }
      else{
          currentPath = Paths.get("/");
      }
      
      //Create UI - Crosshairs, Boxes, and Labels
      SetupUI(currentPath);
      
      }
    
    public void SetupUI(Path directory){
      
      //Speed up camera
      flyCam.setMoveSpeed(10);
      
      MakeCrosshairs();
      
      //Holds all the boxes so they're clickable
      shootables = new Node("Shootables");
      labels = new Node("Labels");
      rootNode.attachChild(shootables);
      rootNode.attachChild(labels);
      
      //Get all files in current directory
      fileHash = fb.GetFileNames(directory);
      
      //Lay boxes out in rows of 5
      int xaxis = 0;
      int zaxis = 0;
      
      for (Map.Entry<Path,Path> file : fileHash.entrySet()) {
        String filename = file.getKey().toString();
        
        if (xaxis > 20){
          xaxis = 0;
          zaxis -= 5;
        }
        
        MakeABox(filename,xaxis,1,zaxis);
        MakeALabel(filename,xaxis,1,zaxis);
        
        xaxis += 5;
      }
      
      //Call the background process for getting file sizes
      future = executor.submit(getSizeHash);
    }
    
    /**
     * Paints a box on the screen
     * 
     * @param filename, x, y, z values for location.
     *
     */
    public void MakeABox(String filename, float x, float y, float z){
        Box box = new Box(1,y,1);
        Geometry boxGeo = new Geometry("Box", box);
        boxGeo.setLocalTranslation(new Vector3f(x,y,z));
        boxGeo.setName(filename);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.randomColor());
        boxGeo.setMaterial(mat);
        shootables.attachChild(boxGeo);
    }
    
    /**
     * Paints a label on the screen
     * 
     * @param filename, x, y, z values for location.
     *
     */
    public void MakeALabel(String filename, float x, float y, float z){
        float moveX = (float) x - .8f;
        float moveZ = (float) z + 1.02f;
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText helloText = new BitmapText(guiFont, false);
        helloText.setSize(0.3f);
        helloText.setName("label-"+filename);
        // to keep label short
        if (filename.length() > 15) {
            filename = filename.substring(0, 14) + "...";
        }
        helloText.setText(filename);
        helloText.setLocalTranslation(moveX, 1, moveZ);
        rootNode.attachChild(helloText);
    }
    
    /**
     * Simply adds a 2d + to the screen for help in "shooting".
     * 
     * @param 
     *
     */
    public void MakeCrosshairs(){
      guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
      BitmapText cross = new BitmapText(guiFont, false);
      cross.setSize(guiFont.getCharSet().getRenderedSize() * 2);
      cross.setText("+");
      cross.setLocalTranslation((settings.getWidth() / 2) , (settings.getHeight() / 2)  , 0);
      guiNode.attachChild(cross);
    }
    
    /**
     * Animations for blowing up a 3D object
     * 
     * @param 
     *
     */
    public void BlowShitUp(String boxName, Vector3f collisionLocation){
        ParticleEmitter debris = CreateDebris(collisionLocation);
        ParticleEmitter fire = CreateFire(collisionLocation);
        
        rootNode.attachChild(debris);
        rootNode.attachChild(fire);
        
        debris.emitAllParticles();
        fire.emitAllParticles();
        
        shootables.detachChildNamed(boxName);
        rootNode.detachChildNamed("label-"+boxName);
    }
    
     /**
     * Explodes debris from whatever object we're blowing up
     * 
     * @param 
     *
     */
    public ParticleEmitter CreateDebris(Vector3f location){
        ParticleEmitter debrisEffect = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
        Material debrisMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debrisMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debrisEffect.setMaterial(debrisMat);
        debrisEffect.setImagesX(3); debrisEffect.setImagesY(3); // 3x3 texture animation
        debrisEffect.setRotateSpeed(4);
        debrisEffect.setSelectRandomImage(true);
        debrisEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 8, 0));
        debrisEffect.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
        debrisEffect.setGravity(0f,6f,0f);
        debrisEffect.getParticleInfluencer().setVelocityVariation(.60f);
        debrisEffect.setLocalTranslation(location);
        debrisEffect.setParticlesPerSec(0);
        return debrisEffect;
    }
    
    /**
     * Explodes fire from whatever object we're blowing up
     * 
     * @param 
     *
     */
    public ParticleEmitter CreateFire(Vector3f location){
        ParticleEmitter fireEffect = new ParticleEmitter("Fire", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect.setMaterial(fireMat);
        fireEffect.setImagesX(2); fireEffect.setImagesY(2); // 2x2 texture animation
        fireEffect.setEndColor( new ColorRGBA(1f, 0f, 0f, 1f) );   // red
        fireEffect.setStartColor( new ColorRGBA(1f, 1f, 0f, 0.5f) ); // yellow
        fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect.setStartSize(1.5f);
        fireEffect.setEndSize(0.1f);
        fireEffect.setGravity(0f,0f,0f);
        fireEffect.setLowLife(0.5f);
        fireEffect.setHighLife(3f);
        fireEffect.getParticleInfluencer().setVelocityVariation(0.3f);
        fireEffect.setLocalTranslation(location);
        fireEffect.setParticlesPerSec(0);
        return fireEffect;
    }
    
     /**
     * Listeners for key values. Handles picking dirs, deleting files, going to parent dir
     * 
     * @param 
     *
     */
    public void initKeys() {
      inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
      inputManager.addMapping("Back", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
      inputManager.addMapping("Delete", new KeyTrigger(KeyInput.KEY_SPACE));
      inputManager.addListener(shootListener, "Shoot");
      inputManager.addListener(shootListener, "Back");
      inputManager.addListener(shootListener, "Delete");
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
            String hit = closest.getGeometry().getName();
            Path hitPath = fileHash.get(Paths.get(hit));
            
            if(fb.CheckValidPath(hitPath)){
              rootNode.detachAllChildren();
              currentPath = hitPath;
              future = executor.submit(getSizeHash);
              SetupUI(currentPath);
            }
            else {
                System.out.println("Access Denied");
            }
          }
        }
        
        else if (name.equals("Back") && !keyPressed){
            Path parentPath = fb.GetParent(currentPath);   
            if(parentPath != null){
                currentPath = parentPath;
                rootNode.detachAllChildren();
                future = executor.submit(getSizeHash);
                SetupUI(currentPath);
            }
        }
        
        if (name.equals("Delete") && !keyPressed) {

          CollisionResults results = new CollisionResults();
          Ray ray = new Ray(cam.getLocation(), cam.getDirection());
          shootables.collideWith(ray, results);

          if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            String closestName = closest.getGeometry().getName();
            String hit = closest.getGeometry().getName();
            Path hitPath = fileHash.get(Paths.get(hit));
            Vector3f collisionLocation = closest.getGeometry().getLocalTranslation();
            BlowShitUp(closestName,collisionLocation);
          }
        }
        
      }
    };
    
    /**
     * JMonkey built in loop that allows us to update the screen
     * 
     * @param tpf - not sure what this does
     *
     */
    @Override
    public void simpleUpdate(float tpf) {
        
      //Always default to not repainting the boxes
      timeToUpdate = false;
     

      //Check if our background process is done, if it is, set update flag
      if(future != null){
          //Get the waylist when its done
        if(future.isDone()){
          timeToUpdate = true;
          future = null;
        }
        else if(future.isCancelled()){
          //Set future to null. Maybe we succeed next time...
          future = null;
        }
      }
      
      //If we're positive it's time to update, we can readd all the boxes with
      //their correct size.
      if(timeToUpdate && (sizeHash != null)){
        for (Map.Entry<Path,Path> file : fileHash.entrySet()) {
          String filename = file.getKey().toString();
          float normalizedSize = sizeHash.get(file.getValue());
          Geometry oldBox = (Geometry) shootables.getChild(filename);
          Vector3f location = oldBox.getLocalTranslation();
          shootables.detachChildNamed(filename);
          MakeABox(filename,location.x,normalizedSize,location.z);         
        }
      }
      

    }
    
    
   /**
    * Honestly not quite sure how to explain this function definition. But it 
    * allows me to call the getSizeHash as a bg job. I'm also certain that
    * I shouldn't be returning the sizeHash, since it's a global.
    * 
    * @param
    *
    */
    private Callable<Map<Path,Float>> getSizeHash = new Callable<Map<Path,Float>>(){
        public Map<Path,Float> call() throws Exception {

            
            fileHash = fb.GetFileNames(currentPath);
            sizeHash = fb.getSizeList(fileHash);
            sizeHash = fb.normalize(sizeHash);

            return sizeHash;
        }
    };
    
    /**
     * Overrides the destroy call to make sure that bg jobs get killed too when
     * the program is closed.
     * 
     *
     *
     */
    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown();
    }
}
