package com.balanza;

import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SerialReader {
    private SerialPort port;
    private String regex;
    private double currentWeight = 0.0;
    private DatabaseManager dbManager = new DatabaseManager();

    public SerialReader() {
        Properties props = loadProperties();
        String model = props.getProperty("model", "BFS IND-100");
        Map<String, String> config = dbManager.getConfigForModel(model);
        
        String portName = config.getOrDefault("port", props.getProperty("port", "COM4"));
        int baud = Integer.parseInt(config.getOrDefault("baudrate", props.getProperty("baudrate", "9600")));
        regex = config.getOrDefault("regex", props.getProperty("regex", "\\u0002(\\d+)")); // Default si vacío

        // Validar regex
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            System.out.println("Regex inválido: " + regex + ". Usando default: \\u0002(\\d+)");
            regex = "\\u0002(\\d+)"; // Default para tu balanza
        }

        System.out.println("Usando config para modelo: " + model + " - Puerto: " + portName + ", Baud: " + baud + ", Regex: " + regex);

        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baud);
        port.setComPortParameters(baud, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (port.openPort()) {
            System.out.println("Puerto abierto exitosamente.");
        } else {
            System.out.println("Error al abrir el puerto: " + port.getLastErrorCode() + " - " + port.getLastErrorLocation());
            return;
        }

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(port.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Línea recibida (texto): " + line);
                    // Log en hex para depurar chars invisibles
                    System.out.print("Línea en hex: ");
                    for (char c : line.toCharArray()) {
                        System.out.print(String.format("%02X ", (int) c));
                    }
                    System.out.println();
                    Matcher matcher = Pattern.compile(regex).matcher(line);
                    if (matcher.find()) {
                        try {
                            if (matcher.groupCount() >= 1) {
                                currentWeight = Double.parseDouble(matcher.group(1));
                            } else {
                                currentWeight = Double.parseDouble(matcher.group(0));
                            }
                            System.out.println("Peso extraído: " + currentWeight);
                        } catch (NumberFormatException e) {
                            System.out.println("Error parseando peso: " + e.getMessage());
                        }
                    } else {
                        System.out.println("No match con regex en línea: " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error en thread de lectura: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void close() {
        if (port != null) port.closePort();
    }

    public static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Error cargando config.properties: " + e.getMessage());
        }
        return props;
    }
}