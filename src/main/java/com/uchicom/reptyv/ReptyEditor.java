package com.uchicom.reptyv;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.uchicom.ui.ResumeDialog;
import com.uchicom.ui.ResumeFrame;

public class ReptyEditor extends ResumeDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea textArea;

	public ReptyEditor(ResumeFrame repptyViewer, JTextArea textArea) {
		super(repptyViewer, "repty.editor");
		this.textArea = textArea;
		initComponents();
	}

	private void initComponents() {
		setTitle("ReptyEditor");
		getContentPane().add(new JScrollPane(textArea));
		pack();
	}

}
