/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.usfirst.frc2465.StrongholdBot16;

import edu.wpi.first.wpilibj.Preferences;

/**
 *
 * @author Scott and Tyres
 */
public class RobotPreferences {
    
    // PID Controller Settings
	// the good kine :D P:0.0022 I:0.000010 D:0.00001
	// the other good kine P:0.00038 I:0.00001 D:0.001
	// 3-28-16 the even better good kine P: 0.008 I: 0.000004 D: 0.006
    
    static public double getAutoRotateP() {
        //return Preferences.getInstance().getDouble("AutoRotateP", 0.0002);
    	return 0.008;
    }
    static public double getAutoRotateI() {
        //return Preferences.getInstance().getDouble("AutoRotateI", 0.00005);
    	return 0.000004;
    }
    static public double getAutoRotateD() {
        //return Preferences.getInstance().getDouble("AutoRotateD", 0.00);
    	return 0.006;
    }
    static public double getAutoRotateOnTargetToleranceDegrees() {
        return Preferences.getInstance().getDouble("AutoRotateOnTargetToleranceDegrees", 2.0);
    }
    static public double getAutoRotateDefaultTaretDegrees() {
        return Preferences.getInstance().getDouble("AutoRotateDefaultTargetDegrees",0.0);
    }
}
