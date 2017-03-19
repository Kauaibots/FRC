package org.usfirst.frc2465.Hercules.commands;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.WaitCommand;

/**
 *
 */
// :D
public class MiddlePeg extends CommandGroup {
	
	public MiddlePeg(){
		requires(Robot.drive);
		RobotMap.imu.zeroYaw();
		//this.addSequential(new AutoRotate(0, 2));
		this.addParallel(new TiltPincherU());
		this.addSequential( new DriveDistance(55.0f, .2f, 0f));
		this.addSequential(new WaitCommand(1));
		this.addSequential( new FinalGear());
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	Robot.drive.setAutoRotation(true);
    	Robot.drive.setSetpoint(0);
    }

   /* // Make this return true when this Command no longer needs to run execute()
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
