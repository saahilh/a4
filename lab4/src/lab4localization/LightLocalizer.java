package lab4localization;

import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	private final double LS_DIST = 8.0;
	private final int ROTATION_SPEED = 60;
	private final int FORWARD_SPEED = 55;
	private final int lineDetectionValue = 40;
	private final double tileSize = 30.48;
	private Navigation nav;
	double blackLines[] = new double[4];

	public LightLocalizer(Odometer odo, Navigation nav, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.nav = nav;
	}
	
	public void doLocalization() {		
		odo.setPosition(new double [] {-1,-1,0}, new boolean[] {false,false,true});
		
		// travel to starting point on grid
		moveToGridStart();
		
		//determining x theta and y theta:
		findBlackLines();
		
		stop(); //stop the motors
		
		//calculate actual position and correct odometer values
		calculateTruePosition();
		
		nav.travelTo(0.0, 0.0, FORWARD_SPEED); //travel to the real 0,0 as the robot now knows its true position
		
		nav.turnTo(0, true); //turn to 0 degrees
		
		odo.reset();
	}
	
	private boolean blackLineDetected(){
		colorSensor.fetchSample(colorData, 0);

		//if we run over a black line, calculate and update odometer values
		if((int)(colorData[0]*100) < lineDetectionValue){
			return true;
		}
		else {
			return false;
		}
	}
	
	private void moveToGridStart(){
		while(!blackLineDetected()){
			nav.setSpeeds(FORWARD_SPEED, FORWARD_SPEED);
		}
		Sound.beep();
		stop();
		odo.setPosition(new double [] {-LS_DIST,0,0}, new boolean[] {true,true,true});
		nav.travelTo(-LS_DIST/2, 0, FORWARD_SPEED);
		nav.turnTo(95, true);
		odo.setPosition(new double [] {-LS_DIST/2,0,90}, new boolean[] {true,true,true});
		
		while(!blackLineDetected()){
			nav.setSpeeds(FORWARD_SPEED, FORWARD_SPEED);
		}
		Sound.beep();
		
		stop();
		odo.setPosition(new double [] {-LS_DIST/2,-LS_DIST,90}, new boolean[] {true,true,true});
		nav.travelTo(-LS_DIST/2, -LS_DIST/2, FORWARD_SPEED);
		nav.turnTo(185, true);
		odo.setPosition(new double [] {-LS_DIST/2,-LS_DIST/2,180}, new boolean[] {true,true,true});
	}

	/*	finds all the black lines and stores in the array blackLines[]	*/
	private void findBlackLines(){ 
		for (int i = 0; i < blackLines.length; i++){
			while(!blackLineDetected()){
				nav.rotate(ROTATION_SPEED);
			}
			Sound.beep();
			blackLines[i] = odo.getTheta();
			
			if(i == blackLines.length-1) break; //exits here once the last black line is detected
			
			//turn away from black line to avoid capturing the same line twice
			while(odo.getTheta() > blackLines[i] - 15*Math.PI/180){
				Sound.buzz();
				nav.rotate(ROTATION_SPEED);
			}
		}
	}
	
	private void calculateTruePosition(){
		double thetaX = blackLines[2] - blackLines[0];
		double thetaY = blackLines[3] - blackLines[1];
		
		double trueX = (-LS_DIST) * Math.abs(Math.cos(thetaX/2));
		double trueY = (-LS_DIST) * Math.abs(Math.cos(thetaY/2));
		
		odo.setPosition(new double[]{trueX, trueY, -1}, new boolean[]{true, true, false} );
	}
	
	private void stop(){
		nav.setSpeeds(0,0);
	}
	
}
