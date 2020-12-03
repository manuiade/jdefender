
package mygame.AppStates;

import mygame.AppStates.PlayerControlAppState;
import mygame.AppStates.WorldAppState;
import mygame.AppStates.GamePlayAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import mygame.Explosion;


/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */


//Classe TowerControlAppState, sottoclasse di AbstactAppState, per la creazione e il controllo delle torri nemiche
public class TowerControlAppState extends AbstractAppState implements PhysicsCollisionListener {

    
    //Parametri di gioco
    private SimpleApplication app;
    private Node rootNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private BulletAppState bulletAppState;
    
    //Oggetti di gioco
    private GamePlayAppState gamePlayAppState;
    private WorldAppState worldAppState;
    private BulletControlAppState bulletControlAppState;
    private PlayerControlAppState playerControlAppState;
    
    
    //Oggetto fisico torre e materiale
    private Spatial towers;
    private Material towerMat;
    
    //Numero torri da caricare
    private int N_TOWER;
    
    //Lista posizioni in cui piazzare le torri
    private List<Vector3f> towerPos = new ArrayList<Vector3f>();
    
    //Lista in cui piazzare le esplosioni
    private List<Explosion> explosion = new ArrayList<Explosion>();  
    
    //Oggetto per gestire la fisica della torre
    private RigidBodyControl towerControl;
    
    //Timer per il calcolo del cooldown di una torre
    private float towerTimer=-1;
    
    //Flag per il controllo delle collisioni
    private boolean collide=false;

    //Vettori posizione e direzione di una torre
    private Vector3f ballPosition;
    private Vector3f ballDirection;
    
    //Lista audio torre distrutta
    private List<AudioNode> towerDeath = new ArrayList<>();
    
    //Array flag per i suoni delle torri già distrutte
    private boolean[] played;        
    
    
    
    //Costruttore
    public TowerControlAppState(){
        super();
    }
    
    
    
    //Metodo per l'inizializzazione dei parametri dello spazio di gioco e creazione delle torri nemiche
    @Override
    public void initialize(AppStateManager stateManager,Application app){
        
        super.initialize(stateManager, app);
        
        //Parametri di gioco
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = stateManager;
        
        
    
        bulletAppState = stateManager.getState(BulletAppState.class);
        gamePlayAppState = stateManager.getState(GamePlayAppState.class);
        worldAppState = stateManager.getState(WorldAppState.class);
        playerControlAppState = stateManager.getState(PlayerControlAppState.class);
        bulletControlAppState = stateManager.getState(BulletControlAppState.class);

        
        //Numero di torri da caricare per il livello corrente
        N_TOWER = gamePlayAppState.getTowerNumber();
      
        //Inizializzazione vettori posizione e direzione
        ballPosition = new Vector3f();
        ballDirection = new Vector3f();
        
        
        //Inizializzazione posizioni delle torri nemiche
        towerMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
        towerMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Tower/tower.png"));
        towerPos.add(new Vector3f(-150,50,3));
        towerPos.add(new Vector3f(130,50,-120));
        towerPos.add(new Vector3f(280,50,-40));
        towerPos.add(new Vector3f(170,50,-285));
        towerPos.add(new Vector3f(-130,50,-115));
        towerPos.add(new Vector3f(100,50,270));
        towerPos.add(new Vector3f(-30,50,-300));
       
        
        //Creazione torri nemiche
        initTowers();
        
        //Aggiunta collisionListener
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
      
        
        //Creazione suoni torri distrutte
        AudioNode towerDeath1 = new AudioNode(assetManager, "Sounds/colpito.wav", AudioData.DataType.Buffer);
        towerDeath1.setPositional(false);
        towerDeath1.setLooping(false);
        towerDeath1.setVolume(0.7f);

        AudioNode towerDeath2 = new AudioNode(assetManager, "Sounds/opsscusa.wav", AudioData.DataType.Buffer);
        towerDeath2.setPositional(false);
        towerDeath2.setLooping(false);
        towerDeath2.setVolume(0.7f);     
        
        //Aggiunta suoni allla lista di suoni
        towerDeath.add(towerDeath1);
        towerDeath.add(towerDeath2);
        
        
        //Flag per il controllo suoni delle torri già distrutte
        played = new boolean[N_TOWER];
        
        for(int i = 0; i < N_TOWER; i++) {
            played[i] = false;
        }
        
    }
    
    
    //Metodo per la creazione delle torri
    public void initTowers(){   
        
        for(int i=0; i < N_TOWER;++i){
            
            //Creazione oggetto fisico torre
             Spatial tower_obj = makeTower(i);
             
             //Posizionamento torre
             Vector3f towerLoc = towerPos.get(i);
             towerLoc.setY(worldAppState.coord().getY());
             tower_obj.setLocalTranslation(towerLoc);  
             
             //Aggiunta controlli fisici per la torre
             towerControl = new RigidBodyControl(0f);
             tower_obj.addControl(towerControl);
             
             //Nodo esplosione per la torre in caso venga distrutta
             explosion.add(new Explosion(this.stateManager, this.app,i));
             explosion.get(i).explosionNode.setLocalTranslation(towerLoc.add(0,80,0));
             
             //Collegamento torri allo spazio di gioco
             rootNode.attachChild(tower_obj);
             rootNode.attachChild(explosion.get(i).explosionNode);
             
             //Aggiunta fisica delle torri allo spazio di gioco
             bulletAppState.getPhysicsSpace().add(towerControl);  
             
        }
        
    }
    
    
    //Metodo per la creazione fisica dell'oggetto torre
    public Spatial makeTower(int index) {
        
        Spatial tower_obj = assetManager.loadModel("Models/Tower/tower.j3o");
        tower_obj.setName("tower-"+ index);
        tower_obj.setMaterial(towerMat);
        tower_obj.scale(0.003f);
        
        //Impostazione parametri della torre
        tower_obj.setUserData("ShootCoolDown", (float) (index+1));
        tower_obj.setUserData("Alive",true);
        return tower_obj;  
        
    }
    
    
    //Metodo di aggiornamento dello stato delle torri e dei loro colpi
    @Override
    
