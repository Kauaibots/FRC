/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.usfirst.frc2465.StrongholdBot16.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 *
 * @author Tyres
 */

public class AngleSensor extends SensorBase implements PIDSource, LiveWindowSendable {

    DigitalInput    data_pin;
    DigitalOutput   chipselect_pin;
    DigitalOutput   clock_pin;
    ITable table;
    
    public AngleSensor(DigitalInput data_pin, DigitalOutput chipselect_pin, DigitalOutput clock_pin) {
        this.data_pin = data_pin;
        this.chipselect_pin = chipselect_pin;
        this.clock_pin = clock_pin;
        initSensor();
    }
    
    /**
     * Initialize the AngleSensor.
     */
    protected void initSensor()
    {
    	chipselect_pin.set(true);
    	clock_pin.set(true);
    }

    /**
     * Return the actual angle in degrees.
     * 
     * The angle is based on the current sensor reading, which is a 12-bit value, giving a range
     * of 4096 digital values (0-4095) across a full-scale range of 360 degrees.
     * 
     * The value output from this function is normalized to a range of -180 to 180 degrees.
     * 
     * The sensor has a chip select and clock pin.  Chip select must be low in order to read data.
     * On the first rising clock edge, the first data bit is valid.
     * 
     * Per the datasheet, 500ns is the latency between when chipselect goes low and data is first valid,
     * and between each time the clock goes high and data is read to ready.
     * 
     * @return the current heading of the robot in degrees. This heading is based on integration
     * of the returned rate from the AngleSensor.
     */
    double getAngle()
    {
    	chipselect_pin.set(true);
    	int angle=0;
    	
    	int bit;
    	double real_Angle;
    	
    	clock_pin.set(true);
    	chipselect_pin.set(false);
    	delayNS();
    	
    	for (int i=0;i<12;i++)
    	{
    		clock_pin.set(false);
    		clock_pin.set(true);
    		/* 500ns delay needed here */
    		delayNS();
    		bit = data_pin.get() ? 1:0;
    		angle = angle<<1;
    		angle |= bit;
    	}
    	
    	chipselect_pin.set(true);
    	clock_pin.set(true);
    	
    	real_Angle = ((double)angle*360)/4096;
    	real_Angle = real_Angle-180;

    	return real_Angle;
    }


    private void delayNS() 
    {
		long fpga_time = Utility.getFPGATime();
		while ( Utility.getFPGATime() == fpga_time ) {
			/* Keep reading time until the next microsecond is reached. */
		}    	
    }
    
    public double pidGet() {
        return getAngle();
    }

    public void updateTable() {
        if (table != null) {
            table.putNumber("Value", getAngle());
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
        return "Gyro";
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

