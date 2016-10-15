package lab4localization;

import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
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
	}

}
