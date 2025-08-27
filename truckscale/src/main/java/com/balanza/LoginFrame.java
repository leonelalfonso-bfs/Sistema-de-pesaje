package com.balanza;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JLabel logoLabel;
    private Image originalLogo;

    public LoginFrame() {
        setTitle("Login - Sistema de Pesadas");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Carga imagen original del logo
        try {
            originalLogo = ImageIO.read(new java.io.File("C:\\balanzas\\Nuevo Java\\truckscale\\bfs_logo.png"));
        } catch (IOException e) {
            System.err.println("Error cargando logo: " + e.getMessage());
            originalLogo = null;
        }

        // Panel Norte con logo escalable
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(new DropShadowBorder());
        logoLabel = new JLabel();
        updateLogoScale(); // Escala inicial
        northPanel.add(logoLabel);
        add(northPanel, BorderLayout.NORTH);

        // Centro: Formulario con relieve
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createBevelBorder(1));
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(new JLabel("Usuario:"));
        userField = new JTextField();
        centerPanel.add(userField);
        centerPanel.add(new JLabel("Contraseña:"));
        passField = new JPasswordField();
        centerPanel.add(passField);
        add(centerPanel, BorderLayout.CENTER);

        // Sur: Botón con icono escalable
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loginButton = new JButton("Ingresar", createScaledIcon("/icons/login.png", 24, 24));
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.addActionListener(e -> {
            if ("admin".equals(userField.getText()) && "1234".equals(new String(passField.getPassword()))) {
                dispose();
                new MainFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        southPanel.add(loginButton);
        add(southPanel, BorderLayout.SOUTH);

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
            int width = getWidth() / 4; // Ej.: 25% del ancho de la ventana
            int height = -1; // Mantiene aspect ratio
            Image scaled = originalLogo.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        }
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}