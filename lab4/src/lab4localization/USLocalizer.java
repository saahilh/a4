package lab4localization;

import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 30;
	
	/**
	 * @param {int} US_MAX - max reading of ultrasonic sensor; used to assume no object is in front of the robot
	 * @param {double} ANGLE_CORR_LOW, ANGLE_CORR_HI - parameters for correcting robot rotation wrap-around
	 */
	
	//TODO: test ANGLE_CORR_LOW, ANGLE_CORR_HI, change if required; also modify US_MAX if needed
		
	public static int US_MAX = 255;
	public static double ANGLE_CORR_LOW = 45, ANGLE_CORR_HI = 225;

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
	
	/* 
	 * fallingEdge(): to be called when starting with US facing away from a wall
	 * rotates till wall is found, sets this as angleA, reverses direction till
	 * wall is found, sets this as angleB. finally, turns to the calculated "actual" 
	 * (0, 0) point and sets this as the odometer start 
	 */
	
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
	
	/* 
	 * risingEdge(): to be called when starting with US facing towards a wall
	 * rotates till end of wall is found, sets this as angleB, reverses direction 
	 * till other end of wall is found, sets this as angleA. finally, turns to
	 * the calculated "actual" (0, 0) point and sets this as the odometer start
	 */
	
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
	
	/*	
	 * turns to the heading calculated to be the actual 0 position 
	 * of the grid the robot is on, accounting for the heading 
	 * wrap-around correction angle	
	 */
	private void turnActualZero(double angleA, double angleB){ 
		if(angleA <= angleB){
			nav.turnTo(ANGLE_CORR_LOW - (angleA + angleB) / 2, true);
		}
		else if(angleA >= angleB) {
			nav.turnTo(ANGLE_CORR_HI - (angleA + angleB) / 2, true);
		}
	}
	
	//TODO: implement a way to deal with noise in US readings
	
	/**	@return - filtered data */
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
		
		//TODO: implement data filter
		
		return distance;
	}

}
