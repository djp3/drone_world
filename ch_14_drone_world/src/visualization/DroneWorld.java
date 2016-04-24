package visualization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterPointShape;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

import simulator.Drone;
import simulator.Person;
import simulator.PersonState;
import simulator.Place;
import simulator.Position;
import simulator.Simulator;

public class DroneWorld extends SimpleApplication implements AnimEventListener {
	
	private Random random = new Random(10L);
	private Simulator simulator;

	Box ground;
	Material ground_mat;
	Geometry ground_geo;

	private Spatial canonical_place;
	private Spatial canonical_person;
	private Spatial canonical_drone;

	private Map<Person,Spatial> people;
	private Map<Place,Spatial> places;
	private Map<Drone, Node> drones;

	public DroneWorld(Simulator simulator, Collection<Person> people, Collection<Place> places, Collection<Drone> drones) {
		if (simulator == null) {
			throw new IllegalArgumentException("\"simulator\" can't be null");
		}
		this.simulator = simulator;

		if (people == null) {
			throw new IllegalArgumentException("\"people\" can't be null");
		}
		this.people = new HashMap<Person, Spatial>();
		for (Person person : people) {
			this.people.put(person, null);
		}

		if (places == null) {
			throw new IllegalArgumentException("\"places\" can't be null");
		}
		this.places = new HashMap<Place, Spatial>();
		for (Place place : places) {
			this.places.put(place, null);
		}

		if (drones == null) {
			throw new IllegalArgumentException("\"drones\" can't be null");
		}
		this.drones = new HashMap<Drone, Node>();
		for (Drone drone : drones) {
			this.drones.put(drone, null);
		}
	}

	public DroneWorld() {
		this((Simulator) null, null, null, null);
	}

	public DroneWorld(AppState... initialStates) {
		super(initialStates);
	}

	protected Node player;

