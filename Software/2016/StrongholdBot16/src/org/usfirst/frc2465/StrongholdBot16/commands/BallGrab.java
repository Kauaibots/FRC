package org.usfirst.frc2465.StrongholdBot16.commands;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.RobotMap;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
// :D
public class BallGrab extends CommandGroup {
	
	public BallGrab(){
		requires(Robot.ballcontrol);
		Robot.ballcontrol.hold();
		
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if(Robot.ballcontrol.isBallPresent() == true){
    		Robot.ballcontrol.exhale();
    		if(Robot.ballcontrol.isBallPresent() == false){
    			Robot.ballcontrol.hold();
    		}
    	}
    	else if(Robot.ballcontrol.isBallPresent() == false){
    		Robot.ballcontrol.inhale();
    		if(Robot.ballcontrol.isBallPresent() == true){
    			Robot.ballcontrol.hold();
    		}
    	}
    	else{
    		Robot.ballcontrol.hold();
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
		return false;
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.ballcontrol.hold();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
