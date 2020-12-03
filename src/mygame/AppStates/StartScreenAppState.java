
package mygame.AppStates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;



/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */


//Classe StartScreenAppState, sottoclasse di AbstractAppState per il controllo delle schermate di gioco
public class StartScreenAppState extends AbstractAppState implements ScreenController {
    
    
    //Parametri di gioco
    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private InputManager inputManager;    
    private AudioRenderer audioRenderer;
    private AppStateManager stateManager;
    private Listener listener;
    private Node guiNode;
    private BitmapFont guiFont;
    
    //Attributi dell'interfaccia grafica
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Screen screenCompletion;
    private Element imageSoundIconStart;
    private ImageRenderer imageRendererStart;
    private ViewPort guiViewPort;
    
    //Nodo audio schermata principale
    private AudioNode soundTrack;
    
    //Flag per il controllo dei suoni
    private boolean flagSoundEnabled = true;
    private boolean audioOn = true;
    
    //Flag per il controllo dello stato di gioco
    private boolean hasGameStarted = false;
    private boolean updateWinningScreen = false;
    
    //Variabili per l'aggiornamento a video delle schermate
    private int counter = 1;
    private boolean countDirUp = true;
    private long updateTime = 0;
   
    //Path per il salvataggio dei record
    private final String FILENAME = "./record.bin";
    
    
    // Trigger per attivare/disattivare volume
    private static final String MAPPING_MUTE = "Mute";
    private static final Trigger TRIGGER_MUTE = new KeyTrigger(KeyInput.KEY_M);
    
