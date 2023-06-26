//Martinez Ramirez Sergi Alberto 4CM12
//Zárate González Erik Daniel 4CM14
//MÉNDEZ MARTÍNEZ YOSELIN ELIZABETH 4CM13

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

package com.mycompany.webserver;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.InputStream;  
import java.util.StringTokenizer;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.*;

public class WebServer {

  private static final String HOME_PAGE_ENDPOINT = "/";
  private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
  private static final String ENDPOINT_PROCESS = "/procesar_datos";
  // private static final String SERVER1 = "http://34.125.244.24/task";
  // private static final String SERVER2 = "http://34.125.5.19/task";
  // private static final String SERVER3 = "http://34.125.205.237/task";
  private static List<String> SERVER_NODES = new ArrayList<String>();
  private static final int NUMERO_DE_ARCHIVOS = 46;
  private int NODE_NUMBERS = 3;

  private static String ZOOKEEPER_ADDRESS = "localhost:2181";
  private static final int SESSION_TIMEOUT = 3000;
  private static final String NODO_OBJETIVO = "/servers";

  private final int port; 
  private HttpServer server; 
  private final ObjectMapper objectMapper;
  private ZooKeeper zooKeeper;

  public WebServer(int port, String zooKeeperAddress) {
    this.port = port;
    this.ZOOKEEPER_ADDRESS = zooKeeperAddress;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public void startServer() {
    try {
      this.server = HttpServer.create(new InetSocketAddress(port), 0);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);
    HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
    taskContext.setHandler(this::handleTaskRequest);
    homePageContext.setHandler(this::handleRequestForAsset);

    server.setExecutor(Executors.newFixedThreadPool(8));
    server.start();

    connectToZooKeeper();
  }

  private void connectToZooKeeper() {
    try {
      System.out.println("ZK: " + ZOOKEEPER_ADDRESS);
      zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, null);
      crearNodoObjetivo();
    } catch (IOException | KeeperException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void crearNodoObjetivo() throws KeeperException, InterruptedException {
    if (zooKeeper.exists(NODO_OBJETIVO, false) == null) {
      zooKeeper.create(NODO_OBJETIVO, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
  }

  private void watchTargetNode() {
    try {
      List<String> serverNodes = zooKeeper.getChildren(NODO_OBJETIVO, null);
      System.out.println("Nodos disponibles:\n" + serverNodes);
      actualizarNodos(serverNodes);
    } catch (KeeperException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void actualizarNodos(List<String> serverNodes) {
    NODE_NUMBERS = serverNodes.size();

    List<String> auxServerNodes = new ArrayList<String>();
    for (String nodo : serverNodes) {
      try {
        byte[] data = zooKeeper.getData(NODO_OBJETIVO + "/" + nodo, false, null);
        auxServerNodes.add("http://" + new String(data) + "/task");
      } catch (KeeperException | InterruptedException e) {
        e.printStackTrace();
      }
    }
    SERVER_NODES = auxServerNodes;
    System.out.println(SERVER_NODES);
  }

  private void handleRequestForAsset(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
      exchange.close();
      return;
    }

    byte[] response;

    String asset = exchange.getRequestURI().getPath(); 

    if (asset.equals(HOME_PAGE_ENDPOINT)) { 
      response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
    } else {
      response = readUiAsset(asset); 
    }
    addContentType(asset, exchange);
    sendResponse(response, exchange);
  }

  private byte[] readUiAsset(String asset) throws IOException {
    InputStream assetStream = getClass().getResourceAsStream(asset);

    if (assetStream == null) {
      return new byte[]{};
    }
    return assetStream.readAllBytes(); 
  }

  private static void addContentType(String asset, HttpExchange exchange) {

    String contentType = "text/html";  
    if (asset.endsWith("js")) {
      contentType = "text/javascript";
    } else if (asset.endsWith("css")) {
      contentType = "text/css";
    }
    exchange.getResponseHeaders().add("Content-Type", contentType);
  }

  private void handleTaskRequest(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
      exchange.close();
      return;
    }

    watchTargetNode();

    try {
      FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class); 
      String frase = frontendSearchRequest.getSearchQuery();
      String[] intervalos = divideNumber(NUMERO_DE_ARCHIVOS);
      List<Task> tasks = new ArrayList<Task>(NODE_NUMBERS);
      for (int i = 0; i < intervalos.length; i++) {
        int minLim = Integer.parseInt((intervalos[i]).split(",")[0]);
        int maxLim = Integer.parseInt((intervalos[i]).split(",")[1]);
        tasks.add(new Task(minLim, maxLim, frase));
      }
      System.out.println("Los intervalos son:");
      for (Task task: tasks) {
        System.out.println("[" + task.obtenerMinLim() + ", " + task.obtenerMaxLim()  + "] : " + task.obtenerFrase());
      }

      Aggregator aggregator = new Aggregator();
      // List<byte[]> results = aggregator.sendTasksToWorkers(Arrays.asList(SERVER1, SERVER2, SERVER3), tasks);
      List<byte[]> results = aggregator.sendTasksToWorkers(SERVER_NODES, tasks);

      Map<String, Double> archivos = juntarListas(results);

      //ordenamos los archivos por relevancia
      List<Map.Entry<String, Double>> sortedFiles = sortByValue(archivos);

      System.out.println("ARCHIVOS POR ORDEN DE RELEVANCIA");
      for (Map.Entry<String, Double> entry : sortedFiles) {
        System.out.println(entry.getKey() + ": " + entry.getValue());
      }

      StringBuilder resultBuilder = new StringBuilder();
      for (int i = 0; i < 5; i++) {
        resultBuilder.append(sortedFiles.get(i).getKey()).append("\n");
      }

      System.out.println(resultBuilder);

      FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(frase, resultBuilder.toString());
      byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse);
      sendResponse(responseBytes, exchange);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
  }

  private String[] divideNumber(int N) {
    int longitudIntervalos = 0;
    String[] intervalos = new String[NODE_NUMBERS];
    if ((N%2) == 0)
    longitudIntervalos = (int) (N/NODE_NUMBERS) + 1;
    else
    longitudIntervalos = (int) (N/NODE_NUMBERS);

    int minLim = 1, maxLim = 0;
    for (int i = 0; i < NODE_NUMBERS; i++) {
      if (i == (NODE_NUMBERS-1)) {
        maxLim = N;
      } else {
        maxLim = (minLim + longitudIntervalos) - 1;
      }
      intervalos[i] = String.valueOf(minLim) + "," + String.valueOf(maxLim);
      minLim = maxLim + 1;
    }

    return intervalos;
  }

  private Map<String, Double> juntarListas(List<byte[]> resultados) {
    Map<String, Double> archivos = new HashMap<String, Double>();
    for (byte[] resultado : resultados) {
      Map<String, Double> archivo = (Map<String, Double>) SerializationUtils.deserialize(resultado);
      archivos.putAll(archivo);
    }
    return archivos;
  }

  private List<Map.Entry<String, Double>> sortByValue(Map<String, Double> map) {
    List<Map.Entry<String, Double>> sortedList = new java.util.ArrayList<>(map.entrySet());
    Collections.sort(sortedList, (a, b) -> a.getValue() < b.getValue() ? 1 : a.getValue() == b.getValue() ? 0 : -1);
    return sortedList;
  }

  private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
    exchange.sendResponseHeaders(200, responseBytes.length);
    OutputStream outputStream = exchange.getResponseBody();
    outputStream.write(responseBytes);
    outputStream.flush();
    outputStream.close();
  }
}
