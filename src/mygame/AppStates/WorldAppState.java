
package mygame.AppStates;

import mygame.AppStates.GamePlayAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;


/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */



//Classe WorldAppState, sottoclasse di AbstractAppState per la creazione della mappa di gioco e per l'inizializzazione delle partite
public class WorldAppState extends AbstractAppState implements PhysicsCollisionListener{
    
    
    //Parametri di gioco
    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private InputManager inputManager;
    private BulletAppState bulletAppState;
    private ViewPort viewPort;
    private AppStateManager stateManager;
    
    
    //Oggetti di gioco
    private GamePlayAppState gamePlayAppState; 
    private StartScreenAppState startScreenAppState;
    private PlayerControlAppState playerControlAppState;
    private BulletControlAppState bulletControlAppState;
    private TowerControlAppState towerControlAppState;
    
    
    //Filtri per l'acqua
    private FilterPostProcessor fpp;
    private WaterFilter waterFilter;
    
    //Luci
    private AmbientLight ambient;
    private DirectionalLight sun;
    
    //Oggetto per il controllo della fisica della mappa
    private RigidBodyControl scenePhy;
    
    
    //Nodo spazio di gioco
    private Node cityNode;
    

    //Oggetto fisico mappa di gioco
    private Node sceneNode;
    private Material cityMaterial;
    private Spatial citySpatial;
    
    //Nome mappa di gioco
    private static final String CITYNAME = "map";
    
    //Flag per il controllo delle collisioni
    private boolean collide = false;
    
    //Livello corrente
    private int gameLevel=1;
    
    //Timer livello
    protected float timer;

    
    //Timer per il controllo dei tempi di caricamento
    private long loadTime;
    private long waitTime;
    private long currentTime;
     
   
    
    //Enumerator contenente gli stati di gioco
    private enum LoadState {
        LOADING, LOADED, IDLE, INITIALIZE, WIN, LOSE, TIME;
    }
    
    //Inizializzazione enumerator
    private LoadState gameStateLoading = LoadState.INITIALIZE;
    
    
    //Costruttore
    public WorldAppState(){
        super();
    }
    
    
    
    //Inizializzazione dei parametri di gioco e della mappa di gioco
    @Override
    public void initialize(AppStateManager stateManager, Application app){
       
        super.initialize(stateManager, app);
        
        //Parametri di gioco
        this.stateManager = stateManager;
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.viewPort = this.app.getViewPort();
        
        //Creazione fisica dello spazio di gioco
        scenePhy = new RigidBodyControl(0f);
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
       
        //Oggetti di gioco
        gamePlayAppState = stateManager.getState(GamePlayAppState.class);
        startScreenAppState = stateManager.getState(StartScreenAppState.class);
    

        //Inizializzazione nodo mappa di gioco
        sceneNode = new Node(CITYNAME);
        
        //Aggiunta collisionListener
        bulletAppState.getPhysicsSpace().addCollisionListener(this);  
        
        //Inizializzazione filtri di luci
        initLight();
  
      
    }
    

    
    //Metodo di aggiornamento dello stato di gioco
    @Override
    public void update(float tpf){   
        
       
        currentTime = System.currentTimeMillis()/1000;
        
        if(null != gameStateLoading)switch (gameStateLoading) {
            
            //Impostazione stato di gioco WIN
            case WIN:
                startScreenAppState.setWinScreen();
                loadTime = System.currentTimeMillis()/1000 + 4;  // Tempo di attesa
                gameStateLoading = LoadState.INITIALIZE;
                break;
                
            //Impostazione stato di gioco LOSE
            case LOSE:
                startScreenAppState.setLoseScreen();
                loadTime = System.currentTimeMillis()/1000 + 4;  // Tempo di attesa
                gameStateLoading = LoadState.INITIALIZE;
                break;
                
            //Impostazione stato di gioco WIN
            case TIME:
                startScreenAppState.setTimeScreen();
                loadTime = System.currentTimeMillis()/1000 + 4;  // Tempo di attesa
                gameStateLoading = LoadState.INITIALIZE;
                break;
                
            //Inizializzazione partita
            case INITIALIZE:
                
                if(currentTime >= loadTime){
                    
                    //Schermata di caricamento
                    gameStateLoading = LoadState.LOADING;
                    startScreenAppState.showLevel(gamePlayAppState.getLevel());
                    startScreenAppState.showRecord();
                    startScreenAppState.setLoadingScreen();
                    waitTime = System.currentTimeMillis()/1000 + 4; // Tempo di attesa
                    
                    //Cancellamento vecchio stato di gioco
                    if(towerControlAppState != null)
                       stateManager.detach(towerControlAppState); 
                    
                    if(playerControlAppState != null)
                       stateManager.detach(playerControlAppState);
                    
                    if(bulletControlAppState != null)
                       stateManager.detach(bulletControlAppState);
                    
                    if(fpp!=null){
                        fpp.removeAllFilters();
                    }
                    
                }
                
                break;
                
            //Caricamento gioco
            case LOADING:
                
                if(currentTime >= waitTime) {
                    
                    //Inizializzazione mappa di gioco
                    initCity();                    
                    
                    gamePlayAppState.setTowerNumber(gamePlayAppState.getLevel()/3 +3);
                    //aggiungere limite delle torri
                    
                    
                    //Creazione classi di gioco
                    
                    playerControlAppState = new PlayerControlAppState();
                    stateManager.attach(playerControlAppState);
                    
                    towerControlAppState = new TowerControlAppState();
                    stateManager.attach(towerControlAppState); 
                    
                    
                    bulletControlAppState = new BulletControlAppState();
                    stateManager.attach(bulletControlAppState);                    
                    
                    gameStateLoading = LoadState.LOADED;
                    gamePlayAppState.startGame();
                }   
                
                break;
                
            //Stato "in gioco"
            case LOADED:
                
                //Caricamento interfaccia testuale durante il gioco
                startScreenAppState.setInGameGUI();
                gameStateLoading = LoadState.IDLE;
                setTimer(60.0f);
                break;
                
            default:
                break;
                
        }

        
    }
    
    
    
    
    //Metodo per il controllo delle collisioni sulla mappa
    
