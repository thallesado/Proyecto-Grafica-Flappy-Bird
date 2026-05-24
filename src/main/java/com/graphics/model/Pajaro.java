package com.graphics.model;

/**
 * Representa un jugador/pájaro dentro del juego.
 */
public class Pajaro {

    public float x;
    public float y;
    public float velY;

    public float ancho;
    public float alto;

    public float r;
    public float g;
    public float b;

    public boolean vivo;
    public int puntaje;
    public float temporizadorAutoAscenso;

    public Pajaro(float x, float y, float ancho, float alto, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.r = r;
        this.g = g;
        this.b = b;
        this.vivo = true;
        this.puntaje = 0;
        this.temporizadorAutoAscenso = 0.0f;
        this.velY = 0.0f;
    }

    public void reset(float y, float velY, boolean vivo) {
        reset(this.x, y, velY, this.ancho, this.alto, vivo);
    }

    public void reset(float x, float y, float velY, float ancho, float alto, boolean vivo) {
        this.x = x;
        this.y = y;
        this.velY = velY;
        this.ancho = ancho;
        this.alto = alto;
        this.vivo = vivo;
        this.puntaje = 0;
        this.temporizadorAutoAscenso = 0.0f;
    }

    public void saltar(float impulsoSalto) {
        if (vivo) {
            this.velY = impulsoSalto;
        }
    }

    public void aplicarFisica(float dt, float gravedad, float velocidadMaxCaida) {
        if (!vivo) {
            return;
        }

        velY += gravedad * dt;

        if (velY < velocidadMaxCaida) {
            velY = velocidadMaxCaida;
        }

        y += velY * dt;
    }

    public boolean tocaTechoOSuelo() {
        float top = y + (alto * 0.5f);
        float bottom = y - (alto * 0.5f);

        return top >= 1.0f || bottom <= -1.0f;
    }
}