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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class WebClient { //clase WebCLient
  private HttpClient client; //tiene por dato un objeto unico de tipo HttpClient

  //EL constructor crea un objeto HttpClient llamado client y utiliza el protocolo http version 1.1
  public WebClient() {
    this.client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .build();
  }

  //El metodo sendTask recibe la direccion con la que establecera conexion y los datos a enviar hacia el servidor
  //Devuelve los CompletableFuture en un tipo String
  public CompletableFuture<byte[]> sendTask(String url, byte[] requestPayload) {
    //crea un objeto HttpRequest este permite construir una solicitud http con el metodo post y la direccion de destino
    HttpRequest request = HttpRequest.newBuilder()
      .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
      .uri(URI.create(url))
      .build();

    //Metodo sendAsync para enviar una solicitud request deuna manera asincrona 
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(HttpResponse::body);
  }
}
