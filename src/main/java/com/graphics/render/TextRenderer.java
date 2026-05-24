package com.graphics.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

import com.graphics.core.EstadoJuego;
import com.graphics.model.Pajaro;

/**
 * Renderiza textos en pantalla usando NanoVG.
 */
public class TextRenderer {

    private final long window;
    private long vg;

    public TextRenderer(long window) {
        this.window = window;
    }

    public void init() {
        vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);

        if (vg == 0) {
            throw new RuntimeException("No se pudo inicializar NanoVG");
        }

        int fuente = NanoVG.nvgCreateFont(vg, "fuente", "C:\\Windows\\Fonts\\arial.ttf");

        if (fuente == -1) {
            throw new RuntimeException("No se pudo cargar la fuente Arial");
        }
    }

    public void render(
            EstadoJuego estadoJuego,
            Pajaro pajaro1,
            Pajaro pajaro2,
            Pajaro pajaro3,
            int numJugadores,
            int nivelActual
    ) {
        int[] anchoVentana = new int[1];
        int[] altoVentana = new int[1];
        int[] anchoFrame = new int[1];
        int[] altoFrame = new int[1];

        GLFW.glfwGetWindowSize(window, anchoVentana, altoVentana);
        GLFW.glfwGetFramebufferSize(window, anchoFrame, altoFrame);

        if (anchoVentana[0] <= 0 || altoVentana[0] <= 0) {
            return;
        }

        float pixelRatio = (float) anchoFrame[0] / (float) anchoVentana[0];

        NanoVG.nvgBeginFrame(vg, anchoVentana[0], altoVentana[0], pixelRatio);

        if (estadoJuego == EstadoJuego.MENU) {
            float centerX = anchoVentana[0] / 2.0f;

            dibujarTextoConFondoCentrado(
                    "FLAPPY BIRD OPENGL",
                    centerX,
                    altoVentana[0] * 0.24f,
                    36,
                    anchoVentana[0] * 0.70f,
                    altoVentana[0] * 0.10f,
                    16.0f,
                    0.95f, 0.92f, 0.25f,
                    0.05f, 0.05f, 0.05f
            );

            dibujarTextoConFondoCentrado(
                    "Presiona ENTER para seleccionar jugadores",
                    centerX,
                    altoVentana[0] * 0.42f,
                    26,
                    anchoVentana[0] * 0.72f,
                    altoVentana[0] * 0.08f,
                    12.0f,
                    0.20f, 0.22f, 0.26f,
                    1.0f, 1.0f, 1.0f
            );

            dibujarTextoConFondoCentrado(
                    "O presiona 1, 2 o 3 directamente para comenzar",
                    centerX,
                    altoVentana[0] * 0.54f,
                    22,
                    anchoVentana[0] * 0.76f,
                    altoVentana[0] * 0.08f,
                    10.0f,
                    0.14f, 0.45f, 0.70f,
                    1.0f, 1.0f, 1.0f
            );
        }

        if (estadoJuego == EstadoJuego.SELECCIONAR_JUGADORES) {
            float centerX = anchoVentana[0] / 2.0f;

            dibujarTextoConFondoCentrado(
                    "Selecciona numero de jugadores",
                    centerX,
                    altoVentana[0] * 0.25f,
                    32,
                    anchoVentana[0] * 0.75f,
                    altoVentana[0] * 0.09f,
                    14.0f,
                    0.18f, 0.20f, 0.26f,
                    1.0f, 1.0f, 1.0f
            );

            dibujarTextoConFondoCentrado(
                    "Presiona 1 para 1 Jugador",
                    centerX,
                    altoVentana[0] * 0.17f,
                    28,
                    anchoVentana[0] * 0.64f,
                    altoVentana[0] * 0.08f,
                    12.0f,
                    0.10f, 0.50f, 0.10f,
                    1.0f, 1.0f, 1.0f
            );

            dibujarTextoConFondoCentrado(
                    "Presiona 2 para 2 Jugadores",
                    centerX,
                    altoVentana[0] * 0.33f,
                    28,
                    anchoVentana[0] * 0.68f,
                    altoVentana[0] * 0.08f,
                    12.0f,
                    0.10f, 0.50f, 0.10f,
                    1.0f, 1.0f, 1.0f
            );
            dibujarTextoConFondoCentrado(
                    "Presiona 3 para 3 Jugadores",
                    centerX,
                    altoVentana[0] * 0.49f,
                    28,
                    anchoVentana[0] * 0.68f,
                    altoVentana[0] * 0.08f,
                    12.0f,
                    0.10f, 0.50f, 0.10f,
                    1.0f, 1.0f, 1.0f
            );
            dibujarTextoConFondoCentrado(
                    "Teclas: SPACE P1, UP P2, W P3",
                    centerX,
                    altoVentana[0] * 0.65f,
                    24,
                    anchoVentana[0] * 0.70f,
                    altoVentana[0] * 0.08f,
                    10.0f,
                    0.15f, 0.35f, 0.60f,
                    1.0f, 1.0f, 1.0f
            );
        }

        if (estadoJuego == EstadoJuego.JUGANDO) {
            dibujarTexto("Nivel: " + nivelActual, anchoVentana[0] / 2.0f - 60, 35, 24, 1.0f, 1.0f, 1.0f);

            if (numJugadores == 1) {
                dibujarTexto("Puntos: " + pajaro1.puntaje, 25, 35, 28, 1.0f, 1.0f, 1.0f);
            } else if (numJugadores == 2) {
                dibujarTexto("P1: " + pajaro1.puntaje, 25, 35, 24, 1.0f, 1.0f, 1.0f);
                dibujarTexto("P2: " + pajaro2.puntaje, anchoVentana[0] - 120, 35, 24, 1.0f, 1.0f, 1.0f);
            } else if (numJugadores == 3) {
                dibujarTexto("P1: " + pajaro1.puntaje, 25, 35, 24, 1.0f, 1.0f, 1.0f);
                dibujarTexto("P2: " + pajaro2.puntaje, anchoVentana[0] / 2.0f - 40, 35, 24, 1.0f, 1.0f, 1.0f);
                dibujarTexto("P3: " + pajaro3.puntaje, anchoVentana[0] - 120, 35, 24, 1.0f, 1.0f, 1.0f);
            }
        }

        if (estadoJuego == EstadoJuego.GAME_OVER) {
            float centerX = anchoVentana[0] / 2.0f;

            dibujarTextoConFondoCentrado(
                    "GAME OVER",
                    centerX,
                    altoVentana[0] * 0.33f,
                    44,
                    anchoVentana[0] * 0.55f,
                    altoVentana[0] * 0.10f,
                    14.0f,
                    0.18f, 0.10f, 0.12f,
                    1.0f, 0.16f, 0.16f
            );

            if (numJugadores == 1) {
                dibujarTextoCentrado("Puntos: " + pajaro1.puntaje, centerX, altoVentana[0] * 0.47f, 30, 1.0f, 1.0f, 1.0f);
            } else if (numJugadores == 2) {
                dibujarTextoCentrado("P1: " + pajaro1.puntaje + "  |  P2: " + pajaro2.puntaje, centerX, altoVentana[0] * 0.47f, 28, 1.0f, 1.0f, 1.0f);
            } else {
                dibujarTextoCentrado(
                        "P1: " + pajaro1.puntaje + "  |  P2: " + pajaro2.puntaje + "  |  P3: " + pajaro3.puntaje,
                        centerX,
                        altoVentana[0] * 0.47f,
                        26,
                        1.0f,
                        1.0f,
                        1.0f
                );
            }

            dibujarTextoCentrado("Presiona R para reiniciar", centerX, altoVentana[0] * 0.57f, 24, 0.95f, 0.95f, 0.95f);
        }

        NanoVG.nvgEndFrame(vg);
    }

    private void dibujarTexto(String texto, float x, float y, float tamano, float r, float g, float b) {
        try (NVGColor color = NVGColor.calloc()) {
            color.r(r);
            color.g(g);
            color.b(b);
            color.a(1.0f);

            NanoVG.nvgFontSize(vg, tamano);
            NanoVG.nvgFontFace(vg, "fuente");
            NanoVG.nvgFillColor(vg, color);
            NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
            NanoVG.nvgText(vg, x, y, texto);
        }
    }

    private void dibujarTextoCentrado(String texto, float x, float y, float tamano, float r, float g, float b) {
        try (NVGColor color = NVGColor.calloc()) {
            color.r(r);
            color.g(g);
            color.b(b);
            color.a(1.0f);

            NanoVG.nvgFontSize(vg, tamano);
            NanoVG.nvgFontFace(vg, "fuente");
            NanoVG.nvgFillColor(vg, color);
            NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
            NanoVG.nvgText(vg, x, y, texto);
        }
    }

    private void dibujarTextoConFondoCentrado(
            String texto,
            float x,
            float y,
            float tamano,
            float anchoFondo,
            float altoFondo,
            float radioEsquinas,
            float fondoR,
            float fondoG,
            float fondoB,
            float textoR,
            float textoG,
            float textoB
    ) {
        try (NVGColor colorFondo = NVGColor.calloc(); NVGColor colorTexto = NVGColor.calloc()) {
            colorFondo.r(fondoR);
            colorFondo.g(fondoG);
            colorFondo.b(fondoB);
            colorFondo.a(0.90f);

            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRoundedRect(vg, x - anchoFondo * 0.5f, y - altoFondo * 0.5f, anchoFondo, altoFondo, radioEsquinas);
            NanoVG.nvgFillColor(vg, colorFondo);
            NanoVG.nvgFill(vg);

            colorTexto.r(textoR);
            colorTexto.g(textoG);
            colorTexto.b(textoB);
            colorTexto.a(1.0f);

            NanoVG.nvgFontSize(vg, tamano);
            NanoVG.nvgFontFace(vg, "fuente");
            NanoVG.nvgFillColor(vg, colorTexto);
            NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
            NanoVG.nvgText(vg, x, y, texto);
        }
    }

    public void cleanup() {
        if (vg != 0) {
            NanoVGGL3.nvgDelete(vg);
        }
    }
}