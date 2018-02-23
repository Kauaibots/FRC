package org.usfirst.frc2465.Clyde.commands;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.subsystems.Claw;


public class ClawOp extends CommandGroup {

	
	

 public ClawOp() {
	 	
	 addSequential (new ClawGrab(Claw.GrabState.CLOSED));
 }
 @Override
 protected void initialize() {
 }


 @Override
 protected void execute() {
 }


 @Override
 protected boolean isFinished() {
     return false;
 }


 @Override
 protected void end() {
 }


 @Override
 protected void interrupted() {
 }
}

