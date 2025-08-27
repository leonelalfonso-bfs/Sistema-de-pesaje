package com.balanza;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Timer;
import java.util.TimerTask;

public class MainFrame extends JFrame {
    private JTextField patenteChasisField, patenteAcopladoField, nombreChoferField, dniChoferField;
    private JTextField clientRazonField, clientCuitField, transportRazonField, transportCuitField;
    private JTextField procedenciaField, destinoField, observacionesField;
    private JTextField entradaField, salidaField, brutoField, taraField, netoField;
    private JLabel pesoRealTimeLabel;
    private SerialReader serialReader;
    private DatabaseManager dbManager;
    private JLabel logoLabel;
    private Image originalLogo;

    public MainFrame() {
        setTitle("Pesadas de Camiones - Versión Moderna");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Carga imagen original del logo
        try {
            originalLogo = ImageIO.read(new java.io.File("C:\\balanzas\\Nuevo Java\\truckscale\\bfs_logo.png"));
        } catch (IOException e) {
            System.err.println("Error cargando logo: " + e.getMessage());
            originalLogo = null;
        }

        // Panel Norte: Título, logo escalable y config
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(new DropShadowBorder());
        logoLabel = new JLabel();
        updateLogoScale(); // Escala inicial
        northPanel.add(logoLabel);
        JLabel titleLabel = new JLabel("Sistema de Pesadas");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        northPanel.add(titleLabel);
        JButton configButton = new JButton("Configurar", createScaledIcon("/icons/config.png", 24, 24));
        configButton.addActionListener(e -> new ConfigFrame());
        northPanel.add(configButton);
        add(northPanel, BorderLayout.NORTH);

        // Centro: Pestañas con relieve
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createBevelBorder(0));
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Pestaña 1: Datos Camión
        JPanel camionPanel = createStyledPanel();
        addLabeledField(camionPanel, "Patente Chasis:", patenteChasisField = new JTextField(20), 0, 0);
        JButton suggestButton = new JButton("Sugerir", createScaledIcon("/icons/suggest.png", 24, 24));
        suggestButton.addActionListener(e -> showSuggestions());
        camionPanel.add(suggestButton, createConstraints(2, 0));
        addLabeledField(camionPanel, "Patente Acoplado:", patenteAcopladoField = new JTextField(20), 0, 1);
        addLabeledField(camionPanel, "Nombre Chofer:", nombreChoferField = new JTextField(20), 0, 2);
        addLabeledField(camionPanel, "DNI Chofer:", dniChoferField = new JTextField(20), 0, 3);
        tabbedPane.addTab("Datos Camión", createScaledIcon("/icons/truck.png", 16, 16), camionPanel);

        // Pestaña 2: Cliente/Transporte
        JPanel clientePanel = createStyledPanel();
        addLabeledField(clientePanel, "Cliente Razón Social:", clientRazonField = new JTextField(20), 0, 0);
        addLabeledField(clientePanel, "Cliente CUIT:", clientCuitField = new JTextField(20), 0, 1);
        addLabeledField(clientePanel, "Transporte Razón Social:", transportRazonField = new JTextField(20), 0, 2);
        addLabeledField(clientePanel, "Transporte CUIT:", transportCuitField = new JTextField(20), 0, 3);
        addLabeledField(clientePanel, "Procedencia:", procedenciaField = new JTextField(20), 0, 4);
        addLabeledField(clientePanel, "Destino:", destinoField = new JTextField(20), 0, 5);
        addLabeledField(clientePanel, "Observaciones:", observacionesField = new JTextField(20), 0, 6);
        tabbedPane.addTab("Cliente/Transporte", createScaledIcon("/icons/client.png", 16, 16), clientePanel);

