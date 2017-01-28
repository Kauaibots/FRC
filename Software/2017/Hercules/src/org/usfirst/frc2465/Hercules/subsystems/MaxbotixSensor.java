/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.usfirst.frc2465.Hercules.subsystems;

import org.usfirst.frc2465.Hercules.RobotMap;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 *
 * @author Scott
 */

public class MaxbotixSensor extends SensorBase implements PIDSource, LiveWindowSendable {

    AnalogInput   analog_channel;
    ITable table;
    
    public MaxbotixSensor(AnalogInput analog_input) {
        analog_channel = analog_input;
        initSensor();
    }
    
    /**
     * Initialize the ProximitySensor.
     */
    protected void initSensor()
    {
        // Be careful not to set the average bits more than once.
        int num_average_bits = 4;
        if ( analog_channel.getAverageBits() != num_average_bits )
        {
            analog_channel.setAverageBits(num_average_bits); // 2^4 = 16-sample average
            float sampleRate = (float) (50.0 * 
                    (1 << (num_average_bits)));
            AnalogInput.setGlobalSampleRate(sampleRate);
            Timer.delay(0.2);
        }
    }

    /**
     * Return the actual distance in millimeters.
     * 
     * @return the current distance in millimeters.
     */

    public double getDistanceCM()
    {
        double voltage = analog_channel.getAverageVoltage();
        
        // Transform voltage to distance.  Conversion from 
        // voltage to CM is:  distance_cm = (Vcc/1024)
        //
        double distance_cm = 0.0;
        distance_cm = voltage/(5.0/1024);
        return distance_cm;
    }
    
    final static double cm_to_inches = 1/2.54;
    
    public double getDistanceInches() {
        return getDistanceCM() * cm_to_inches;
    }
    
    public double pidGet() {
        return getDistanceCM();
    }

    public void updateTable() {
        if (table != null) {
            table.putNumber("Value", getDistanceCM());
        }
    }

    public void startLiveWindowMode() {
    }

    public void stopLiveWindowMode() {
    }

    public void initTable(ITable itable) {
        table = itable;
        updateTable();
    }

    public ITable getTable() {
        return table;
    }

    public String getSmartDashboardType() {
        return "Ultrasonic";
    }

	@Override
	public PIDSourceType getPIDSourceType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPIDSourceType(PIDSourceType arg0) {
		// TODO Auto-generated method stub
		
	}
    
}

