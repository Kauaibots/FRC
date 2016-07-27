package org.usfirst.frc2465.StrongholdBot16.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.usfirst.frc2465.StrongholdBot16.Robot;
import org.usfirst.frc2465.StrongholdBot16.subsystems.Drive;
import org.usfirst.frc2465.StrongholdBot16.subsystems.Drive.SpeedPIDTuneDirection;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class AutoSpeedPIDTune extends Command {

	double curr_p = .1;
	double curr_i = 0;
	double curr_d = 0;
	Drive.SpeedPIDTuneDirection dir = SpeedPIDTuneDirection.Rotate;
    double velocity = .1;
    double timeout_secs = 5;
	boolean test_started;
	boolean test_done;
	boolean all_tests_done;
	BufferedWriter writer;

	final double MIN_P = .05;
	final double MAX_P = 1;
	final double MAX_STEP_P = .05;

	final double MIN_I = 0;
	final double MAX_I = 1;
	final double MAX_STEP_I = .0001;
	
	final double MIN_D = 0;
	final double MAX_D = 1;
	final double MAX_STEP_D = .01;

	public AutoSpeedPIDTune() {
    	requires(Robot.drive);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	writer = null;
    	test_done = false;
    	all_tests_done = false;
    	curr_p = MIN_P;
    	test_started = Robot.drive.startSpeedPIDTuneRun(dir, velocity,  curr_p, curr_i, curr_d, timeout_secs);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if (!test_started) {
        	test_started = Robot.drive.startSpeedPIDTuneRun(dir, velocity,  curr_p, curr_i, curr_d, timeout_secs);
        	if ( test_started ) {
            	/* Open File */
            	Charset charset = Charset.forName("US-ASCII");
        		File file = new File(dir.toString() + Double.toString(velocity) + ".csv");

            	try {
            		// if file doesn't exist, then create it
            		if (!file.exists()) {
        				file.createNewFile();
        			}

        			FileWriter fw = new FileWriter(file.getAbsoluteFile());
        			writer = new BufferedWriter(fw);
            		String header = "Direction,Velocity,P,I,D,Duration\r\n";
            		writer.write(header);
            	} catch (IOException x) {
            	    System.err.format("IOException: %s%n", x);
            	}    	        		
        	}
    	} else {
    		test_done = Robot.drive.isSpeedPIDTuneActive();
    		if ( test_done ) {
    			double time_to_success_seconds = 0.0;
    			boolean success = Robot.drive.getLastSpeedPIDTuneStats(time_to_success_seconds);
				/* Write to File */
		    	try {
		    		String row = dir.toString() + "," +
		    				Double.toString(velocity) + "," +
		    				Double.toString(curr_p) + "," + 
		    				Double.toString(curr_i) + "," + 
		    				Double.toString(curr_d) + "," +
		    				Double.toString(success ? time_to_success_seconds : timeout_secs) +
		    				"\r\n";
		    		writer.write(row);
		    	} catch (IOException x) {
		    	    System.err.format("IOException: %s%n", x);
		    	}
		    	/* Get ready for new test */
		    	if (curr_p <= MAX_P ) {
		    		curr_p += MAX_STEP_P;
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
    		}
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return all_tests_done;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
		all_tests_done = true;
		/* Close the File */
		try {
			String s = "Interrupted!  HELP!\r\n";
			writer.write(s);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}    	
    }
}
