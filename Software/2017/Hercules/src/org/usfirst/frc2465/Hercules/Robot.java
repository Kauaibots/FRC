// RobotBuilder Version: 2.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc2465.Hercules;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc2465.Hercules.subsystems.Drive;

import com.kauailabs.navx.frc.AHRS;

import org.usfirst.frc2465.Hercules.Robot;
import org.usfirst.frc2465.Hercules.commands.*;
import org.usfirst.frc2465.Hercules.subsystems.*;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot implements PIDOutput {

    Command autonomousCommand;
    SendableChooser<Command> autoChooser;
    public static Drive drive;


    public static OI oi;
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    public static Pincher pincher;
    public static Funnel funnel;
    public static Winch winch;
    public static Air air;
    public static Vision vision;
    public static NetworkTable table;
    public static PIDController strafeController;
    public static AHRS ahrs;

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    RobotMap.init();
    
    	Robot.drive = new Drive();      

        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        pincher = new Pincher();
        funnel = new Funnel();
        winch = new Winch();
        air = new Air();
        table = NetworkTable.getTable("SmartDashboard");
        vision = new Vision();
        

        // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        // OI must be constructed after subsystems. If the OI creates Commands
        //(which it very likely will), subsystems are not guaranteed to be
        // constructed yet. Thus, their requires() statements may grab null
        // pointers. Bad news. Don't move it.
        oi = new OI();

        // instantiate the command used for the autonomous period
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=AUTONOMOUS
       
        autoChooser = new SendableChooser<Command>();
        autoChooser.addDefault("Frozen", new AutonomousCommand());
        autoChooser.addObject("LeftPeg", new LeftPeg());
        autoChooser.addObject("MiddlePeg", new MiddlePeg());
        autoChooser.addObject("RightPeg", new RightPeg());
        autoChooser.addObject("Baseline", new Baseline());
        autoChooser.addObject("MiddleBaseline", new MiddleBaseline());
        autoChooser.addObject("AutoRotateTest", new AutoRotateTest());


        SmartDashboard.putData("Autonomous Chooser", autoChooser);
        
        RobotMap.imu.zeroYaw();
        // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=AUTONOMOUS
    }

    /**
     * This function is called when the disabled button is hit.
     * You can use it to reset subsystems before shutting down.
     */
    
    
    
    public void disabledInit(){

    }

    public void disabledPeriodic() {
        Scheduler.getInstance().run();
    }

    public void autonomousInit() {
        // schedule the autonomous command (example)
    	 autonomousCommand = (Command) autoChooser.getSelected();
         if(autonomousCommand != null){
         	autonomousCommand.start();
         }
     	 Robot.table.putBoolean("Timer", true);

        
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
    }

    public void teleopInit() {
        // This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
        //Robot.pincher.defaultPneumatics();
        Robot.drive.setFODEnabled(true);
        RobotMap.strafeEncoder.reset();
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
        
        boolean sees = Robot.vision.seePeg();
        SmartDashboard.putBoolean("Sees?", sees);
        
    	Robot.table.putBoolean("Timer", Robot.vision.sendTime());

        double x_val = Robot.vision.getCurrentX();
        SmartDashboard.putNumber("x_val", x_val);
        
        double y_val = Robot.vision.getCurrentY();
        SmartDashboard.putNumber("y_val", y_val);
        
        double z_val = Robot.vision.getCurrentZ();
        SmartDashboard.putNumber("z_val", z_val);
        
        
        SmartDashboard.putNumber("GearDetector", RobotMap.gearDetector.getDistanceInches());
        SmartDashboard.putBoolean("GearPresent", Robot.pincher.detectGear());
        
        SmartDashboard.putNumber("PressureSensor", Robot.air.getPressureSensor() );
                
        SmartDashboard.putBoolean("FOD Enabled", Robot.drive.getFODEnabled());
        
        SmartDashboard.putNumber("Yaw", RobotMap.imu.getYaw());
        
        SmartDashboard.putNumber("GameTime", Timer.getMatchTime());
    }
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
    }

	@Override
	public void pidWrite(double output) {
		// TODO Auto-generated method stub
		
	} 
}