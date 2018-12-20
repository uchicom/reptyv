package com.uchicom.reptyv;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * keyとマッピングさせたcsvを読み込んでpdfに出力する
 * 
 * @author hex
 *
 */
public class Main {

	public static void main(String[] args) {
		// viewer起動
		SwingUtilities.invokeLater(() -> {
			JTextArea editorText = new JTextArea();
			JTextArea parameterText = new JTextArea();
			ReptyViewer viewer = new ReptyViewer(editorText, parameterText);
			ReptyEditor reptyEditor = new ReptyEditor(viewer, editorText);
			ParameterEditor parameterEditor = new ParameterEditor(viewer, parameterText);
			
			viewer.setVisible(true);
			reptyEditor.setVisible(true);
			parameterEditor.setVisible(true);
		});

	}

}
