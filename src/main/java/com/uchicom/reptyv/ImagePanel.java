// (C) 2025 uchicom
package com.uchicom.reptyv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class ImagePanel extends JPanel {
  /** */
  private static final long serialVersionUID = 1L;

  private BufferedImage image;

  JTextField pointerTextField;

  public ImagePanel(JTextField pointerTextField) {
    this.pointerTextField = pointerTextField;
  }

  public void setImage(BufferedImage image) {
    this.image = image;
    setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    g.drawImage(image, 0, 0, this);
    int height = image == null ? 0 : image.getHeight();
    float rate = image == null ? 1 : PDRectangle.A4.getHeight() / image.getWidth();
    if (pointerTextField == null) {
      return;
    }
    String[] splits = pointerTextField.getText().split(",");
    if (splits.length < 2) {
      return;
    }
    int x1 = Math.round(Integer.parseInt(splits[0]) * rate);
    int y1 = Math.round(height - Integer.parseInt(splits[1]) * rate);
    drawPointer(g, x1, y1, Color.BLUE);
    if (splits.length < 4) {
      return;
    }
    int x2 = Math.round(Integer.parseInt(splits[2]) * rate);
    int y2 = Math.round(height - Integer.parseInt(splits[3]) * rate);
    drawPointer(g, x2, y2, Color.CYAN);
    g.setColor(Color.GREEN);
    g.drawLine(x1, y1, x2, y2);
  }

  void drawPointer(Graphics g, int x, int y, Color color) {
    g.setColor(color);
    g.drawLine(x - 100, y, x + 100, y);
    g.drawLine(x, y - 100, x, y + 100);
  }
}
