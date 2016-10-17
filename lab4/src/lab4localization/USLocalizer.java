package lab4localization;

import java.util.Arrays;

import lejos.hardware.Sound;
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
		
		rotateWhile(4, US_MAX, false, ROTATION_SPEED); //rotate till no longer facing a wall
		
		rotateWhile(4, measuredDistance + NOISE_MARGIN, true, ROTATION_SPEED);  //rotate to beginning of noise margin
		angleTemp1 = odo.getTheta();							   				//robot has entered the noise margin, get first temp angle
		rotateWhile(4, measuredDistance - NOISE_MARGIN, true, ROTATION_SPEED); 	//rotate until robot has exited noise margin
		angleTemp2 = odo.getTheta();                             				//get second temp angle 
		
		angleA = (angleTemp1 + angleTemp2)/2;                    				//calculate first falling edge 
		
		rotateWhile(4, measuredDistance + NOISE_MARGIN, false, -ROTATION_SPEED);//rotate other way out of noise margin
		
		rotateWhile(4, measuredDistance + NOISE_MARGIN, true, -ROTATION_SPEED); //rotate until the robot reaches the noise margin
		angleTemp1 = odo.getTheta();                           					//robot has reached the noise margin, take first temp angle 
		rotateWhile(5, measuredDistance - NOISE_MARGIN, true, -ROTATION_SPEED); //rotate until exit noise margin, record second temp angle
		angleTemp2 = odo.getTheta();
		
		angleB = (angleTemp1 + angleTemp2)/2;                 					//calculate second falling edge 
		
		turnActualZero(angleA, angleB);                      					//turn to correct zero positioning 
		
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
		
		rotateWhile(4, US_MAX, true, ROTATION_SPEED);
		
		rotateWhile(4, measuredDistance - NOISE_MARGIN, false, ROTATION_SPEED);
		angleTemp1 = odo.getTheta();
		rotateWhile(4, measuredDistance + NOISE_MARGIN, false, ROTATION_SPEED);
		angleTemp2 = odo.getTheta();
		
		angleA = (angleTemp1 + angleTemp2)/2;
		
		rotateWhile(4, measuredDistance - NOISE_MARGIN, true, -ROTATION_SPEED);
		
		rotateWhile(4, measuredDistance - NOISE_MARGIN, false, -ROTATION_SPEED);
		angleTemp1= odo.getTheta();
		rotateWhile(4, measuredDistance + NOISE_MARGIN, false, -ROTATION_SPEED);
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
		if(!(angleA>0 || angleA <= 0)) Sound.beep();
		if(!(angleB>0 || angleB <= 0)) Sound.buzz();
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
				e.printStackTrace();
			}
		}
		Arrays.sort(sampleData);
		return sampleData[(int) Math.floor(sampleData.length/2)];
	}
	/**
	 * @param filterSampleSize - size of filter to be used for while loop
	 * @param toCompare - the value to be compared with getFilteredData(filterSampleSize)
	 * @param greaterOrLess - compare greater than or less than? true = >=, false = <
	 * @param rotateSpeed - speed of rotation; can be positive or negative
	 * 
	 * rotateWhile() - Compares the filter result with the value in @param toCompare using the sign indicated by @param greaterOrLess. While true continues to rotate at @param rotateSpeed.
	 */
	private void rotateWhile(int filterSampleSize, int toCompare, boolean greaterOrLess, int rotateSpeed){
		if(greaterOrLess == true){
			while(getFilteredData(filterSampleSize) >= toCompare){
				nav.rotate(rotateSpeed);
			}
		}
		else if (greaterOrLess == false){
			while(getFilteredData(filterSampleSize) < toCompare){
				nav.rotate(rotateSpeed);
			}
		}
		throw new Error("Invalid value for greaterOrLess in rotateWhile method.");
	}
	
}

