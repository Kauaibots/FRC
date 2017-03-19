package org.usfirst.frc2465.Hercules.commands;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.WaitCommand;

/**
 *
 */
// :D
public class LeftBoiler extends CommandGroup {
	
	public LeftBoiler(){
		requires(Robot.drive);
		//this.addSequential(new AutoRotate(0, 2));
		this.addParallel(new TiltPincherU());
		this.addSequential( new DriveDistance(93.0f, .2f, 0f));
		this.addSequential(new WaitCommand(0.25));
		this.addSequential( new AutoRotate(58, 2));
		this.addSequential(new WaitCommand(0.5));
		this.addSequential(new StrafeDistance(-17, 58, 3));
		this.addSequential( new DriveDistance(24.0f, .2f, 0f));
		this.addSequential(new FinalGear(58));

		
	}

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	//Robot.drive.setAutoRotation(true);
    	//Robot.drive.setSetpoint(0);
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
