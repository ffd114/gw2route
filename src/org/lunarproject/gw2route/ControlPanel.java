package org.lunarproject.gw2route;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cef.OS;
import org.cef.browser.CefBrowser;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

    private final JButton backButton_;
    private final JButton forwardButton_;
    private final JTextField address_field_;
    private final JLabel zoom_label_;
    private double zoomLevel_ = 0;
    private final CefBrowser browser_;

    public ControlPanel(CefBrowser browser) {
        browser_ = browser;
        setEnabled(browser_ != null);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        //add(Box.createHorizontalStrut(5));
        //add(Box.createHorizontalStrut(5));

        backButton_ = new JButton("<");
        backButton_.setAlignmentX(LEFT_ALIGNMENT);
        backButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.goBack();
            }
        });
        add(backButton_);
        add(Box.createHorizontalStrut(5));

        forwardButton_ = new JButton(">");
        forwardButton_.setAlignmentX(LEFT_ALIGNMENT);
        forwardButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.goForward();
            }
        });
        add(forwardButton_);
        add(Box.createHorizontalStrut(5));

        address_field_ = new JTextField(100);
        address_field_.setAlignmentX(LEFT_ALIGNMENT);
        address_field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.loadURL(getAddress());
            }
        });
        address_field_.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .clearGlobalFocusOwner();
                address_field_.requestFocus();
            }
        });
        add(address_field_);
        add(Box.createHorizontalStrut(5));

        JButton goButton = new JButton("Go");
        goButton.setAlignmentX(LEFT_ALIGNMENT);
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.loadURL(getAddress());
            }
        });
        add(goButton);
        add(Box.createHorizontalStrut(5));

        JButton minusButton = new JButton("-");
        minusButton.setAlignmentX(CENTER_ALIGNMENT);
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.setZoomLevel(--zoomLevel_);
                zoom_label_.setText(new Double(zoomLevel_).toString());
            }
        });
        add(minusButton);

        zoom_label_ = new JLabel("0.0");
        add(zoom_label_);

        JButton plusButton = new JButton("+");
        plusButton.setAlignmentX(CENTER_ALIGNMENT);
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser_.setZoomLevel(++zoomLevel_);
                zoom_label_.setText(new Double(zoomLevel_).toString());
            }
        });
        add(plusButton);
    }

    public String getAddress() {
        String address = address_field_.getText();
        // If the URI format is unknown "new URI" will throw an
        // exception. In this case we interpret the value of the
        // address field as search request. Therefore we simply add
        // the "search" scheme.
        try {
            address = address.replaceAll(" ", "%20");
            URI test = new URI(address);
            if (test.getScheme() != null)
                return address;
            if (test.getHost() != null && test.getPath() != null)
                return address;
            String specific = test.getSchemeSpecificPart();
            if (specific.indexOf('.') == -1)
                throw new URISyntaxException(specific, "No dot inside domain");
        } catch (URISyntaxException e1) {
            address = "search://" + address;
        }
        return address;
    }

    public void setAddress(CefBrowser browser, String address) {
        if (browser == browser_)
            address_field_.setText(address);
    }
}