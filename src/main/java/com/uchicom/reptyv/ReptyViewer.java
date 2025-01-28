// (C) 2024 uchicom
package com.uchicom.reptyv;

import com.uchicom.repty.Repty;
import com.uchicom.repty.dto.Template;
import com.uchicom.ui.FileOpener;
import com.uchicom.ui.ResumeFrame;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.yaml.snakeyaml.Yaml;

/**
 * yamlを監視してリアルタイムに画面に表示する。
 *
 * @author hex
 */
public class ReptyViewer extends ResumeFrame implements FileOpener {

  JTextField drawMapKeyTextField = new JTextField();
  JTextField pointerTextField = new JTextField();
  JComboBox<SimpleEntry<String, Integer>> pointerOrderComboBox = new JComboBox<>();
  ImagePanel imagePanel = new ImagePanel(pointerOrderComboBox, pointerTextField);
  File draft;
  JTextArea parameterText;
  JTextArea editorText;

  // 設定
  JComboBox<FontDisplayDto> editorFontComboBox;
  JComboBox<FontDisplayDto> parameterFontComboBox;
  JTextField editorFontSizeTextField;
  JTextField parameterFontSizeTextField;
  JTextField draftPathTextField;
  JTextField templatePathTextField;
  JCheckBox draftDisplayCheckBox;
  JTextField draftPageTextField;

  private static final long serialVersionUID = 1L;

  private static final String CONF_FILE_PATH = "./conf/reptyv.properties";

  String pressPoint = null;

  public ReptyViewer() {
    super(new File(CONF_FILE_PATH), "reptyv");
    initComponents();
  }

