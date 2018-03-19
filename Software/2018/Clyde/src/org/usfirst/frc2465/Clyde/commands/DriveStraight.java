
package org.usfirst.frc2465.Clyde.commands;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;


public class DriveStraight extends Command {

	double inches;
	int rotationsPerInch = 231;
	int leftCount;
	int rightCount;
	double distance;
	float speed = 0.50f;
	boolean finished = false;
	
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
    	
		SmartDashboard.putNumber("EncoderL", leftCount);
		SmartDashboard.putNumber("EncoderR", rightCount);
		
		SmartDashboard.putBoolean("Finished", Robot.drive.isStopped());
    	
    	if (Robot.drive.isStopped()) {
    		Robot.drive.setMotion(0.0f, 0.0f);
    		finished = true;
    	}
    	else {
        	Robot.drive.configureAutoStop((int) distance);
        	
        	Robot.drive.setMotion(speed, speed);
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
    	
    	Robot.drive.autoStop = false;
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    @Override
    protected void interrupted() {
    	Robot.drive.autoStop = false;
    }
}
