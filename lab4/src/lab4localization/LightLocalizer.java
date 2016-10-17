package lab4localization;

import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	private final double[]  calibrationCoordinates = {-3.0,-3.0};
	private final double sensorDistance = 4.0;
	private final int ROTATION_SPEED = 25;
	private final int driveSpeed = 30;
	private final int lineDetectionValue = 20;
	private Navigation nav;
	//TODO: account for distance of light sensor from the origin point of the robot
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}
	
	public void doLocalization() {
		//TODO: write doLocalization() method for LightLocalizer
		
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees

		while(!blackLineDetected()){											// Travel to point on grid
			nav.setSpeeds(driveSpeed, driveSpeed);
		}
		
		nav.setSpeeds(0, 0);
		odo.setPosition(new double [] {-sensorDistance,0,0}, new boolean[] {true,true,true});
		nav.turnTo(-90, true);
		
		while(!blackLineDetected()){
			nav.setSpeeds(driveSpeed, driveSpeed);
		}
		nav.goForward(sensorDistance/2);
		nav.setSpeeds(0, 0);
		odo.setPosition(new double [] {-sensorDistance,-sensorDistance/2,-90}, new boolean[] {true,true,true});  //positioned to start trig...unsure if theta should be -90 or 0
		
		//determineing theta y
		
		while(!blackLineDetected()){        //rotate until we detect the y axis
		nav.rotate(ROTATION_SPEED);
		}
		
		double angleA = odo.getTheta();    //Rotate until we detect the -y axis
		
		for (int i = 1; i <= 1; i++){
			while(!blackLineDetected()){
				nav.rotate(ROTATION_SPEED);
			}	
		}
		
		double angleB = odo.getTheta();
		double angleY = angleB- angleA;    //calculate angle Y
		
		//calculating actual x
		
		double trueX = -sensorDistance*Math.cos(angleY/2);
		
		//rotate back to -90 degrees and start calculating our actual y value
		
		nav.turnTo(-90, true);
		
		//rotate counterclockwise until we detect the -x axis
		
		while(!blackLineDetected()){
			nav.rotate(-ROTATION_SPEED);
		}
				
		

		
	}
	private boolean blackLineDetected()
	{
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
