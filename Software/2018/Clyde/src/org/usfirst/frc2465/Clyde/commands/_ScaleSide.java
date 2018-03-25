
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _ScaleSide extends CommandGroup {

	public _ScaleSide() {

		this.addSequential(new ClawGrab(Value.kReverse));
		
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
				/*this.addSequential(new DriveDistance(230, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new DriveDistance(230, false));
				this.addSequential(new AutoRotate(180));
				this.addSequential(new DriveDistance(60, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(25, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(15, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));*/
				//Cross Baseline if on opposite side
				this.addSequential(new DriveDistance(110, false));
				this.addSequential(new AutoRotate(0));
			}
		} else if (RobotPreferences.startingPosition == 2) {
			if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(45, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new DriveDistance(64, false));
				this.addSequential(new AutoRotate(0));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(74, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(45, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new DriveDistance(52, false));
				this.addSequential(new AutoRotate(0));
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(74, false));
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
			} else if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(150, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(20, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(20, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));
			} else if (RobotPreferences.getSwitch() == 'L') {
				/*this.addSequential(new DriveDistance(230, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new DriveDistance(230, false));
				this.addSequential(new AutoRotate(180));
				this.addSequential(new DriveDistance(60, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.1);
				this.addSequential(new GoToInchNoPID(36, true));
				this.addSequential(new DriveDistance(25, false));
				this.addSequential(new ClawSpin(Motion.OUT), 1);
				this.addSequential(new DriveDistance(15, true));
				this.addSequential(new GoToInchNoPID(8.125f, true));*/
				//Cross baseline if not on our side
				this.addSequential(new DriveDistance(110, false));
				this.addSequential(new AutoRotate(0));
			}
		}
	}

	public void initialize() {
		System.out.println("New Run Scale Side\n");
		char switchPos = RobotPreferences.getSwitch();
		char scalePos = RobotPreferences.getScale();
		RobotMap.imu.zeroYaw();
		SmartDashboard.putString("Autonomous", String.valueOf(switchPos) + "    " + RobotPreferences.startingPosition);
		System.out.println("Scale" + String.valueOf(scalePos)+ "Switch " + String.valueOf(switchPos) + "    " + RobotPreferences.startingPosition);	}

}
