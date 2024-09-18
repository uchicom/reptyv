package com.uchicom.reptyv;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.uchicom.ui.ResumeDialog;
import com.uchicom.ui.ResumeFrame;

public class ParameterEditor extends ResumeDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea textArea;

	public ParameterEditor(ResumeFrame repptyViewer, JTextArea textArea) {
		super(repptyViewer, "parameter.editor");
		this.textArea = textArea;
		initComponents();
	}

	private void initComponents() {
		setTitle("Parameter");
		getContentPane().add(new JScrollPane(textArea));
		pack();
	}
}
