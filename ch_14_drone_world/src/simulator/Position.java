package simulator;

public class Position implements Comparable<Position>{
	
	private double latitude;
	private double longitude;
	private double height;


	public void checkLongitude(double longitude) {
		if((longitude < -180.0) || (longitude > 180.0)){
			throw new IllegalArgumentException("longitude out of bounds (-180,180):"+longitude);
		}
	}

	public void checkLatitude(double latitude) {
		if((latitude < -90.0) || (latitude > 90.0)){
			throw new IllegalArgumentException("latitude out of bounds (-90,90):"+latitude);
		}
	}
	
	public Position(double latitude, double longitude,double height) {
		setLatitude(latitude);
		setLongitude(longitude);
		setHeight(height);
	}

	
	public Position(Position position){
		this(position != null? position.getLatitude():0,position!=null?position.getLongitude():0,position!=null?position.getHeight():0);
	}

	public double getLatitude() {
		return latitude;
	}
	
	void setLatitude(double latitude) {
		checkLatitude(latitude);
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	void setLongitude(double longitude) {
		checkLongitude(longitude);
		this.longitude = longitude;
	}
	
	public double getHeight() {
		return height;
	}
	
	void setHeight(double height) {
		this.height = height;
	}
	
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Position))
			return false;
		Position other = (Position) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	@Override
	/**
	 * West to East, then North to South, then low to high
	 */
	public int compareTo(Position other) {
		if (this == other)
			return 0;
		if (other == null)
			return 1;
		
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return Double.compare(longitude, other.longitude);
		
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return Double.compare(other.latitude,latitude);
		
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return Double.compare(height, other.height);
		
		return 0;
	}

	
	public String toString(){
		StringBuffer out = new StringBuffer();
		out.append("("+latitude+","+longitude+","+height+")");
		return out.toString();
	}

	
	
	

}
