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

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Aggregator { //clase Aggregator tiene como unico dato un objeto de tipo webClient
  private final ObjectMapper objectMapper;
  private WebClient webClient;

  public Aggregator() { //el constructor instacia ub objeto webClient
    this.webClient = new WebClient();
    this.objectMapper = new ObjectMapper();
  }

  //Unico metodo de la clase Aggregator es sendTasksToWorkers
  //Este metodo recibe una lista de los trabajadores y la lista de las tareas
  public List<byte[]> sendTasksToWorkers(List<String> workersAddresses, List<Task> tasks) {
    //comunicacion asincrona de la clase CompletableFuture
    //arreglo futures para almacenar las respuestas futuras de los dos servidores
    CompletableFuture<byte[]>[] futures = new CompletableFuture[workersAddresses.size()];

    //Itera los elementos de la lista y se obtienen las direcciones de cada uno de los trabajadores asi como cada una de las tareas que estan en la lista
    for (int i = 0; i < workersAddresses.size(); i++) {
      String workerAddress = workersAddresses.get(i);
      Task task = tasks.get(i);

      //almacena las tareas en un formato de byte y se envian las tareas asincronas con el metodo sendTask 
      byte[] requestPayload = SerializationUtils.serialize(task);
      futures[i] = webClient.sendTask(workerAddress, requestPayload);
    }

    //Declaracion de la lista de resultados 
    //conforme lleguen se van agregando a la lista
    List<byte[]> results = new ArrayList();
    for (int i = 0; i < workersAddresses.size(); i++) {
      results.add(futures[i].join());
    }

    //Egresa la lista de todas las respuestas asincronas 
    return results;
  }
}
