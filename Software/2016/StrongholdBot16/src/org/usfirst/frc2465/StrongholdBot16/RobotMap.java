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
    
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import java.util.Vector;

import org.usfirst.frc2465.StrongholdBot16.subsystems.AngleSensor;
import org.usfirst.frc2465.StrongholdBot16.subsystems.LidarLITE;
import org.usfirst.frc2465.StrongholdBot16.subsystems.MaxbotixSensor;
import org.usfirst.frc2465.StrongholdBot16.subsystems.ProximitySensor;

import com.kauailabs.navx.frc.AHRS;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */

/* Scott Libert, 2/23/2016:  Updated to work w/competition robot on stop-build day. */
public class RobotMap {
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    public static DigitalInput wristHome;
    public static SpeedController wristSpeedController;
    public static DigitalInput wristAngleData;
    public static DigitalOutput wristAngleClock;
    public static DigitalOutput wristAngleChipSelect;
    public static AngleSensor wristAngleSensor;
    public static DigitalInput elbowHighLimit;
    public static DigitalInput elbowLowLimit;
    public static SpeedController elbowSpeedController;
    public static DigitalInput elbowAngleData;
    public static DigitalOutput elbowAngleClock;
    public static DigitalOutput elbowAngleChipSelect;
    public static AngleSensor elbowAngleSensor;    
    public static DigitalInput shoulderHighLimit;
    public static DigitalInput shoulderLowLimit;
    public static SpeedController shoulderSpeedController;
    public static DigitalInput shoulderAngleData;
    public static DigitalOutput shoulderAngleClock;
    public static DigitalOutput shoulderAngleChipSelect;
    public static AngleSensor shoulderAngleSensor;        
    public static AnalogInput turretHighLimit;
    public static AnalogInput turretLowLimit;
    public static DigitalInput turretHomeLimit;
    public static SpeedController turretSpeedController;
    public static DigitalInput turretEncoderA;
    public static DigitalInput turretEncoderB;
    public static DigitalInput turretEncoderIndex;
    public static Encoder turretEncoder;
    public static AnalogInput infraredFrontInput;
    public static AnalogInput infraredBackInput;
    public static AnalogInput infraredBallGrabber;
    public static Relay visionRingLightRelay;
    public static SerialPort serialPort;
    public static AHRS imu;
    public static CANTalon driveLeftFrontSC;
    public static CANTalon driveRightFrontSC;
    public static CANTalon driveLeftRearSC;
    public static CANTalon driveRightRearSC;
    public static RobotDrive robotDrive;
    public static ProximitySensor infraredFrontSensor;
    public static ProximitySensor infraredBackSensor;
    public static ProximitySensor infraredBallGrabberSensor;
    public static VictorSP ballGrabSpeedController;
    public static AnalogInput ballGrabLimit;
    public static DigitalInput wedgeLowLimit;
    public static DigitalInput wedgeHighLimit;
    public static VictorSP wedgeSpeedController;
    

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS

