package lab4localization;

import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 30;
	
	/**
	 * @param {int} US_MAX - max reading of ultrasonic sensor; used to assume no object is in front of the robot
	 * @param {double} ANGLE_CORR_LOW, ANGLE_CORR_HI - parameters for correcting robot rotation wrap-around
	 * @param {int}	NOISE_MARGIN - margin for errors caused by noise in US readings
	 */	
	
	public static int US_MAX = 255;
	public static double ANGLE_CORR_LOW = 45, ANGLE_CORR_HI = 225;
	public static int NOISE_MARGIN = 1;
	//TODO: test ANGLE_CORR_LOW, ANGLE_CORR_HI, NOISE_MARGIN; change if required; also modify US_MAX if needed
	//TODO: account for distance of US sensor from the origin point of the robot
	
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
	
	/*  
	 * TODO: test fallingEdge() and risingEdge() in random starting orientations 
	 * in the starting square (but on the 45 degrees line going from one corner
	 * of it to the required (0,0) that must be found) and record data. determine 
	 * which of the two US methods is better (i.e., lower standard deviation) and 
	 * use that for the rest of the lab
	 */
	
	public void doLocalization() {
		double zeroHeading;
		if (locType == LocalizationType.FALLING_EDGE) {
			zeroHeading = fallingEdge();
		} 
		else if (locType == LocalizationType.RISING_EDGE){
			zeroHeading = risingEdge();
		}
		else{
			throw new Error("Invalid localization type selected");
		}
		nav.turnTo(zeroHeading, true);
	}
	
	/* 
	 * fallingEdge(): to be called when starting with US facing away from a wall
	 * rotates till wall is found, sets this as angleA, reverses direction till
	 * wall is found, sets this as angleB. finally, turns towards the true (0, 0)
	 */
	
	private double fallingEdge(){ 
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
	
		return getActualZero(angleA, angleB);
	}
	
	/* 
	 * risingEdge(): to be called when starting with US facing towards a wall
	 * rotates till end of wall is found, sets this as angleB, reverses direction 
	 * till other end of wall is found, sets this as angleA. finally, turns towards
	 * true (0, 0)
	 */
	
	private double risingEdge(){
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
	
		return getActualZero(angleA, angleB);
	}
	
	/*	
	 * Turns to the heading calculated to be the actual 0 position 
	 * of the grid the robot is on, accounting for the heading 
	 * wrap-around correction angle.	
	 */
	private double getActualZero(double angleA, double angleB){ 
		if(angleA <= angleB){
			return ANGLE_CORR_LOW - (angleA + angleB) / 2;
		}
		else if(angleA > angleB) {
			return ANGLE_CORR_HI - (angleA + angleB) / 2;
		}
		throw new Error("Actual zero calculation error.");
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
