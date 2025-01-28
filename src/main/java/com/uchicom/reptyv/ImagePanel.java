// (C) 2025 uchicom
package com.uchicom.reptyv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.AbstractMap.SimpleEntry;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class ImagePanel extends JPanel {
  /** */
  private static final long serialVersionUID = 1L;

  private BufferedImage image;

  JComboBox<SimpleEntry<String, Integer>> pointerOrderComboBox;
  JTextField pointerTextField;

  public ImagePanel(
      JComboBox<SimpleEntry<String, Integer>> pointerOrderComboBox, JTextField pointerTextField) {
    this.pointerOrderComboBox = pointerOrderComboBox;
    this.pointerTextField = pointerTextField;
  }

  public void setImage(BufferedImage image) {
    this.image = image;
    setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
  }

  public int getIntXFromUI(int x) {
    float rate = image == null ? 1 : PDRectangle.A4.getHeight() / image.getWidth();
    return Math.round(x / rate);
  }

  public float getXFromUI(int x) {
    SimpleEntry<String, Integer> entry =
        (SimpleEntry<String, Integer>) pointerOrderComboBox.getSelectedItem();
    float rate = image == null ? 1 : PDRectangle.A4.getHeight() / image.getWidth();
    return Math.round(x * entry.getValue() / rate) / (float) entry.getValue();
  }

  public int getIntYFromUI(int y) {
    float rate = image == null ? 1 : PDRectangle.A4.getHeight() / image.getWidth();
    return Math.round(image.getHeight() - (y / rate));
  }

  public float getYFromUI(int y) {
    SimpleEntry<String, Integer> entry =
        (SimpleEntry<String, Integer>) pointerOrderComboBox.getSelectedItem();
    float rate = image == null ? 1 : PDRectangle.A4.getHeight() / image.getWidth();
    return Math.round((image.getHeight() - (y / rate)) * entry.getValue())
        / (float) entry.getValue();
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
    int x1 = Math.round(Float.parseFloat(splits[0]) * rate);
    int y1 = Math.round(height - Float.parseFloat(splits[1]) * rate);
    drawPointer(g, x1, y1, Color.BLUE);
    if (splits.length < 4) {
      return;
    }
    int x2 = Math.round(Float.parseFloat(splits[2]) * rate);
    int y2 = Math.round(height - Float.parseFloat(splits[3]) * rate);
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
