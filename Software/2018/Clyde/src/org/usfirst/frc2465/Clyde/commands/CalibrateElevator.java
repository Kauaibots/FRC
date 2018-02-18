package org.usfirst.frc2465.Clyde.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.usfirst.frc2465.Clyde.Robot;
import org.usfirst.frc2465.Clyde.RobotPreferences;
import org.usfirst.frc2465.Clyde.subsystems.Elevator;

public class CalibrateElevator extends Command {

	public enum State {IDLE, GOING_TO_BOTTOM, GOING_TO_TOP, RETURN_TO_BOTTOM, DONE};
	State state;
	
	Elevator elevator;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	protected void initialize() {
		
		state = State.IDLE;
		elevator = Robot.elevator;
	}
	
	protected void execute() {
		if ((state == State.IDLE) && (elevator.isBottom())) {
			elevator.setHome();
			state = State.GOING_TO_TOP;
		}
		else if ((state == State.IDLE) && (elevator.isBottom() == false)) {
			state  = State.GOING_TO_BOTTOM;
		}
		else if (state == State.GOING_TO_BOTTOM) {
			
			if (elevator.isBottom() == false) {
				elevator.setMotion(Elevator.Motion.DOWN);
			} else {
				elevator.setMotion(Elevator.Motion.STOP);
				elevator.setHome();
				state = State.GOING_TO_TOP;
			}
		}
		else if (state == State.GOING_TO_TOP) {
			
			if (elevator.isTop() == false) {
				elevator.setMotion(Elevator.Motion.UP);
			}
			else {
				elevator.setMotion(Elevator.Motion.STOP);
				elevator.setTop();
				state = State.RETURN_TO_BOTTOM;
			}
		}
		else if (state == State.RETURN_TO_BOTTOM) {
				
			if (elevator.isBottom() == false) {
				elevator.setMotion(Elevator.Motion.DOWN);
			} else {
				elevator.setMotion(Elevator.Motion.STOP);
				state = State.DONE;
			}
			
		}
		else if (state == State.DONE ) {
			
			elevator.getPIDController().setInputRange(0 , RobotPreferences.getTopEncoderPos());
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
