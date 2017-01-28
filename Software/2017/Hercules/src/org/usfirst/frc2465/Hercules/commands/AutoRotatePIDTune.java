package org.usfirst.frc2465.Hercules.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class AutoRotatePIDTune extends Command {

	public static final double MIN_P = .002;
	public static final double MAX_P = .05;
	public static final double STEP_P = .002;

	public static final double MIN_I = 0;
	public static final double MAX_I = 1;
	public static final double STEP_I = .0001;
	
	public static final double MIN_D = 0;
	public static final double MAX_D = 4.0;
	public static final double STEP_D = .2;
	
	public static final double MIN_FF = 0;
	public static final double MAX_FF = 0;
	public static final double STEP_FF = 0.2;
	
	double curr_p;
	double curr_i;
	double curr_d;
	double curr_ff;
    
    double timeout_secs = 5;
    double target_angle_degrees = 170;
	
    boolean test_started;
	boolean test_done;
	boolean all_tests_done;
	boolean delay;
	double delay_start;
	double test_start_timestamp;
	
	final double delay_period_seconds = 2.0;
	
	BufferedWriter writer;

	public AutoRotatePIDTune() {
    	requires(Robot.drive);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	writer = null;
    	test_started = false;
    	test_done = false;
    	delay = false;
    	all_tests_done = false;
    	curr_p = MIN_P;
    	curr_i = 0;
    	curr_d = MIN_D;
    	curr_ff = MIN_FF;
    	Robot.drive.setFODEnabled(false);
    }

    protected void outputToDashboard(){
    	SmartDashboard.putBoolean("AutoRotatePIDTune_TestInProgress", test_started);
     	SmartDashboard.putNumber("AutoRotatePIDTune_P", curr_p);
    	SmartDashboard.putNumber("AutoRotatePIDTune_I", curr_i);
    	SmartDashboard.putNumber("AutoRotatePIDTune_D", curr_d);
    	SmartDashboard.putNumber("AutoRotatePIDTune_FF", curr_ff);
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
    		
    		RobotMap.imu.zeroYaw();
    		Robot.drive.setSetpoint(target_angle_degrees);
    		Timer.delay(0.1);
    		Robot.drive.setAutoRotateCoefficients(curr_p, curr_i, curr_d, curr_ff);
    		Robot.drive.setAutoRotation(true);
			SmartDashboard.putBoolean("AutoRotatePIDTune_OnTarget", false);
        	test_started = true;
        	if ( test_started ) {
        		test_start_timestamp = Timer.getFPGATimestamp();
            	/* Open File */
        		File file = new File("AutoRotate" + Double.toString(target_angle_degrees) + "_degrees" + ".csv");

            	try {
            		// if file doesn't exist, then create it
            		if (!file.exists()) {
        				file.createNewFile();
        			}

        			FileWriter fw = new FileWriter(file.getAbsoluteFile());
        			writer = new BufferedWriter(fw);
            		String header = "AngularError,P,I,D,FF,Duration\r\n";
            		writer.write(header);
            	} catch (IOException x) {
            	    System.err.format("IOException: %s%n", x);
            	}    	        		
        	}
    	} else {
			double angular_error = Robot.drive.getPIDController().getError();
        	SmartDashboard.putNumber("AutoRotatePIDTune_AngularError", angular_error);
    		boolean on_target = (Math.abs(angular_error) < 2.0);
			SmartDashboard.putBoolean("AutoRotatePIDTune_OnTarget", on_target);
    		double timestamp_now = Timer.getFPGATimestamp();
    		test_done = (/*on_target || */((timestamp_now - test_start_timestamp) >= timeout_secs));   
    		if ( test_done ) {
        		Robot.drive.setSetpoint(0.0);
    			Robot.drive.setAutoRotation(false);
    			double test_duration = timestamp_now - test_start_timestamp;
    			boolean success = (test_duration < timeout_secs);
    			Double time_to_success_seconds = 0.0;
				/* Write to File */

		    	try {
		    		String row = Double.toString(angular_error) + "," +
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
    	Robot.drive.doMecanum(0,0,0);
    	outputToDashboard();
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return all_tests_done;
    }

    // Called once after isFinished returns true
    protected void end() {
		all_tests_done = true;
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
