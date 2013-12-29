// ==========================================================
// Filename:    WaveDisplay.java
// Description: plots time domain data of waves
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

class WaveDisplay extends JPanel {

    JPanel            parameterPanel;

    JPanel            wave_parameterLbPanel;
    JLabel            wave_parameterLb;

    DecimalEntryPanel frequencyDEP;
    DecimalEntryPanel amplitudeDEP;
    DecimalEntryPanel phaseDEP;

    WavePlotPanel     wavePlotPanel;

    public WaveDisplay (String name) {

	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setBorder(BorderFactory.createRaisedBevelBorder());

	// ---------------------------
	// ----- parameter panel -----
	// ---------------------------

	parameterPanel = new JPanel();
	parameterPanel.setLayout(new BoxLayout(parameterPanel, 
					       BoxLayout.Y_AXIS));

	parameterPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

	wave_parameterLbPanel = new JPanel();
	wave_parameterLb      = new JLabel("Wave Parameters:");

	wave_parameterLbPanel.add(wave_parameterLb);
	
	parameterPanel.add(wave_parameterLbPanel);

	// ----- frequency -----
	frequencyDEP = new DecimalEntryPanel(" frequency ", "Hz    ", 3);
	frequencyDEP.setValue(2); // default value
	parameterPanel.add(frequencyDEP);

	// ----- amplitude -----
	amplitudeDEP = new DecimalEntryPanel(" amplitude ", "Volts ", 3);
	amplitudeDEP.setValue(1); // default value
	parameterPanel.add(amplitudeDEP);

	// ----- phase -----
	phaseDEP     = new DecimalEntryPanel("   phase   ", "rad.  ", 3);
	phaseDEP.setValue(0); // default value
	parameterPanel.add(phaseDEP);

	add(parameterPanel);

	// ----------------
	// ----- plot -----
	// ----------------

	wavePlotPanel = new WavePlotPanel(name);
	add(wavePlotPanel);

    }

    void updateDisplay (double time, double sample_rate) {
	wavePlotPanel.wavePlot.time       = time;
	wavePlotPanel.wavePlot.sample_rate = sample_rate;
	wavePlotPanel.wavePlot.frequency  = frequencyDEP.getValue();
	wavePlotPanel.wavePlot.amplitude  = amplitudeDEP.getValue();
	wavePlotPanel.wavePlot.phase      = phaseDEP.getValue();
	wavePlotPanel.wavePlot.generateSineData();
	wavePlotPanel.wavePlot.repaint();
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
    WavePlot wavePlot;

    public WavePlotPanel (String name) {

	setLayout(new BorderLayout());
	setBorder(BorderFactory.createLoweredBevelBorder());

	plotPanel = new JPanel();
	wavePlot = new WavePlot();
	plotPanel.add(wavePlot);
	plotPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
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
	amplitudeLb = new JLabel("V");
	amplitudePanel.add(amplitudeLb);
	add(amplitudePanel, BorderLayout.WEST);

    }

    void sum2waves(WaveDisplay w1, WaveDisplay w2) {
	
	// TBD check that sample rates are same
	
	XY temp1, temp2;
	XY sum = new XY();
	

	wavePlot.xMax = Double.MIN_VALUE;
	wavePlot.xMin = Double.MAX_VALUE;
	wavePlot.yMax = Double.MIN_VALUE;
	wavePlot.yMin = Double.MAX_VALUE;
	
	wavePlot.data.clear();
	
	for (int i=0; i < w1.wavePlotPanel.wavePlot.data.size(); i++) {
	    
	    temp1 = (XY) w1.wavePlotPanel.wavePlot.data.get(i);
	    temp2 = (XY) w2.wavePlotPanel.wavePlot.data.get(i);
	    
	    sum.x = temp1.x + temp2.x;
	    sum.y = temp1.y + temp2.y;
	    
	    wavePlot.data.add(i, new XY (sum.x, sum.y));
	    
	    if (sum.x > wavePlot.xMax) wavePlot.xMax = sum.x;
	    if (sum.x < wavePlot.xMin) wavePlot.xMin = sum.x;
	    if (sum.y > wavePlot.yMax) wavePlot.yMax = sum.y;
	    if (sum.y < wavePlot.yMin) wavePlot.yMin = sum.y;
	    
	}
    }
}

class WavePlot extends JPanel {

    double time        = 10;
    double frequency   = 1;
    double amplitude   = 1;
    double phase       = 0;
    double sample_rate = 128;

    Vector data = new Vector();

    double xMax, xMin, yMax, yMin;
    int    height = 100, width = 200;
    Color  fg, bg;

    public WavePlot () {
	fg = Color.red;
	bg = Color.white;

	setBorder(BorderFactory.createLineBorder(Color.black));
	setPreferredSize(new Dimension(width, height));
	setSize(new Dimension(width, height));
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

	XY p1, p2;

	for (int i = 0; i < data.size() - 1; i++) {

	    p1 = (XY) data.get(i);
	    p2 = (XY) data.get(i+1);

	    g2.draw(new Line2D.Double( p1.x * xScale + xOffset, 
				      -p1.y * yScale + yOffset, 
				       p2.x * xScale + xOffset, 
				      -p2.y * yScale + yOffset));
	}
    }

    public void generateSineData() {

	xMax = Double.MIN_VALUE;
	xMin = Double.MAX_VALUE;
	yMax = Double.MIN_VALUE;
	yMin = Double.MAX_VALUE;
	
	double x1 = 0, y1 = 0;

	data.clear();

	for (int i = 0; x1 < time; i++) {

	    if (sample_rate > 0) {
		x1 += 1/sample_rate;
	    } else {
		return; // TBD invalid sample rate
	    }

	    y1 = amplitude*Math.sin(frequency*x1 + phase);

	    data.add(i, new XY(x1, y1));

	    if (x1 > xMax) xMax = x1;
	    if (x1 < xMin) xMin = x1;
	    if (y1 > yMax) yMax = y1;
	    if (y1 < yMin) yMin = y1;

	}
    }
}
