// ==========================================================
// Filename:    WavesApplet.java
// Description: demonstrates wave properties
//
// Authors:     L.R. McFarland
// Language:    java
// Created:     2001-11-10
// ==========================================================

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.text.*;

import java.text.*;

import java.lang.Math;
import java.util.Vector;


public class WavesApplet extends JApplet implements ActionListener  {

    int                frameNumber = -1;
    Timer              timer;
    boolean            frozen = false;

    JPanel             mainPanel;

    // ----- paramteres -----
    JPanel             topPanel;
    DecimalEntryPanel  timeDEP;
    DecimalEntryPanel  sampleRateDEP;
    DecimalEntryPanel  phaseStepDEP;

    // ----- input waves -----
    JPanel             waveA_panel;
    WaveDisplay        waveA_time_display;
    FFTDisplay         waveA_FFT_display;

    JPanel             waveB_panel;
    WaveDisplay        waveB_time_display;
    FFTDisplay         waveB_FFT_display;

    // ----- sum -----
    JPanel             sumPanel;
    LissajousDisplay   lissajous;
    WavePlotPanel      waves;
    FFTDisplay         ffts;


    public void init() {

	// ----- display -----

	mainPanel = new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

	// ---------------------
	// ----- top panel -----
	// ---------------------

	topPanel  = new JPanel();

	timeDEP   = new DecimalEntryPanel("Time:", "sec", 5);
	timeDEP.setValue(2); // defalut value
	topPanel.add(timeDEP);

	sampleRateDEP = new DecimalEntryPanel("Sample Rate", "Hz ", 3);
	sampleRateDEP.setValue(64); // default value
	topPanel.add(sampleRateDEP);

	phaseStepDEP = new DecimalEntryPanel("Animation Phase Step:","rad",5);
	phaseStepDEP.setValue(-0.2); // defalut value
	topPanel.add(phaseStepDEP);

	mainPanel.add(topPanel);

	// ----- animation -----

	int fps = 12; // TBD from command line argument
        int delay = (fps > 0) ? (1000 / fps) : 100;

        //Set up a timer that calls this object's action handler.
        timer = new Timer(fps, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

	// -------------------------
	// ----- wave displays -----
	// -------------------------

	// ----- wave A -----
	waveA_panel = new JPanel();
	waveA_panel.setLayout(new BoxLayout(waveA_panel, BoxLayout.X_AXIS));
	waveA_panel.setBorder(BorderFactory.createRaisedBevelBorder());

	waveA_time_display = new WaveDisplay("Wave A");
	waveA_panel.add(waveA_time_display);

	// fft display
	waveA_FFT_display = new FFTDisplay("FFT (Wave A)");
	waveA_panel.add(waveA_FFT_display);

	// fft.time_data = waves.wavePlot.data;
	// TBD set wave data

	mainPanel.add(waveA_panel);


	// ----- wave B -----
	waveB_panel = new JPanel();
	waveB_panel.setLayout(new BoxLayout(waveB_panel, BoxLayout.X_AXIS));
	waveB_panel.setBorder(BorderFactory.createRaisedBevelBorder());

	waveB_time_display = new WaveDisplay("Wave B");
	waveB_panel.add(waveB_time_display);

	waveB_FFT_display = new FFTDisplay("FFT (Wave B)");
	waveB_panel.add(waveB_FFT_display);

	mainPanel.add(waveB_panel);

	// ----- sum panel -----

	sumPanel = new JPanel();
	sumPanel.setLayout(new BoxLayout(sumPanel, BoxLayout.X_AXIS));
	sumPanel.setBorder(BorderFactory.createRaisedBevelBorder());

	lissajous = new LissajousDisplay();
	sumPanel.add(lissajous);

	waves = new WavePlotPanel("Wave A + Wave B");
	sumPanel.add(waves);

	ffts = new FFTDisplay("FFT (Wave B + Wave B)");
	sumPanel.add(ffts);

        sumPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (frozen) {
                    frozen = false;
                    startAnimation();
                } else {
                    frozen = true;
                    stopAnimation();
                }
            }
        });

	mainPanel.add(sumPanel);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

	// --------------------
	// ----- defaults -----
	// --------------------

	waveA_time_display.frequencyDEP.setValue(8);
	waveB_time_display.frequencyDEP.setValue(32);

	waveA_time_display.wavePlotPanel.wavePlot.fg = Color.red;
	waveB_time_display.wavePlotPanel.wavePlot.fg = Color.blue;

	waves.wavePlot.fg = Color.magenta;
	ffts.fft_sizeCB.setSelectedIndex(5);

	updateDisplay();

    }

    //Invoked by the browser only.  invokeLater not needed
    //because startAnimation can be called from any thread.
    public void start() {
	startAnimation();
    }

    //Invoked by the browser only.  invokeLater not needed
    //because stopAnimation can be called from any thread.
    public void stop() {
        stopAnimation();
    }

    //Can be invoked from any thread.
    public synchronized void startAnimation() {
        if (frozen) { 
            //Do nothing.  The user has requested that we 
            //stop changing the image.
        } else {
            //Start animating!
            if (!timer.isRunning()) {
                timer.start();
            }
        }
    }

    //Can be invoked from any thread.
    public synchronized void stopAnimation() {
        //Stop the animating thread.
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    public void actionPerformed(ActionEvent e) {
        //Advance the animation frame.
        frameNumber++;
	waveB_time_display.phaseDEP.setValue(frameNumber * 
					     phaseStepDEP.getValue());
	updateDisplay();

    }


    public void updateDisplay () {

	// TBD combine with AAA

	waveA_time_display.updateDisplay(timeDEP.getValue(), 
					 sampleRateDEP.getValue());
	waveB_time_display.updateDisplay(timeDEP.getValue(), 
					 sampleRateDEP.getValue());

	waveA_FFT_display.updateDisplay(
			    waveA_time_display.wavePlotPanel.wavePlot.data);
	waveB_FFT_display.updateDisplay(
		            waveB_time_display.wavePlotPanel.wavePlot.data);

	waves.sum2waves(waveA_time_display, waveB_time_display);
	waves.repaint();

	ffts.updateDisplay(waves.wavePlot.data);
	ffts.repaint();

	lissajous.plotPanel.sum2waves(waveA_time_display, waveB_time_display);
	lissajous.plotPanel.repaint();


    }

}

class XY {

    public double x = 0, y = 0;

    XY () {
	x = 0; y = 0;
    }

    XY (XY a) {
	x = a.x; y = a.y;
    }

    XY (Complex a) {
	x = a.r; y = a.i;
    }

    XY (double a, double b) {
	x = a; y = b;
    }

    public String toString() {
	StringBuffer result = new StringBuffer("(" + x + ", " + y + ") ");
	return(result.toString());
    }

}

class Complex {

    public double r = 0, i = 0;

    Complex () {
	r = 0; i = 0;
    }

    Complex (Complex a) {
	r = a.r; i = a.i;
    }

    Complex (XY a) {
	r = a.x; i = a.y;
    }

    Complex (double a, double b) {
	r = a; i = b;
    }

    public String toString() {
	StringBuffer result = new StringBuffer("(" + r + ", " + i + ") ");
	return(result.toString());
    }

}
