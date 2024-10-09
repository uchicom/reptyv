// (C) 2024 uchicom
package com.uchicom.reptyv;

import com.uchicom.repty.Repty;
import com.uchicom.repty.dto.Template;
import com.uchicom.ui.FileOpener;
import com.uchicom.ui.ImagePanel;
import com.uchicom.ui.ResumeFrame;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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

  private JTextField textField = new JTextField();
  private ImagePanel panel = new ImagePanel();
  private File draft;
  private JTextArea parameterText;
  private JTextArea editorText;

  // 設定
  JComboBox<FontDisplayDto> editorFontComboBox;
  JComboBox<FontDisplayDto> parameterFontComboBox;
  JTextField editorFontSizeTextField;
  JTextField parameterFontSizeTextField;

  /** */
  private static final long serialVersionUID = 1L;

  private static final String CONF_FILE_PATH = "./conf/reptyv.properties";

  public ReptyViewer() {
    super(new File(CONF_FILE_PATH), "reptyv");
    this.editorText = new JTextArea();
    this.parameterText = new JTextArea();
    this.editorFontSizeTextField = new JTextField();
    this.parameterFontSizeTextField = new JTextField();
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
              update(editorText.getText(), parameterText.getText());
            }
          }
        };
    parameterText.addKeyListener(keyListener);
    editorText.addKeyListener(keyListener);
    editorFontComboBox = createFontComboBox(editorText, editorFontSizeTextField);
    parameterFontComboBox = createFontComboBox(parameterText, parameterFontSizeTextField);
    initComponents();
  }

  private void initComponents() {
    setTitle("ReptyV");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    FileOpener.installDragAndDrop(panel, this);
    JSplitPane splitPane = new JSplitPane();
    splitPane.setResizeWeight(0.7);
    String divider = config.getProperty("reptyv.divider");
    if (divider != null) {
      splitPane.setDividerLocation(Integer.valueOf(divider));
    }
    splitPane.addPropertyChangeListener(
        e -> config.put("reptyv.divider", String.valueOf(splitPane.getDividerLocation())));
    JPanel basePanel = new JPanel(new BorderLayout());
    basePanel.add(new JScrollPane(panel), BorderLayout.CENTER);
    basePanel.add(textField, BorderLayout.NORTH);
    splitPane.setLeftComponent(basePanel);
    JTabbedPane ctrlPanel = new JTabbedPane();
    ctrlPanel.addTab("Editor", new JScrollPane(editorText));
    ctrlPanel.addTab("Parameter", new JScrollPane(parameterText));
    ctrlPanel.addTab("Config", createConfigPanel());
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
    gbc.gridx = 1;
    gbc.gridy = 0;
    configPanel.add(editorFontComboBox, gbc);
    gbc.gridy = 1;
    configPanel.add(parameterFontComboBox, gbc);
    gbc.gridx = 2;
    gbc.gridy = 0;
    configPanel.add(editorFontSizeTextField, gbc);
    gbc.gridy = 1;
    configPanel.add(parameterFontSizeTextField, gbc);
    return configPanel;
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
   * @param baseFile
   */
  private void watch(File yamlFile) {
    Thread thread =
        new Thread(
            () -> {
              WatchKey key = null;
              try {
                WatchService service = FileSystems.getDefault().newWatchService();
                regist(service, yamlFile);
                while ((key = service.take()) != null) {

                  // スレッドの割り込み = 終了要求を判定する. 必要なのか不明
                  if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                  }
                  if (!key.isValid()) continue;
                  for (WatchEvent<?> event : key.pollEvents()) {
                    // eventではファイル名しかとれない
                    // 監視対象のフォルダを取得する必要がある
                    if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
                      update(yamlFile);
                    }
                  }
                  key.reset();
                }
              } catch (IOException e) {
                e.printStackTrace();
              } catch (InterruptedException e) {
                e.printStackTrace();
                key.cancel();
              }
            });
    thread.setDaemon(false); // mainスレッドと運命を共に
    thread.start();
  }

  /**
   * 監視サービスにファイルを登録する
   *
   * @param service
   * @param file
   * @throws IOException
   */
  public void regist(WatchService service, File file) throws IOException {
    Path path = file.getParentFile().toPath();
    path.register(service, new Kind[] {StandardWatchEventKinds.ENTRY_MODIFY}, new Modifier[] {});
    update(file);
  }

  /**
   * ファイルを更新
   *
   * @param yamlFile
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

    try (PDDocument document = this.draft != null ? Loader.loadPDF(draft) : new PDDocument();
        Repty yamlPdf = new Repty(document, template); ) {
      // PDFドキュメントを作成
      yamlPdf.init();
      yamlPdf.addKeys(textField.getText().split(" "));
      if (draft == null) {
        PDPage d = yamlPdf.createPage(paramMap);
        document.addPage(d);
      } else {
        PDPage d = document.getPage(0);
        yamlPdf.appendPage(paramMap, d);
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

      panel.setImage(renderer.renderImageWithDPI(0, 72));
      panel.repaint();
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
   * @param yamlText
   * @param parameterText
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
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    if (fileList.size() > 0) {
      try {
        open(fileList.get(0));
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
        e.printStackTrace();
      }
    }
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  @Override
  public void open(File file) throws IOException {
    String filename = file.getName();
    if (filename.matches(".*\\.[yY][aA]?[mM][lL]$")) {
      watch(file);
    } else if (filename.matches(".*\\.[pP][dD][fF]$")) {
      this.draft = file;
    }
  }
}
