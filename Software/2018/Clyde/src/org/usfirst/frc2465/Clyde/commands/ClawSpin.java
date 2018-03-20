package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.subsystems.Claw;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class ClawSpin extends Command {

	Claw.Motion motion;

	public ClawSpin(Motion state) {
		motion = state;

		requires(Robot.claw);
	}

	@Override
	protected void initialize() {
		Robot.claw.setMotion(motion);
		
		System.out.println("ClawSpin command initialized.   Direction: " + motion + "\n");
		System.out.flush();
	}

	@Override
	protected void execute() {

		/*
		 * Joystick driver = Robot.oi.driveStick;
		 * 
		 * if (driver.getRawButton(10)) { motion = Motion.IN; } else if
		 * (driver.getRawButton(11)) { motion = Motion.OUT; } else { motion =
		 * Motion.STOP; }
		 */
	}

	@Override
	protected boolean isFinished() {

		return false;
	}

	@Override
	protected void end() {
	}

	@Override
	protected void interrupted() {
		// will stop the claw when button is released (whileHeld in OI runs this method
		// when a button is released)
		Robot.claw.setMotion(Motion.STOP);
		System.out.println("ClawSpin command completed." + "\n");
	}
}
