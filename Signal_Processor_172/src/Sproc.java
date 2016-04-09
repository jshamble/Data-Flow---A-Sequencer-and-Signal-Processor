import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.EnvelopeFollower;
import be.hogent.tarsos.dsp.GainProcessor;
import be.hogent.tarsos.dsp.MultichannelToMono;
import be.hogent.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.hogent.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.hogent.tarsos.dsp.WaveformWriter;
import be.hogent.tarsos.dsp.filters.BandPass;
import be.hogent.tarsos.dsp.resample.RateTransposer;

import com.sun.media.sound.SF2Layer;
import com.sun.media.sound.SF2Soundbank;


public class Sproc extends SignalProcessor{
	
	private static final int MYTHREADS = 4;
    private ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
    
    private FFT fft = new FFT();
    private Complex[] comp1;
    private Complex[] comp2;
    private Complex[] convolvedResult;
    //only want convolveMode static allocated once for each sproc.
	private boolean convolvedAlready = false;
	private Convolver convolver = new Convolver();
	private File FormantWav = null;
	private File FormantWav1 = null;
	private File FormantWav2 = null;
	private File FormantWav3 = null;
	private File FormantWav4 = null;
	private File ChorWav = null;
	private File RingModWav = null;

	Instrument[] instr;
	
	 /** 2*pi*(freq/samplerate) */
	//pick a frequency... say 440 Hz "A" - the note that orchestras tune to.
    private double fmod = 2*Math.PI*(44.0/44100.0);
    
	//sproc
	//message can be sent... when message is clicked...  [LOAD BANK (SOUNDFONT)] 
	// INBOX |[SELECT BANK OFFSET (catch bankdoesnotexistException)] [SELECT SOUND] [EDIT NOTE DATA] | OUTBOX                                                              | 
	
    public SourceDataLine getDataLine()
    {
    	return dataLine;
    }
    
	//create the itemBox here...
	private MidiChannel[] mc;
	
	private String[] bankNames = {};
	private String[] instrumentNames = {};
	private JFrame PianoRoll = null;
	
	//File WavSample = null;
	File WavSample = null;
	File ConvWavSample = null;
	
	public void setConvWavSample(File newFile)
	{
		ConvWavSample = newFile;
	}
	
	public File getFile()
	{
		return WavSample;
	}
	
	public static int fileNameMangle;
	private int fileNameMangleNumber;
	boolean fileChanged = false;
	
