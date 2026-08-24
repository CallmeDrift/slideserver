package com.slideshow.server;

import com.slideshow.common.ISlideShowServer;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * mirey, recuerda: java -jar slideshow-server.jar
 */
public class ServerMain {

    public static void main(String[] args) {
        Map<String, String> config = loadEnvFile();
        String host = args.length > 2 ? args[2] : getConfig(config, "HOST", "192.168.1.4");
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Integer.parseInt(getConfig(config, "PORT", "1082"));
        String bindingName = args.length > 1 ? args[1] : getConfig(config, "BINDING_NAME", "SlideServer");
        System.setProperty("java.rmi.server.hostname", host);

        SwingUtilities.invokeLater(() -> {
            try {
                SlideViewerFrame viewer = new SlideViewerFrame();
                SlideShowServerImpl serverImpl = new SlideShowServerImpl(viewer);

                Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind(bindingName, serverImpl);

                new ServerControlPanel(serverImpl, viewer);

                System.out.println("Servidor RMI activo en puerto " + port + " con binding '" + bindingName + "'");
                System.out.println("Host expuesto: " + host);
                System.out.println("Los controles remotos deben conectarse a: rmi://" + host + ":" + port + "/" + bindingName);
                System.out.println("Interfaz del contrato: " + ISlideShowServer.class.getName());

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "No se pudo iniciar el servidor: " + e.getMessage());
                System.exit(1);
            }
        });
    }

    private static String getConfig(Map<String, String> config, String key, String defaultValue) {
        return System.getenv().getOrDefault(key, config.getOrDefault(key, defaultValue));
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> values = new HashMap<>();
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator > 0) {
                    String key = trimmed.substring(0, separator).trim();
                    String value = trimmed.substring(separator + 1).trim();
                    values.put(key, value.replaceAll("^\\\"|\\\"$|^'|'$", ""));
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer el archivo .env: " + e.getMessage());
        }
        return values;
    }
}
