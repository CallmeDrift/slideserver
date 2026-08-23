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
java -jar target/slideshow-server.jar [puerto] [nombreBinding]
```

Por defecto: puerto `1082`, binding `SlideServer`.

