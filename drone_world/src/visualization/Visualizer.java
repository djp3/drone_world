package visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterPointShape;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
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
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

import reference.DistanceAwarePromiscuousController;
import reference.GreedyController;
import reference.PromiscuousController;
import reference.RandomDroneController;
import simulator.Drone;
import simulator.Explosion;
import simulator.Pair;
import simulator.Person;
import simulator.Place;
import simulator.Position;
import simulator.Simulator;
import simulator.enums.PersonState;

/**
 * The visualization for the simulator
 * @author djp3
 *
 */
public class Visualizer extends SimpleApplication implements AnimEventListener {
	
	private Random random = new Random(10L);
	private Simulator simulator;

	Box ground;
	Material ground_mat;
	Geometry ground_geo;

	private Spatial canonical_place;
	private Spatial canonical_person;
	
	private List<Spatial> canonical_drones; //The first one is for the student drones, the rest are the professors competitors

	private Map<Person,Spatial> people;
	private Map<Place,Spatial> places;
	private Map<Drone, Node> drones;

	private boolean _doneWithInit = false;
	private Object _doneWithInitLock = new Object();
	private void setDoneWithInit(boolean newValue){
		synchronized(_doneWithInitLock){
			_doneWithInit = newValue;
		}
	}

	private boolean getDoneWithInit(){
		synchronized(_doneWithInitLock){
			return _doneWithInit;
		}
	}

