
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class DriveDistance extends Command {

	double inches;
	int rotationsPerInch = 231;
	int leftCount;
	int rightCount;
	double distance;
	float speed = 0.50f;
	boolean finished = false;
	boolean reverse;
	double angle;

	WPI_TalonSRX left = RobotMap.talon1;
	WPI_TalonSRX right = RobotMap.talon3;

	public DriveDistance(double distance, boolean reverse) {
		inches = distance;
		this.reverse = reverse;

		requires(Robot.drive);
	}

	@Override
	protected void initialize() {
		RobotPreferences.driveFinished = false;
		Robot.drive.zeroEncoder();
		distance = Math.abs(rotationsPerInch * inches);
		angle = RobotMap.imu.getAngle();
		RobotMap.talon2.set(com.ctre.phoenix.motorcontrol.ControlMode.Follower, 1);
		RobotMap.talon4.set(com.ctre.phoenix.motorcontrol.ControlMode.Follower, 3);
	}

	@Override
	protected void execute() {

		leftCount = RobotMap.talon1.getSelectedSensorPosition(0);
		rightCount = -RobotMap.talon3.getSelectedSensorPosition(0);

		SmartDashboard.putNumber("EncoderL", leftCount);
		SmartDashboard.putNumber("EncoderR", rightCount);

		if (!reverse) {
			if (leftCount >= distance && rightCount >= distance) {
				finished = true;
			}
			if (leftCount > rightCount + 40 && finished == false) {
				Robot.drive.setMotion(speed - 0.1f, speed + 0.1f);
			} else if (rightCount > leftCount + 40 && finished == false) {
				Robot.drive.setMotion(speed + 0.1f, speed - 0.1f);
			} else if (finished == false) {
				Robot.drive.setMotion(speed, speed);
			}
		}
		else if (reverse) {
			leftCount = -leftCount;
			rightCount = -rightCount;
			speed = -speed;
			if (leftCount >= distance && rightCount >= distance) {
				finished = true;
			}
			if (leftCount > rightCount + 40 && finished == false) {
				Robot.drive.setMotion(speed - 0.1f, speed + 0.1f);
			} else if (rightCount > leftCount + 40 && finished == false) {
				Robot.drive.setMotion(speed + 0.1f, speed - 0.1f);
			} else if (finished == false) {
				Robot.drive.setMotion(speed, speed);
			}
		}

	}

	// Make this return true when this Command no longer needs to run execute()
	@Override
	protected boolean isFinished() {
		return finished;
	}

	// Called once after isFinished returns true
	@Override
	protected void end() {
		RobotPreferences.driveFinished = true;
		Robot.drive.setAutoRotation(true);
		Robot.drive.setSetpoint(angle);
		finished = false;
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	@Override
	protected void interrupted() {
		finished = false;
	}
}
