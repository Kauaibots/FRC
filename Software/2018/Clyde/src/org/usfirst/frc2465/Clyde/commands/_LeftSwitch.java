
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _LeftSwitch extends CommandGroup {

	boolean side; //True is the Red Alliance, false is Blue
	
	public _LeftSwitch(boolean side) {

		this.side = side;
		
		System.out.println("New Run \n");
		System.out.flush();
		this.addSequential( new DriveDistance(150, false));		
		this.addSequential( new AutoRotate(90));
		this.addSequential(new ClawSpin(Motion.IN), 0.3);
		this.addSequential(new GoToInchNoPID(36, true));
		this.addSequential(new DriveDistance(20, false));
		this.addSequential(new ClawSpin(Motion.OUT), 2);
		this.addSequential(new DriveDistance(20, true));
		this.addSequential(new GoToInchNoPID(8.125f, true));
	}
	
	public void initialize() {
		RobotMap.imu.zeroYaw();
	}
		
}
