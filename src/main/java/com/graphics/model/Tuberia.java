package com.graphics.model;

/**
 * Representa una tubería del juego.
 */
public class Tuberia {

    public float x;
    public float gapCentroY;

    public boolean puntuadoP1;
    public boolean puntuadoP2;
    public boolean puntuadoP3;

    public Tuberia(float x, float gapCentroY) {
        this.x = x;
        this.gapCentroY = gapCentroY;
        this.puntuadoP1 = false;
        this.puntuadoP2 = false;
        this.puntuadoP3 = false;
    }
}