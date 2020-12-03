
package mygame.AppStates;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */

//Classe CameraControAppState, sottoclasse di AbstractAppState per il controllo della telecamera
public class CameraControlAppState extends AbstractAppState {
    
    //Variabili a cui assegnare lo stato del gioco
    private Camera cam;
    private InputManager inputManager;
    private Node camNode;
    private Node playerNode;
    private RigidBodyControl playerPhy;
    private Application app;
    
    //Classe giocatore, per controllare il suo spostamento
    private PlayerControlAppState playerControlAppState;
    private Spatial playerSpatial;
    
    //Vettori per regolare lo spostamento del giocatore
    private Vector3f followPoint;
    private Vector3f fpDir;
    private Vector2f center;
    private Vector3f speed;
    private Vector3f tmp;
   
    //Velocità di movimento
    private final float SPEED = 60f;
    
    //Limiti di spostamento lungo la mappa di gioco
    private final float LIMXZ = 470f;
    private final float LIMY = 700f;
    private final float LIMYD = -30f;
      
    
    //Assegnazione dei parametri dello spazio di gioco agli attributi della classe corrente e inizializzazione parametri della telecamera
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        
        super.initialize(stateManager, app);
        
        this.app = app;
        this.inputManager = app.getInputManager();
        this.cam = app.getCamera();
        
        this.playerNode = stateManager.getState(PlayerControlAppState.class).getPlayerNode();
        this.camNode = (Node)playerNode.getChild("camNode");      
        this.playerControlAppState = stateManager.getState(PlayerControlAppState.class);
               
        followPoint = new Vector3f(0f, 0f, 1f);
        
        this.playerPhy = playerNode.getControl(RigidBodyControl.class);
        this.playerSpatial = playerNode.getChild(PlayerControlAppState.VEHICLE);
        this.center = new Vector2f(app.getContext().getSettings().getWidth()/2,
                                   app.getContext().getSettings().getHeight()/2);
        
        
    } 

    
    //Metodo di update per l'aggiornamento della posizione e direzione della telecamera
    @Override
    public void update(float tpf) {
        
        //Ferma la telecamera se il giocatore viene colpito
        if(playerControlAppState.getHit()) {
            
            playerPhy.setLinearVelocity(Vector3f.ZERO);
            playerPhy.setAngularVelocity(Vector3f.ZERO);
            playerSpatial.setLocalRotation(playerSpatial.getLocalRotation());
            camNode.setLocalRotation(camNode.getLocalRotation());
            return;
            
        }
                    
        //Imposta la velocità di movimento del giocatore
        Vector3f pos = playerSpatial.getWorldTranslation();
        speed = cam.getDirection().mult(SPEED); 
        playerPhy.setLinearVelocity(speed);
        Vector3f tmpSpeed = speed;
        
        //Limitatore di posizione   
        // -------------- asse x -------------------
        if(pos.x >= LIMXZ) {
            
            if(pos.add(speed).x < pos.x) {
                playerPhy.setLinearVelocity(speed);
            }
            
            else {
            tmpSpeed.setX(0f);
            playerPhy.setLinearVelocity(tmpSpeed);
            }
            
        }
        
        if(pos.x <= -LIMXZ) {
            
            if(pos.add(speed).x > pos.x) {
                playerPhy.setLinearVelocity(speed);
            }
            else {
            tmpSpeed.setX(0f);
            playerPhy.setLinearVelocity(tmpSpeed);
            }
            
        }
        
        //---------------asse y-----------------------
        if(pos.y >= LIMY) {
            
            if(pos.add(speed).y < pos.y) {
                playerPhy.setLinearVelocity(speed);
            }
            else {
            tmpSpeed.setY(0f);
            playerPhy.setLinearVelocity(tmpSpeed);
            }
            
        }
        
        if(pos.y <= LIMYD) {
            
            if(pos.add(speed).y > pos.y) {
                playerPhy.setLinearVelocity(speed);
            }
            else {
            tmpSpeed.setY(0f);
            playerPhy.setLinearVelocity(tmpSpeed);
            }
            
        }
        
        //------------------asse z----------------------
        if(pos.z >= LIMXZ) {
            
            if(pos.add(speed).z < pos.z) {
                playerPhy.setLinearVelocity(speed);
            }
            else {
            tmpSpeed.setZ(0f);
            playerPhy.setLinearVelocity(tmpSpeed);
            }
            
        }
        
        if(pos.z <= -LIMXZ) {
            
            if(pos.add(speed).z > pos.z) {
                playerPhy.setLinearVelocity(speed);
            }
            else {
            tmpSpeed.setZ(0f);
            playerPhy.setLinearVelocity(tmpSpeed);
            }
            
        }
        
        
        //Salvo la posizione attuale del cursore
        Vector2f mousePos = inputManager.getCursorPosition();                                    
		
	//Rotazione rispetto alla posizione del mouse dal centro dello schermo               
        Vector3f rotCamera = new Vector3f(
                                    mousePos.x - center.x,
                                    mousePos.y - center.y, 0);
        
        rotCamera = new Vector3f(rotCamera.y * 0.0015f, -rotCamera.x* 0.0025f, 0);
        
        tmp = playerPhy.getPhysicsRotation().mult(rotCamera);
        playerPhy.setAngularVelocity(tmp);
		
       
        
       //Rotazione aereo   
       tmp = new Vector3f(rotCamera.x, 0, -rotCamera.y);       
       Quaternion q = playerSpatial.getLocalRotation().fromAngleAxis(rotCamera.length()*0.6f, tmp);
       playerSpatial.setLocalRotation(q);
       
            
       //Rotazione dela camera per rendere più realistico il movimento mediante la variabile rotCamera      
       q = camNode.getLocalRotation().fromAngleAxis(rotCamera.length()*0.2f, rotCamera);
       camNode.setLocalRotation(q);      
       
    }
        
        
    //Metodo di cleanup, per ripulire il vecchio spazio di gioco
    @Override
    public void cleanup() {
        
    }
    
      
}
