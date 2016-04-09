import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ObjectList {
	
	//effect inlet siezs will be dynamicall determined...
	//it will be square with knobs, each know has a label, but the size of
	//the square will be determined by hteme number of knobs/elements
	
	//where ai is the (optional) automtion input
	//| ai EFFECT_NAME EFFECT_SETTINGS settings/parameter list (text editable) |
	//| knob knob              |
	//| knob outlet            |
	
	//all of the SignalProcessor objects in the chain.
	private ArrayList<SignalProcessor> allSP = new ArrayList<SignalProcessor>();
	private ArrayList<String> saveData = new ArrayList<String>();
	private static int saveCount = 0;
	private Integer INT;
	private String filename;
	private ArrayList<Wire> allWires = new ArrayList<Wire>();
	private Wire wire;
	private ArrayList<Integer> inputWires = new ArrayList<Integer>();
	private ArrayList<Integer> outputWires = new ArrayList<Integer>();
	
	
	public void saveAsData(int saveAs, String filepath) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer;	
		if(saveAs == 1)
		{
			filename = filepath;
	    }
		else
		{
			filename = "untitiled" + saveCount + ".txt";
			saveCount++;
		}

		writer = new PrintWriter(filename, "UTF-8");
		
		for(int i = 0; i < allSP.size(); i++)
		{
			if(allSP.get(i).isATrafficLight())
			{
				//write save specific instructions to a file here...
				writer.print(allSP.get(i).getName());
				writer.print(";");
				writer.print(allSP.get(i).getULeftX());
				writer.print(";");
				writer.print(allSP.get(i).getULeftY());
				writer.print(";");
				writer.print(allSP.get(i).getID());
				writer.print(";");
				writer.print(allSP.get(i).getOBWireOutCount());
				
				for(int j = 0; j < allSP.get(i).output_Pair_1.size(); j++)
				{
					writer.print(";");
					writer.print(allSP.get(i).input_Pair_2.get(j).getID());
					writer.print(";");
					writer.print(allSP.get(i).output_Pair_1.get(j).getID());
				}
				//now add particular stuff to the inputs to be loaded list...
				//acrually, scrap the inputs to be laoded list,
				//just check against all objects for the input name (int ID) when it is triggered,
				//check in the list of messages first, then the list of effects, and finally the list of Everything that
				//is not a message nor an effect
				
				writer.println();
			}
			else if(allSP.get(i).isASproc())
			{
					//write save specific instructions to a file here...
					writer.print(allSP.get(i).getName());
					writer.print(";");
					writer.print(allSP.get(i).getULeftX());
					writer.print(";");
					writer.print(allSP.get(i).getULeftY());
					writer.print(";");

					//filename
					writer.print(((Sproc) (allSP.get(i))).getFile().getAbsolutePath());
					writer.print(";");
					
					writer.print(allSP.get(i).getID());
					writer.print(";");
					writer.print(allSP.get(i).getOBWireOutCount());
					
					for(int j = 0; j < allSP.get(i).output_Pair_1.size(); j++)
					{
						writer.print(";");
						writer.print(allSP.get(i).input_Pair_2.get(j).getID());
						writer.print(";");
						writer.print(allSP.get(i).output_Pair_1.get(j).getID());
					}
					writer.println();
			}
			//add in other objects here as you make them!
		}
		writer.close();
	}
	
	public void loadData(JFrame frame, JPanel panel, HandlerClass h, File file, SoundFontLoader sfl) throws IOException, NumberFormatException, MidiUnavailableException
	{
		//for every line in the file...
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		
		//clear all present objects on the screen.
		
		for(int i = 0; i < allSP.size(); i++)
		{
			if(allSP.get(i).isASproc())	
			{
				//maybe find a way to delete it...
				((Sproc)allSP.get(i)).getInstrumentList().setVisible(false);
			}
		}
		//allWires.clear();
		allSP.removeAll(allSP);
		allWires.removeAll(allWires);
		
		//for every line in the file...
		
		while ((line = br.readLine()) != null) {
		   // process the line.
			
			//using the ; as a regex character,
			
			String [] readline = line.split(";");
			//is the first token is a traffic light
			if("tlight".compareTo(readline[0]) == 0)
			{
				//create new traffic light object at current location passed in...
				System.out.println("new Traffic Light Object Created");
				TrafficLight t = new TrafficLight(frame, INT.parseInt(readline[1]),INT.parseInt(readline[2]), h);
				t.setID(INT.parseInt(readline[3]));
				for(int i = 0; i < INT.parseInt(readline[4]) * 2; i = i + 2)
				{
					// + 4 due to the readline offset.
					
					//then add the wires for the appropriate inputs...
					//input output pairs are listed last...
					//Wirecount:input0:output0:InputsToBeLoaded0:input1:output1:InputsToBeLoaded1:...etc.
					
					//find the input and output...
					
					int inputindex = INT.parseInt(readline[i + 5]);
					int outputindex = INT.parseInt(readline[i + 6]);
					inputWires.add(inputindex);
					outputWires.add(outputindex);
					
					
					//(allSP.get(inputindex), allSP.get(outputindex));
					//now, make a new wire object and connect the two!
				}
				allSP.add(t);
			}
			if("sproc".compareTo(readline[0]) == 0)
			{
				//create new traffic light object at current location passed in...
				System.out.println("new Sproc Object Created");
				Sproc sproc = new Sproc(frame, panel, INT.parseInt(readline[1]),INT.parseInt(readline[2]), h, sfl);
				
				//initlialize loaded wav file here...
				sproc.setFile(new File(readline[3]) );
				
				sproc.setID(INT.parseInt(readline[4]));
				for(int i = 0; i < INT.parseInt(readline[5]) * 2; i = i + 2)
				{
					// + 4 due to the readline offset.
					
					//then add the wires for the appropriate inputs...
					//input output pairs are listed last...
					//Wirecount:input0:output0:InputsToBeLoaded0:input1:output1:InputsToBeLoaded1:...etc.
					
					//find the input and output...
					

					int inputindex = INT.parseInt(readline[i + 6]);
					int outputindex = INT.parseInt(readline[i + 7]);
					inputWires.add(inputindex);
					outputWires.add(outputindex);
					
					//(allSP.get(inputindex), allSP.get(outputindex));
					//now, make a new wire object and connect the two!
				}
				allSP.add(sproc);
			}
			//add in other objects here as you make them!
		}
		br.close();
		

		//add the WIRES LAST!!!!
		
		//now that you have all signal processor objects loaded, search though them...
		
		
		for(int z = 0; z < outputWires.size(); z++)
		{
			System.out.println("in" + inputWires.get(z));
			System.out.println("out" + outputWires.get(z));
		}
		
		//set the start and endpoints here...
		for(int i = 0; i < outputWires.size(); i++)
		{
			//first, find out which wire matches the ID:
			int matchIDout = -1;
			int matchIDin = -1;
			for(int k = 0; k < allSP.size(); k++)
			{
				if(outputWires.get(i) == allSP.get(k).getID())
				{
					matchIDout = k;
				}
				else if(inputWires.get(i) == allSP.get(k).getID())
				{
					matchIDin = k;
				}
			}
			
			System.out.println(allSP.get(matchIDout).getOBWireOutCount());
			
			wire = new Wire(( allSP.get(matchIDout)).getobRightX(),( allSP.get(matchIDout)).getMidpointY());
			//wire.setEndpoints((( allSP.get(matchIDin)).getobRightX()), ( allSP.get(matchIDin)).getMidpointY());
			//now, make the physical connection here.
			
			wire.setInId(inputWires.get(i));
			wire.setOutId(outputWires.get(i));
			
			wire.connect(allSP.get(matchIDout), allSP.get(matchIDin));
			
			//then increment the count of the outbox (already done itn connect)
			//then add it to the list of active wires...
			allWires.add(wire);
			wire = null;
		}

		h.getCanvas().setObjectList(allSP,allWires);
		h.setALLSP(allSP);
		
	}
	
	public int getSize()
	{
		return allSP.size();
	}
	
	public ObjectList()
	{
		//initialize all Dictionary objects here...
	}
	
	public void createObject(JFrame frame, JPanel panel, String name, int upperLeftX, int upperLeftY,HandlerClass h, SoundFontLoader sfl) throws MidiUnavailableException
	{
		if(name.length() > 4)
		{
			if("tlight".compareTo(name) == 0)
			{
				//create new traffic light object at current location passed in...
				System.out.println("new Traffic Light Object Created");
				TrafficLight t = new TrafficLight(frame, upperLeftX, upperLeftY, h);
				allSP.add(t);
			}
			//clink = chain link objects...
			//clink [name]
			if("sproc".compareTo(name.substring(0, 5)) == 0)
			{
				//create new traffic light object at current location passed in...
				System.out.println("new Signal Processor Object Created");
				Sproc s = new Sproc(frame, panel, upperLeftX, upperLeftY, h, sfl);
				allSP.add(s);
			}
			//TODO add other string methods here using substring...
			
			//easter egg: Tetris (TO BE IMPLEMENTED SOMETIME: MAke all objects and wires fall, just like tetris pieces):
			/*if("tetris".compareTo(name.substring(0, 6)) == 0)
			{
				
				//infinite loop....
				int tetrisCounter = 0;
				
				int delay = 139;
				while(true)
				{
					tetrisCounter++;
					if(tetrisCounter % 500000000 == 0)
					{

						//tetris blocks fall here now...
						//play tetris A music
						for(int i = 0; i < allSP.size(); i++)
						{
							if(allSP.get(i).isATrafficLight())
							{
								allSP.get(i).setULeftX(allSP.get(i).getULeftX() - 10);
								allSP.get(i).setULeftY(allSP.get(i).getULeftY() - 10);
								allSP.get(i).updateOB();
								h.getCanvas().repaint();
							}
						}
					}
					if(Integer.MAX_VALUE == tetrisCounter - 100)
					{
						tetrisCounter = 0;
					}
				}
				
			}*/
			

			h.getCanvas().setObjectList(allSP,allWires);
			h.setALLSP(allSP);
		}	
	}

	public ArrayList<Wire> getWirelist() {
		return allWires;
	}
}