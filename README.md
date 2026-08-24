# Slides show Server (laboratorio de sistemas distribuidos)

Servidor de presentación de diapositivas controlable de forma remota vía RMI.
Aqui ta el server y se expone la interfaz ISlideShowServer para que el cliente lo implemente cuando lo haga.

## Compilar

```bash
mvn clean package
```

Genera `target/slideshow-server.jar` (el jar con PDFBox).

## Ejecutar

```bash
java -jar target/slideshow-server.jar [puerto] [nombreBinding] [ipServidor]
```

Por defecto: puerto `1082` y binding `SlideServer`. Si no se pasa
`ipServidor`, la JVM detecta una dirección local automáticamente. La IP es la
que RMI inserta en el stub que reciben los clientes; si el equipo tiene varias
interfaces (VPN, Wi-Fi y Ethernet, por ejemplo), indícala explícitamente.

Ejemplo en una red local:

```bash
java -jar target/slideshow-server.jar 1082 SlideServer 192.168.1.14
```
