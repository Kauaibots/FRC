package org.usfirst.frc2465.StrongholdBot16.commands;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.RobotMap;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
// :D
public class BallGrab extends CommandGroup {
	boolean isDone = false;
	boolean inhaling = false;
	
	public BallGrab(){
		requires(Robot.ballcontrol);
		Robot.ballcontrol.hold();
		
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    	if(Robot.ballcontrol.isBallPresent() == false){
    		inhaling = true;
    		Robot.ballcontrol.inhale();
    	}
    	else{
    		inhaling = false;
    		Robot.ballcontrol.exhale();
    		Robot.ballcontrol.ballMotorExtend(5);
    	}
    	isDone = false;    	
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	
    	if(inhaling == true){
    		if(Robot.ballcontrol.isBallPresent() == true){
    			Robot.ballcontrol.ballMotorExtend(.125);
    			inhaling = false;
    		}
    	}
    	else{
    		isDone = Robot.ballcontrol.checkTime();
    	}
    	
    	SmartDashboard.putBoolean("BallPresent", Robot.ballcontrol.isBallPresent());
    	SmartDashboard.putBoolean("Inhale", inhaling);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
		return isDone;
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
