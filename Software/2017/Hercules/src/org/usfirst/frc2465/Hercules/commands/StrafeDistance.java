package org.usfirst.frc2465.Hercules.commands;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class StrafeDistance extends Command {
	
	float distance_in_inches;
	float desired_angle;
	boolean fod_enabled;
	double start_time;
	double timeout;
	
    public StrafeDistance(float distance_inches,float desired_angle, double timeout) {
        this.distance_in_inches = distance_inches;
        requires(Robot.drive);
    	this.timeout = timeout;
    	this.desired_angle = desired_angle;
    }
    

    // Called just before this Command runs the first time
    protected void initialize() {
    	fod_enabled = Robot.drive.getFODEnabled();
    	Robot.drive.setFODEnabled(false);
    	Robot.drive.enableAutoStrafe(distance_in_inches);
    	System.out.println("StrafeDistance command initialized.");
    	start_time = Timer.getFPGATimestamp();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	Robot.drive.doMecanum(0, 0, 0);
    	Robot.drive.setAutoRotation(true);
    	Robot.drive.setSetpoint(desired_angle);
    	SmartDashboard.putNumber("AutoStrafe Error", Robot.drive.getStrafeController().getError());
        SmartDashboard.putNumber("AutoStrafe Setpoint", Robot.drive.getStrafeController().getSetpoint());
        SmartDashboard.putBoolean("AutoStrafe On Target", Robot.drive.getStrafeController().onTarget());
        SmartDashboard.putBoolean("AutoStrafe Timeout", Timer.getFPGATimestamp() - start_time > timeout);
        SmartDashboard.putNumber("AutoStrafe startTime", start_time);
        SmartDashboard.putNumber("AutoStrafe Time", Timer.getFPGATimestamp());
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return Robot.drive.isAutoStrafeOnTarget() || Timer.getFPGATimestamp() - start_time > timeout;
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.drive.disableAutoStrafe();
    	Robot.drive.doMecanum(0, 0, 0); /* Stop the Robot */
    	Robot.drive.setFODEnabled(fod_enabled);
    	//boolean stopped = Robot.drive.isStopped();
    	System.out.println("StrafeDistance command complete.");
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
