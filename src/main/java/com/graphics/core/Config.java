package com.graphics.core;

/**
 * Constantes generales del juego.
 */
public final class Config {

    private Config() {
    }

    public static int ANCHO = 900;
    public static int ALTO = 700;

    public static String FONDO_RECURSO = "textures/fondo.jpg";

    public static String FONDO_RUTA_ABSOLUTA =
            "C:\\Users\\andre\\Desktop\\Grafica\\opengl-java-class\\src\\main\\resources\\textures\\fondo.jpg";

    public static float BIRD_X = -0.45f;
    public static float BIRD2_X = -0.30f;
    public static float BIRD3_X = 0.30f;
    public static float BIRD_ANCHO = 0.40f;
    public static float BIRD_ALTO = 0.12f;

    public static float GRAVEDAD = -1.0f;
    public static float IMPULSO_SALTO = 0.60f;
    public static float VELOCIDAD_MAX_CAIDA = -1.4f;

    public static float TUBERIA_ANCHO = 0.18f;
    public static float GAP_ALTO = 0.58f;
    public static float VELOCIDAD_TUBERIAS = 0.65f;
    public static float TIEMPO_ENTRE_TUBERIAS = 1.5f;
    public static float GAP_MIN_CENTRO = -0.45f;
    public static float GAP_MAX_CENTRO = 0.45f;

    public static float TUBERIA_CABEZA_FACTOR_ANCHO = 1.35f;
    public static float TUBERIA_CABEZA_ALTO = 0.12f;

    public static float NIVEL_INCREMENTO_VELOCIDAD = 1.20f;
    public static int NIVELES_POR_AUMENTO = 5;

    public static int PUNTOS_PARA_SUBIR_TECHO = 3;
    public static float VELOCIDAD_AUTO_ASCENSO = 1.8f;
    public static float TIEMPO_AUTO_ASCENSO = 0.5f;

    public static float getBirdInicialVelY() {
        return IMPULSO_SALTO * 0.5f;
    }
}