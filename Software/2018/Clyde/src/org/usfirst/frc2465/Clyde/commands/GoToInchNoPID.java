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
	boolean finished = false;
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
		motion = Motion.STOP;
		finished = false;
		Robot.elevator.setGoToInch(false);
		System.out.println("GoToInch command initialized.   Auto: " + auto + " Inch: " + autoTargetInch + "\n");
		System.out.flush();
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

		if (targetInch - error < 5) {
			speed = 0.75f;
		}
		else if (error > 8) {
			speed = 1.0f;
		} else if (error <= 8 && error > 2) {
			speed = 0.75f;
		} else if (error <= 2) {
			speed = 0.35f;
		}
		else {
			speed = 0.5f;
		}

		// Set the direction and/or mode
		if (Robot.elevator.getCurrentInches() <= 13 && motion == Motion.DOWN && !Robot.elevator.isBottom()) {
			motion = Motion.DOWN;
			speed = 0.08f;
		} else if (error < toleranceInch && (!Robot.elevator.isBottom() || Robot.elevator.isTop())) {
			motion = Motion.HOLD;
			finished = true;
		} else if (Robot.elevator.isBottom() && targetInch < 10) {
			motion = Motion.STOP;
		} else if (targetInch > Robot.elevator.getCurrentInches()) {
			motion = Motion.UP;
		} 
		else if (Robot.elevator.getCurrentInches() <= 13 && motion == Motion.DOWN) {
			motion = Motion.DOWN;
			speed = 0.08f;
		}
		else if (targetInch < Robot.elevator.getCurrentInches()) {
			motion = Motion.DOWN;
		}

		Robot.elevator.setMotion(motion, speed);
		SmartDashboard.putNumber("Elevator Speed", speed);

	}

	@Override
	protected boolean isFinished() {
		
		if (auto) {
			return finished;
		}
		else {
		return false;
		}
	}

	protected void end() {

		Robot.elevator.setMotion(Motion.HOLD, 0.0f);
		
		System.out.println("GoToInch command complete.   Auto: " + auto + "\n");

	}
	
	protected void interrupted() {
		if (auto) {
			Robot.elevator.setMotion(Motion.HOLD, 0.0f);
		}
		System.out.println("GoToInch command interrupted.   Auto: " + auto + "\n");
	}
}
