package org.usfirst.frc2465.Clyde.commands;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ElevatorGoToInch extends Command {

	double target_inch;
	boolean previous_inch = false;
	boolean quit;
	
	public ElevatorGoToInch (float inch, boolean quit) {
		
		target_inch = inch - RobotPreferences.getFloorOffset();
		this.quit = quit;
		
		requires(Robot.elevator);
	}
	
	protected void initialize() {
    	previous_inch = Robot.elevator.getGoToInch();
    	Robot.elevator.setGoToInch(true);
    	Robot.elevator.setSetpoint(target_inch); 
    	System.out.println("GoToInch command initialized.");
	}
	
	protected void execute() {
    	Robot.elevator.setMotion(null);
        SmartDashboard.putNumber("GoToInch Error", Robot.elevator.getPIDController().getError());
        SmartDashboard.putNumber("GoToInch Setpoint", Robot.elevator.getPIDController().getSetpoint());
        SmartDashboard.putBoolean("GoToInch On Target", Robot.elevator.getPIDController().onTarget());
		
	}
	
	@Override
	protected boolean isFinished() {
		
		if (quit) {
			return Robot.elevator.onTarget();
		} else {
			return false;
		}
	}
	
	protected void end() {
    	System.out.println("GoToInch command complete.");
    	Robot.elevator.setGoToInch(previous_inch);
    	Robot.elevator.setMotion(null);

	}

}
