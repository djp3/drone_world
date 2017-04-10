package simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PositionTest {

	private static final double EPSILON = 1E-6; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPositionDoubleDouble() {
		try{
			new Position(0.0,0.0,0.0);
		}
		catch(RuntimeException e){
			fail("This shouldn't fail");
		}
		
		try{
			new Position(-90.0,-180.0,0.0);
		}
		catch(RuntimeException e){
			fail("This shouldn't fail");
		}
		
		try{
			new Position(90.0,180.0,0.0);
		}
		catch(RuntimeException e){
			fail("This shouldn't fail");
		}
		
		try{
			new Position(90.1,180.0,0.0);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
		
		try{
			new Position(90.0,180.1,0.0);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
		
		try{
			new Position(-90.1,180.0,0.0);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
		
		try{
			new Position(90.0,-180.1,0.0);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
	}
	

	@Test
	public void testGetLatitude() {
		Position position = new Position(0.0,0.0,0.0);
		assertTrue(Math.abs(position.getLatitude() - 0.0) < EPSILON);
		
		position = new Position(-90.0,-180.0,0.0);
		assertTrue(Math.abs(position.getLatitude() - -90.0) < EPSILON);
		
		position = new Position(90.0,180.0,0.0);
		assertTrue(Math.abs(position.getLatitude() - 90.0) < EPSILON);
	}

	@Test
	public void testGetLongitude() {
		Position position = new Position(0.0,0.0,0.0);
		assertTrue(Math.abs(position.getLongitude() - 0.0) < EPSILON);
		
		position = new Position(-90.0,-180.0,0.0);
		assertTrue(Math.abs(position.getLongitude() - -180.0) < EPSILON);
		
		position = new Position(90.0,180.0,0.0);
		assertTrue(Math.abs(position.getLongitude() - 180.0) < EPSILON);
	}
	
	@Test
	public void testGetHeight() {
		Position position = new Position(0.0,0.0,0.0);
		assertTrue(Math.abs(position.getHeight() - 0.0) < EPSILON);
		
		position = new Position(-90.0,-180.0,10.0);
		assertTrue(Math.abs(position.getHeight() - 10.0) < EPSILON);
		
		position = new Position(90.0,180.0,-10.0);
		assertTrue(Math.abs(position.getHeight() - -10.0) < EPSILON);
	}

	@Test
	public void testPositionPosition() {
		Position position1 = new Position(0.0,0.0,0.0);
		Position position = new Position(position1);
		assertTrue(Math.abs(position.getLatitude() - 0.0) < EPSILON);
		assertTrue(Math.abs(position.getLongitude() - 0.0) < EPSILON);
		assertTrue(Math.abs(position.getHeight() - 0.0) < EPSILON);
		
		position1 = new Position(-90.0,-180.0,-10.0);
		position = new Position(position1);
		assertTrue(Math.abs(position.getLatitude() - -90.0) < EPSILON);
		assertTrue(Math.abs(position.getLongitude() - -180.0) < EPSILON);
		assertTrue(Math.abs(position.getHeight() - -10.0) < EPSILON);
		
		position1 = new Position(90.0,180.0,10.0);
		position = new Position(position1);
		assertTrue(Math.abs(position.getLatitude() - 90.0) < EPSILON);
		assertTrue(Math.abs(position.getLongitude() - 180.0) < EPSILON);
		assertTrue(Math.abs(position.getHeight() - 10.0) < EPSILON);
	}
	
	@Test
	public void testPositionPositionNull() {
		Position position = new Position(null);
		assertTrue(Math.abs(position.getLatitude() - 0.0) < EPSILON);
		assertTrue(Math.abs(position.getLongitude() - 0.0) < EPSILON);
		assertTrue(Math.abs(position.getHeight() - 0.0) < EPSILON);
	}
	
	@Test
	public void testPositionPositionSet() {
		Position position = new Position(null);
		position.setLatitude(-50);
		position.setLongitude(-10.0);
		position.setHeight(10.0);
		
		assertTrue(Math.abs(position.getLatitude() - -50.0) < EPSILON);
		assertTrue(Math.abs(position.getLongitude() - -10.0) < EPSILON);
		assertTrue(Math.abs(position.getHeight() - 10.0) < EPSILON);
		
		try{
			position.setLatitude(91);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
		
		try{
			position.setLatitude(-91);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
		
		try{
			position.setLongitude(-181);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
		
		try{
			position.setLongitude(181);
			fail("This should fail");
		}
		catch(RuntimeException e){
		}
	}
	
	@Test
	public void testEquals() {
		Position position1 = new Position(0.0,0.0,0.0);
		Position position2 = new Position(position1);
		
		assertTrue(!position1.equals(null));
		assertTrue(position1.compareTo(null) > 0);
		assertTrue(!position1.equals("dummy"));
		
		assertTrue(position1.equals(position1));
		assertTrue(position1.compareTo(position1) == 0);
		assertTrue(position1.hashCode() == position1.hashCode());
		
		assertTrue(position1.equals(position2));
		assertTrue(position1.compareTo(position2) == 0);
		assertTrue(position2.compareTo(position1) == 0);
		assertTrue(position1.hashCode() == position2.hashCode());
		
		position1 = new Position(-45.0,-45.0,0.0);
		position2 = new Position(-45.0,0.0,0.0);
		assertTrue(!position1.equals(position2));
		assertTrue(position1.compareTo(position2) < 0);
		assertTrue(position2.compareTo(position1) > 0);
		assertTrue(position1.hashCode() != position2.hashCode());
		
		position1 = new Position(0.0,-45.0,0.0);
		position2 = new Position(-45.0,-45.0,0.0);
		assertTrue(!position1.equals(position2));
		assertTrue(position1.compareTo(position2) < 0);
		assertTrue(position2.compareTo(position1) > 0);
		assertTrue(position1.hashCode() != position2.hashCode());
		
		position1 = new Position(-45.0,45.0,10.0);
		position2 = new Position(-45.0,45.0,0.0);
		assertTrue(!position1.equals(position2));
		assertTrue(position1.compareTo(position2) > 0);
		assertTrue(position2.compareTo(position1) < 0);
		assertTrue(position1.hashCode() != position2.hashCode());
	}


}
