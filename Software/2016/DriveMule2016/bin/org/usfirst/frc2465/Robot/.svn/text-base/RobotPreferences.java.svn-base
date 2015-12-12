/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.usfirst.frc2465.Robot;

import edu.wpi.first.wpilibj.Preferences;

/**
 *
 * @author Scott
 */
public class RobotPreferences {
    
    // PID Controller Settings
    
    static public double getAutoRotateP() {
        return Preferences.getInstance().getDouble("AutoRotateP", 0.0070);
    }
    static public double getAutoRotateI() {
        return Preferences.getInstance().getDouble("AutoRotateI", 0.00001);
    }
    static public double getAutoRotateD() {
        return Preferences.getInstance().getDouble("AutoRotateD", 0.0);
    }
    static public double getAutoRotateOnTargetToleranceDegrees() {
        return Preferences.getInstance().getDouble("AutoRotateOnTargetToleranceDegrees", 2.0);
    }
    static public double getAutoRotateDefaultTaretDegrees() {
        return Preferences.getInstance().getDouble("AutoRotateDefaultTargetDegrees",0.0);
    }
}