	/* Use the main event loop to trigger repeating actions. */
	@Override
	public void simpleUpdate(float tpf) {
		//System.out.println("DroneWorld Person:");
		for (Entry<Person, Spatial> personEntry : people.entrySet()) {
			Person person = personEntry.getKey();
			//System.out.println("\t"+Integer.toHexString(System.identityHashCode(person))+" "+person.toString());
			
			switch (personEntry.getKey().getState()) {
			case WAITING: {
				personEntry.getValue().setLocalTranslation(latLong2Transform(person.getPosition().getLatitude(),
						person.getPosition().getLongitude(), person.getPosition().getHeight()));
			}
				break;
			case EMBARKING: {
				for (Entry<Drone, Node> droneEntry : drones.entrySet()) {
					Drone drone = droneEntry.getKey();
					for (Person p : drone.getEmbarkers()) {
						if (personEntry.getKey().equals(p)) {
							personEntry.getValue()
									.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
											drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
						}
					}
				}

			}
				break;
			case IN_DRONE: {
				for (Entry<Drone, Node> droneEntry : drones.entrySet()) {
					Drone drone = droneEntry.getKey();
					for (Person p : drone.getPassengers()) {
						if (personEntry.getKey().equals(p)) {
							personEntry.getValue()
									.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
											drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
						}
					}
				}

			}
				break;
			case DISEMBARKING: {
				for (Entry<Drone, Node> droneEntry : drones.entrySet()) {
					Drone drone = droneEntry.getKey();
					for (Person p : drone.getDisembarkers()) {
						if (personEntry.getKey().equals(p)) {
							personEntry.getValue()
									.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
											drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
						}
					}
				}
			}
				break;
			case ARRIVED: {
				personEntry.getValue().setLocalTranslation(latLong2Transform(person.getPosition().getLatitude(),
						person.getPosition().getLongitude(), person.getPosition().getHeight()));
			}
				break;
			default:
				throw new IllegalArgumentException("Unhandled Drone State: " + personEntry.getKey().getState());
			}
		}
		//System.out.println("DroneWorld Places:");
		for (Entry<Place, Spatial> placeEntry : places.entrySet()) {
			Place place = placeEntry.getKey();
			Spatial baseNode = placeEntry.getValue();
			if(place.getWaitingToEmbark().size()!=0){
				//System.out.println("\tPlace: "+place.getName());
				for(Person p:place.getWaitingToEmbark()){
					if(!people.keySet().contains(p)){
						throw new RuntimeException("Someone was cloned:"+p);
					}
					//System.out.println("\t\tWaiting:"+Integer.toHexString(System.identityHashCode(p))+" "+p);
				}
			}
			
		}
		
		//System.out.println("DroneWorld Drone:");
		for (Entry<Drone, Node> droneEntry : drones.entrySet()) {
			Drone drone = droneEntry.getKey();
			Node baseNode = droneEntry.getValue();
			
			/*
			System.out.println("\tDrone:"+Integer.toHexString(System.identityHashCode(drone))+" "+drone.getId());
			if((drone.getEmbarkers().size() != 0) || (drone.getPassengers().size() != 0) || (drone.getDisembarkers().size() != 0)){
				for(Person p:drone.getEmbarkers()){
					System.out.println("\t\tEmbark:"+Integer.toHexString(System.identityHashCode(p))+" "+p);
				}
				for(Person p:drone.getPassengers()){
					System.out.println("\t\tPass:"+Integer.toHexString(System.identityHashCode(p))+" "+p);
				}
				for(Person p:drone.getDisembarkers()){
					System.out.println("\t\tDis:"+Integer.toHexString(System.identityHashCode(p))+" "+p);
				}
			}*/
			
			Spatial droneNode = baseNode.getChild("drone");
			Node particlesNode = (Node) baseNode.getChild("particles");
			switch (droneEntry.getKey().getState()) {
			case BEGIN: {

			}
				break;
			case EMBARKING: {

			}
				break;
			case ASCENDING: {

				baseNode.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
						drone.getPosition().getLongitude(), drone.getPosition().getHeight()));

				ParticleEmitter fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 20);
				Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
				mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
				// point
				fire.setShape(new EmitterPointShape(Vector3f.ZERO));
				fire.setMaterial(mat_red);
				fire.setParticlesPerSec(0);
				fire.setImagesX(2);
				fire.setImagesY(2); // 2x2 texture animation
				fire.setStartColor(new ColorRGBA(1.0f, 0f, 0f, 0.4f));
				fire.setEndColor(new ColorRGBA(1f, 1f, 0f, 0.1f));
				fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0f, -0.4f, 0f));
				// fire.setFaceNormal(Vector3f.UNIT_Y);
				fire.setRotateSpeed(1.0f);
				fire.setStartSize(0.05f);
				fire.setEndSize(0.01f);
				fire.setGravity(0, 0.0f, 0);
				fire.setLowLife(0.2f);
				fire.setHighLife(1.0f);
				fire.getParticleInfluencer().setVelocityVariation(0.1f);
				fire.setLocalTranslation(0, 0.2f, 0);

				// Match it to the drone
				particlesNode.attachChild(fire);
				fire.emitAllParticles();

			}
				break;
			case IN_TRANSIT: {
				baseNode.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
						drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
				baseNode.rotate(0,0.5f*tpf,0);
				particlesNode.detachAllChildren();
			}
				break;
			case DESCENDING: {
				baseNode.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
						drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
			}
				break;
			case DISEMBARKING: {

			}
				break;
			case RECHARGING: {
				ParticleEmitter electricity = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 1);
				Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
				mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
				// point
				electricity.setShape(new EmitterPointShape(Vector3f.ZERO));
				electricity.setMaterial(mat_red);
				electricity.setParticlesPerSec(1f);
				electricity.setImagesX(2);
				electricity.setImagesY(2); // 2x2 texture animation
				electricity.setStartColor(new ColorRGBA(0.2f, 0f, 1f, 0.1f));
				electricity.setEndColor(new ColorRGBA(0f, 0f, 1f, 0.3f));
				electricity.getParticleInfluencer().setInitialVelocity(new Vector3f(0f, 0.0f, 0f));
				electricity.setFaceNormal(Vector3f.UNIT_Y);
				electricity.setRotateSpeed(1.0f);
				electricity.setStartSize(0.3f);
				electricity.setEndSize(0.4f);
				electricity.setGravity(0, -0.1f, 0);
				electricity.setLowLife(0.2f);
				electricity.setHighLife(2.0f);
				electricity.getParticleInfluencer().setVelocityVariation(1);

				particlesNode.attachChild(electricity);

			}
				break;
			case IDLING: {
				particlesNode.detachAllChildren();
				droneNode.rotate(0, 0.1f * tpf, 0);
			}
				break;
			default:
				throw new IllegalArgumentException("Unhandled Drone State: " + droneEntry.getKey().getState());
			}

		}
	}

	private TerrainQuad terrain;
	Material mat_terrain;
	private boolean doneWithInit = false;

	/** Initialize the materials used in this scene. */
	public void initMaterials() {
		ground_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("assets/ground.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.EdgeClamp);
		ground_mat.setTexture("ColorMap", tex3);
	}

	private void initGround() {
		ground = new Box(10f, 0.1f, 10f);
		ground.scaleTextureCoordinates(new Vector2f(1, 1));
		ground_geo = new Geometry("Ground", ground);
		ground_geo.setMaterial(ground_mat);
		ground_geo.setLocalTranslation(0, -0.1f, 0);
		ground_geo.setShadowMode(ShadowMode.Receive);
		this.rootNode.attachChild(ground_geo);
		
		rootNode.attachChild(SkyFactory.createSky(
	            assetManager, "Textures/Sky/Bright/FullskiesBlueClear03.dds", false));

	}

	private void initBase() {
		canonical_place = assetManager.loadModel("assets/house.blend");
		canonical_place.scale(0.02f);
		canonical_place.setShadowMode(ShadowMode.Cast);
	}

	private void initDrone(boolean isHighResolution) {
		boolean bluebox = !isHighResolution;

		if (bluebox) {
			Box b = new Box(0.1f, 0.1f, 0.1f); // create cube shape
			Geometry geom = new Geometry("Box", b); // create cube geometry from
													// the
			// shape
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create
			// a
			// simple
			// material
			mat.setColor("Color", ColorRGBA.Blue); // set color of material to
													// blue
			geom.setMaterial(mat); // set the cube's material
			canonical_drone = geom;
			canonical_drone.setShadowMode(ShadowMode.Cast);
		} else {
			canonical_drone = assetManager.loadModel("assets/Quandtum_SAP-1_v2_0.blend");
			canonical_drone.scale(0.1f);
			canonical_drone.setLocalTranslation(0f, 0f, 0f);
		}
		canonical_drone.setShadowMode(ShadowMode.Cast);
	}

	private void initPerson() {
		canonical_person = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
		canonical_person.scale(0.001f);
		canonical_person.rotate(0.0f, -3.0f, 0.0f);
		canonical_person.setShadowMode(ShadowMode.Cast);
	}

	private void initLighting() {
		// viewPort.setBackgroundColor(ColorRGBA.LightGray);
		// DirectionalLight dl = new DirectionalLight();
		// dl.setDirection(new Vector3f(-0.1f, -1f, -1).normalizeLocal());
		// rootNode.addLight(dl);

		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-1.0f, -1.0f, -1.0f));
		rootNode.addLight(sun);

		/* Drop shadows */
		final int SHADOWMAP_SIZE = 1024;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
		dlsr.setLight(sun);
		viewPort.addProcessor(dlsr);

		DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
		dlsf.setLight(sun);
		dlsf.setEnabled(true);
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		fpp.addFilter(dlsf);
		viewPort.addProcessor(fpp);
	}

	private Vector3f latLong2Transform(double latitude, double longitude,double height) {
		double scalex = 240.0;
		float x = (float) ((34.448868 - latitude) * scalex - 4.01);

		double scalez = 200.0;
		float z = (float) ((-119.6629439 - longitude) * scalez - 7.7);
		return new Vector3f(x, (float) height, z);
	}

	@Override
	public void simpleInitApp() {

		assetManager.registerLocator("", FileLocator.class);
		initLighting();
		initMaterials();
		initGround();
		initBase();
		initDrone(this.simulator.isHighResolution());
		initPerson();


		for (Entry<Place, Spatial> placeEntry : places.entrySet()) {

			Place place = placeEntry.getKey();
			Node baseNode = new Node();

			// Display a line of text with a default font
			guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
			BitmapText name = new BitmapText(guiFont, false);
			name.setSize(guiFont.getCharSet().getRenderedSize());
			name.setText(place.getName());
			name.setLocalTranslation(0.0f, 0.5f, place.getName().length() / 2 * 0.1f);
			name.rotate(0f, FastMath.HALF_PI, 0f);
			name.setLocalScale(0.01f);
			name.setShadowMode(ShadowMode.Cast);
			baseNode.attachChild(name);

			// Add the hut
			Spatial base = canonical_place.clone();
			Position position = place.getPosition();
			baseNode.attachChild(base);
			baseNode.setLocalTranslation(latLong2Transform(position.getLatitude(), position.getLongitude(),0f));

			rootNode.attachChild(baseNode);
		}

		for (Entry<Person, Spatial> personEntry : people.entrySet()) {
			Person person = personEntry.getKey();
			Node baseNode = new Node();

			Spatial personNode = canonical_person.clone();
			personNode.setUserData("name", person.getId());
			personNode.rotate(0, FastMath.TWO_PI * random.nextFloat(), 0);
			personNode.setUserData("person", person);
			
			baseNode.attachChild(personNode);

			control = personNode.getControl(AnimControl.class);
			control.addListener(this);
			channel = control.createChannel();
			if (random.nextFloat() > .5) {
				channel.setAnim("Idle1", 0.05f);
			} else {
				channel.setAnim("Idle3", 0.05f);
			}
			channel.setSpeed(random.nextFloat()*0.5f+0.5f);

			guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
			BitmapText name = new BitmapText(guiFont, false);
			name.setSize(guiFont.getCharSet().getRenderedSize());
			name.setText(person.getName());
			name.setLocalTranslation(0.0f, 0.5f, person.getName().length() / 2 * 0.2f);
			name.rotate(0f, FastMath.HALF_PI, 0f);
			name.setLocalScale(0.01f);
			name.setShadowMode(ShadowMode.Cast);

			baseNode.attachChild(name);

			Position position = person.getPosition();

			baseNode.setLocalTranslation(
					jitter(0.2f, 0, 0.2f, latLong2Transform(position.getLatitude(), position.getLongitude(),0f)));

			personEntry.setValue(baseNode);
			rootNode.attachChild(baseNode);
		}

		for (Entry<Drone, Node> droneEntry : drones.entrySet()) {

			Drone drone = droneEntry.getKey();
			Position position = drone.getStart().getPosition();

			Spatial droneNode = canonical_drone.clone();
			droneNode.setName("drone");
			
			Node particlesNode = new Node();
			particlesNode.setName("particles");
			
			Node baseNode = new Node();
			
			baseNode.attachChild(particlesNode);
			baseNode.attachChild(droneNode);
			
			baseNode.setLocalTranslation(latLong2Transform(position.getLatitude(), position.getLongitude(),position.getHeight()));
			baseNode.rotate(0, FastMath.TWO_PI * random.nextFloat(), 0);
			
			droneEntry.setValue(baseNode);
			
			rootNode.attachChild(baseNode);

		}
		initKeys();
		//flyCam.setMoveSpeed(5);
		flyCam.setEnabled(false);
		// Enable a chase cam for this target (typically the player).
		ChaseCamera chaseCam = new ChaseCamera(cam, drones.values().iterator().next(), inputManager);
		chaseCam.setSmoothMotion(true);
		chaseCam.setMinDistance(2f);
		chaseCam.setMaxDistance(10);
		chaseCam.setZoomSensitivity(10);
		chaseCam.setDefaultDistance(2f);
		chaseCam.setMaxVerticalRotation(FastMath.PI);
		chaseCam.setMinVerticalRotation(-1.0f*FastMath.PI);
		
		doneWithInit = true;
	}

	private Vector3f jitter(float x, float y, float z, Vector3f in) {
		float xJitter = (2.0f * random.nextFloat() - 1.0f) * x;
		float yJitter = (2.0f * random.nextFloat() - 1.0f) * y;
		float zJitter = (2.0f * random.nextFloat() - 1.0f) * z;
		return new Vector3f(in.getX() + xJitter, in.getY() + yJitter, in.getZ() + zJitter);
	}

	private AnimChannel channel;
	private AnimControl control;

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		Person p = control.getSpatial().getUserData("person");
		if (p.getState().equals(PersonState.EMBARKING)) {
			channel.setAnim("Jump", 0.05f);
			channel.setLoopMode(LoopMode.DontLoop);
		}
		else if (p.getState().equals(PersonState.DISEMBARKING)) {
			channel.setAnim("Jump", 0.05f);
			channel.setLoopMode(LoopMode.DontLoop);
		}
		else if (p.getState().equals(PersonState.ARRIVED)) {
			channel.setAnim("Death1", 0.05f);
			channel.setLoopMode(LoopMode.DontLoop);
		}
		else if (p.getState().equals(PersonState.IN_DRONE)) {
			channel.setAnim("Spin", 0.05f);
			channel.setLoopMode(LoopMode.DontLoop);
		} else {
			if (animName.equals("Idle2")) {
				channel.setAnim("Idle1", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("Idle1")) {
				channel.setAnim("Idle3", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("Idle3")) {
				channel.setAnim("Backflip", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("Backflip")) {
				channel.setAnim("Spin", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("Spin")) {
				channel.setAnim("Block", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("Block")) {
				channel.setAnim("JumpNoHeight", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("JumpNoHeight")) {
				channel.setAnim("SideKick", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (animName.equals("SideKick")) {
				channel.setAnim("Idle2", 0.05f);
				channel.setLoopMode(LoopMode.DontLoop);
			}
		}
	}

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// unused
	}

	/** Custom Keybinding: Map named actions to inputs. */
	private void initKeys() {
		inputManager.addMapping("Idle1", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addListener(actionListener, "Idle1");
		inputManager.addMapping("Idle2", new KeyTrigger(KeyInput.KEY_2));
		inputManager.addListener(actionListener, "Idle2");
		inputManager.addMapping("Idle3", new KeyTrigger(KeyInput.KEY_3));
		inputManager.addListener(actionListener, "Idle3");
	}

	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if (!keyPressed) {
				if (name.equals("Idle1")) {
					channel.setAnim("Idle1", 0.05f);
					channel.setLoopMode(LoopMode.DontLoop);
				} else if (name.equals("Idle2")) {
					channel.setAnim("Idle2", 0.05f);
					channel.setLoopMode(LoopMode.DontLoop);
				} else if (name.equals("Idle3")) {
					channel.setAnim("Idle3", 0.05f);
					channel.setLoopMode(LoopMode.DontLoop);
				}
			}
		}
	};

	@Override
	public void handleError(String errorMsg, Throwable t) {
		if (simulator != null) {
			simulator.end(errorMsg);
			simulator = null;
		}
		super.handleError(errorMsg, t);

	}

	@Override
	public void destroy() {
		if (simulator != null) {
			simulator.end(null);
			simulator = null;
		}
		super.destroy();
	}

	public void launch() {
		try {
			this.start();
			while(!doneWithInit){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			if (simulator != null) {
				simulator.start();
			}
		} catch (RuntimeException e1) {
			try {
				this.stop();
			} catch (RuntimeException e2) {
			} finally {
				if (simulator != null) {
					simulator.end(e1.toString());
				}
			}
		}

	}

	public static void main(String[] args) {
		DroneWorld app = new DroneWorld();
		app.start(); // start the game
	}

}
