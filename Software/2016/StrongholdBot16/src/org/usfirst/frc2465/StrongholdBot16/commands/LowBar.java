package org.usfirst.frc2465.StrongholdBot16.commands;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.RobotMap;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
// :D
public class LowBar extends CommandGroup {
	
	double start_time = 0;

	public LowBar(){
		requires(Robot.drive);
		//this.addParallel( new DriveDistance(60f, .2f, 0f));
		//this.addSequential( new AutoRotate(180));
		//this.addSequential( new DriveDistance(-60f, .2f, 0f));
		
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    	Robot.drive.setFODEnabled(false);
    	Robot.drive.doMecanum(0, -.1, 0);
		start_time = Timer.getFPGATimestamp();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	//Robot.drive.doMecanum(0, 0, 0);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
		if(start_time!= 0){
			if((Timer.getFPGATimestamp() - start_time) > 5.2){
				start_time = 0;
				return true;
			}
		}
        
		//return Robot.drive.isStopped();
    	return false;
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.drive.setFODEnabled(true);    	
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
