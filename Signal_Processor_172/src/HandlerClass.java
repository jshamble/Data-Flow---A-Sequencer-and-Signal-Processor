import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

	public class HandlerClass extends JPanel implements KeyListener, MouseListener, MouseMotionListener
	{

		//have a stack of deleted objects (Wires and SignalProcessors),
		//then simply pop the last element of the stack and do the appropriate calculations accordingly
		
		private SoundFontLoader sfl;
		
		private static JFileChooser fileChooser = new JFileChooser();
		
		private boolean mouseJustMoved = false;
		
		public boolean getMouseMoved()
		{
			return mouseJustMoved;
		}
		
		public void setMouseMoved(boolean mm)
		{
			mouseJustMoved = mm;
		}
		
		private JScrollPane jsp;
		
		public JScrollPane getScrollPane()
		{
			return jsp;
		}
		
		private boolean stasis = false;
		
		private int verticalScrollBarOffset = 0;
		private int horizontalScrollBarOffset = 0;
		
		//stasis - a.k.a a user is attempting to connect a wire...
		
		public boolean getStasis()
		{
			return stasis;
		}
		
		public void setStatis(boolean newStasis)
		{
			stasis = newStasis;
		}
		
		private Wire wire;
		
		public Wire getWire()
		{
			return wire;
		}
		
		private boolean selectionMode = false;
		private String objName = "";
		
		private boolean creationMode = false;
		
		private JFrame jframe;
		
		private ObjectList ol = new ObjectList();
		
		private Graphics gContext;
		//private MainLoop ml = new MainLoop();
		//dragging rectangle dimensions.
		private int onClickX = 0;
		private int onClickY = 0;
		private int onClickX2 = 0;
		private int onClickY2 = 0;
		
		private int currentMouseLocationX = 0;
		private int currentMouseLocationY = 0;

		private int releasedMouseLocationX = 0;
		private int releasedMouseLocationY = 0;
		
		private int differenceVectorX = 0;
		private int differenceVectorY = 0;
		
		private ArrayList<SignalProcessor> allSP = new ArrayList<SignalProcessor>();

		private ArrayList<SignalProcessor> allSPRemoved = new ArrayList<SignalProcessor>();
		
		public void setALLSP(ArrayList<SignalProcessor> asp)
		{
			allSP = asp;
		}
		
		private Canvas canvas;
		
		private HandlerClass hc;
		
		private JPanel panel;
		
		public boolean getSelectionMode()
		{
			return selectionMode;
		}
		
		public ObjectList getObjectList()
		{
			return ol;
		}
		
		public void setHandlerClass(HandlerClass h)
		{
			hc = h;
		}

		public HandlerClass(JFrame frame)
		{
			jframe = frame;
		}
		
		public void setSFL(SoundFontLoader sfload)
		{
			sfl = sfload;
		}
		

		public void setPanel(JPanel jp) {
			panel = jp;
		}
		
		//inner classes
		
        public class ListenAdditionsScrolled implements ChangeListener{

			@Override
			public void stateChanged(ChangeEvent ce) {
				// TODO Auto-generated method stub
				
					Object source = ce.getSource();
				    if (source instanceof BoundedRangeModel) {
				      BoundedRangeModel aModel = (BoundedRangeModel) source;
				      if (!aModel.getValueIsAdjusting()) {
				        System.out.println("Changed: " + aModel.getValue());
				      }
				    } else {
				      //System.out.println("Something changed: " + source);
				    	System.out.println(((JViewport) source).getX());
				    }
				    
				  jsp.revalidate();
	              jframe.repaint();
			}
        }
        
        private class CustomAdjustmentListener implements AdjustmentListener {
        	        @Override
        	        public void adjustmentValueChanged(AdjustmentEvent evt) {
        	        Adjustable source = evt.getAdjustable();
  

        	        
          	        for(int i = 0; i < allSP.size(); i++)
              		  {
              			  if(allSP.get(i).isASproc())
              			  {
              				  ((Sproc)allSP.get(i)).getInstrumentList().setVisible(false);
              			  }
              		  }
          	        
          	        
        	        
        	  // check if user is currently dragging the scrollbar's knob
        	  if (evt.getValueIsAdjusting()) {
        		  return;
        	  }
        	  // get the orientation of the adjustable object
        	 
        	  int orient = source.getOrientation();
        	  
        	  /*if (orient == Adjustable.HORIZONTAL) {
        	    System.out.println("Event from horizontal scrollbar");
        	  }
        	  else {
        	    System.out.println("Event from vertical scrollbar");
        	  }*/
        	  
        	  
        	  // get the type of adjustment which caused the value changed event
        	  int type = evt.getAdjustmentType();
        	  switch (type) {
        	    case AdjustmentEvent.UNIT_INCREMENT:
        	      //System.out.println("increased by one unit");
        	  break;
        	    case AdjustmentEvent.UNIT_DECREMENT:
        	      //System.out.println("decreased by one unit");
        	  break;
        	    case AdjustmentEvent.BLOCK_INCREMENT:
        	     // System.out.println("increased by one block");
        	  break;
        	    case AdjustmentEvent.BLOCK_DECREMENT:
        	     // System.out.println("decreased by one block");
        	
        	  break;
        	
        	    case AdjustmentEvent.TRACK:
        	
        	     // System.out.println("knob on the scrollbar was dragged");
        
        	  break;
        	 
        	  
        	  }
        	            // get the current value in the adjustment event
        	            int value = evt.getValue();

        	        	  if (orient == Adjustable.HORIZONTAL) {
        	        		  horizontalScrollBarOffset = value;
        	        	
        	        	  }
        	        	  else
        	        	  {
        	        		  verticalScrollBarOffset = value;
        	        	  }
        	        	  
        	        	  //for all sprocs...
    	        		  for(int i = 0; i < allSP.size(); i++)
    	        		  {
    	        			  if(allSP.get(i).isASproc())
    	        			  {
    	        				  ((Sproc)allSP.get(i)).getInstrumentList().setLocation(((Sproc)allSP.get(i)).getULeftX()+1 - horizontalScrollBarOffset, ((Sproc)allSP.get(i)).getULeftY() -9 - verticalScrollBarOffset);
    	        				  ((Sproc)allSP.get(i)).getInstrumentList().setVisible(true);
    	        			  }
    	        		  }
        	            
        	            //System.out.println("Current Value: " + value);
        	  }
        }

        
        //end inner classes

		public void setCanvas(Canvas can) {
			
				String path = "./res/everb2.jpg";
		        File file = new File(path);
		        BufferedImage image;
				
		        try {
					image = ImageIO.read(file);

			        JLabel label = new JLabel(new ImageIcon(image));
			        label.setVisible(true);
			        label.setSize(new Dimension(1000,1000));
			        label.setBounds(0, 0, 308, 94+30);
			        jframe.getLayeredPane().add(label, new Integer(0));
			        
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			canvas = can;
			//here is the tricky part...
			//amke sure to add the canvas to a background layer (integer value) of the content pane....

			//change to max width and height later, 20000 it's fine for now...
			canvas.setBounds(0, 20, 20000, 20000);
			canvas.setPreferredSize(new Dimension(20000,20000));
			jframe.setPreferredSize(new Dimension(20000,20000));
			jframe.getLayeredPane().setPreferredSize(new Dimension(20000,20000));
	        //jframe.getLayeredPane().setPreferredSize(new Dimension(20000, 20000));
			
	        //so, layer 1 is our canvas. Anything placed above 1
	        //will be displayed in front of the canvas.
	        
	        //jframe.getLayeredPane().add(canvas, new Integer(1));
	        
	        //high integer value to ovveride everything...
	        
			JScrollPane scrollBar=new JScrollPane(canvas,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollBar.setOpaque(true);
	        scrollBar.addMouseListener(this);
	        scrollBar.addMouseMotionListener(this);
			scrollBar.addKeyListener(this);
	        //scrollBar.setSize(scrollBar.getViewport().getSize().width,scrollBar.getViewport().getSize().height);
			scrollBar.add(jframe.getJMenuBar(),new Integer(0));
	        scrollBar.revalidate();
	        
	        jsp = scrollBar;
	        canvas.setScrollBar(jsp);
	        
	        //ad the menu bar backk....
			
	        jframe.getLayeredPane().add(scrollBar, BorderLayout.PAGE_END, new Integer(100));
	        jframe.getLayeredPane().add(jframe.getJMenuBar(),new Integer (101));
	        
	        jsp.setBounds(0, 20, 100, 100); // not sure why this works... but it updates properly in the paintCompnent method (bottom) 
	        
	        //jsp.getViewport().addChangeListener(new ListenAdditionsScrolled());
	        
	        CustomAdjustmentListener adjustmentListener = new CustomAdjustmentListener();
	        jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
	        jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener); 
	        
	        jframe.revalidate();
			//jsp.setBounds(0, 20, jframe.getSize().width,  jframe.getSize().height);
			
			//jframe.setLayeredPane(layeredPane);
			
		}
		
		public Canvas getCanvas() {
			return canvas;
		}
		
		public void setGraphicsContext(Graphics g)
		{
			gContext = g;
		}
		
		public Graphics getGraphicsContext()
		{
			return gContext;
		}
		
		public int getOnClickX()
		{
			return onClickX;
		}
		
		public int getOnClickX2()
		{
			return onClickX2;
		}
		
		public int getOnClickY()
		{
			return onClickY;
		}
		
		public int getOnClickY2()
		{
			return onClickY2;
		}
		
		public void setOnClickX(int newx)
		{
			onClickX = newx;
		}
		
		public void setOnClickX2(int newx2)
		{
			onClickX2 = newx2;
		}
		
		public void setOnClickY(int newy)
		{
			onClickY = newy;
		}
		
		public void setOnClickY2(int newy2)
		{
			onClickY2 = newy2;
		}
		
		public boolean getCreationMode()
		{
			return creationMode;
		}
		
		public int getCurrentMouseLocationX()
		{
			return currentMouseLocationX;
		}
		
		public int getCurrentMouseLocationY()
		{
			return currentMouseLocationY;
		}
		
		public int getReleasedMouseLocationX()
		{
			return releasedMouseLocationX;
		}
		
		public int getReleasedMouseLocationY()
		{
			return releasedMouseLocationY;
		}
		
		public String getObjName()
		{
			return objName;
		}
		
		public int getDifferenceVectorX()
		{
			return differenceVectorX;
		}

		public int getDifferenceVectorY()
		{
			return differenceVectorY;
		}
		
		@Override
		public void mouseClicked(MouseEvent me) {
		}

		@Override
		public void mouseEntered(MouseEvent me) {
			// When the Mouse enters the window
			
		}

		@Override
		public void mouseExited(MouseEvent me) {
			// When the Mouse leaves the window borders.
			
		}

		@Override
		public void mousePressed(MouseEvent me){
			
			//need to translate all MOUSE variables only 
			//whenever the scrollbar is clicked...
			
			//me.getButton
			//left click 1,
			//middle wheel click 2
			//right click 3
			//System.out.println(me.getButton());
			
			//here we set the initial x and y for mouse drag events, UNTIL
			//the mouse is released
			
			//turn off creation mode
			//reset the object name to the empty string.

			onClickX = me.getX()+ horizontalScrollBarOffset;
			onClickY = me.getY()+ verticalScrollBarOffset;
			/*
			if(wire != null)
			{
				wire.setEndpoints(onClickX, onClickY);
			}
			canvas.repaint();*/
			

			try {
				ol.createObject(jframe, panel, objName, currentMouseLocationX-7, currentMouseLocationY-7, hc, sfl);
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			objName = "";
			creationMode = false;
			
			//now, every object is unselected here...
			for(int i = 0 ; i < allSP.size(); i++)
			{
				allSP.get(i).setSelected(false);
				
				//now, check for collision detection for all objects here...
				//may need to add another for loop outside here, the collision
				//detection is done at the same time as the object unselection for algorithmic efficiency purposes...
				if(allSP.get(i).isATrafficLight())
				{
					onClickListenerTlight(me,(TrafficLight) allSP.get(i));
				}
				if(allSP.get(i).isASproc())
				{
					onClickListenerSproc(me,(Sproc) allSP.get(i));
				}
			}

			//update the rectangle here...
			
			
			//updtae all position of sproc's JCombo boxes here
			//and also set their visible fields to true.

			onClickListenerKnobs(me);
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			// TODO Auto-generated method stub
			
			//here, check for intersection with ANY Inbox of all SignalProcessor Objects.
			for(int i = 0; i < allSP.size(); i++)
			{
				if(allSP.get(i).isASproc())
				{
					onReleaseListenerSproc(me, (Sproc) allSP.get(i));
				}
			}

			//wire intersection test:
			//(for wire insertion)
			//simply call the AABBInstersection Muose method, passing in the coordinates
			//of each Inbox object for every SignalProcessor object...
			//make sure to increment the wire count and store the object in the wire list
			//for each successful connection...
			
			//wire intersection test:
			//(for wire deletion)
			//per -pixel collision detection.
			//first check if the mouse coordinates are within
			//the bounding box of the coordinates of the wire
			
			selectionMode = false;
			stasis = false;

			onClickX = 0;
			onClickY = 0;
			onClickX2 = 0;
			onClickY2 = 0;

			canvas.repaint();

			releasedMouseLocationX = me.getX() + horizontalScrollBarOffset;
			releasedMouseLocationY = me.getY() + verticalScrollBarOffset;

		}
		
		//MouseMotiionListener inherited methods

		@Override
		public void mouseDragged(MouseEvent me) {
			// TODO Auto-generated method stub
			
			onClickX2 = me.getX() + horizontalScrollBarOffset;
			onClickY2 = me.getY() + verticalScrollBarOffset;
			currentMouseLocationX =  me.getX() + horizontalScrollBarOffset;
			currentMouseLocationY =  me.getY() + verticalScrollBarOffset;
			
			//System.out.println(onClickX2 - onClickX);
			//System.out.println(onClickY2 - onClickY);
			
			//now, the wire's endpoints are updated on the screen here.
			//if not dragged to an inputbox, simply do not add the wire to the
			//wire list. Don't forget to increment the counter of the outbox 
			//if it is connected to an inbox!
			
			//ooh a null check... should optimize later!!!
			if(wire != null)
			{
				wire.setEndpoints(currentMouseLocationX-5, currentMouseLocationY-50);
			}
			if(!stasis)
			{
				selectionMode = true;
			}
			//update the rectangle here...
			canvas.repaint();
			
		}

		@Override
		public void mouseMoved(MouseEvent me) {
			mouseJustMoved = true;
			//the object's top left corner will be located
			//at the current mouse's position...
			differenceVectorX = me.getX() + horizontalScrollBarOffset - currentMouseLocationX;
			differenceVectorY = me.getY() + verticalScrollBarOffset  - currentMouseLocationY;
			
			currentMouseLocationX = me.getX() + horizontalScrollBarOffset;
			currentMouseLocationY = me.getY() + verticalScrollBarOffset ;

			canvas.repaint();
		}

		@Override
		public void keyPressed(KeyEvent key) {
			// TODO Auto-generated method stub

			if(key.isControlDown())
			{
				//when control + 1 is held down, bring up a textbox for the user to type in their string...

				if('1' == key.getKeyChar())
				{
					//prompt the user for text input here...
					//see textvaluechanged for info...
					creationMode = true;
					canvas.repaint();
				}
				
				//control z is undo delete...
				if(26 == key.getKeyChar())
				{
					if(allSPRemoved.size() > 0)
					{
						allSP.add( allSPRemoved.get(allSPRemoved.size()-1) );
						if(allSPRemoved.get(allSPRemoved.size()-1).isASproc())
						{
							((Sproc) allSPRemoved.get(allSPRemoved.size()-1)).getInstrumentList().setLocation(  ((Sproc) allSPRemoved.get(allSPRemoved.size()-1)).getULeftX(), ((Sproc) allSPRemoved.get(allSPRemoved.size()-1)).getULeftY()-10  );
							((Sproc) allSPRemoved.get(allSPRemoved.size()-1)).getInstrumentList().setVisible(true);
						}
						allSPRemoved.remove(allSPRemoved.size()-1);
					}

					canvas.setObjectList(allSP,ol.getWirelist());
					canvas.repaint();
				}
				if(19 == key.getKeyChar())
				{
					try {
						ol.saveAsData(0,"");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//shortcuts
				//TODO
				//ctrl + s: save
				//ctrl + c: copy object(s)
				//ctrl + v: paste object(s) (at mouse cursor, or, if pixel x and y location matches copy object, then translate object 20 pix right, 20 pix down)
			}
		}

		@Override
		public void keyReleased(KeyEvent key) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent key) {
			// TODO Auto-generated method stub

			//when in creation mode...

			if(creationMode)
			{
				//draw a dot on the current location, and the user's text on the screen...

				//if(key.getKeyChar() !+ )
				//if it's not a backspace...

				//System.out.println((int)key.getKeyChar());
				
				//if key pressed is the enter key...
				if((int)key.getKeyChar() == 10)
				{
					//send the string to the Object List class...
					try {
						ol.createObject(jframe, panel, objName, getCurrentMouseLocationX()-7,  getCurrentMouseLocationY()-7, hc, sfl);
					} catch (MidiUnavailableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					objName = "";
					creationMode = false;
				}
				//if key pressed is a normal input key...
				else if((int)key.getKeyChar() != 8)
				{
					objName = objName + key.getKeyChar();
				}
				else if((int)key.getKeyChar() == 8)
				{
					//a.k.a is at least the empty string - used to avoid exceptions...
					if(objName.length() > 0)
					{
						objName = objName.substring(0, objName.length()-1);
					}
				}
				
				// .update() displayed text on the screen...
				canvas.repaint();
			}
			
			//if it is the delete key...
			if((int)key.getKeyChar() == 127)
			{
				
						Iterator<SignalProcessor> it = allSP.iterator();
						while (it.hasNext()) {
						    SignalProcessor sp = it.next();
						    // Do something
							if(sp.getSelected())
							{
								//find all wire connections, and disconnect them, (saving both the input and output pairs in a queue.. to create a new wire objet later)
								//but for now, just remove them (first)
									
									Iterator<Wire> itWire = ol.getWirelist().iterator();
									while (itWire.hasNext()) {
									    Wire wir = itWire.next();
									    
									//for the size of the wirelist,
									//find each wire that that has an input-output pair, such that this is
									//on the input list, and then disconnect them, and the remove the wire from the wire list.
									//actually, jsut use an iterator to do this, otherwise it will cause problems when an 
									//object is removed...
									
									if(wir.getinId() == sp.getID())
									{
										//use an iterator here, too.
										
										Iterator<SignalProcessor> ItspInner = allSP.iterator();
										while (ItspInner.hasNext()) {
											SignalProcessor spInner = ItspInner.next();
										    
											if((wir.getoutId()) == spInner.getID())
											{
												//decrement the out wire count...
												spInner.setOBWireOutCount(spInner.getOBWireOutCount() - 1);
												wir.disconnect(spInner,sp);
												itWire.remove();
											}
										}
									}
									
									//also, do the same check, but reverse the inputID and outputID.
									
									if(wir.getoutId() == sp.getID())
									{
										//use an iterator here, too.
										
										Iterator<SignalProcessor> ItspOuter = allSP.iterator();
										while (ItspOuter.hasNext()) {
											SignalProcessor spOuter = ItspOuter.next();
											if((wir.getinId()) == spOuter.getID())
											{
												//decrement the out wire count...
												//or set it to zero...
												sp.setOBWireOutCount(sp.getOBWireOutCount() - 1);
												wir.disconnect(spOuter,sp);
												itWire.remove();
											}
										}
									}
									
								}
								sp.setSelected(false);
								
								if(sp.isASproc())
								{
									((Sproc)sp).getInstrumentList().setVisible(false);
								}
								
								allSPRemoved.add(sp);
								it.remove();
							}
						}
						
				canvas.setObjectList(allSP,ol.getWirelist());
				canvas.repaint();
			}
		}
		
		//also make onReleaseListeners for each SignalProcessor Type.
		
		private void onClickListenerTlight(MouseEvent me, TrafficLight tlight)
		{

			int currPositionX = me.getX() + horizontalScrollBarOffset;
			int currPositionY = me.getY() + verticalScrollBarOffset;
			
			/*
			System.out.println("xULEFT" + tlight.getULeftX());
			System.out.println("xBRIGHTX" + tlight.getBRightX());

			System.out.println("xULEFTY" + tlight.getULeftY());
			System.out.println("xBRIGHTY" + tlight.getBRightY());
			

			System.out.println("meX : " + currPositionX);
			System.out.println("meY : " + currPositionY); */
			
			//bounding box specific collision detection for traffic light objects:
			//(Done in order of priority)
			//check one: Green (Go)
			
			if((tlight).AABBInstersectionMouse((tlight).getBRightX(), (tlight).getULeftY(), (tlight).getULeftX(), (tlight).getBRightY()+60,currPositionX,currPositionY))
					{
				//begin all playback.
				if(me.getButton() == 1)
					{
						System.out.println("playback begun");
						sequenceMode = true;

						for(int i = 0; i < allSP.size(); i++)
						{
							if(allSP.get(i).isASproc())
							{
								((Sproc)allSP.get(i)).setPlayed(false);
							}
						}
						
						for(int i = 0; i < tlight.input_Pair_2.size(); i++)
						{
							if(tlight.input_Pair_2.get(i).isASproc())
							{
								if(((Sproc) tlight.input_Pair_2.get(i)).getFile() != null)
								{
										//if(!((Sproc) tlight.input_Pair_2.get(i)).getStreamAvailable())
										//{
											 Runnable worker = new SoundSync(((Sproc) tlight.input_Pair_2.get(i)));	
											 executor.execute(worker);
										//}
								}
							}
						}
						
					}
			}
			//check two: Wire connection (output Box) - a small rectangle drawn to the side (basically 4 pixels wide, Tlight height tall...)
			if((tlight).AABBInstersectionMouse((tlight).getBRightX()+30, (tlight).getULeftY()-30, (tlight).getULeftX()+30, (tlight).getBRightY()+30,currPositionX,currPositionY))
				{

				if(me.getButton() == 1)
					{
						//begin connect wire
						System.out.println("wire in temporal stasis");
						stasis = true;
						//gotta get the rightmost part of the square for output boxes.
						//leftmost part for input boxes.
						//to do this, calculate the midpoint Y, then
						//use the rightmost X as the beginning
						wire = new Wire((tlight).getobRightX(),(tlight).getMidpointY());
						wire.setEndpoints(currPositionX-5, currPositionY-50);
						wire.setOutId(tlight.getID());
					}
			}
			//check three: yellow (pause)
			if((tlight).AABBInstersectionMouse((tlight).getBRightX(), (tlight).getULeftY()-30, (tlight).getULeftX(), (tlight).getBRightY()+30,currPositionX,currPositionY))
				{
				if(me.getButton() == 1)
				{
					//pause playback.
					System.out.println("playback paused");
				}
			}
			//chcek four: Red (stop)
			if((tlight).AABBInstersectionMouse((tlight).getBRightX(), (tlight).getULeftY()-60, (tlight).getULeftX(), (tlight).getBRightY(),currPositionX,currPositionY))
			{

				if(me.getButton() == 1)
				{
					//stop all playback.
					System.out.println("playback stopped");
					
					sequenceMode = false;
					
					for(int i = 0; i < allSP.size(); i++)
					{
						if(allSP.get(i).isASproc())
						{
							((Sproc)allSP.get(i)).stop();
						}
					}
					
					//then stop the threads here

					executor = Executors.newFixedThreadPool(MYTHREADS);
				}
			}
		}
		


		private boolean convolveMode = false;
		public boolean getConvMode()
		{
			return convolveMode;
		}
		
		//set with AABB
		public void setConvMode(boolean newcm)
		{
			convolveMode = newcm;
		}
		
		private boolean chorMode = false;
		public boolean getChorMode()
		{
			return chorMode;
		}
		
		//set with AABB
		public void setChorMode(boolean newcm)
		{
			chorMode = newcm;
		}
		
		private boolean formantMode = false;
		public boolean getFormantMode()
		{
			return formantMode;
		}
		
		//set with AABB
		public void setFormantMode(boolean newfm)
		{
			formantMode = newfm;
		}
		
		private boolean ringModMode = false;
		public boolean getRingModMode()
		{
			return ringModMode;
		}
		
		//set with AABB
		public void setRingModMode(boolean newrm)
		{
			ringModMode = newrm;
		}
		
		public boolean hAABBInstersectionMouse(int ULeftX, int ULeftY, int BRightX,  int BRightY, int mouseX, int mouseY)
		{
			if( (mouseX < (BRightX + horizontalScrollBarOffset) ) && (mouseX > (ULeftX + horizontalScrollBarOffset)) && (mouseY > (BRightY + verticalScrollBarOffset)) && (mouseY < (ULeftY + verticalScrollBarOffset)))
			{
				//yes, 
				return true;
			}
			return false;
		}
		
		private void onClickListenerKnobs(MouseEvent me)
		{
			int currPositionX = me.getX() + horizontalScrollBarOffset;
			int currPositionY = me.getY() + verticalScrollBarOffset;
			
			if(hAABBInstersectionMouse(0, 60, 60, 0, currPositionX, currPositionY))
			{
				if(getConvMode())
				{

					System.out.println("ConvModeOff");
					setConvMode(false);
				}
				else
				{

					System.out.println("ConvModeOn");
					setConvMode(true);
				}
			}

			if(hAABBInstersectionMouse(61, 60, 150, 0, currPositionX, currPositionY))
			{
				if(getChorMode())
				{

					System.out.println("ChorModeOff");
					setChorMode(false);
				}
				else
				{

					System.out.println("ChorModeOn");
					setChorMode(true);
				}
			}
			
			if(hAABBInstersectionMouse(151, 60, 230, 0, currPositionX, currPositionY))
			{
				if(getFormantMode())
				{
					System.out.println("FormantModeOff");
					setFormantMode(false);
				}
				else
				{
					System.out.println("FormantModeOn");
					setFormantMode(true);
				}
			}
			
			if(hAABBInstersectionMouse(231, 60, 330, 0, currPositionX, currPositionY))
			{
				if(getRingModMode())
				{
					System.out.println("RingModModeOff");
					setRingModMode(false);
				}
				else
				{
					System.out.println("RingModModeOn");
					setRingModMode(true);
				}
			}
		}
		
		private void onClickListenerSproc(MouseEvent me, Sproc sproc)
		{
			//DONE! need to do onreleaselistener, and also tlight's intersection tests.
			
/*			System.out.println("xULEFT" + sproc.getULeftX());
			System.out.println("xBRIGHTX" + sproc.getBRightX());

			System.out.println("xULEFTY" + sproc.getULeftY());
			System.out.println("xBRIGHTY" + sproc.getBRightY());
			
			int currPositionX = me.getX() + horizontalScrollBarOffset;
			int currPositionY = me.getY() + verticalScrollBarOffset;

			System.out.println("meX : " + currPositionX);
			System.out.println("meY : " + currPositionY);*/

			int currPositionX = me.getX() + horizontalScrollBarOffset;
			int currPositionY = me.getY() + verticalScrollBarOffset;

			//check two: Wire connection (outputBox) - a small rectangle drawn to the side (basically 4 pixels wide, Tlight height tall...)
			if((sproc).AABBInstersectionMouse((sproc).getBRightX(), (sproc).getULeftY(), (sproc).getBRightX() + 30, (sproc).getBRightY(), currPositionX, currPositionY))
			{
				if(me.getButton() == 1)
					{
						//begin connect wire
						System.out.println("wire in temporal stasis");
						stasis = true;
						//gotta get the rightmost part of the square for output boxes.
						//leftmost part for input boxes.
						//to do this, calculate the midpoint Y, then
						//use the rightmost X as the beginning
						wire = new Wire((sproc).getobRightX(),(sproc).getMidpointY());
						wire.setEndpoints(currPositionX-5, currPositionY-50);
						wire.setOutId(sproc.getID());
					}
			}
			//piano roll check for sproc 30 left X offset...
			if((sproc).AABBInstersectionMouse((sproc).getBRightX() - 30, (sproc).getULeftY(), (sproc).getBRightX(), (sproc).getBRightY(), currPositionX, currPositionY))
				{
				if(me.getButton() == 1)
					{
						//begin connect wire
						System.out.println("Opening new Piano Roll Editor Window.");
						
						//check if the sproc already has one. (initilaize the private class variable to null in the class)
						if(sproc.getPianoRoll() == null)
						{
							//create a new one, and open it in a window
							
							//fix this later... frame not openin on click..
							
							
							PianoRoll pr = new PianoRoll();
							pr.revalidate();
							pr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							pr.setSize(640, 480);
							pr.setVisible(true);
							
							//add the piano roll here
							
							sproc.setPianoRoll(pr);
							
							//add Panels here...
							
						}
						else
						{
							//open up the Jpanels for the already edited one in a new window (frame),
							//if and oly if they are not open already...
						}
					}
			}
			//ON / OFF Convolution (create sproc object first)
			//X,Y,X2,Y2
			//open convolution file check..
			if((sproc).AABBInstersectionMouse((sproc).getBRightX() - 120, (sproc).getULeftY(), (sproc).getBRightX()-90, (sproc).getBRightY(), currPositionX, currPositionY))
			{	
				if(me.getButton() == 1)
					{
					int returnVal = fileChooser.showOpenDialog(jframe);
		    	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    	    	
		    	        File file = fileChooser.getSelectedFile();
						sproc.setFileName(file.getName());
						
		    	        try {
							//now, we save the file as an isntance variable in the sproc object.
							//maybe delete the previously stored file there...
							sproc.setConvWavSample(file);
							//sets up convolution
							sproc.InitConvolved();
							//set with aabb
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    	    } else {
		    	        System.out.println("File access cancelled by user.");
		    	    }
					}
			}
			//open file check..
			if((sproc).AABBInstersectionMouse((sproc).getBRightX() - 90, (sproc).getULeftY(), (sproc).getBRightX()-60, (sproc).getBRightY(), currPositionX, currPositionY))
			{

				if(me.getButton() == 1)
					{
					int returnVal = fileChooser.showOpenDialog(jframe);
		    	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    	    	
		    	        File file = fileChooser.getSelectedFile();
						sproc.setFileName(file.getName());
						
		    	        try {
							//now, we save the file as an isntance variable in the sproc object.
							//maybe delete the previously stored file there...
							sproc.setFile(file);
							
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    	    } else {
		    	        System.out.println("File access cancelled by user.");
		    	    }
					}
			}
			//test wav sound / MIDI sequence played.
			if((sproc).AABBInstersectionMouse((sproc).getBRightX() - 60, (sproc).getULeftY(), (sproc).getBRightX()-30, (sproc).getBRightY(), currPositionX, currPositionY))
			{
				if(sproc.getFile() != null)
				{
					if(me.getButton() == 1)
					{
						if(!(sproc.getStopMode()))
						{
							//if(!sproc.getStreamAvailable())
							//{
								Runnable worker = new SoundSyncPlay(sproc);	
								executor.execute(worker);
							//}			
						}
						else
						{
							//System.out.println("Now Playing " + sproc.getFileName());
							sproc.stop();
							executor.shutdownNow();
							executor = Executors.newFixedThreadPool(MYTHREADS);
						}
					}
				}
			}
		}
		

		private void onReleaseListenerSproc(MouseEvent me, Sproc sproc) {
				//check for AABB Bounding box collision of Mouse and the Inbox here...
			
			int currPositionX = me.getX() + horizontalScrollBarOffset;
			int currPositionY = me.getY() + verticalScrollBarOffset;

			if(stasis && (sproc).AABBInstersectionMouse((sproc).getULeftX() - 30, (sproc).getULeftY(), (sproc).getULeftX(), (sproc).getBRightY(), currPositionX, currPositionY))
			{
						//end stasis
						stasis = false;
						//begin connect wire
					
						//set the new endpoints to the inbox's left side (ULeftX + offset, YCentroid)
						wire.setEndpoints(sproc.getULeftX() -30, sproc.getIBCentroidY());
						
						//first, find the object in the same list with the same outID
						
						int outIDIndex = -1;
						
						for(int i = 0; i < allSP.size(); i++)
						{
							if(wire.getoutId() == allSP.get(i).getID())
							{
								outIDIndex = i;
								break;
							}
						}
						
						wire.setInId(sproc.getID());
						
						wire.connect(allSP.get(outIDIndex), sproc);
					
						//then add the wire to the wire list...
						ol.getWirelist().add(wire);
					
						//mark the objects as connected, and make sure to have an update method in the wire class that
						//sets the start and endpoints according to the current location of the outbox and the inbox.
						//and constantly draw and update the wire in the Canvas, by setting the startpoints and endpoints
						//a for loop for each object should do the trick, use getwirelist.
						wire = null;
			}
		}

		public int getHorizontalScrollBarOffset() {
			// TODO Auto-generated method stub
			return horizontalScrollBarOffset;
		}
		

		public int getVerticalScrollBarOffset() {
			// TODO Auto-generated method stub
			return verticalScrollBarOffset;
		}

		private boolean sequenceMode = false;
		
		public boolean getSequence() {
			// TODO Auto-generated method stub
			return sequenceMode;
		}
		

		private static final int MYTHREADS = 4;
	    private ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
	}