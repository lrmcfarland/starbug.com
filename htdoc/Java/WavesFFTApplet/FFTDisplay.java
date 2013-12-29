// ==========================================================
// Filename:    FFTDisplay.java
// Description: plots frequency domain data of waves
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

class FFTDisplay extends JPanel {

    JPanel            parameterPanel;

    JPanel            fft_parameterLbPanel;
    JLabel            fft_parameterLb;

    JPanel            fft_sizePanel;
    JLabel            fft_sizeLb;
    JComboBox         fft_sizeCB;

    JPanel            fft_windowPanel;
    JLabel            fft_windowLb;
    JComboBox         fft_windowCB;

    JPanel            fft_typePanel;
    JLabel            fft_typeLb;
    JComboBox         fft_typeCB;

    FFTPlotPanel      fftPlotPanel;



    public FFTDisplay(String name) {

	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setBorder(BorderFactory.createRaisedBevelBorder());

	// ---------------------------
	// ----- parameter panel -----
	// ---------------------------

	parameterPanel = new JPanel();
	parameterPanel.setLayout(new BoxLayout(parameterPanel, 
					       BoxLayout.Y_AXIS));

	parameterPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

	fft_parameterLbPanel = new JPanel();
	fft_parameterLb      = new JLabel("FFT Parameters:");

	fft_parameterLbPanel.add(fft_parameterLb);
	
	parameterPanel.add(fft_parameterLbPanel);


	// ----- fft size -----
	fft_sizePanel = new JPanel();

	fft_sizeLb    = new JLabel("Size:     ");

	fft_sizePanel.add(fft_sizeLb);

	String[] fft_sizes = { "4", "8", "16", "32", "64", "128", "512", 
			       "1024", "2048", "4096" };
	fft_sizeCB = new JComboBox(fft_sizes);
	fft_sizeCB.setSelectedIndex(3);
	fft_sizePanel.add(fft_sizeCB);

	parameterPanel.add(fft_sizePanel);

	// ----- fft window -----
	fft_windowPanel = new JPanel();

	fft_windowLb    = new JLabel("Window: ");

	fft_windowPanel.add(fft_windowLb);

	// Create combo box with lunar phase choices.
	String[] fft_windows = { "None", "Blackman", "Kaiser"};
	fft_windowCB = new JComboBox(fft_windows);
	fft_windowCB.setSelectedIndex(1);
	fft_windowPanel.add(fft_windowCB);

	parameterPanel.add(fft_windowPanel);

	// ----- fft type -----
	fft_typePanel = new JPanel();

	fft_typeLb    = new JLabel("Type:    ");

	fft_typePanel.add(fft_typeLb);

	// Create combo box with lunar phase choices.
	String[] fft_types = { "Raw", "PSD"};
	fft_typeCB = new JComboBox(fft_types);
	fft_typeCB.setSelectedIndex(1);
	fft_typePanel.add(fft_typeCB);

	parameterPanel.add(fft_typePanel);

	add(parameterPanel);

	// ----------------
	// ----- plot -----
	// ----------------

	fftPlotPanel = new FFTPlotPanel(name);
	add(fftPlotPanel);

    }


    void updateDisplay (Vector time_data) {
	fftPlotPanel.fftPlot.fft_size = 
	    (int) Math.pow(2, fft_sizeCB.getSelectedIndex() + 2);

	fftPlotPanel.fftPlot.fft_window = 
	    (String) fft_windowCB.getSelectedItem();

	fftPlotPanel.fftPlot.fft_type = 
	    (String) fft_typeCB.getSelectedItem();

	fftPlotPanel.fftPlot.FFT(time_data);
	fftPlotPanel.fftPlot.repaint();
    }


}

class FFTPlotPanel extends JPanel {

    JPanel   namePanel;
    JLabel   nameLb;

    JPanel   frequencyPanel; // to get label to center
    JLabel   frequencyLb;

    JPanel   amplitudePanel; // to get label to center
    JLabel   amplitudeLb;

