
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _Baseline extends CommandGroup {

	public _Baseline() {

		if (RobotPreferences.startingPosition == 2) {
			if (RobotPreferences.getSwitch() == 'L') {
				this.addSequential(new DriveDistance(60, false));
				this.addSequential(new AutoRotate(-90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new DriveDistance(84, false));
				this.addSequential(new AutoRotate(0));
				this.addSequential(new DriveDistance(60, false));
			}
			if (RobotPreferences.getSwitch() == 'R') {
				this.addSequential(new DriveDistance(60, false));
				this.addSequential(new AutoRotate(90));
				this.addSequential(new ClawSpin(Motion.IN), 0.15);
				this.addSequential(new DriveDistance(72, false));
				this.addSequential(new AutoRotate(0));
				this.addSequential(new DriveDistance(60, false));
			}
		} else if (RobotPreferences.startingPosition != 0){
			this.addSequential(new DriveDistance(110, false));
			this.addSequential(new AutoRotate(0));
		}
	}

	public void initialize() {
		System.out.println("New Run Baseline\n");
		char switchPos = RobotPreferences.getSwitch();
		RobotMap.imu.zeroYaw();
		SmartDashboard.putString("Autonomous", String.valueOf(switchPos) + "    " + RobotPreferences.startingPosition);
		System.out.println(String.valueOf(switchPos) + "    " + RobotPreferences.startingPosition);	}

}
