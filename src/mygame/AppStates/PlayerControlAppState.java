
package mygame.AppStates;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Listener;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import mygame.Explosion;

/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */


//Classe PlayerControlAppState, sottoclasse di AbstractAppState, usata per creare e gestire il giocatore
public class PlayerControlAppState extends AbstractAppState implements PhysicsCollisionListener {
    
    //Variabili a cui assegnare lo stato del gioco
    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private InputManager inputManager;  
    private AppStateManager stateManager;
    private Listener listener;
    private CameraNode camNode;
    private ViewPort viewPort;
    
    //Permette di controllare la fisica del giocatore
    private BulletAppState bulletAppState;
    
    //Oggetti di gioco
    private CameraControlAppState cameraControlAppState;
    private TowerControlAppState towerControlAppState;
    private Explosion explosion;
    
    //Nodo giocatore
    private Node playerNode;
    
    //Modello giocatore
    private Spatial playerSpatial;
    
    //Permette di controllare la fisica dei proiettili
    private BulletControlAppState bulletControlAppState;
    
    //Entità fisica del giocatore
    private RigidBodyControl playerControl;
    
    //Flag per il controllo dell'input di sparo
    private boolean fire;
    
    //Flag per il controllo delle collisioni
    private boolean collide;
    
    //Flag per il controllo della morte del giocatore
    private boolean dead = false;
    
    //Informazioni sul giocatore
    public static final String PLAYERNAME = "player";
    public static final String VEHICLE = "vehicle";
    

    //Stato esplosione del giocatore
    private int state=0;
    
    //Timer stato esplosione
    private float explosionTimer=0;
    
    //Cooldown tra ogni sparo
    private float shootTimer=5;
    
    //Flag per il controllo del prossimo sparo
    private boolean shotReady;
    
    //Posizione del giocatore
    private Vector3f position;
    
    //Nodo audio sparo
    private AudioNode shootAudio;
    
    //Nodo audio jet
    private AudioNode jetAudio;
    
    //Nodo audio esplosione
    private AudioNode explosionAudio;
    
    //Flag controllo collisione del giocatore
    boolean hit = false;
    
    //Flag controllo audio esplosione
    private boolean audioExpPlayed = false;

    //Mappatura pulsante di sparo
    private static final String MAPPING_SHOT = "Shot";
    private static final Trigger TRIGGER_SHOT = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    
    