	public void setFile(File newFile)
	{
		fileChanged = true;
		WavSample = newFile;
		//initialize the chorus when this is set...
		//may need to be dirrefent ever time
		//10 cents
		try {
			//YES, only 44.1 Hz MONO is supported...
			//convert all tracks to MONO in audacity...
			//30 seems to be a good chorusey sound
			fileNameMangleNumber = fileNameMangle;
			
			initChorus(WavSample,"chorus" + fileNameMangle + ".wav",30);
			
			
			initFormant(WavSample,"formanta" + fileNameMangle + ".wav",0,1610);
			initFormant(WavSample,"formantb" + fileNameMangle + ".wav",0,2300);
			initFormant(WavSample,"formantc" + fileNameMangle + ".wav",0,2400);
			initFormant(WavSample,"formantd" + fileNameMangle + ".wav",0,640);
			initFormant(WavSample,"formante" + fileNameMangle + ".wav",0,595);
			
			initRingMod(WavSample,"fm" + fileNameMangle + ".wav",0);
			
			FormantWav = new File("formanta" + fileNameMangle + ".wav");
			FormantWav1 = new File("formantb" + fileNameMangle + ".wav");
			FormantWav2 = new File("formantc" + fileNameMangle + ".wav");
			FormantWav3 = new File("formantd" + fileNameMangle + ".wav");
			FormantWav4 = new File("formante" + fileNameMangle + ".wav");
			
			ChorWav = new File("chorus" + fileNameMangle + ".wav");
			RingModWav = new File("fm" + fileNameMangle + ".wav");
			
			fileNameMangle++;
			
		} catch (UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//then call playChorus()
		//playChorused();
		
	}
	
	public JFrame getPianoRoll()
	{
		return PianoRoll;
	}
	
	JPanel panel = new JPanel();
	
	//private Synthesizer synth1;
	//private static SoundFontLoader SFL = new SoundFontLoader();

	//private JComboBox bankList = new JComboBox(bankNames);
	private JComboBox InstrumentList;
	
	
/*
	public JComboBox getBankList()
	{
		//return bankList;
	}
*/

	public JComboBox getInstrumentList()
	{
		return InstrumentList;
	}

	MixingAudioInputStream mixer=null;

	private int currentInstrumentSelectedIndex = 0;
	
	//constructor
	public Sproc(JFrame frame, JPanel panel, int upperLeftX, int upperLeftY, HandlerClass h, SoundFontLoader sfl) throws MidiUnavailableException
	{	
		//
		//init midihandler once
		MidiHandler(sfl);
		//
		//set up the instuments
		instr = sfl.getSynth().getDefaultSoundbank().getInstruments();
	    //load a SoundFont...
	
		//transimmter and reciever midi messages

		mc = sfl.getSynth().getChannels();
		
		instrumentNames = sfl.getInstrumentArraynames();
		InstrumentList = new JComboBox(instrumentNames);
		
		id++;
		identifier = id;
		
		hand = h;
		
		//initialize rectangular arguments for Sproc
		
		uLeftX = upperLeftX;
		uLeftY = upperLeftY;
		
		//now, calculate offset of bottom right X and bottom right Y
		bRightX = uLeftX + 330;
		bRightY = uLeftY - 30;
		
		//initialize inbox
		//initialize outbox

		updateIB();
		updateOB();
		
		/*
		//InstrumentList = new JComboBox();
		bankList.setOpaque(true);
		//bankList.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		bankList.setPreferredSize(new Dimension(10,10));
		bankList.setMaximumSize(bankList.getPreferredSize());
		bankList.setSize(new Dimension(100,30));
		bankList.setLocation(uLeftX, uLeftY-10);
		*/
		
		//InstrumentList = new JComboBox();
		InstrumentList.setOpaque(true);
		//bankList.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		InstrumentList.setPreferredSize(new Dimension(10,10));
		InstrumentList.setMaximumSize(InstrumentList.getPreferredSize());
		InstrumentList.setSize(new Dimension(210,30));
		//InstrumentList.setLocation(uLeftX+1, uLeftY-9);
		InstrumentList.setLocation(uLeftX+1 - h.getHorizontalScrollBarOffset(), uLeftY - 9 - h.getVerticalScrollBarOffset());
		  
		frame.getLayeredPane().add(InstrumentList, new Integer (4));
		//for some reason, need to re-add the keylisteser with the combo box...
		//fixes the problem of objects not appearing...
		
		InstrumentList.getEditor().getEditorComponent().addKeyListener(frame.getKeyListeners()[0]);
		InstrumentList.addKeyListener(frame.getKeyListeners()[0]);
		InstrumentList.setVisible(true);
		//add the instrument list to the scoll pane too!
		//or somethign like it... it seems to be scrolling with the scroll pane...
		//maybe take the difference between it and the horizontal and vertical offsets to prevent that from happening?
		
		//offset ...

		//frame.getLayeredPane().add(bankList, new Integer (3));
		
		//Container contentPane = frame.getContentPane();
		//contentPane..add(c1, new Integer0); // 0 to display underneath 1, 0 = bottom (backmost in the Z-Buffer), 1= higher, 2 = higher than 1, etc....
		// to display on top of...
		
		//frame.getLayeredPane().add(c1);
		//must call validate to display component...
		//frame.validate();
		
		//bankList.setPrototypeDisplayValue("ItemWWW");
		//popup menu...?

		//frame.add(InstrumentList);
		hand.getCanvas().repaint();
		//add an action listener...
		InstrumentList.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				currentInstrumentSelectedIndex = InstrumentList.getSelectedIndex();
				System.out.println("current index " + currentInstrumentSelectedIndex);
	        	//synth1.loadInstrument(instr[currentInstrumentSelectedIndex]);
	        	
	        	//turn all notes off for current channel...
	        	mc[0].allNotesOff();
	        	//change the program to the currently selected bank...
	        	mc[0].programChange(currentInstrumentSelectedIndex);
	        	
			}
			
	    	}
	    );
	}
	
	@Override
	public void drawUpdate(int translateX, int translateY)
	{
		uLeftX = uLeftX + translateX;
		uLeftY = uLeftY + translateY;
		
		bRightX = uLeftX + 330;
		bRightY = uLeftY - 30;
		
		updateIB();
		updateOB();

		//bankList.setMaximumSize(bankList.getPreferredSize());
		//hand.getCanvas().repaint();
		//bankList.setVisible(false);
		//InstrumentList.setVisible(false);
	}
	
	@Override
	public boolean isASproc()
	{
		return true;
	}
	
	private boolean stopMode = false;
	private InputStream in;
	
	public boolean getStreamAvailable()
	{
		//try {
			//if(dataLine != null)
			{
				//System.out.println("isRunning : " + clip.isRunning());
				//System.out.println("isactive :" + clip.isActive());
				return dataLine.isActive(); // && clip.isRunning();
			}
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		//return false;
	}
	
	//private static Clip clip;
	
	//private InputStream waveStream;
    private final int EXTERNAL_BUFFER_SIZE = 9000;// 524288; // 128Kb
    private SourceDataLine dataLine;
    private SourceDataLine dataLineChor;
    private SourceDataLine dataLineFor;
    private SourceDataLine dataLineConv;
    private SourceDataLine dataLineConvResult;

    private SourceDataLine dataLineF1;
    private SourceDataLine dataLineF2;
    private SourceDataLine dataLineF3;
    private SourceDataLine dataLineF4;

	private AudioInputStream audioInputStream = null;
	private AudioInputStream audioInputStreamConv = null;
	private AudioInputStream audioInputStreamChor= null;
	

	private AudioInputStream audioInputStreamF1= null;
	private AudioInputStream audioInputStreamF2= null;
	private AudioInputStream audioInputStreamF3= null;
	private AudioInputStream audioInputStreamF4= null;
	
	private AudioFormat audioFormat= null;
	private AudioFormat audioFormatConv= null;
	private AudioFormat audioFormatChor= null;

	private AudioFormat audioFormatF1= null;
	private AudioFormat audioFormatF2= null;
	private AudioFormat audioFormatF3= null;
	private AudioFormat audioFormatF4= null;
	
	public void playSequence() {

		
		
		/*MAKE A CASE FOR FORMANT NOW!!!
		 * 
		 * 
		 * 
		 */
		
		//set with AABB BoundingBOX
		if(hand.getConvMode())
		{
			playConvSequence();
		}
		else
		{
			stopMode = true;
			
			hand.getCanvas().repaint();
			
			try {
				if(hand.getRingModMode())
				{
					audioInputStream = AudioSystem.getAudioInputStream(RingModWav);
				}
				else if(hand.getFormantMode())
				{
					audioInputStream = AudioSystem.getAudioInputStream(FormantWav);
					audioInputStreamF1 = AudioSystem.getAudioInputStream(FormantWav1);
					audioInputStreamF2 = AudioSystem.getAudioInputStream(FormantWav2);
					audioInputStreamF3 = AudioSystem.getAudioInputStream(FormantWav3);
					audioInputStreamF4 = AudioSystem.getAudioInputStream(FormantWav4);
					
					audioFormat = audioInputStream.getFormat();
					audioFormatF1 = audioInputStreamF1.getFormat();
					audioFormatF2 = audioInputStreamF2.getFormat();
					audioFormatF3 = audioInputStreamF3.getFormat();
					audioFormatF4 = audioInputStreamF4.getFormat();
					
					Info infoF1 = new Info(SourceDataLine.class, audioFormatF1);
					try {
					    dataLineF1 = (SourceDataLine) asF1.getLine(infoF1);
					    dataLineF1.open(audioFormatF1, EXTERNAL_BUFFER_SIZE);
					} catch (LineUnavailableException e1) {
					}
					dataLineF1.start();
					
					//initialize formant stuff here too...
				}
				else
				{
					audioInputStream = AudioSystem.getAudioInputStream(WavSample);
				}
			} catch (UnsupportedAudioFileException e1) {
			} catch (IOException e1) {
			}
		 
			// Obtain the information about the AudioInputStream
			audioFormat = audioInputStream.getFormat();
			Info info = new Info(SourceDataLine.class, audioFormat);
		 
			// opens the audio channel
			try {
			    dataLine = (SourceDataLine) as2.getLine(info);//(SourceDataLine) AudioSystem.getLine(info);
			    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
			} catch (LineUnavailableException e1) {
			}
		 
			// Starts the music :P
			dataLine.start();
		 
			int readBytes = 0; 
			
			/* Algorithm: Convert two wav files (byte arrays) into complex arrays. (byteToComplexArray(byte[]))
		     * then, call fft.convolve(Complex[],Complex[]) (or try cconv for circular convolution)
		     * then, convert that complex [] into a byte array.... ()
		     * then save it as a byte array as a private instance variable called "CONOLVED_SIGNAL"
		     */
	
			//long starttime = System.nanoTime();
			
			if(hand.getChorMode())
			{
				Runnable worker = new SoundSyncChor(this);
				executor.execute(worker);
			}
			
			
			byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
			
			try {
			    while (readBytes != -1) {
				readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
					if (readBytes > -1){
						if(hand.getFormantMode())
						{
							//use bitwise or... or add and divide by number of elments (5)
							//optimization, since bitwise or may only potentially look at the first bit,
							//it will optimize this operation when compared to division by a max factor
							//of halving the number of comaprisons
							
							//readBytes |= audioInputStreamF1.read(audioBuffer, 0,audioBuffer.length);
							//readBytes |= audioInputStreamF2.read(audioBuffer, 0,audioBuffer.length);
							//readBytes |= audioInputStreamF3.read(audioBuffer, 0,audioBuffer.length);
							//readBytes |= audioInputStreamF4.read(audioBuffer, 0,audioBuffer.length);
							readBytes += audioInputStreamF1.read(audioBuffer, 0,audioBuffer.length);
							readBytes += audioInputStreamF2.read(audioBuffer, 0,audioBuffer.length);
							readBytes += audioInputStreamF3.read(audioBuffer, 0,audioBuffer.length);
							readBytes += audioInputStreamF4.read(audioBuffer, 0,audioBuffer.length);
							
							//div by number of elements
							readBytes /= 5;
						}
						dataLine.write(audioBuffer, 0, readBytes);
					}
			    }
			} catch (IOException e1) {
			} finally {
			    // plays what's left and and closes the audioChannel
				//starttime = System.nanoTime();
				//this is what's causing the cpu hog...
			    //dataLine.drain();
	
				//stopSequencedWavFile();
				
				//much better alternative =)
				while(dataLine.available() != EXTERNAL_BUFFER_SIZE){}
				stopSequencedWavFile();
			    dataLine.close();
			}
	
			
			//trigger the next sounds in the file...
			//if(hand.getSequence())
	
			//System.out.println("elapsed time: " + (System.nanoTime() - starttime));
		}
	}

	File ConvResult = null;
	
	//before isntruction
	//B = 10, C =5
	//after instruction
	//A <- B || B <- C
	//A = 10, B = 5
	
	public void InitConvolved()
	{
		//try running it on 8 threads or so
		//input File, ImpulseFile, OutputFile, THREAD COUNT
		if(convolvedAlready == false && fileChanged == true)
		{
			ConvResult = new File("OUTPUT");
			convolver.convolve(WavSample, ConvWavSample, ConvResult,2);// Runtime.getRuntime().availableProcessors());
			//WavSample = ConvResult;
			convolvedAlready = true;
			fileChanged = false;
		}
		
		//now, init the file settings and play it...
		//playConvSequence();
	}
	
	public void playConvSequence() {

		stopMode = true;
		
		hand.getCanvas().repaint();
		
		try {
			if(ConvResult != null)
			{
				audioInputStream = AudioSystem.getAudioInputStream(ConvResult);
			}
			else
			{
				audioInputStream = AudioSystem.getAudioInputStream(WavSample);
			}
		} catch (UnsupportedAudioFileException e1) {
		} catch (IOException e1) {
		}
	 
		// Obtain the information about the AudioInputStream
		audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);
	 
		// opens the audio channel
		try {
		    dataLine = (SourceDataLine) as1.getLine(info);//AudioSystem.getLine(info);
		    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
		}
	 
		// Starts the music :P
		dataLine.start();
	 
		int readBytes = 0; 
		
		/* Algorithm: Convert two wav files (byte arrays) into complex arrays. (byteToComplexArray(byte[]))
	     * then, call fft.convolve(Complex[],Complex[]) (or try cconv for circular convolution)
	     * then, convert that complex [] into a byte array.... ()
	     * then save it as a byte array as a private instance variable called "CONOLVED_SIGNAL"
	     */

		//long starttime = System.nanoTime();
		
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		
		try {
		    while (readBytes != -1) {
			readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
				if (readBytes > -1){
					dataLine.write(audioBuffer, 0, readBytes);
				}
		    }
		} catch (IOException e1) {
		} finally {
			//much better alternative =)
			while(dataLine.available() != EXTERNAL_BUFFER_SIZE){}
			stopSequencedWavFile();
		    dataLine.close();
		}
	}

	//private byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
	//private AudioInputStream audioInputStream = null;
	
	public static double centToFactor(double cents){
		return 1 / Math.pow(Math.E,cents*Math.log(2)/1200/Math.log(Math.E)); 
	}
	private static double factorToCents(double factor){
		return 1200 * Math.log(1/factor) / Math.log(2); 
	}
	
	//maybe like 10 cents or so to start out...
	//renamed from startCli
	private void initChorus(File source,String target,double cents) throws UnsupportedAudioFileException, IOException{
		File inputFile = source;
		AudioFormat format = AudioSystem.getAudioFileFormat(inputFile).getFormat();	
		double sampleRate = 44100f; //= format.getSampleRate();
		double factor = centToFactor(cents);
		RateTransposer rateTransposer = new RateTransposer(factor);
		WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(factor, sampleRate));
		WaveformWriter writer = new WaveformWriter(format,target);
		AudioDispatcher dispatcher;
		//if(format.getChannels() != 1){
		//	dispatcher = AudioDispatcher.fromFile(inputFile,wsola.getInputBufferSize() * format.getChannels(),wsola.getOverlap() * format.getChannels());
		//	dispatcher.addAudioProcessor(new MultichannelToMono(format.getChannels(),true));
		//}else{
			dispatcher = AudioDispatcher.fromFile(inputFile,wsola.getInputBufferSize(),wsola.getOverlap());
		//}
		wsola.setDispatcher(dispatcher);
		dispatcher.addAudioProcessor(wsola);
		dispatcher.addAudioProcessor(rateTransposer);
		dispatcher.addAudioProcessor(writer);
		//chorusedAD = dispatcher;
		//run later when this is called...
		dispatcher.run();
	}
	
	private void initFormant(File source,String target,double cents, int formant) throws UnsupportedAudioFileException, IOException{
		File inputFile = source;
		AudioFormat format = AudioSystem.getAudioFileFormat(inputFile).getFormat();	
		double sampleRate = 44100f; //= format.getSampleRate();
		double factor = centToFactor(0);
		RateTransposer rateTransposer = new RateTransposer(factor);
		WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(factor, sampleRate));
		WaveformWriter writer = new WaveformWriter(format,target);
		AudioDispatcher dispatcher;
		//if(format.getChannels() != 1){
		//	dispatcher = AudioDispatcher.fromFile(inputFile,wsola.getInputBufferSize() * format.getChannels(),wsola.getOverlap() * format.getChannels());
		//	dispatcher.addAudioProcessor(new MultichannelToMono(format.getChannels(),true));
		//}else{
			dispatcher = AudioDispatcher.fromFile(inputFile,wsola.getInputBufferSize(),wsola.getOverlap());
		//}
		wsola.setDispatcher(dispatcher);
		
		//add the bandpass filters here...
		//formant, bandiwitdth, sample rate
		//vowel sounds - 5 formants - a e i o u - courtesy of wikipedia
		
		dispatcher.addAudioProcessor(new BandPass(formant, 100, 44100));
		//a
		//dispatcher.addAudioProcessor(new BandPass(1610, 100, 44100));
		//e
		//dispatcher.addAudioProcessor(new BandPass(2300 , 100, 44100));
		//i
		//dispatcher.addAudioProcessor(new BandPass(2400 , 100, 44100));
		//o
		//dispatcher.addAudioProcessor(new BandPass(640 , 100, 44100));
		//u
		//dispatcher.addAudioProcessor(new BandPass(595 , 100, 44100));
		
		//naw, do some cool jazz chord instead, something like -> 1 b3 5 b7 9 11 13
		
		
		//then, simply retrieve the autogenerated - name - mangled file when nescessary.
		
		
		dispatcher.addAudioProcessor(wsola);
		dispatcher.addAudioProcessor(rateTransposer);
		dispatcher.addAudioProcessor(writer);
		//chorusedAD = dispatcher;
		//run later when this is called...
		dispatcher.run();
	}	

	private void initRingMod(File source,String target,double cents) throws UnsupportedAudioFileException, IOException{
		File inputFile = source;
		AudioFormat format = AudioSystem.getAudioFileFormat(inputFile).getFormat();	
		double sampleRate = 44100f; //= format.getSampleRate();
		double factor = centToFactor(0);
		RateTransposer rateTransposer = new RateTransposer(factor);
		WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(factor, sampleRate));
		WaveformWriter writer = new WaveformWriter(format,target);
		AudioDispatcher dispatcher;
		//if(format.getChannels() != 1){
		//	dispatcher = AudioDispatcher.fromFile(inputFile,wsola.getInputBufferSize() * format.getChannels(),wsola.getOverlap() * format.getChannels());
		//	dispatcher.addAudioProcessor(new MultichannelToMono(format.getChannels(),true));
		//}else{
		
		//multiply by fmod here
			dispatcher = AudioDispatcher.fromFile(inputFile,(int) (wsola.getInputBufferSize()),(int) (wsola.getOverlap()));
		//}
		wsola.setDispatcher(dispatcher);
		
		//add the bandpass filters here...
		//formant, bandiwitdth, sample rate
		//vowel sounds - 5 formants - a e i o u - courtesy of wikipedia
		
		
		//then, simply retrieve the autogenerated - name - mangled file when nescessary.
		
		//multiply by fmod at some point.
		//sample rate, attack rate, release rate.
		
		//determine attack and release rate by peaks of an FFT
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream inp = new BufferedInputStream(new FileInputStream(WavSample));
		
		int read;
		byte[] buff = new byte[1024];
		while ((read = inp.read(buff)) > 0)
		{
		    out.write(buff, 0, read);
		}
		out.flush();
		byte[] audioBytes = out.toByteArray();
		
		double [] fftvalues = fft.calculateFFT(audioBytes);
		
		//find the peak of the array.
		
		int peak = 0;
		
		for(int i = 0; i < fftvalues.length; i++)
		{
			if(((int)fftvalues[i]) > peak)
			{
				peak = (int)fftvalues[i];
			}
		}
		
		//now print out and use the peak here...
		
		System.out.println(" peak " + peak);
		
		//now threshold it, attack time is based on the peak of the envelope
		
		double attackTime = peak * 0.00001;
		
		System.out.println(" attackTime " + attackTime);
		
		hand.getCanvas().setAttackTime(attackTime);
		
		EnvelopeFollower follower = new EnvelopeFollower(44100,attackTime,.0004);
		dispatcher.addAudioProcessor(follower);
		dispatcher.addAudioProcessor(writer);
		
		
		//try the nevelope follower
		
		//chorusedAD = dispatcher;
		//run later when this is called...
		dispatcher.run();
	}
	

	AudioSystem as1;
	AudioSystem as2;
	

	AudioSystem asF1;
	AudioSystem asF2;
	AudioSystem asF3;
	AudioSystem asF4;
	
	ArrayList<AudioInputStream> FormantList =new ArrayList<AudioInputStream>();
	
