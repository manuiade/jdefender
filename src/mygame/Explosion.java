
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;


/**
 *
 * @autori Manuel Iaderosa e Davide Casalini
 * 
 */


/**
 * Classe Explosion, utilizzata per realizzare le esplosioni dei proiettili,del giocatore
 * e delle torri danneggiate
 */


public final class Explosion {
    
    //Emettitori di effetti usati per le esplosioni dei proiettili e del giocatore
    private ParticleEmitter burstEmitter;
    private ParticleEmitter shockwaveEmitter;
    private ParticleEmitter fireEmitter;
    private ParticleEmitter smokeEmitter;
    private ParticleEmitter embersEmitter;
    
    //Emettitori di effetti usati per le torri danneggiate
    private ParticleEmitter towerFireEmitter;
    private ParticleEmitter towerSmokeEmitter;
    
    //Classe contenente la radice della directory del progetto
    private AssetManager assetManager;
  
    //Nodo a cui si collegano gli emettitori
    public Node explosionNode;
    
    //Variabile che conta la durata dell'esplosione di ogni proiettile
    private float timerBullet=0;
    
    //Costruttore per inizializzare le esplosioni dei proiettili e del giocatore
    public Explosion(AppStateManager stateManager,Application app){
        
        this.assetManager = app.getAssetManager();
        explosionNode = new Node("explosionNode");
        initBurst();
        initEmbers();
        initFire();
        initShockwave();
        initSmoke();    
        
    }
    
    //Costruttore per inizializzare le esplosioni delle torri danneggiate
    public Explosion(AppStateManager stateManager,Application app,int index){
        
        this.assetManager = app.getAssetManager();
        explosionNode = new Node("explosionNode");
        initTowerFire();
        initTowerSmoke();
        
    }
    
    //Metodo che inizializza l'effetto fuoco sulle torri danneggiate
    public void initTowerFire(){
        
        //Inizializzazione effetto
        towerFireEmitter = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager,"Common/MatDefs/Misc/Particle.j3md");   
        fireMat.setTexture("Texture",assetManager.loadTexture("Effects/flame.png"));
        towerFireEmitter.setMaterial(fireMat);
        towerFireEmitter.setImagesX(2);
        towerFireEmitter.setImagesY(2);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(towerFireEmitter);
        
