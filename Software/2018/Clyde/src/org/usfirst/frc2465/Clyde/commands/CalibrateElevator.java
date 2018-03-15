package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator;

public class CalibrateElevator extends Command {

	public enum State {IDLE, GOING_TO_BOTTOM, GOING_TO_TOP, RETURN_TO_BOTTOM, DONE};
	State state;
	
	Elevator elevator;
	
	static final float SPEED = 0.4f;
	
	boolean finished;
	
	public CalibrateElevator() {

		requires(Robot.elevator);
	}
	
	protected void initialize() {
		
		state = State.IDLE;
		elevator = Robot.elevator;
		Robot.elevator.setGoToInch(false);
		finished = false;
	}
	
	protected void execute() {
		SmartDashboard.putString("state", state.toString());
		
		if ((state == State.IDLE) && (elevator.isBottom())) {
			elevator.setHome();
			state = State.GOING_TO_TOP;
		}
		else if ((state == State.IDLE) && (elevator.isBottom() == false)) {
			state  = State.GOING_TO_BOTTOM;
		}
		else if (state == State.GOING_TO_BOTTOM) {
			
			if (elevator.isBottom() == false) {
				elevator.setMotion(Elevator.Motion.DOWN, SPEED);
			} else {
				elevator.setMotion(Elevator.Motion.STOP, SPEED);
				elevator.setHome();
				state = State.GOING_TO_TOP;
			}
		}
		else if (state == State.GOING_TO_TOP) {
			
			if (elevator.isTop() == false) {
				elevator.setMotion(Elevator.Motion.UP, SPEED);
			}
			else {
				elevator.setMotion(Elevator.Motion.STOP, SPEED);
				elevator.setTop();
				state = State.RETURN_TO_BOTTOM;
			}
		}
		else if (state == State.RETURN_TO_BOTTOM) {
				
			if (elevator.isBottom() == false) {
				elevator.setMotion(Elevator.Motion.DOWN, SPEED);
			} else {
				elevator.setMotion(Elevator.Motion.STOP, SPEED);
				state = State.DONE;
			}
			
		}
		else if (state == State.DONE ) {
			
			elevator.getPIDController().setInputRange(0 , RobotPreferences.getTopEncoderPos());
			finished = true;
		}
	}
	
	@Override
	protected boolean isFinished() {
		// TODO Auto-generated method stub
		return finished;
	}
	
	protected void end() {

	}
}
