package org.lunarproject.gw2route;

/**
 * Option.java serves as a mediator for the options text file and the program's
 * actual variables; it contains the exact string keys as in the text file and
 * is responsible to converting the values to the proper type.
 */

import java.awt.Dimension;
import org.ini4j.Ini;


public class Option {
	
	public Ini File;
	Ini.Section urls;
	Ini.Section javascript;
	Ini.Section preferences;
	Ini.Section componentconstants;
	Ini.Section standarddimensions;
	Ini.Section windowpresets;
	Ini.Section colorpresets;
	
	String URL_HOMEPAGE;
	String URL_LASTVISITED;
	String URL_UPDATE;
	String URL_SITE;
	String URL_LOCAL;
	
	String JS_QUICK_1;
	String JS_QUICK_2;
	String JS_QUICK_3;
	String JS_QUICK_4;
	String JS_QUICK_A;
	String JS_QUICK_B;
	String JS_QUICK_TOGGLE;
	
	String JS_GPS_POSITION;
	String JS_GPS_DIRECTION;
	String JS_GPS_CAMERA;
	String JS_GPS_IDENTITY;
	
	String LANGUAGE;
	float OPACITY_FOCUSED;
	float OPACITY_UNFOCUSED;
	boolean wantOpacityOnFocus;
	boolean wantAlwaysOnTop;
	boolean wantNavbar;
	boolean wantLastVisited;
	int BORDER_THICKNESS;
	boolean wantGPS;
	int GPS_REFRESH_RATE;
	
	WindowPreset WINDOWPRESET_START;
	WindowPreset[] WINDOWPRESET_USER = new WindowPreset[8];
	ColorPreset COLORPRESET_START;
	ColorPreset[] COLORPRESET_USER = new ColorPreset[4];
	String[] JS_SIZE_USER = new String[8];
	
	int MENUBAR_HEIGHT;
	int NAVBAR_THICKNESS;
	int NAVBAR_HEIGHT;
	
	Dimension FRAME_MINIMUM;
	Dimension FRAME_QUICK_1;
	Dimension FRAME_QUICK_2;
	Dimension FRAME_QUICK_3;
	Dimension FRAME_QUICK_4;
	Dimension FRAME_QUICK_A;
	Dimension FRAME_QUICK_B;
	
	
	public Option(Ini pIni)
	{
		this.File = pIni;
		// INI sections
		urls = File.get("URLs");
		javascript = File.get("JavaScript");
		preferences = File.get("Preferences");
		componentconstants = File.get("ComponentConstants");
		standarddimensions = File.get("StandardDimensions");
		windowpresets = File.get("WindowPresets");
		colorpresets = File.get("ColorPresets");
		
		// INI properties, same order as in text file
		URL_HOMEPAGE = urls.get("URL_HOMEPAGE");
		URL_LASTVISITED = urls.get("URL_LASTVISITED");
		URL_UPDATE = urls.get("URL_UPDATE");
		URL_SITE = urls.get("URL_SITE");
		URL_LOCAL = urls.get("URL_LOCAL");
		
		JS_QUICK_1 = javascript.get("JS_QUICK_1");
		JS_QUICK_2 = javascript.get("JS_QUICK_2");
		JS_QUICK_3 = javascript.get("JS_QUICK_3");
		JS_QUICK_4 = javascript.get("JS_QUICK_4");
		JS_QUICK_A = javascript.get("JS_QUICK_A");
		JS_QUICK_B = javascript.get("JS_QUICK_B");
		JS_QUICK_TOGGLE = javascript.get("JS_QUICK_TOGGLE");
		
		JS_GPS_POSITION = javascript.get("JS_GPS_POSITION");
		JS_GPS_DIRECTION = javascript.get("JS_GPS_DIRECTION");
		JS_GPS_CAMERA = javascript.get("JS_GPS_CAMERA");
		JS_GPS_IDENTITY = javascript.get("JS_GPS_IDENTITY");
		
		LANGUAGE = preferences.get("LANGUAGE");
		OPACITY_FOCUSED = Float.parseFloat(preferences.get("OPACITY_FOCUSED"));
		OPACITY_UNFOCUSED = Float.parseFloat(preferences.get("OPACITY_UNFOCUSED"));
		wantOpacityOnFocus = Boolean.parseBoolean(preferences.get("wantOpacityOnFocus"));
		wantAlwaysOnTop = Boolean.parseBoolean(preferences.get("wantAlwaysOnTop"));
		wantNavbar = Boolean.parseBoolean(preferences.get("wantNavbar"));
		wantLastVisited = Boolean.parseBoolean(preferences.get("wantLastVisited"));
		BORDER_THICKNESS = Integer.parseInt(preferences.get("BORDER_THICKNESS"));
		wantGPS = Boolean.parseBoolean(preferences.get("wantGPS"));
		GPS_REFRESH_RATE = Integer.parseInt(preferences.get("GPS_REFRESH_RATE"));
		
		MENUBAR_HEIGHT = Integer.parseInt(componentconstants.get("MENUBAR_HEIGHT"));
		NAVBAR_THICKNESS = Integer.parseInt(componentconstants.get("NAVBAR_THICKNESS"));
		NAVBAR_HEIGHT = Integer.parseInt(componentconstants.get("NAVBAR_HEIGHT"));
		
		FRAME_MINIMUM = WindowPreset.parseDimension(standarddimensions.get("FRAME_MINIMUM"));
		FRAME_QUICK_1 = WindowPreset.parseDimension(standarddimensions.get("FRAME_QUICK_1"));
		FRAME_QUICK_2 = WindowPreset.parseDimension(standarddimensions.get("FRAME_QUICK_2"));
		FRAME_QUICK_3 = WindowPreset.parseDimension(standarddimensions.get("FRAME_QUICK_3"));
		FRAME_QUICK_4 = WindowPreset.parseDimension(standarddimensions.get("FRAME_QUICK_4"));
		FRAME_QUICK_A = WindowPreset.parseDimension(standarddimensions.get("FRAME_QUICK_A"));
		FRAME_QUICK_B = WindowPreset.parseDimension(standarddimensions.get("FRAME_QUICK_B"));
		
		WINDOWPRESET_START = new WindowPreset(windowpresets.get("WINDOWPRESET_START"));
		WINDOWPRESET_USER[0] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER0"));
		WINDOWPRESET_USER[1] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER1"));
		WINDOWPRESET_USER[2] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER2"));
		WINDOWPRESET_USER[3] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER3"));
		WINDOWPRESET_USER[4] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER4"));
		WINDOWPRESET_USER[5] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER5"));
		WINDOWPRESET_USER[6] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER6"));
		WINDOWPRESET_USER[7] = new WindowPreset(windowpresets.get("WINDOWPRESET_USER7"));
		JS_SIZE_USER[0] = javascript.get("JS_SIZE_USER0");
		JS_SIZE_USER[1] = javascript.get("JS_SIZE_USER1");
		JS_SIZE_USER[2] = javascript.get("JS_SIZE_USER2");
		JS_SIZE_USER[3] = javascript.get("JS_SIZE_USER3");
		JS_SIZE_USER[4] = javascript.get("JS_SIZE_USER4");
		JS_SIZE_USER[5] = javascript.get("JS_SIZE_USER5");
		JS_SIZE_USER[6] = javascript.get("JS_SIZE_USER6");
		JS_SIZE_USER[7] = javascript.get("JS_SIZE_USER7");
		
		COLORPRESET_START = new ColorPreset(colorpresets.get("COLORPRESET_START"));
		COLORPRESET_USER[0] = new ColorPreset(colorpresets.get("COLORPRESET_USER0"));
		COLORPRESET_USER[1] = new ColorPreset(colorpresets.get("COLORPRESET_USER1"));
		COLORPRESET_USER[2] = new ColorPreset(colorpresets.get("COLORPRESET_USER2"));
		COLORPRESET_USER[3] = new ColorPreset(colorpresets.get("COLORPRESET_USER3"));
	}
	
	
	// Methods to update both this object's and the text file's variables.
	// Note that only a few variables are changeable from the program's UI.
	// =========================================================================
	
