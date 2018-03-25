
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class DriveDistance extends Command {

	double inches;
	int rotationsPerInch = 211;
	int leftCount;
	int rightCount;
	double distance;
	double error;
	float speed = 0.50f;
	boolean finished = false;
	boolean reverse;

	WPI_TalonSRX left = RobotMap.talon1;
	WPI_TalonSRX right = RobotMap.talon3;

	public DriveDistance(double distance, boolean reverse) {
		inches = distance;
		this.reverse = reverse;

		requires(Robot.drive);
	}

	@Override
	protected void initialize() {
		finished = false;
		Robot.drive.zeroEncoder();
		Timer.delay(0.02);
		System.out.println(RobotMap.talon1.getSelectedSensorPosition(0));
		System.out.println(RobotMap.talon3.getSelectedSensorPosition(0));
		distance = Math.abs(rotationsPerInch * inches);
		System.out.println("DriveDistance command initialized.   Inches: " + inches + "\n");
		System.out.flush();
		RobotMap.talon2.set(com.ctre.phoenix.motorcontrol.ControlMode.Follower, 1);
		RobotMap.talon4.set(com.ctre.phoenix.motorcontrol.ControlMode.Follower, 3);
	}

	@Override
	protected void execute() {
		
		error = Math.abs(inches - (leftCount+rightCount)/2/rotationsPerInch);

		leftCount = -RobotMap.talon1.getSelectedSensorPosition(0);
		rightCount = -RobotMap.talon3.getSelectedSensorPosition(0);

		SmartDashboard.putNumber("EncoderL", leftCount);
		SmartDashboard.putNumber("EncoderR", rightCount);

		if (Robot.elevator.getCurrentInches() > 50) {
			speed = 0.36f;
			}
		else if (inches - error < 8) {
			speed = 0.60f;
		}
		else if (inches > 30 && error > 8) {
			speed = 0.85f;
		}
		else if (inches > 30 && error < 5) {
			speed = 0.40f;
		}
		else {
			speed = 0.60f;
		}
		
		
		if (!reverse) {
			if ((leftCount + rightCount)/2 >= distance) {
				finished = true;
				System.out.println(leftCount + "      " + rightCount +"\n");
				System.out.flush();
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
			if ((leftCount + rightCount)/2 >= distance) {
				finished = true;
				System.out.println(leftCount + "      " + rightCount +"\n");
				System.out.flush();
			}
			if (leftCount > rightCount + 40 && finished == false) {
				Robot.drive.setMotion(-speed - 0.1f, -speed + 0.1f);
			} else if (rightCount > leftCount + 40 && finished == false) {
				Robot.drive.setMotion(-speed + 0.1f, -speed - 0.1f);
			} else if (finished == false) {
				Robot.drive.setMotion(-speed, -speed);
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
		Robot.drive.setMotion(0.0f, 0.0f);
		System.out.println("DriveDistance command complete." + "\n");
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	@Override
	protected void interrupted() {
		Robot.drive.setMotion(0.0f, 0.0f);
		System.out.println("DriveDistance command interrupted." + "\n");

	}
}
