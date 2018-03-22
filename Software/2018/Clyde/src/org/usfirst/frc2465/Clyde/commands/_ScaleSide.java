
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _ScaleSide extends CommandGroup {

	public _ScaleSide() {

		System.out.println("New Run \n");
		System.out.flush();
		if (RobotPreferences.startingPosition == 1) {
			if (RobotPreferences.getScale() == 'L') {
				this.addSequential(new DriveDistance(240, false));
				this.addSequential(new AutoRotate(45));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new GoToInchNoPID(82, true));
				this.addSequential(new DriveDistance(50, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(150, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(20, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(230, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new DriveDistance(153, false));
				this.addSequential(new AutoRotate(178));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(18, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(12, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			}
		} else if (RobotPreferences.startingPosition == 2) {
			if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(60, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new DriveDistance(84, false));
				this.addSequential(new AutoRotate(0));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(65, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(60, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new DriveDistance(72, false));
				this.addSequential(new AutoRotate(0));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(65, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			}
		} else if (RobotPreferences.startingPosition == 3) {
			if (RobotPreferences.getScale() == 'R') {
				this.addSequential(new DriveDistance(240, false));
				this.addSequential(new AutoRotate(-45));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new GoToInchNoPID(82, true));
				this.addSequential(new DriveDistance(50, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			}else if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(150, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(20, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(230, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new DriveDistance(153, false));
				this.addSequential(new AutoRotate(178));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(18, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(12, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			}
		}
	}

	public void initialize() {
		RobotMap.imu.zeroYaw();
	}

}