public void play() {
	
	//need to fix for convolved results...
	
	stopMode = true;
	
	hand.getCanvas().repaint();
	
	try {
		//logival ands when adding the other three effects
		if(hand.getFormantMode())
		{
			audioInputStream = AudioSystem.getAudioInputStream(FormantWav);
			audioInputStreamF1 = AudioSystem.getAudioInputStream(FormantWav1);
			audioInputStreamF2 = AudioSystem.getAudioInputStream(FormantWav2);
			audioInputStreamF3 = AudioSystem.getAudioInputStream(FormantWav3);
			audioInputStreamF4 = AudioSystem.getAudioInputStream(FormantWav4);
			
			audioFormat = audioInputStream.getFormat();
			audioFormatF1 = audioInputStreamF1.getFormat();
			audioFormatF2 = audioInputStreamF2.getFormat();
			audioFormatF3 = audioInputStreamF3.getFormat();
			audioFormatF4 = audioInputStreamF4.getFormat();
			
			Info infoF1 = new Info(SourceDataLine.class, audioFormatF1);
			try {
			    dataLineF1 = (SourceDataLine) asF1.getLine(infoF1);
			    dataLineF1.open(audioFormatF1, EXTERNAL_BUFFER_SIZE);
			} catch (LineUnavailableException e1) {
			}
			dataLineF1.start();
			
			//initialize formant stuff here too...
		}
		else if(hand.getConvMode())
		{
			//convolution mode
			audioInputStream = AudioSystem.getAudioInputStream(ConvResult);	
		}
		else if(hand.getRingModMode())
		{
			audioInputStream = AudioSystem.getAudioInputStream(RingModWav);
		}
		else
		{
			//normal mode (all off...)
			audioInputStream = AudioSystem.getAudioInputStream(WavSample);
		}
		//add other three modes here...
	} catch (UnsupportedAudioFileException e1) {
	} catch (IOException e1) {
	}
 
	// Obtain the information about the AudioInputStream
	audioFormat = audioInputStream.getFormat();
	Info info = new Info(SourceDataLine.class, audioFormat);
 
	// opens the audio channel
	try {
	    dataLine = (SourceDataLine) as1.getLine(info);//AudioSystem.getLine(info);
	    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
	} catch (LineUnavailableException e1) {
	}
 
	// Starts the music :P
	dataLine.start();
	

	//now, start the chorused line if chorusedMode is on 
	//ADD IF STATEMENT LATER WITH AABB on RACK
	
	int readBytes = 0;
	byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
	byte[] audioBufferRing = new byte[EXTERNAL_BUFFER_SIZE*2];
	
	//int waveform type, float fSignalfreq (get from FFT analysis), float fAmplitude, AudioFormat audioFormat, long lLength (AudioFormat.getlength or soemthing)
	
	//amplitude.. try different values...
	int readBytesOsc = 0;
	boolean secondsPassed = false; 
	//ring mod mode....
	
	if(hand.getChorMode())
	{
		Runnable worker = new SoundSyncChor(this);
		executor.execute(worker);
	}
	
/*
	if(hand.getFormantMode())
	{
		Runnable worker = new SoundSyncFormant(this,FormantWav1);
		Runnable worker2 = new SoundSyncFormant(this,FormantWav2);
		Runnable worker3 = new SoundSyncFormant(this,FormantWav3);
		Runnable worker4 = new SoundSyncFormant(this,FormantWav4);
		executor.execute(worker);
		executor.execute(worker2);
		executor.execute(worker3);
		executor.execute(worker4);
	}
*/
	//Oscillator osc = new Oscillator(0, 440, 0.5f, audioFormat, audioInputStream.getFrameLength());
	//Oscillator osc2 = new Oscillator(0, 2500, 0.5f, audioFormat, WavSample.length()/4);
	{		
	try {
		
	    while (readBytes != -1) {
	    	
    	//readBytes = audioInputStream.read(audioBuffer, 0,audioBuffer.length);
    	readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
		
		if(hand.getFormantMode())
		{
			//use bitwise or... or add and divide by number of elments (5)
			//optimization, since bitwise or may only potentially look at the first bit,
			//it will optimize this operation when compared to division by a max factor
			//of halving the number of comaprisons
			
			//readBytes |= audioInputStreamF1.read(audioBuffer, 0,audioBuffer.length);
			//readBytes |= audioInputStreamF2.read(audioBuffer, 0,audioBuffer.length);
			//readBytes |= audioInputStreamF3.read(audioBuffer, 0,audioBuffer.length);
			//readBytes |= audioInputStreamF4.read(audioBuffer, 0,audioBuffer.length);
			readBytes += audioInputStreamF1.read(audioBuffer, 0,audioBuffer.length);
			readBytes += audioInputStreamF2.read(audioBuffer, 0,audioBuffer.length);
			readBytes += audioInputStreamF3.read(audioBuffer, 0,audioBuffer.length);
			readBytes += audioInputStreamF4.read(audioBuffer, 0,audioBuffer.length);
			
			//div by number of elements
			readBytes /= 5;
		}
		/*else if(secondsPassed)
		{
			//readBytes += audioInputStreamChor.read(audioBufferChor, 0,audioBufferChor.length);
			//readBytes /= (byte)2;
		}*/
    	
		//now, if Ring Mod mode is on, then multiply readbytes * fmod
		/*if(hand.getRingModMode())
		{
			//maybe bitwise or-ring it with the frequency modulation (ring mod) will
			//be equiavalent to multiplying the input signal to the fm factor... I'll have to 
			//test later.
			
			//multiplying does not workkk... find another way...
			
			readBytes |= (int)fmod;
		}*/
		if (dataLine != null && readBytes >= 0){
		    //dataLine.write(audioBuffer, 0, readBytes);
		    dataLine.write(audioBuffer, 0, readBytes);
			//simultaneously write the chorus I suppose

			/*if(hand.getFormantMode())
			{
				//playFormant(audioBufferF1,audioBufferF2,audioBufferF3,audioBufferF4);
			}*/
		    /*if(hand.getChorMode())
		    {
				if (dataLineChor != null && readBytesChor >= 0)
					{
						//simultaneously write I suppose
				    	//run in concurrent thread...
					    dataLineChor.write(audioBufferChor, 0, readBytesChor);
					}
		       //Runnable worker = new SoundSyncChor(this,audioBuffer);
		       //executor.execute(worker);
			   //playChorus(audioBufferChor);
		    }*/
		    /*
		    else
		    {
		    	if((System.nanoTime() - chorusDelay) > 10)
		    	{
			    	//call playChorus here
			    	//run in a separate thread...?
		    		//no, run in here I suppose... simultaneously?
			    	if(hand.getChorMode())
			    	{
			    		//Runnable worker = new SoundSyncChor(this);
			    		//executor.execute(worker);
			    		secondsPassed = true;
			    	}
		    	}
		    }*/
		}
	}
	} catch (IOException e1) {
	} finally {
	    // plays what's left and and closes the audioChannel
			
			while(dataLine != null && dataLine.available() != EXTERNAL_BUFFER_SIZE)
			{				
			}
			
			if(dataLine != null)
			{
				dataLine.close();
				dataLine.drain();
				if(hand.getChorMode())
				{
					dataLineChor.close();
					dataLineChor.drain();
				}
				else if(hand.getFormantMode())
				{
					dataLineF1.close();
					dataLineF1.drain();
				}
			}
		}
		stopMode = false;
	}
	
	//trigger the next sounds in the file...
	//if(hand.getSequence())
	}

