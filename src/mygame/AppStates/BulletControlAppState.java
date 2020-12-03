
package mygame.AppStates;

/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;
import mygame.Explosion;


//Classe BulletControlAppState, sottoclasse di AbstractAppState, utilizzata per realizzare e gestire i proiettili del giocatore e delle torri
public class BulletControlAppState extends AbstractAppState implements PhysicsCollisionListener{
    
    //Variabili a cui assegnare lo stato del gioco
    private SimpleApplication app;
    private Node rootNode;
    private CameraNode camNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    
    //Permette di controllare la fisica dei proiettili
    private BulletAppState bulletAppState;
    
    //Geometria usata per creare i proiettili
    private Geometry ballGeo;
    
    //Vettore per controllare la posizione in cui creare il proiettile
    private Vector3f position;
    
    //Classe giocatore, per il controllo della sua posizione
    private PlayerControlAppState playerControlAppState;
    
    //Flag per il controllo di una collisione
    private boolean collide;
    
    //Lista di oggetti di tipo esplosione
    private List<Explosion> explosion;
    
    //Lista posizioni per prevenire sovrapposizioni di esplosioni
    private List<Vector3f> prevPos = new ArrayList<>();
    
    //Conteggio esplosioni
    private int collisionNumber;
    
    //Variabile per salvare il tempo che intercorre tra 2 frame
    private float tpf;
    
    //Variabile che registra la posizione della collisione corrente
    private Vector3f ballPos = new Vector3f(0,0,0);
    
    //Flag utilizzato per non sovrapporre le esplosioni
    private boolean ballExplosion;
    
    //Flag che verifica la collisione di un proiettile
    private boolean ballHit=false;
    
    
    //Assegnazione dei parametri dello spazio di gioco agli attributi della classe corrente
    @Override
    public void initialize(AppStateManager stateManager,Application app){
        
        super.initialize(stateManager, app);
        
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = stateManager;
    
        bulletAppState = stateManager.getState(BulletAppState.class);
        playerControlAppState = stateManager.getState(PlayerControlAppState.class);
        
        //Aggiunta collisionListener al bulletAppState
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        
        prevPos.add(Vector3f.ZERO);
        explosion = new ArrayList<>();
        position = new Vector3f();
        collide = false;
        collisionNumber=0;
        
    }

    
    //Metodo per la creazione del proiettile sparato da una torre
      public void towerShot(int index, Vector3f ballPosition){
        
        //Inizializzazione proiettile
        position = ballPosition;
        Sphere ballMesh = new Sphere(32,32,2f,true,false);
        ballGeo = new Geometry("ball",ballMesh);
        Material ballMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md"); 
        ballGeo.setMaterial(ballMat);
        ballGeo.setLocalTranslation(position);
        
        //Regolazione fisica del proiettile
        RigidBodyControl ballPhy = new RigidBodyControl(0.001f);
        ballGeo.addControl(ballPhy);
        bulletAppState.getPhysicsSpace().add(ballPhy);
        ballPhy.setCcdSweptSphereRadius(.1f);
        ballPhy.setCcdMotionThreshold(0.001f);       
        ballPhy.setFriction(10f);      
        
        //Aggiunta controllo di inseguimento
        ballGeo.addControl(new BulletControl(ballPhy, playerControlAppState)); 
        
        //Collegamento del proiettile al nodo principale
        rootNode.attachChild(ballGeo);
        
    }
    
      
    //Metodo per la creazione del proiettile sparato dal giocatore
    public void playerShot(){
        
        //Inizializzazione proiettile
        this.camNode = playerControlAppState.getCamNode();
        Sphere ballMesh = new Sphere(32,32,.5f,true,false);
        ballGeo = new Geometry("ball",ballMesh);     
        Material ballMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md"); 
        ballGeo.setMaterial(ballMat);
        ballGeo.setLocalTranslation(playerControlAppState.getVehiclePosition().add(camNode.getCamera().getDirection().mult(5)));
        
        //Regolazione fisica del proiettile
        RigidBodyControl ballPhy = new RigidBodyControl(.001f);
        ballGeo.addControl(ballPhy);
        bulletAppState.getPhysicsSpace().add(ballPhy);
        ballPhy.setCcdSweptSphereRadius(.1f);
        ballPhy.setCcdMotionThreshold(0.001f);
        ballPhy.setLinearVelocity(camNode.getCamera().getDirection().mult(300));
        ballPhy.setFriction(10f);
        
        //Collegamento del proiettile al nodo principale
        rootNode.attachChild(ballGeo);
        
    }
   
    
    //Metodo per il controllo delle collisioni tra un proiettile e lo spazio di gioco
    @Override
    public void collision(PhysicsCollisionEvent event) {
        
        collide = (event.getNodeA().getName().equals(PlayerControlAppState.PLAYERNAME) && event.getNodeB().getName().equals("ball"))
                || (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().equals(PlayerControlAppState.PLAYERNAME)) ||
                (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().matches("tower-."))
                || (event.getNodeA().getName().matches("tower-.") && event.getNodeB().getName().equals("ball")) ||
                (event.getNodeA().getName().equals("cityNode") && event.getNodeB().getName().equals("ball"))
                || (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().equals("cityNode"))
                || (event.getNodeA().getName().equals("ball") && event.getNodeB().getName().equals("ball"));
        
        //In caso di collisione viene salvata la posizione della collisione, il flag di collisione va a true e viene rimosso il proiettile
        
        if(collide){  
            
                if(event.getNodeA().getName().equals("ball")){
                    
                    ballPos = event.getNodeA().getLocalTranslation();
                    rootNode.detachChild(event.getNodeA());
                    event.getNodeA().removeFromParent();
                    ballHit = true;  
                    
                }
                   
                
                if(event.getNodeB().getName().equals("ball")){
                    
                   ballPos = event.getNodeB().getLocalTranslation();
                   rootNode.detachChild(event.getNodeB());
                   event.getNodeB().removeFromParent();
                   ballHit = true;   
                   
                }
                
        } 
        
    } 
    
