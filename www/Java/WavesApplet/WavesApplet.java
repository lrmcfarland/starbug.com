// ==========================================================
// Filename:    WavesApplet.java
// Description: demonstrates wave properties
//              from: http://java.sun.com/docs/books/tutorial/uiswing/painting/example-swing/AnimatorApplicationTimer.java
// Authors:     L.R. McFarland
// Language:    java
// Created:     2001-11-03
// ==========================================================

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.text.*;

import java.text.*;

import java.lang.Math;
import java.util.Vector;

public class WavesApplet extends JApplet implements ActionListener {

    int                frameNumber = -1;
    Timer              timer;
    boolean            frozen = false;

    JPanel             mainPanel;

    JPanel             topPanel;
    DecimalEntryPanel  timeDEP;
    DecimalEntryPanel  phaseStepDEP;

    WaveDisplay        waveD1;
    WaveDisplay        waveD2;

    JPanel             sumPanel;

    WavePlotPanel      waves;
    LissajousDisplay   lissajous;


    public void init() {

	// ----- display -----

	mainPanel = new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

	// ---------------------
	// ----- top panel -----
	// ---------------------

	topPanel  = new JPanel();

	timeDEP   = new DecimalEntryPanel("Time:", "sec", 5);
	timeDEP.setValue(10); // defalut value
	topPanel.add(timeDEP);

	phaseStepDEP = new DecimalEntryPanel("Animation Phase Step:","rad",5);
	phaseStepDEP.setValue(-0.1); // defalut value
	topPanel.add(phaseStepDEP);

	mainPanel.add(topPanel);

	// ----- animation -----

	int fps = 6; // TBD from command line argument
        int delay = (fps > 0) ? (1000 / fps) : 100;

        //Set up a timer that calls this object's action handler.
        timer = new Timer(delay, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

	// -------------------------
	// ----- wave displays -----
	// -------------------------

	waveD1 = new WaveDisplay("Wave A");
	mainPanel.add(waveD1);

	waveD2 = new WaveDisplay("Wave B");
	mainPanel.add(waveD2);

	// ----- sum panel -----

	sumPanel = new JPanel();
	sumPanel.setLayout(new BoxLayout(sumPanel, BoxLayout.X_AXIS));
	sumPanel.setBorder(BorderFactory.createRaisedBevelBorder());

	lissajous = new LissajousDisplay();
	sumPanel.add(lissajous);

	waves = new WavePlotPanel("Wave A + Wave B");
	sumPanel.add(waves);

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

	waveD1.frequencyDEP.setValue(1);
	waveD2.frequencyDEP.setValue(2);

	waveD1.plotPanel.plot.fg = Color.red;
	waveD2.plotPanel.plot.fg = Color.blue;
	waves.plot.fg            = Color.magenta;

	waveD1.updateDisplay(timeDEP.getValue());
	waveD2.updateDisplay(timeDEP.getValue());
	waves.sum2waves(waveD1, waveD2);
	waves.repaint();
	lissajous.plotPanel.sum2waves(waveD1, waveD2);
	lissajous.plotPanel.repaint();

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
	waveD2.phaseDEP.setValue(frameNumber * phaseStepDEP.getValue());
	waveD1.updateDisplay(timeDEP.getValue());
	waveD2.updateDisplay(timeDEP.getValue());
	waves.sum2waves(waveD1, waveD2);
	waves.repaint();
	lissajous.plotPanel.sum2waves(waveD1, waveD2);
	lissajous.plotPanel.repaint();
    }
}

class WaveDisplay extends JPanel {

    DecimalEntryPanel frequencyDEP;
    DecimalEntryPanel amplitudeDEP;
    DecimalEntryPanel phaseDEP;
    DecimalEntryPanel sampleRateDEP;
    JPanel            parameterPanel;
    WavePlotPanel     plotPanel;

    public WaveDisplay (String name) {

	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setBorder(BorderFactory.createRaisedBevelBorder());

	// ---------------------------
	// ----- parameter panel -----
	// ---------------------------

	parameterPanel = new JPanel();
	parameterPanel.setLayout(new BoxLayout(parameterPanel, 
					       BoxLayout.Y_AXIS));

	frequencyDEP = new DecimalEntryPanel(" frequency ", "Hz ", 3);
	frequencyDEP.setValue(2); // default value
	parameterPanel.add(frequencyDEP);

	amplitudeDEP = new DecimalEntryPanel(" amplitude ", "dB ", 3);
	amplitudeDEP.setValue(1); // default value
	parameterPanel.add(amplitudeDEP);

	phaseDEP = new DecimalEntryPanel("    phase    ", "rad", 3);
	phaseDEP.setValue(0); // default value
	parameterPanel.add(phaseDEP);

	sampleRateDEP = new DecimalEntryPanel("sample rate", "Hz ", 3);
	sampleRateDEP.setValue(32); // default value
	parameterPanel.add(sampleRateDEP);

	add(parameterPanel);

	// ----------------
	// ----- plot -----
	// ------ ---------

	plotPanel = new WavePlotPanel(name);
	add(plotPanel);

    }

