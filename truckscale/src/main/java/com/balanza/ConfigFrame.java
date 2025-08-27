package com.balanza;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream; // Import agregado para resolver el error
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigFrame extends JFrame {
    private JComboBox<String> modelCombo;
    private JComboBox<String> portCombo;
    private JComboBox<Integer> baudCombo;
    private JTextField regexField;
    private DatabaseManager dbManager = new DatabaseManager();
    private JLabel logoLabel;
    private Image originalLogo;

    public ConfigFrame() {
        setTitle("Configuración Balanza");
        setSize(450, 350);
        setLayout(new BorderLayout(10, 10));

        // Carga imagen original del logo
        try {
            originalLogo = ImageIO.read(new java.io.File("C:\\balanzas\\Nuevo Java\\truckscale\\bfs_logo.png"));
        } catch (IOException e) {
            System.err.println("Error cargando logo: " + e.getMessage());
            originalLogo = null;
        }

        // Norte: Logo escalable
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(new DropShadowBorder());
        logoLabel = new JLabel();
        updateLogoScale(); // Escala inicial
        northPanel.add(logoLabel);
        add(northPanel, BorderLayout.NORTH);

        // Centro: Form con relieve
        JPanel centerPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createBevelBorder(1));
        centerPanel.add(new JLabel("Modelo Balanza:"));
        modelCombo = new JComboBox<>();
        loadModels();
        modelCombo.addActionListener(e -> loadConfigForSelectedModel());
        centerPanel.add(modelCombo);

        JButton newModelButton = new JButton("Nuevo", createScaledIcon("/icons/new.png", 24, 24));
        newModelButton.addActionListener(e -> addNewModel());
        centerPanel.add(new JLabel()); // Placeholder
        centerPanel.add(newModelButton);

        centerPanel.add(new JLabel("Puerto:"));
        portCombo = new JComboBox<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            portCombo.addItem(port.getSystemPortName());
        }
        centerPanel.add(portCombo);

        centerPanel.add(new JLabel("Baudrate:"));
        baudCombo = new JComboBox<>(new Integer[]{2400, 4800, 9600, 19200, 38400, 57600, 115200});
        centerPanel.add(baudCombo);

        centerPanel.add(new JLabel("Regex para peso:"));
        regexField = new JTextField();
        centerPanel.add(regexField);

        add(centerPanel, BorderLayout.CENTER);

        // Sur: Guardar con icono
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Guardar", createScaledIcon("/icons/save.png", 24, 24));
        saveButton.addActionListener(e -> saveConfig());
        southPanel.add(saveButton);
        add(southPanel, BorderLayout.SOUTH);

        // Listener para resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLogoScale();
            }
        });

        loadProperties();
        setVisible(true);
    }

    private void updateLogoScale() {
        if (originalLogo != null) {
            int width = getWidth() / 4;
            int height = -1;
            Image scaled = originalLogo.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        }
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void loadModels() {
        List<String> models = dbManager.getAllBalanzaModels();
        modelCombo.removeAllItems();
        for (String model : models) {
            modelCombo.addItem(model);
        }
        if (models.isEmpty()) {
            modelCombo.addItem("BFS IND-100");
        }
    }

    private void loadConfigForSelectedModel() {
        String selectedModel = (String) modelCombo.getSelectedItem();
        if (selectedModel != null) {
            Map<String, String> config = dbManager.getConfigForModel(selectedModel);
            if (!config.isEmpty()) {
                portCombo.setSelectedItem(config.get("port"));
                baudCombo.setSelectedItem(Integer.parseInt(config.get("baudrate")));
                regexField.setText(config.get("regex"));
            }
        }
    }

    private void addNewModel() {
        String newModel = JOptionPane.showInputDialog(this, "Ingrese nombre del nuevo modelo:");
        if (newModel != null && !newModel.trim().isEmpty()) {
            modelCombo.addItem(newModel);
            modelCombo.setSelectedItem(newModel);
        }
    }

    private void loadProperties() {
        Properties props = SerialReader.loadProperties();
        String defaultModel = props.getProperty("model", "BFS IND-100");
        modelCombo.setSelectedItem(defaultModel);
        loadConfigForSelectedModel();
    }

    private void saveConfig() {
        String selectedModel = (String) modelCombo.getSelectedItem();
        if (selectedModel == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un modelo");
            return;
        }
        String port = (String) portCombo.getSelectedItem();
        String baudrate = baudCombo.getSelectedItem().toString();
        String regex = regexField.getText();

        dbManager.saveConfigForModel(selectedModel, port, baudrate, regex);

        Properties props = new Properties();
        props.setProperty("model", selectedModel);
        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            props.store(out, null);
            JOptionPane.showMessageDialog(this, "Configuración guardada para " + selectedModel);
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
        }
    }
}