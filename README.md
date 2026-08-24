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

También puedes configurar el servidor en `.env`:

```dotenv
HOST=192.168.1.4
PORT=1082
BINDING_NAME=SlideServer
```

La configuración usa primero los argumentos, después las variables del sistema,
luego `.env` y finalmente los valores por defecto. `HOST` es la dirección que
RMI inserta en el stub que reciben los clientes.

Ejemplo en una red local:

```bash
java -jar target/slideshow-server.jar 1082 SlideServer 192.168.1.14
```