    void updateDisplay (double time) {
	plotPanel.plot.time       = time;
	plotPanel.plot.frequency  = frequencyDEP.getValue();
	plotPanel.plot.amplitude  = amplitudeDEP.getValue();
	plotPanel.plot.phase      = phaseDEP.getValue();
	plotPanel.plot.sampleRate = sampleRateDEP.getValue();
	plotPanel.plot.generateSineData();
	plotPanel.plot.repaint();
    }

}

class WavePlotPanel extends JPanel {

    JPanel   namePanel;
    JLabel   nameLb;

    JPanel   timePanel; // to get label to center
    JLabel   timeLb;

    JPanel   amplitudePanel; // to get label to center
    JLabel   amplitudeLb;

    JPanel   plotPanel;
    WavePlot plot;

    public WavePlotPanel (String name) {

	setLayout(new BorderLayout());
	setBorder(BorderFactory.createLoweredBevelBorder());

	plotPanel = new JPanel();
	plot = new WavePlot();
	plotPanel.add(plot);
	plotPanel.setBorder(BorderFactory.createEmptyBorder(5,1,1,1));
	add(plotPanel, BorderLayout.CENTER);

	namePanel = new JPanel();
	nameLb = new JLabel(name);
	namePanel.add(nameLb);
	add(namePanel, BorderLayout.NORTH);

	timePanel = new JPanel();
	timeLb = new JLabel("sec");
	timePanel.add(timeLb);
	add(timePanel, BorderLayout.SOUTH);

	amplitudePanel = new JPanel();
	amplitudeLb = new JLabel("dB");
	amplitudePanel.add(amplitudeLb);
	add(amplitudePanel, BorderLayout.WEST);

    }

    void sum2waves(WaveDisplay w1, WaveDisplay w2) {
	
	// TBD check that sample rates are same
	
	Complex temp1, temp2;
	Complex sum = new Complex();
	
	// TBD maximum values
	plot.xMax = -1e10;
	plot.xMin =  1e10; 
	plot.yMax = -1e10;
	plot.yMin =  1e10;
	
	plot.data.clear();
	
	for (int i=0; i < w1.plotPanel.plot.data.size(); i++) {
	    
	    temp1 = (Complex) w1.plotPanel.plot.data.get(i);
	    temp2 = (Complex) w2.plotPanel.plot.data.get(i);
	    
	    sum.x = temp1.x + temp2.x;
	    sum.y = temp1.y + temp2.y;
	    
	    plot.data.add(i, new Complex (sum.x, sum.y));
	    
	    if (sum.x > plot.xMax) plot.xMax = sum.x;
	    if (sum.x < plot.xMin) plot.xMin = sum.x;
	    if (sum.y > plot.yMax) plot.yMax = sum.y;
	    if (sum.y < plot.yMin) plot.yMin = sum.y;
	    
	}
    }
}

class WavePlot extends JPanel {

    double time  = 10;
    double frequency  = 1;
    double amplitude  = 1;
    double phase = 0;
    double sampleRate = 128;

    Vector data = new Vector();

    double xMax, xMin, yMax, yMin;
    int    height = 100, width = 400;
    Color  fg, bg;

    public WavePlot () {
	setBorder(BorderFactory.createLineBorder(Color.black));
	setPreferredSize(new Dimension(width, height));
	setSize(new Dimension(width, height));
	fg = Color.red;
	bg = Color.white;
    }

    public void paintComponent (Graphics g) {

	super.paintComponent(g);

	double xScale  = 1;
	double yScale  = 1;
	double xOffset = 0;
	double yOffset = 0;

	Graphics2D g2 = (Graphics2D) g;
	Dimension d   = getSize();

	xScale  = d.width  / (xMax - xMin);
	yScale  = d.height / (yMax - yMin);
	yOffset = d.height/2;
	xOffset = -xMin * xScale;

	g2.draw(new Line2D.Double (0, yOffset, d.width, yOffset));

	g2.setPaint(fg);

	Complex p1, p2;

	for (int i = 0; i < data.size() - 1; i++) {

	    p1 = (Complex) data.get(i);
	    p2 = (Complex) data.get(i+1);

	    g2.draw(new Line2D.Double( p1.x * xScale + xOffset, 
				      -p1.y * yScale + yOffset, 
				       p2.x * xScale + xOffset, 
				      -p2.y * yScale + yOffset));
	}
    }

