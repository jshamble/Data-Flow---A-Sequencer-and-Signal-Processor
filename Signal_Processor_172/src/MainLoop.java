import java.awt.Color;



import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioPlayer;
import be.hogent.tarsos.dsp.effects.DelayEffect;
import be.hogent.tarsos.dsp.filters.BandPass;
import be.hogent.tarsos.dsp.filters.LowPassFS;
import be.hogent.tarsos.dsp.synthesis.AmplitudeLFO;
import be.hogent.tarsos.dsp.synthesis.NoiseGenerator;
import be.hogent.tarsos.dsp.synthesis.SineGenerator;



import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;

import com.sun.media.sound.SF2Soundbank;

public class MainLoop extends JPanel implements Scrollable{

	static JFrame frame = new JFrame("Data Flow");
	
	private static HandlerClass handler = new HandlerClass(frame);

	private static Canvas can = new Canvas(handler,frame);
	
	private static JScrollPane Vpane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private static JScrollPane Hpane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
	 //create JPanel
	private static JPanel pnl = new JPanel(new BorderLayout());

	private static JFileChooser fileChooser = new JFileChooser();

	private static JLayeredPane layeredPane = new JLayeredPane();
	/**
	 * @param args
	 */

	//private static JScrollPane scrollPane = new JScrollPane(can);
	
	public static void addMenus()
	{
		
		JMenuBar menubar = new JMenuBar();
        ImageIcon exitIcon = new ImageIcon("exit.png");
        ImageIcon loadIcon = new ImageIcon("./res/file.png");
        ImageIcon saveIcon = new ImageIcon("./res/save.png");

        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        
        JMenuItem loadMenuItem = new JMenuItem("Open", loadIcon);
        loadMenuItem.setMnemonic(KeyEvent.VK_L);
        loadMenuItem.setToolTipText("Open a File");
        loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	//add object saving code here...
            	//need a getter for all objects
            	//that is invoked here..., to load all objects
            	
            	 int returnVal = fileChooser.showOpenDialog(frame);
            	    if (returnVal == JFileChooser.APPROVE_OPTION) {
            	        File file = fileChooser.getSelectedFile();
            	        try {
            	          // What to do with the file, load the data
        					try {
								handler.getObjectList().loadData(frame, pnl, handler, file,sfl);
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (MidiUnavailableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
            	        } catch (IOException ex) {
            	          System.out.println("problem accessing file"+file.getAbsolutePath());
            	        }
            	    } else {
            	        System.out.println("File access cancelled by user.");
            	    }
            }
        });
        
