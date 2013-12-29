// ==========================================================
// Filename:    LissajousDisplay.java
// Description: plots Lissajous figures
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

    int height = 150, width = 150;

    public LissajousPlotPanel () {

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setBorder(BorderFactory.createRaisedBevelBorder());

	setPreferredSize(new Dimension(width, height));
	setSize(new Dimension(width, height));

	plotPanel = new JPanel();
	plotPanel.setLayout(new BoxLayout(plotPanel, BoxLayout.Y_AXIS));

	plotPanel.add(new JLabel("Lissajous"));

	plot      = new LissajousPlot();
	plotPanel.add(plot);
	plotPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	add(plotPanel, BorderLayout.CENTER);

    }

    void sum2waves(WaveDisplay w1, WaveDisplay w2) {

	// TBD check that sample rates are same
	
	XY temp1, temp2;
	XY sum = new XY();
	
	plot.xMax = Double.MIN_VALUE;
	plot.xMin = Double.MAX_VALUE; 
	plot.yMax = Double.MIN_VALUE;
	plot.yMin = Double.MAX_VALUE;
	
	plot.data.clear();

	for (int i=0; i < w1.wavePlotPanel.wavePlot.data.size(); i++) {
	    
	    temp1 = (XY) w1.wavePlotPanel.wavePlot.data.get(i);
	    temp2 = (XY) w2.wavePlotPanel.wavePlot.data.get(i);
	    
	    sum.x = temp1.y;
	    sum.y = temp2.y;
	    
	    plot.data.add(i, new XY (sum.x, sum.y));
	    
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

	XY p1, p2;

	for (int i = 0; i < data.size() - 1; i++) {

	    p1 = (XY) data.get(i);
	    p2 = (XY) data.get(i+1);

	    g2.draw(new Line2D.Double( p1.x * xScale * 0.9 + xOffset, 
				      -p1.y * yScale * 0.9 + yOffset, 
				       p2.x * xScale * 0.9 + xOffset, 
				      -p2.y * yScale * 0.9 + yOffset));
	}
    }
}
