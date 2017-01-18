/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.usfirst.frc2465.StrongholdBot16.subsystems;

import org.usfirst.frc2465.StrongholdBot16.RobotMap;

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

public class ProximitySensor extends SensorBase implements PIDSource, LiveWindowSendable {

    public static final int kShortRange    = 0; 
    public static final int kMediumRange   = 1; 
    public static final int kLongRange     = 2; 

    int sensor_range;
    AnalogInput   analog_channel;
    ITable table;
    
    public ProximitySensor(AnalogInput analog_input, int sensor_range) {
        this.sensor_range = sensor_range;
        analog_channel = analog_input;
        initProximitySensor();
    }
    
    /**
     * Initialize the ProximitySensor.
     */
    protected void initProximitySensor()
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

    public double getDistanceMM()
    {
        double voltage = analog_channel.getAverageVoltage();
        
        // Transform voltage to distance.  Exponential values
        // were derived from the datasheets:
        //
        // GP2D120XJ00F (Short Range [30cm]):  https://www.sparkfun.com/datasheets/Sensors/Infrared/GP2D120XJ00F_SS.pdf
        // GP2Y0A21YK (Medium Range [80cm]):  http://www.sparkfun.com/datasheets/Components/GP2Y0A21YK.pdf
        // GP2Y0A02YK0F (Long Range [150cm]):  https://www.sparkfun.com/datasheets/Sensors/Infrared/gp2y0a02yk_e.pdf
        
        double distance_mm = 0.0;
        switch ( sensor_range )
        {
        case kShortRange:
        
                distance_mm = 126.27 * Math.pow(voltage, -1.102);
                break;
                
        case kMediumRange:
                distance_mm = 267.1 * Math.pow(voltage, -1.23);
                break;
                
        case kLongRange:
                distance_mm = 61.681 * Math.pow(voltage, -1.133);
                break;
        }
        
        return distance_mm;
    }
    
    final static double mm_to_inches = 1.0/25.4;
    
    public double getDistanceInches() {
        return getDistanceMM() * mm_to_inches;
    }
    
    public double pidGet() {
        return getDistanceMM();
    }

    public void updateTable() {
        if (table != null) {
            table.putNumber("Value", getDistanceInches());
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

