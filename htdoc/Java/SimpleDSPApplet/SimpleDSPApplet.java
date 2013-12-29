// ==========================================================
// Filename:    SimpleDSPApplet.java
// Description: a simple DSP applet
//
// Authors:     L.R. McFarland
// Language:    java
// Created:     19 August 2003
// ==========================================================
 
import java.applet.Applet;
 
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
 
import javax.swing.*;
import javax.swing.text.*;
 
import java.text.*;
 
import java.lang.Math;
import java.util.Vector;

import ptolemy.plot.*;
 
// -----------------------
// ----- FFT Factory -----
// -----------------------
 
class FFTException extends Exception {
 
    public FFTException () {
        super();
    }
 
    public FFTException (String emsg) {
        super(emsg);
    }
 
}

class FFTFactory {

    Vector timeDomainData;
    Vector fftData;

    public FFTFactory() {}

    public void generate(double time, double sampleRate,
			 TimeDomainParametersPanel tdParameters,
			 FFTParametersPanel fftParameters)
	throws FFTException {

	timeDomainData = new Vector();

	double val         = 0;
	double currentTime = 0;

	if (sampleRate == 0) {
	    throw new FFTException("Error SignalPlot::updateDisplay( ):" +
				   " sample rate == 0");
	}

	// ----- time domain data -----

	for (int i = 0; currentTime < time; i++) {

	    currentTime = (double)i / sampleRate;

	    // TBD is this in radians?

	    val = tdParameters.amplitude() * 
		Math.sin(2.0*Math.PI*tdParameters.frequency() * 
			 currentTime + tdParameters.phase());

	    timeDomainData.add(i, new XY(currentTime, val));

	}

	// ----- frequency data -----

	int fft_size = (int) 
	    Math.pow(2, fftParameters.fftSizeCB.getSelectedIndex() + 2);

        if (fft_size > timeDomainData.size()) {
	    throw new FFTException("Error SignalPlot::FFT( ):" +
				   " not enough time sampled" +
				   " data for this fft size.");
        }

	FFT(timeDomainData, fft_size, 
	    fftParameters.fftWindowCB.getSelectedIndex());

    }

    public Vector getTimeDomainData() {
	return(timeDomainData);
    }

    public Vector getFrequencyDomainData() {
	return(fftData);
    }


    // FFT "adapted" from: 
    // http://sepwww.stanford.edu/oldsep/hale/FftLab.html
    // who seems to have adapted it from "Numerical Recipes in C"

