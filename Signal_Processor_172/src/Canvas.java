import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Canvas extends JPanel{
			//to add gradient effect, make sure paintComponents are always invoked in the paintcomponent method.
			//or, at least the get height method is called, don't jsut keep creating nerw objects every time...
		    //Paint p;

		//30 == traffilight scale, hardcoded for now
	
	private double attackTime = 0.0;
	
	public void setAttackTime( double newAttackTime)
	{
		attackTime = newAttackTime;
	}
	
		private int tLeftX = 0;
		private int tLeftY = 0;

		private int bRightX = 0;
		private int bRightY = 0;
	
	  	 Point2D start = new Point2D.Float(0, 0);
	     Point2D end = new Point2D.Float(50, 50);

	  	 Point2D start2 = new Point2D.Float(0, 0);
	     Point2D end2 = new Point2D.Float(50, 50);

	  	 Point2D start3 = new Point2D.Float(0, 0);
	     Point2D end3 = new Point2D.Float(50, 50);
	     
	     float[] dist = {0.0f, 0.2f, 1.0f};
	     
	     Color[] rcolors = {Color.RED, Color.WHITE, Color.RED};
	     Color[] ycolors = {Color.YELLOW, Color.WHITE, Color.YELLOW};
	     Color[] gcolors = {Color.GREEN, Color.WHITE, Color.GREEN};
	     Color[] bcolors = {Color.BLACK, Color.WHITE, Color.BLACK};
			
			
		    final static BasicStroke stroke = new BasicStroke(2.0f);
	
			private HandlerClass h;
			private ArrayList<SignalProcessor> allSP = new ArrayList<SignalProcessor>();
			private ArrayList<Wire> allWires = new ArrayList<Wire>();
			private ObjectList o;
			private JFrame frame;
			
			private JScrollPane jsp;
			
			public void setScrollBar(JScrollPane js)
			{
				jsp = js;
			}
			
			public void setObjectList(ArrayList<SignalProcessor> asp, ArrayList<Wire> allw)
			{
				allSP = asp;
				allWires = allw;
			}

			public Canvas(HandlerClass hand, JFrame fram)
			{
				//this.setPreferredSize(new Dimension(getWidth(),getHeight()));
				//this.setVisible(true);
				frame = fram;
				h = hand;
			}
			
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				//this.setPreferredSize(new Dimension(100,100));

				//this.setSize(new Dimension(getWidth(),getHeight()));
				this.setBackground(Color.WHITE);
				
				Graphics2D g2 = (Graphics2D) g.create();
				
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        
		        //if(redtowhite == null)
		        //{
		        	//p = new GradientPaint(0, 0, new Color(0x7D838F),getWidth(), 0, new Color(0x272B39));

					//redtowhite = new GradientPaint(0, 0, new Color(0xE50000),getWidth(), 0, new Color(0xFFFFFF));
					//yellowtowhite = new GradientPaint(0, 0, new Color(0xFBFF0A),getWidth(), 0, new Color(0xFFFFFF));
					//greentowhite = new GradientPaint(0, 0, new Color(0x00E620),getWidth(), 0, new Color(0xFFFFFF));
		        //}
				
		        
				g.setColor(Color.BLACK);
				//System.out.println(getOnClickX() + " :: " + getOnClickY() + " :: " + getOnClickX2() + " :: " + getOnClickY2());
				//g.drawRect(getOnClickX(), getOnClickY(), getOnClickX2(), getOnClickY2());
				//four lines make a rectangle
				//top line
				
				//use this for AABB (Axis - Aligned Boudning Box) bounding box detection...
				
				//this is the rectangle.. don't draw if one of them is zero. since getOnclickX is set to zero first...
				//only perfor one check for optimization purposes.
				//alsok, don't draw the rectangle when dragging and connecting wires (stasis mode)
				if(!h.getStasis())
					{
					//other computations are calucated here as an optimization...
					if(h.getOnClickX() != 0 && h.getOnClickY2() != 0)
					{
						g.drawLine(h.getOnClickX(), h.getOnClickY(), h.getOnClickX2(), h.getOnClickY());
						//bottom line
						g.drawLine(h.getOnClickX(), h.getOnClickY2(), h.getOnClickX2(), h.getOnClickY2());
						//left line
						g.drawLine(h.getOnClickX(), h.getOnClickY(), h.getOnClickX(), h.getOnClickY2());
						//right line
						g.drawLine(h.getOnClickX2(), h.getOnClickY(), h.getOnClickX2(), h.getOnClickY2());
					}
				}
				else
				{
					//draw the wire.

					g.setColor(Color.BLACK);
					System.out.println(h.getWire().getEndPointY());
					g.drawLine(h.getWire().getStartPointX(),h.getWire().getStartPointY(),h.getWire().getEndPointX()+5,h.getWire().getEndPointY()+50);
				}
				
				
				//setGraphicsContext(g);
				
				//the bottom left could be either of the x's, and so could the bottom right, top left, and top right,
				//making sure to switch the two when nessecary (as the rectangle selection is always changing.)
				//using the AABB bounding box algorithm found here: http://www.youtube.com/watch?v=ghqD3e37R7E
				
				if(h.getOnClickX() < h.getOnClickX2())
				{
					tLeftX = h.getOnClickX();
					bRightX = h.getOnClickX2();
				}
				else
				{
					tLeftX = h.getOnClickX2();
					bRightX = h.getOnClickX();
				}
				
				if(h.getOnClickY() < h.getOnClickY2())
				{
					tLeftY = h.getOnClickY();
					bRightY = h.getOnClickY2();
				}
				else
				{
					tLeftY = h.getOnClickY2();
					bRightY = h.getOnClickY();
				}
				
				if(h.getCreationMode())
				{
					//draw a line at=
					g.setColor(Color.BLUE);
					g.drawOval(h.getCurrentMouseLocationX()-7, h.getCurrentMouseLocationY()-7, 5, 5);
					g.setColor(Color.BLACK);
					g.drawString(h.getObjName(), h.getCurrentMouseLocationX(), h.getCurrentMouseLocationY());
				}
				
				//here, we go through each SignalProcessor object, and call their respective drawUpdate methods respectively
				
				for(int i = 0; i < allSP.size(); i++)
				{
					if(allSP.get(i).isATrafficLight())
					{
						updateTrafficLight(g,g2,(TrafficLight)allSP.get(i));
					}
					else if(allSP.get(i).isASproc())
					{
						updateSproc(g,g2,(Sproc)allSP.get(i));
						//System.out.println(allSP.get(i).getULeftX());
						//System.out.println(allSP.get(i).getULeftY());
						//((Sproc)allSP.get(i)).getInstrumentList().setLocation(allSP.get(i).getULeftX(), allSP.get(i).getULeftY()-10);
					}
				}
				
				//here, we go through Each WIRE object and update it's location and connections drawn on the screen...
				//we do this by drawing the object....
				for(int j = 0; j < allWires.size(); j++)
				{
					int inIDIndex = -1;
					int outIDIndex = -1;
					//now, get the proper ID associated with the in and outs...
					for(int k = 0; k < allSP.size(); k++)
					{	
						if(allWires.get(j).getinId() == allSP.get(k).getID())
						{
							inIDIndex = k;
						}
						if(allWires.get(j).getoutId() == allSP.get(k).getID())
						{
							outIDIndex = k;
						}
					}
					
					if(outIDIndex != -1)
					{
						if(allSP.get(outIDIndex).isASproc())
						{
							allWires.get(j).setStartpoints(allSP.get(outIDIndex).getBRightX() + 30, allSP.get(outIDIndex).getOBCentroidY());
						}
						else
						{
							allWires.get(j).setStartpoints(allSP.get(outIDIndex).getBRightX() + 60, allSP.get(outIDIndex).getOBCentroidY());
						}
						allWires.get(j).setEndpoints(allSP.get(inIDIndex).getULeftX() - 30, allSP.get(inIDIndex).getIBCentroidY());
						g.drawLine(allWires.get(j).getStartPointX(), allWires.get(j).getStartPointY(), allWires.get(j).getEndPointX(), allWires.get(j).getEndPointY());
					}
				}
				
				//draw effects here....
				if(h.getConvMode())
				{
					g.drawString("CONV REV ON", 0 + h.getHorizontalScrollBarOffset(), 120 + h.getVerticalScrollBarOffset());
				}
				else
				{
					g.drawString("CONV REV OFF", 0 + h.getHorizontalScrollBarOffset(), 120 + h.getVerticalScrollBarOffset());
				}
				
				if(h.getChorMode())
				{
					g.drawString("CHORUS ON", 50 + h.getHorizontalScrollBarOffset(), 150 + h.getVerticalScrollBarOffset());
				}
				else
				{
					g.drawString("CHORUS OFF", 50 + h.getHorizontalScrollBarOffset(), 150 + h.getVerticalScrollBarOffset());
				
				}
				
				if(h.getFormantMode())
				{
					g.drawString("FORMANT ON", 100 + h.getHorizontalScrollBarOffset(), 120 + h.getVerticalScrollBarOffset());
				}
				else
				{
					g.drawString("FORMANT OFF", 100 + h.getHorizontalScrollBarOffset(), 120 + h.getVerticalScrollBarOffset());
				}
				
				if(h.getRingModMode())
				{
					g.drawString("ENVELOPE ON", 150+ h.getHorizontalScrollBarOffset(), 150+ h.getVerticalScrollBarOffset());
					g.drawString("Attack time: " + attackTime, 150+ h.getHorizontalScrollBarOffset(), 180+ h.getVerticalScrollBarOffset());
				}
				else
				{
					g.drawString("ENVELOPE OFF", 150+ h.getHorizontalScrollBarOffset(), 150+ h.getVerticalScrollBarOffset());
				}


				this.revalidate();			//	Needed to recalculate the scroll bars and display menu text...
				
				//needed to update the scrollbar dimensions...
				jsp.setBounds(0, 20, frame.getSize().width - 15,  frame.getSize().height - 57);
				
				//replace with max x and y...
				this.setPreferredSize(new Dimension(20000,20000));
				this.setSize(new Dimension(20000,20000));
				this.invalidate();
				this.validate();
				//g.drawLine(0,0, 20000, 20000);

				//done so that the translations will be done ONLY when the mouse is moved.
				h.setMouseMoved( false );
				
				jsp.repaint();
		}			
			//abstractions (methods) for cleanliness of SignalProcessorObjects
			public void updateTrafficLight(Graphics g, Graphics2D g2, TrafficLight tlight)
			{
				//during the selction mode...
				if(h.getSelectionMode())
				{
					//Axis Aligned Bounding Box collision Detection
					if(!(tlight.getBRightX() < tLeftX || bRightX < tlight.getULeftX() || tlight.getBRightY() < tLeftY || bRightY < tlight.getULeftY()))
					{
						tlight.setSelected(true);
					}
					else
					{
						tlight.setSelected(false);
					}
				}

				//go though each selected object and translate them based on the difference between the mouseClicked's location
				//and the mouseDragged's location.

				g.setColor(Color.BLACK);
				
				if(tlight.getSelected() && h.getSelectionMode() == false)
				{
					g.setColor(Color.BLUE);
					
					//already know it's a traffic light...
					

					//done so that the translations will be done ONLY when the mouse is moved.
					if(h.getMouseMoved())
					{
						(tlight).drawUpdate(h.getDifferenceVectorX(),h.getDifferenceVectorY());
					}
					
				}
				
				//top line
				g.drawLine((tlight).getULeftX(), (tlight).getULeftY(), (tlight).getBRightX(), (tlight).getULeftY());
				//bottom line
				g.drawLine((tlight).getULeftX(), (tlight).getBRightY(), (tlight).getBRightX(), (tlight).getBRightY());
				//left line
				g.drawLine((tlight).getULeftX(), (tlight).getULeftY(), (tlight).getULeftX(), (tlight).getBRightY());
				//right line
				g.drawLine((tlight).getBRightX(),  (tlight).getULeftY(), (tlight).getBRightX(), (tlight).getBRightY());
				
				//then, draw three circles
				
				//position? offset 10 * scale from top to bottom
				
				/*
				g.setColor(Color.GREEN);
				g.fillOval((tlight).getCircle1X(), (tlight).getCircle1Y(), (tlight).getScale(), (tlight).getScale());

				g.setColor(Color.YELLOW);
				g.fillOval((tlight).getCircle1X(), (tlight).getCircle2Y(), (tlight).getScale(), (tlight).getScale());

				g.setColor(Color.RED);
				g.fillOval((tlight).getCircle1X(), (tlight).getCircle3Y(), (tlight).getScale(), (tlight).getScale());
				*/

				//Paint redtowhite = new GradientPaint((tlight).getCircle1X(), (tlight).getCircle1Y(), new Color(0xE50000),(tlight).getCircle1X() - (tlight).getScale(), (tlight).getCircle1Y() - (tlight).getScale(), new Color(0xFFFFFF));
				//Paint yellowtowhite = new GradientPaint((tlight).getCircle1X(), (tlight).getCircle2Y(), new Color(0xFBFF0A),(tlight).getCircle1X() - (tlight).getScale(),(tlight).getCircle2Y() -  (tlight).getScale(), new Color(0xFFFFFF));
				//Paint greentowhite = new GradientPaint((tlight).getCircle1X(), (tlight).getCircle3Y(), new Color(0x00E620),(tlight).getCircle1X() - (tlight).getScale(),(tlight).getCircle3Y() -  (tlight).getScale(), new Color(0xFFFFFF));


				 start.setLocation((tlight).getCircle1X(), (tlight).getCircle1Y());

				 end.setLocation((tlight).getCircle1X()+30, (tlight).getCircle1Y());
				 
			     LinearGradientPaint redtowhite = new LinearGradientPaint(start, end, dist, rcolors);

				 start2.setLocation((tlight).getCircle1X(), (tlight).getCircle2Y());
				 

				 end2.setLocation((tlight).getCircle1X()+30, (tlight).getCircle2Y());
			     
			     LinearGradientPaint yellowtowhite = new LinearGradientPaint(start2, end2, dist, ycolors);
			     

				 start3.setLocation((tlight).getCircle1X(), (tlight).getCircle3Y());
				 end3.setLocation((tlight).getCircle1X()+30, (tlight).getCircle3Y());
				 
			     LinearGradientPaint greentowhite = new LinearGradientPaint(start3, end3, dist, gcolors);
				
				g2.setPaint(greentowhite);
				g2.fill(new Ellipse2D.Double((tlight).getCircle1X(), (tlight).getCircle1Y(), (tlight).getScale(), (tlight).getScale()));
				g2.setPaint(yellowtowhite);
				g2.fill(new Ellipse2D.Double((tlight).getCircle1X(), (tlight).getCircle2Y(), (tlight).getScale(), (tlight).getScale()));
				g2.setPaint(redtowhite);
				g2.fill(new Ellipse2D.Double((tlight).getCircle1X(), (tlight).getCircle3Y(), (tlight).getScale(), (tlight).getScale()));
				
				//now, draw the outletbox using four lines...
				
				g.setColor(Color.BLACK);

				//top line
				g.drawLine((tlight).getouLeftX(), (tlight).getouLeftY(), (tlight).getobRightX(), (tlight).getouLeftY());
				//bottom line
				g.drawLine((tlight).getouLeftX(), (tlight).getobRightY(), (tlight).getobRightX(), (tlight).getobRightY());
				//left line
				g.drawLine((tlight).getouLeftX(), (tlight).getouLeftY(), (tlight).getouLeftX(), (tlight).getobRightY());
				//right line
				g.drawLine((tlight).getobRightX(),  (tlight).getouLeftY(), (tlight).getobRightX(), (tlight).getobRightY());
				//then, print a number in the middle of it at the centroid.....
				
				g.drawString("" + (tlight).getOBWireOutCount(), (tlight).getOBCentroidX() - 7, (tlight).getOBCentroidY() + 5);
				
				
				//draw play on the traffic light...

				g.drawLine((tlight).getobRightX() - 50, (tlight).getobRightY()+10, (tlight).getobRightX() - 50, (tlight).getobRightY()+20);
				//line diagonal 1
				g.drawLine((tlight).getobRightX() - 50, (tlight).getobRightY()+10, (tlight).getobRightX() - 40, (tlight).getobRightY()+15);
				//line diagonal 2
				g.drawLine((tlight).getobRightX() - 50, (tlight).getobRightY()+20, (tlight).getobRightX() - 40, (tlight).getobRightY()+15);	

				//draw the stop on the traffic light...
				
				//top line
				g.drawLine((tlight).getouLeftX() - 20, (tlight).getouLeftY()-20, (tlight).getobRightX() - 40, (tlight).getouLeftY()-20);
				//bottom line
				g.drawLine((tlight).getouLeftX() - 20, (tlight).getouLeftY()-10, (tlight).getobRightX() - 40, (tlight).getouLeftY()-10);
				//left line
				g.drawLine((tlight).getouLeftX() - 20, (tlight).getouLeftY()-20, (tlight).getouLeftX() - 20, (tlight).getobRightY()-40);
				//right line
				g.drawLine((tlight).getouLeftX() - 10, (tlight).getouLeftY()-20, (tlight).getouLeftX() - 10, (tlight).getobRightY()-40);
			
			}
			
			public void updateSproc(Graphics g, Graphics2D g2, Sproc sproc)
			{
				
				//listen for midi events for sporcs...
				
				//make sure to mute if mute is on...
				
				//during the selction mode...
				if(h.getSelectionMode())
				{
					//Axis Aligned Bounding Box collision Detection
					if(!(sproc.getBRightX() < tLeftX || bRightX < sproc.getULeftX() || sproc.getBRightY() < tLeftY || bRightY < sproc.getULeftY()))
					{
						sproc.setSelected(true);
					}
					else
					{
						sproc.setSelected(false);
					}
				}

				//go though each selected object and translate them based on the difference between the mouseClicked's location
				//and the mouseDragged's location.

				g.setColor(Color.BLACK);
				
				if(sproc.getSelected() && h.getSelectionMode() == false)
				{
					g.setColor(Color.BLUE);
					
					//already know it's a traffic light...
					System.out.println(h.getDifferenceVectorX());
					System.out.println(h.getDifferenceVectorY());
					
					//only when the mouse is moved. set to false immendiately afterward =)

					//done so that the tranlations will be done ONLY when the mouse is moved.
					if(h.getMouseMoved())
					{
						(sproc).drawUpdate(h.getDifferenceVectorX(),h.getDifferenceVectorY());
						//set position of banks and instruments here...
						(sproc).getInstrumentList().setLocation((sproc).getInstrumentList().getLocation().x + h.getDifferenceVectorX(),(sproc).getInstrumentList().getLocation().y + h.getDifferenceVectorY());
					}
				}
				
				//top line
				g.drawLine((sproc).getULeftX(), (sproc).getULeftY(), (sproc).getBRightX(), (sproc).getULeftY());
				//bottom line
				g.drawLine((sproc).getULeftX(), (sproc).getBRightY(), (sproc).getBRightX(), (sproc).getBRightY());
				//left line
				g.drawLine((sproc).getULeftX(), (sproc).getULeftY(), (sproc).getULeftX(), (sproc).getBRightY());
				//right line
				g.drawLine((sproc).getBRightX(),  (sproc).getULeftY(), (sproc).getBRightX(), (sproc).getBRightY());
				
				//Draw the Inlet BOX
				
				//top line
				g.drawLine((sproc).getiuLeftX(), (sproc).getiuLeftY(), (sproc).getibRightX(), (sproc).getiuLeftY());
				//bottom line
				g.drawLine((sproc).getiuLeftX(), (sproc).getibRightY(), (sproc).getibRightX(), (sproc).getibRightY());
				//left line
				g.drawLine((sproc).getiuLeftX(), (sproc).getiuLeftY(), (sproc).getiuLeftX(), (sproc).getibRightY());
				//right line
				g.drawLine((sproc).getibRightX(),  (sproc).getiuLeftY(), (sproc).getibRightX(), (sproc).getibRightY());
				//then, print a "I" in the middle of it at the centroid.....

				g.drawString("IN", (sproc).getIBCentroidX()-5, (sproc).getIBCentroidY() + 5);
				
				//draw a circle to indicate it's an inbox...

				g.drawOval((sproc).getIBCentroidX()-15, (sproc).getIBCentroidY()-15, 30, 30);
				
				//Draw the OUtLet Box.

				//top line
				g.drawLine((sproc).getouLeftX(), (sproc).getouLeftY(), (sproc).getobRightX(), (sproc).getouLeftY());
				//bottom line
				g.drawLine((sproc).getouLeftX(), (sproc).getobRightY(), (sproc).getobRightX(), (sproc).getobRightY());
				//left line
				g.drawLine((sproc).getouLeftX(), (sproc).getouLeftY(), (sproc).getouLeftX(), (sproc).getobRightY());
				//right line
				g.drawLine((sproc).getobRightX(),  (sproc).getouLeftY(), (sproc).getobRightX(), (sproc).getobRightY());
				//then, print a number in the middle of it at the centroid.....

				g.drawString("" + (sproc).getOBWireOutCount(), (sproc).getOBCentroidX() - 7, (sproc).getOBCentroidY() + 5);
				
				//draw the mini piano label
				
				//first, divide it into three lines, a 1/3 and 2/3 offset from the bottom right x ...
				
				g.drawLine((sproc).getBRightX() -10 , (sproc).getULeftY(), (sproc).getBRightX() -10, (sproc).getBRightY());
				g.drawLine((sproc).getBRightX() -20 , (sproc).getULeftY(), (sproc).getBRightX() -20, (sproc).getBRightY());
				g.drawLine((sproc).getBRightX() -30 , (sproc).getULeftY(), (sproc).getBRightX() -30, (sproc).getBRightY());
				
				//then, draw the two FILLED rectangles...
				
				 //start3.setLocation((sproc).getCircle1X(), (sproc).getCircle3Y());
				 //end3.setLocation((sproc).getCircle1X()+30, (sproc).getCircle3Y());

				 LinearGradientPaint blacktowhite = new LinearGradientPaint(start3, end3, dist, bcolors);
			     
				 g2.setPaint(blacktowhite);
			     g2.fillRect((sproc).getBRightX() -14, (sproc).getBRightY(), 8, 17);
			     g2.fillRect((sproc).getBRightX() -24, (sproc).getBRightY(), 8, 17);
				
			   //Draw the Open WAV File Box

					//top line
					g.drawLine((sproc).getouLeftX() - 60, (sproc).getouLeftY(), (sproc).getobRightX() - 60, (sproc).getouLeftY());
					//bottom line
					g.drawLine((sproc).getouLeftX() - 60, (sproc).getobRightY(), (sproc).getobRightX() - 60, (sproc).getobRightY());
					//left line
					g.drawLine((sproc).getouLeftX() - 60, (sproc).getouLeftY(), (sproc).getouLeftX() - 60, (sproc).getobRightY());
					//right line
					g.drawLine((sproc).getobRightX() - 60,  (sproc).getouLeftY(), (sproc).getobRightX() - 60, (sproc).getobRightY());
					
					//draw the note in the middle of the wav box... (line and fillOval...)
					g.drawLine((sproc).getobRightX() - 100, (sproc).getobRightY()-30, (sproc).getobRightX() - 100, (sproc).getobRightY()-15);
					g.fillOval( (sproc).getobRightX() - 110, (sproc).getobRightY() - 20, 10, 10);
					
					//then draw the WAV extension
					g.drawString("WAV", (sproc).getobRightX() - 117, (sproc).getobRightY());
					
					//Draw the open Convolution File Box.
					


					//top line
					g.drawLine((sproc).getouLeftX() - 90, (sproc).getouLeftY(), (sproc).getobRightX() - 90, (sproc).getouLeftY());
					//bottom line
					g.drawLine((sproc).getouLeftX() - 90, (sproc).getobRightY(), (sproc).getobRightX() - 90, (sproc).getobRightY());
					//left line
					g.drawLine((sproc).getouLeftX() - 90, (sproc).getouLeftY(), (sproc).getouLeftX() - 90, (sproc).getobRightY());
					//right line
					g.drawLine((sproc).getobRightX() - 90,  (sproc).getouLeftY(), (sproc).getobRightX() - 90, (sproc).getobRightY());
					
					//draw the note in the middle of the wav box... (line and fillOval...)
					g.drawLine((sproc).getobRightX() - 130, (sproc).getobRightY()-30, (sproc).getobRightX() - 130, (sproc).getobRightY()-15);
					g.fillOval( (sproc).getobRightX() - 140, (sproc).getobRightY() - 20, 10, 10);
					
					//then draw the WAV extension
					g.drawString("CNV", (sproc).getobRightX() - 147, (sproc).getobRightY());
					
					//Draw the Play
					//or stop, depending on if stopmode is enabled...
					if(sproc.getStopMode())
					{
						//top line
						g.drawLine((sproc).getouLeftX() - 50, (sproc).getouLeftY()+10, (sproc).getobRightX() - 70, (sproc).getouLeftY()+10);
						//bottom line
						g.drawLine((sproc).getouLeftX() - 50, (sproc).getouLeftY()+20, (sproc).getobRightX() - 70, (sproc).getouLeftY()+20);
						//left line
						g.drawLine((sproc).getouLeftX() - 50, (sproc).getouLeftY()+10, (sproc).getouLeftX() - 50, (sproc).getobRightY()-10);
						//right line
						g.drawLine((sproc).getouLeftX() - 40, (sproc).getouLeftY()+10, (sproc).getouLeftX() - 40, (sproc).getobRightY()-10);
						
					}
					else
					{
						//draw the stop
						//line vertical...
						g.drawLine((sproc).getobRightX() - 80, (sproc).getobRightY()-20, (sproc).getobRightX() - 80, (sproc).getobRightY()-10);
						//line diagonal 1
						g.drawLine((sproc).getobRightX() - 80, (sproc).getobRightY()-20, (sproc).getobRightX() - 70, (sproc).getobRightY()-15);
						//line diagonal 2
						g.drawLine((sproc).getobRightX() - 80, (sproc).getobRightY()-10, (sproc).getobRightX() - 70, (sproc).getobRightY()-15);
					}
				//draw the text nodes...
					


				//need to inda a better wav to detect if a sequence has stopped...
				//perhaps using the wav data itself?
					
					//also, try getting the looping mechanism working later
				/*if(sproc.getStopMode() && !sproc.getStreamAvailable())
				{
					//then switch to start mode...
					
					//have a HandlerClass called "currentPass" initialiazed to 1 whenever the greenlight is pressed.
					//check if the pass is less than the current pass...
					
					//basically only call stopWavFile if it has been played, aka playmode == true
					
						
						sproc.stopWavFile();
					
					
					//and then trigger (start playing)
					//the next samples conneceted in the outlet.
				}*/
				
				/*
				 * 
				 * 
				 * 
				 * IMPORtANT......
				 * 
				 * 
				 * 
				 */
				
				//use this numebr as the "length" of the wav file.
				//automatically turn it off once the time has elapsed
				//from when the wav has started to when the wav ends
				//	return audio.getLength();
				
			    //System.out.println(sproc.getStreamAvailable());
			     
			     
			     
			     
			     
			     
			     
			}

}