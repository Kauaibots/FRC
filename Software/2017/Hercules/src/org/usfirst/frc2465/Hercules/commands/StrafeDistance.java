package org.usfirst.frc2465.Hercules.commands;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class StrafeDistance extends Command {
	
	float distance_inches;
	float fwd;
	boolean fod_enabled;
	
    public StrafeDistance(float distance_inches, float fwd) {
        this.distance_inches = distance_inches;
        this.fwd = fwd;
        requires(Robot.drive);
    }
    

    // Called just before this Command runs the first time
    protected void initialize() {
    	fod_enabled = Robot.drive.getFODEnabled();
    	Robot.drive.setFODEnabled(false);
    	RobotMap.imu.zeroYaw();
    	Robot.drive.enableStrafeAutoStop(distance_inches);
    	System.out.println("StrafeDistance command initialized.");
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	Robot.drive.doMecanum(0, fwd, 0);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return Robot.drive.isStopped();
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.drive.doMecanum(0, 0, 0); /* Stop the Robot */
    	Robot.drive.disableAutoStop();
    	Robot.drive.setFODEnabled(fod_enabled);
    	boolean stopped = Robot.drive.isStopped();
    	System.out.println("StrafeDistance command complete.");
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
