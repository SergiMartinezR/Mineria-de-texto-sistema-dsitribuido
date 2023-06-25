/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.Comparator;

public class ServidorDeProcesamiento {
  private static final String TASK_ENDPOINT = "/task";
  private static final String RUTA_DEL_DIRECTORIO = "../LIBROS_TXT/";

  private final int port;
  private HttpServer server;

  public static void main(String[] args) {
    int serverPort = 8080;
    if (args.length == 1) {
      serverPort = Integer.parseInt(args[0]);
    }

    ServidorDeProcesamiento webServer = new ServidorDeProcesamiento(serverPort);
    webServer.startServer();

    System.out.println("Servidor escuchando en el puerto " + serverPort);
  }

  public ServidorDeProcesamiento(int port) {
    this.port = port;
  }

  public void startServer() {
    try {
      this.server = HttpServer.create(new InetSocketAddress(port), 0);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    HttpContext taskContext = server.createContext(TASK_ENDPOINT);

    taskContext.setHandler(this::handleTaskRequest);

    server.setExecutor(Executors.newFixedThreadPool(8));
    server.start();
  }

  private void handleTaskRequest(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
      exchange.close();
      return;
    }

    byte[] requestBytes = exchange.getRequestBody().readAllBytes();
    byte[] responseBytes = calculateResponse(requestBytes);

    sendResponse(responseBytes, exchange);
  }

  private byte[] calculateResponse(byte[] requestBytes) {
    String bodyString = new String(requestBytes);
    List<String> palabras = obtenerPalabrasDeCadena(bodyString);
    File directorio = new File(RUTA_DEL_DIRECTORIO);
    List<File> archivos = Arrays.asList(directorio.listFiles());
    // List<File> archivos = Arrays.asList(new File("../LIBROS_TXT/Hitler,_Adolf__1935_._Mi_lucha_[11690].txt"));
    
    Map<String, Map<String, Integer>> palabrasContadas = contarPalabrasEnArchivos(palabras, archivos);

    for (Map.Entry<String, Map<String, Integer>> archivo : palabrasContadas.entrySet()) {
      System.out.print("\nArchivo " + archivo.getKey() + " | ");
      for (Map.Entry<String, Integer> palabra : archivo.getValue().entrySet())
        System.out.print("{" + palabra.getKey() + ":" + palabra.getValue() + "}, ");
    }
    
    Map<String, Double> tfidfscore = calcularTfIdf(palabrasContadas);

    //ordenamos los archivos por relevancia
    List<Map.Entry<String, Double>> sortedFiles = sortByValue(tfidfscore);

    StringBuilder resultBuilder = new StringBuilder();
    for (Map.Entry<String, Double> entry : sortedFiles) {
      resultBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    }

    String result = resultBuilder.toString();
    System.out.println("Resultado:\n" + result);

    return result.getBytes();

    // BigInteger result = BigInteger.ONE;
    //
    // for (int i = minVal; i <= maxVal; i++) {
    //   result = result.multiply(BigInteger.valueOf(i));
    // }
    //
    // System.out.println(String.format("El resultado de la multiplicación es: %s\n", result));
    // return result.toString().getBytes();
  }

  private Map<String, Map<String, Integer>> contarPalabrasEnArchivos(List<String> palabras, List<File> archivos) {
    Map<String, Map<String, Integer>> palabrasEnArchivos = new HashMap<String, Map<String, Integer>>();
    for (File archivo : archivos) {
      File file = archivo;
      Map<String, Integer> hashMap = new HashMap<String, Integer>();
      int noPalabrasDelArchivo = 0;
      try(Scanner sc = new Scanner(new FileInputStream(file))){

        while(sc.hasNext()){
          String palabraTexto = sc.next();
          if(palabraTexto.indexOf("\\")==-1) {
            palabraTexto = eliminarCaracteresCadena(palabraTexto);
            // System.out.print("\n" + palabraTexto + " | " );
            int indice = palabras.indexOf(palabraTexto);
            if (indice != -1) {
              String palabra = palabras.get(indice);
              // System.out.print("Si");
              if (hashMap.containsKey(palabra)) {
                int aux = hashMap.get(palabra);
                aux++;
                hashMap.put(palabra, aux++);
              } else {
                hashMap.put(palabra, 1);
              }
            }
          }
        }

      } catch (Exception e) {
        System.out.println("Archivo no encontrado");
      }
      palabrasEnArchivos.put(archivo.getName(), hashMap);
    } 
    return palabrasEnArchivos;
  }

  private Map<String, Double> calcularTfIdf(Map<String, Map<String, Integer>> palabrasContadas) {
    Map<String, Double> tfIdfScores = new HashMap<>();

    int numDocumentos = palabrasContadas.size();

    for (Map.Entry<String, Map<String, Integer>> archivo : palabrasContadas.entrySet()) {
      String nombreArchivo = archivo.getKey();
      Map<String, Integer> palabrasEnArchivo = archivo.getValue();

      for (Map.Entry<String, Integer> palabra : palabrasEnArchivo.entrySet()) {
        String palabraActual = palabra.getKey();
        int frecuenciaEnDocumento = palabra.getValue();

        int documentosConPalabra = 0;
        for (Map<String, Integer> palabrasEnOtroArchivo : palabrasContadas.values()) {
          if (palabrasEnOtroArchivo.containsKey(palabraActual)) {
            documentosConPalabra++;
          }
        }

        double tf = (double) frecuenciaEnDocumento / palabrasEnArchivo.size();
        double idf = Math.log((double) numDocumentos / (1 + documentosConPalabra));

        double tfIdf = tf * idf;

        tfIdfScores.put(nombreArchivo, tfIdf);
      }
    }

    return tfIdfScores;
  }

   private List<Map.Entry<String, Double>> sortByValue(Map<String, Double> map) {
    List<Map.Entry<String, Double>> sortedList = new java.util.ArrayList<>(map.entrySet());
    sortedList.sort(Comparator.comparing(Map.Entry::getValue));
    return sortedList;
  }

  private List<String> obtenerPalabrasDeCadena(String cadena) {
    List<String> palabras = Arrays.asList(cadena.split("[\\pP\\s&&[^']]+"));
    for (int i = 0; i < palabras.size(); ++i)
      palabras.set(i, palabras.get(i).toLowerCase());
    return palabras;
  }

  private String eliminarCaracteresCadena(String cadena) {
    List<String> palabras = Arrays.asList(cadena.split("[\\pP\\s&&[^']]+"));
    if (palabras.size() != 0) {
      return palabras.get(0).toLowerCase();
    } else {
      return null;
    }
  }

  private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
    exchange.sendResponseHeaders(200, responseBytes.length);
    OutputStream outputStream = exchange.getResponseBody();
    outputStream.write(responseBytes);
    outputStream.flush();
    outputStream.close();
    exchange.close();
  }
}
