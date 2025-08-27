package com.balanza;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    public static final String DB_URL = "jdbc:sqlite:truckscale.db";

    public DatabaseManager() {
        createTables();
        addFechaColumnIfMissing();
    }

    private void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY, razon_social TEXT, cuit TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS transports (id INTEGER PRIMARY KEY, razon_social TEXT, cuit TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS weighings (id INTEGER PRIMARY KEY AUTOINCREMENT, patente_chasis TEXT, patente_acoplado TEXT, " +
                    "nombre_chofer TEXT, dni_chofer TEXT, client_razon TEXT, client_cuit TEXT, transport_razon TEXT, transport_cuit TEXT, " +
                    "procedencia TEXT, destino TEXT, observaciones TEXT, peso_entrada REAL, peso_salida REAL, bruto REAL, tara REAL, neto REAL, fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP, salida_pendiente INTEGER DEFAULT 1)");
            stmt.execute("CREATE TABLE IF NOT EXISTS balanza_configs (model TEXT PRIMARY KEY, port TEXT, baudrate TEXT, regex TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS client_config (id INTEGER PRIMARY KEY AUTOINCREMENT, razon_social TEXT, cuit TEXT, direccion TEXT, localidad TEXT, provincia TEXT, mail TEXT, logo_path TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addFechaColumnIfMissing() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE weighings ADD COLUMN fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name")) {
                e.printStackTrace();
            }
        }
    }

    public void saveWeighing(String patenteChasis, String patenteAcoplado, String nombreChofer, String dniChofer,
                             String clientRazon, String clientCuit, String transportRazon, String transportCuit,
                             String procedencia, String destino, String observaciones,
                             double entrada, double salida, int salidaPendiente) {
        double bruto = Math.max(entrada, salida);
        double tara = Math.min(entrada, salida);
        double neto = bruto - tara;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO weighings (patente_chasis, patente_acoplado, nombre_chofer, dni_chofer, client_razon, client_cuit, " +
                             "transport_razon, transport_cuit, procedencia, destino, observaciones, peso_entrada, peso_salida, bruto, tara, neto, salida_pendiente) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, patenteChasis);
            pstmt.setString(2, patenteAcoplado);
            pstmt.setString(3, nombreChofer);
            pstmt.setString(4, dniChofer);
            pstmt.setString(5, clientRazon);
            pstmt.setString(6, clientCuit);
            pstmt.setString(7, transportRazon);
            pstmt.setString(8, transportCuit);
            pstmt.setString(9, procedencia);
            pstmt.setString(10, destino);
            pstmt.setString(11, observaciones);
            pstmt.setDouble(12, entrada);
            pstmt.setDouble(13, salida);
            pstmt.setDouble(14, bruto);
            pstmt.setDouble(15, tara);
            pstmt.setDouble(16, neto);
            pstmt.setInt(17, salidaPendiente);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPreviousPatentesChasis() {
        List<String> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT patente_chasis FROM weighings")) {
            while (rs.next()) {
                list.add(rs.getString("patente_chasis"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getAllBalanzaModels() {
        List<String> models = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT model FROM balanza_configs")) {
            while (rs.next()) {
                models.add(rs.getString("model"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return models;
    }

    public Map<String, String> getConfigForModel(String model) {
        Map<String, String> config = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT port, baudrate, regex FROM balanza_configs WHERE model = ?")) {
            pstmt.setString(1, model);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                config.put("port", rs.getString("port"));
                config.put("baudrate", rs.getString("baudrate"));
                config.put("regex", rs.getString("regex"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void saveConfigForModel(String model, String port, String baudrate, String regex) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO balanza_configs (model, port, baudrate, regex) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, model);
            pstmt.setString(2, port);
            pstmt.setString(3, baudrate);
            pstmt.setString(4, regex);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getClientConfig() {
        Map<String, String> config = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM client_config WHERE id = 1")) {
            if (rs.next()) {
                config.put("razon_social", rs.getString("razon_social"));
                config.put("cuit", rs.getString("cuit"));
                config.put("direccion", rs.getString("direccion"));
                config.put("localidad", rs.getString("localidad"));
                config.put("provincia", rs.getString("provincia"));
                config.put("mail", rs.getString("mail"));
                config.put("logo_path", rs.getString("logo_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void saveClientConfig(String razonSocial, String cuit, String direccion, String localidad, String provincia, String mail, String logoPath) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO client_config (id, razon_social, cuit, direccion, localidad, provincia, mail, logo_path) VALUES (1, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, razonSocial);
            pstmt.setString(2, cuit);
            pstmt.setString(3, direccion);
            pstmt.setString(4, localidad);
            pstmt.setString(5, provincia);
            pstmt.setString(6, mail);
            pstmt.setString(7, logoPath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getWeighings() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM weighings ORDER BY id DESC")) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("fecha", rs.getString("fecha"));
                map.put("patente_chasis", rs.getString("patente_chasis"));
                map.put("bruto", rs.getDouble("bruto"));
                map.put("tara", rs.getDouble("tara"));
                map.put("neto", rs.getDouble("neto"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Object> getWeighingById(int id) {
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM weighings WHERE id = ?")) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                map.put("id", rs.getInt("id"));
                map.put("patente_chasis", rs.getString("patente_chasis"));
                map.put("patente_acoplado", rs.getString("patente_acoplado"));
                map.put("nombre_chofer", rs.getString("nombre_chofer"));
                map.put("dni_chofer", rs.getString("dni_chofer"));
                map.put("client_razon", rs.getString("client_razon"));
                map.put("client_cuit", rs.getString("client_cuit"));
                map.put("transport_razon", rs.getString("transport_razon"));
                map.put("transport_cuit", rs.getString("transport_cuit"));
                map.put("procedencia", rs.getString("procedencia"));
                map.put("destino", rs.getString("destino"));
                map.put("observaciones", rs.getString("observaciones"));
                map.put("peso_entrada", rs.getDouble("peso_entrada"));
                map.put("peso_salida", rs.getDouble("peso_salida"));
                map.put("bruto", rs.getDouble("bruto"));
                map.put("tara", rs.getDouble("tara"));
                map.put("neto", rs.getDouble("neto"));
                map.put("fecha", rs.getString("fecha"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public int getLastId() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM weighings")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

	public Map<String, Object> getLastWeighing() {
		// TODO Auto-generated method stub
		return null;
	}
}