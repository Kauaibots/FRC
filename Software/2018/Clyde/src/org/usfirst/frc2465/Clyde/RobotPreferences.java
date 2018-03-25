package org.usfirst.frc2465.Clyde;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class RobotPreferences {

	// PID Controller Settings
	// the good kine :D P:0.0022 I:0.000010 D:0.00001
	
	static DriverStation ds;

	static double ElevatorP;
	static double ElevatorI;
	static double ElevatorD;

	static double rotateP;
	static double rotateI;
	static double rotateD;

	public static boolean rotateFinished;
	public static boolean driveFinished;

	public static int startingPosition;
	
	static public double getAutoRotateP() {
		// return Preferences.getInstance().getDouble("AutoRotateP", 0.0002);

		return 0.028;
	}

	static public double getAutoRotateI() {
		// return Preferences.getInstance().getDouble("AutoRotateI", 0.00005);

		return 0.00000249;
	}

	static public double getAutoRotateD() {
		// return Preferences.getInstance().getDouble("AutoRotateD", 0.00);

		return 0.115;
	}

	static public double getAutoRotateOnTargetToleranceDegrees() {
		return 3.0; /*
					 * Preferences.getInstance().getDouble("AutoRotateOnTargetToleranceDegrees",
					 * 2.0);
					 */
	}

	static public double getAutoRotateDefaultTaretDegrees() {
		return Preferences.getInstance().getDouble("AutoRotateDefaultTargetDegrees", 0.0);
	}

	// Elevator PID Controller Settings
	/*static public double getElevatorP() {

		ElevatorP = SmartDashboard.getNumber("ElevatorP", 0.0015);

		return ElevatorP;
	}

	static public double getElevatorI() {

		ElevatorI = SmartDashboard.getNumber("ElevatorI", 0.0);

		return ElevatorI;
	}

	static public double getElevatorD() {

		ElevatorD = SmartDashboard.getNumber("ElevatorD", 0.0);

		return ElevatorD;
	}*/

	static public double getElevatorOnTargetToleranceInches() {

		return 0.5;
	}

	static public double getElevatorDefaultTargetInches() {

		return Preferences.getInstance().getDouble("ElevatorDefaultTargetInches", 0.0);
	}

	static public void setTopEncoderPos(int encoderCount) {

		Preferences.getInstance().putDouble("encoderTopPos", encoderCount);
	}

	static public double getTopEncoderPos() {

		return Preferences.getInstance().getDouble("encoderTopPos", 0.0);
	}

	static public double getElevatorHeight() {

		// Low sensor to bottom of carriage at high sensor
		return 74.25;
	}

	static public double getFloorOffset() {

		// Floor to bottom of carriage at low sensor
		return 8.125;
	}

	static public char getSwitch() {
		String gameData;
		gameData = ds.getInstance().getGameSpecificMessage();
		if (gameData.length() > 0) {
			return gameData.charAt(0);
		} else {
			return 'N';
		}
	}
	
	static public char getScale() {
		String gameData;
		gameData = ds.getInstance().getGameSpecificMessage();
		if (gameData.length() > 0) {
			return gameData.charAt(1);
		} else {
			return 'N';
		}
	}
	
	static public char getEnemy() {
		String gameData;
		gameData = ds.getInstance().getGameSpecificMessage();
		if (gameData.length() > 0) {
			return gameData.charAt(2);
		} else {
			return 'N';
		}
	}
}
