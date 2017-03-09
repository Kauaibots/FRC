/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.usfirst.frc2465.Hercules;

import edu.wpi.first.wpilibj.Preferences;

/**
 *
 * @author Scott and Tyres
 */
public class RobotPreferences {
    
    // PID Controller Settings
	// the good kine :D P:0.0022 I:0.000010 D:0.00001
    
    static public double getAutoRotateP() {
        //return Preferences.getInstance().getDouble("AutoRotateP", 0.0002);
    	return 0.013;
    }
    static public double getAutoRotateI() {
        //return Preferences.getInstance().getDouble("AutoRotateI", 0.00005);
    	return 0.000002;
    }
    static public double getAutoRotateD() {
        //return Preferences.getInstance().getDouble("AutoRotateD", 0.00);
    	return 0.0002;		//battery died 6:30 2/28/17 0.0000
    }
    static public double getAutoRotateOnTargetToleranceDegrees() {
        return 2.0; /*Preferences.getInstance().getDouble("AutoRotateOnTargetToleranceDegrees", 2.0); */
    }
    static public double getAutoRotateDefaultTaretDegrees() {
        return Preferences.getInstance().getDouble("AutoRotateDefaultTargetDegrees",0.0);
    }
}