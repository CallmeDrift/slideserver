package com.slideshow.server;

import com.slideshow.common.ISlideShowServer;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * mirey, recuerda: java -jar slideshow-server.jar
 */
public class ServerMain {

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1082;
        String bindingName = args.length > 1 ? args[1] : "SlideServer";
        String host = "192.168.1.4";
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
}
