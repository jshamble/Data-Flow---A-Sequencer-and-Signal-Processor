import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TrafficLight extends SignalProcessor {
	
	private int scale = 30;
	
	private int Circle1X = 0;
	private int Circle1Y = 0;
	private int Circle2Y = 0;
	private int Circle3Y = 0;
	
	public int getCircle1X()
	{
		return Circle1X;
	}
	
	public int getCircle1Y()
	{
		return Circle1Y;
	}
	
	public int getCircle2Y()
	{
		return Circle2Y;
	}
	
	public int getCircle3Y()
	{
		return Circle3Y;
	}
	
	public int getScale()
	{
		return scale;
	}
	
	
	
	public TrafficLight(JFrame frame, int upperLeftX, int upperLeftY, HandlerClass h)
	{
		id++;
		identifier = id;
		
		hand = h;
		
		uLeftX = upperLeftX;
		uLeftY = upperLeftY;
		
		//make height three times as long as the width

		bRightX = uLeftX - (1*scale);
		bRightY = uLeftY - (3*scale);
		
		//set the positions of the circles...
		
		Circle1X = uLeftX - scale;
		Circle1Y = uLeftY - scale;
		Circle2Y = Circle1Y - scale;
		Circle3Y = Circle2Y - scale;
		
		updateOB();
		
		//draw traffic light...
		h.getCanvas().repaint();
	}
	
	@Override
	public boolean isATrafficLight()
	{
		return true;
	}
	
	@Override
	public void drawUpdate(int translateX, int translateY)
	{
		
		uLeftX = uLeftX + translateX;
		uLeftY = uLeftY + translateY;
		
		//make height three times as long as the width

		bRightX = uLeftX - (1*scale);
		bRightY = uLeftY - (3*scale);
		
		//set the positions of the circles...
		
		Circle1X = uLeftX - scale;
		Circle1Y = uLeftY - scale;
		
		Circle2Y = Circle1Y - scale;
		
		Circle3Y = Circle2Y - scale;

		updateOB();
		
		//hand.getCanvas().repaint();
	}
	
	public String getName()
	{
		return "tlight";
	}
}

