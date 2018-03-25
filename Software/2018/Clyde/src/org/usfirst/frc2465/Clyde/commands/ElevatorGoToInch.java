package org.usfirst.frc2465.Clyde.commands;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator;
import org.usfirst.frc2465.Clyde.subsystems.Elevator.Motion;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ElevatorGoToInch extends Command {

	double target_inch;
	// boolean previous_inch = false; What is this for?
	boolean quit;
	int doneCounter;

	public ElevatorGoToInch(float inch, boolean quit) {

		target_inch = inch - RobotPreferences.getFloorOffset();
		this.quit = quit;

		requires(Robot.elevator);
	}

	protected void initialize() {
		// previous_inch = Robot.elevator.getGoToInch(); What is this for?
		Robot.elevator.setGoToInch(false);
		Robot.elevator.setSetpoint(target_inch);
		Robot.elevator.setGoToInch(true);
		System.out.println("GoToInch command initialized.");
	}

	protected void execute() {
				
		SmartDashboard.putNumber("GoToInch Error", Robot.elevator.getPIDController().getError());
		SmartDashboard.putNumber("GoToInch Setpoint", Robot.elevator.getPIDController().getSetpoint());
		SmartDashboard.putBoolean("GoToInch On Target", Robot.elevator.getPIDController().onTarget());
		
		if (Robot.elevator.getPIDController().onTarget()) {
			Robot.elevator.setGoToInch(false);
			Robot.elevator.setMotion(Motion.HOLD, 0.0f);
		}
		else {
			Robot.elevator.setMotion(Motion.STOP, 0.0f);
		}
		
		if (Robot.elevator.isBottom() && target_inch < 1) {
			Robot.elevator.setGoToInch(false);
			Robot.elevator.setMotion(Motion.STOP, 0.0f);
		}
	}

	@Override
	protected boolean isFinished() {

		if (Robot.elevator.onTarget()) {
			doneCounter++;
		} else {
			doneCounter = 0;
		}

		if (quit) {
			return doneCounter > 2;
		} else {
			return false;
		}
	}

	protected void end() {
		System.out.println("GoToInch command complete.");
		// Robot.elevator.setGoToInch(previous_inch); What is this for?

	}
}