        //Regolazione parametri dell'effetto
        towerFireEmitter.setSelectRandomImage(true);
        towerFireEmitter.setRandomAngle(true);      
        towerFireEmitter.setStartColor(new ColorRGBA(1f, 1f, .5f, 1f));
        towerFireEmitter.setEndColor(new ColorRGBA(1f, 0f, 0f, 0f));
        towerFireEmitter.setGravity(0,-3,0);
        towerFireEmitter.getParticleInfluencer().
        setVelocityVariation(0.6f);
        towerFireEmitter.getParticleInfluencer().
        setInitialVelocity(new Vector3f(0,6f,0));
        towerFireEmitter.setLowLife(0.5f);
        towerFireEmitter.setHighLife(2f);
        towerFireEmitter.setStartSize(20f);
        towerFireEmitter.setEndSize(2f);
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        towerFireEmitter.setParticlesPerSec(0);
        
    }
    
    //Metodo che inizializza l'effetto fumo sulle torri danneggiate
    public void initTowerSmoke(){
        
        //Inizializzazione effetto
        towerSmokeEmitter = new ParticleEmitter("dust emitter", Type.Triangle, 100);
        Material dustMat = new Material(assetManager,"Common/MatDefs/Misc/Particle.j3md");
        dustMat.setTexture("Texture",assetManager.loadTexture("Effects/smoke.png"));
        towerSmokeEmitter.setMaterial(dustMat);    
        towerSmokeEmitter.setImagesX(2);
        towerSmokeEmitter.setImagesY(2);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(towerSmokeEmitter);
        
        //Regolazione parametri dell'effetto
        towerSmokeEmitter.setSelectRandomImage(true);
        towerSmokeEmitter.setRandomAngle(true);
        towerSmokeEmitter.setGravity(0,-5,0);
        towerSmokeEmitter.getParticleInfluencer().setVelocityVariation(.2f);
        towerSmokeEmitter.setStartSize(13f);
        towerSmokeEmitter.getParticleInfluencer().
        setInitialVelocity(new Vector3f(0,40f,0));
        towerSmokeEmitter.setEndSize(15f);
        towerSmokeEmitter.setHighLife(5f);
        towerSmokeEmitter.setLowLife(3f);      
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        towerSmokeEmitter.setParticlesPerSec(0);
        
  
    }
    
    //Metodo che inizializza l'effetto scoppio sulle esplosioni
    public void initBurst(){
        
        //Inizializzazione effetto
        burstEmitter= new ParticleEmitter("Burst",ParticleMesh.Type.Triangle,5);
        Material burstMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        burstMat.setTexture("Texture", assetManager.loadTexture("Effects/flash.png"));
        burstEmitter.setMaterial(burstMat);
        burstEmitter.setImagesX(2);
        burstEmitter.setImagesY(2);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(burstEmitter);
        
        //Regolazione parametri dell'effetto
        burstEmitter.setSelectRandomImage(true);
        burstEmitter.setRandomAngle(true);      
        burstEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
        burstEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, .25f));
        burstEmitter.setStartSize(.1f);
        burstEmitter.setEndSize(6.0f);
        burstEmitter.setGravity(0, 0, 0);
        burstEmitter.setLowLife(.75f);
        burstEmitter.setHighLife(.75f);
        burstEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2f, 0));
        burstEmitter.getParticleInfluencer().setVelocityVariation(1);
        burstEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, .5f));
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        burstEmitter.setParticlesPerSec(0);
        
    }
    
    //Metodo che inizializza l'effetto onda d'urto sulle esplosioni
    public void initShockwave(){
        
        //Inizializzazione effetto
        shockwaveEmitter= new ParticleEmitter("Shock",ParticleMesh.Type.Triangle,2);
        Material shockwaveMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        shockwaveMat.setTexture("Texture", assetManager.loadTexture("Effects/shockwave.png"));
        shockwaveEmitter.setMaterial(shockwaveMat);
        shockwaveEmitter.setImagesX(1);
        shockwaveEmitter.setImagesY(1);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(shockwaveEmitter);
        
        //Regolazione parametri dell'effetto
        shockwaveEmitter.setFaceNormal(Vector3f.UNIT_Y);
        shockwaveEmitter.setStartColor(new ColorRGBA(.68f, 0.77f, 0.61f, 1f));
        shockwaveEmitter.setEndColor(new ColorRGBA(.68f, 0.77f, 0.61f, 0f));
        shockwaveEmitter.setStartSize(1f);
        shockwaveEmitter.setEndSize(7f);
        shockwaveEmitter.setGravity(0, 0, 0);
        shockwaveEmitter.setLowLife(1f);
        shockwaveEmitter.setHighLife(1f);
        shockwaveEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        shockwaveEmitter.getParticleInfluencer().setVelocityVariation(0f);
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        shockwaveEmitter.setParticlesPerSec(0);
    }

    //Metodo che inizializza l'effetto onda d'urto sulle esplosioni
    public void initFire(){
        
        //Inizializzazione effetto
        fireEmitter= new ParticleEmitter("Emitter",ParticleMesh.Type.Triangle,100);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
        fireEmitter.setMaterial(fireMat);
        fireEmitter.setImagesX(2);
        fireEmitter.setImagesY(2);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(fireEmitter);
        
        //Regolazione parametri dell'effetto
        fireEmitter.setRandomAngle(true);
        fireEmitter.setSelectRandomImage(true);
        fireEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        fireEmitter.setStartColor(new ColorRGBA(1f, 1f, .5f, 1f));
        fireEmitter.setEndColor(new ColorRGBA(1f, 0f, 0f, 0f));
        fireEmitter.setGravity(0, -.5f, 0);
        fireEmitter.setStartSize(1f);
        fireEmitter.setEndSize(0.05f);
        fireEmitter.setLowLife(.5f);
        fireEmitter.setHighLife(2f);
        fireEmitter.getParticleInfluencer().setVelocityVariation(0.3f);
        fireEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3f, 0));
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        fireEmitter.setParticlesPerSec(0);
    }
    
    //Metodo che inizializza l'effetto onda d'urto sulle esplosioni
    public void initSmoke(){
        
        //Inizializzazione effetto
        smokeEmitter= new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle,20);
        Material smokeMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        smokeMat.setTexture("Texture", assetManager.loadTexture("Effects/smoke.png"));
        smokeEmitter.setMaterial(smokeMat);
        smokeEmitter.setImagesX(2);
        smokeEmitter.setImagesY(2);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(smokeEmitter);
        
        //Regolazione parametri dell'effetto
        smokeEmitter.setSelectRandomImage(true);
        smokeEmitter.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1f));
        smokeEmitter.setEndColor(new ColorRGBA(.1f, 0.1f, 0.1f, .5f));
        smokeEmitter.setLowLife(4f);
        smokeEmitter.setHighLife(4f);
        smokeEmitter.setGravity(0,2,0);
        smokeEmitter.setFacingVelocity(true);
        smokeEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 6f, 0));
        smokeEmitter.getParticleInfluencer().setVelocityVariation(1);
        smokeEmitter.setStartSize(.5f);
        smokeEmitter.setEndSize(3f);
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        smokeEmitter.setParticlesPerSec(0);
    }
    
    //Metodo che inizializza l'effetto onda d'urto sulle esplosioni
    public void initEmbers(){
        
        //Inizializzazione effetto
        embersEmitter= new ParticleEmitter("Embers",ParticleMesh.Type.Triangle,50);
        Material embersMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        embersMat.setTexture("Texture", assetManager.loadTexture("Effects/embers.png"));
        embersEmitter.setMaterial(embersMat);
        embersEmitter.setImagesX(1);
        embersEmitter.setImagesY(1);
        
        //Collegamento dell'effetto al nodo principale
        explosionNode.attachChild(embersEmitter);
        
        //Regolazione parametri dell'effetto
        embersEmitter.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, 1.0f));
        embersEmitter.setEndColor(new ColorRGBA(0, 0, 0, 0.5f));
        embersEmitter.setStartSize(1.2f);
        embersEmitter.setEndSize(1.8f);
        embersEmitter.setGravity(0, -.5f, 0);
        embersEmitter.setLowLife(1.8f);
        embersEmitter.setHighLife(5f);
        embersEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3, 0));
        embersEmitter.getParticleInfluencer().setVelocityVariation(.5f);
        embersEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        
        //Azzeramento emissioni dell'effetto fino al richiamo dell'esplosione
        embersEmitter.setParticlesPerSec(0);

    }
    
    
    //Metodo che attiva l'esplosione del giocatore
    public int explosion(float timer) {
     
          // Il timer regola l'attivazione degli effetti nel giusto ordine
          if (timer > 0f && timer<=.6f) {
            burstEmitter.emitAllParticles();
            shockwaveEmitter.emitAllParticles();
          }
          
          if (timer > .6f && timer <=1) {
            fireEmitter.emitAllParticles();
            embersEmitter.emitAllParticles();
            smokeEmitter.emitAllParticles();
            return 1;
          }
          
          if (timer > 1 && timer <= 1.5f) {
            //Annulla gli effetti
            burstEmitter.killAllParticles();
            shockwaveEmitter.killAllParticles();
          }
          
          if (timer > 2.5f) {
            //Annulla gli effetti
            smokeEmitter.killAllParticles();
            embersEmitter.killAllParticles();
            fireEmitter.killAllParticles();
            return 2;
          }
             
       return 0;
       
    }
    
    
    //Metodo che attiva l'esplosione di una torre
    public void towerExplode(){
        
        towerFireEmitter.emitAllParticles();
        towerSmokeEmitter.setParticlesPerSec(50);
        towerSmokeEmitter.emitAllParticles();
        
    }
    
    
    //Metodo che attiva l'esplosione di un proiettile
    public void ballExplode(float tpf){
        
        // Il timer regola l'attivazione degli effetti nel giusto ordine
        while(timerBullet<2.0f){
            
          if (timerBullet > 0f && timerBullet<=.6f) {
            burstEmitter.emitAllParticles();
            shockwaveEmitter.emitAllParticles();
          }
          
          if (timerBullet > .6f && timerBullet <=1) {
            fireEmitter.emitAllParticles();
            embersEmitter.emitAllParticles();
            smokeEmitter.emitAllParticles();
          }
          
          if (timerBullet > 1 && timerBullet <= 1.5f) {
            //Annulla gli effetti
            burstEmitter.killAllParticles();
            shockwaveEmitter.killAllParticles();
          }
          
          if (timerBullet > 1.5f) {
            //Annulla gli effetti
            smokeEmitter.killAllParticles();
            embersEmitter.killAllParticles();
            fireEmitter.killAllParticles();          
          }
          
          timerBullet+=tpf;
          
        }
        
    }
    
    //Metodo che inibisce un'esplosione
    public void killPartcles() {
        
         burstEmitter.killAllParticles();
         shockwaveEmitter.killAllParticles();
         smokeEmitter.killAllParticles();
         embersEmitter.killAllParticles();
         fireEmitter.killAllParticles();
         
    }
    
    
    //Metodo getter per il timer
    public float getTimerBullet() {        
        return timerBullet;       
    }
    
    
    //Metodo setter per il timer
    public void setTimerBullet(float value) {        
        this.timerBullet = value;
    }
}