    public void generateSineData() {

	// TBD maximum values
	xMax = -1e10;
	xMin =  1e10; 
	yMax = -1e10;
	yMin =  1e10;

	double x1 = 0, y1 = 0;

	data.clear();

	for (int i = 0; x1 < time; i++) {

	    if (sampleRate > 0) {
		x1 += 1/sampleRate;
	    } else {
		return; // TBD invalid sample rate
	    }

	    y1 = amplitude*Math.sin(frequency*x1 + phase);

	    data.add(i, new Complex(x1, y1));

	    if (x1 > xMax) xMax = x1;
	    if (x1 < xMin) xMin = x1;
	    if (y1 > yMax) yMax = y1;
	    if (y1 < yMin) yMin = y1;

	}
    }
}

class LissajousDisplay extends JPanel {

    LissajousPlotPanel plotPanel;

    public LissajousDisplay () {

	plotPanel = new LissajousPlotPanel();
	add(plotPanel);
    }

}

class LissajousPlotPanel extends JPanel {

    JPanel        plotPanel;
    LissajousPlot plot;

    int height = 150, width = 153;

    public LissajousPlotPanel () {

	setLayout(new BorderLayout());
	setBorder(BorderFactory.createRaisedBevelBorder());
	setPreferredSize(new Dimension(width, height));
	setSize(new Dimension(width, height));

	plotPanel = new JPanel();

	plotPanel.add(new JLabel("Lissajous"));

	plot      = new LissajousPlot();
	plotPanel.add(plot);
	plotPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	add(plotPanel, BorderLayout.CENTER);

    }

    void sum2waves(WaveDisplay w1, WaveDisplay w2) {

	// TBD check that sample rates are same
	
	Complex temp1, temp2;
	Complex sum = new Complex();
	
	// TBD maximum values
	plot.xMax = -1e10;
	plot.xMin =  1e10; 
	plot.yMax = -1e10;
	plot.yMin =  1e10;
	
	plot.data.clear();

	for (int i=0; i < w1.plotPanel.plot.data.size(); i++) {
	    
	    temp1 = (Complex) w1.plotPanel.plot.data.get(i);
	    temp2 = (Complex) w2.plotPanel.plot.data.get(i);
	    
	    sum.x = temp1.y;
	    sum.y = temp2.y;
	    
	    plot.data.add(i, new Complex (sum.x, sum.y));
	    
	    if (sum.x > plot.xMax) plot.xMax = sum.x;
	    if (sum.x < plot.xMin) plot.xMin = sum.x;
	    if (sum.y > plot.yMax) plot.yMax = sum.y;
	    if (sum.y < plot.yMin) plot.yMin = sum.y;
	    
	}
    }
}

class LissajousPlot extends JPanel {

    double xMax, xMin, yMax, yMin;
    int    height = 100, width = 100;
    Color  fg, bg;

    Vector data = new Vector();


    public LissajousPlot () {
	setBorder(BorderFactory.createLineBorder(Color.black));
	setPreferredSize(new Dimension(width, height));
	setSize(new Dimension(width, height));
	fg = Color.green;
	bg = Color.black;
	setBackground(bg);
    }

    public void paintComponent (Graphics g) {

	super.paintComponent(g);

	double xScale  = 1;
	double yScale  = 1;
	double xOffset = 0;
	double yOffset = 0;


	Graphics2D g2 = (Graphics2D) g;
	Dimension d   = getSize();

	xScale  = d.width  / (xMax - xMin);
	yScale  = d.height / (yMax - yMin);
	yOffset = d.height/2;
	xOffset = -xMin * xScale;

	g2.setPaint(fg);

	Complex p1, p2;

	for (int i = 0; i < data.size() - 1; i++) {

	    p1 = (Complex) data.get(i);
	    p2 = (Complex) data.get(i+1);

	    g2.draw(new Line2D.Double( p1.x * xScale * 0.9 + xOffset, 
				      -p1.y * yScale * 0.9 + yOffset, 
				       p2.x * xScale * 0.9 + xOffset, 
				      -p2.y * yScale * 0.9 + yOffset));
	}
    }
}

class Complex {

    public double x = 0, y = 0;

    Complex () {
	x = 0; y = 0;
    }

    Complex (Complex a) {
	x = a.x; y = a.y;
    }

    Complex (double a, double b) {
	x = a; y = b;
    }
}
