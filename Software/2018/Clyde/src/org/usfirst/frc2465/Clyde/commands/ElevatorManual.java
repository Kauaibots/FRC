package org.usfirst.frc2465.Clyde.commands;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator.Motion;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ElevatorManual extends Command {

	Motion motion;
	
	static final float SPEED = 0.65f;
	static final float SLOWSPEED = 0.30f;

	public ElevatorManual(Motion motion) {

		this.motion = motion;
		
		requires(Robot.elevator);
	}

	protected void initialize() {

	}

	protected void execute() {

		if (motion == Motion.HOLD && Robot.elevator.isBottom() || Robot.elevator.getCurrentInches() <= 11.5 && motion == Motion.HOLD) {
			Robot.elevator.setMotion(Motion.STOP, SPEED);
		}
		else if (Robot.oi.driveStick.getRawButton(1)){
			Robot.elevator.setMotion(motion, SLOWSPEED);
		}
		else {
			Robot.elevator.setMotion(motion, SPEED);
		}
	}

	@Override
	protected boolean isFinished() {

		return false;
	}

	protected void end() {


	}
}
