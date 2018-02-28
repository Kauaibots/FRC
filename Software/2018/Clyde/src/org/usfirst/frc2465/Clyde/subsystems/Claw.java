// RobotBuilder Version: 2.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc2465.Clyde.subsystems;

import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.commands.ClawSpin;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=IMPORTS

public class Claw extends Subsystem {

    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTANTS

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTANTS

    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    private final static DoubleSolenoid clawPiston = RobotMap.clawPiston;
    private final SpeedControllerGroup clawMotors = RobotMap.clawMotors;
    
	public enum Motion {IN, OUT, STOP};
	Motion motion;
	
	Double SPEED = 0.5; 

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS

    @Override
    public void initDefaultCommand() {
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND


    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND

        // Set the default command for a subsystem here.
        //setDefaultCommand(new ClawSpin(Motion.STOP));
    }

    @Override
    public void periodic() {
        // Put code here to be run every loop

    }

    // Put methods for controlling this subsystem
    // here. Call these from Commands.


	    
    public void updateDashboard() {
    	
		SmartDashboard.putString("Grabber State", getMotion().toString());
    }
    
    public void setMotion(Motion motion) {
    	
    	switch(motion) {
            case IN:    clawMotors.set(-SPEED);   break;
            case OUT:	clawMotors.set(SPEED);  break;
            case STOP:  clawMotors.set(0);       break;
    	}
    }
    
    public Motion getMotion() {
    	if (clawMotors.get() >= 0.05) {
    		return Motion.IN;
    	}
    	else  if (clawMotors.get() <= -0.05) {
    		return Motion.OUT;
    	}
    	else  if (clawMotors.get() <= -0.05 && clawMotors.get() >= 0.05) {
    		return Motion.STOP;
    	}
		return motion;
    }
    
    public static void setPosition(Value open) {
    
    	clawPiston.set(open);
    	
    }

}