    JPanel   plotPanel;
    FFTPlot  fftPlot;

    public FFTPlotPanel (String name) {

	setLayout(new BorderLayout());
	setBorder(BorderFactory.createLoweredBevelBorder());

	plotPanel = new JPanel();
	fftPlot   = new FFTPlot();
	plotPanel.add(fftPlot);
	plotPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	add(plotPanel, BorderLayout.CENTER);

	namePanel = new JPanel();
	nameLb = new JLabel(name);
	namePanel.add(nameLb);
	add(namePanel, BorderLayout.NORTH);

	frequencyPanel = new JPanel();
	frequencyLb = new JLabel("Hz");
	frequencyPanel.add(frequencyLb);
	add(frequencyPanel, BorderLayout.SOUTH);

	amplitudePanel = new JPanel();
	amplitudeLb = new JLabel("dB");
	amplitudePanel.add(amplitudeLb);
	add(amplitudePanel, BorderLayout.WEST);

    }

}


class FFTPlot extends JPanel {

    int    fft_size = 16; // must be a power of 2
    String fft_window;
    String fft_type;


    Vector fft_buffer = new Vector();;

    Vector psd = new Vector();

    double xMax, xMin, yMax, yMin;
    int    height = 100, width = 200;
    Color  fg, fg1, fg2;
    Color  bg;

    public FFTPlot () {

	fg  = Color.black;
	fg1 = Color.red;
	fg2 = Color.blue;
	bg  = Color.black;
	// setBackground(bg);

	setBorder(BorderFactory.createLineBorder(fg));
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
	Complex p1, p2;


	// PSD reads "backwards" because of the way four1() outputs its data
	// this reordering will not allow the inverse fft to work correctly


	// PSD plot
	if (fft_type == "PSD") {

	    psd.clear();

	    for (int i = 0, j = 0; i < fft_buffer.size(); i++) {

		p1 = (Complex) fft_buffer.get(i);

		if (i < fft_buffer.size()/2) {
		    psd.add(j++, new Complex(Math.log(p1.r*p1.r + p1.i*p1.i), 
					     0));
		} 

		/*

		psd.add(i, new Complex(Math.log(p1.r*p1.r), 
				       Math.log(p1.i*p1.i)));

		*/

	    }


	    if (psd.size() > 0) {
		xMax = psd.size()/2;
	    }
	    
	    xMin = 0;
	    yMax = Double.MIN_VALUE;
	    yMin = Double.MAX_VALUE;
	    
	    for (int i = 0; i < psd.size(); i++) {
		Complex cTemp = (Complex) psd.get(i);
		if (cTemp.r > yMax) yMax = cTemp.r;
		if (cTemp.r < yMin) yMin = cTemp.r;
		if (cTemp.i > yMax) yMax = cTemp.i;
		if (cTemp.i < yMin) yMin = cTemp.i;
	    }

	    xScale  = d.width  / (xMax - xMin); // Assumes xMin > 0
	    yScale  = d.height / (yMax - yMin); // Assumes yMin > 0
	    yOffset = d.height;
	    xOffset = -xMin * xScale;

	    for (int i = 0; i < psd.size() - 1; i++) {

		p1 = (Complex) psd.get(i);

		g2.setPaint(fg1);

		/* 
		p2 = (Complex) psd.get(i + 1);


		g2.draw(new Line2D.Double( i      * xScale + xOffset, 
					   -p1.r  * yScale + yOffset, 
					   (i +1) * xScale + xOffset, 
					   -p2.r   * yScale + yOffset));
		*/


		g2.draw(new Line2D.Double( i      * xScale + xOffset, 
					   yOffset, 
					   i      * xScale + xOffset, 
					  -p1.r   * yScale + yOffset));

	    }


	}

	// Raw FFT plot
	if (fft_type == "Raw") {

	    if (fft_size > 0) {
		xMax = fft_size;
	    }
	    
	    xMin = 0;
	    yMax = Double.MIN_VALUE;
	    yMin = Double.MAX_VALUE;
	    
	    for (int i = 0; i < fft_buffer.size(); i++) {
		Complex cTemp = (Complex) fft_buffer.get(i);
		if (cTemp.r > yMax) yMax = cTemp.r;
		if (cTemp.r < yMin) yMin = cTemp.r;
		if (cTemp.i > yMax) yMax = cTemp.i;
		if (cTemp.i < yMin) yMin = cTemp.i;
	    }
	    
	    xScale  = d.width  / (xMax - xMin); // Assumes xMin > 0
	    yScale  = d.height / (yMax - yMin); // Assumes yMin < 0
	    yOffset = d.height/2;
	    xOffset = -xMin * xScale;
	    
	    g2.setPaint(fg);
	    g2.draw(new Line2D.Double (0, yOffset, d.width, yOffset));
	    
	    for (int i = 0; i < fft_buffer.size() - 1; i++) {
		
		p1 = (Complex) fft_buffer.get(i);
		p2 = (Complex) fft_buffer.get(i+1);
		
		g2.setPaint(fg1);
		g2.draw(new Line2D.Double( i      * xScale + xOffset, 
					   -p1.r   * yScale + yOffset, 
					   (i + 1) * xScale + xOffset, 
					   -p2.r   * yScale + yOffset));


		g2.setPaint(fg2);
		g2.draw(new Line2D.Double( i      * xScale + xOffset, 
					   -p1.i   * yScale + yOffset, 
					   (i + 1) * xScale + xOffset, 
					   -p2.i   * yScale + yOffset));


		
	    }

	}

    }