    @Override
    public void collision(PhysicsCollisionEvent event) {
        collide = (event.getNodeA().getName().equals("cityNode") && event.getNodeB().getName().equals("ball"))
                || (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().equals("cityNode"));
              
        //Toglie un proiettile dallo stato di gioco in caso di collisione sulla mappa
         if(collide){
             
              if(event.getNodeA().getName().equals("ball"))
                 rootNode.detachChild(event.getNodeA());
              
              else
                 rootNode.detachChild(event.getNodeB());
                      
            }
    }
    
    
    
    
    //Metodo di inizializzazione luci
    public void initLight(){
        
       ambient = new AmbientLight();
       rootNode.addLight(ambient);
       sun = new DirectionalLight();
       sun.setDirection(new Vector3f(0.8f, -0.7f, -1));
       sun.setColor(ColorRGBA.White.mult(1.5f));
       rootNode.addLight(sun);
       
    }
    
    
    //Metodo di inizializzazione della mappa di gioco
    public void initCity(){
        
        //Fisica della mappa di gioco
        bulletAppState.getPhysicsSpace().removeAll(rootNode);
        
        //Ulteriore pulizia vecchio spazio di gioco (per sicurezza)
        if(sceneNode != null)
          rootNode.detachAllChildren();    
        
        
        //Creazione mappa fisica
        cityMaterial = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
        cityMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/City/SciFi_HumanCity_Diffuse-Map.jpg"));        
        cityNode = new Node("cityNode");    
        citySpatial = assetManager.loadModel("Models/City/SciFi_HumanCity_Kit05-OBJ.j3o");
        citySpatial.setMaterial(cityMaterial);
        citySpatial.scale(4f);
        citySpatial.setLocalTranslation(Vector3f.ZERO.add(0,50,0));
        cityNode.attachChild(citySpatial);
        
        //Aggiunta fisica alla mappa
        cityNode.addControl(scenePhy);
        bulletAppState.getPhysicsSpace().add(scenePhy);       
        sceneNode.attachChild(cityNode);
        
        //Texture dello sfondo 2D
        Texture west = assetManager.loadTexture("Textures/Lagoon/lagoon_west.jpg");
        Texture east = assetManager.loadTexture("Textures/Lagoon/lagoon_east.jpg");
        Texture north = assetManager.loadTexture("Textures/Lagoon/lagoon_north.jpg");
        Texture south = assetManager.loadTexture("Textures/Lagoon/lagoon_south.jpg");
        Texture up = assetManager.loadTexture("Textures/Lagoon/lagoon_up.jpg");
        Texture down = assetManager.loadTexture("Textures/Lagoon/lagoon_down.jpg");
        
     
        //Aggiunta sfondo allo spazio di gioco
        sceneNode.attachChild(SkyFactory.createSky(assetManager, west, east, north, south, up, down));       
        rootNode.attachChild(sceneNode);
        
        
        //Filtro acqua
        fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        waterFilter = new WaterFilter(sceneNode, new Vector3f(0.8f,-0.7f,-1));
        fpp.addFilter(waterFilter);
        
    }
    
    
    
    //Metodo getter, per le coordinate della mappa di gioco
    public Vector3f coord(){
        return citySpatial.getLocalTranslation();
    }

    
    
    //Metodo pulizia spazio di gioco, in caso di chiusura dell'applicazione
    @Override
    public void stateDetached(AppStateManager stateManager){
        
        bulletAppState.getPhysicsSpace().removeAll(rootNode);
        rootNode.detachAllChildren();
        stateManager.detach(gamePlayAppState);
        stateManager.detach(playerControlAppState);
        stateManager.detach(towerControlAppState);
        stateManager.detach(bulletAppState);
        rootNode.removeLight(ambient);
        rootNode.removeLight(sun);
        
    }
    
    
    //Metodo per il controllo delle torri in vita
    public int getTowerAlive(){
        
        if(gameStateLoading == LoadState.IDLE){
        int count=0;
        
        for(int i=0; i < gamePlayAppState.getTowerNumber(); ++i){
            if(towerControlAppState.getStatus(i))
                count ++;
        }
        
        return count;
        }
        
        else
            return 3;
        
    }
    
    
   //Metodo per il controllo dello stato del giocatore
   public boolean getPlayerAlive(){
       
       if(gameStateLoading == LoadState.IDLE)
          return playerControlAppState.getAlive();
       else
          return true;
       
   }
   
   
   //Metodo di levelUp
   public void levelUp(int level){
       gameStateLoading = LoadState.WIN;
   }
   
   
   //Metodo di levelDown in caso di morte del giocatore
   public void levelDown1(int level){
       gameStateLoading = LoadState.LOSE;
   }
   
   
   //Metodo di levelDown in caso di tempo scaduto
   public void levelDown2(int level){
       gameStateLoading = LoadState.TIME;
   }
   
   
   //Metodo getter, per il tempo rimasto
   public float getTimer(){
       if(gameStateLoading == LoadState.IDLE)
          return timer;
       else
          return 1;
   }
   
   
   //Metodo setter, per regolare il timer
   public void setTimer(float value){
       timer = value;
   }
   
   
    
}
