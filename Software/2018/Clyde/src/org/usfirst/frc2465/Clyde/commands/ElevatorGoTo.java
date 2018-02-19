package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator;

public class ElevatorGoTo extends Command {

	public enum Loc {HOME, SWITCH, SCALE, TOP};
	Loc loc;
	
	Elevator elevator;
	boolean quit;
	
	public ElevatorGoTo(Loc loc, boolean quit) {
		this.quit = quit;
		this.loc = loc;
	}
	
	protected void initialize() {
		
		elevator = Robot.elevator;
	}
	
	protected void execute() {
		
		//TODO: Edit these values
		switch (loc) {
			case HOME:	new ElevatorGoToInch(0, quit);		break;
			case SWITCH: new ElevatorGoToInch(24, quit);	break;
			case SCALE: new ElevatorGoToInch(72, quit);		break;
			case TOP: new ElevatorGoToInch((float) RobotPreferences.getElevatorHeight(), quit); 
		}
	}
	

	
	
	@Override
	protected boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected void end() {

	}
}
