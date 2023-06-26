//Martinez Ramirez Sergi Alberto 4CM12
//Zárate González Erik Daniel 4CM14
//MÉNDEZ MARTÍNEZ YOSELIN ELIZABETH 4CM13

package com.mycompany.webserver;

public class Task implements java.io.Serializable {
  private int minLim;
  private int maxLim;
  private String frase;

  public Task(int minLim, int maxLim, String frase) {
    this.minLim = minLim;
    this.maxLim = maxLim;
    this.frase = frase;
  }

  public int obtenerMinLim() {
    return this.minLim;
  }

  public int obtenerMaxLim() {
    return this.maxLim;
  }

  public String obtenerFrase() {
    return this.frase;
  }
}
