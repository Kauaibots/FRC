package org.usfirst.frc2465.Hercules.commands;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.WaitCommand;

/**
 *
 */
// :D
public class TravelToPeg extends CommandGroup {
	
	boolean changed = Robot.vision.didChange();
	float current_angle;
	
	public TravelToPeg(){
		requires(Robot.drive);
		
		if(changed == true){
		this.addSequential( new AutoRotateToPeg( Robot.vision.getCurrentZ()) );
		this.addSequential( new AutoRotate(current_angle) );
		this.addSequential( new DriveDistance(Robot.vision.getCurrentX(), .2f, 0f) );
		this.addSequential( new DriveDistance(0f, .2f, Robot.vision.getCurrentY()) );
		}
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    	current_angle = RobotMap.imu.getYaw();
    }
/*
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
