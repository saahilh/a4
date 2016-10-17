package lab4localization;

import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	private final double sensorDistance = 8.5;
	private final int ROTATION_SPEED = 35;
	private final int driveSpeed = 45;
	private final int lineDetectionValue = 40;
	private final double tileSize = 30.48;
	private Navigation nav;
	double blackLines[] = new double[4];

			
	//TODO: account for distance of light sensor from the origin point of the robot
	
	public LightLocalizer(Odometer odo, Navigation nav, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.nav = nav;
	}
	
	public void doLocalization() {
		//TODO: write doLocalization() method for LightLocalizer
		
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		odo.setPosition(new double [] {0,0,0}, new boolean[] {false,false,true});
		
		while(!blackLineDetected()){											// Travel to point on grid
			nav.setSpeeds(driveSpeed, driveSpeed);
		}
		Sound.beep();
		odo.setPosition(new double [] {-sensorDistance,0,0}, new boolean[] {true,false,false});
		nav.travelTo(-sensorDistance/2, 0, driveSpeed);
		nav.rotate(-ROTATION_SPEED);
		nav.turnTo(90, true);
		odo.setPosition(new double [] {-sensorDistance/2,0,90}, new boolean[] {true,true,true});
		
		while(!blackLineDetected()){
			nav.setSpeeds(driveSpeed, driveSpeed);
		}
		Sound.beep();
		
		nav.setSpeeds(0, 0);
		odo.setPosition(new double [] {-sensorDistance,-sensorDistance,90}, new boolean[] {true,true,false});
		nav.travelTo(-sensorDistance, -sensorDistance/2, driveSpeed);
		nav.turnTo(180, true);
		odo.setPosition(new double [] {-sensorDistance/2,-sensorDistance/2,180}, new boolean[] {false,true,true});  //positioned to start trig...unsure if theta should be -90 or 0
		
		//determineing theta y and x theta
		
       //rotate until we detect the y axis
		nav.rotate(ROTATION_SPEED);
		
		for (int i = 0; i < blackLines.length; i++){
			while(!blackLineDetected()){
				nav.rotate(ROTATION_SPEED);
			}
			Sound.beep();
			blackLines[i] = odo.getTheta();
			
			//turn off of black line so as not to capture the same line twice
			while(odo.getTheta() > blackLines[i] - 15*Math.PI/180){
				Sound.buzz();
				nav.rotate(ROTATION_SPEED);
			}
		}
		
		double thetaX = blackLines[2] - blackLines[0];
		double thetaY = blackLines[3] - blackLines[1];
		
		double trueX = -sensorDistance* Math.cos(thetaX/2);
		double trueY = -(sensorDistance/2)*Math.cos(thetaY/2);

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

}
