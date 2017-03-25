package org.usfirst.frc2465.Hercules.commands;

import org.usfirst.frc2465.Hercules.Robot;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
// :D
public class RightPeg extends CommandGroup {
	
	public RightPeg(){
		requires(Robot.drive);
		this.addSequential( new DriveDistance(68.0f, .2f, 0f));
	}
/*
    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
		return false;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
    */
}