        JMenuItem saveMenuItem = new JMenuItem("Save", saveIcon);
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setToolTipText("Save a File");
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	//add object saving code here...
            	//need a getter for all objects
            	//that is invoked here..., to save all objects
            	
            	
            	try {
					handler.getObjectList().saveAsData(0,"");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            	//this method can also be called with the schortcut Ctrl + s;
            }
        });
        
        JMenuItem saveAsMenuItem = new JMenuItem("Save As", saveIcon);
        saveAsMenuItem.setMnemonic(KeyEvent.VK_A);
        saveAsMenuItem.setToolTipText("Save a File with a filename in a directory.");
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	//add object saving code here...
            	//need a getter for all objects
            	//that is invoked here..., to save all objects
            	

        		int returnVal = fileChooser.showSaveDialog(frame);
        	    if (returnVal == JFileChooser.APPROVE_OPTION) 
        	    {
        	    	String filepath = fileChooser.getSelectedFile().getAbsolutePath();
	            	try {
						handler.getObjectList().saveAsData(1,filepath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	
        	    }
            	
            	//this method can also be called with the schortcut Ctrl + s;
            }
        });
        

        JMenuItem exitMenuItem = new JMenuItem("Exit", exitIcon);
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setToolTipText("Exit application");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        
        
        file.add(exitMenuItem);
        file.add(loadMenuItem);
        file.add(saveMenuItem);
        file.add(saveAsMenuItem);
        
        menubar.add(file);
        
        //frame.setJMenuBar(menubar);

        menubar.setOpaque(true);
        menubar.setVisible(true);
        
        frame.setJMenuBar(menubar);
	}
	
	public static void init()
	{
		//JPopupMenu.setDefaultLightWeightPopupEnabled( false );
		
		addMenus();
		
		handler.setSFL(sfl);
		handler.setPanel(pnl);
		handler.setCanvas(can);
		handler.setHandlerClass(handler);
		
		//null layout manager - preferred size is null...
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		frame.addMouseListener(handler);
		frame.addMouseMotionListener(handler);
		frame.addKeyListener(handler);

		//add this to mane the menus visible.
		
		//sets up textarea frame
		//scrollPane.add
		//scrollPane.setPreferredSize(new Dimension(450, 110));
		
		/*
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);  
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  
		frame.setContentPane(scrollPane);
		*/

		/*
		Vpane.getViewport().setOpaque(false);
		Hpane.getViewport().setOpaque(false);
		frame.add(Vpane,BorderLayout.EAST);
		frame.add(Hpane,BorderLayout.SOUTH);
		*/
        //scrollbar will always be above the canvas....

		frame.setVisible(true);
		//frame.getLayeredPane().validate();

	}

	static SoundFontLoader sfl = new SoundFontLoader();
	
	public static void main(String[] args) {
		
		//first, initial settings
		init();
		
		try {
			sfl.loadSoundFont();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("loading instruments ...");
		try{
		SF2Soundbank sf2 = new SF2Soundbank(sfl.getSoundFileName());
		sfl.getSynth().loadAllInstruments(sf2);
		sfl.setInstrumentArray(sf2.getInstruments());
		try {

			System.out.println("done loading instruments");

			System.out.println("opening Synthesizer ...");
			sfl.getSynth().open();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} catch (IOException e) {System.out.println(e.getMessage());}

		System.out.println("done opening Synthesizer.");

		
		
		
		/*
		*      _______                       _____   _____ _____  
		*     |__   __|                     |  __ \ / ____|  __ \ 
		*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
		*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
		*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
		*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
		*                                                         
		* -----------------------------------------------------------
		*
		*  TarsosDSP is developed by Joren Six at 
		*  The School of Arts,
		*  University College Ghent,
		*  Hoogpoort 64, 9000 Ghent - Belgium
		*  
		* -----------------------------------------------------------
		*
		*  Info: http://tarsos.0110.be/tag/TarsosDSP
		*  Github: https://github.com/JorenSix/TarsosDSP
		*  Releases: http://tarsos.0110.be/releases/TarsosDSP/
		*  
		*  TarsosDSP includes modified source code by various authors,
		*  for credits and info, see README.
		* 
		*/


		/**
		 * Shows how a synthesizer can be constructed using some simple ugen blocks.
		 * @author Joren Six
		 */
		
				AudioDispatcher dispatcher = new AudioDispatcher(1024);
				
				//dispatcher.fromFile(audioFile, size, 0);
				
				 /**
				   * Constructs a band pass filter with the requested center frequency,
				   * bandwidth and sample rate.
				   * 
				   * @param freq
				   *          the center frequency of the band to pass (in Hz)
				   * @param bandWidth
				   *          the width of the band to pass (in Hz)
				   * @param sampleRate
				   *          the sample rate of audio that will be filtered by this filter
				   */
				//center freq = formant (Choose some cool jazz chord or vowel sounds)
				//bandWidth in Hz... not sure about this one. Probably around 500 to 1000 Hz to be safe. (100 Hz on the website say it's good)
				//sampleRate = 44100 hz always
				/*
				dispatcher.addAudioProcessor(new BandPass(1000,100,44100));
				dispatcher.addAudioProcessor(new NoiseGenerator(0.2));
				dispatcher.addAudioProcessor(new LowPassFS(1000,44100));
				dispatcher.addAudioProcessor(new LowPassFS(1000,44100));
				dispatcher.addAudioProcessor(new LowPassFS(1000,44100));
				dispatcher.addAudioProcessor(new SineGenerator(0.05,220));
				dispatcher.addAudioProcessor(new AmplitudeLFO(10,0.9));
				dispatcher.addAudioProcessor(new SineGenerator(0.2,440));
				dispatcher.addAudioProcessor(new SineGenerator(0.1,880));
				dispatcher.addAudioProcessor(new DelayEffect(1.5, 0.4, 44100));
				dispatcher.addAudioProcessor(new AmplitudeLFO());
				dispatcher.addAudioProcessor(new SineGenerator(0.05,1760));
				dispatcher.addAudioProcessor(new SineGenerator(0.01,2460));
				dispatcher.addAudioProcessor(new DelayEffect(0.757, 0.4, 44100));
				try {
					dispatcher.addAudioProcessor(new AudioPlayer( new AudioFormat(44100, 16, 1, true, false)));
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dispatcher.run();
		*/
		
		
	}
	
	//down here we find the implemented Scrollable methods

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		// TODO Auto-generated method stub
		return false;
	}	
	

}

