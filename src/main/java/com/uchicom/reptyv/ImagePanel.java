// (C) 2025 uchicom
package com.uchicom.reptyv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
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
  private Image currentImage;
  private float base = 1.5F;
  private int ratio = 0;
  private int width = 0;
  private int height = 0;

  public ImagePanel(
      JComboBox<SimpleEntry<String, Integer>> pointerOrderComboBox, JTextField pointerTextField) {
    this.pointerOrderComboBox = pointerOrderComboBox;
    this.pointerTextField = pointerTextField;
  }

  public void addRatio(int d) {
    this.ratio += d;
    setScaledImage();
  }

  public int getRatio() {
    return ratio;
  }

  public int getRatioLabel() {
    return (int) Math.round(100 * Math.pow(base, ratio));
  }

  public void setImage(BufferedImage image) {
    this.image = image;
    setScaledImage();
    repaint();
  }

  public void setScaledImage() {
    width = (int) (image.getWidth() * Math.pow(base, ratio));
    height = (int) (image.getHeight() * Math.pow(base, ratio));
    currentImage = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
    setPreferredSize(new Dimension(width, height));
  }

  float getRate() {
    return image == null ? 1 : PDRectangle.A4.getHeight() / image.getWidth();
  }

  public int getIntXFromUI(int x) {
    float rate = getRate();
    return (int) Math.round(x / (rate * Math.pow(base, ratio)));
  }

  public float getXFromUI(int x) {
    SimpleEntry<String, Integer> entry =
        (SimpleEntry<String, Integer>) pointerOrderComboBox.getSelectedItem();
    float rate = getRate();
    return Math.round(x * entry.getValue() / (rate * Math.pow(base, ratio)))
        / (float) entry.getValue();
  }

  public int getIntYFromUI(int y) {
    float rate = getRate();
    return (int) Math.round(image.getHeight() - (y / (rate * Math.pow(base, ratio))));
  }

  public float getYFromUI(int y) {
    SimpleEntry<String, Integer> entry =
        (SimpleEntry<String, Integer>) pointerOrderComboBox.getSelectedItem();
    float rate = getRate();
    return Math.round(
            ((image.getHeight() - (y / (rate * Math.pow(base, ratio))))) * entry.getValue())
        / (float) entry.getValue();
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if (currentImage == null) {
      return;
    }
    g.drawImage(currentImage, 0, 0, this);
    int height = image == null ? 0 : image.getHeight();
    float rate = getRate();
    if (pointerTextField == null) {
      return;
    }
    String[] splits = pointerTextField.getText().split(",");
    if (splits.length < 2) {
      return;
    }
    int x1 = (int) Math.round(Float.parseFloat(splits[0]) * rate * Math.pow(base, ratio));
    int y1 =
        (int) Math.round((height - Float.parseFloat(splits[1])) * rate * Math.pow(base, ratio));
    drawPointer(g, x1, y1, Color.BLUE);
    if (splits.length < 4) {
      return;
    }
    int x2 = (int) Math.round(Float.parseFloat(splits[2]) * rate * Math.pow(base, ratio));
    int y2 =
        (int) Math.round((height - Float.parseFloat(splits[3])) * rate * Math.pow(base, ratio));
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
