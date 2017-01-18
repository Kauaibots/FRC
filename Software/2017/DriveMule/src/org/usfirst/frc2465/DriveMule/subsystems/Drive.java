// RobotBuilder Version: 1.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc2465.DriveMule.subsystems;

import org.usfirst.frc2465.DriveMule.RobotMap;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.DriveMule.RobotPreferences;
import org.usfirst.frc2465.DriveMule.commands.StickDrive;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.StatusFrameRate;
import com.kauailabs.navx.frc.AHRS;


/**
 *
 */

public class Drive extends PIDSubsystem {
    
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    AHRS imu = RobotMap.imu;
    CANTalon leftFrontSC = RobotMap.driveLeftFrontSC;
    CANTalon leftRearSC = RobotMap.driveLeftRearSC;
    CANTalon rightFrontSC = RobotMap.driveRightFrontSC;
    CANTalon rightRearSC = RobotMap.driveRightRearSC;
    RobotDrive robotDrive = RobotMap.robotDrive;
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS

    static final double cWidth          = 25.0;                 // Distance between left/right wheels
    static final double cLength         = 17.5;                 // Distance btwn front/back wheels
    static final double wheelDiameter   = 10.0;                  // Per AndyMark Specs
    static final int codesPerRev		= 256;
    static final int ticksPerRev 		= 4*codesPerRev;
    static final int num100msPerSec 	= 10;
    static final double motorRPMs 		= 2650.0f;
    static final double transRatio 		= 8.45f;
    static final double wheelRadius     = wheelDiameter / 2;
    static final double disPerRev 		= wheelDiameter * Math.PI;
    static final double pulsePerInch	= ticksPerRev / disPerRev;

/////////////////////////////////////////////////////////////////////////////////////
// Mecanum Constants
/////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
// Proportional translation vs. Rotation
//
// For the same motor speed, the distance of translation and distance of rotation
// are not the same, due to the proportions of the wheel radius, and the 
// distance between front/back and left/right wheels.
//////////////////////////////////////////////////////////

    /* Drive System Orientation Notes */
    /* Positive Y Axis Values:  linear FORWARD motion (towards the front of robot)
     * Positive X Axis Values:  linear RIGHTWARD (STARBOARD) motion ("strafing" - towards the right side of the robot)
     * Positive Z Axis Values:  angular CLOCKWISE motion
     * 
     * Top Down Perspective:  All orientation directions (LEFT/RIGHT, FRONT/BACK) are from a Top-Down Perspective.
     * 
     * Distance Units:  The underlying drive system controllers measure distance/time in 
     * RPM units (when in Speed Mode).
     * 
     * The Drive System Motor orientation is:
     * - Positive Speeds cause motors on the RIGHT side of the robot to spin towards the front.
     * - Positive Speeds cause motors on the LEFT side of the robot to spin towards the back.
     */
    
    static final double cRotK = ((cWidth + cLength)/2) / wheelRadius; // Rotational Coefficient

    /* Axes of Motion:        */
    /* First Column:   X Axis */
    /* Second Column:  Y Axis */
    /* Third Column:   Rotate Axis */
    /* NOTE:  This table assumes positive motion on all motors rotates toward the robot front. */
    static double invMatrix[][] = new double[][] {
        {   1, 1,  cRotK },	/* Left Front  */
        {  -1, 1, -cRotK }, /* Right Front */
        {   1, 1, -cRotK }, /* Right Rear  */
        {  -1, 1,  cRotK }, /* Left Rear   */
    };
       
    CANTalon.TalonControlMode currControlMode;
    int maxOutputSpeed;
    int maxTicksPer100MS;
    int maxRPMsAtWheel;
    double tolerance_degrees;
    boolean fod_enable = true;
    double next_autorotate_value = 0.0;
    boolean auto_stop = false;
    
    /* AutoSpeedPID Tune variables. */
    final int TERM_VELOCITY_THRESHOLD = 5;
    int lfe;
    int rfe;
    int rre;
    int lre;
    
    public enum SpeedPIDTuneDirection { Forward, Strafe, Rotate }    
    
