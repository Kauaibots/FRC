
package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotMap;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;

public class _LeftPass extends CommandGroup {

	public _LeftPass() {

		this.addSequential( new DriveDistance(261, false));		
		this.addSequential( new AutoRotate(0));
	}
	
	public void initialize() {
		RobotMap.imu.zeroYaw();
	}
		
}
