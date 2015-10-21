package org.lunarproject.gw2route;

/**
 * GPS.java reads Guild Wars 2 memory-mapped file using the Mumble Link API and
 * updates the website's JavaScript variables. http://wiki.mumble.info/wiki/Link
 * This file's code originally written by Lulan.8497
 * Source: https://forum-en.guildwars2.com/forum/community/api/Mumble-Link-for-Java-using-JNA
 */

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import org.cef.browser.CefBrowser;

public class GPS implements Runnable {
	
	private static final int MEM_MAP_SIZE = 5460;
	private static final String MEM_MAP_NAME = "MumbleLink";
	private final HANDLE sharedFile;
	private final Pointer sharedMemory;
	
	public boolean wantLoop = true;
	public int uiVersion = 0;
	public int uiTick = 0;
	public float[] fAvatarPosition = new float[0];
	public float[] fAvatarFront = new float[0];
	public float[] fAvatarTop = new float[0];
	public float[] fCameraPosition = new float[0];
	public float[] fCameraFront = new float[0];
	public float[] fCameraTop = new float[0];
	public char[] identity = new char[0];
	public char[] gameName = new char[0];
	public int context_len = 0;
	public byte[] context = new byte[0];
	
	public CefBrowser TheBrowser;
	public Option TheOptions;

	public GPS(CefBrowser pBrowser, Option pOptions)
	{
		this.sharedFile = Kernel32.INSTANCE.CreateFileMapping(
				WinBase.INVALID_HANDLE_VALUE, null, WinNT.PAGE_EXECUTE_READWRITE, 0, MEM_MAP_SIZE, MEM_MAP_NAME);
		this.sharedMemory = Kernel32.INSTANCE.MapViewOfFile(
				this.sharedFile, WinNT.SECTION_MAP_READ, 0, 0, MEM_MAP_SIZE);
		
		this.TheBrowser = pBrowser;
		this.TheOptions = pOptions;
	}

	@Override
	public void run()
	{
		try
		{
			while (wantLoop && this.sharedMemory != null)
			{
				fAvatarPosition = this.sharedMemory.getFloatArray(8, 3);
				fAvatarFront = this.sharedMemory.getFloatArray(20, 3);
				fCameraFront = this.sharedMemory.getFloatArray(568, 3);
				identity = this.sharedMemory.getCharArray(592, 256);
				final String indenitystr = sanitizeIdentity(new String(identity)).trim();
				
				NativeInterface.open();
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						if (Navi.verifySite(TheBrowser, TheOptions))
						{
							// Tell the website to update its global variables
							String js = TheOptions.JS_GPS_POSITION + Arrays.toString(fAvatarPosition) + ";"
								+ TheOptions.JS_GPS_DIRECTION + Arrays.toString(fAvatarFront) + ";"
								+ TheOptions.JS_GPS_CAMERA + Arrays.toString(fCameraFront) + ";"
								+ TheOptions.JS_GPS_IDENTITY + indenitystr + ";";
							TheBrowser.executeJavaScript(js, TheBrowser.getURL(), 0);
						}
					}
				});

				// Original code with all API variables
				/*
				uiVersion = this.sharedMemory.getInt(0);
				uiTick = this.sharedMemory.getInt(4);
				fAvatarPosition = this.sharedMemory.getFloatArray(8, 3);
				fAvatarFront = this.sharedMemory.getFloatArray(20, 3);
				fAvatarTop = this.sharedMemory.getFloatArray(32, 3);
				gameName = this.sharedMemory.getCharArray(44, 256);
				fCameraPosition = this.sharedMemory.getFloatArray(556, 3);
				fCameraFront = this.sharedMemory.getFloatArray(568, 3);
				fCameraTop = this.sharedMemory.getFloatArray(580, 3);
				identity = this.sharedMemory.getCharArray(592, 256);
				context_len = this.sharedMemory.getInt(1104);
				context = this.sharedMemory.getByteArray(1108, 256);
				System.out.println("uiVersion: " + uiVersion);
				System.out.println("uiTick: " + uiTick);
				System.out.println("fAvatarPosition: " + Arrays.toString(fAvatarPosition));
				System.out.println("fAvatarFront: " + Arrays.toString(fAvatarFront));
				System.out.println("fAvatarTop: " + Arrays.toString(fAvatarTop));
				System.out.println("gameName: " + (new String(gameName)).trim());
				System.out.println("fCameraPosition: " + Arrays.toString(fCameraPosition));
				System.out.println("fCameraFront: " + Arrays.toString(fCameraFront));
				System.out.println("fCameraTop: " + Arrays.toString(fCameraTop));
				System.out.println("identity: " + (new String(identity)).trim());
				System.out.println("context_len: " + context_len);
				System.out.println("context: " + Arrays.toString(context));
				System.out.println("#####################################################");
				*/

				Thread.sleep(TheOptions.GPS_REFRESH_RATE);
			}
		}
		catch (InterruptedException ex)
		{
			System.out.println("Thread sleep error.");
        }
	}
	
	/**
	 * Retrieved JSON may sometimes be untrimmed, extracts only the outermost {} part.
	 * @param pString JSON to sanitize.
	 * @return sanitized JSON string.
	 */
	public String sanitizeIdentity(String pString)
	{
		String s = pString;
		int bgn = s.indexOf("{");
		int end = s.indexOf("}");
		if (bgn == -1 || end == -1)
		{
			return "null";
		}
		return s.substring(bgn, end+1);
	}
	
	// Calling this before terminating the program will prevent an access violation.
	public void kill()
	{
		wantLoop = false;
	}
}