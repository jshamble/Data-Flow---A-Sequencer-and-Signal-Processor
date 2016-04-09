
public class Wire
{
	//Wire
	
	//make sure that they aren't connected already, can't connect two different wires
	//from the same inbox to the same outbox
	
	private int startPointX = 0;
	private int startPointY = 0;
	private int endPointX = 0;
	private int endPointY = 0;
	
	private int inId = -1;
	private int outId = -1;
	
	public int getinId()
	{
		return inId;
	}
	
	public int getoutId()
	{
		return outId;
	}
	
	public void setInId(int newInId)
	{
		inId = newInId;
	}
	
	public void setOutId(int newOutId)
	{
		outId = newOutId;
	}
	
	public int getStartPointX()
	{
		return startPointX;
	}
	
	public int getStartPointY()
	{
		return startPointY;
	}
	
	public int getEndPointX()
	{
		return endPointX;
	}
	
	public int getEndPointY()
	{
		return endPointY;
	}
	
	public void setStartpoints(int x, int y)
	{
		startPointX = x;
		startPointY = y;
	}
	
	public void setEndpoints(int x, int y)
	{
		endPointX = x;
		endPointY = y;
	}
	
	public Wire(int x, int y)
	{
		startPointX = x;
		startPointY = y;
	}
	
	public void connectEffect(SignalProcessor sp1, Effect e)
	{
	  sp1.effects.add(e);
	}
	
	public void connect (SignalProcessor out, SignalProcessor in)
	{
		out.setOBWireOutCount(out.getOBWireOutCount()+1);
		out.output_Pair_1.add(out);
		out.input_Pair_2.add(in);
	}
	
	public void disconnect (SignalProcessor out, SignalProcessor in) //method (Undo Key), or click and delete
	{
		out.output_Pair_1.remove(out);
		out.input_Pair_2.remove(in);
	}

}