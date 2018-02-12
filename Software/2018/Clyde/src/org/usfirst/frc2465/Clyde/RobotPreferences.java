package org.usfirst.frc2465.Clyde;

import edu.wpi.first.wpilibj.Preferences;

public class RobotPreferences {
    
    // PID Controller Settings
	// the good kine :D P:0.0022 I:0.000010 D:0.00001
	    
    static public double getAutoRotateP() {
        //return Preferences.getInstance().getDouble("AutoRotateP", 0.0002);
    	return 0.0025;
    }
    static public double getAutoRotateI() {
        //return Preferences.getInstance().getDouble("AutoRotateI", 0.00005);
    	return 0.0;
    }
    static public double getAutoRotateD() {
        //return Preferences.getInstance().getDouble("AutoRotateD", 0.00);
    	return 0.0;
    }
    static public double getAutoRotateOnTargetToleranceDegrees() {
        return 2.0; /*Preferences.getInstance().getDouble("AutoRotateOnTargetToleranceDegrees", 2.0); */
    }
    static public double getAutoRotateDefaultTaretDegrees() {
        return Preferences.getInstance().getDouble("AutoRotateDefaultTargetDegrees",0.0);
    }
    
    // Elevator PID Controller Settings
    static public double getElevatorP() {

    	return 0.0025;
    }
    static public double getElevatorI() {

    	return 0.0;
    }
    static public double getElevatorD() {

    	return 0.0;
    }
    static public double getElevatorOnTargetToleranceInches() {
        
    	return 0.5;
    }
    static public double getElevatorDefaultTaretInches() {
    	
        return Preferences.getInstance().getDouble("ElevatorDefaultTargetInches",0.0);
    }
    
    static public void setTopEncoderPos(int encoderCount) {
    	
    	Preferences.getInstance().putDouble("encoderTopPos", encoderCount);
    }
    
    static public double getTopEncoderPos() {
    	
    	return Preferences.getInstance().getDouble("EncoderTopPos", 0.0);
    }
    
    static public double getElevatorHeight() {
    	
    	//CHANGE ME
    	return 62.0;
    }
    
    static public double getFloorOffset() {
    	
    	//CHANGE ME
    	return 6;
    }
}
