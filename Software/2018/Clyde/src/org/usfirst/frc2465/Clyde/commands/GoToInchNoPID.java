package org.usfirst.frc2465.Clyde.commands;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator;
import org.usfirst.frc2465.Clyde.subsystems.Elevator.Motion;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GoToInchNoPID extends Command {

	boolean auto;
	int doneCounter;
	float speed;
	double error;
	double targetInch;
	double autoTargetInch;
	double toleranceInch = 0.5;
	Motion motion;

	public GoToInchNoPID(float inch, boolean auto) {

		autoTargetInch = inch;

		this.auto = auto;

		requires(Robot.elevator);
	}

	protected void initialize() {
		Robot.elevator.setGoToInch(false);
		System.out.println("GoToInch command initialized.");
	}

	protected void execute() {

		SmartDashboard.putString("Elevator Command", "Auto");
		
		Joystick arduino = Robot.oi.arduino;

		if (Robot.elevator.isBottom()) {
			Robot.elevator.setHome();
		}

		// Set the target inch
		if (auto) {
			targetInch = autoTargetInch;
		} else if (arduino.getRawButton(3)) {
			targetInch = 27;
		} else if (arduino.getRawButton(4)) {
			targetInch = 57;
		} else if (arduino.getRawButton(5)) {
			targetInch = 83;
		} else {
			targetInch = 8.125;
		}

		// Set the speed
		double error = Math.abs(Robot.elevator.getCurrentInches() - targetInch);

		if (error > 8) {
			speed = 0.7f;
		} else if (error < 8 && error > 2) {
			speed = 0.5f;
		} else if (error < 2) {
			speed = 0.2f;
		}

		// Set the direction and/or mode
		if (Robot.elevator.getCurrentInches() <= 13 && motion == Motion.DOWN && !Robot.elevator.isBottom()) {
			Robot.elevator.setMotion(Motion.DOWN, 0.08f);
		} else if (error < toleranceInch && (!Robot.elevator.isBottom() || Robot.elevator.isTop())) {
			motion = Motion.HOLD;
		} else if (Robot.elevator.isBottom() && targetInch < 10) {
			motion = Motion.STOP;
		} else if (targetInch > Robot.elevator.getCurrentInches()) {
			motion = Motion.UP;
		} 
		else if (Robot.elevator.getCurrentInches() <= 13 && motion == Motion.DOWN) {
			Robot.elevator.setMotion(Motion.DOWN, 0.08f);
		}
		else if (targetInch < Robot.elevator.getCurrentInches()) {
			motion = Motion.DOWN;
		}

		Robot.elevator.setMotion(motion, speed);

	}

	@Override
	protected boolean isFinished() {

		return false;
	}

	protected void end() {

		System.out.println("GoToInch command complete.");

	}
}