	public void set_URL_LASTVISITED(String pValue)
	{
		URL_LASTVISITED = pValue;
		urls.put("URL_LASTVISITED", pValue);
	}
	
	public void set_LANGUAGE(String pValue)
	{
		LANGUAGE = pValue;
		preferences.put("LANGUAGE", pValue);
	}
	 
	public void set_wantOpacityOnFocus(boolean pValue)
	{
		wantOpacityOnFocus = pValue;
		preferences.put("wantOpacityOnFocus", pValue);
	}
	
	public void set_wantAlwaysOnTop(boolean pValue)
	{
		wantAlwaysOnTop = pValue;
		preferences.put("wantAlwaysOnTop", pValue);
	}
	
	public void set_wantNavbar(boolean pValue)
	{
		wantNavbar = pValue;
		preferences.put("wantNavbar", pValue);
	}
	
	public void set_wantLastVisited(boolean pValue)
	{
		wantLastVisited = pValue;
		preferences.put("wantLastVisited", pValue);
	}
	
	public void set_OPACITY_UNFOCUSED(float pValue)
	{
		OPACITY_UNFOCUSED = pValue;
		preferences.put("OPACITY_UNFOCUSED", pValue);
	}
	
	public void set_WINDOWPRESET_START(int pWidth, int pHeight, int pPosX, int pPosY)
	{
		windowpresets.put("WINDOWPRESET_START", WindowPreset.getString(pWidth, pHeight, pPosX, pPosY));
	}
	
	public void set_WINDOWPRESET_USER(WindowPreset pPreset, int pNumber)
	{
		WINDOWPRESET_USER[pNumber] = pPreset;
		windowpresets.put("WINDOWPRESET_USER" + Integer.toString(pNumber), pPreset.toString());
	}
	
	public void set_COLORPRESET_START()
	{
		colorpresets.put("COLORPRESET_START", COLORPRESET_START.toString());
	}
	
	public void set_COLORPRESET_USER(int pNumber)
	{
		String preset = COLORPRESET_START.toString();
		COLORPRESET_USER[pNumber] = new ColorPreset(preset);
		colorpresets.put("COLORPRESET_USER" + Integer.toString(pNumber), preset);
	}
	
	public void set_BORDER_THICKNESS(int pValue)
	{
		BORDER_THICKNESS = pValue;
		preferences.put("BORDER_THICKNESS", pValue);
	}
	
	public void set_wantGPS(boolean pValue)
	{
		wantGPS = pValue;
		preferences.put("wantGPS", pValue);
	}
}
