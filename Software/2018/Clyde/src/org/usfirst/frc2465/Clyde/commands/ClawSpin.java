package org.usfirst.frc2465.Clyde.commands;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.subsystems.Claw;
import org.usfirst.frc2465.Clyde.subsystems.Claw.Motion;


public class ClawSpin extends Command {

	Claw.Motion motion;

	
	
 public ClawSpin(Motion state) {
	 state = motion;
	 
	 requires(Robot.claw);
 }
 @Override
 protected void initialize() {
 }


 @Override
 protected void execute() {
	 
	 /*Joystick driver = Robot.oi.driveStick;
	 
	 if (driver.getRawButton(10)) {
		 motion = Motion.IN;
	 }
	 else if (driver.getRawButton(11)) {
		 motion = Motion.OUT;
	 }
	 else {
		 motion = Motion.STOP;
	 }*/
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