    // FFT "adapted" from: 
    // http://sepwww.stanford.edu/oldsep/hale/FftLab.html
    // who seems to have adapted it from "Numerical Recipes in C"

    void FFT (Vector time_data) {

	fft_buffer.clear();

	if (fft_size > time_data.size()) {
	    return; 	// TBD error message, not enough time sampled data
	}

	for (int i = 0; i < fft_size; i++) {
	    XY xyTemp = (XY) time_data.get(i);
	    fft_buffer.add(i, new Complex(xyTemp.y, 0)); // TBD x data?
	}

	// Apply Window

	if (fft_window == "Blackman") {
	    Blackman();
	}

	if (fft_window == "Kaiser") {
	    Kaiser();
	}

	    
	// bit-reversal section
	for (int i = 0, j = 0; i < fft_buffer.size(); ++i) {
	    if (j >= i) {
		Complex cTemp = (Complex) fft_buffer.get(j);
		fft_buffer.set(j, fft_buffer.get(i));
		fft_buffer.set(i, cTemp);
	    }
	    int m = fft_size/2;
            while (m >= 1 && j >= m) {
		j -= m;
		m /= 2;
            }
            j += m;
	}

	boolean reverse = false; // TBD move to interface

	// Danielson-Lanczos formula
	double delta;
	int mmax, istep;
        for (mmax=1,istep=2*mmax; mmax<fft_size; mmax=istep,istep=2*mmax) {
	    if (reverse) {
		delta = Math.PI/(double)mmax;
	    } else {
		delta = -Math.PI/(double)mmax;
	    }
            for (int m=0; m<mmax; ++m) {
	            double w  = m*delta;
	            double wr = Math.cos(w);
	            double wi = Math.sin(w);
	            for (int i = m; i < fft_size; i += istep) {
	                int j = i + mmax;

			Complex ciTemp = (Complex) fft_buffer.get(i);
			Complex cjTemp = (Complex) fft_buffer.get(j);

			double tr = wr*cjTemp.r - wi*cjTemp.i;
			double ti = wr*cjTemp.i + wi*cjTemp.r;

			cjTemp.r  = ciTemp.r - tr;
			cjTemp.i  = ciTemp.i - ti;

			ciTemp.r += tr;
			ciTemp.i += ti;

	            }

            }

            mmax = istep;

        }

	// TBD print vector
	if (false) {
	    for (int i = 0; i < fft_buffer.size(); i++) {
		System.out.println("fft_buffer[" + i + "] = " + 
				   fft_buffer.get(i));
	    }
	    System.out.println("");
	}


    }