        // Pestaña 3: Pesos
        JPanel pesosPanel = createStyledPanel();
        addLabeledField(pesosPanel, "Peso Entrada:", entradaField = new JTextField(10), 0, 0);
        JButton captureEntrada = new JButton("Capturar", createScaledIcon("/icons/capture.png", 24, 24));
        captureEntrada.addActionListener(e -> entradaField.setText(String.valueOf(serialReader.getCurrentWeight())));
        pesosPanel.add(captureEntrada, createConstraints(2, 0));
        addLabeledField(pesosPanel, "Peso Salida:", salidaField = new JTextField(10), 0, 1);
        JButton captureSalida = new JButton("Capturar", createScaledIcon("/icons/capture.png", 24, 24));
        captureSalida.addActionListener(e -> salidaField.setText(String.valueOf(serialReader.getCurrentWeight())));
        pesosPanel.add(captureSalida, createConstraints(2, 1));
        addLabeledField(pesosPanel, "Bruto:", brutoField = new JTextField(10), 0, 2);
        addLabeledField(pesosPanel, "Tara:", taraField = new JTextField(10), 0, 3);
        addLabeledField(pesosPanel, "Neto:", netoField = new JTextField(10), 0, 4);
        JButton calculateButton = new JButton("Calcular", createScaledIcon("/icons/calc.png", 24, 24));
        calculateButton.addActionListener(e -> calculateWeights());
        pesosPanel.add(calculateButton, createConstraints(1, 5, 2));
        tabbedPane.addTab("Pesos", createScaledIcon("/icons/weight.png", 16, 16), pesosPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Sur: Peso real time con sombra
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBorder(new DropShadowBorder());
        JLabel realTimeLabel = new JLabel("Peso en tiempo real:");
        realTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        southPanel.add(realTimeLabel);
        pesoRealTimeLabel = new JLabel("0 kg");
        pesoRealTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        pesoRealTimeLabel.setForeground(new Color(0, 102, 204));
        southPanel.add(pesoRealTimeLabel);
        JButton saveButton = new JButton("Guardar", createScaledIcon("/icons/save.png", 24, 24));
        saveButton.addActionListener(e -> saveWeighing());
        southPanel.add(saveButton);
        add(southPanel, BorderLayout.SOUTH);

        serialReader = new SerialReader();
        dbManager = new DatabaseManager();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double weight = serialReader.getCurrentWeight();
                String formattedWeight = (weight % 1 == 0) ? String.format("%.0f", weight) : String.format("%.1f", weight);
                pesoRealTimeLabel.setText(formattedWeight + " kg");
            }
        }, 0, 1000);

        // Listener para resize: Escala logo automáticamente
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLogoScale();
            }
        });

        setVisible(true);
    }

    private void updateLogoScale() {
        if (originalLogo != null) {
            int width = getWidth() / 4; // 25% del ancho
            int height = -1; // Aspect ratio
            Image scaled = originalLogo.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        }
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private JPanel createStyledPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(new DropShadowBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setBackground(new Color(255, 255, 255));
        return panel;
    }

    private void addLabeledField(JPanel panel, String labelText, JTextField field, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label, createConstraints(x, y));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(field, createConstraints(x + 1, y));
    }

    private GridBagConstraints createConstraints(int x, int y) {
        return createConstraints(x, y, 1);
    }

    private GridBagConstraints createConstraints(int x, int y, int width) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void showSuggestions() {
        String[] suggestions = dbManager.getPreviousPatentesChasis().toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(this, "Selecciona patente previa:", "Sugerencias", JOptionPane.PLAIN_MESSAGE, null, suggestions, null);
        if (selected != null) {
            patenteChasisField.setText(selected);
        }
    }

    private void calculateWeights() {
        try {
            double entrada = Double.parseDouble(entradaField.getText());
            double salida = Double.parseDouble(salidaField.getText());
            double bruto = Math.max(entrada, salida);
            double tara = Math.min(entrada, salida);
            double neto = bruto - tara;
            brutoField.setText(String.valueOf(bruto));
            taraField.setText(String.valueOf(tara));
            netoField.setText(String.valueOf(neto));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa pesos válidos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveWeighing() {
        try {
            double entrada = Double.parseDouble(entradaField.getText());
            double salida = Double.parseDouble(salidaField.getText());
            dbManager.saveWeighing(patenteChasisField.getText(), patenteAcopladoField.getText(), nombreChoferField.getText(), dniChoferField.getText(),
            	    clientRazonField.getText(), clientCuitField.getText(), transportRazonField.getText(), transportCuitField.getText(),
            	    procedenciaField.getText(), destinoField.getText(), observacionesField.getText(), entrada, salida, 1);  // Agrega el 1 (o 0) al final
            JOptionPane.showMessageDialog(this, "Pesada guardada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa todos los datos válidos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}