    //Listener che reagisce alla forza/superficie dei tasti
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if(isPressed && name.equals(MAPPING_MUTE)) {
                guiToggleSound();
            }
            else{
                
            }
        }
        
    };
    
    
    
    //Costruttore esplicito per inizializzare la schermata principale
    public StartScreenAppState(Node guiNode,BitmapFont guiFont, Node rootNode){
        this.guiNode = guiNode;
        this.guiFont = guiFont;
        this.rootNode = rootNode;
    }
    
    
    
    //Metodo per l'inizializzazione dei parametri di gioco e della schermata principale
     @Override
    public void initialize(AppStateManager stateManager, Application app) {
        
        super.initialize(stateManager, app);
        
        //Inizializzazione dei parametri di gioco
        this.stateManager = stateManager;
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.guiViewPort = this.app.getGuiViewPort();
        this.audioRenderer = this.app.getAudioRenderer();
        this.app.getFlyByCamera().setEnabled(false);
        this.listener = this.app.getListener();
        
        
        //Inizializzazione suono schermata principale
        soundTrack = new AudioNode(assetManager, "Sounds/soundtrack.wav", AudioData.DataType.Buffer);
        soundTrack.setPositional(false);
        soundTrack.setLooping(true);
        soundTrack.setVolume(0.7f);
        
        //Inizializzazione schermata principale
        initNifty();
        
       //Trigger tasto volume
       inputManager.addMapping(MAPPING_MUTE, TRIGGER_MUTE);
       inputManager.addListener(actionListener, MAPPING_MUTE);
    }   


    //Metodo per l'aggiornamento a video delle schermate
    @Override
    public void update(float tpf) {
        if(updateWinningScreen) {
            if(updateTime < System.currentTimeMillis()) {
                
                if(countDirUp == true) {
                    if(counter < 10) {
                        counter++;    
                    } else {
                        countDirUp = false;
                    }
                }
                if(countDirUp == false) {
                    if(counter > 1) {
                        counter--;    
                    } else {
                        countDirUp = true;
                    }
                }
                //Aggiorna l'immagine
                updateTime = System.currentTimeMillis() + 50;
            }
        }
    }
    
    
    //Inizializzazione interfaccia nifty, mediante file xml
    private void initNifty() {
        
        niftyDisplay = new NiftyJmeDisplay(assetManager,
                                                          inputManager,
                                                          audioRenderer,
                                                          guiViewPort);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/Nifty/startScreen.xml", "start", this);

        //Aggancio dell'interfaccia nifty alla guiViewPort dell'applicazione
        guiViewPort.addProcessor(niftyDisplay);
        soundTrack.play();
        
    }
    

    
    
    //Metodo per l'inizio del gioco
    public void menuStartGame() {
        
        //Disabilita la musica della schermata principale
        soundTrack.stop();
        
        //Inizio gioco
        hasGameStarted = true;
        GamePlayAppState gamePlayAppState = new GamePlayAppState(guiNode,guiFont,rootNode);
        stateManager.attach(gamePlayAppState);
        
    }
    
    
    //Metodo per il caricamento della schermata delle istruzioni
    public void menuInstructionsGame(){
        nifty.gotoScreen("instructions");       
    }
    
    
    
    //Metodo per la chiusura dell'applicazione
    public void menuQuitGame() {
        
        guiViewPort.removeProcessor(niftyDisplay);
        app.stop();
        
    }
    
    
    
    //Metodo per il caricamento della schermata di partenza
    public void setStartScreen() {        
        nifty.gotoScreen("start");
    }
    
    
    
    //Metodo per il caricamento della schermata di loading
    public void setLoadingScreen() {
        
        nifty.gotoScreen("loading");
    }
    
    
    //Metodo per il caricamento della schermata di vittoria
    public void setWinScreen() {
        
        nifty.gotoScreen("youwin");
    }
    
    
    //Metodo per il caricamento della schermata di sconfitta
    public void setLoseScreen() {
        
        nifty.gotoScreen("youdied");
    }
    
    
    //Metodo per il caricamento della schermata di tempo scaduto
    public void setTimeScreen() {
        
        nifty.gotoScreen("timeisup");
    }
    
    
    //Metodo per il caricamento della schermata di load completo
    public void setLoadedScreen() {
        
        nifty.gotoScreen("loaded");
    }
    
    
    //Metodo per il caricamento della schermata "in gioco"
    public void setInGameGUI() {
        
        nifty.gotoScreen("ingameGUI");
    }
    
    
    //Metodo che aggiorna a schermo il numero di torri ancora in vita
    public void updateGameGUITower(int towerAlive, int maxTower) {
        
        //Vengono stampate due scritte, una in penombra rispetto all'altra
        Screen screen = nifty.getScreen("ingameGUI");
        Element tower = screen.findElementByName("tower");
        Element towerS = screen.findElementByName("towerS");
        TextRenderer textRenderer = tower.getRenderer(TextRenderer.class);
        TextRenderer textRendererS = towerS.getRenderer(TextRenderer.class);
        textRenderer.setText("" + towerAlive + " / " + maxTower);
        textRendererS.setText("" + towerAlive + " / " + maxTower);
        
    }
    
    
    //Metodo che mostra il livello raggiunto
    public void showLevel(int currentLevel) {
        
        Screen screen = nifty.getScreen("loading");
        Element level = screen.findElementByName("level");       
        TextRenderer textRenderer = level.getRenderer(TextRenderer.class);       
        textRenderer.setText("Level : " + currentLevel);
     
    }
    
    
    //Metodo che mostra il livello massimo raggiunto
    public void showRecord() {
        
        Screen screen = nifty.getScreen("loading");
        Element record = screen.findElementByName("record");
        TextRenderer textRenderer = record.getRenderer(TextRenderer.class);
        textRenderer.setText("------------- Highest Level : " + getRecord()+" -------------");
        
    }
    
    
    //Metodo che aggiorna a schermo il tempo rimanente
    public void updateGameGUITimer(float currentTimer) {
        
        Screen screen = nifty.getScreen("ingameGUI");
        Element timer = screen.findElementByName("timer");       
        TextRenderer textRenderer = timer.getRenderer(TextRenderer.class);       
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        textRenderer.setText("Time: " + df.format(currentTimer));
        
    }
    
    
    //Metodo per la riproduzione dei suoni in caso di pressing o hovering di un bottone
    public void guiToggleSound() {
        System.out.println("DioPorco");
        if(audioOn) {
            
            audioOn = false;
            soundTrack.pause();
            Screen screen = nifty.getScreen("start");
            Element soundIcon = screen.findElementById("soundOn");
            soundIcon.setVisible(false);
            soundIcon = screen.findElementById("soundOff");
            soundIcon.setVisible(true);
            screen = nifty.getScreen("ingameGUI");
            soundIcon = screen.findElementById("soundOn");
            soundIcon.setVisible(false);
            soundIcon = screen.findElementById("soundOff");
            soundIcon.setVisible(true);

        } 
        else {
            
            audioOn = true;
            if(!hasGameStarted)
                soundTrack.play();
            Screen screen = nifty.getScreen("start");
            Element soundIcon = screen.findElementById("soundOn");
            soundIcon.setVisible(true);
            soundIcon = screen.findElementById("soundOff");
            soundIcon.setVisible(false);
            screen = nifty.getScreen("ingameGUI");
            soundIcon = screen.findElementById("soundOn");
            soundIcon.setVisible(true);
            soundIcon = screen.findElementById("soundOff");
            soundIcon.setVisible(false);
            
        }
        
    }


    
    //Merodo per il controllo delle schermate caricate nel file xml
    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
    }

    
    //Metodo per il controllo di caricamento di una nuova schermata da xml
    @Override
    public void onStartScreen() {
        Screen s = nifty.getCurrentScreen();
    }

    
    //Metodo per il controllo di chiusura di una schermata da xml
    @Override
    public void onEndScreen() {
        Screen s = nifty.getCurrentScreen();
    }
    
    
    
    //Metodo per il caricamento da file di testo del record
    public int getRecord() {
        
        int record ;
        
        try {
            InputStream in = new FileInputStream(new File(FILENAME));
            DataInputStream dataIn = new DataInputStream(in);
            
            record = dataIn.readInt();
            
            dataIn.close();
            in.close();
        } catch (FileNotFoundException ex) {            
            record = 1;
        } catch (IOException ex) {
            record = 1;
        }
        
        return record;
        
    }
    
    
    //Metodo per il salvataggio di un nuovo record
    public void saveRecord(int newRecord) {
        
        try {
        OutputStream out = new FileOutputStream(FILENAME);
        DataOutputStream dataOut = new DataOutputStream(out);
        
        dataOut.writeInt(newRecord);
        
        dataOut.close();
        out.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Cannot write/create to file: file not found ");
        } catch (IOException e) {
            System.out.println("Cannot write/create to file: IOException");
        }
        
    }
    
    
    //Metodo getter per verificare l'audio
    public boolean isAudioOn() {
        return audioOn;
    }
    
}