    /* ===================================================================
     * Function:    genBHwin()
     * Description: generates Blackman-Harris fft window
     *              This program generates a minimum 4-term Blackman-Harris 
     *              window for a FFT with n points. Details taken from the 
     *              paper "On the Use of Windows for Harmonic Analysis With 
     *              the Discrete Fourier Transform" by Fred Harris. Proc. 
     *              IEEE Jan 1978.  pp 64-65.
     * Authors:     R. Sherman
     * Created:     April 24, 1993
     * Language:    C
     * ===================================================================
     */

    void Blackman () {

	double a0 = 0.35875;
	double a1 = 0.48829;
	double a2 = 0.14128;
	double a3 = 0.01168;
	double two_pi_n, argfn;

	two_pi_n = 2.0*Math.PI/(double)fft_buffer.size();

	for (int i = 0; i < fft_buffer.size(); i++) {
	    Complex cTemp = (Complex) fft_buffer.get(i);

	    argfn = ((double)i)*two_pi_n;

	    double dTemp = 
		a0 - a1*Math.cos(argfn) + a2*Math.cos(2.0*argfn) -
		a3*Math.cos(3.0*argfn);

	    cTemp.r *= dTemp;
	    cTemp.i *= dTemp;

	}

    }

    /* ===================================================================
     * Function:    genKBwin()
     * Description: generates Kaiser Bessel fft window
     *
     * Authors:     R. Sherman, L.R. McFarland
     * Created:     April 20, 1993
     * Modified:    Mon Apr 17 17:02:20 PDT 1995 to fix overwrite problem
     * Language:    C
     * ===================================================================
     */

    void Kaiser () {

	double alpha;
	double ak, denom;
	int    i, j;

	double[] kbWindow = new double[fft_buffer.size()];

	alpha = 3.5;
	denom = bessm(Math.PI*alpha);

	// ----- generate coefficients -----

	for (i = 0; i <= fft_buffer.size()/2; i++) {
	    ak = (double)i/((double)fft_buffer.size()/2.0);
	    kbWindow[fft_buffer.size()/2 - i] = 
		bessm(Math.PI*alpha*Math.sqrt(1.0-ak*ak))/denom;
	}


	for (i = fft_buffer.size()/2, j = i; i < fft_buffer.size(); i++, j--) {
	    kbWindow[i] = kbWindow[j];
	}


	// ----- apply window -----
	for (i = 0; i < fft_buffer.size(); i++) {
	    Complex cTemp = (Complex) fft_buffer.get(i);

	    cTemp.r *= kbWindow[i];
	    cTemp.i *= kbWindow[i];

	}

	
    }

    /* ===================================================================
     * Function:    bessm()
     * Description: generates bessel coefficient for Kaiser Bessel fft window
     *
     * Authors:     R. Sherman
     * Created:     April 20, 1993
     * Language:    C, Motif
     * ===================================================================
     */
    
    double bessm(double x) {

	double t, tm, t2, b;
	
	t = x / 3.7500000;
	
	if( x < 3.75000)  {
	    
	    t2 =  t * t;
	    b  = t2 * ( 0.2659732 + t2 * ( 0.0360768 + 0.0045813 * t2));
	    b  = t2 * ( 3.0899424 + t2 * ( 1.2067492 + b));
	    b  = 1.00000 + t2 * ( 3.5156229 + b);
	    
	} else {
	    
	    tm = 1.0000 / t;
	    b  = tm * (  0.02635537 + tm * (-0.01647633 + 0.00392337 * tm));
	    b  = tm * ( -0.00157565 + tm * ( 0.00916281 + tm * 
					     ( -0.02057706 + b)));
	    b  = 0.39894228 + tm * ( 0.01328592 + tm * ( 0.00225319 + b));
	    b  = b / (Math.exp( -x ) * Math.sqrt(x));
	}
	
	return(b);
    }

}
