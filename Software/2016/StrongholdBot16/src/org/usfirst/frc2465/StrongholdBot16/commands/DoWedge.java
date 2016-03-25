package org.usfirst.frc2465.StrongholdBot16.commands;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.RobotMap;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
// :D
public class DoWedge extends CommandGroup {
		boolean goDown;
		boolean started;
		boolean done;
		double start_time;
		static final double timeout = 2.0;
	public DoWedge(boolean score){
		requires(Robot.wedge);
		goDown = score;
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    	started = false;
    	done = false;
    	start_time = 0;
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if(!started){
    		if(goDown){
    			if(!Robot.wedge.isDown()){
    				Robot.wedge.setMotor(1);
    				started = true;
    				start_time = Timer.getFPGATimestamp();
    			}
    		}
    		else{
    			if(!Robot.wedge.isUp()){
    				Robot.wedge.setMotor(-1);
    				started = true;
    				start_time = Timer.getFPGATimestamp();
    			}
    		}
    	}
    	else{
    		double curr_time = Timer.getFPGATimestamp();
    		double total_time = curr_time - start_time;
    		if((goDown && Robot.wedge.isDown()) || (!goDown && Robot.wedge.isUp()) || total_time > timeout){
    			Robot.wedge.setMotor(0);
    			Robot.wedge.setPosition(!goDown);
    			done = true;
    		}
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
		return done;
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.wedge.setMotor(0);
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
