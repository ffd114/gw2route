package org.lunarproject.gw2route;

/**
 * Navi.java native OS browser wrapper frame for gw2timer.com website.
 * Based on SimpleWebBrowserExample.java and uses DJ Native Swing project by
 * Christopher Deckers. Resizable frame from coderanch.com post by "Iovcev Elena".
 * Various codes copied/adapted from Internet posting, mainly stackoverflow.com.
 * 
 * Libraries/APIs used:
 * DJNativeSwing for embedding browser in program.
 * Standard Widget Toolkit for displaying GUI with DJNativeSwing. 
 * ini4j for reading and writing user options from and to portable text files.
 * 
 * See variable declarations for program version.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.ini4j.Ini;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefAppHandlerAdapter;


public class Navi extends JPanel {
	
	final static String PROGRAMNAME = "gw2route";
	final static String PROGRAMVERSION = "0.0.2b";
	final static String DIRECTORY_ICONS = "img/";
	final static String FILENAME_OPTIONS = "options.ini";
	final static String FILENAME_TRANSLATIONS = "translations.ini";
	final static String FILENAME_BOOKMARKS = "bookmarks.txt";
	final static String REPO_URI = "https://github.com/ffd114/gw2route";
	final static String UPDATE_URI = "https://github.com/ffd114/gw2route/releases";
	
	WindowPreset currentWindowPreset;
	int ICON_HEIGHT = 16;
	int ADD_VERTICAL_PIXELS;
	int ADD_HORIZONTAL_PIXELS;
	int BORDER_THICKNESS_TOTAL;
	int RESOLUTION_WIDTH;
	int RESOLUTION_HEIGHT;
	static Color COLOR_BAR_CURRENT;
	int OPACITY_LEVELS_10 = 10;
	float OPACITY_STEP = 0.10f;
	boolean isMiniaturized = false;
	boolean isGPSStarted = false;
	
	protected Translation TheTranslations;
	protected static Option TheOptions;
	protected Bookmark TheBookmarks;
	private final CefApp     _cefApp;
	private final CefClient  _client;
	private final CefBrowser _browser;
	private CefSettings _settings;
	
	private static ResizableFrame TheFrame; // Frame resizable edges
	private static JPanel TheContainer; // Frame's container
	private static JPanel TheBar; // The draggable top bar of the overlay that contains the menu
	private static CustomMenu TheMenu; // The menu and its submenus on the bar
	private static JPopupMenu ThePopup; // The context menu that pops up from the bar after right clicking
	private static ClassLoader TheClassLoader; // Helper class to load images
	private static GPS TheGPS; // GPS memory reading class
	
	public static Point mousePressedPoint;
	
	final static String TEXT_FILELOADWARNING = " was not found in program's directory!\n"
		+ "The file should've been packaged with the program when you downloaded it.";
	final static String TEXT_FILESAVEWARNING = " was unable to be saved!";
	final static String TEXT_ABOUT =
		PROGRAMNAME + " browser-overlay for Guild Wars 2 by GW2Timer.com\n"
		+ "Version: " + PROGRAMVERSION + " - Released: 2014.07.16 - Created: 2014.06.01\n"
		+ "Browser embed code part of SWT by Eclipse and DJ Native Swing by Christopher Deckers.\n"
		+ "Uses icons from Crystal set by Everaldo Coelho.\n"
		+ "\n"
		+ "Shortcuts: Left click the \"G\" icon to miniaturize, right click it to minimize.\n"
		+ "Double click the overlay window bar to maximize. Move cursor over edges to resize.\n"
		+ "Press F5 in the browser to reload the website.\n"
		+ "\n"
		+ "Appearance: Right click the window bar to open the popup context menu.\n"
		+ "With the context menu visible, keypress (a letter then a number):\n"
		+ "S + # = load a Size preset.\n"
		+ "C + # = load a Color preset.\n"
		+ "# = load Opacity value.\n";

	public Navi() throws InterruptedException
	{
		super(new BorderLayout());
		// Load options and data first before doing anything
		loadStorage();

		// Initialize browser
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		webBrowserPanel.setBorder(BorderFactory.createEmptyBorder());

		// Add args to enable using flash
		String[] args = new String[] {"--enable-system-flash"};

		// Initialize cefbrowser
		CefApp.addAppHandler(new CefAppHandlerAdapter(args) {
			@Override
			public void stateHasChanged(org.cef.CefApp.CefAppState state) {
				// Shutdown the app if the native CEF part is terminated
				if (state == CefAppState.TERMINATED)
					System.exit(0);
			}
		});

		// Setup settings
		_settings = new CefSettings();

		// Enable cache
		_settings.cache_path = "cache";

		_cefApp = CefApp.getInstance(args, _settings);
		_client = _cefApp.createClient();
		_browser = _client.createBrowser(TheOptions.URL_HOMEPAGE, false, true);

		if (TheOptions.wantLastVisited)
		{
			_browser.loadURL(TheOptions.URL_LASTVISITED);
		}

		webBrowserPanel.add(_browser.getUIComponent(), BorderLayout.CENTER);
		add(webBrowserPanel, BorderLayout.CENTER);

		TheClassLoader = this.getClass().getClassLoader();

		// Browser frame initializations in separate functions to reduce clutter
		sumDimensions();
		createMenu();
		styleFrame();
		createPopup();
		bindFrameFocus();
		bindFrameClose();
		enableGPS(TheOptions.wantGPS);
	}
	

	/**
	 * Reads the options and other text files and initializes them.
	 * Must be in this order.
	 */
	private void loadStorage()
	{
		try
		{
			TheOptions = new Option(new Ini(new File(FILENAME_OPTIONS)));
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(TheFrame,
				FILENAME_OPTIONS + TEXT_FILELOADWARNING,
				"Warning", JOptionPane.ERROR_MESSAGE);
		}
		
		try
		{
			TheTranslations = new Translation(
				new Ini(new File(FILENAME_TRANSLATIONS)),
				TheOptions.LANGUAGE);
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(TheFrame,
				FILENAME_TRANSLATIONS + TEXT_FILELOADWARNING,
				"Warning", JOptionPane.ERROR_MESSAGE);
		}
		
		try
		{
			TheBookmarks = new Bookmark(FILENAME_BOOKMARKS);
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(TheFrame,
				FILENAME_BOOKMARKS + TEXT_FILELOADWARNING,
				"Warning", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Enables or disables the GPS class.
	 * @param pEnable or not.
	 */
	private void enableGPS(boolean pEnable)
	{
		if (pEnable)
		{
			if (!isGPSStarted)
			{
				TheGPS = new GPS(_browser, TheOptions);
				Thread gpsThread = new Thread(TheGPS);
				gpsThread.start();
				isGPSStarted = true;
			}
		}
		else
		{
			if (isGPSStarted)
			{
				TheGPS.kill();
				TheGPS = null;
				isGPSStarted = false;
				_browser.reload();
			}
		}
	}

	/**
	 * Extended class to recolor the menu bar, which itself is inside a JPanel.
	 */
	private class CustomMenu extends JMenuBar implements MouseMotionListener {
		
		public CustomMenu()
		{
			addMouseMotionListener(this);
		}

		/**
		 * Creates a rectangle of set color variable over the menu bar such that
		 * it becomes its "background". This method is automatically called when
		 * the menu bar is constructed or hovered on.
		 * @param g 
		 */
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Navi.COLOR_BAR_CURRENT);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
		
		public void mouseDragged(MouseEvent e)
		{
			Point currCoords = e.getLocationOnScreen();
			TheFrame.setLocation(currCoords.x - mousePressedPoint.x, currCoords.y - mousePressedPoint.y);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			
		}
	}
	
	/**
	 * Gets a icon object from its filename.
	 * @param pName filename without the extension.
	 * @return icon.
	 */
	private ImageIcon getIcon(String pName)
	{
		return new ImageIcon(TheClassLoader.getResource(DIRECTORY_ICONS + pName + ".png"));
	}
	
	/**
	 * Sums dimension quantities.
	 */
	private void sumDimensions()
	{
		BORDER_THICKNESS_TOTAL = TheOptions.BORDER_THICKNESS * 2;
		ADD_HORIZONTAL_PIXELS = BORDER_THICKNESS_TOTAL;
		ADD_VERTICAL_PIXELS = TheOptions.MENUBAR_HEIGHT + BORDER_THICKNESS_TOTAL;
	}
	
	private void resizeByThickness(int pNewThickness)
	{
		int adjust = pNewThickness - TheOptions.BORDER_THICKNESS;
		TheFrame.setSize(new Dimension(
			TheFrame.getWidth() + (adjust * 2),
			TheFrame.getHeight() + (adjust * 2)));
	}

	/**
	 * Creates the menu bar and add menu items to it. Also converts bookmarks to
	 * menu items in the bookmarks submenu.
	 */
	private void createMenu()
	{
		// Menu creation
		//==================================================================
		
		TheMenu = new CustomMenu();
		TheMenu.setCursor(new CustomCursor().NORMAL);
		JRadioButtonMenuItem tempradioitem;
		JMenuItem tempitem;
		
		JMenuItem menu_Miniaturize = new JMenuItem(getIcon("m_miniaturize"));
		JMenu menu_Main = new JMenu("");
		menu_Main.setIcon(getIcon("m_menu"));
		JMenuItem menu_Minimize = new JMenu("");
		menu_Minimize.setIcon(getIcon("m_minimize"));
		JMenuItem menu_Quick_1 = new JMenuItem(getIcon("m_1"));
		JMenuItem menu_Quick_2 = new JMenuItem(getIcon("m_2"));
		JMenuItem menu_Quick_3 = new JMenuItem(getIcon("m_3"));
		JMenuItem menu_Quick_4 = new JMenuItem(getIcon("m_4"));
		JMenuItem menu_Quick_A = new JMenuItem(getIcon("m_A"));
		JMenuItem menu_Quick_B = new JMenuItem(getIcon("m_B"));
		JMenuItem menu_Quick_Toggle = new JMenuItem(getIcon("m_toggle"));
		TheMenu.add(Box.createHorizontalGlue());
		
		menu_Miniaturize.setPreferredSize(new Dimension(16, ICON_HEIGHT));
		menu_Quick_1.setPreferredSize(new Dimension(20, ICON_HEIGHT));
		menu_Quick_2.setPreferredSize(new Dimension(20, ICON_HEIGHT));
		menu_Quick_3.setPreferredSize(new Dimension(20, ICON_HEIGHT));
		menu_Quick_4.setPreferredSize(new Dimension(20, ICON_HEIGHT));
		menu_Quick_A.setPreferredSize(new Dimension(20, ICON_HEIGHT));
		menu_Quick_B.setPreferredSize(new Dimension(20, ICON_HEIGHT));
		menu_Quick_Toggle.setPreferredSize(new Dimension(24, ICON_HEIGHT));
		
		// Press and drag mouse to move window
		menu_Miniaturize.addMouseMotionListener(TheMenu);
		menu_Main.addMouseMotionListener(TheMenu);
		menu_Minimize.addMouseMotionListener(TheMenu);
		menu_Quick_1.addMouseMotionListener(TheMenu);
		menu_Quick_2.addMouseMotionListener(TheMenu);
		menu_Quick_3.addMouseMotionListener(TheMenu);
		menu_Quick_4.addMouseMotionListener(TheMenu);
		menu_Quick_A.addMouseMotionListener(TheMenu);
		menu_Quick_B.addMouseMotionListener(TheMenu);
		menu_Quick_Toggle.addMouseMotionListener(TheMenu);
		
		// Right click to open context menu
		menu_Quick_1.addMouseListener(new PopupListener());
		menu_Quick_2.addMouseListener(new PopupListener());
		menu_Quick_3.addMouseListener(new PopupListener());
		menu_Quick_4.addMouseListener(new PopupListener());
		menu_Quick_A.addMouseListener(new PopupListener());
		menu_Quick_B.addMouseListener(new PopupListener());
		menu_Quick_Toggle.addMouseListener(new PopupListener());
		
		TheMenu.add(menu_Miniaturize);
		TheMenu.add(menu_Main);
		TheMenu.add(menu_Minimize);
		TheMenu.add(menu_Quick_1);
		TheMenu.add(menu_Quick_2);
		TheMenu.add(menu_Quick_3);
		TheMenu.add(menu_Quick_4);
		TheMenu.add(menu_Quick_A);
		TheMenu.add(menu_Quick_B);
		TheMenu.add(menu_Quick_Toggle);
		
		
		// "G" miniaturize button top level
		//------------------------------------------------------------------
		menu_Miniaturize.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_miniaturize_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_miniaturize"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					miniaturizeFrame();
				}
				else
				{
					TheFrame.setState(Frame.ICONIFIED);
				}
			}
		});
		
		// "_" minimize button top level
		//------------------------------------------------------------------
		menu_Minimize.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_minimize_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_minimize"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setState(Frame.ICONIFIED);
				}
			}
		});
		
		// "≡" menu top level
		//------------------------------------------------------------------
		menu_Main.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenu) e.getSource()).setIcon(getIcon("m_menu_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenu) e.getSource()).setIcon(getIcon("m_menu"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
		});
		
		JMenu menu_Bookmarks = new JMenu(TheTranslations.get("Bookmarks"));
		JMenu menu_Options = new JMenu(TheTranslations.get("Options"));
		// JMenuItem item_Navigation = new JCheckBoxMenuItem(TheTranslations.get("Show", "Navigation"));
		JMenuItem item_Home = new JMenuItem(TheTranslations.get("Homepage"));
		JMenuItem item_Update = new JMenuItem(TheTranslations.get("Update"));
		JMenuItem item_About = new JMenuItem(TheTranslations.get("About"));
		JMenuItem item_Exit = new JMenuItem(TheTranslations.get("Exit"));
		
		menu_Bookmarks.setIcon(getIcon("bookmark_folder"));
		menu_Options.setIcon(getIcon("options"));
		// item_Navigation.setIcon(getIcon("navigation"));
		item_Home.setIcon(getIcon("home"));
		item_Update.setIcon(getIcon("update"));
		item_About.setIcon(getIcon("about"));
		item_Exit.setIcon(getIcon("exit"));

		menu_Bookmarks.setMnemonic(KeyEvent.VK_B);
		menu_Options.setMnemonic(KeyEvent.VK_S);
		// item_Navigation.setMnemonic(KeyEvent.VK_N);
		item_Home.setMnemonic(KeyEvent.VK_H);
		item_Update.setMnemonic(KeyEvent.VK_U);
		item_About.setMnemonic(KeyEvent.VK_A);
		item_Exit.setMnemonic(KeyEvent.VK_X);
		
		menu_Main.add(menu_Bookmarks);
		// menu_Main.add(item_Navigation);
		menu_Main.addSeparator();
		menu_Main.add(item_Home);
		menu_Main.add(item_Update);
		menu_Main.add(item_About);
		menu_Main.addSeparator();
		menu_Main.add(menu_Options);
		menu_Main.addSeparator();
		menu_Main.add(item_Exit);
		
		// item_Navigation.setSelected(TheOptions.wantNavbar);

		/*
		item_Navigation.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				doShowNavbar(e.getStateChange() == ItemEvent.SELECTED, false);
			}
		});
		*/
		item_Home.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_browser.loadURL(TheOptions.URL_HOMEPAGE);
			}
		});
		item_Update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Desktop.getDesktop().browse(new URI(UPDATE_URI));
					TheFrame.setState(Frame.ICONIFIED);
				}
				catch (IOException | URISyntaxException ex) {}
			}
		});
		item_About.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{

				try
				{
					Desktop.getDesktop().browse(new URI(REPO_URI));
					TheFrame.setState(Frame.ICONIFIED);
				}
				catch (IOException | URISyntaxException ex) {}

			}
		});
		item_Exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doExit();
			}
		});

		// Menu items for "Bookmark" menu
		//------------------------------------------------------------------
		for (Map.Entry<String, String> entry : TheBookmarks.Book.entrySet())
		{
			String key = entry.getKey();
			tempitem = new JMenuItem(key);
			tempitem.setIcon(getIcon("bookmark"));

			tempitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					String key = e.getActionCommand();
					_browser.loadURL(TheBookmarks.Book.get(key));
				}
			});
			menu_Bookmarks.add(tempitem);
		}
		
		// Menu items for "Options" menu
		//------------------------------------------------------------------
		// Options submenu
		JMenuItem item_AlwaysOnTop = new JCheckBoxMenuItem(TheTranslations.get("AlwaysOnTop"));
		JMenuItem item_OpaqueOnFocus = new JCheckBoxMenuItem(TheTranslations.get("Opaque", "Focused"));
		JMenuItem item_LastVisited = new JCheckBoxMenuItem(TheTranslations.get("Enable", "LastVisited"));
		JMenuItem item_EnableGPS = new JCheckBoxMenuItem(TheTranslations.get("Enable") + " GPS");
		JMenu menu_Language = new JMenu(TheTranslations.get("Language"));
		
		item_AlwaysOnTop.setIcon(getIcon("alwaysontop"));
		item_OpaqueOnFocus.setIcon(getIcon("opaque"));
		item_LastVisited.setIcon(getIcon("lastvisited"));
		item_EnableGPS.setIcon(getIcon("gps"));
		menu_Language.setIcon(getIcon("language"));
		
		item_AlwaysOnTop.setMnemonic(KeyEvent.VK_A);
		item_OpaqueOnFocus.setMnemonic(KeyEvent.VK_Q);
		item_LastVisited.setMnemonic(KeyEvent.VK_V);
		item_EnableGPS.setMnemonic(KeyEvent.VK_G);
		menu_Language.setMnemonic(KeyEvent.VK_L);
		
		menu_Options.add(item_AlwaysOnTop);
		menu_Options.add(item_OpaqueOnFocus);
		menu_Options.addSeparator();
		menu_Options.add(item_LastVisited);
		menu_Options.add(item_EnableGPS);
		menu_Options.addSeparator();
		menu_Options.add(menu_Language);

		item_AlwaysOnTop.setSelected(TheOptions.wantAlwaysOnTop);
		item_OpaqueOnFocus.setSelected(TheOptions.wantOpacityOnFocus);
		item_LastVisited.setSelected(TheOptions.wantLastVisited);
		item_EnableGPS.setSelected(TheOptions.wantGPS);
		
		item_AlwaysOnTop.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				boolean want = (e.getStateChange() == ItemEvent.SELECTED);
				TheOptions.set_wantAlwaysOnTop(want);
				TheFrame.setAlwaysOnTop(want);
			}
		});
		
		item_LastVisited.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				TheOptions.set_wantLastVisited(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		item_EnableGPS.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				boolean want = (e.getStateChange() == ItemEvent.SELECTED);
				TheOptions.set_wantGPS(want);
				enableGPS(want);
			}
		});
		
		// Opacity on Focus option
		item_OpaqueOnFocus.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					TheOptions.set_wantOpacityOnFocus(true);
					if (TheFrame.isFocused())
					{
						TheFrame.setOpacity(TheOptions.OPACITY_FOCUSED);
					}
				}
				else
				{
					TheOptions.set_wantOpacityOnFocus(false);
					TheFrame.setOpacity(TheOptions.OPACITY_UNFOCUSED);
				}
			}
		});
		
		// Menu items for "Language" menu
		//------------------------------------------------------------------
		ButtonGroup group_Language = new ButtonGroup();
		final ArrayList<JRadioButtonMenuItem> item_LanguageArraylist = new ArrayList();
		
		for (int i = 0; i < TheTranslations.lang.length; i++)
		{
			tempradioitem = new JRadioButtonMenuItem(TheTranslations.lang[i]);
			
			tempradioitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					for (int i = 0; i < TheTranslations.lang.length; i++)
					{
						if (item_LanguageArraylist.get(i).isSelected())
						{
							// Save the selected language and ask user to restart
							TheOptions.set_LANGUAGE(TheTranslations.code[i]);
							
							JOptionPane.showMessageDialog(TheFrame,
								TheTranslations.getFromLang("LanguageSelected", TheTranslations.code[i]),
								TheTranslations.getFromLang("Language", TheTranslations.code[i]),
								JOptionPane.INFORMATION_MESSAGE);
							
							doExit();
							break;
						}
					}
				}
			});
			
			group_Language.add(tempradioitem);
			item_LanguageArraylist.add(tempradioitem);
			menu_Language.add(tempradioitem);
			
			// Select the language written in the options
			if (TheTranslations.code[i].equals(TheOptions.LANGUAGE))
			{
				item_LanguageArraylist.get(i).setSelected(true);
			}
		}
		
		// "1234AB" buttons top level
		//------------------------------------------------------------------
		
		/**
		 * Bar, Compact, and Tall sizes use the standard width so it's assumed
		 * that the website's sidebar must fit entirely in the window, so have
		 * to consider the border thickness too.
		 */
		menu_Quick_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_1_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_1"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setSize(
						TheOptions.FRAME_QUICK_1.width + ADD_HORIZONTAL_PIXELS,
						TheOptions.FRAME_QUICK_1.height + ADD_VERTICAL_PIXELS);

					verifiedExecute(TheOptions.JS_QUICK_1);
				}
			}
		});
		menu_Quick_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_2_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_2"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setSize(
						TheOptions.FRAME_QUICK_2.width + ADD_HORIZONTAL_PIXELS,
						TheOptions.FRAME_QUICK_2.height + ADD_VERTICAL_PIXELS);

					verifiedExecute(TheOptions.JS_QUICK_2);
				}
			}
		});
		menu_Quick_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_3_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_3"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setSize(
						TheOptions.FRAME_QUICK_3.width + ADD_HORIZONTAL_PIXELS,
						TheOptions.FRAME_QUICK_3.height + ADD_VERTICAL_PIXELS);

					verifiedExecute(TheOptions.JS_QUICK_3);
				}
			}
		});
		menu_Quick_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_4_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_4"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setSize(
						TheOptions.FRAME_QUICK_4.width + ADD_HORIZONTAL_PIXELS,
						TheOptions.FRAME_QUICK_4.height + ADD_VERTICAL_PIXELS);

					verifiedExecute(TheOptions.JS_QUICK_4);
				}
			}
		});
		menu_Quick_A.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_A_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_A"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setSize(
						TheOptions.FRAME_QUICK_A.width + ADD_HORIZONTAL_PIXELS,
						TheOptions.FRAME_QUICK_A.height + ADD_VERTICAL_PIXELS);
					
					verifiedExecute(TheOptions.JS_QUICK_A);
				}
			}
		});
		menu_Quick_B.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_B_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_B"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					TheFrame.setSize(
						TheOptions.FRAME_QUICK_B.width + ADD_HORIZONTAL_PIXELS,
						TheOptions.FRAME_QUICK_B.height + ADD_VERTICAL_PIXELS);
					
					verifiedExecute(TheOptions.JS_QUICK_B);
				}
			}
		});
		menu_Quick_Toggle.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_toggle_h"));
			}
			public void mouseExited(MouseEvent e)
			{
				((JMenuItem) e.getSource()).setIcon(getIcon("m_toggle"));
			}
			public void mousePressed(MouseEvent e)
			{
				mousePressedPoint = TheFrame.getMousePosition();
			}
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					verifiedExecute(TheOptions.JS_QUICK_TOGGLE);
				}
			}
		});
		
		
	} // END OF menu creation
	
	
	
	
	
	
	
	
	/**
	 * Creates the context menu that pops up after right clicking the menu bar.
	 * Serves mainly as a boolean options menu.
	 */
	private void createPopup()
	{
		ThePopup = new JPopupMenu();
		JMenuItem tempitem;
		JRadioButtonMenuItem tempradioitem;
		// Add listener to components that can bring up popup menus
		MouseListener popupListener = new PopupListener();
		TheBar.addMouseListener(popupListener);
		
		
		final JMenu menu_Sizes = new JMenu("(S) " + TheTranslations.get("Sizes"));
		final JMenu menu_Colors = new JMenu("(C) " + TheTranslations.get("Colors"));
		final JMenu menu_Border = new JMenu("(B) " + TheTranslations.get("Border"));
		
		menu_Sizes.setIcon(getIcon("presets"));
		menu_Colors.setIcon(getIcon("colors"));
		menu_Border.setIcon(getIcon("border"));
		
		menu_Sizes.setMnemonic(KeyEvent.VK_S);
		menu_Colors.setMnemonic(KeyEvent.VK_C);
		menu_Border.setMnemonic(KeyEvent.VK_B);
		
		ThePopup.add(menu_Sizes);
		ThePopup.add(menu_Colors);
		ThePopup.add(menu_Border);
		ThePopup.addSeparator();
		
		// Menu items for "Sizes" menu
		//------------------------------------------------------------------
		for (int i = 0; i < TheOptions.WINDOWPRESET_USER.length; i++)
		{
			tempitem = new JMenuItem(TheTranslations.get("Load") + " #" + (i+1));
			tempitem.setIcon(getIcon("open"));
			tempitem.setMnemonic('0' + (i+1));
			menu_Sizes.add(tempitem);
			tempitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					int index = getMenuItemIndex(menu_Sizes, e);
					loadWindowPreset(TheOptions.WINDOWPRESET_USER[index]);
					verifiedExecute(TheOptions.JS_SIZE_USER[index]);
				}
			});
		}
		
		menu_Sizes.addSeparator();
		
		for (int i = 0; i < TheOptions.WINDOWPRESET_USER.length; i++)
		{
			tempitem = new JMenuItem(TheTranslations.get("Save") + " #" + (i+1));
			tempitem.setIcon(getIcon("save"));
			menu_Sizes.add(tempitem);
			tempitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					// index needs to subtract the upper "Load" items and the separator item
					int index = getMenuItemIndex(menu_Sizes, e) - TheOptions.WINDOWPRESET_USER.length - 1;
					saveWindowPreset(index);
				}
			});
		}
		
		
		// Menu items for "Colors" menu
		//------------------------------------------------------------------
		// Load Theme items
		for (int i = 0; i < TheOptions.COLORPRESET_USER.length; i++)
		{
			tempitem = new JMenuItem(TheTranslations.get("Load", "Theme") + " #" + (i+1));
			tempitem.setIcon(getIcon("open"));
			tempitem.setMnemonic('0' + (i+1));
			menu_Colors.add(tempitem);
			tempitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					int index = getMenuItemIndex(menu_Colors, e);
					loadColorPreset(TheOptions.COLORPRESET_USER[index]);
				}
			});
		}
		
		// Set individual component color items
		JMenuItem item_ColorBarFocused = new JMenuItem(TheTranslations.get("Set", "Bar", "Focused"));
		JMenuItem item_ColorBarUnfocused = new JMenuItem(TheTranslations.get("Set", "Bar", "Not", "Focused"));
		JMenuItem item_ColorBorderFocused = new JMenuItem(TheTranslations.get("Set", "Border", "Focused"));
		JMenuItem item_ColorBorderUnfocused = new JMenuItem(TheTranslations.get("Set", "Border", "Not", "Focused"));
		
		item_ColorBarFocused.setIcon(getIcon("colorset"));
		item_ColorBarUnfocused.setIcon(getIcon("colorset"));
		item_ColorBorderFocused.setIcon(getIcon("colorset"));
		item_ColorBorderUnfocused.setIcon(getIcon("colorset"));
		
		menu_Colors.addSeparator();
		menu_Colors.add(item_ColorBarFocused);
		menu_Colors.add(item_ColorBarUnfocused);
		menu_Colors.add(item_ColorBorderFocused);
		menu_Colors.add(item_ColorBorderUnfocused);
		
		item_ColorBarFocused.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Color selectedcolor = JColorChooser.showDialog(
					TheFrame, "", TheOptions.COLORPRESET_START.BarFocused);
				if (selectedcolor != null)
				{
					TheOptions.COLORPRESET_START.BarFocused = selectedcolor;
					TheOptions.set_COLORPRESET_START();
				}
			}
		});
		item_ColorBarUnfocused.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Color selectedcolor = JColorChooser.showDialog(
					TheFrame, "", TheOptions.COLORPRESET_START.BarUnfocused);
				if (selectedcolor != null)
				{
					TheOptions.COLORPRESET_START.BarUnfocused = selectedcolor;
					TheOptions.set_COLORPRESET_START();
				}
			}
		});
		item_ColorBorderFocused.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Color selectedcolor = JColorChooser.showDialog(
					TheFrame, "", TheOptions.COLORPRESET_START.BorderFocused);
				if (selectedcolor != null)
				{
					TheOptions.COLORPRESET_START.BorderFocused = selectedcolor;
					TheOptions.set_COLORPRESET_START();
				}
			}
		});
		item_ColorBorderUnfocused.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Color selectedcolor = JColorChooser.showDialog(
					TheFrame, "", TheOptions.COLORPRESET_START.BorderUnfocused);
				if (selectedcolor != null)
				{
					TheOptions.COLORPRESET_START.BorderUnfocused = selectedcolor;
					TheOptions.set_COLORPRESET_START();
				}
			}
		});
		
		menu_Colors.addSeparator();
		
		// Save Theme items
		for (int i = 0; i < TheOptions.COLORPRESET_USER.length; i++)
		{
			tempitem = new JMenuItem(TheTranslations.get("Save", "Theme") + " #" + (i+1));
			tempitem.setIcon(getIcon("save"));
			menu_Colors.add(tempitem);
			tempitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					// index needs to subtract the upper "Load" items, "Set" items, and the separator items
					int index = getMenuItemIndex(menu_Colors, e) - TheOptions.COLORPRESET_USER.length - (4+2);
					TheOptions.set_COLORPRESET_USER(index);
				}
			});
		}
		
		// Menu items for "Border" menu
		//------------------------------------------------------------------
		int maxborderpixels = 2;
		for (int i = 0; i <= maxborderpixels; i++)
		{
			tempitem = new JMenuItem(i + " " + TheTranslations.get("Pixel"));
			tempitem.setIcon(getIcon("pip"));
			tempitem.setMnemonic('0' + i);
			menu_Border.add(tempitem);
			tempitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					int index = getMenuItemIndex(menu_Border, e);
					resizeByThickness(index);
					TheOptions.set_BORDER_THICKNESS(index);
					sumDimensions();
					doFrameFocus();
				}
			});
		}
		
		
		// Opacity range 0-100%
		//------------------------------------------------------------------
		ButtonGroup group_Opacity = new ButtonGroup();
		final ArrayList<JRadioButtonMenuItem> item_OpacityArraylist = new ArrayList();

		for (int i = 0; i < OPACITY_LEVELS_10; i++)
		{
			tempradioitem = new JRadioButtonMenuItem(Integer
				.toString(OPACITY_LEVELS_10 * (OPACITY_LEVELS_10 - i)) + "%");
			if (i == 0)
			{
				tempradioitem.setMnemonic('0');
			}
			else
			{
				tempradioitem.setMnemonic(('9' - i) + 1);
			}

			tempradioitem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					for (int i = 0; i < OPACITY_LEVELS_10; i++)
					{
						if (item_OpacityArraylist.get(i).isSelected())
						{
							TheOptions.set_OPACITY_UNFOCUSED(
								(OPACITY_LEVELS_10 - i) * OPACITY_STEP);
							TheFrame.setOpacity(TheOptions.OPACITY_UNFOCUSED);
							break;
						}
					}
				}
			});

			group_Opacity.add(tempradioitem);
			item_OpacityArraylist.add(tempradioitem);
			ThePopup.add(tempradioitem);
		}
		item_OpacityArraylist.get(OPACITY_LEVELS_10 -
			(int)(TheOptions.OPACITY_UNFOCUSED * OPACITY_LEVELS_10)).setSelected(true);
	}
	
	/**
	 * Listener class for popup menu.
	 */
	class PopupListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger())
			{
				ThePopup.show(e.getComponent(),
						   e.getX(), e.getY());
			}
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * Saves the current frame size and resizes the frame to fit only the bar
	 * icon, or resizes it back to old size if already miniaturized.
	 */
	private void miniaturizeFrame()
	{
		if (isMiniaturized)
		{
			TheFrame.setSize(currentWindowPreset.Width, currentWindowPreset.Height);
		}
		else
		{
			currentWindowPreset = new WindowPreset(TheFrame);
			TheFrame.setSize(
				TheOptions.MENUBAR_HEIGHT,
				TheOptions.MENUBAR_HEIGHT + BORDER_THICKNESS_TOTAL);
		}
		isMiniaturized = !isMiniaturized;
	}

	/**
	 * Adjusts the frame's size and position from a preset.
	 * @param pPreset to read.
	 */
	private void loadWindowPreset(WindowPreset pPreset)
	{
		TheFrame.setSize(pPreset.Width, pPreset.Height);
		TheFrame.setLocation(pPreset.PosX, pPreset.PosY);
	}
	
	/**
	 * Saves the frame's size and position to the associated variable and the
	 * options text file.
	 * @param pNumber to select preset.
	 */
	private void saveWindowPreset(int pNumber)
	{
		WindowPreset preset = new WindowPreset(TheFrame);
		TheOptions.set_WINDOWPRESET_USER(preset, pNumber);
	}
	
	/**
	 * Reassigns the color objects from a preset.
	 * @param pPreset to read.
	 */
	private void loadColorPreset(ColorPreset pPreset)
	{
		TheOptions.COLORPRESET_START = new ColorPreset(pPreset.toString());
		TheOptions.set_COLORPRESET_START();
		
		TheFrame.setFocusable(false);
		TheFrame.setFocusable(true);
		
		// Refresh frame visuals
		doFrameFocus();
	}

	/**
	 * Sets the default style and behavior of the frame that contains the browser.
	 */
	private void styleFrame()
	{
		// Create frame (the window)
		Point initialLocation = new Point(
			TheOptions.WINDOWPRESET_START.PosX,
			TheOptions.WINDOWPRESET_START.PosY);
		Dimension initialDimension = new Dimension(
			TheOptions.WINDOWPRESET_START.Width,
			TheOptions.WINDOWPRESET_START.Height);
		Dimension minimumDimension = new Dimension(
			TheOptions.FRAME_MINIMUM.width + ADD_HORIZONTAL_PIXELS,
			TheOptions.FRAME_MINIMUM.height + ADD_VERTICAL_PIXELS);
		TheFrame = new ResizableFrame(initialDimension, minimumDimension, initialLocation);
		RESOLUTION_WIDTH = (int) TheFrame.screen.getWidth();
		RESOLUTION_HEIGHT = (int) TheFrame.screen.getHeight();

		// Additional frame settings
		TheFrame.setTitle(PROGRAMNAME);
		TheFrame.getContentPane().add(this, BorderLayout.CENTER);
		TheFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		TheFrame.setAlwaysOnTop(TheOptions.wantAlwaysOnTop);
		TheFrame.setOpacity(TheOptions.OPACITY_UNFOCUSED);
		TheFrame.setVisible(true);

		// Container inside the frame
		TheContainer = (JPanel) TheFrame.getContentPane();
		TheContainer.setBackground(TheOptions.COLORPRESET_START.BarUnfocused);

		// Top bar of the frame, acts as a menu and a place to drag-move the frame
		TheBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
		TheBar.add(TheMenu);
		TheBar.setPreferredSize(new Dimension(
			TheOptions.WINDOWPRESET_START.Width,
			TheOptions.MENUBAR_HEIGHT));
		TheBar.setBackground(TheOptions.COLORPRESET_START.BarUnfocused);
		TheBar.addMouseListener(TheFrame);
		TheBar.addMouseMotionListener(TheFrame);
		TheContainer.add(TheBar, BorderLayout.NORTH);

		// Set program taskbar icon
		TheFrame.setIconImage(getIcon("icon_program").getImage());
		
		// Show navbar if chosen before
		// doShowNavbar(TheOptions.wantNavbar, true);
	}

	/**
	 * Does options saving before exiting when user exits the program.
	 */
	private void bindFrameClose()
	{
		TheFrame.addWindowListener( new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				doExit();
			}
		});
	}
	
	/**
	 * Does actions needed to be done before closing the program.
	 */
	private void doExit()
	{
		// Stop GPS loop thread
		enableGPS(false);
		// Save last visited URL
		TheOptions.set_URL_LASTVISITED(_browser.getURL());
		// Save all options and exit
		saveOptions();
		System.exit(0);
	}

	/**
	 * Detects frame's focus state to do the visual changes.
	 */
	private void bindFrameFocus()
	{
		// Frame focus change visual
		TheFrame.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
				doFrameFocus();
			}

			public void windowLostFocus(WindowEvent e) {
				doFrameFocus();
			}
		});
	}
	
	/**
	 * Changes the visuals of the frame depending focus state.
	 */
	private void doFrameFocus()
	{
		if (TheFrame.isFocused())
		{
			// Restyle the bar
			COLOR_BAR_CURRENT = TheOptions.COLORPRESET_START.BarFocused;
			TheBar.setBackground(TheOptions.COLORPRESET_START.BarFocused);
			if (TheOptions.wantOpacityOnFocus)
			{
				TheFrame.setOpacity(TheOptions.OPACITY_FOCUSED);
			}
			else
			{
				TheFrame.setOpacity(TheOptions.OPACITY_UNFOCUSED);
			}

			// Restyle the border
			LineBorder panelBorder = new LineBorder(
				TheOptions.COLORPRESET_START.BorderFocused, TheOptions.BORDER_THICKNESS);
			TheContainer.setBorder(panelBorder);
		}
		else
		{
			// Restyle the bar
			COLOR_BAR_CURRENT = TheOptions.COLORPRESET_START.BarUnfocused;
			TheBar.setBackground(TheOptions.COLORPRESET_START.BarUnfocused);
			if (TheOptions.wantOpacityOnFocus)
			{
				TheFrame.setOpacity(TheOptions.OPACITY_UNFOCUSED);
			}
			else
			{
				TheFrame.setOpacity(TheOptions.OPACITY_UNFOCUSED);
			}

			// Restyle the border
			LineBorder panelBorder = new LineBorder(
				TheOptions.COLORPRESET_START.BorderUnfocused, TheOptions.BORDER_THICKNESS);
			TheContainer.setBorder(panelBorder);
		}
	}

	/**
	 * Saves all options that could have been changed while using the program
	 * into the options text file.
	 */
	private void saveOptions()
	{
		// Current frame size and position
		if ( ! isMiniaturized)
		{
			TheOptions.set_WINDOWPRESET_START(
				TheFrame.getWidth(),
				TheFrame.getHeight(),
				TheFrame.getX(),
				TheFrame.getY()
			);
		}

		// Save the file
		try
		{
			TheOptions.File.store();
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(TheFrame,
				FILENAME_OPTIONS + TEXT_FILESAVEWARNING,
				"Warning", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Tells if current viewed site is GW2Timer or at least localhost so
	 * JavaScript is executed exclusively there.
	 * @param pBrowser to get function.
	 * @param pOption to get comparison.
	 * @return true if so.
	 */
	protected static boolean verifySite(CefBrowser pBrowser, Option pOption)
	{
		String currenturl = pBrowser.getURL();
		String sitedomain = pOption.URL_SITE;
		String localdomain = pOption.URL_LOCAL;
		
		// Checks if the substring from the beginning of the URL contains the match
		if (currenturl.length() >= sitedomain.length())
		{
			if (currenturl.substring(0, sitedomain.length()).equals(sitedomain))
			{
				return true;
			}
		}
		if (currenturl.length() >= localdomain.length())
		{
			if (currenturl.substring(0, localdomain.length()).equals(localdomain))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Executes JavaScript after verifying the current URL location.
	 * Does nothing if not.
	 * @param pJavaScript to execute.
	 */
	private void verifiedExecute(String pJavaScript)
	{
		if (verifySite(_browser, TheOptions))
		{
			_browser.executeJavaScript(pJavaScript, _browser.getURL(), 0);
		}
	}
	
	/**
	 * Gets the top to bottom index of menu item inside a menu.
	 * @param pMenu container.
	 * @param pEvent that the menu item triggered.
	 * @return index of the menu item.
	 */
	private int getMenuItemIndex(JMenu pMenu, ActionEvent pEvent)
	{
		JMenuItem tempitem;
		for (int i = 0; i < pMenu.getItemCount(); i++)
		{
			tempitem = pMenu.getItem(i);
			// Ignore separators, which returns null from getItem
			if (tempitem != null)
			{
				if (tempitem.equals(((JMenuItem) pEvent.getSource())))
				{
					return i;
				}
			}
		}
		return -1;
	}
}