    public static void init() {
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        //wristHome = new DigitalInput(13);
        //LiveWindow.addSensor("Wrist", "Home", wristHome);
        
        //wristSpeedController = new Talon(0);
        //LiveWindow.addActuator("Wrist", "SpeedController", (Talon) wristSpeedController);
        
        wedgeLowLimit = new DigitalInput(0);
        LiveWindow.addSensor("Wedge", "LowLimit", wedgeLowLimit);

        wedgeHighLimit = new DigitalInput(1);
        LiveWindow.addSensor("Wedge", "HighLimit", wedgeHighLimit);
        
        wedgeSpeedController = new VictorSP(5);
        LiveWindow.addActuator("Wedge", "SpeedController", wedgeSpeedController);
        
        //wristAngleData = new DigitalInput(0);
        //LiveWindow.addSensor("Wrist", "AngleData", wristAngleData);
        
        //wristAngleClock = new DigitalOutput(1);
        //LiveWindow.addActuator("Wrist", "AngleClock", wristAngleClock);
        
        //wristAngleChipSelect = new DigitalOutput(2);
        //LiveWindow.addActuator("Wrist", "AngleChipSelect", wristAngleChipSelect);
        
        //wristAngleSensor = new AngleSensor(wristAngleData, wristAngleChipSelect, wristAngleClock);
        //LiveWindow.addSensor("Wrist", "AngleSensor", wristAngleSensor);
        
        //elbowHighLimit = new DigitalInput(19);
        //LiveWindow.addSensor("Elbow", "HighLimit", elbowHighLimit);
        
        //elbowLowLimit = new DigitalInput(20);
        //LiveWindow.addSensor("Elbow", "LowLimit", elbowLowLimit);
        
        //elbowSpeedController = new Talon(1);
        //LiveWindow.addActuator("Elbow", "SpeedController", (Talon) elbowSpeedController);
        
        //elbowAngleData = new DigitalInput(3);
        //LiveWindow.addSensor("Elbow", "AngleData", elbowAngleData);
        
        //elbowAngleClock = new DigitalOutput(4);
        //LiveWindow.addActuator("Elbow", "AngleClock", elbowAngleClock);
        
        //elbowAngleChipSelect = new DigitalOutput(5);
        //LiveWindow.addActuator("Elbow", "AngleChipSelect", elbowAngleChipSelect);
        
        //elbowAngleSensor = new AngleSensor(elbowAngleData, elbowAngleChipSelect, elbowAngleClock);
        //LiveWindow.addSensor("Elbow", "AngleSensor", elbowAngleSensor);
        
        //shoulderHighLimit = new DigitalInput(21);
        //LiveWindow.addSensor("Shoulder", "HighLimit", shoulderHighLimit);
        
        //shoulderLowLimit = new DigitalInput(22);
        //LiveWindow.addSensor("Shoulder", "LowLimit", shoulderLowLimit);
        
        //shoulderSpeedController = new Talon(2);
        //LiveWindow.addActuator("Shoulder", "SpeedController", (Talon) shoulderSpeedController);
        
        //shoulderAngleData = new DigitalInput(6);
        //LiveWindow.addSensor("Shoulder", "AngleData", shoulderAngleData);
        
        //shoulderAngleClock = new DigitalOutput(7);
        //LiveWindow.addActuator("Shoulder", "AngleClock", shoulderAngleClock);
        
        //shoulderAngleChipSelect = new DigitalOutput(8);
        //LiveWindow.addActuator("Shoulder", "AngleChipSelect", shoulderAngleChipSelect);
        
        //shoulderAngleSensor = new AngleSensor(shoulderAngleData, shoulderAngleChipSelect, shoulderAngleClock);
        //LiveWindow.addSensor("Shoulder", "AngleSensor", shoulderAngleSensor);                
        
        //turretHighLimit = new AnalogInput(6);
        //LiveWindow.addSensor("Turret", "HighLimit", turretHighLimit);
        
        //turretLowLimit = new AnalogInput(7);
        //LiveWindow.addSensor("Turret", "LowLimit", turretLowLimit);
        
        //turretHomeLimit = new DigitalInput(9);
        //LiveWindow.addSensor("Turret", "HomeLimit", turretHomeLimit);
        
        //turretSpeedController = new Talon(3);
        //LiveWindow.addActuator("Turret", "SpeedController", (Talon) turretSpeedController);
        
        //turretEncoderA = new DigitalInput(10);
        //LiveWindow.addSensor("Turret", "AngleData", turretEncoderA);
        
        //turretEncoderB = new DigitalInput(11);
        //LiveWindow.addActuator("Turret", "AngleClock", turretEncoderB);
        
        //turretEncoderIndex = new DigitalInput(12);
        //LiveWindow.addActuator("Turret", "AngleChipSelect", turretEncoderIndex);
        
        //turretEncoder = new Encoder(turretEncoderA, turretEncoderB, turretEncoderIndex);
        //LiveWindow.addSensor("Turret", "Encoder", turretEncoder);
      
        //infraredFrontInput = new AnalogInput(0);
        
        //infraredBackInput = new AnalogInput(1);
        
        infraredBallGrabber = new AnalogInput(2);
        
        //infraredFrontSensor = new ProximitySensor(infraredFrontInput, ProximitySensor.kMediumRange);
        //LiveWindow.addSensor("Infrared", "FrontSensor", infraredFrontSensor);

        //infraredBackSensor = new ProximitySensor(infraredBackInput, ProximitySensor.kMediumRange);
        //LiveWindow.addSensor("Infrared", "BackSensor", infraredBackSensor);
        
        infraredBallGrabberSensor = new ProximitySensor(infraredBallGrabber, ProximitySensor.kMediumRange);
        LiveWindow.addSensor("Infrared", "BallGrabber", infraredBallGrabberSensor);
        
        visionRingLightRelay = new Relay(1);
        LiveWindow.addActuator("Vision", "RingLightRelay", visionRingLightRelay);

        ballGrabSpeedController = new VictorSP(4);
        LiveWindow.addActuator("BallGrab", "SpeedController", ballGrabSpeedController);        
        
        try { 
            driveLeftFrontSC = new CANTalon(1);
            LiveWindow.addActuator("Drive", "LeftFrontSC", driveLeftFrontSC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	
        
        try { 
            driveRightFrontSC = new CANTalon(2);
            LiveWindow.addActuator("Drive", "RightFrontSC", driveRightFrontSC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	
        
        try { 
            driveLeftRearSC = new CANTalon(4);
            LiveWindow.addActuator("Drive", "LeftRearSC", driveLeftRearSC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	
        
        try { 
            driveRightRearSC = new CANTalon(3);
            LiveWindow.addActuator("Drive", "RightRearSC", driveRightRearSC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try {
	        robotDrive = new RobotDrive(driveLeftFrontSC, driveLeftRearSC,
	              driveRightFrontSC, driveRightRearSC);
	        robotDrive.setSafetyEnabled(true);
	        robotDrive.setExpiration(0.1);
	        robotDrive.setSensitivity(0.5);
	        robotDrive.setMaxOutput(1.0);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        try {
            imu = new AHRS(SPI.Port.kMXP);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if ( imu != null ) {
            LiveWindow.addSensor("IMU", "Gyro", imu);
        }

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
    }
}
