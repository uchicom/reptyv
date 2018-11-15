package com.uchicom.reptyv;

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
			ReptyViewer viewer = new ReptyViewer();
			viewer.setVisible(true);
		});

	}

}
