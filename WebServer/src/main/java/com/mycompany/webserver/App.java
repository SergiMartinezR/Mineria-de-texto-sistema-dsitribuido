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

import java.io.IOException;

public class App {

  public static void main(String[] args) throws IOException, InterruptedException {
    int currentServerPort = 3000;
    String zooKeeperAddress = "localhost";
    if (args.length == 2) {
      currentServerPort = Integer.parseInt(args[0]);
      zooKeeperAddress = args[1];
    } else {
      System.out.println("Uso: java -jar WebServer puerto IP_zookeeper");
      System.exit(1);
    }
    App application = new App();

    WebServer webServer = new WebServer(currentServerPort, zooKeeperAddress);
    webServer.startServer();

    System.out.println("Servidor escuchando en el puerto: " + currentServerPort);
  }
}
