package org.usfirst.frc2465.StrongholdBot16.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.subsystems.Drive;
import org.usfirst.frc2465.StrongholdBot16.subsystems.Drive.SpeedPIDTuneDirection;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class AutoSpeedPIDTune extends Command {

	public static final double MIN_FF = 5.0;
	public static final double MAX_FF = 100.0;
	public static final double STEP_FF = 1;
	
	public static final double MIN_P = 8;
	public static final double MAX_P = 10;
	public static final double STEP_P = .1;

	public static final double MIN_I = 0;
	public static final double MAX_I = 1;
	public static final double STEP_I = .0001;
	
	public static final double MIN_D = 0;
	public static final double MAX_D = 4.0;
	public static final double STEP_D = .2;
	
	double curr_p = MIN_P;
	double curr_i = 0;
	double curr_d = MIN_D;
	double curr_ff = MIN_FF;
	
	Drive.SpeedPIDTuneDirection dir = SpeedPIDTuneDirection.Rotate;
    
	double velocity = .25;
    double timeout_secs = 5;
	
    boolean test_started;
	boolean test_done;
	boolean all_tests_done;
	boolean delay;
	double delay_start;
	
	final double delay_period_seconds = 2.0;
	
	BufferedWriter writer;

	CANTalon.TalonControlMode previous_mode;
	
	public AutoSpeedPIDTune() {
    	requires(Robot.drive);
    }

    // Called just before this Command runs each time.
    protected void initialize() {
    	writer = null;
    	test_started = false;
    	test_done = false;
    	delay = false;
    	all_tests_done = false;
    	curr_ff = MIN_FF;
    	previous_mode = Robot.drive.getMode();
    	Robot.drive.setMode(CANTalon.TalonControlMode.Speed);
    }

    protected void outputToDashboard(){
    	SmartDashboard.putNumber("AutoSpeedPIDTune_Velocity", velocity);
    	SmartDashboard.putString("AutoSpeedPIDTuneDirection", dir.toString());
    	SmartDashboard.putNumber("AutoSpeedPIDTune_P", curr_p);
    	SmartDashboard.putNumber("AutoSpeedPIDTune_I", curr_i);
    	SmartDashboard.putNumber("AutoSpeedPIDTune_D", curr_d);
    	SmartDashboard.putNumber("AutoSpeedPIDTune_F", curr_ff);    	
    }
    
    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if (!test_started) {
    		if ( delay ) {
    			if ( ( Timer.getFPGATimestamp() - delay_start ) >= delay_period_seconds ) {
    				delay = false;
    			} else {
    				return;
    			}   			
    		}
        	test_started = Robot.drive.startSpeedPIDTuneRun(dir, velocity,  curr_p, curr_i, curr_d, curr_ff, timeout_secs);
        	if ( test_started ) {
            	SmartDashboard.putNumber("AutoSpeedPIDTune_Velocity", velocity);
            	SmartDashboard.putString("AutoSpeedPIDTuneDirection", dir.toString());
            	SmartDashboard.putNumber("AutoSpeedPIDTune_P", curr_p);
            	SmartDashboard.putNumber("AutoSpeedPIDTune_I", curr_i);
            	SmartDashboard.putNumber("AutoSpeedPIDTune_D", curr_d);
            	SmartDashboard.putNumber("AutoSpeedPIDTune_F", curr_ff);
            	/* Open File */
        		File file = new File(dir.toString() + Double.toString(velocity) + ".csv");

            	try {
            		// if file doesn't exist, then create it
            		if (!file.exists()) {
        				file.createNewFile();
        			}

        			FileWriter fw = new FileWriter(file.getAbsoluteFile());
        			writer = new BufferedWriter(fw);
            		String header = "Direction,Velocity,P,I,D,FF,Duration\r\n";
            		writer.write(header);
            	} catch (IOException x) {
            	    System.err.format("IOException: %s%n", x);
            	}    	        		
        	}
    	} else {
    		test_done = !Robot.drive.isSpeedPIDTuneActive();
    		if ( test_done ) {
    			Double time_to_success_seconds = 0.0;
    			boolean success = Robot.drive.getLastSpeedPIDTuneStats(time_to_success_seconds);
				/* Write to File */
		    	try {
		    		String row = dir.toString() + "," +
		    				Double.toString(velocity) + "," +
		    				Double.toString(curr_p) + "," + 
		    				Double.toString(curr_i) + "," + 
		    				Double.toString(curr_d) + "," +
		    				Double.toString(curr_ff) + "," +
		    				Double.toString(success ? time_to_success_seconds : timeout_secs) +
		    				"\r\n";
		    		writer.write(row);
		    	} catch (IOException x) {
		    	    System.err.format("IOException: %s%n", x);
		    	}
		    	/* Get ready for new test */
		    	if (curr_p <= MAX_P ) {
		    		curr_p += STEP_P;
		    	} else {
		    		all_tests_done = true;
		    		/* Close the File */
		    		try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		    	}
		    	test_started = false;
		    	delay = true;
		    	delay_start = Timer.getFPGATimestamp();
    		}
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return all_tests_done;
    }

    // Called once after isFinished returns true
    protected void end() {
		all_tests_done = true;
    	Robot.drive.setMode(previous_mode);
		/* Close the File */
		try {
			if ( writer != null ) {
				String s = "Interrupted!  HELP!\r\n";			
				writer.write(s);
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	end();
    }
}
