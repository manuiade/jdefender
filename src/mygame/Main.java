
package mygame;

import mygame.AppStates.StartScreenAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.system.AppSettings;
import java.util.logging.Level;

/**
 * 
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */

/*
* Classe Main, sottoclasse di SimpleApplication per regolare le impostazioni, per lanciare e inizializzare l'applicazione
*/

public class Main extends SimpleApplication {

    //Oggetto statico usato per la configurazione delle impostazioni e l'avvio dell'applicazione
    private static Main app;
    
    //Metodo main, in cui vengono configurate le impostazione schermo e si lancia l'applicazione
    public static void main(String[] args) {
        
        app = new Main();
        AppSettings settings = new AppSettings(true);
        app.setShowSettings(false);
        settings.setTitle("JDefender");
        //settings.setFullscreen(true);
        settings.setResolution(1024,768);
        app.setSettings(settings);
        
        //Avvio dell'applicazione
        app.start();
        
    }

    
    //Metodo per l'inizializzazione dell'applicazione
    @Override
    public void simpleInitApp() {
        
       java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
       setDisplayStatView(false);      
       flyCam.setEnabled(false);
       
       //Inizializzazione della schermata di gioco iniziale
       StartScreenAppState startScreenAppState = new StartScreenAppState(guiNode,guiFont,rootNode);
       stateManager.attach(startScreenAppState);
       
       //Personalizzazione del cursore
       JmeCursor jc = (JmeCursor) assetManager.loadAsset("Interface/Nifty/resources/cursor.cur");
       inputManager.setCursorVisible(true);
       inputManager.setMouseCursor(jc);

    }

}