    public void update(float tpf){
        towerTimer+=tpf/3;
        for(int i=0; i < N_TOWER;++i){
            
            //Controlla se una torre è pronta a sparare
            if(this.getCoolDown(i)< towerTimer && this.getStatus(i) && !playerControlAppState.getHit()){
                
                //La torre i-esima spara un colpo e reimposta il suo cooldown
                this.setCoolDown(i, (float)N_TOWER + 3);
                bulletControlAppState.towerShot(i, rootNode.getChild("tower-"+i).getLocalTranslation().add(0, 100, 0));
                
            }      
            
            //Controlo stato di una torre
            explosionControl(i);
            
        }
        
        //Reimposta il timer per il conteggio del cooldwon
        if(towerTimer > (float)N_TOWER){
            
            for(int i=0; i < N_TOWER;++i)
                this.setCoolDown(i,(float)(i+1-(gamePlayAppState.getLevel()/10)));
            towerTimer=0;
            
        }
        
    }
    

    
    //Metodo di controllo per la collisione contro le torri
    @Override
    public void collision(PhysicsCollisionEvent event) {
        
        if(stateManager.hasState(this)){
            
          for(int i=0; i < N_TOWER;++i){
            
            collide = (event.getNodeA().getName().equals("tower-"+i) && event.getNodeB().getName().equals("ball"))
                || (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().equals("tower-"+i));
            
            
            //Controlla se una torre è stata colpita da un proiettile
            if(collide){
                
                //La torre i-esima è stata colpita e viene impostata come morta
                this.setStatus(i, false);
                int count = 0;
                
                for(int j = 0; j < N_TOWER; j++) {                   
                    if(getStatus(j) == false) //quindi la torre j-esima è morta
                        count++;
                }
                
                //Riproduce il suono della distruzione di una torre
                if(count != N_TOWER && played[i] == false && stateManager.getState(StartScreenAppState.class).isAudioOn()) {
                    
                    int rand = (int)(Math.random() * 100) % towerDeath.size() ;
                    towerDeath.get(rand).play();
                    played[i] = true;
                    
                }
            }
            
			collide = false;
                        
        }
          
        }
        
    }
    
    
    //Metodo getter, per restiruire il cooldown della torre i-esima
    public float getCoolDown(int index){
        return rootNode.getChild("tower-"+index).getUserData("ShootCoolDown");
    }
    
    
    //Metodo setter, per impostare il cooldown della torre i-esima
    public void setCoolDown(int index,float value){
        rootNode.getChild("tower-"+index).setUserData("ShootCoolDown",value);
    }
    
    
    //Metodo getter, per restiruire lo stato della torre i-esima
    public boolean getStatus(int index){
        return rootNode.getChild("tower-"+index).getUserData("Alive");
    }
    
    
    //Metodo setter, per impostare lo stato della torre i-esima
    public void setStatus(int index, boolean value){
        rootNode.getChild("tower-"+index).setUserData("Alive",value);
    }
 
    
    //Metodo di controllo stato di una torre
    public void explosionControl(int i){
        
           if(getStatus(i)==false)
             //Esplosione di una torre
             explosion.get(i).towerExplode();
        
    }
    
    
    //Metodo di cleanup, per ripulire il vecchio spazio di gioco
    @Override
    public void cleanup(){
    
        super.cleanup();     
    }
    
    
    //Aggancio della classe proiettili allo spazio di gioco
    public void attachBullContrAppState() {
        bulletControlAppState = stateManager.getState(BulletControlAppState.class);
    }
    
    
    //Disabilita il suono per una torre già distrutta
    public void inibitTowerExplosionAudio() {
        
        for(int i= 0; i < N_TOWER; i++) {
            played[i] = true;
        }
        
    }
    
}
