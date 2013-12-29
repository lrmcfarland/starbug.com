// ==========================================================
// Filename:    DecimalEntryPanel.java
// Description: creates decimal entry panel widgets
//              from: http://java.sun.com/docs/books/tutorial/uiswing/components/example-swing/DecimalField.java
//              and from: http://java.sun.com/docs/books/tutorial/uiswing/components/example-swing/FormattedDocument.java
// Authors:     L.R. McFarland
// Language:    java
// Created:     2001-11-1
// ==========================================================

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.text.*;

import java.text.*;

class DecimalEntryPanel extends JPanel {

    double        value;
    JTextField    textField;
    DecimalFormat decimalFormat;


    public DecimalEntryPanel (String name, String units, int columns) {

	// setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	add(new JLabel(name));

	textField = new JTextField(columns);
	decimalFormat = new DecimalFormat();
	textField.setDocument(new FormattedDocument(decimalFormat));
	add(textField);

	add(new JLabel(units));

    }

    public double getValue() {
        double retVal = 0.0;

        try {
            retVal = decimalFormat.parse(textField.getText()).doubleValue();
        } catch (ParseException e) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            // TBD Toolkit.getDefaultToolkit().beep();
            // TBD System.err.println("getValue: could not parse: " + 
	    // TBD 	       textField.getText());
        }
        return retVal;
    }



    public void setValue(double value) {
        textField.setText(decimalFormat.format(value));
    }

}

class FormattedDocument extends PlainDocument {
    private Format format;

    public FormattedDocument(Format f) {
        format = f;
    }

    public Format getFormat() {
        return format;
    }

    public void insertString(int offs, String str, AttributeSet a) 
        throws BadLocationException {

        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(offs, currentText.length());
        String proposedResult = beforeOffset + str + afterOffset;

        try {
            format.parseObject(proposedResult);
            super.insertString(offs, str, a);
        } catch (ParseException e) {
            // TBD Toolkit.getDefaultToolkit().beep();
            // TBD System.err.println("insertString: could not parse: "
	    // TBD + proposedResult);
        }
    }

    public void remove(int offs, int len) throws BadLocationException {
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(len + offs,
                                                   currentText.length());
        String proposedResult = beforeOffset + afterOffset;

        try {
            if (proposedResult.length() != 0)
                format.parseObject(proposedResult);
            super.remove(offs, len);
        } catch (ParseException e) {
            // TBD Toolkit.getDefaultToolkit().beep();
            // TBD System.err.println("remove: could not parse: " + proposedResult);
        }
    }
}

