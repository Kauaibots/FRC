
package org.usfirst.frc2465.Clyde.commands;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;


public class DriveStraight extends Command {

	double inches;
	double rotationsPerInch = 231;
	double tolerance = 20;
	double leftCount;
	double rightCount;
	double distance;
	float speed;
	
	WPI_TalonSRX left = RobotMap.talon1;
	WPI_TalonSRX right = RobotMap.talon3;
	
	
	
    public DriveStraight(double distance) {
    	inches = distance;
    	
    	requires(Robot.drive);
    }


    @Override
    protected void initialize() {
    	Robot.drive.zeroEncoder();
    	distance = rotationsPerInch * inches;
    }

    @Override
    protected void execute() {
    	
    	leftCount = RobotMap.talon1.getSelectedSensorPosition(0);
    	rightCount = RobotMap.talon3.getSelectedSensorPosition(0);
    	
    	if (distance > leftCount && distance > rightCount) {
    		
    	}
    	
    	if (leftCount > rightCount) {
    		
    	}
    	
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished() {
        return false;
    }

    // Called once after isFinished returns true
    @Override
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    @Override
    protected void interrupted() {
    }
}
