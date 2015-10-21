package org.lunarproject.gw2route;

/**
 * Main.java executes the program.
 */
import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import javax.swing.SwingUtilities;

public class Main {
	
	public static void main(String args[])
	{
		NativeInterface.open();
		UIUtils.setPreferredLookAndFeel(); // Set to OS' UI look
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Navi navi = new Navi();
				}
				catch (InterruptedException ex)
				{
					
				}
			}
		});
		NativeInterface.runEventPump();
		
	}
}
