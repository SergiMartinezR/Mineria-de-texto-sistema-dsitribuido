# Mineria-de-texto-sistema-dsitribuido

Un proyecto que busca una frase entre libros y los ordena por orden de relevancia

## Requisitos

- openjdk 11.0.19
- Apache Maven 3.9.2
- Apache Zookeeper 3.6.3 (PRECAUCIÓN: Con próposito de eliminar la salida en terminal del log de conexión con zookeeper, se utilizó esta versión. Sin embargo, esta versión cuenta con mútliples vulnerabilidades de seguridad. Se recomienda siempre utilizar la versión más nueva)

## Instalación

1. Clona este repo:
   ```zsh
   git clone https://github.com/SergiMartinezR/Mineria-de-texto-sistema-dsitribuido.git
   ```
2. Descomprime la carpeta que contiene los libros:
    ```zsh
    unrar x LIBROS_TXT.rar
    ```
    Si quieres utilizar otra carpeta, modifica el código del servidor de procesamiento
3. Compila el servidor de procesamiento:
   ```zsh
   cd ServidorProcesamiento
   mvn clean compile assembly:single
   ``` 
4. Compila el servidor web:
   ```zsh
   cd ../WebServer
   mvn clean compile assembly:single
   ``` 

## Uso

### Distribuido (recomendado)

#### Servidor Zookeeper

1. Instala y configura zookeeper en la respectiva máquina virtual.

2. Inicia el servidor zookeeper.

**Nota:** No es necesario que el servidor zookeeper esté en una máquina virtual separada, puede ejecutarse junto con algún otro servidor o nodo de procesamiento. Siempre y cuando el firewall de tus máquinas virtuales lo permita, no debería haber ningún problema para acceder a zookeeper.
En mi caso el firewall de las máquinas virtuales de google cloud solo permite el tráfico http o https, por esta razón tuve que crear una máquina virtual dedicada únicamente a este servidor.

#### Servidor Web

1. Mueve el ejecutable (.jar) del servidor web a la máquina virtual que fungirá como el servidor web

2. Inicia el servidor:
    ```zsh
    java -jar WebServer-1.0-SNAPSHOT-jar-with-dependencies.jar puerto ip_zookeeper:puerto_zookeeper
    ``` 
    Ejemplo:
    ```zsh
    java -jar WebServer-1.0-SNAPSHOT-jar-with-dependencies.jar 80 34.106.144.237:80
    ``` 

**Nota:** Se recomienda ejecutar el servidor web en el puerto 80, para que sea fácilmente accesible desde cualquier navegador.

#### Servidores de Procesamiento

1. Mueve el ejecutable del servidor de procesamiento a las máquinas virtuales que fungirán como nodos de procesamiento

2. Coloca la carpeta que contiene los libros en el mismo directorio que el ejecutable. De otra forma, el programa no funcionará bien

3. Inicia el servidor en cada nodo de procesamiento:
    ```zsh
    java -jar ServidorDeProcesamiento-1.0-SNAPSHOT-jar-with-dependencies.jar puerto ip_de_esta_maquina ip_zookeeper:puerto_zookeeper
    ``` 
    Ejemplo:
    ```zsh
    java -jar ServidorDeProcesamiento-1.0-SNAPSHOT-jar-with-dependencies.jar 80 34.125.61.49 34.106.144.237:80
    ``` 
    En este ejemplo no es necesario agregar el puerto a la ip de la máquina actual porque se está ejecutando en el puerto 80. Si deseas ejecutar tu servidor en algún otro puerto debes agregar también el puerto, por ejemplo `34.125.61.49:8081`.

**Nota:** Se recomienda ejecutar los servidores de procesamiento en el puerto 80, para que no tengas que poner el puerto en la ejecución de cada máquina, como en la nota del paso 2.


**Nota 2:** El mínimo de nodos de procesamiento para que la aplicación funcione correctamente es de 1 nodo. Y el máximo depende de cuantos archivos tengas en la carpeta de archivos que quieras leer. En el ejemplo de la carpeta que se comparte aquí, hay 46 libros, por tanto el máximo es de 46 nodos. Aunque con 3 funciona bastante bien.


¡Listo! ahora ingresa al servidor web desde cualquier navegador e ingresa la frase que quieras buscar, la aplicación te recomendará libros que coincidan con tu búsqueda.

### Local

En pocas palabras, es lo mismo que en la sección anterior, solo que cambias las direcciones ip por direcciones locales. Por ejemplo: `localhost:8081`. Aún así dejo el tutorial completo.

#### Servidor Zookeeper

1. Instala y configura zookeeper.

2. Inicia el servidor zookeeper.

#### Servidor Web

1. Dirígete a la carpeta del servidor web:
    ```zsh
    cd WebServer
    ```
    El comando anterior supone que estás en la carpeta raíz del repositorio.
2. Inicia el servidor:
    ```zsh
    java -jar target/WebServer-1.0-SNAPSHOT-jar-with-dependencies.jar puerto ip_zookeeper:puerto_zookeeper
    ``` 
    Ejemplo:
    ```zsh
    java -jar target/WebServer-1.0-SNAPSHOT-jar-with-dependencies.jar 3000 localhost:2181
    ``` 

#### Servidores de Procesamiento

1. Dirígete a la carpeta del servidor de procesamiento:
    ```zsh
    cd ServidorProcesamiento
    ```
    El comando anterior supone que estás en la carpeta raíz del repositorio.

2. Coloca la carpeta que contiene los libros en el directorio ServidorProcesamiento. De otra forma, el programa no funcionará bien

3. Inicia los servidores para cada nodo de procesamiento:
    ```zsh
    java -jar target/ServidorDeProcesamiento-1.0-SNAPSHOT-jar-with-dependencies.jar puerto ip_de_esta_maquina ip_zookeeper:puerto_zookeeper
    ``` 
    Ejemplo:
    ```zsh
    java -jar target/ServidorDeProcesamiento-1.0-SNAPSHOT-jar-with-dependencies.jar 8081 localhost:8081 localhost:2181
    ``` 

**Nota:** El mínimo de nodos de procesamiento para que la aplicación funcione correctamente es de 1 nodo. Y el máximo depende de cuantos archivos tengas en la carpeta de archivos que quieras leer. En el ejemplo de la carpeta que se comparte aquí, hay 46 libros, por tanto el máximo es de 46 nodos. Aunque con 3 funciona bastante bien.


¡Listo! ahora ingresa al servidor web desde cualquier navegador e ingresa la frase que quieras buscar, la aplicación te recomendará libros que coincidan con tu búsqueda.

## Demostración

A continuación se muestran algunos vídeos demostrativos de la aplicación. Si quieres ver más capturas, dirigete al apartado de [capturas](capturas/README.md)

Una disculpa por la cálidad del vídeo, pero no encontré otra forma mejor para grabar ambas funcionalidades al mismo tiempo.

### Con 3 nodos

Aquí se muestra el funcionamiento con los 3 nodos recomendados.

![](capturas/3nodos.mp4) 

### Con 2 nodos

En este vídeo se mata un nodo de procesamiento. Y, al momento de hacer una petición, el servidor distribuye el trabajo en los nodos disponibles.

![](capturas/2nodos.mp4) 

### Con 1 nodo

En este, se matan 2 nodos de procesamiento, lo cuál deja a la aplicación con solo un nodo.

![](capturas/1nodo.mp4) 
