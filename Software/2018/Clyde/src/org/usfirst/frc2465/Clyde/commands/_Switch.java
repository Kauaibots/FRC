
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _Switch extends CommandGroup {

	public _Switch() {

		System.out.println("New Run \n");
		System.out.flush();
		if (DriverStation.getInstance().getLocation() == 1) {
			if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(150, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(20, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(220, false));
				this.addSequential(new AutoRotate(88));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new DriveDistance(153, false));
				this.addSequential(new AutoRotate(178));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(12, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(12, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			}
		} else if (DriverStation.getInstance().getLocation() == 2) {
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
		} else if (DriverStation.getInstance().getLocation() == 3) {
			if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(150, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(20, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(220, false));
				this.addSequential(new AutoRotate(-88));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new DriveDistance(153, false));
				this.addSequential(new AutoRotate(178));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(12, false));
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
