# Mineria-de-texto-sistema-dsitribuido
Un proyecto que busca una frase entre libros y los ordena por orden de relevancia

## Requisitos
- openjdk 11.0.19
- Apache Maven 3.9.2
- Apache Zookeeper 3.6.3 (PRECAUCIÓN: Con próposito de eliminar la salida en terminal del log de conexión con zookeeper, se utilizó esta versión. Sin embargo, esta versión cuenta con mútliples vulnerabilidades de seguridad. Se recomienda siempre utilizar la versión más nueva)

## Instalación

1. Clona este repo
   ```zsh
   git clone https://github.com/SergiMartinezR/Mineria-de-texto-sistema-dsitribuido.git
   ```
2. Descomprime la carpeta que contiene los libros
    ```zsh
    unrar LIBROS_TXT.rar
    ```
    Si quieres utilizar otra carpeta, modifica el código del servidor de procesamiento
3. Compila el servidor de procesamiento
   ```zsh
   cd ServidorProcesamiento
   mvn clean compile assembly:single
   ``` 
4. Compila el servidor web
   ```zsh
   cd ../WebServer
   mvn clean compile assembly:single
   ``` 
