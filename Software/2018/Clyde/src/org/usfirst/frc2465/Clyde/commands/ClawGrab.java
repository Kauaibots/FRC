package org.usfirst.frc2465.Clyde.commands;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Claw;

import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Joystick;


public class ClawGrab extends Command {

	Value state;
	

 public ClawGrab(Value state) {
	 
	 requires(Robot.claw);
	 
	 this.state = state;
	 SmartDashboard.putString("ClawGrab",  state.toString());

 }
 @Override
 protected void initialize() {
	 
	 Claw.setPosition(state);
	 
 }


 @Override
 protected void execute() {
 }


 @Override
 protected boolean isFinished() {
     return true;
 }


 @Override
 protected void end() {
 }


 @Override
 protected void interrupted() {
 }
}

