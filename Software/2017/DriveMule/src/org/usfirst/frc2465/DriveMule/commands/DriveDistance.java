package org.usfirst.frc2465.DriveMule.commands;

import org.usfirst.frc2465.DriveMule.Robot;
import org.usfirst.frc2465.DriveMule.RobotMap;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class DriveDistance extends Command {
	
	float distance_inches;
	float fwd;
	float strafe;
	boolean fod_enabled;
	
    public DriveDistance(float distance_inches, float fwd, float strafe) {
        this.distance_inches = distance_inches;
        this.fwd = fwd;
        this.strafe = strafe;
        requires(Robot.drive);
    }
    

    // Called just before this Command runs the first time
    protected void initialize() {
    	fod_enabled = Robot.drive.getFODEnabled();
    	Robot.drive.setFODEnabled(false);
    	RobotMap.imu.zeroYaw();
    	Robot.drive.enableAutoStop(distance_inches);
    	System.out.println("DriveDistance command initialized.");
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	Robot.drive.doMecanum(strafe, fwd, 0);
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
    	System.out.println("DriveDistance command complete.");
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
