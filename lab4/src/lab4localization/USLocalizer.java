package lab4localization;

import java.util.Arrays;

import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 30;
	
	/**
	 * @param {int} US_MAX - max reading of ultrasonic sensor; used to assume no object is in front of the robot
	 * @param {double} ANGLE_CORR_LOW, ANGLE_CORR_HI - parameters for correcting robot rotation wrap-around
	 * @param {int}	NOISE_MARGIN - margin for errors caused by noise in US readings
	 */	
	
	public static int US_MAX = 40;
	public static double ANGLE_CORR_LOW = 45, ANGLE_CORR_HI = 235;
	public static int NOISE_MARGIN = 1;
	public final double	us_SensorDistanceFromOrigin = 4.0;
	private static int measuredDistance = 35;
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
		if (locType == LocalizationType.FALLING_EDGE) {
			fallingEdge();
		} 
		else if (locType == LocalizationType.RISING_EDGE){
			risingEdge();
		}
		else{
			throw new Error("Invalid localization type selected");
		}
	}
	
	/* 
	 * fallingEdge(): to be called when starting with US facing away from a wall
	 * rotates till wall is found, sets this as angleA, reverses direction till
	 * wall is found, sets this as angleB. finally, turns towards the true (0, 0)
	 */
	
	private void fallingEdge(){ 
		double angleA, angleB, angleTemp1, angleTemp2;
		
		while(getFilteredData(4) >= measuredDistance + NOISE_MARGIN){        //Rotate to beginning of noise margin
			nav.rotate(ROTATION_SPEED);
		}
		
		angleTemp1 = odo.getTheta();							   // Robot has entered the noise margin, get first temp angle
		
		while(getFilteredData(4) >= measuredDistance - NOISE_MARGIN){       // Rotate until robot has exited noise margin
			nav.rotate(ROTATION_SPEED);	
		}
		
		angleTemp2 = odo.getTheta();                              // Get second temp angle 
		angleA = (angleTemp1 + angleTemp2)/2;                    //Calculate first falling edge 
		
		while(getFilteredData(4) < measuredDistance + NOISE_MARGIN){      //Rotate other way out of noise margin
			nav.rotate(-ROTATION_SPEED);
		}
		
		while(getFilteredData(4) >= measuredDistance + NOISE_MARGIN){    //Rotate until the robot reaches the noise margin
			nav.rotate(-ROTATION_SPEED);
		}

		angleTemp1 = odo.getTheta();                           //Robot has reached the noise margin, take first temp angle 
		
		while(getFilteredData(5) >= measuredDistance - NOISE_MARGIN){    //Rotate until exit noise margin, record second temp angle
			nav.rotate(-ROTATION_SPEED);	
		}
		angleTemp2 = odo.getTheta();
		angleB = (angleTemp1 + angleTemp2)/2;                 // Calculate second falling edge 
		
		turnActualZero(angleA, angleB);                      // Turn to correct zero positioning 
		odo.reset();
	}
	
	/* 
	 * risingEdge(): to be called when starting with US facing towards a wall
	 * rotates till end of wall is found, sets this as angleB, reverses direction 
	 * till other end of wall is found, sets this as angleA. finally, turns towards
	 * true (0, 0)
	 */
	
	private void risingEdge(){
		double angleA, angleB, angleTemp1, angleTemp2;
		
		while(getFilteredData(4) < measuredDistance - NOISE_MARGIN){
			nav.rotate(ROTATION_SPEED);
		}
		angleTemp1 = odo.getTheta();
		
		while(getFilteredData(4) <= measuredDistance + NOISE_MARGIN){
			nav.rotate(ROTATION_SPEED);
		}
		angleTemp2 = odo.getTheta();
		angleA = (angleTemp1 + angleTemp2)/2;
		
		while(getFilteredData(4) > measuredDistance - NOISE_MARGIN){
			nav.rotate(-ROTATION_SPEED);
		}
		
		while(getFilteredData(4) <= measuredDistance - NOISE_MARGIN){
			nav.rotate(-ROTATION_SPEED);
		}
		angleTemp1= odo.getTheta();
		
		while(getFilteredData(4) <= measuredDistance + NOISE_MARGIN){
			nav.rotate(-ROTATION_SPEED);
		}
		angleTemp2 = odo.getTheta();
		angleB = (angleTemp1 + angleTemp2)/2;
	
		turnActualZero(angleA, angleB);
		
		odo.reset();
	}
	
	/*	
	 * Turns to the heading calculated to be the actual 0 position 
	 * of the grid the robot is on, accounting for the heading 
	 * wrap-around correction angle.	
	 */
	private void turnActualZero(double angleA, double angleB){ 
		if(angleA <= angleB){
			nav.turnTo(ANGLE_CORR_LOW - (angleA + angleB)/2, true);
		}
		else if(angleA >= angleB) {
			nav.turnTo(ANGLE_CORR_HI - (angleA + angleB)/2, true);
		}
		throw new Error("Actual zero calculation error.");
	}
	
	/**	@return - filtered data */
	private float getFilteredData(int sampleSize){

		float sampleData[] = new float[sampleSize];

		for(int index = 0 ; index < sampleData.length; index++)
		{
			usSensor.fetchSample(usData, 0);

			if(usData[0]*100 > US_MAX)
				usData[0] = US_MAX;

			sampleData[index] = usData[0]*100;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Arrays.sort(sampleData);
		return sampleData[(int) Math.floor(sampleData.length/2)];
	}
}

