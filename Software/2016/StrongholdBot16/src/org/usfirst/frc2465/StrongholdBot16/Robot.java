// RobotBuilder Version: 1.5
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc2465.StrongholdBot16;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.StrongholdBot16.subsystems.Drive;
import org.usfirst.frc2465.StrongholdBot16.subsystems.Vision.DetectionInfo;
import org.usfirst.frc2465.StrongholdBot16.commands.*;
import org.usfirst.frc2465.StrongholdBot16.subsystems.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

    Command autonomousCommand;
    SendableChooser autoChooser;

    public static OI oi;
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    public static Wrist wrist;
    public static Elbow elbow;
    public static Shoulder shoulder;
    public static Turret turret;
    public static Arm arm;
    public static DriverCamera driverCamera;
    public static Ultrasonic ultrasonic;
    public static Infrared infrared;
    public static Vision vision;
    public static Drive drive;
    public static BallControl ballcontrol;
    public static ProximitySensor proximitySensor;
    public static Wedge wedge;

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS

    Alliance alliance;
    boolean teleop_countdown;
    
    public enum RobotPosition {
        kFlat(0),
        kOn_Ramp(1),
        kSkewed(2);
        
        private int value;
        
        private RobotPosition(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
    }    
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    RobotMap.init();
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        wrist = new Wrist();
        elbow = new Elbow();
        shoulder = new Shoulder();
        turret = new Turret();
        arm = new Arm();
        driverCamera = new DriverCamera();
        ultrasonic = new Ultrasonic(SerialPort.Port.kMXP);
        infrared = new Infrared();
        vision = new Vision();
        drive = new Drive();
        ballcontrol = new BallControl();
        wedge = new Wedge();
        

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        // OI must be constructed after subsystems. If the OI creates Commands 
        //(which it very likely will), subsystems are not guaranteed to be 
        // constructed yet. Thus, their requires() statements may grab null 
        // pointers. Bad news. Don't move it.
        oi = new OI();

        // instantiate the command used for the autonomous period
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=AUTONOMOUS
        autonomousCommand = null;
        autoChooser = new SendableChooser();
        autoChooser.addDefault("Reach", new Reach());
        autoChooser.addObject("LowBar", new LowBar());
        autoChooser.addObject("Moat", new Moat());
        autoChooser.addObject("Ramparts", new Ramparts());
        autoChooser.addObject("Porta", new Porta());
        autoChooser.addObject("RoughTerrain", new RoughTerrain());
        autoChooser.addDefault("ChivalDeFries", new ChivalDeFries());
        autoChooser.addObject("LowBarAndBall", new LowBarAndBall());
        autoChooser.addObject("LowBarSlide 1", new LowBarSlide(1));
        autoChooser.addObject("LowBarSlide 2", new LowBarSlide(2));
        SmartDashboard.putData("Autonomous Chooser", autoChooser);
        

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=AUTONOMOUS        
    }

    /**
     * This function is called when the disabled button is hit.
     * You can use it to reset subsystems before shutting down.
     */
    public void disabledInit(){
    	Robot.vision.disableDetection();
    }

    public void disabledPeriodic() {
        Scheduler.getInstance().run();
        updateDashboard();
    }

    public void enableVisionProcessing() {
        /* Configure vision processing algorithm based upon current alliance. */
        /* Red Alliance:  Detect Blue Lights on Shield/Tower. */
        /* Blue Alliance:  Detect Red Lights on Shield/Tower. */
        /* NOTE:  In both cases, retro-reflective target tracking is performed. */
        
        alliance = DriverStation.getInstance().getAlliance();
        Vision.DetectAlgorithm algorithm = Vision.DetectAlgorithm.TOWER_AND_SHIELDS_BLUE;
        if ( alliance == Alliance.Blue ) {
        	algorithm = Vision.DetectAlgorithm.TOWER_AND_SHIELDS_RED;
        }
        boolean enable_stream = true;
        boolean enable_file = true;
    	Robot.vision.enableDetection(algorithm, enable_stream, enable_file);    	
    }
    
    public void autonomousInit() {
        // schedule the autonomous command (example)
        autonomousCommand = (Command) autoChooser.getSelected();
        if(autonomousCommand != null){
        	autonomousCommand.start();
        }
        enableVisionProcessing();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
        updateDashboard();
    }

    public void teleopInit() {
        // This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to 
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
        enableVisionProcessing();
        double seconds_remaining = DriverStation.getInstance().getMatchTime();
        if ( seconds_remaining > 15 ) {
        	teleop_countdown = true;
        } else {
        	teleop_countdown = false;
        }
        /* TODO:  This is a hack to deal with autonomous programs that incorrectly
         * leave drive system in FOD disabled mode.  Remove it? */
        Robot.drive.setFODEnabled(true); 
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
        updateDashboard();
    	/* If less than 2 seconds remain in the teleop period, disable */
        /* the vision processor, allowing the final writes to disk to  */
        /* occur (helping protected against corrupted files when the   */
        /* robot is turned off.                                        */
        double seconds_remaining = DriverStation.getInstance().getMatchTime();
    	if (teleop_countdown && ( seconds_remaining < 2 )) {
    		Robot.vision.disableDetection();
    	}
    }

    public void testPeriodic() {
        LiveWindow.run();
    }
    
    public void updateDashboard() {
        // Drive
    	SmartDashboard.putBoolean("UltrasonicConnected", Robot.ultrasonic.isConnected());
    	SmartDashboard.putNumber("FrontLeftDistanceCM", Robot.ultrasonic.getFrontLeftDistanceCM());
    	SmartDashboard.putNumber("FrontRightDistanceCM", Robot.ultrasonic.getFrontRightDistanceCM());
    	SmartDashboard.putNumber("BackLeftDistanceCM", Robot.ultrasonic.getBackLeftDistanceCM());
    	SmartDashboard.putNumber("BackRightDistanceCM", Robot.ultrasonic.getBackRightDistanceCM());
    	SmartDashboard.putNumber("FrontCenterDistanceCM", Robot.ultrasonic.getFrontCenterDistanceCM());
    	
    	SmartDashboard.putNumber("BallIRSensorInches", RobotMap.infraredBallGrabberSensor.getDistanceInches());
    	//SmartDashboard.putNumber("FrontIRSensor", RobotMap.infraredFrontSensor.getDistanceInches());
    	
    	//SmartDashboard.putBoolean("IsAgressive", value);
        
        if(RobotMap.imu != null)
        {
	    	SmartDashboard.putBoolean("IMU_Connected",      RobotMap.imu.isConnected());
	        SmartDashboard.putNumber( "IMU_Yaw",            RobotMap.imu.getYaw());
	        SmartDashboard.putNumber( "IMU_CompassHeading", RobotMap.imu.getCompassHeading());
	        SmartDashboard.putNumber("Pitch_X", RobotMap.imu.getPitch());
	        SmartDashboard.putNumber("Roll_Y", RobotMap.imu.getRoll());
        }
        
        if(Robot.drive != null)
        {
        	SmartDashboard.putBoolean("FOD_Enabled",        Robot.drive.getFODEnabled() );
        }
        
        SmartDashboard.putBoolean("GPUConnected", Robot.vision.isConnected());
        SmartDashboard.putString("GPUHostname", Robot.vision.getHostname());
        SmartDashboard.putString("GPUIpAddress", Robot.vision.getIpAddress());
        SmartDashboard.putNumber("GPUCamera",  
        		(Robot.vision.getCurrentCamera() == Vision.CameraSelector.FRONT_CAMERA) ? 0 :1);
        SmartDashboard.putBoolean("GPUDetecting", Robot.vision.isDetecting());
        
        Vision.DetectionInfo target = Robot.vision.new DetectionInfo();
        Vision.DetectionInfo tower = Robot.vision.new DetectionInfo();
        Vision.DetectionInfo shield = Robot.vision.new DetectionInfo();
        Robot.vision.getTargetDetectionInfo(target);
        Robot.vision.getTowerDetectionInfo(tower);
        Robot.vision.getShieldDetectionInfo(shield);
        SmartDashboard.putBoolean("GPUTowerDetect", tower.detected);
        SmartDashboard.putNumber("GPUTowerAngle",  tower.angle_degrees);
        SmartDashboard.putBoolean("GPUTargetDetect", target.detected);
        SmartDashboard.putNumber("GPUTargetAngle",  target.angle_degrees);
        SmartDashboard.putNumber("GPUTargetDistanceInches",  target.distance_inches);
        SmartDashboard.putBoolean("GPUShieldDetect", shield.detected);
        SmartDashboard.putNumber("GPUShieldAngle", shield.angle_degrees);
        
        updateLights();
    }
    
    static final int LIGHT_FLAT = 0x0001;
    static final int LIGHT_ON_RAMP = 0x0002;
    static final int LIGHT_SKEWED = 0x0004;
    static final int TOWER_DETECT = 0x0008;
    static final int TARGET_DETECT = 0x0010;
    static final int SHIELD_DETECT = 0x0020;
    
    public void updateLights() {
        RobotPosition pos = getRobotPosition();  
        int on_position_light_number = LIGHT_FLAT;
        if ( pos == RobotPosition.kOn_Ramp) {
        	on_position_light_number = LIGHT_ON_RAMP;
        } else if ( pos == RobotPosition.kSkewed) {
        	on_position_light_number = LIGHT_SKEWED;
        }
        Vision.DetectionInfo target = Robot.vision.new DetectionInfo();
        Vision.DetectionInfo tower = Robot.vision.new DetectionInfo();
        Vision.DetectionInfo shield = Robot.vision.new DetectionInfo();
        Robot.vision.getTargetDetectionInfo(target);
        Robot.vision.getTowerDetectionInfo(tower);
        Robot.vision.getShieldDetectionInfo(shield);
        if ( target.detected ) { 
        	on_position_light_number |= TARGET_DETECT;
        }
        if ( tower.detected ) {
        	on_position_light_number |= TOWER_DETECT;
        }
        if ( shield.detected ) {
        	on_position_light_number |= SHIELD_DETECT;
        }
    	oi.getLights().setOutput(on_position_light_number, true);            	
    }
    
    public RobotPosition getRobotPosition()
    { 
    	float pitch = RobotMap.imu.getPitch();
    	float roll = RobotMap.imu.getRoll();
    	RobotPosition position = RobotPosition.kFlat;
    	//FLAT
    	if(roll < 2 || roll > 0 && pitch < 2 || pitch > 0)
    	{
    		position = RobotPosition.kFlat; 
    	}
    	//ON_RAMP DETECTION
    	if(roll < 3 || roll > 0)
    	{
    		position = RobotPosition.kOn_Ramp; 
    	}
    	//SKEWED
    	if(roll < 6 || roll > 0 && pitch < 6 || pitch > 0)
    	{
    		position = RobotPosition.kSkewed; 
    	}
    	return position;
    }
}