  private void initComponents() {
    setTitle("ReptyV");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    editorText = new JTextArea();
    parameterText = new JTextArea();
    editorFontSizeTextField = new JTextField();
    parameterFontSizeTextField = new JTextField();
    templatePathTextField = new JTextField();
    draftPathTextField = new JTextField();
    draftDisplayCheckBox = new JCheckBox("Display");
    draftDisplayCheckBox.addItemListener(
        e -> {
          waitingCursor(() -> update(editorText.getText(), parameterText.getText()));
        });
    KeyListener keyListener =
        new KeyListener() {

          @Override
          public void keyTyped(KeyEvent e) {
            System.out.println(e);
            // TODO Auto-generated method stub

          }

          @Override
          public void keyReleased(KeyEvent e) {
            System.out.println(e);
            // TODO Auto-generated method stub

          }

          @Override
          public void keyPressed(KeyEvent e) {
            System.out.println(e);
            if (KeyEvent.VK_F5 == e.getKeyCode()) {
              waitingCursor(() -> update(editorText.getText(), parameterText.getText()));
            } else if (KeyEvent.VK_S == e.getKeyCode()
                && (InputEvent.CTRL_DOWN_MASK & e.getModifiersEx()) == InputEvent.CTRL_DOWN_MASK) {
              waitingCursor(() -> save(new File(templatePathTextField.getText())));
            }
          }
        };
    draftPageTextField = new JTextField();
    draftPageTextField.setText("0");
    parameterText.addKeyListener(keyListener);
    editorText.addKeyListener(keyListener);
    editorFontComboBox = createFontComboBox(editorText, editorFontSizeTextField);
    parameterFontComboBox = createFontComboBox(parameterText, parameterFontSizeTextField);

    FileOpener.installDragAndDrop(imagePanel, this);
    JSplitPane splitPane = new JSplitPane();
    splitPane.setResizeWeight(0.7);
    String divider = config.getProperty("reptyv.divider");
    if (divider != null) {
      splitPane.setDividerLocation(Integer.valueOf(divider));
    }
    splitPane.addPropertyChangeListener(
        e -> config.put("reptyv.divider", String.valueOf(splitPane.getDividerLocation())));
    JPanel basePanel = new JPanel(new BorderLayout());
    imagePanel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {

            SimpleEntry<String, Integer> entry =
                (SimpleEntry<String, Integer>) pointerOrderComboBox.getSelectedItem();
            if (entry.getValue() == 1) {
              pressPoint =
                  imagePanel.getIntXFromUI(e.getX()) + "," + imagePanel.getIntYFromUI(e.getY());
            } else {
              pressPoint = imagePanel.getXFromUI(e.getX()) + "," + imagePanel.getYFromUI(e.getY());
            }
            pointerTextField.setText(pressPoint);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            pressPoint = null;
          }

          @Override
          public void mouseExited(MouseEvent e) {
            pressPoint = null;
          }
        });
    imagePanel.addMouseMotionListener(
        new MouseMotionAdapter() {

          @Override
          public void mouseDragged(MouseEvent e) {
            if (pressPoint == null) {
              return;
            }
            SimpleEntry<String, Integer> entry =
                (SimpleEntry<String, Integer>) pointerOrderComboBox.getSelectedItem();
            if (entry.getValue() == 1) {
              pointerTextField.setText(
                  pressPoint
                      + ","
                      + imagePanel.getIntXFromUI(e.getX())
                      + ","
                      + imagePanel.getIntYFromUI(e.getY()));
            } else {
              pointerTextField.setText(
                  pressPoint
                      + ","
                      + imagePanel.getXFromUI(e.getX())
                      + ","
                      + imagePanel.getYFromUI(e.getY()));
            }
          }
        });

    basePanel.add(new JScrollPane(imagePanel), BorderLayout.CENTER);
    JPanel nothPanel = new JPanel(new GridLayout(1, 4));
    nothPanel.add(new JLabel("DrawMapKeys:"));
    nothPanel.add(drawMapKeyTextField);
    nothPanel.add(new JLabel("Point(x,y[,x2,y2]):"));
    pointerOrderComboBox.addItem(new SimpleEntry<>("整数", 1));
    pointerOrderComboBox.addItem(new SimpleEntry<>("小数点以下第一位", 10));
    pointerOrderComboBox.addItem(new SimpleEntry<>("小数点以下第２位", 100));
    nothPanel.add(pointerOrderComboBox);
    pointerTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {

              @Override
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                imagePanel.repaint();
              }

              @Override
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                imagePanel.repaint();
              }

              @Override
              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                imagePanel.repaint();
              }
            });
    nothPanel.add(pointerTextField);
    basePanel.add(nothPanel, BorderLayout.NORTH);
    splitPane.setLeftComponent(basePanel);
    JTabbedPane ctrlPanel = new JTabbedPane();
    ctrlPanel.addTab("Editor", new JScrollPane(editorText));
    ctrlPanel.addTab("Parameter", new JScrollPane(parameterText));
    ctrlPanel.addTab("Config", new JScrollPane(createConfigPanel()));
    splitPane.setRightComponent(ctrlPanel);
    getContentPane().add(splitPane);
    pack();
  }

  JPanel createConfigPanel() {
    JPanel configPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    configPanel.add(new JLabel("Editor Font"), gbc);
    gbc.gridy = 1;
    configPanel.add(new JLabel("Parameter Font"), gbc);
    gbc.gridy = 2;
    configPanel.add(new JLabel("Template Yaml Path"), gbc);
    gbc.gridy = 3;
    configPanel.add(new JLabel("Draft PDF Path"), gbc);
    gbc.gridy = 4;
    configPanel.add(new JLabel("Draft Page"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    configPanel.add(editorFontComboBox, gbc);
    gbc.gridy = 1;
    configPanel.add(parameterFontComboBox, gbc);
    gbc.gridy = 2;
    configPanel.add(templatePathTextField, gbc);
    gbc.gridy = 3;
    configPanel.add(draftPathTextField, gbc);
    gbc.gridy = 4;
    configPanel.add(draftPageTextField, gbc);
    gbc.gridx = 2;
    gbc.gridy = 0;
    configPanel.add(editorFontSizeTextField, gbc);
    gbc.gridy = 1;
    configPanel.add(parameterFontSizeTextField, gbc);
    gbc.gridy = 3;
    configPanel.add(draftDisplayCheckBox, gbc);
    return configPanel;
  }

  void waitingCursor(Runnable runnable) {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    runnable.run();
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  JComboBox<FontDisplayDto> createFontComboBox(JTextArea textArea, JTextField fontSizeTextField) {
    Font font = textArea.getFont();
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    FontDisplayDto[] fontDisplayDtos = new FontDisplayDto[fonts.length];
    int selectIndex = -1;
    for (int i = 0; i < fonts.length; i++) {
      FontDisplayDto fontDisplayDto = new FontDisplayDto();
      fontDisplayDto.font = fonts[i];
      fontDisplayDtos[i] = fontDisplayDto;
      if (fontDisplayDto.font.getFontName().equals(font.getFontName())) {
        selectIndex = i;
      }
    }
    JComboBox<FontDisplayDto> comboBox = new JComboBox<>(fontDisplayDtos);
    comboBox.setSelectedIndex(selectIndex);
    comboBox.addActionListener(
        e -> {
          FontDisplayDto fontDisplayDto = (FontDisplayDto) comboBox.getSelectedItem();
          textArea.setFont(
              fontDisplayDto.font.deriveFont(Float.parseFloat(fontSizeTextField.getText())));
        });
    fontSizeTextField.setText(String.valueOf(font.getSize()));
    fontSizeTextField.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {
            // TODO Auto-generated method stub

          }

          @Override
          public void focusLost(FocusEvent e) {
            FontDisplayDto fontDisplayDto = (FontDisplayDto) comboBox.getSelectedItem();
            try {
              textArea.setFont(
                  fontDisplayDto.font.deriveFont(Float.parseFloat(fontSizeTextField.getText())));
            } catch (NumberFormatException e1) {
              fontSizeTextField.setText(String.valueOf(textArea.getFont().getSize()));
            }
          }
        });
    return comboBox;
  }

  /**
   * ファイルを更新
   *
   * @param yamlFile テンプレートyamlファイル
   */
  public void update(File yamlFile) {
    Map<String, Object> paramMap = createParameterMap();
    try (FileInputStream fis = new FileInputStream(yamlFile)) {

      Yaml yaml = new Yaml();
      Template template = null;
      template = yaml.loadAs(fis, Template.class);

      draw(template, paramMap);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void draw(Template template, Map<String, Object> paramMap) {

    try (PDDocument document = isDisplayDraft() ? Loader.loadPDF(draft) : new PDDocument();
        Repty yamlPdf = new Repty(document, template); ) {
      // PDFドキュメントを作成
      yamlPdf.init();
      yamlPdf.addKeys(drawMapKeyTextField.getText().split(" "));
      int page = 0;
      if (isDisplayDraft()) {
        page = Integer.parseInt(draftPageTextField.getText());
        PDPage d = document.getPage(page);
        yamlPdf.appendPage(paramMap, d);
      } else {
        PDPage d = yamlPdf.createPage(paramMap);
        document.addPage(d);
      }
      PDFRenderer renderer = new PDFRenderer(document);
      yamlPdf
          .pdFontMap
          .entrySet()
          .forEach(
              entry -> {
                try {
                  entry.getValue().subset();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });

      imagePanel.setImage(renderer.renderImageWithDPI(page, 72));
      imagePanel.repaint();
    } catch (NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  Map<String, Object> createParameterMap() {
    Map<String, Object> paramMap = new HashMap<>();
    for (String line : parameterText.getText().split("\n")) {
      if (line.isBlank()) {
        continue;
      }
      String[] splits = line.split("=");
      paramMap.put(splits[0], splits[1]);
    }
    return paramMap;
  }

  /**
   * ファイルを更新
   *
   * @param yamlText テンプレートyamlテキスト
   * @param parameterText パラメータテキスト
   */
  public void update(String yamlText, String parameterText) {
    Map<String, Object> paramMap = createParameterMap();

    Yaml yaml = new Yaml();
    Template template = null;
    template = yaml.loadAs(yamlText, Template.class);

    draw(template, paramMap);
  }

  /*
   * (非 Javadoc)
   *
   * @see com.uchicom.ui.FileOpener#open(java.util.List)
   */
  @Override
  public void open(List<File> fileList) {
    waitingCursor(
        () -> {
          if (fileList.size() > 0) {
            try {
              open(fileList.get(0));
            } catch (IOException e) {
              JOptionPane.showMessageDialog(this, e.getMessage());
              e.printStackTrace();
            }
          }
        });
  }

  @Override
  public void open(File file) throws IOException {
    String filename = file.getName();
    if (filename.matches(".*\\.[yY][aA]?[mM][lL]$")) {
      loadYaml(file);
    } else if (filename.matches(".*\\.[pP][dD][fF]$")) {
      this.draft = file;
      draftPathTextField.setText(file.getCanonicalPath());
      draftDisplayCheckBox.setSelected(true);
      update(editorText.getText(), parameterText.getText());
    }
  }

  void loadYaml(File yamlFile) {
    try (FileInputStream fis = new FileInputStream(yamlFile)) {
      templatePathTextField.setText(yamlFile.getCanonicalPath());
      editorText.setText(new String(fis.readAllBytes(), "UTF-8"));
      Yaml yaml = new Yaml();
      Template template = yaml.loadAs(editorText.getText(), Template.class);
      drawMapKeyTextField.setText(
          template.getDrawMap().keySet().stream().collect(Collectors.joining(" ")));
      draw(template, createParameterMap());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  boolean isDisplayDraft() {
    return draft != null && draftDisplayCheckBox.isSelected();
  }

  void save(File file) {
    if (editorText.getText().isBlank()) {
      JOptionPane.showMessageDialog(this, "No data.");
      return;
    }
    if (file.exists()) {
      int result =
          JOptionPane.showConfirmDialog(this, "Overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION);
      if (result != JOptionPane.YES_OPTION) {
        return;
      }
    }
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(editorText.getText().getBytes("UTF-8"));
      fos.flush();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
      e.printStackTrace();
    }
  }
}
