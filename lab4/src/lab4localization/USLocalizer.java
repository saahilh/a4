package lab4localization;

import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 30;
	public static int US_MAX = 255;
	private int empty = 420;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private Navigation nav;
	
	public USLocalizer(Odometer odo, Navigation navigation,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.nav = navigation;
	}
	
	public void doLocalization() { 		
		if (locType == LocalizationType.FALLING_EDGE) {
			fallingEdge();
		} else {
			risingEdge();
		}
	}
	
	/* fallingEdge(): to be called when starting with US facing away from a wall
	 * rotates till wall is found, sets this as angleA, reverses direction till
	 * wall is found, sets this as angleB */
	
	private void fallingEdge(){ 
		double angleA, angleB;
		
		while(getFilteredData() >= US_MAX){
			nav.rotate(ROTATION_SPEED);
		}
		angleA = odo.getTheta();
		
		while(getFilteredData() < US_MAX){
			nav.rotate(-ROTATION_SPEED);
		}
		
		while(getFilteredData() >= US_MAX){
			nav.rotate(-ROTATION_SPEED);
		}
		angleB = odo.getTheta();
	
		turnToZero(angleA, angleB);

		odo.reset(); //TODO: write this method
	}
	
	/* risingEdge(): to be called when starting with US facing towards a wall
	 * rotates till end of wall is found, sets this as angleB, reverses direction 
	 * till other end of wall is found, sets this as angleA */
	
	private void risingEdge(){
		double angleA, angleB;
		
		while(getFilteredData() < US_MAX){
			nav.rotate(ROTATION_SPEED);
		}
		angleB = odo.getTheta();
		
		while(getFilteredData() >= US_MAX){
			nav.rotate(-ROTATION_SPEED);
		}
		
		while(getFilteredData() < US_MAX){
			nav.rotate(-ROTATION_SPEED);
		}
		angleA = odo.getTheta();
	
		turnToZero(angleA, angleB);
	
		odo.reset();
	}
	
	//turns towards zero (average point between the two input angles) 
	//TODO: figure out how to implement this method correctly for this odometer
	private void turnToZero(double angleA, double angleB){
		nav.turnTo(angleB + (angleA - angleB) / 2, true);
	}
	
	//TODO: still need to implement a way to deal with noise in US readings
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
				
		return distance;
	}

}
