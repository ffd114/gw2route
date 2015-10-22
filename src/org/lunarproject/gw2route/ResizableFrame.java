package org.lunarproject.gw2route;

/**
 * ResizableFrame.java created by Iovcev Elena. Constructs a JFrame that is undecorated
 * without an OS' usual GUI elements outside the frame, and is resizable on the
 * edges and moveable from a custom title bar.
 * Source: http://www.coderanch.com/t/415944/GUI/java/user-ve-undecorated-window-resizable
 */
import org.cef.CefApp;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;
 
public class ResizableFrame extends JFrame implements MouseMotionListener, MouseListener
{
	private Point start_drag;
	private Point start_loc;
	private Point precedent_loc;
	private int precedent_width;
	private int precedent_height;
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private int minWidth;
	private int minHeight;
	private Point initialLocation;
	Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	
	int cursorAreaSides = 2;
	int cursorAreaCorners = 4;
	CustomCursor cursors = new CustomCursor();
	

	public ResizableFrame(Dimension initialDimension, Dimension minimumDimension, Point initialLocation)
	{
		this.initialLocation = initialLocation;
		minWidth = (int) minimumDimension.getWidth();
		minHeight = (int) minimumDimension.getHeight();
		Init((int) initialDimension.getWidth(), (int) initialDimension.getHeight());
	}
			