	public Visualizer(Simulator simulator, Collection<Person> people, Collection<Place> places, Collection<Drone> drones) {
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

	public Visualizer() {
		this((Simulator) null, null, null, null);
	}

	public Visualizer(AppState... initialStates) {
		super(initialStates);
	}

	//Head's up display items
	private BitmapText hudWaitingText;
	private Geometry hudWaitingGeom;
	private TreeMap<String, Pair<BitmapText, Geometry>> hudCompanyDelivery;
	private TreeMap<String, Pair<BitmapText, Geometry>> hudCompanyFlying;
	private TreeMap<String, Pair<BitmapText, Geometry>> hudCompanyDead;
	private BitmapFont consoleFont;

	// A set of all the drones that have exploded to ensure animations are played only once
	private Set<String> explodingDrones = new HashSet<String>();
	private Set<String> smokingDrones = new HashSet<String>();
	private Set<String> quarantinedDrones = new HashSet<String>();
	
	
	/** Initialize the materials used in this scene. */
	public void initMaterials() {
		ground_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("assets/map.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.EdgeClamp);
		ground_mat.setTexture("ColorMap", tex3);
	}

	@SuppressWarnings("deprecation")
	private void initGround() {
		ground = new Box(10f, 0.1f, 16.019f);
		ground.scaleTextureCoordinates(new Vector2f(1, 1));
		ground_geo = new Geometry("Ground", ground);
		ground_geo.setMaterial(ground_mat);
		ground_geo.setLocalTranslation(0, -0.1f, 0);
		ground_geo.setShadowMode(ShadowMode.Receive);
		rootNode.attachChild(ground_geo);
		
		rootNode.attachChild(SkyFactory.createSky( assetManager, "Textures/Sky/Bright/FullskiesBlueClear03.dds", false));
	}

	private void initBase() {
		canonical_place = assetManager.loadModel("assets/house.blend");
		canonical_place.scale(0.015f);
		canonical_place.setShadowMode(ShadowMode.Cast);
	}

	private void initDrone(boolean isHighResolution) {
		boolean box = !isHighResolution;
		canonical_drones = new ArrayList<Spatial>();

		if (box) {
			List<ColorRGBA> colors = new ArrayList<ColorRGBA>();
			colors.add(ColorRGBA.Blue);
			colors.add(ColorRGBA.Green);
			colors.add(ColorRGBA.Magenta);
			colors.add(ColorRGBA.Red);
			colors.add(ColorRGBA.Cyan);
			colors.add(ColorRGBA.Yellow);
			colors.add(ColorRGBA.Pink);
			for(ColorRGBA color: colors){
				Box b = new Box(0.1f, 0.1f, 0.1f); // create cube shape
				Geometry geom = new Geometry("Box", b); // create cube geometry from the shape
				Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create // a // simple // material
				mat.setColor("Color", color);
				geom.setMaterial(mat); // set the cube's material
				Spatial drone = geom;
				drone.setLocalTranslation(0f, 0.1f, 0f);
				drone.setShadowMode(ShadowMode.Cast);
				canonical_drones.add(drone);
			}
		} else {
			assetManager.registerLocator("assets/Quandtum_SAP-1/", FileLocator.class);
			Spatial drone = assetManager.loadModel("Quandtum_SAP-1_v2_0.blend");
			assetManager.unregisterLocator("assets/Quandtum_SAP-1/", FileLocator.class);
			drone.scale(0.1f);
			drone.setLocalTranslation(0f, 0f, 0f);
			drone.setShadowMode(ShadowMode.Cast);
			canonical_drones.add(drone);
			
			System.out.println("This program uses a model from XhyldazhK at http://www.blendswap.com/blends/view/76328");
			assetManager.registerLocator("assets/nauyaca/", FileLocator.class);
			drone = assetManager.loadModel("spaceship01.blend");
			assetManager.unregisterLocator("assets/nauyaca/", FileLocator.class);
			drone.scale(0.02f);
			drone.setLocalTranslation(0f, 0.33f, 0f);
			Quaternion q1 = new Quaternion().fromAngleNormalAxis(FastMath.HALF_PI,Vector3f.UNIT_X);
			drone.setLocalRotation(q1);
			drone.setShadowMode(ShadowMode.Cast);
			canonical_drones.add(drone);
			
			assetManager.registerLocator("assets/simple", FileLocator.class);
			drone = assetManager.loadModel("Spaceship_Request.blend");
			assetManager.unregisterLocator("assets/simple", FileLocator.class);
			drone.scale(0.04f);
			drone.setLocalTranslation(0f, 0.2f, 0f);
			q1 = new Quaternion().fromAngleNormalAxis(FastMath.PI,Vector3f.UNIT_Y);
			drone.setLocalRotation(q1);
			drone.setShadowMode(ShadowMode.Cast);
			canonical_drones.add(drone);
			
			System.out.println("This program uses a model from granthus at http://www.blendswap.com/blends/view/42451");
						
			assetManager.registerLocator("assets/42451_Orion_Rising", FileLocator.class);
			drone = assetManager.loadModel("OrionRising01.blend");
			assetManager.unregisterLocator("assets/42451_Orion_Rising", FileLocator.class);
			drone.scale(0.04f);
			drone.setLocalTranslation(0f, 0.2f, 0f);
			q1 = new Quaternion().fromAngleNormalAxis(FastMath.PI,Vector3f.UNIT_Y);
			drone.setLocalRotation(q1);
			drone.setShadowMode(ShadowMode.Cast);
			canonical_drones.add(drone);
			
		}
	}

	private void initPerson() {
		canonical_person = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
		canonical_person.scale(0.001f);
		canonical_person.rotate(0.0f, -3.0f, 0.0f);
		canonical_person.setShadowMode(ShadowMode.Cast);
	}

	private void initLighting() {
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
		double scalex = 275.0; 
		float x = (float) ((34.448868 - latitude) * scalex - 5.51);

		double scalez = 218.0; 
		float z = (float) ((-119.6629439 - longitude) * scalez - 11.9);
		return new Vector3f(x, (float) height, z);
	}

	@Override
	public void simpleInitApp() {
		
		if(simulator.isQuitting() || simulator.isSimulationEnded()){
			return;
		}

		assetManager.registerLocator("", FileLocator.class);
		initLighting();
		initMaterials();
		initGround();
		initBase();
		initDrone(this.simulator.isHighResolution());
		initPerson();
		
		consoleFont = assetManager.loadFont("Interface/Fonts/Console.fnt");
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");


		for (Entry<Place, Spatial> placeEntry : places.entrySet()) {

			Place place = placeEntry.getKey();
			Node baseNode = new Node();

			// Display a line of text with a default font
			BitmapText frontName = new BitmapText(guiFont, false);
			frontName.setSize(guiFont.getCharSet().getRenderedSize());
			frontName.setText(place.getName());
			frontName.setLocalTranslation((place.getName().length() / 2.0f) * -0.05f ,0.55f,0.0f);
			frontName.setLocalScale(0.01f);
			frontName.setShadowMode(ShadowMode.Off);
			
			baseNode.attachChild(frontName);
			
			BitmapText backName = new BitmapText(guiFont, false);
			backName.setSize(guiFont.getCharSet().getRenderedSize());
			backName.setText(place.getName());
			backName.setLocalTranslation((place.getName().length() / 2.0f) * 0.05f ,0.55f,0.0f);
			backName.rotate(0f, FastMath.PI, 0f);
			backName.setLocalScale(0.01f);
			backName.setShadowMode(ShadowMode.Off);
			
			baseNode.attachChild(backName);

			// Add the hut
			Spatial base = canonical_place.clone();
			Position position = place.getPosition();
			baseNode.attachChild(base);
			baseNode.setLocalTranslation(latLong2Transform(position.getLatitude(), position.getLongitude(),0f));
			
			baseNode.rotate(0, FastMath.TWO_PI * random.nextFloat(), 0);

			rootNode.attachChild(baseNode);
		}

		for (Entry<Person, Spatial> personEntry : people.entrySet()) {
			Person person = personEntry.getKey();
			Node baseNode = new Node();

			Spatial personNode = canonical_person.clone();
			//personNode.setUserData("name", person.getId());
			personNode.rotate(0, FastMath.TWO_PI * random.nextFloat(), 0);
			personNode.setUserData("person", person);
			personNode.setLocalTranslation(random.nextFloat()*0.2f-0.1f, 0.0f,random.nextFloat()*0.2f-0.1f);
			
			
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

			BitmapText frontName = new BitmapText(guiFont, false);
			frontName.setSize(guiFont.getCharSet().getRenderedSize());
			frontName.setText(person.getName());
			frontName.setLocalTranslation((person.getName().length() / 2.0f) * -0.04f ,0.35f,0.0f);
			frontName.setLocalScale(0.005f);
			frontName.setShadowMode(ShadowMode.Off);

			baseNode.attachChild(frontName);
			
			BitmapText backName = new BitmapText(guiFont, false);
			backName.setSize(guiFont.getCharSet().getRenderedSize());
			backName.setText(person.getName());
			backName.setLocalTranslation((person.getName().length() / 2.0f) * 0.04f ,0.35f,0.0f);
			backName.rotate(0f, FastMath.PI, 0f);
			backName.setLocalScale(0.005f);
			backName.setShadowMode(ShadowMode.Off);

			baseNode.attachChild(backName);

			Position position = person.getPosition();

			baseNode.setLocalTranslation( latLong2Transform(position.getLatitude(), position.getLongitude(),0f));

			personEntry.setValue(baseNode);
			rootNode.attachChild(baseNode);
		}

		for (Entry<Drone, Node> droneEntry : drones.entrySet()) {

			Drone drone = droneEntry.getKey();
			Position position = drone.getStart().getPosition();

			Spatial droneNode;
			int size = canonical_drones.size();
			int index = 0; //Assume the drone is a student drone (draw drone 0)
			if(size > 1){  //If there are more drones available draw a different one for professor's drones
				if(droneEntry.getKey().getCompanyName().equals(RandomDroneController.companyName)){
					index = 0;
					index = (index % (size-1)) +1; //Make the professor's drone be one with index greater than 0
				} else if(droneEntry.getKey().getCompanyName().equals(GreedyController.companyName)){
					index = 1;
					index = (index % (size-1)) +1; //Make the professor's drone be one with index greater than 0
				} else if(droneEntry.getKey().getCompanyName().equals(PromiscuousController.companyName)){
					index = 2;
					index = (index % (size-1)) +1; //Make the professor's drone be one with index greater than 0
				} else if(droneEntry.getKey().getCompanyName().equals(DistanceAwarePromiscuousController.companyName)){
					index = 3;
					index = (index % (size-1)) +1; //Make the professor's drone be one with index greater than 0
				}
			}
			droneNode = canonical_drones.get(index).clone();
			droneNode.setName("drone");
			
			Node particlesNode = new Node();
			particlesNode.setName("particles");
			
			Node baseNode = new Node();
			
			baseNode.attachChild(particlesNode);
			baseNode.attachChild(droneNode);
			
			BitmapText frontName = new BitmapText(guiFont, false);
			frontName.setSize(guiFont.getCharSet().getRenderedSize());
			frontName.setText(drone.getName());
			frontName.setLocalTranslation((drone.getName().length() / 2.0f) * -0.04f ,-0.15f,0.0f);
			//name.rotate(0f, FastMath.HALF_PI, 0f);
			frontName.setLocalScale(0.005f);
			frontName.setShadowMode(ShadowMode.Off);

			baseNode.attachChild(frontName);
			
			BitmapText backName = new BitmapText(guiFont, false);
			backName.setSize(guiFont.getCharSet().getRenderedSize());
			backName.setText(drone.getName());
			backName.setLocalTranslation((drone.getName().length() / 2.0f) * 0.04f ,-0.15f,0.0f);
			backName.rotate(0f, FastMath.PI , 0f);
			backName.setLocalScale(0.005f);
			backName.setShadowMode(ShadowMode.Off);

			baseNode.attachChild(backName);
			
			baseNode.setLocalTranslation(latLong2Transform(position.getLatitude(), position.getLongitude(),position.getHeight()));
			baseNode.rotate(0, FastMath.TWO_PI * random.nextFloat(), 0);
			
			droneEntry.setValue(baseNode);
			
			rootNode.attachChild(baseNode);

		}
		//Set up heads up display
		setDisplayStatView(false);
		setDisplayFps(false);
		
		hudWaitingText = new BitmapText(consoleFont,false);
		hudWaitingText.setSize(consoleFont.getCharSet().getRenderedSize()*2);
		hudWaitingText.setColor(ColorRGBA.Black);
		hudWaitingText.setText("Ninjas in waiting");
		hudWaitingText.setLocalTranslation(10,10+hudWaitingText.getLineHeight(),0);
		guiNode.attachChild(hudWaitingText);
		
		Box hudWaitingBox = new Box(5f,10f,10f);
		hudWaitingGeom = new Geometry("Waiting", hudWaitingBox);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create
		mat.setColor("Color", new ColorRGBA(0.4f,0.76f,0.38f,0.8f));
		hudWaitingGeom.setMaterial(mat); // set the cube's material
		guiNode.attachChild(hudWaitingGeom);
		
		TreeSet<String> droneCompanies = new TreeSet<String>();
		for(Drone d:drones.keySet()){
			droneCompanies.add(d.getCompanyName());
		}
		
		hudCompanyDelivery = new TreeMap<String, Pair<BitmapText,Geometry>>();
		hudCompanyFlying = new TreeMap<String, Pair<BitmapText,Geometry>>();
		hudCompanyDead = new TreeMap<String, Pair<BitmapText,Geometry>>();
		for(String company:droneCompanies){
			BitmapText hudCompanyDeliveryText = new BitmapText(consoleFont,false);
			hudCompanyDeliveryText.setSize(consoleFont.getCharSet().getRenderedSize()*2);
			hudCompanyDeliveryText.setColor(ColorRGBA.Black);
			hudCompanyDeliveryText.setText("");
			guiNode.attachChild(hudCompanyDeliveryText);
			
			Box hudCompanyDeliveryBox = new Box(5f, 10f, 10f);
			Geometry hudCompanyDeliveryGeom = new Geometry("Waiting", hudCompanyDeliveryBox);
			mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create
			mat.setColor("Color", new ColorRGBA(0.4f,1.0f,0.38f,0.2f));
			hudCompanyDeliveryGeom.setMaterial(mat); // set the cube's material
			guiNode.attachChild(hudCompanyDeliveryGeom);
			
			hudCompanyDelivery.put(company,new Pair<BitmapText,Geometry>(hudCompanyDeliveryText,hudCompanyDeliveryGeom));
			
			BitmapText hudCompanyFlyingText = new BitmapText(consoleFont,false);
			hudCompanyFlyingText.setSize(consoleFont.getCharSet().getRenderedSize()*2);
			hudCompanyFlyingText.setColor(ColorRGBA.Black);
			hudCompanyFlyingText.setText("");
			guiNode.attachChild(hudCompanyFlyingText);
			
			Box hudCompanyFlyingBox = new Box(5f, 10f, 10f);
			Geometry hudCompanyFlyingGeom = new Geometry("Waiting", hudCompanyFlyingBox);
			mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create
			mat.setColor("Color", new ColorRGBA(1.0f,0.4f,0.38f,0.2f));
			hudCompanyFlyingGeom.setMaterial(mat); // set the cube's material
			guiNode.attachChild(hudCompanyFlyingGeom);
			
			hudCompanyFlying.put(company,new Pair<BitmapText,Geometry>(hudCompanyFlyingText,hudCompanyFlyingGeom));
			
			BitmapText hudCompanyDeadText = new BitmapText(consoleFont,false);
			hudCompanyDeadText.setSize(consoleFont.getCharSet().getRenderedSize()*2);
			hudCompanyDeadText.setColor(ColorRGBA.Black);
			hudCompanyDeadText.setText("");
			guiNode.attachChild(hudCompanyDeadText);
			
			Box hudCompanyDeadBox = new Box(5f, 10f, 10f);
			Geometry hudCompanyDeadGeom = new Geometry("Waiting", hudCompanyDeadBox);
			mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create
			mat.setColor("Color", new ColorRGBA(0.0f,0.0f,0.0f,0.2f));
			hudCompanyDeadGeom.setMaterial(mat); // set the cube's material
			guiNode.attachChild(hudCompanyDeadGeom);
			
			hudCompanyDead.put(company,new Pair<BitmapText,Geometry>(hudCompanyDeadText,hudCompanyDeadGeom));
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
		
		setDoneWithInit(true);
	}

	/* Use the main event loop to trigger repeating actions. */
	@Override
	public void simpleUpdate(float tpf) {
		
		int numNinjasWaiting = 0;
		Map<String, Integer> droneDeliveries = new TreeMap<String,Integer>();
		Map<String, Integer> droneFlying = new TreeMap<String,Integer>();
		Map<String, Integer> droneDeaths = new TreeMap<String,Integer>();
		Map<String, Long> droneTotalWaitingTime = new TreeMap<String,Long>();
		
		Explosion.drawExplosions(tpf, speed);
		
		for (Entry<Person, Spatial> personEntry : people.entrySet()) {
			Person person = personEntry.getKey();
			
			switch (personEntry.getKey().getState()) {
			case WAITING: {
				numNinjasWaiting++;
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
				droneFlying.merge(person.getDeliveryCompany(), 1, (v1, v2) -> {return v1+v2;});
	
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
				droneFlying.merge(person.getDeliveryCompany(), 1, (v1, v2) -> {return v1+v2;});
	
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
				droneFlying.merge(person.getDeliveryCompany(), 1, (v1, v2) -> {return v1+v2;});
			}
			break;
			case ARRIVED: {
				personEntry.getValue().setLocalTranslation(latLong2Transform(person.getPosition().getLatitude(),
						person.getPosition().getLongitude(), person.getPosition().getHeight()));
				
				droneDeliveries.merge(person.getDeliveryCompany(), 1, (v1, v2) -> {return v1+v2;});
				droneTotalWaitingTime.merge(person.getDeliveryCompany(), person.getEndTransitTime()-person.getStartTransitTime(), (v1, v2) -> {return v1+v2;});
			}
			break;
			case DYING: {
				personEntry.getValue().setLocalTranslation(latLong2Transform(person.getPosition().getLatitude(),
						person.getPosition().getLongitude(), person.getPosition().getHeight()));
			}
			break;
			case QUARANTINED:
			case DEAD: {
				personEntry.getValue().setLocalTranslation(latLong2Transform(person.getPosition().getLatitude(),
						person.getPosition().getLongitude(), person.getPosition().getHeight()));
				
				droneDeaths.merge(person.getDeliveryCompany(), 1, (v1, v2) -> {return v1+v2;});
			}
			break;
			default:
				throw new IllegalArgumentException("Unhandled Drone State: " + personEntry.getKey().getState());
			}
		}
		
		hudWaitingGeom.setLocalScale(numNinjasWaiting, 1, 1);
		hudWaitingGeom.setLocalTranslation(450+(numNinjasWaiting/2.0f*10.0f),10+hudWaitingText.getLineHeight()/2,0);
		
		int i = 0;
		for(Entry<String, Pair<BitmapText, Geometry>> p: hudCompanyDelivery.entrySet()){
			i++;
			Geometry hudCompanyDeliveryGeom = p.getValue().getValue();
			BitmapText hudCompanyDeliveryText = p.getValue().getKey();
			Integer count = droneDeliveries.get(p.getKey());
			if(count == null){
				count = 0;
			}
			
			Long totalWait = droneTotalWaitingTime.get(p.getKey());
			if(totalWait == null){
				totalWait = 0L;
			}
			String formattedCompany = String.format("%-20s %09d",p.getKey().subSequence(0, Math.min(p.getKey().length(),20)),(count==0)?totalWait:totalWait/count);
			hudCompanyDeliveryText.setText(formattedCompany);
			hudCompanyDeliveryText.setLocalTranslation(10,10+(25.0f*i)+hudCompanyDeliveryText.getLineHeight(),0);
			
			hudCompanyDeliveryGeom.setLocalScale(count, 1, 1);
			float deliveredWidth = (count/2.0f*10.0f);
			hudCompanyDeliveryGeom.setLocalTranslation(450+deliveredWidth,10+(25*i)+hudCompanyDeliveryText.getLineHeight()/2.0f,0);
			
			/* Now tack on the in flight bars */
			Integer flyingCount = droneFlying.get(p.getKey()); 
			if(flyingCount == null){
				flyingCount = 0;
			}
			
			Geometry hudCompanyFlyingGeom = hudCompanyFlying.get(p.getKey()).getValue();
			BitmapText hudCompanyFlyingText = hudCompanyFlying.get(p.getKey()).getKey();
			
			hudCompanyFlyingGeom.setLocalScale(flyingCount, 1, 1);
			float flyingWidth = (flyingCount/2.0f*10.0f);
			hudCompanyFlyingGeom.setLocalTranslation(450+2*deliveredWidth+flyingWidth,10+(25*i)+hudCompanyFlyingText.getLineHeight()/2.0f,0);
			
			/* Now tack on the death bars */
			Integer deathCount = droneDeaths.get(p.getKey()); 
			if(deathCount == null){
				deathCount = 0;
			}
			
			Geometry hudCompanyDeathGeom = hudCompanyDead.get(p.getKey()).getValue();
			BitmapText hudCompanyDeathText = hudCompanyDead.get(p.getKey()).getKey();
			
			hudCompanyDeathGeom.setLocalScale(deathCount, 1, 1);
			float deadWidth = deathCount/2.0f*10.0f;
			hudCompanyDeathGeom.setLocalTranslation(450+2*deliveredWidth+2*flyingWidth+deadWidth,10+(25*i)+hudCompanyDeathText.getLineHeight()/2.0f,0);
			
		}
		
		
		for (Entry<Place, Spatial> placeEntry : places.entrySet()) {
			Place place = placeEntry.getKey();
			//Spatial baseNode = placeEntry.getValue();
			if(place.getWaitingToEmbark().size()!=0){
				for(Person p:place.getWaitingToEmbark()){
					if(!people.keySet().contains(p)){
						throw new RuntimeException("Someone was cloned:"+p);
					}
				}
			}
			
		}
		
		for (Entry<Drone, Node> droneEntry : drones.entrySet()) {
			Drone drone = droneEntry.getKey();
			Node baseNode = droneEntry.getValue();
			
			Spatial droneNode = baseNode.getChild("drone");
			Node particlesNode = (Node) baseNode.getChild("particles");
			switch (droneEntry.getKey().getState()) {
			case BEGIN: {
				particlesNode.detachAllChildren();
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
				fire.getParticleInfluencer().setVelocityVariation(0.3f);
				fire.setLocalTranslation(0, 0.2f, 0);
	
				// Match it to the drone
				particlesNode.attachChild(fire);
				fire.emitAllParticles();
			}
			break;
			case EXPLODING: {
				
				baseNode.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
						drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
	
				if(!explodingDrones.contains(drone.getName())){
					Explosion.boom(particlesNode, new Explosion(renderManager,assetManager));
					explodingDrones.add(drone.getName());
				}
			}
			break;
			case QUARANTINED: {
				baseNode.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
						drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
				
				if(!quarantinedDrones.contains(drone.getName())){
					quarantinedDrones.add(drone.getName());
					
					ParticleEmitter electricity = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 50);
					Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
					mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
					// point
					electricity.setShape(new EmitterPointShape(Vector3f.ZERO));
					electricity.setMaterial(mat_red);
					electricity.setParticlesPerSec(5f);
					electricity.setImagesX(2);
					electricity.setImagesY(2); // 2x2 texture animation
					electricity.setStartColor(new ColorRGBA(0.2f, 0.2f, 0f, 0.8f));
					electricity.setEndColor(new ColorRGBA(1.0f, 1.0f, 0f, 0.3f));
					electricity.getParticleInfluencer().setInitialVelocity(new Vector3f(0f, 0.5f, 0f));
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
			}
			break;
			case DYING:
			case DEAD: {
				baseNode.setLocalTranslation(latLong2Transform(drone.getPosition().getLatitude(),
						drone.getPosition().getLongitude(), drone.getPosition().getHeight()));
				
				if(!smokingDrones.contains(drone.getName())){
					smokingDrones.add(drone.getName());
						
					ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Triangle, 300);
					emit.setParticlesPerSec(20);
				    emit.setGravity(0, 0, 0);
				    emit.setVelocityVariation(0.1f);
			        emit.setLowLife(1);
			        emit.setHighLife(3);
			        emit.setInitialVelocity(new Vector3f(0, .5f, 0));
				    emit.setImagesX(15);
				    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
				    mat.setTexture("Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
				    emit.setMaterial(mat);
				    emit.setStartSize(0.01f);
				    emit.setEndSize(0.2f);
				    emit.setStartColor(new ColorRGBA(0.0f, 0f, 0f, 0.4f));
					emit.setEndColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
			        
					// Match it to the drone
					particlesNode.attachChild(emit);
					emit.emitAllParticles();
				}
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
				electricity.setParticlesPerSec(5f);
				electricity.setImagesX(2);
				electricity.setImagesY(2); // 2x2 texture animation
				electricity.setStartColor(new ColorRGBA(0.2f, 0f, 1f, 0.8f));
				electricity.setEndColor(new ColorRGBA(0f, 0f, 1f, 0.3f));
				electricity.getParticleInfluencer().setInitialVelocity(new Vector3f(0f, 0.5f, 0f));
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
				droneNode.rotate(0, 0.1f * tpf, 0);
				particlesNode.detachAllChildren();
			}
			break;
			case IGNORED: {
				droneNode.rotate(0.1f*tpf, 0.1f * tpf, 0.1f*tpf);
				particlesNode.detachAllChildren();
			}
			break;
			default:
				throw new IllegalArgumentException("Unhandled Drone State: " + droneEntry.getKey().getState());
			}
		}
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
		}
		else if (p.getState().equals(PersonState.DYING)) {
			channel.setAnim("Death2", 0.05f);
			channel.setLoopMode(LoopMode.DontLoop);
		}
		else if (p.getState().equals(PersonState.DEAD)) {
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
			StringBuffer sb = new StringBuffer("");
			sb.append(errorMsg+"\n");
			for(StackTraceElement s: t.getStackTrace()){
				sb.append(s.toString());
			}
			simulator.end(sb.toString());
			simulator = null;
		}
		super.handleError(errorMsg, t);

	}

	@Override
	public void destroy() {
		if (this.simulator != null) {
			this.simulator.end("Simulator ended from the visualization destroy call");
			this.simulator = null;
		}
		super.destroy();
	}

	public void launch() {
		
		//Start the jMonkeyEngine SimpleApplication
		this.start();
		
		//Wait for the initialization to end in a different thread
		while(!getDoneWithInit()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		//Launch the simulator
		if (this.simulator != null) {
			this.simulator.start();
		}
	}

	public static void main(String[] args) {
		Visualizer app = new Visualizer();
		app.start(); 
	}

}
