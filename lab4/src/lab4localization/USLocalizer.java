package lab4localization;

import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 30;
	public static int US_MAX = 255;
	public static double ANGLE_CORR_LOW = 45, ANGLE_CORR_HI = 225;
	//parameters for correcting the angle the heading turns to, to account for wraparound when robot is turning
	//must find better values for these through testing

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
	
		turnActualZero(angleA, angleB);

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
	
		turnActualZero(angleA, angleB);
	
		odo.reset();
	}
	
	//turns to the heading account for the heading correction angle
	private void turnActualZero(double angleA, double angleB){ 
		if(angleA <= angleB){
			setHeadingRealZero(angleA, angleB, ANGLE_CORR_LOW);
		}
		else if(angleA >= angleB) {
			setHeadingRealZero(angleA, angleB, ANGLE_CORR_HI);
		}
	}
	
	// set heading to calculated zero (average point between the two input angles) 
	private void setHeadingRealZero(double angleA, double angleB, double angleCorrection){
		nav.turnTo(angleCorrection - (angleA + angleB) / 2, true);
	}
	
	//TODO: still need to implement a way to deal with noise in US readings
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
				
		return distance;
	}

}
