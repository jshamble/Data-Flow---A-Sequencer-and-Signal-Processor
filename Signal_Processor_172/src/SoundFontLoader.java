import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiSystem;



import javax.sound.sampled.*;

import com.sun.media.sound.SF2Instrument;
import com.sun.media.sound.SF2Soundbank;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SoundFontLoader {
	
	private Transmitter midiTransmitter;
	Instrument[] instruments;
	//private File realfont = new File("C:/Users/Shamblen/workspace/Signal_Processor_172/SF2/RealFont_2_1.SF2");
	private File realfont; 
	//look in the relative directory in SF2
	
	//private String filePath = new File("").getAbsolutePath();
	private SF2Instrument[] InstrArray;
	private String [] instrNames;
	
	private static Synthesizer mainSynth;
	
	public SoundFontLoader()
	{
		try {
			mainSynth = MidiSystem.getSynthesizer();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Synthesizer getSynth()
	{
		return mainSynth;
	}
	
	public File getSoundFileName()
	{
		return realfont;
	}
    
    public File getSoundFont() {

        String fileName = null;
        boolean bName = false;
        File dir = new File("./SF2/");//filePath + "/SF2/");
        File[] files = dir.listFiles();

        for (File f : files) {

            fileName = f.getName();

            Pattern uName = Pattern.compile("(.*/)*.+\\.(sf2)$");
            Matcher mUname = uName.matcher(fileName.toLowerCase());
            bName = mUname.matches();
            if (bName) {
            	//loads the first file that it mtaches on the list...
            	System.out.println("loaded " + fileName + " located in " + dir + "/" + fileName);
            	return f;
            }
        }
        
		return dir;
    }
	
	public void loadSoundFont() throws MidiUnavailableException
	{
		//add override method for user-selected soudnfont
		
		realfont = getSoundFont();
			
			//user getName to differentiate them...
			//and also to display in a nice gridlist interface... perhaps with regular expression
			//String searching...
			
			//params:
			
			//noteOn
			//noteNumber - the MIDI note number, from 0 to 127 (60 = Middle C)
			//Velocity - 0 to 127
			
			//program / bank change
			
			//the loading of soundfont files will have to be done via message passing(not implemented yet, will be inplemented in the summer or something, not now =)
			//It will scan for the .SF2/.sf2 extension in the sf2 folder initially, and grab the first one (don't care about upper or lowercase regex.)
			
			//int bank, int program...
			//have one drop down list for the bank, another for the program
			//right click to bring up a new panel to edit the notes via piano roll...
			
			//mc[0].programChange(5);
			//turn all notes off... (undefined behavior in the MIDI 1.0 spec. if notes are left on, not nesc. guaranteed to switch...)
			//mc[0].allNotesOff();
			//then trigger the notes wanted on again.
			//mc[0].noteOn(60,600);
			
			//userful methods....
			//mc[0].getPitchBend(); // pitch bend knob - get it working as an effect....
			//mc[0].programChange(bank, program)
			//mc[0].programChange(program)
			//mc[0].allNotesOff();
			//mc[0].noteOn(60,600);
			//mc[0].setSolo(soloState) // useful for playing just the measure alone...
			
			//look into the sequencer class to do this... http://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Sequencer.html
			//http://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Sequence.html
			
			//remember mc[1], mc[2], etc. are separate channels...
			
			/* IMPORTANT
			 * 
			 * MIDI CHANNELS WILL BE IN USE FOR ONE MEASURE, then not being used.
			 * however, with multiple measures, how do we solve this problem of not using a another mIDI channel's channel while it is still in use?
			 * 
			 * SImple, add the integer identifier (0 through mc.length) to the "inUSE" arraylist, and simply remove it from the "inUse" ArrayList
			 * After the measure is over && the other MIDI channels are there are no more MIDI channels to use, we don't want a reverb/effect cutoff or something!)
			 * 
			 * After the measure is over, enqueue the number onto the queue. dequeue the number when there are no more channels () integers available to pick from
			 * 
			 * 
			 * 
			 * 
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 *
			 * 
			 * 
			 * 
			 * 
			 * 
			 */
			
			//void programChange(int program)
			//changes it to another MIDI program (patch)- numbers 0 to 127 in the sf2 file
			
			//noteOff
			//noteNumber - the MIDI note number, from 0 to 127 (60 = Middle C)
			//Velocity - 0 to 127
			
			 /*   ADVANCED! :::
				* 
				void programChange(int bank,
				int program)
				Changes the program using bank and program (patch) numbers.
				 It is possible that the underlying synthesizer does not support a specific bank, or program. In order to verify that a call to programChange was successful, 
				 use getProgram and getController. Since banks are changed by way of control changes, you can verify the current bank with the following statement:
				int bank = (getController(0) * 128)
				 + getController(32); 
				Parameters:
				bank - the bank number to switch to (0 to 16383)
				program - the program (patch) to use in the specified bank (0 to 127)
				See Also:
				programChange(int), getProgram()
			 */
			
			//API:
			//http://docs.oracle.com/javase/7/docs/api/javax/sound/midi/MidiChannel.html
	}

	public void setInstrumentArray(SF2Instrument[] instruments) {
		// TODO Auto-generated method stub
		InstrArray = instruments;
		instrNames = new String[InstrArray.length];
		for(int i = 0; i < InstrArray.length; i++)
		{
			instrNames[i] = InstrArray[i].getName();
		}
	}
	
	public Instrument [] getInstrumentArray()
	{
		return InstrArray;
	}
	
	public String [] getInstrumentArraynames()
	{
		return instrNames;
	}
	
}
