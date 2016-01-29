package org.usfirst.frc2465.StrongholdBot16.subsystems;

import java.util.TimerTask;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

public class LidarLITE implements PIDSource{
	private I2C i2c;
	private byte[] distance;
	private java.util.Timer updater;
	
	private final int LIDAR_ADDR = 0x62;
	private final int LIDAR_CONFIG_REGISTER = 0x00;
	private final int LIDAR_DISTANCE_REGISTER = 0x8f;
	
	public LidarLITE(Port port) {
		i2c = new I2C(port, LIDAR_ADDR);
		
		distance = new byte[2];
		
		updater = new java.util.Timer();
	}
	
	// Distance in cm
	public int getDistance() {
		return (int)Integer.toUnsignedLong(distance[0] << 8) + Byte.toUnsignedInt(distance[1]);
	}

	public double pidGet() {
		return getDistance();
	}
	
	// Start 10Hz polling
	public void start() {
		updater.scheduleAtFixedRate(new LIDARUpdater(), 0, 100);
	}
	
	// Start polling for period in milliseconds
	public void start(int period) {
		updater.scheduleAtFixedRate(new LIDARUpdater(), 0, period);
	}
	
	public void stop() {
		updater.cancel();
		updater = new java.util.Timer();
	}
	
	// Update distance variable
	public void update() {
		i2c.write(LIDAR_CONFIG_REGISTER, 0x04); // Initiate measurement
		Timer.delay(0.04); // Delay for measurement to be taken
		i2c.read(LIDAR_DISTANCE_REGISTER, 2, distance); // Read in measurement
		Timer.delay(0.005); // Delay to prevent over polling
	}
	
	// Timer task to keep distance updated
	private class LIDARUpdater extends TimerTask {
		public void run() {
			update();
			/*
			while(true) {
				update();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			*/
		}
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