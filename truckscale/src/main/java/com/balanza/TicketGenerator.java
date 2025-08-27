package com.balanza;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class TicketGenerator {
    public static void generatePdf(Map<String, Object> pesada, Map<String, String> clientConfig, String filePath) throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Encabezado con datos de cliente
        Paragraph header = new Paragraph();
        String logoPath = clientConfig.get("logo_path");
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                Image logo = Image.getInstance(logoPath.replace("file:", "")); // Elimina "file:" si está presente
                logo.scaleToFit(100, 50);
                header.add(logo);
            } catch (Exception e) {
                System.err.println("Error cargando logo: " + e.getMessage());
            }
        }
        header.add(new Phrase("\n" + clientConfig.get("razon_social") + "\nCUIT: " + clientConfig.get("cuit") + "\nDirección: " + clientConfig.get("direccion") + "\nLocalidad: " + clientConfig.get("localidad") + "\nProvincia: " + clientConfig.get("provincia") + "\nMail: " + clientConfig.get("mail"), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        document.add(header);

        // Cuerpo con datos de pesada
        Paragraph body = new Paragraph("\nTicket #" + pesada.get("id") + "\nFecha: " + pesada.get("fecha") + "\nPatente Chasis: " + pesada.get("patente_chasis") + "\nPatente Acoplado: " + pesada.get("patente_acoplado") + "\nChofer: " + pesada.get("nombre_chofer") + " (DNI: " + pesada.get("dni_chofer") + ")\nCliente: " + pesada.get("client_razon") + " (CUIT: " + pesada.get("client_cuit") + ")\nTransporte: " + pesada.get("transport_razon") + " (CUIT: " + pesada.get("transport_cuit") + ")\nProcedencia: " + pesada.get("procedencia") + "\nDestino: " + pesada.get("destino") + "\nObservaciones: " + pesada.get("observaciones") + "\nPeso Entrada: " + pesada.get("peso_entrada") + "\nPeso Salida: " + pesada.get("peso_salida") + "\nBruto: " + pesada.get("bruto") + "\nTara: " + pesada.get("tara") + "\nNeto: " + pesada.get("neto"), FontFactory.getFont(FontFactory.HELVETICA, 12));
        document.add(body);

        // Pie con logo BFS y datos
        Paragraph footer = new Paragraph("\nBalanzas Full Service SRL - CUIT:30-71534221-5\nLuis Braile 705 - San Lorenzo - Santa Fe - ventas@fullservicebalanzas.com.ar\nwww.fullservicebalanzas.com.ar", FontFactory.getFont(FontFactory.HELVETICA, 10));
        try {
            Image bfsLogo = Image.getInstance("C:/balanzas/Nuevo Java/truckscale/bfs_logo.png");
            bfsLogo.scaleToFit(100, 50);
            footer.add(bfsLogo);
        } catch (Exception e) {
            System.err.println("Error cargando bfs_logo: " + e.getMessage());
        }
        document.add(footer);

        document.close();
    }
}