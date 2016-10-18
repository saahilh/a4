package lab4localization;

import java.util.Arrays;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 60;
	
	/**
	 * @param {int} US_MAX - max reading of ultrasonic sensor; used to assume no object is in front of the robot
	 * @param {double} ANGLE_CORR_LOW, ANGLE_CORR_HI - parameters for correcting robot rotation wrap-around
	 */	
	
	public static int US_MAX = 40;
	public static double ANGLE_CORR_LOW = 45, ANGLE_CORR_HI = 235;
	private final int measuredDistance = 30;
	public final double	us_SensorDistanceFromOrigin = 4.0;
	//TODO: test ANGLE_CORR_LOW, ANGLE_CORR_HI, change if required; also modify US_MAX if needed
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
		double angleA, angleB, angleCorrection, turnAngle;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			nav.rotate(-ROTATION_SPEED);
			
			while(true){
				if((int)getFilteredData() > 30){
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then calcuate the angle
			
			while(true){
				if((int)getFilteredData() < measuredDistance){
					Sound.beep();
					angleA = odo.getTheta();
					nav.setSpeeds(0, 0);
					break;
				}
			}
			
			// switch direction and wait until it sees no wall
			
			nav.rotate(ROTATION_SPEED);
			
			while(true){
				if((int)getFilteredData() > measuredDistance){
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then calculate the angle
			
			while(true){
				if((int)getFilteredData() < 30){
					Sound.beep();
					angleB = odo.getTheta();
					nav.setSpeeds(0, 0);
					break;
				}
			}
			
			if(angleA > angleB){
				angleCorrection = 225 - (angleA + angleB) / 2;
			}
			else{
				angleCorrection = 45 - (angleA + angleB) / 2;
			}
			
			// wrap angle to positive value
			if(angleCorrection < 0) angleCorrection += 360;
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angleCorrection}, new boolean [] {true, true, true});
			
			// adjust angle
			turnAngle = angleCorrection + 60;
			
			// wrap angle to less than 360
			if(turnAngle > 360) turnAngle -= 360;
			
			// rotate
			if(turnAngle < 0) nav.turnTo(turnAngle + 360, true);
			else nav.turnTo(turnAngle, true);
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true}); 		//reset odometer after calibration 
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			// rotate the robot until it sees no wall
						nav.rotate(-ROTATION_SPEED);
						
						while(true){
							if((int)getFilteredData() < measuredDistance){
								break;
							}
						}
						
						// keep rotating until the robot sees a wall, then latch the angle
						
						while(true){
							if((int)getFilteredData() > measuredDistance){
								Sound.beep();
								angleA = odo.getTheta();
								nav.setSpeeds(0, 0);
								break;
							}
						}
						
						// switch direction and wait until it sees no wall
						
						nav.rotate(ROTATION_SPEED);
						
						while(true){
							if((int)getFilteredData() < measuredDistance){
								break;
							}
						}
						
						// keep rotating until the robot sees a wall, then latch the angle
						
						while(true){
							if((int)getFilteredData() > measuredDistance){
								Sound.beep();
								angleB = odo.getTheta();
								nav.setSpeeds(0, 0);
								break;
							}
						}
						
						if(angleA > angleB){
							angleCorrection = 225 - (angleA + angleB) / 2;
						}
						else{
							angleCorrection = 45 - (angleA + angleB) / 2;
						}
						
						// wrap angle to positive value
						if(angleCorrection < 0) angleCorrection += 360;
						
						// angleA is clockwise from angleB, so assume the average of the
						// angles to the right of angleB is 45 degrees past 'north'
						
						// update the odometer position (example to follow:)
						odo.setPosition(new double [] {0.0, 0.0, angleCorrection}, new boolean [] {true, true, true});
						
						// adjust angle
						turnAngle = angleCorrection + 60;
						
						// wrap angle to less than 360
						if(turnAngle > 360) turnAngle -= 360;
						
						// rotate
						if(turnAngle < 0) nav.turnTo(turnAngle + 360, true);
						else nav.turnTo(turnAngle, true);
						
						odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true}); 		//reset odometer after calibration 
					
		}
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
		distance *= 100;
		if(distance > 255) distance = 255;
		
		return distance;
	}
}

