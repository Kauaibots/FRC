package org.usfirst.frc2465.Clyde.commands;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator.Motion;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ElevatorManual extends Command {

	Motion motion;
	
	static final float SPEED = 0.75f;
	static final float SLOWSPEED = 0.75f;
	boolean finished = false;

	public ElevatorManual(Motion motion) {

		this.motion = motion;
		
		requires(Robot.elevator);
	}

	protected void initialize() {
		
		Robot.elevator.setGoToInch(false);
		finished = false;
	}

	protected void execute() {
		
		if (Robot.elevator.isBottom()) {
			Robot.elevator.setHome();
		}
		
		SmartDashboard.putString("Elevator Command", "Manual");
		
		Joystick arduino = Robot.oi.arduino;
		
		if (arduino.getRawButton(3) || arduino.getRawButton(4) || arduino.getRawButton(5)) {
			finished = true;
		}

		if ((motion == Motion.HOLD || motion == Motion.DOWN) && Robot.elevator.isBottom() || Robot.elevator.getCurrentInches() <= 13 && motion == Motion.HOLD) {
			Robot.elevator.setMotion(Motion.STOP, SPEED);
		}
		else if (motion == Motion.UP && Robot.elevator.isTop()) {
			Robot.elevator.setMotion(Motion.HOLD, SPEED);
		}
		else if (Robot.elevator.getCurrentInches() <= 13 && motion == Motion.DOWN) {
			Robot.elevator.setMotion(Motion.DOWN, 0.08f);
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

		return finished;
	}

	protected void end() {


	}
}
