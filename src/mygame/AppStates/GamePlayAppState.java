
package mygame.AppStates;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */

//Classe GamePlay AppState, sottoclasse di AbstractAppState per gestire la partita
public class GamePlayAppState extends AbstractAppState {
    
    //Variabili a cui assegnare lo stato del gioco
    private Node rootNode;
    private Application app;
    private AppStateManager stateManager;
    private Node guiNode;
    private BitmapFont guiFont;
    private AssetManager assetManager;
    
    
    //Oggetti di gioco
    private WorldAppState world; 
    private TowerControlAppState towerControlAppState;
    private StartScreenAppState startScreenAppStates;
    private PlayerControlAppState playerControlAppState;
    
    //Livello corrente
    private int level = 1;
    
    //Numero di torri da caricare
    private int towerNumber;
    
    //Numero di torri vive
    private int towerAlive;
    
    //Flag per il controllo di una vittoria
    private boolean win = false;
    
    //Lista suoni in caso di morte
    private List<AudioNode> death = new ArrayList<>();
    
    //Suono in caso di vittoria
    private AudioNode levelUpAudio;
    
    
    //Costruttore esplicito, per inizializzare il nodo di gioco principale e l'interfaccia testuale
    public GamePlayAppState(Node guiNode, BitmapFont guiFont, Node rootNode){
        this.guiNode = guiNode;
        this.guiFont = guiFont;
        this.rootNode = rootNode;
    }
   
    
    //Metodo per l'inizializzazione dei parametri dello spazio di gioco e i suoni    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        
        //Inizializzazione parametri di gioco e delle schermate in caso di vittoria o sconfitta
        super.initialize(stateManager, app);
        this.stateManager = stateManager;
        this.assetManager = app.getAssetManager();
        startScreenAppStates = stateManager.getState(StartScreenAppState.class);
        
        //Inizializzazione mappa di gioco
        world = new WorldAppState();
        stateManager.attach(world);
      
        //Inizializzazione suoni morte del giocatore
        AudioNode death1 = new AudioNode(assetManager, "Sounds/hellodarknes.wav", AudioData.DataType.Buffer);
        death1.setPositional(false);
        death1.setLooping(false);
        death1.setVolume(0.7f);
        
        AudioNode death2 = new AudioNode(assetManager, "Sounds/morte2.wav", AudioData.DataType.Buffer);
        death2.setPositional(false);
        death2.setLooping(false);
        death2.setVolume(0.7f);
        
        //Aggiunta dei suoni alla lista suoni
        death.add(death1);
        death.add(death2);
        
        //Inizializzazione suono livello successivo
        levelUpAudio = new AudioNode(assetManager, "Sounds/nuovo_livello.wav", AudioData.DataType.Buffer);
        levelUpAudio.setPositional(false);
        levelUpAudio.setLooping(false);
        levelUpAudio.setVolume(0.7f);
        
        
        
        
        
    }
    

    //Metodo di update, per controllare lo stato di gioco
    @Override
    public void update(float tpf){
            
        //Controllo torri nemiche ancora in vita
        towerAlive = world.getTowerAlive();
        if(towerAlive==0){
            
            //Disabilitazione suoni
            setLevel(level+1);
            if(stateManager.getState(StartScreenAppState.class).isAudioOn()) {
                
                playerControlAppState.stopJetAudio();
                playerControlAppState.inibitorExplosionAudio();
                towerControlAppState.inibitTowerExplosionAudio();
                levelUpAudio.play();
                
            }
            
            //Salvataggio nuovo record, nel caso
            if(startScreenAppStates.getRecord() < level) {
                
                startScreenAppStates.saveRecord(level);
                System.out.println("----- new record ---> " + startScreenAppStates.getRecord());
                
            }
            
            //Avanzamento di livello
            world.levelUp(level);
        }
        
        //Controllo giocatore ancora in vita
        if(!world.getPlayerAlive()){     
            
            //Abilita un suono a caso dalla lista dei suoni in caso di morte del giocatore
            if(startScreenAppStates.isAudioOn()) {
                
                int rand = (int)(Math.random() * 100) % death.size() ;
                death.get(rand).play();
                
            }
            
            //Decrementa il livello in caso di sconfitta
            if(level>=2)
                level--;
            else
                level=1;
            
            //Decremento di livello in caso di morte
            world.levelDown1(level);
            
        }
        
        
        //Aggiornamento del tempo rimasto
        startScreenAppStates.updateGameGUITimer(world.getTimer());
        
        //Aggiornamento delle torri nemiche ancora in vita
        startScreenAppStates.updateGameGUITower(towerAlive, getTowerNumber());
       
        
        //Decremento del tempo di gioco rimasto
        world.setTimer(world.getTimer() - tpf);
        
        //Controllo del tempo di gioco rimasto
        if(world.getTimer() <= 0){
            
            //Disabilitazione dei suoni
            if(stateManager.getState(StartScreenAppState.class).isAudioOn()) {
                
                playerControlAppState.stopJetAudio();
                playerControlAppState.inibitorExplosionAudio();
                towerControlAppState.inibitTowerExplosionAudio();
                
            }
            
            //Decrementa il livello in caso di sconfitta
            if(level>=2)
                level--;
            else
                level=1;
            
            //Decremento di livello in caso di tempo scaduto
            world.levelDown2(level);
            
        }
        
    }
    
    
    //Metodo che inizializza una partita
    public void startGame(){
        
       towerControlAppState = stateManager.getState(TowerControlAppState.class);
       playerControlAppState = stateManager.getState(PlayerControlAppState.class);
       world = stateManager.getState(WorldAppState.class);
   
    }
    
    
    //Metodo getter per il livello corrente
    public int getLevel(){
        return level;
    }
    
    
    //Metodo setter per il livello corrente
    public void setLevel(int value){
        level = value;
    }
    
    
    //Metodo getter per il numero di torri in vita
    public int getTowerNumber(){
        return towerNumber;
    }
    
     
    //Metodo setter per il numero di torri da caricare
    public void setTowerNumber(int value){
        towerNumber = value;
    }
      
    
    //Metodo getter per controllare la vittoria di un livello
    public boolean getWin(){
          return win;
    }
     
      
    //Metodo di cleanup, per ripulire il vecchio spazio di gioco
    @Override
    public void cleanup(){
        rootNode.detachAllChildren();
        super.cleanup();
    }
      
}
