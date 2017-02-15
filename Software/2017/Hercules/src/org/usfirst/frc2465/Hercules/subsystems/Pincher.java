// RobotBuilder Version: 2.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc2465.Hercules.subsystems;

import org.usfirst.frc2465.Hercules.RobotMap;
import org.usfirst.frc2465.Hercules.commands.*;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.command.Subsystem;


/**
 *
 */
public class Pincher extends Subsystem {
	double start_time = 0;
	double timePeriod = 0;
	
	boolean oldGearDetected = false;
	
    private final DoubleSolenoid fingers = RobotMap.pincherFingers;
    private final DoubleSolenoid tilter = RobotMap.pincherTilter;
    private final DoubleSolenoid ejector = RobotMap.pincherEjector;
    
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS


    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    public void initDefaultCommand() {
    	setDefaultCommand(new DefaultPneumatics());

    }
    
	boolean gearPresent;
	
    public boolean detectGear(){
    	double distance = RobotMap.gearDetector.getDistanceInches();
        if(distance < 3.0 && distance > 1.0){
        	gearPresent = true;
        	}
    	return gearPresent;
    }
    
    public void openPincher(){
    	fingers.set(Value.kForward);
    }
    
    public void closePincher(){
    	fingers.set(Value.kReverse);
    }
    
    public void tiltUpPincher(){
    	tilter.set(Value.kForward);
    }
    
    public void tiltDownPincher(){
    	tilter.set(Value.kReverse);
    }
    
    public void deployEjector(){
    	ejector.set(Value.kForward);
    }
    
    public void retractEjector(){
    	ejector.set(Value.kReverse);
    }
    
    public void closeUpPincher(){
    	fingers.set(Value.kReverse);
    	tilter.set(Value.kForward);
    }
    
    public void autoClose(){
    	if(detectGear() == true && oldGearDetected == false){
    		closeUpPincher();
    	}
    	oldGearDetected = detectGear();
    }
    
    public void defaultPneumatics(){
    	tiltDownPincher();
    	openPincher();
    	retractEjector();
    }
    
    
    
    
    
}