    //Metodo update, lanciato ad ogni frame di gioco
    @Override
    public void update(float tpf){
        
        this.tpf = tpf;
              
        //In caso di collisione, si effettua un controllo per evitare la sovrapposizione delle esplosioni (ottimizzazione codice)
        ballExplosion = true;
        if(ballHit){
            
            for(Vector3f tmp : prevPos){
                if(tmp == ballPos)
                    ballExplosion = false;
                
            }
            if(ballExplosion || prevPos.size() == 1){
                
                //Si aggiunge una nuova esplosione alla lista delle esplosioni che si attacca alla posizione della collisione
                explosion.add(new Explosion(stateManager, app));
                explosion.get(collisionNumber).explosionNode.setLocalTranslation(ballPos);                  
                rootNode.attachChild(explosion.get(collisionNumber).explosionNode);
                
                collisionNumber++;
                
                //Lista delle posizioni per il controllo delle sovrapposizioni
                prevPos.add(ballPos);
                
            }
                        
            ballHit = false;
            
        }
        
        //Controllo dello stato dell'esplosione 
        explosionControl(tpf);
    }
    
    //Metodo getter per attivare un'esplosione
    public boolean getCollision(){
        return ballExplosion;
    }
    
    //Metodo getter per misurare il gap tra due frame
    public float getTpf(){
        return this.tpf;        
    }
    

    //Metodo che gestisce le esplosioni dei vari proiettili
    public void explosionControl(float tpf){
        
        float tmpTimer;
        
        for (Explosion tmp : explosion) {
            
            tmpTimer = tmp.getTimerBullet();
            if(tmpTimer > 2.0f){
                
                rootNode.detachChild(tmp.explosionNode);
                //System.out.println(tmp.timer);
                tmp.setTimerBullet(-1);
                
            }
            if(tmpTimer >=0 && tmpTimer <=2.0f){
                
                tmp.setTimerBullet(tmpTimer + tpf);
                tmpTimer = tmp.getTimerBullet();
                tmp.explosion(tmpTimer);       
                
            }
            
        }
        
    }
    
    
    //Metodo di cleanup, per ripulire il vecchio spazio di gioco
    @Override
    public void cleanup(){ 
        super.cleanup();     
    }
}
