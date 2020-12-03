
package mygame.AppStates;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;


/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */

//Classe BulletControl, sottoclasse di AbstractControl, usata per il controllo di ogni proiettile sparato dalle torri
public class BulletControl extends AbstractControl {

    private RigidBodyControl ballPhy;
    private PlayerControlAppState playerControlAppState;
    private Camera cam;
    
    //Fattori per regolare la traiettoria dei proiettili delle torri
    private float timeInseguimento=0;
    private float timeInutile = 0;
    private final float TINUT = 5f;
    private final float FOLLOW_FACTOR = 0.5f;
    private float distance;
    private Vector3f tmp;
    
    //Questo fattore regola la difficoltà
    private final float TINSEG = 2f;
    
    

    //Costruttore che dota ogni proiettile delle proprietà fisiche dello spazio di gioco e delle informazioni sul giocatore
    public BulletControl(RigidBodyControl ballPhy, PlayerControlAppState playerControlAppState) {
        
        this.ballPhy = ballPhy;
        this.playerControlAppState = playerControlAppState;
        this.cam = playerControlAppState.getCamera();
        
    }
    
       
    //Metodo che ad ogni frame regola la traiettoria del proiettile
    @Override
    protected void controlUpdate(float tpf) {
        
         //Interrompe l'inseguimento da parte del proiettile
         if(timeInseguimento > TINSEG || playerControlAppState.getHit()) { 
              ballPhy.setLinearVelocity(ballPhy.getLinearVelocity());
              timeInutile += tpf;
         }
         
         //Regola la traiettoria del proiettile per seguire lo spostamento del giocatore
         else {            
              timeInseguimento += tpf;
              distance = playerControlAppState.getVehiclePosition().distance(spatial.getWorldTranslation());
              tmp =  cam.getDirection().mult(distance * FOLLOW_FACTOR); 
              tmp = playerControlAppState.getVehiclePosition().add(tmp);
              
              //Calcolo mediante Posizione globale
              tmp = tmp.subtract(spatial.getWorldTranslation());                                  
              ballPhy.setLinearVelocity(tmp.mult(timeInseguimento*timeInseguimento));
         }
              
        //Rimuove il proiettile quando è lontano dalla mappa di gioco    
        if(timeInutile > TINUT) {
              spatial.removeFromParent();
        }
              
    }    

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Da implementare nella sottoclasse       
    }
    
}