public void playChorus()
{
	//if called from stop play again...
	
	//start chorus stuff
		//.1 second... = 100000000 ns
		//now, begin playback of the chorused line
		try {
				//get this instnaces's name - mangle filepath.
				//File file2 = new File("C:/Users/Shamblen/workspace/Signal_Processor_172/" + "chorus" + fileNameMangleNumber + ".wav");
				audioInputStreamChor = AudioSystem.getAudioInputStream(ChorWav);
				//yes, this works
		} catch (UnsupportedAudioFileException e1) {
		} catch (IOException e1) {
		}
	 
		// Obtain the information about the AudioInputStream
		audioFormatChor = audioInputStreamChor.getFormat();
		Info infoChor = new Info(SourceDataLine.class, audioFormatChor);
	 
		// opens the audio channel
		try {
		    dataLineChor = (SourceDataLine) as2.getLine(infoChor);//(SourceDataLine) AudioSystem.getLine(infoChor);
		    dataLineChor.open(audioFormatChor, EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
		}
		dataLineChor.start();

		//end chorus stuff

		int readBytesChor = 0;
		byte[] audioBufferChor = new byte[EXTERNAL_BUFFER_SIZE];
		
			//stop playback of chorused line
			try {

			    while (readBytesChor != -1) {
			    	
					readBytesChor = audioInputStreamChor.read(audioBufferChor, 0,audioBufferChor.length);
					if (dataLineChor != null && readBytesChor >= 0)
						{
							//simultaneously write I suppose
					    	//run in concurrent thread...
						    dataLineChor.write(audioBufferChor, 0, readBytesChor);
						}
			    }
			    } catch (IOException e1) {}
			 finally {
			    // plays what's left and and closes the audioChannel
					
					while(dataLineChor != null && dataLineChor.available() != EXTERNAL_BUFFER_SIZE)
					{				
					}
					dataLineChor.close();
					dataLineChor.drain();
			}

			    //time stretch * 4 -> 1024 FFT, overlap of 8, 128 new samples per frame.
			    //FFT - > cartesian to polar, then multiply by the strech factor, in this casse is 4.
			    //then switch from polar to cartesian, then take the iFFT
}


//class area
private SourceDataLine DLF = null;
private AudioInputStream audioInputStreamFor = null;
private AudioFormat audioFormatFor = null;

public void playFormantParallel(File wavFile)
{
	//start chorus stuff
		//.1 second... = 100000000 ns
		//now, begin playback of the chorused line
		try {
				//get this instnaces's name - mangle filepath.
				//File file2 = new File("C:/Users/Shamblen/workspace/Signal_Processor_172/" + "chorus" + fileNameMangleNumber + ".wav");
			audioInputStreamFor = AudioSystem.getAudioInputStream(wavFile);
				//yes, this works
		} catch (UnsupportedAudioFileException e1) {
		} catch (IOException e1) {
		}
	 
		// Obtain the information about the AudioInputStream
		audioFormatFor = audioInputStreamFor.getFormat();
		Info infoFor = new Info(SourceDataLine.class, audioFormatFor);
	 
		// opens the audio channel
		try {
			DLF = (SourceDataLine) as2.getLine(infoFor);//(SourceDataLine) AudioSystem.getLine(infoChor);
			DLF.open(audioFormatFor, EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
		}
	 
		// Starts the music :P

		DLF.start();

		int readBytesFor = 0;
		byte[] audioBufferFor = new byte[EXTERNAL_BUFFER_SIZE];
		
			//stop playback of chorused line
			try {

			    while (readBytesFor != -1) {
			    	readBytesFor = audioInputStreamFor.read(audioBufferFor, 0,audioBufferFor.length);
					if (dataLineFor != null && readBytesFor >= 0)
						{
							//simultaneously write I suppose
					    	//run in concurrent thread...
						    dataLineFor.write(audioBufferFor, 0, readBytesFor);
						}
			    }
			    } catch (IOException e1) {}

			    //time stretch * 4 -> 1024 FFT, overlap of 8, 128 new samples per frame.
			    //FFT - > cartesian to polar, then multiply by the strech factor, in this casse is 4.
			    //then switch from polar to cartesian, then take the iFFT
}


public void playFormant(byte [] audioBufferF1,byte [] audioBufferF2,byte [] audioBufferF3,byte [] audioBufferF4)
{
	
	
			//stop playback of chorused line
		int readBytesF1 =0 ;
			try {
				readBytesF1 += audioInputStreamF1.read(audioBufferF1, 0,audioBufferF1.length);
				readBytesF1 += audioInputStreamF2.read(audioBufferF2, 0,audioBufferF2.length);
				readBytesF1 += audioInputStreamF3.read(audioBufferF3, 0,audioBufferF3.length);
				readBytesF1 += audioInputStreamF4.read(audioBufferF4, 0,audioBufferF4.length);
				readBytesF1 /= 4;
				
					if (readBytesF1 >= 0)
					{
						dataLineF1.write(audioBufferF1, 0, readBytesF1);
					}
					
			    } catch (IOException e1) {}

			    //time stretch * 4 -> 1024 FFT, overlap of 8, 128 new samples per frame.
			    //FFT - > cartesian to polar, then multiply by the strech factor, in this casse is 4.
			    //then switch from polar to cartesian, then take the iFFT
}



public void stopWavFile()
{
	if(stopMode)
	{
		if(dataLine != null) {dataLine.close();}
		stopMode = false;
		if(dataLineChor != null) {dataLineChor.close();}
		dataLineChor = null;
		executor = Executors.newFixedThreadPool(MYTHREADS);
	}
}

public void stop()
{
		if(dataLine != null) {dataLine.close();}
		dataLine = null;
		if(dataLineChor != null) {dataLineChor.close();}
		dataLineChor = null;
		stopMode = false;
		executor = Executors.newFixedThreadPool(MYTHREADS);
}
	
	public void stopSequencedWavFile()
	{
			//create a new runnable class
		
			for(int i = 0; i < input_Pair_2.size(); i++)
			{
				//if(!((Sproc) input_Pair_2.get(i)).getStreamAvailable())
				//{
					 Runnable worker = new SoundSync((Sproc)(input_Pair_2.get(i)));
					 executor.execute(worker);
				//}
			}

			stopMode = false;
			//executor.shutdown();

	}
	
	public void setStopMode(boolean newStopMode)
	{
		stopMode = newStopMode;
	}
	
	public boolean getStopMode()
	{
		return stopMode;
	}
	
	private AudioInputStream audio;
	
	public AudioInputStream getAudio()
	{
		return audio;
	}
	
	public AudioInputStream getAudioCopy()
	{
		return audio = new AudioInputStream( (TargetDataLine) WavSample );
	}

	public void setPianoRoll(PianoRoll pr) {
		PianoRoll = pr;
	}
	
		private String fileName = "";
		
	public String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	
	public void setFileName(String newFileName) {
		// TODO Auto-generated method stub
		fileName = newFileName;
	}
	


    private ArrayList<String> MIDIDeviceNames = new  ArrayList<String>();
   
    public void MidiHandler(SoundFontLoader sfl)
    {
    	//turn into private class area var
    	MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        
        boolean MIDIDeviceIsOpen = false;
        
        for(int k = 0; k < infos.length; k++)
        {
	        for(int j = 0; j < MIDIDeviceNames.size(); j++)
	        {
	        	try {
					if( MidiSystem.getMidiDevice(infos[k]).getDeviceInfo().toString().compareTo(MIDIDeviceNames.get(j)) == 0)
					{
						MIDIDeviceIsOpen = true;
						break;
					}
				} catch (MidiUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        
        }
	        
	        for (int i = 0; i < infos.length; i++) {
	            try {
	            device = MidiSystem.getMidiDevice(infos[i]);
	        	MIDIDeviceNames.add(device.getDeviceInfo().toString());
	            //does the device have any transmitters?
	            //if it does, add it to the device list
	            System.out.println(infos[i]);
	
	            //get all transmitters
	            List<Transmitter> transmitters = device.getTransmitters();
	            //and for each transmitter
	
	            for(int j = 0; j<transmitters.size();j++) {
	                //create a new receiver
	                transmitters.get(j).setReceiver(sfl.getSynth().getReceiver());// new MidiInputReceiver(device.getDeviceInfo().toString()) );
	                sfl.getSynth().getTransmitter().setReceiver(sfl.getSynth().getReceiver());//new MidiInputReceiver(device.getDeviceInfo().toString()));
	            	
	                        //using my own MidiInputReceiver
	            }
	            //MidiInputReceiver MIR = new MidiInputReceiver(device.getDeviceInfo().toString());
	            Transmitter trans = device.getTransmitter();
	            trans.setReceiver(sfl.getSynth().getReceiver());
	            //sfl.getSynth().getReceiver()..setReceiver(MIR);
	        	
	          
	            //open each device

	            if(MIDIDeviceIsOpen == false)
	            {
	            	device.open();
	            }
	            //if code gets this far without throwing an exception
	            //print a success message
	            //System.out.println(device.getDeviceInfo()+" Was Opened");
	            }
	        
		    
	        catch (MidiUnavailableException e) {}
	     
	        }   
        }
    
    //cycyle thought the midi channels with a static variable that is incremented after verey not is played...
    
    private class MidiInputReceiver implements Receiver {
        public String name;
        public MidiInputReceiver(String name) 
        {
            this.name = name;
        }
        public void send(MidiMessage msg, long timeStamp) 
        {
            System.out.println("midi received");	
        	//send midi message, maybe record midi sequences with this thing...
        	//mc[0].
        }
        public void close() {}
    }
    
    
}