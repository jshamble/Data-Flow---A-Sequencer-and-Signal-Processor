import java.util.ArrayList;

abstract class SignalProcessor 
{
	protected static int id = 0;
	protected int identifier;
	
	private boolean playedAlready = false;
	
	public boolean getPlayed()
	{
		return playedAlready;
	}
	
	public void setPlayed(boolean newPlay)
	{
		playedAlready = newPlay;
	}
	
	//returns the int identifier of the signalprocessor object
	public int getID()
	{
		return identifier;
	}
	
	public void setID(int newID)
	{
		identifier = newID;
	}
	
	protected HandlerClass hand;
	
	
	//make it so the signal processors (rectangles) themselves are animated, not the wires.
	//perhaps use gradient fill with one color per new branch (randomly generated...)...
	//how to tell if there is a new branch? check if the number of outputs is greater than one.
	//if so, then randomize the color of each extension of the tree...
	private double volume = 80;
	

	//an outletport will display the number of wires that it is feeding signals into.
	//  ASCII ART
	//   ___
	//  
	// |  3  | 
	//   ___
	
	//this outlet box has three wire going out.
	
	//each SIgnalPorcessor Object and all of its inheritors will have one (and only one)
	//outlet box (OUTBOX), and a counter for the number of wires going out, initially zero;
	protected int wireOutCount = 0;
	protected int ouLeftX = 0;
	protected int ouLeftY = 0;
	protected int obRightX = 0;
	protected int obRightY = 0;
	protected int obScale = 30;
	
	//the inlet box (INBOX)
	protected int iuLeftX = 0;
	protected int iuLeftY = 0;
	protected int ibRightX = 0;
	protected int ibRightY = 0;
	protected int ibScale = 30;
	
	public int getOBWireOutCount()
	{
		return wireOutCount;
	}
	
	public void setOBWireOutCount(int newWireoutCount)
	{
		wireOutCount = newWireoutCount;
	}
	
	public int getouLeftX()
	{
		return ouLeftX;
	}
	
	public int getouLeftY()
	{
		return ouLeftY;
	}
	
	public int getobRightX()
	{
		return obRightX;
	}
	
	public int getobRightY()
	{
		return obRightY;
	}
	
	public int getiuLeftX()
	{
		return iuLeftX;
	}
	
	public int getiuLeftY()
	{
		return iuLeftY;
	}
	
	public int getibRightX()
	{
		return ibRightX;
	}
	
	public int getibRightY()
	{
		return ibRightY;
	}
	
		
	//the calucaltion of the coordinates of the outletobx will be as follows:
	//always to the right of the object:
	//start with the bottom right x, bottom right Y.
	
	//then afterward, get the mid point of the two Y'S (add them together and divide by two.)
	//finally, starting from that point (BrightX,midpointY), top left X will be BrightX,
	//top Left y will be (obscale / 2 ) pix up, bottom Right y will be (obscale / 2 ) pix down
	//and bottom left X will be BrightX + obScale.
	//the number will be drawn at the centroid, ((x1 + x2)/2),((y1 + y2)/2)
	
	//AABB Bounding box collision detection
	
	//dimensions for the rectangular object...

	//protected variables 
	//are inherited by the subclass..
	protected int uLeftX = 0;
	protected int uLeftY = 0;
	protected int bRightX = 0;
	protected int bRightY = 0;
	protected int midpointY = 0;
	
	public int getMidpointY(){
		return midpointY;
	}

	protected int obCentroidX = 0;
	protected int obCentroidY = 0;
	
	protected int ibCentroidX = 0;
	protected int ibCentroidY = 0;
	
	public void updateOB()
	{
		if(this.isASproc())
		{
			ouLeftX = bRightX;
			obRightX = bRightX + obScale;
		}
		else if(this.isATrafficLight())
		{
			ouLeftX = uLeftX;
			obRightX = uLeftX + obScale;
		}
		//calcualte midpoint between top right y and bottom right y...
		midpointY = (uLeftY + bRightY) / 2;
		ouLeftY = midpointY - (obScale / 2);
		obRightY = midpointY + (obScale / 2);
		obCentroidX = (ouLeftX + obRightX ) / 2;
		obCentroidY = (ouLeftY + obRightY ) / 2;
	}
	
	public void updateIB()
	{
		if(this.isASproc())
		{
			iuLeftX = uLeftX - ibScale;
			ibRightX = uLeftX;
		}
		else if(this.isATrafficLight())
		{
			iuLeftX = uLeftX;
			ibRightX = uLeftX + ibScale;
		}
		//calculate midpoint between top right y and bottom right y...
		midpointY = (uLeftY + bRightY) / 2;
		iuLeftY = midpointY - (ibScale / 2);
		ibRightY = midpointY + (ibScale / 2);
		ibCentroidX = (iuLeftX + ibRightX ) / 2;
		ibCentroidY = (iuLeftY + ibRightY ) / 2;
	}
	
	
	protected int scale = 1;
	
	//input output pairs for fileloading
	protected ArrayList<SignalProcessor> output_Pair_1 = new ArrayList<SignalProcessor>();
	protected ArrayList<SignalProcessor> input_Pair_2 = new ArrayList<SignalProcessor>();
	
	public ArrayList<Effect> effects;
	
	
	public int getIBCentroidX()
	{
		return ibCentroidX;
	}
	
	public int getIBCentroidY()
	{
		return ibCentroidY;
	}
	
	public int getOBCentroidX()
	{
		return obCentroidX;
	}
	
	
	public int getOBCentroidY()
	{
		return obCentroidY;
	}
	
	
	public int getULeftX()
	{
		return uLeftX;
	}
	
	public int getULeftY()
	{
		return uLeftY;
	}
	
	public int getBRightX()
	{
		return bRightX;
	}
	
	public int getBRightY()
	{
		return bRightY;
	}
	
	public void setULeftX(int newLeftX)
	{
	   uLeftX = newLeftX;
	}
	
	public  void setULeftY(int newLeftY)
	{
		   uLeftY = newLeftY;
	}
	
	public void setBRightX(int newRightX)
	{
		bRightX = newRightX;
	}
	
	public void setBRightY(int newRightY)
	{
		bRightY = newRightY;
	}
	
	public void setVolume(double newVolume)
	{
		volume = newVolume;
	}
	
	public double getVolume()
	{
		return volume;
	}

	//methods to be overridden in subclasses....
	
	public boolean isATrafficLight() {return false;}
	public boolean isASproc() {return false;}
	
	public boolean isSelected = false;
	public void drawUpdate(int translateX, int translateY) {}

	public void setSelected(boolean b) {
		isSelected = b;
	};
	
	public boolean getSelected() {
		return isSelected;
	};
	
	public String getName()
	{
		return "sproc"; //sprocket
	}


	//checks for mouse coordinate intersection within a bounding box
	public boolean AABBInstersectionMouse(int ULeftX, int ULeftY, int BRightX,  int BRightY, int mouseX, int mouseY)
	{
		if( (mouseX < BRightX) && (mouseX > ULeftX) && (mouseY > BRightY) && (mouseY < ULeftY))
		{
			//yes, 
			return true;
		}
		return false;
	}
	
	public void update()
	{
		
	}
	
	
	//connection is done in the Wire Class....
}