    //Controllo input di sparo
    private ActionListener actionListener = new ActionListener() {
        
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            fire = isPressed && name.equals(MAPPING_SHOT);
        }
        
    };
    
    
    //Costruttore
    public PlayerControlAppState(){
        super();
    }
    
    
    //Metodo per l'inizializzazione dei parametri dello spazio di gioco e creazione del giocatore
    @Override
    public void initialize(AppStateManager stateManager,Application app){
        
        super.initialize(stateManager, app);
        
        //Inizializzazione parametri di gioco
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.stateManager = stateManager;
        this.listener = this.app.getListener();
        this.viewPort = app.getViewPort();
        
        
        bulletAppState = stateManager.getState(BulletAppState.class);
        towerControlAppState = stateManager.getState(TowerControlAppState.class);
        bulletControlAppState = stateManager.getState(BulletControlAppState.class);
        
        
        
        //Creazione giocatore
        buildPlayer();
        
        //Inizializzazione esplosione giocatore
        explosion = new Explosion(this.stateManager,this.app);
        
        //Collegamento nodo esplosione al giocatore
        playerNode.attachChild(explosion.explosionNode);
        
        
        //Aggiunta collisionListener
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
      
        //Inizializzazione controllo telecamera
        cameraControlAppState = new CameraControlAppState();
        stateManager.attach(cameraControlAppState);
        

        
        //Trigger mouse-sparo
        inputManager.addMapping(MAPPING_SHOT, TRIGGER_SHOT);
        inputManager.addListener(actionListener, new String[] {MAPPING_SHOT});
        
        
        //Inizializzazione audio sparo
        shootAudio = new AudioNode(assetManager, "Sounds/shot.wav", AudioData.DataType.Buffer);
        shootAudio.setPositional(false);
        shootAudio.setLooping(false);
        shootAudio.setVolume(0.5f);
        
        //Inizializzazione audio jet
        jetAudio = new AudioNode(assetManager, "Sounds/motoreJet.wav", AudioData.DataType.Buffer);
        jetAudio.setPositional(false);
        jetAudio.setLooping(true);
        jetAudio.setVolume(0.1f);
        
        if (stateManager.getState(StartScreenAppState.class).isAudioOn()) {
            jetAudio.play();
        }
            
        //Inizializzazione audio esplosione
        explosionAudio = new AudioNode(assetManager, "Sounds/esplosione.wav", AudioData.DataType.Buffer);
        explosionAudio.setPositional(false);
        explosionAudio.setLooping(false);
        explosionAudio.setVolume(0.3f);
        
        

        
    }

    
    //Metodo utilizzato per la creazione del giocatore nello spazio di gioco
    public void buildPlayer(){
        
        //Inizializzazione e posizionamento nodo giocatore
        playerNode = new Node(PLAYERNAME);
        playerNode.setLocalTranslation(Vector3f.ZERO.add(0,50,0));
        rootNode.attachChild(playerNode);
        
        //Inizializzazione e posizionamento camera
        camNode = new CameraNode("camNode",cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0,1,-8)); //-------------------------------QUESTO VALORE DIPENDE DAL MODELLO
        Quaternion quat = new Quaternion();
        quat.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
        camNode.setLocalRotation(quat);
        playerNode.attachChild(camNode);
        camNode.setEnabled(true);
        
        //Inizializzazione fisica giocatore
        playerControl = new RigidBodyControl(10f);
        
        //Creazione modello giocatore
        playerSpatial = assetManager.loadModel("Models/Player/dark_fighter_6.j3o");
        playerSpatial.setName(VEHICLE);
        playerSpatial.scale(0.06f);
        Material mat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Player/dark_fighter_6_color.png")); 
        mat.setTexture("NormalMap", assetManager.loadTexture("Textures/Player/dark_fighter_6_bump.gif"));
        mat.setTexture("GlowMap", assetManager.loadTexture("Textures/Player/dark_fighter_6_illumination.png"));
        mat.setTexture("SpecularMap", assetManager.loadTexture("Textures/Player/dark_fighter_6_specular.png")); 
        playerSpatial.setMaterial(mat);
        
        //Collegamento fisica e modello al nodo del giocatore
        playerNode.attachChild(playerSpatial);
        playerNode.addControl(playerControl);
        
        //Impostazione parametri del giocatore
        playerNode.setUserData("ChargeTimer", 0f);
        playerNode.setUserData("ChargeReady", false);
        playerNode.setUserData("Hit", false);
        playerNode.setUserData("Alive", true);
        
        //Aggiunta fisica del giocatore al bulletAppState
        bulletAppState.getPhysicsSpace().add(playerControl);
        
    
        
    }
    
    

    //Metodo di update, per impostare lo spostamento del giocatore
    @Override
    public void update(float tpf){
        
        position = playerNode.getLocalTranslation();
        
        //Abilita/disabilita il suono del jet
        if(stateManager.getState(StartScreenAppState.class).isAudioOn()) {
            jetAudio.play();
        } 
        
        else {
            jetAudio.pause();
        }
       
        //Aggiorna il cooldown per lo sparo da parte del giocatore
        shootTimer = this.getChargeTimer();
        if(shootTimer<5){
            
            shootTimer +=tpf;
            this.setChargeTimer(shootTimer);
            
        }
        
        else
            this.setChargeReady(true);
              
        
        //Controlla se è possibile sparare
        if(fire && this.getChargeReady() && !hit  ){
            
            //Il giocatore spara un proiettile
            bulletControlAppState.playerShot();
            
            //Riproduzione audio sparo
            if(stateManager.getState(StartScreenAppState.class).isAudioOn()) {
                shootAudio.playInstance();
            }
            
            //Azzeramento cooldown per lo sparo successivo
            this.setChargeTimer(0);
            this.setChargeReady(false);
            
        }
   
        //Controlla se il giocatore è stato colpito
        if(hit)            
            this.setHit(true);
        
        if(getHit()){
            
            if(stateManager.getState(StartScreenAppState.class).isAudioOn()) {
                
                //Interrompe il suono del jet
                jetAudio.stop();
                towerControlAppState.inibitTowerExplosionAudio();
                
            }
            
            //Abilita il suono dell'esplosione
            if(!audioExpPlayed && stateManager.getState(StartScreenAppState.class).isAudioOn()) {                   
                explosionAudio.play();   
            }
            audioExpPlayed = true;
            
            
            
            //Attivazione esplosione dell'aereo in base al tempo trascorso dalla collisione
            explosionTimer+=tpf;
   
            //Disabilita il modello del giocatore in base al tempo trascorso dall'esplosione
            state = explosion.explosion(explosionTimer);
            
            if(state==1){
                playerNode.detachChild(playerSpatial);            
            }
            
            if(state==2){
                //Giocatore sconfitto
                setAlive(false);
            }
            
            state=0;  
                
        }
        
              
    }

    
    
     //Metodo per il controllo delle collisioni tra il giocatore e lo spazio di gioco
    @Override
    public void collision(PhysicsCollisionEvent event) {
        
        collide = (event.getNodeA().getName().equals(PLAYERNAME) && event.getNodeB().getName().equals("cityNode"))
                || (event.getNodeA().getName().equals("cityNode") && event.getNodeB().getName().equals(PLAYERNAME)) ||
                (event.getNodeA().getName().equals(PLAYERNAME) && event.getNodeB().getName().matches("tower-."))
                || (event.getNodeA().getName().matches("tower-.") && event.getNodeB().getName().equals(PLAYERNAME)) ||
                (event.getNodeA().getName().equals(PLAYERNAME) && event.getNodeB().getName().equals("ball"))
                || (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().equals(PLAYERNAME));
        
        
        //In caso di collisione il flag di giocatore colpito viene impostato a true
        if(collide){
            
            hit = true;
                /*
            
            ---------------------------------------------------------------------------------------------------------
                Se si ripresenta il problema del nullPointer exception togliere il commento a queta parte di codice
            ----------------------------------------------------------------------------------------------------------
                for(Spatial i:rootNode.getChildren()) {
                    if(i.getName().equals("ball")) {
                        i.removeControl(BulletControl.class);
                    }
                }
            
               */
                
            }
  
    }

    
    
    //Metodo getter che restituisce la camera
    public Camera getCamera() {
        return cam;
    }
    
    
    //Metodo getter che restituisce il nodo camera
    public CameraNode getCamNode() {
        return camNode;
    }
      
      
    //Metodo getter che restituisce il nodo del giocatore
    public Node getPlayerNode() {
        return playerNode;
    }
    
    
    //Metodo getter che restituisce la posizione corrente del nodo del giocatore
    public Vector3f getPosition(){
        return playerNode.getLocalTranslation();
    }
    
    
    //Metodo getter che restituisce la posizione globale del giocatore
    public Vector3f getVehiclePosition() {
        return playerNode.getChild(VEHICLE).getWorldTranslation();
    }
    
    
    //Metodo getter che restituisce il cooldown per il prossimo sparo
    public float getChargeTimer(){
        return playerNode.getUserData("ChargeTimer");
    }
    
    
    //Metodo setter che imposta il cooldown per il prossimo sparo
    public void setChargeTimer(float value){
        playerNode.setUserData("ChargeTimer", value);
    }
    
    
    //Metodo getter che indica se è possibile sparare
    public boolean getChargeReady(){
        return playerNode.getUserData("ChargeReady");
    }
    
    
    //Metodo setter che imposta il flag per lo sparo
    public void setChargeReady(boolean value){
        playerNode.setUserData("ChargeReady", value);
    }
    
    
    //Metodo getter che indica se il giocatore è stato colpito
    public boolean getHit(){
        return playerNode.getUserData("Hit");
    }
    
    
    //Metodo setter che imposta lo stato colpito/non-colpito del giocatore
    public void setHit(boolean value){
        playerNode.setUserData("Hit", value);
    }
    
    
    //Metodo getter che indica se il giocatore è ancora vivo
    public boolean getAlive(){
        return playerNode.getUserData("Alive");
    }
    
    
    //Metodo setter che imposta lo stato vivo/morto del giocatore
    public void setAlive(boolean value){
        playerNode.setUserData("Alive", value);
    }
    
    

    //Metodo per fermare il suono del jet
    public void stopJetAudio() {
        jetAudio.stop();
    }
    
    
    //Metodo che abilita il suono dell'esplosione
    public void inibitorExplosionAudio() {
        audioExpPlayed = true;        
    }
    
    
    //Metodo di cleanup, per ripulire il vecchio spazio di gioco
    @Override
    public void cleanup(){
       
        super.cleanup();
        
    }
}
