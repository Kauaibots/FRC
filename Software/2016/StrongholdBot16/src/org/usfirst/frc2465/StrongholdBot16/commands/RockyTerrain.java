package org.usfirst.frc2465.StrongholdBot16.commands;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.RobotMap;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
// :D
public class RockyTerrain extends CommandGroup {
	
	public RockyTerrain(){
		requires(Robot.drive);
		this.addParallel( new DriveDistance(55f, .5f, 0f));
		this.addSequential( new AutoRotate(180));
		this.addSequential( new DriveDistance(-55f, .5f, 0f));
		
	}

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
}