    void FFT (Vector time_data, int fft_size, int fft_window) 
	throws FFTException {


	fftData = new Vector();

        for (int i = 0; i < fft_size; i++) {
            XY xyTemp = (XY) time_data.get(i);
            fftData.add(i, new Complex(xyTemp.y, 0));
        }

	
        // Apply Window

        if (fft_window == 1) {
            Blackman(fftData);
        }

        if (fft_window == 2) {
            Kaiser(fftData);
        }

            
        // bit-reversal section
        for (int i = 0, j = 0; i < fftData.size(); ++i) {
            if (j >= i) {
                Complex cTemp = (Complex) fftData.get(j);
                fftData.set(j, fftData.get(i));
                fftData.set(i, cTemp);
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

                        Complex ciTemp = (Complex) fftData.get(i);
                        Complex cjTemp = (Complex) fftData.get(j);

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

    void Blackman (Vector fData) throws FFTException {

        double a0 = 0.35875;
        double a1 = 0.48829;
        double a2 = 0.14128;
        double a3 = 0.01168;
        double two_pi_n, argfn;

        if (fData.size() == 0) {
	    throw new FFTException("Error SignalPlot::Blackman( ):" +
				   " FFT data size == 0");
        }

        two_pi_n = 2.0*Math.PI/(double)fData.size();

        for (int i = 0; i < fData.size(); i++) {
            Complex cTemp = (Complex) fData.get(i);

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

    void Kaiser (Vector fData) throws FFTException {

        double alpha;
        double ak, denom;
        int    i, j;

        double[] kbWindow = new double[fData.size()];

        alpha = 3.5;
        denom = bessm(Math.PI*alpha);

	if (fData.size() == 0) {
	    throw new FFTException("Error SignalPlot::Kaiser( ):" +
				   " denom == 0");
        }

        // ----- generate coefficients -----

        for (i = 0; i <= fData.size()/2; i++) {
            ak = (double)i/((double)fData.size()/2.0);
            kbWindow[fData.size()/2 - i] = 
                bessm(Math.PI*alpha*Math.sqrt(1.0-ak*ak))/denom;
        }


        for (i = fData.size()/2, j = i; i < fData.size(); i++, j--) {
            kbWindow[i] = kbWindow[j];
        }


        // ----- apply window -----
        for (i = 0; i < fData.size(); i++) {
            Complex cTemp = (Complex) fData.get(i);

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

// ----------------------------------
// ----- Decimal Entry Elements -----
// -----------------------------=----
 
class DecimalEntryElements {
 
    JTextField entryValue;
 
    DecimalEntryElements(String name, String units, int width, JPanel panel,
                         GridBagLayout gbl, GridBagConstraints gbc) {
 
        JLabel entryLabel = new JLabel(name);
        gbl.setConstraints(entryLabel, gbc);
        panel.add(entryLabel);
 
        entryValue = new JTextField(width);
        gbc.gridx += 1;
        gbl.setConstraints(entryValue, gbc);
        panel.add(entryValue);
 
        JLabel entryUnits = new JLabel(units);
        gbc.gridx += 1;
        gbl.setConstraints(entryUnits, gbc);
        panel.add(entryUnits);
 
    }
 
    public double value() {
        double val = 0.0;
        try {
            val = Double.parseDouble(entryValue.getText());
        } catch (NumberFormatException e) {
            // TBD error dialog
        }
        return(val);
    }

    public void value(double val) {
        entryValue.setText(Double.toString(val));
    }

}

class DecimalEntryPanel extends JPanel {

    DecimalEntryElements entryElements;

    DecimalEntryPanel(String name, String units, int width) {

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;

        entryElements = new DecimalEntryElements(name, units, width, this,
                                                 gbl, gbc);

    }

    public double value() {
        return(entryElements.value());
    }

    public void value(double val) {
        entryElements.value(val);
    }

}

class TimeDomainParametersPanel extends JPanel {

    DecimalEntryElements frequencyDEE;
    DecimalEntryElements amplitudeDEE;
    DecimalEntryElements phaseDEE;

    public TimeDomainParametersPanel () {

	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);

	GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
	gbc.insets = new Insets(2, 4, 2, 4);

	JLabel timeDomainLabel = new JLabel("Signal Parameters");
	gbc.gridwidth = 3;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbl.setConstraints(timeDomainLabel, gbc);
	add(timeDomainLabel);
	gbc.gridwidth = 1; // restore default

	gbc.gridx = 0;
	gbc.gridy = 1;
	frequencyDEE = new DecimalEntryElements("Frequency", "Hz", 5, this,
						gbl, gbc);

	gbc.gridx = 0;
	gbc.gridy = 2;
	amplitudeDEE = new DecimalEntryElements("Amplitude", "Volts", 5, this,
						gbl, gbc);

	gbc.gridx = 0;
	gbc.gridy = 3;
	phaseDEE = new DecimalEntryElements("Phase", "radians", 5, this,
					    gbl, gbc);

    }

    public double frequency() {
	return(frequencyDEE.value());
    }

    public void frequency(double val) {
	frequencyDEE.value(val);
    }

    public double amplitude() {
	return(amplitudeDEE.value());
    }

    public void amplitude(double val) {
	amplitudeDEE.value(val);
    }

    public double phase() {
	return(phaseDEE.value());
    }

    public void phase(double val) {
	phaseDEE.value(val);
    }

}

class FFTParametersPanel extends JPanel {

    JComboBox fftSizeCB;
    JComboBox fftWindowCB;
    JComboBox fftTypeCB;
 
    public FFTParametersPanel () {

	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);

	GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
	gbc.insets = new Insets(2, 4, 2, 4);

	JLabel fftLabel = new JLabel("FFT Parameters");
	gbc.gridwidth = 3;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbl.setConstraints(fftLabel, gbc);
	add(fftLabel);
	gbc.gridwidth = 1; // restore default

	// ----- size -----
	JLabel fftSizeLabel = new JLabel("Size");
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbl.setConstraints(fftSizeLabel, gbc);
	add(fftSizeLabel);

        String[] fft_sizes = { "4", "8", "16", "32", "64", "128", "256",
                               "512", "1024", "2048", "4096" };
        fftSizeCB = new JComboBox(fft_sizes);
        fftSizeCB.setSelectedIndex(4);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(fftSizeCB, gbc);
        add(fftSizeCB);


	// ----- window -----
	JLabel fftWindowLabel = new JLabel("Window");
	gbc.gridx = 0;
	gbc.gridy = 2;
	gbl.setConstraints(fftWindowLabel, gbc);
	add(fftWindowLabel);

        String[] fft_windows = { "None", "Blackman", "Kaiser"};

        fftWindowCB = new JComboBox(fft_windows);
        fftWindowCB.setSelectedIndex(2);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbl.setConstraints(fftWindowCB, gbc);
        add(fftWindowCB);


	// ----- type -----
	JLabel fftTypeLabel = new JLabel("Type");
	gbc.gridx = 0;
	gbc.gridy = 3;
	gbl.setConstraints(fftTypeLabel, gbc);
	add(fftTypeLabel);

        String[] fft_types = { "Raw", "PSD"};
        fftTypeCB = new JComboBox(fft_types);
        fftTypeCB.setSelectedIndex(1);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbl.setConstraints(fftTypeCB, gbc);
        add(fftTypeCB);

    }


}


class SignalPlot {

    TimeDomainParametersPanel tdParameters  = new TimeDomainParametersPanel();
    Plot                      tdPlot;

    FFTParametersPanel        fftParameters = new FFTParametersPanel();;
    Plot                      fftPlot;

    FFTFactory                fftFactory    = new FFTFactory();

    public SignalPlot(String name, Container cp, GridBagLayout gbl, 
		      GridBagConstraints gbc) {

	JLabel signalLabel = new JLabel(name);
	gbl.setConstraints(signalLabel, gbc);
	cp.add(signalLabel);

	gbc.gridy += 1;

	// ----- time domain -----

	gbl.setConstraints(tdParameters, gbc);
	cp.add(tdParameters);

	tdPlot = new Plot();
        tdPlot.setTitle("Time Domain");
        tdPlot.setMarksStyle("none");
        tdPlot.setXLabel("Seconds");
        tdPlot.setYLabel("Volts");
        tdPlot.setSize(300, 200);

	gbc.gridx += 1;
	gbl.setConstraints(tdPlot, gbc);
	cp.add(tdPlot);

	// ----- FFT -----

	gbc.gridx += 1;
	gbl.setConstraints(fftParameters, gbc);
	cp.add(fftParameters);

	fftPlot = new Plot();
        fftPlot.setTitle("Frequency Domain");
        fftPlot.setMarksStyle("none");
        fftPlot.setXLabel("Hz");
        fftPlot.setYLabel("dB");
        fftPlot.setSize(300, 200);

	gbc.gridx += 1;
	gbl.setConstraints(fftPlot, gbc);
	cp.add(fftPlot);


    }


    public void updateDisplay (double time, double sampleRate) 
	throws FFTException {

	int    dataSet     = 0;
	tdPlot.clear(dataSet);


	fftFactory.generate(time, sampleRate, tdParameters, fftParameters);

	// ----- plot time domain data -----

	Vector timeData = fftFactory.getTimeDomainData();

	for (int i = 0; i < timeData.size(); i++) {

	    XY temp = (XY) timeData.get(i);

	    tdPlot.addPoint(dataSet, temp.x, temp.y, true);

	}

	tdPlot.fillPlot();

	// ----- frequency data -----

	Vector fftData = fftFactory.getFrequencyDomainData();

	fftPlot.clear(0);
	fftPlot.clear(1);
	
	
	if (fftParameters.fftTypeCB.getSelectedIndex() == 0) {
	
	    // RAW
	    for (int i = 0; i < fftData.size(); i++) {
		Complex cTemp = (Complex) fftData.get(i);
		fftPlot.addPoint(0, i, cTemp.r, true);
		fftPlot.addPoint(1, i, cTemp.i, true);
	    }
	
	} else {
	
	    // PSD
	    for (int i = 0; i < fftData.size()/2; i++) {
	
		Complex cTemp = (Complex) fftData.get(i);
	
		double dTemp = 10.0 * Math.log(cTemp.r*cTemp.r + 
					       cTemp.i*cTemp.i);
	
		fftPlot.addPoint(0, 
				 ((double) i / ((double) fftData.size() - 1))
				 * sampleRate, 
				 dTemp, true);
	
	    }
	
	}

	fftPlot.fillPlot();

    }

}
 
// ----------------------------------
// ----- simple DSP application -----
// ----------------------------------
 

public class SimpleDSPApplet extends JApplet {


    // ----- general parameters -----
 
    DecimalEntryPanel timeDisplay;
    DecimalEntryPanel sampleRateDisplay;


    // ----- signals display -----

    SignalPlot signalA;
    SignalPlot signalB;
 
    Vector             abData;
    Plot               abTimePlot;
    FFTParametersPanel abFFTParameters = new FFTParametersPanel();
    Plot               abFFTPlot;
 
    FFTFactory fftFactory = new FFTFactory();

    // ----- control -----
  
    JRadioButton  addRB;
    static String addST = new String("Addition");
 
    JRadioButton  multiplyRB;
    static String multiplyST = new String("Multiplication");
 
    JButton updateBT;


    // ----- constructor -----

    public void init() {
 
        Container cp = getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        cp.setLayout(gbl);
 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(2, 4, 2, 4);
 

        // ----- time -----
 
        timeDisplay = new DecimalEntryPanel("Time", "sec", 5);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbl.setConstraints(timeDisplay, gbc);
        cp.add(timeDisplay);

 
        // ----- sample rate -----
 
        sampleRateDisplay =
            new DecimalEntryPanel("Sample Rate", "Hz", 5);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbl.setConstraints(sampleRateDisplay, gbc);
        cp.add(sampleRateDisplay);
 

        // ----- signals -----

	gbc.gridx = 0;
	gbc.gridy = 1;
	signalA = new SignalPlot("Signal A", cp, gbl, gbc);


	gbc.gridx = 0;
	gbc.gridy = 3;
	signalB = new SignalPlot("Signal B", cp, gbl, gbc);


	abTimePlot = new Plot();
        abTimePlot.setTitle("Time Domain");
        abTimePlot.setMarksStyle("none");
        abTimePlot.setXLabel("Seconds");
        abTimePlot.setYLabel("Volts");
        abTimePlot.setSize(300, 200);

	gbc.gridx = 1;
	gbc.gridy = 5;
	gbl.setConstraints(abTimePlot, gbc);
	cp.add(abTimePlot);


	gbc.gridx = 2;
	gbc.gridy = 5;
	gbl.setConstraints(abFFTParameters, gbc);
	cp.add(abFFTParameters);


	abFFTPlot = new Plot();
        abFFTPlot.setTitle("Frequency Domain");
        abFFTPlot.setMarksStyle("none");
        abFFTPlot.setXLabel("Hz");
        abFFTPlot.setYLabel("dB");
        abFFTPlot.setSize(300, 200);

	gbc.gridx = 3;
	gbc.gridy = 5;
	gbl.setConstraints(abFFTPlot, gbc);
	cp.add(abFFTPlot);


        // --------------------
        // ----- controls -----
        // --------------------

	// ----- combine signals -----
	JPanel combinePanel = new JPanel();

	combinePanel.setLayout(new BoxLayout(combinePanel, BoxLayout.Y_AXIS));

	JLabel combineLabel = new JLabel("Combine Signals by");

	addRB = new JRadioButton(addST);
	addRB.setActionCommand(addST);
	addRB.setSelected(true);

	multiplyRB = new JRadioButton(multiplyST);
	multiplyRB.setActionCommand(multiplyST);

	ButtonGroup combineBG = new ButtonGroup();
	combineBG.add(addRB);
	combineBG.add(multiplyRB);


	combinePanel.add(combineLabel);
	combinePanel.add(addRB);
	combinePanel.add(multiplyRB);

	gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 5;
	gbl.setConstraints(combinePanel, gbc);
        cp.add(combinePanel);
	gbc.gridwidth = 1;



	try {

	    timeDisplay.value(1);
	    sampleRateDisplay.value(100);

	    signalA.tdParameters.frequency(5);
	    signalA.tdParameters.amplitude(1);
	    signalA.updateDisplay(timeDisplay.value(), 
				  sampleRateDisplay.value());

	    signalB.tdParameters.frequency(15);
	    signalB.tdParameters.amplitude(1);
	    signalB.updateDisplay(timeDisplay.value(), 
				  sampleRateDisplay.value());

	    combine();


	} catch (FFTException emsg) {
	    JOptionPane.showMessageDialog(null, emsg);
	}


        // ----- update -----
        updateBT = new JButton("Update");
        updateBT.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

		    try {

			signalA.updateDisplay(timeDisplay.value(),
					      sampleRateDisplay.value());
			
			signalB.updateDisplay(timeDisplay.value(),
					      sampleRateDisplay.value());


			combine();


		    } catch (FFTException emsg) {
			JOptionPane.showMessageDialog(null, emsg);
		    }


                }
            });

	gbc.gridwidth = 5;
        gbc.gridx = 0;
        gbc.gridy = 10;
	gbl.setConstraints(updateBT, gbc);
        cp.add(updateBT);
	gbc.gridwidth = 1;


 
    }



    public void combine() throws FFTException {

	int    dataSet  = 0;
	Vector sigAdata = signalA.fftFactory.getTimeDomainData();
	Vector sigBdata = signalB.fftFactory.getTimeDomainData();

	abData = new Vector();

	// TBD check A and B are same size

	abTimePlot.clear(dataSet);

	// ----- time data -----

	for (int i = 0; i < sigAdata.size(); i++) {

	    XY tempA = (XY) sigAdata.get(i);
	    XY tempB = (XY) sigBdata.get(i);

	    XY tempAB;

	    // TBD multiply

	    if (addRB.isSelected()) {

		tempAB = new XY(tempA.x, tempA.y + tempB.y);

	    } else {

		double y1 = 0.5*Math.cos(tempA.y - tempB.y) -
		    0.5*Math.cos(tempA.y + tempB.y);

		tempAB = new XY(tempA.x, y1);


	    }


	    abData.add(i, tempAB);

	    abTimePlot.addPoint(dataSet, tempAB.x, tempAB.y, true);

	}

	abTimePlot.fillPlot();

	// ----- frequency data -----

	int fft_size = (int) 
	    Math.pow(2, abFFTParameters.fftSizeCB.getSelectedIndex() + 2);

        if (fft_size > abData.size()) {
	    throw new FFTException("Error SimpleDSP::combine( ):" +
				   " not enough time sampled" +
				   " data for this fft size.");
        }

	fftFactory.FFT(abData, fft_size, 
		       abFFTParameters.fftWindowCB.getSelectedIndex());


	Vector fftData = fftFactory.getFrequencyDomainData();

	abFFTPlot.clear(0);
	abFFTPlot.clear(1);
	
	
	if (abFFTParameters.fftTypeCB.getSelectedIndex() == 0) {
	
	    // RAW
	    for (int i = 0; i < fftData.size(); i++) {
		Complex cTemp = (Complex) fftData.get(i);
		abFFTPlot.addPoint(0, i, cTemp.r, true);
		abFFTPlot.addPoint(1, i, cTemp.i, true);
	    }
	
	} else {
	
	    // PSD
	    for (int i = 0; i < fftData.size()/2; i++) {
	
		Complex cTemp = (Complex) fftData.get(i);
	
		double dTemp = 10.0 * Math.log(cTemp.r*cTemp.r + 
					       cTemp.i*cTemp.i);
	
		abFFTPlot.addPoint(0, 
				   ((double) i / ((double) fftData.size() - 1))
				   * sampleRateDisplay.value(), 
				   dTemp, true);
	
	    }
	
	}

	abFFTPlot.fillPlot();

    }


}

// -----------------------------
// ----- auxiliary classes -----
// -----------------------------

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