	private void Init(int pIniWidth, int pIniHeight)
	{
		addMouseMotionListener(this);
		addMouseListener(this);
		this.setSize(pIniWidth, pIniHeight);
	
		setLocation(initialLocation);
		setUndecorated(true);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				CefApp.getInstance().dispose();
				dispose();
			}
		});
	}
	
	public void setMinimumSize(int pMinWidth, int pMinHeight)
	{
		minWidth = pMinWidth;
		minHeight = pMinHeight;
	}
 
	public static Point getScreenLocation(MouseEvent e, JFrame frame) 
	{ 
		Point cursor = e.getPoint();
		Point view_location = frame.getLocationOnScreen();
		return new Point((int) (view_location.getX() + cursor.getX()), 
			(int) (view_location.getY() + cursor.getY()));
	} 

	@Override
	public void mouseDragged(MouseEvent e)
	{
		moveOrFullResizeFrame(e);
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		Point cursorLocation = e.getPoint();
		int xPos = cursorLocation.x;
		int yPos = cursorLocation.y;
				
		// Corner areas are in first order of conditionals so they have higher priority
		if (xPos <= cursorAreaCorners && yPos <= cursorAreaCorners)
			setCursor(cursors.RESIZE_NW);
		else if (xPos >= getWidth() - cursorAreaCorners && yPos <= cursorAreaCorners)
			setCursor(cursors.RESIZE_NE);
		else if (xPos >= getWidth() - cursorAreaCorners && yPos >= getHeight() - cursorAreaCorners)
			setCursor(cursors.RESIZE_SE);
		else if (xPos <= cursorAreaCorners && yPos >= getHeight() - cursorAreaCorners)
			setCursor(cursors.RESIZE_SW);
		// Side areas lower priority than corner areas
		else if (xPos >= cursorAreaSides && xPos <= getWidth() - cursorAreaSides && yPos >= getHeight() - cursorAreaSides)
			setCursor(cursors.RESIZE_S);
		else if (xPos >= getWidth() - cursorAreaSides && yPos >= cursorAreaSides && yPos <= getHeight() - cursorAreaSides)
			setCursor(cursors.RESIZE_E);
		else if (xPos <= cursorAreaSides && yPos >= cursorAreaSides && yPos <= getHeight() - cursorAreaSides)
			setCursor(cursors.RESIZE_W);
		else if (xPos >= cursorAreaSides && xPos <= getWidth() - cursorAreaSides && yPos <= cursorAreaSides)
			setCursor(cursors.RESIZE_N);
		else
			setCursor(cursors.NORMAL);
	}
			
	@Override
	public void mouseClicked(MouseEvent e)
	{
		Object sourceObject = e.getSource();
		if (sourceObject instanceof JPanel)
		{
			// Double click using the left mouse button
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
			{
				if (getCursor().equals(cursors.NORMAL))
				{
					headerDoubleClickResize();
				}
			}
		}
	}
	
	public void moveOrFullResizeFrame(MouseEvent e)
	{
		Object sourceObject = e.getSource();
		Point current = getScreenLocation(e, this);
		Point offset = null;
		try
		{
			offset = new Point((int)current.getX() - (int)start_drag.getX(), (int)current.getY() - (int)start_drag.getY());
		}
		catch (NullPointerException ex)
		{
			return;
		}
		
		if (sourceObject instanceof JPanel && getCursor().equals(cursors.NORMAL))
		{
			setLocation((int) (start_loc.getX() + offset.getX()), (int) (start_loc.getY() + offset.getY()));
		}
		else if (!getCursor().equals(cursors.NORMAL))
		{
			int oldLocationX = (int) getLocation().getX();
			int oldLocationY = (int) getLocation().getY();
			int newLocationX = (int) (this.start_loc.getX() + offset.getX());
			int newLocationY = (int) (this.start_loc.getY() + offset.getY());
			boolean N_Resize = getCursor().equals(cursors.RESIZE_N);
			boolean NE_Resize = getCursor().equals(cursors.RESIZE_NE);
			boolean NW_Resize = getCursor().equals(cursors.RESIZE_NW);
			boolean E_Resize = getCursor().equals(cursors.RESIZE_E);
			boolean W_Resize = getCursor().equals(cursors.RESIZE_W);
			boolean S_Resize = getCursor().equals(cursors.RESIZE_S);
			boolean SW_Resize = getCursor().equals(cursors.RESIZE_SW);
			boolean setLocation = false;
			int newWidth = e.getX();
			int newHeight = e.getY();
					
			if (NE_Resize)
			{
				newHeight = getHeight() - (newLocationY - oldLocationY);
				newLocationX = (int) getLocation().getX();
				setLocation = true;
			}
			else if (E_Resize)
			{
				newHeight = getHeight();
			}
			else if (S_Resize)
			{
				newWidth = getWidth();
			}	
			else if (N_Resize)
			{
				newLocationX = (int) getLocation().getX();
				newWidth = getWidth();
				newHeight = getHeight() - (newLocationY - oldLocationY);
				setLocation = true;
			}
			else if (NW_Resize)
			{
				newWidth = getWidth() - (newLocationX - oldLocationX);
				newHeight = getHeight() - (newLocationY - oldLocationY);
				setLocation = true;
			}
			else if (NE_Resize)
			{
				newHeight = getHeight() - (newLocationY - oldLocationY);
				newLocationX = (int) getLocation().getX();
			}
			else if (SW_Resize)
			{
				newWidth = getWidth() - (newLocationX - oldLocationX);
				newLocationY = (int) getLocation().getY();
				setLocation = true;
			}
			if (W_Resize)
			{
				newWidth = getWidth() - (newLocationX - oldLocationX);
				newLocationY = (int) getLocation().getY();
				newHeight = getHeight();
				setLocation = true;
			}
			
			if (newWidth >= (int)toolkit.getScreenSize().getWidth() || newWidth <= minWidth)
			{
				newLocationX = oldLocationX;
				newWidth = getWidth();
			}
			
			if (newHeight >= (int)toolkit.getScreenSize().getHeight() - 30 || newHeight <= minHeight)
			{
				newLocationY = oldLocationY;
				newHeight = getHeight();
			}
			
			if (newWidth != getWidth() || newHeight != getHeight())
			{
				this.setSize(newWidth, newHeight);
							
				if (setLocation)
				{
					this.setLocation(newLocationX, newLocationY);
				}
			}
		}
	}
	
	private void headerDoubleClickResize()
	{
		if (getWidth() < screen.getWidth() || getHeight() < screen.getHeight()) 
		{
			this.setSize((int)screen.getWidth(),(int)screen.getHeight());
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = this.getSize();
			this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		} 
		else
		{
			this.setSize(precedent_width, precedent_height);
			this.setLocation(precedent_loc);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		this.start_drag = getScreenLocation(e, this);
		this.start_loc = this.getLocation();
		
		if (getWidth() < screen.getWidth() || getHeight() < screen.getHeight()) 
		{
			precedent_loc = this.getLocation();
			precedent_width = getWidth();
			precedent_height = getHeight();
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
			
	@Override
	public void mouseReleased(MouseEvent e) {}
}
