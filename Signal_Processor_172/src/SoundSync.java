import java.io.IOException;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class SoundSync implements Runnable{

	private Sproc sprocc;
	private Canvas can;
	
	public SoundSync(Sproc sproc) {
		// TODO Auto-generated constructor stub
		sprocc = sproc;
	}
	
	public void setSproc(Sproc sproc)
	{
		sprocc = sproc;
	}
	
	public void setCanvas(Canvas canv)
	{
		can = canv;
	}

	@Override
	public void run() {
		sprocc.playSequence();
	}

}
