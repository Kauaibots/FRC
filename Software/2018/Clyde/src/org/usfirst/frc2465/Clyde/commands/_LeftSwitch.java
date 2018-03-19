
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _LeftSwitch extends Command {

	boolean side; // True is red alliance, false is blue
	
	int counter = 0;

	public enum Step {
		IDLE, FORWARD, ROTATE, SWITCH, FORWARD2, EJECT, REVERSE, DOWN, FINISHED
	};

	Step step;

	public _LeftSwitch(boolean side) {
		this.side = side;

	}

	@Override
	protected void initialize() {
		step = Step.IDLE;
	}

	// Called repeatedly when this Command is scheduled to run
	@Override
	protected void execute() {

		switch (step) {
		case IDLE:
			step = Step.FORWARD;
			counter = 0;
			break;
		case FORWARD:
			RobotPreferences.driveFinished = false;
			new DriveDistance(165, false);
			if (RobotPreferences.driveFinished) {
				step = Step.ROTATE;
				break;
			}
		case ROTATE:
			RobotPreferences.rotateFinished = false;
			Robot.drive.setAutoRotation(true);
			Robot.drive.setSetpoint(90.0);
			if (RobotPreferences.rotateFinished) {
				step = Step.SWITCH;
				break;
			}
		case SWITCH:
			new GoToInchNoPID(27, true);
			step = Step.FORWARD2;
			break;
		case FORWARD2:
			RobotPreferences.driveFinished = false;
			new DriveDistance(20, false);
			if (RobotPreferences.driveFinished) {
				step = Step.EJECT;
				counter = 0;
				break;
			}
		case EJECT:
			if (counter > 100) {
				counter ++;
				Robot.claw.setMotion(Motion.OUT);
			}
			else {
				Robot.claw.setMotion(Motion.STOP);
				step = Step.REVERSE;
				break;
			}
		case REVERSE:
			RobotPreferences.driveFinished = false;
			new DriveDistance(20, true);
			if (RobotPreferences.driveFinished) {
				step = Step.DOWN;
				break;
			}
		case DOWN:
			new GoToInchNoPID(8.125f, true);
			step = Step.FINISHED;
			break;
		case FINISHED:
			break;
		}

	}

	// Make this return true when this Command no longer needs to run execute()
	@Override
	protected boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true
	@Override
	protected void end() {
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	@Override
	protected void interrupted() {
	}
}