    boolean speed_pid_test_active;
    boolean last_speed_pid_test_success;
    double last_speed_pid_test_time_to_sucess;
    double last_speed_pid_test_start_time;
    double speed_pid_test_timeout_seconds;
    double last_speed_pid_test_duration;
    double last_speed_pid_test_velocity;
    boolean non_zero_error_seen_once;
    double start_moving_time;
    static final double moving_time = 2.0;
    double prev_speed_pid_p;
    double prev_speed_pid_i;
    double prev_speed_pid_d;
    double prev_speed_pid_ff;
    
    public Drive() {
        super(  "Drive",
                RobotPreferences.getAutoRotateP(),
                RobotPreferences.getAutoRotateI(),
                RobotPreferences.getAutoRotateD(),
                0,
                0.02);
        try {
            getPIDController().setContinuous( true );
            getPIDController().setInputRange(-180,180);
            getPIDController().setOutputRange(-1, 1);
            tolerance_degrees = RobotPreferences.getAutoRotateOnTargetToleranceDegrees();
            getPIDController().setAbsoluteTolerance(tolerance_degrees);
            setSetpoint(0.0/*RobotPreferences.getAutoRotateDefaultTargetDegrees()*/);
            disable();
            
            robotDrive.setSafetyEnabled(false);
            
            maxTicksPer100MS = (int)((motorRPMs/transRatio)*ticksPerRev)/num100msPerSec; /* ~20 Feet/Sec */
            maxRPMsAtWheel = (int)(motorRPMs/transRatio);
            
            setMode( CANTalon.TalonControlMode.Speed);
            //setMode(CANTalon.TalonControlMode.PercentVbus);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
      
    }
    
    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    public void initDefaultCommand() {
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND
        // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND
	
        // Set the default command for a subsystem here.
        setDefaultCommand(new StickDrive());
    }
    
    public void setAutoRotateCoefficients(double p, double i, double d, double ff)
    {
    	getPIDController().setPID(p, i, d, ff);
    }

    void initMotor( CANTalon motor, boolean invert_direction ) {
        try {
            if ( currControlMode == CANTalon.TalonControlMode.Speed )
            {
                //motor.configMaxOutputVoltage(12.0);
                motor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
                // Apply Calibrated P,I,D,F Constants
                motor.setPID(8, 0, 0);
                motor.setF(5);
                motor.changeControlMode(CANTalon.TalonControlMode.Speed);
                // In Speed Mode, the Talon Velocity PID Setpoint is in units of RPMs.
                // The RPMs calculation is based upon the codesPerRev configuration.
                motor.configEncoderCodesPerRev(codesPerRev);
                //motor.setCloseLoopRampRate(0);
                //motor.reverseOutput(invert_direction); /* Invert motor direction for Speed Mode */
                //motor.reverseSensor(invert_direction); /* Invert encoder direction for Speed Mode */
            }
            else
            {
            	motor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
                //motor.setInverted(invert_direction); /* Invert motor direction for PercentVbus Mode */
            }
            motor.enableBrakeMode(true);
            motor.setVoltageRampRate(0);
            motor.enableControl();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    public CANTalon.TalonControlMode getMode() {
    	return currControlMode;
    }
    
    public void setMode( CANTalon.TalonControlMode controlMode ) {
        
        currControlMode = controlMode;

        if ( currControlMode == CANTalon.TalonControlMode.Speed )
        {
    		maxOutputSpeed = maxRPMsAtWheel;
        }
        else // kPercentVbus
        {
            maxOutputSpeed = 1;
        }
        
        initMotor(leftFrontSC, false);
        initMotor(rightFrontSC, true); /* Invert direction of right-side motors */
        initMotor(rightRearSC, true);  /* Invert direction of right-side motors */   
        initMotor(leftRearSC, false);
    }    
    
    void mecanumDriveFwdKinematics( double wheelSpeeds[], double velocities[] )
    {
        for ( int i = 0; i < 3; i++ )
        {
            velocities[i] = 0;
            for ( int wheel = 0; wheel < 4; wheel ++ )
            {
                velocities[i] += wheelSpeeds[wheel] * (1 / invMatrix[wheel][i]);
            }
            velocities[i] *= ((double)1.0/4);
        }
    }

    void mecanumDriveInvKinematics( double velocities[], double[] wheelSpeeds)
    {
        for ( int wheel = 0; wheel < 4; wheel ++ )
        {
            wheelSpeeds[wheel] = 0;
            for ( int i = 0; i < 3; i++ )
            {
                wheelSpeeds[wheel] += velocities[i] * invMatrix[wheel][i];
            }
        }
    }    

    
    public void doMecanum( double vX, double vY, double vRot) {
        
        // If auto-rotating, replace vRot with the next
        // calculated value
        
        if ( getAutoRotation() ) {
            vRot = next_autorotate_value;
        }
        
        boolean imu_connected = false;
        if ( imu != null ) { 
        	imu_connected = imu.isConnected();
        }
                
        // Field-oriented drive - Adjust input angle for gyro offset angle
        
        double curr_gyro_angle_degrees = 0;
        if ( fod_enable && imu_connected ) 
        {
                curr_gyro_angle_degrees = imu.getYaw();
        }
        double curr_gyro_angle_radians = curr_gyro_angle_degrees * Math.PI/180;       
          
        double temp = vX * Math.cos( curr_gyro_angle_radians ) + vY * Math.sin( curr_gyro_angle_radians );
        vY = -vX * Math.sin( curr_gyro_angle_radians ) + vY * Math.cos( curr_gyro_angle_radians );
        vX = temp;
        
        try {
            doMecanumInternal(vX, vY, vRot);			
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }

    protected double returnPIDInput() {
        double current_yaw = 0.0;
        if ( imu.isConnected() ) {
            current_yaw = imu.getYaw();
        }
        SmartDashboard.putNumber( "AutoRotatePIDInput", current_yaw);
        return current_yaw;
    }

    protected void usePIDOutput(double d) {
        next_autorotate_value = d;
        SmartDashboard.putNumber( "AutoRotatePIDOutput", next_autorotate_value);
    }
    
    public void setAutoRotation(boolean enable) {
        if ( enable ) {
            getPIDController().enable();
        }
        else {
            getPIDController().disable();
        }
    }
    
    public boolean getAutoRotation() {
        SmartDashboard.putBoolean( "AutoRotateEnabled", getPIDController().isEnabled());
        return getPIDController().isEnabled();
    }
    
    public void setFODEnabled(boolean enabled) {
        fod_enable = enabled;
    }
    
    public boolean getFODEnabled() {
        return fod_enable;
    }
    
    public void configureAutoStop(CANTalon sc, double distance_revolutions, boolean invert) {
    	sc.setPosition(0);
    	
    	/*
    	if ( distance_revolutions > 0 ) {
    		sc.enableLimitSwitch(true, false);
    		sc.setForwardSoftLimit(distance_revolutions);
    		sc.ConfigFwdLimitSwitchNormallyOpen(true);
    		sc.enableForwardSoftLimit(true);
    	} else {
    		sc.enableLimitSwitch(false, true);
    		sc.setReverseSoftLimit(distance_revolutions);
    		sc.ConfigRevLimitSwitchNormallyOpen(true);
    		sc.enableReverseSoftLimit(true);
    	}
    	*/
		sc.enableLimitSwitch(true, true);
		sc.setForwardSoftLimit(invert ? -distance_revolutions : distance_revolutions);
		sc.ConfigFwdLimitSwitchNormallyOpen(true);
		sc.enableForwardSoftLimit(true);
		sc.setReverseSoftLimit(invert ? -distance_revolutions : distance_revolutions);
		sc.ConfigRevLimitSwitchNormallyOpen(true);
		sc.enableReverseSoftLimit(true);

    	sc.enableBrakeMode(true); /* Why is this here??? */
    }
    
    
    public void enableAutoStop(float distance_inches) {
    	if(!auto_stop) {
    		auto_stop = true;
    		double distance_in_revolutions = distance_inches / disPerRev;
    		configureAutoStop(leftFrontSC, distance_in_revolutions, false);
    		configureAutoStop(leftRearSC, distance_in_revolutions, false);
    		configureAutoStop(rightFrontSC, distance_in_revolutions, true);
    		configureAutoStop(rightRearSC, distance_in_revolutions, true);
    	}
    }
    
    public boolean isStopped() {
    	boolean leftFrontStopped = (leftFrontSC.getFaultForSoftLim() != 0) || (leftFrontSC.getFaultRevSoftLim() != 0);
    	boolean leftRearStopped = (leftRearSC.getFaultForSoftLim() != 0) || (leftRearSC.getFaultRevSoftLim() != 0);
    	boolean rightFrontStopped = (rightFrontSC.getFaultForSoftLim() != 0) || (rightFrontSC.getFaultRevSoftLim() != 0);
    	boolean rightRearStopped = (rightRearSC.getFaultForSoftLim() != 0) || (rightRearSC.getFaultRevSoftLim() != 0);
    	boolean stopped = leftFrontStopped && leftRearStopped && rightFrontStopped && rightRearStopped;
    	return stopped;
    }
    
    public void undoAutoStop(CANTalon sc) {
    	sc.enableForwardSoftLimit(false);
    	sc.enableReverseSoftLimit(false);
		sc.enableLimitSwitch(false, false);
    }
    
    public void disableAutoStop() {
    	if(auto_stop) {
    		auto_stop = false;
    		undoAutoStop(leftFrontSC);
    		undoAutoStop(leftRearSC);
    		undoAutoStop(rightFrontSC);
    		undoAutoStop(rightRearSC);
    	}
    }
    
    public boolean startSpeedPIDTuneRun( SpeedPIDTuneDirection dir, double vel_ratio, 
    								  double p, double i, double d, double ff, double timeout_seconds ) {
    	
    	/* Verify wheel encoder velocities are zero. */
    	if (speed_pid_test_active || !isAvgWheelVelocityAtStopped()) {
    		return false;
    	}
    	
    	prev_speed_pid_p = leftFrontSC.getP();
    	prev_speed_pid_i = leftFrontSC.getI();
    	prev_speed_pid_d = leftFrontSC.getD();
    	prev_speed_pid_ff = leftFrontSC.getF();
    	
    	speed_pid_test_active = true;
        last_speed_pid_test_success = false;
        last_speed_pid_test_time_to_sucess = 0.0;
        last_speed_pid_test_start_time = Timer.getFPGATimestamp();
        last_speed_pid_test_duration = 0;
        non_zero_error_seen_once = false;
        
        speed_pid_test_timeout_seconds = timeout_seconds;

    	/* reprogram PID values */
        prepMotorForSpeedPIDTuning(leftFrontSC,p,i,d,ff);
        prepMotorForSpeedPIDTuning(rightFrontSC,p,i,d,ff);
        prepMotorForSpeedPIDTuning(rightRearSC,p,i,d,ff);
        prepMotorForSpeedPIDTuning(leftRearSC,p,i,d,ff);            
        
        /* start the motors */
        last_speed_pid_test_velocity = vel_ratio;

        double vX = (dir == SpeedPIDTuneDirection.Strafe) ? vel_ratio : 0;
        double vY = (dir == SpeedPIDTuneDirection.Forward) ? vel_ratio : 0;
        double vRot = (dir == SpeedPIDTuneDirection.Rotate) ? vel_ratio : 0;        
        
        doMecanumInternal(vX, vY, vRot);
        
        return true;
    }

	private void doMecanumInternal(double vX, double vY, double vRot) {
		
    	/* Scale input values if any of them exceeds 1.0*/
        double excessRatio = (double)1.0 / ( Math.abs(vX) + Math.abs(vY) + Math.abs(vRot) );
        if ( excessRatio < 1.0 )
        {
            vX      *= excessRatio;
            vY      *= excessRatio;
            vRot    *= excessRatio;
        }		
		
		vRot *= (1/cRotK);
        
        SmartDashboard.putNumber( "vRot", vRot);
        
        double wheelSpeeds[] = new double[4];
        double velocities[] = new double[3];
        velocities[0] = vX;
        velocities[1] = vY;
        velocities[2] = vRot;       
               
        mecanumDriveInvKinematics( velocities, wheelSpeeds );
        
        double left_front_speed = maxOutputSpeed * wheelSpeeds[0];
        double right_front_speed = maxOutputSpeed * wheelSpeeds[1] * -1;
        double right_rear_speed = maxOutputSpeed * wheelSpeeds[2] * -1;
        double left_rear_speed = maxOutputSpeed * wheelSpeeds[3];
        
        leftFrontSC.set(left_front_speed);
        rightFrontSC.set(right_front_speed);
        leftRearSC.set(left_rear_speed);
        rightRearSC.set(right_rear_speed);
        
        SmartDashboard.putNumber( "SpeedOut_FrontLeft", left_front_speed);
        SmartDashboard.putNumber( "SpeedOut_FrontRight", right_front_speed);
        SmartDashboard.putNumber( "SpeedOut_RearRight", right_rear_speed);
        SmartDashboard.putNumber( "SpeedOut_RearLeft", left_rear_speed);
        
        /*
        SmartDashboard.putNumber( "Speed_FrontLeft", leftFrontSC.getEncVelocity());
        SmartDashboard.putNumber( "Speed_RearLeft", leftRearSC.getEncVelocity());
        SmartDashboard.putNumber( "Speed_FrontRight", rightFrontSC.getEncVelocity());
        SmartDashboard.putNumber( "Speed_RearRight", rightRearSC.getEncVelocity());
        */
        SmartDashboard.putNumber( "Speed_FrontLeft", leftFrontSC.getSpeed());
        SmartDashboard.putNumber( "Speed_RearLeft", leftRearSC.getSpeed());
        SmartDashboard.putNumber( "Speed_FrontRight", rightFrontSC.getSpeed());
        SmartDashboard.putNumber( "Speed_RearRight", rightRearSC.getSpeed());    
        
        SmartDashboard.putNumber( "SpeedRaw_FrontLeft", leftFrontSC.getEncVelocity());
        SmartDashboard.putNumber( "SpeedRaw_RearLeft", leftRearSC.getEncVelocity());
        SmartDashboard.putNumber( "SpeedRaw_FrontRight", rightFrontSC.getEncVelocity());
        SmartDashboard.putNumber( "SpeedRaw_RearRight", rightRearSC.getEncVelocity());    
        
        SmartDashboard.putNumber("Position_FrontLeft", leftFrontSC.getPosition());
        SmartDashboard.putNumber("Position_FrontRight", rightFrontSC.getPosition());
        SmartDashboard.putNumber("Position_RearRight", rightRearSC.getPosition());        
        SmartDashboard.putNumber("Position_RearLeft", leftRearSC.getPosition());
        
        SmartDashboard.putString("SoftLimit_FrontLeft", ((leftFrontSC.getFaultForSoftLim() != 0) ? "Fwd/" : "/") + 
        	((leftFrontSC.getFaultRevSoftLim() != 0) ? "Rev" : ""));
        SmartDashboard.putString("SoftLimit_FrontRight", ((rightFrontSC.getFaultForSoftLim() != 0) ? "Fwd/" : "/") + 
            	((rightFrontSC.getFaultRevSoftLim() != 0) ? "Rev" : ""));
        SmartDashboard.putString("SoftLimit_RearRight", ((rightRearSC.getFaultForSoftLim() != 0) ? "Fwd/" : "/") + 
            	((rightRearSC.getFaultRevSoftLim() != 0) ? "Rev" : ""));
        SmartDashboard.putString("SoftLimit_RearLeft", ((leftRearSC.getFaultForSoftLim() != 0) ? "Fwd/" : "/") + 
            	((leftRearSC.getFaultRevSoftLim() != 0) ? "Rev" : ""));
	}
    
    public void prepMotorForSpeedPIDTuning( CANTalon motor, double p, double i, double d, double ff) {
        motor.setFeedbackDevice(FeedbackDevice.QuadEncoder); //motor.setSpeedMode(CANTalon.kQuadEncoder, 256, .4, .01, 0);
    	//We don't tell the motor controller the number of ticks per encoder revolution
        //The Talon needs to be told the number of encoder ticks per 10 ms to rotate
        motor.setPID(p,i,d);
        motor.setF(ff);
        //motor.setCloseLoopRampRate(rampRate);
        //motor.setProfile(profile_int);
        //motor.setIZone(izone_int);
        motor.changeControlMode(CANTalon.TalonControlMode.Speed);    
        motor.setStatusFrameRateMs(StatusFrameRate.QuadEncoder, 10);
        motor.setStatusFrameRateMs(StatusFrameRate.General, 10);
        motor.setStatusFrameRateMs(StatusFrameRate.Feedback, 10);
        motor.enableForwardSoftLimit(false);
    	motor.enableReverseSoftLimit(false);
        motor.configEncoderCodesPerRev(codesPerRev);
    }
    
    public int getAvgWheelClosedLoopError() {
        lfe = leftFrontSC.getClosedLoopError();
        rfe = rightFrontSC.getClosedLoopError();
        rre = rightRearSC.getClosedLoopError();
        lre = leftRearSC.getClosedLoopError();

    	SmartDashboard.putNumber("RightFrontWheelSpeedClosedLoopError", lfe);        
        
        int avg = (Math.abs(lfe) + Math.abs(rfe) + Math.abs(rre) + Math.abs(lre)) / 4;
        
        return avg;
    }
    
    public boolean isAvgWheelVelocityAtTarget() {
    	int avg = getAvgWheelClosedLoopError();
    	SmartDashboard.putNumber("AvgWheelSpeedClosedLoopError", avg);    	
        return (avg <= TERM_VELOCITY_THRESHOLD);
    }
    
    final int STOPPED_VELOCITY_THRESHOLD = 3;
    
    public boolean isAvgWheelVelocityAtStopped() {
        int lfv = leftFrontSC.getEncVelocity();
        int rfv = rightFrontSC.getEncVelocity();
        int rrv = rightRearSC.getEncVelocity();
        int lrv = leftRearSC.getEncVelocity();

        int avg = (lfv + rfv + rrv + lrv) / 4;
 
        return (avg <= STOPPED_VELOCITY_THRESHOLD);
    }
    
    public boolean isSpeedPIDTuneActive() {
    	boolean stop = false;
    	if ( speed_pid_test_active ) {
    		/* check and see if either error is 0, or timeout has occurred. */
    		double test_duration = Timer.getFPGATimestamp() - last_speed_pid_test_start_time;
    		if ( test_duration > speed_pid_test_timeout_seconds ) {
    			/* Timeout */
    			speed_pid_test_active = false;
    			last_speed_pid_test_duration = speed_pid_test_timeout_seconds;
    			stop = true;
    		} else {
    			if ( !non_zero_error_seen_once ) {
    				if ( getAvgWheelClosedLoopError() > TERM_VELOCITY_THRESHOLD ) {
    					non_zero_error_seen_once = true;
    					start_moving_time = Timer.getFPGATimestamp();
    				}
    			} else {
    				boolean at_target = isAvgWheelVelocityAtTarget();
	    			if ( at_target || (Timer.getFPGATimestamp() - start_moving_time) > moving_time ) {
	    				speed_pid_test_active = false;
	    				last_speed_pid_test_duration = test_duration;
	    				stop = true;
	    				if ( at_target ) {
	    					stop = at_target;
	    				}
	    			}
    			}
    		}
    		if ( stop ) {
	    		/* stop the motors */
	    		leftFrontSC.set(0.0);
	    		rightFrontSC.set(0.0);
	    		rightRearSC.set(0.0);
	    		leftRearSC.set(0.0);
	    		/* restore Speed PID Coefficients. */
	            prepMotorForSpeedPIDTuning(leftFrontSC,prev_speed_pid_p,prev_speed_pid_i,prev_speed_pid_d,prev_speed_pid_ff);
	            prepMotorForSpeedPIDTuning(rightFrontSC,prev_speed_pid_p,prev_speed_pid_i,prev_speed_pid_d,prev_speed_pid_ff);
	            prepMotorForSpeedPIDTuning(rightRearSC,prev_speed_pid_p,prev_speed_pid_i,prev_speed_pid_d,prev_speed_pid_ff);
	            prepMotorForSpeedPIDTuning(leftRearSC,prev_speed_pid_p,prev_speed_pid_i,prev_speed_pid_d,prev_speed_pid_ff);
    		}
    	}
    	return speed_pid_test_active;
    }
        
    public boolean getLastSpeedPIDTuneStats( Double time_to_success_seconds ) {        	
    	time_to_success_seconds = last_speed_pid_test_duration;
    	return last_speed_pid_test_success;
    }

}

