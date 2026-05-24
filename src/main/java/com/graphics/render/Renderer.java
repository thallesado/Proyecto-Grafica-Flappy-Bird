package com.graphics.render;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.graphics.core.Config;
import com.graphics.core.EstadoJuego;
import com.graphics.model.Pajaro;
import com.graphics.model.Tuberia;


public class Renderer {

    private final long window;

    private int programa;
    private int vao;
    private int vbo;
    private int texturaFondo;

    private int uOffsetLocation;
    private int uScaleLocation;
    private int uRotationLocation;
    private int uColorLocation;
    private int uUseTextureLocation;
    private int uTextureLocation;

    private final TextRenderer textRenderer;

    public Renderer(long window) {
        this.window = window;
        this.textRenderer = new TextRenderer(window);
    }

    /**
     * Inicializa todos los recursos gráficos.
     *
     * Se llama una sola vez desde Game.init().
     */
    public void init() {
        textRenderer.init();

        crearShaders();
        crearQuadBase();

        TextureLoader textureLoader = new TextureLoader();

        texturaFondo = textureLoader.cargarTextura(
                Config.FONDO_RECURSO,
                Config.FONDO_RUTA_ABSOLUTA
        );
    }

    /**
     * Renderiza un frame completo.
     *
     * Game le pasa el estado actual y los objetos que deben dibujarse.
     */
    public void render(
            EstadoJuego estadoJuego,
            List<Tuberia> tuberias,
            Pajaro pajaro1,
            Pajaro pajaro2,
            Pajaro pajaro3,
            int numJugadores,
            int nivelActual
    ) {
            actualizarViewport();

            GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            GL20.glUseProgram(programa);
            GL30.glBindVertexArray(vao);

            dibujarFondoImagen();

        if (estadoJuego == EstadoJuego.MENU) {
            dibujarPantallaInicio();
        } else if (estadoJuego == EstadoJuego.SELECCIONAR_JUGADORES) {
            dibujarPantallaSeleccionarJugadores();
        } else {
            dibujarTuberias(tuberias);

            if (pajaro1.vivo) {
                dibujarPajaro(pajaro1);
            }

            if (numJugadores >= 2 && pajaro2.vivo) {
                dibujarPajaro(pajaro2);
            }

            if (numJugadores == 3 && pajaro3.vivo) {
                dibujarPajaro(pajaro3);
            }

            if (estadoJuego == EstadoJuego.GAME_OVER) {
                dibujarPantallaGameOver();
            }
        }

        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);

        textRenderer.render(
                estadoJuego,
                pajaro1,
                pajaro2,
                pajaro3,
                numJugadores,
                nivelActual
        );
    }

    // SHADERS

    private void crearShaders() {
        String vertexSrc = """
            #version 330 core

            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec2 aTexCoord;

            uniform vec2 uOffset;
            uniform vec2 uScale;
            uniform float uRotation;

            out vec2 vTexCoord;

            void main() {
                float cosRotation = cos(uRotation);
                float sinRotation = sin(uRotation);
                vec2 rotated = vec2(
                    aPos.x * cosRotation - aPos.y * sinRotation,
                    aPos.x * sinRotation + aPos.y * cosRotation
                );
                vec2 finalPos = rotated * uScale + uOffset;
                gl_Position = vec4(finalPos, aPos.z, 1.0);
                vTexCoord = aTexCoord;
            }
            """;

        String fragmentSrc = """
            #version 330 core

            uniform vec3 uColor;
            uniform bool uUseTexture;
            uniform sampler2D uTexture;

            in vec2 vTexCoord;
            out vec4 fragColor;

            void main() {
                if (uUseTexture) {
                    fragColor = texture(uTexture, vTexCoord);
                } else {
                    fragColor = vec4(uColor, 1.0);
                }
            }
            """;

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "Vertex");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "Fragment");

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(programa));
        }

        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uRotationLocation = GL20.glGetUniformLocation(programa, "uRotation");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        uUseTextureLocation = GL20.glGetUniformLocation(programa, "uUseTexture");
        uTextureLocation = GL20.glGetUniformLocation(programa, "uTexture");

        if (uOffsetLocation == -1 ||
                uScaleLocation == -1 ||
                uRotationLocation == -1 ||
                uColorLocation == -1 ||
                uUseTextureLocation == -1 ||
                uTextureLocation == -1) {
            throw new RuntimeException("No se pudieron obtener uniforms del shader");
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    // QUAD BASE

    private void crearQuadBase() {
        float[] vertices = {
            // x,     y,     z,    u,    v
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f,
             0.5f, -0.5f, 0.0f, 1.0f, 1.0f,
             0.5f,  0.5f, 0.0f, 1.0f, 0.0f,

            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f,
             0.5f,  0.5f, 0.0f, 1.0f, 0.0f,
            -0.5f,  0.5f, 0.0f, 0.0f, 0.0f
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // Posición
        GL20.glVertexAttribPointer(
                0,
                3,
                GL11.GL_FLOAT,
                false,
                5 * Float.BYTES,
                0
        );
        GL20.glEnableVertexAttribArray(0);

        // Coordenadas de textura
        GL20.glVertexAttribPointer(
                1,
                2,
                GL11.GL_FLOAT,
                false,
                5 * Float.BYTES,
                3 * Float.BYTES
        );
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    // =========================================================
    // VIEWPORT Y FONDO
    // =========================================================

    private void actualizarViewport() {
        int[] anchoVentana = new int[1];
        int[] altoVentana = new int[1];

        GLFW.glfwGetFramebufferSize(window, anchoVentana, altoVentana);

        GL11.glViewport(0, 0, anchoVentana[0], altoVentana[0]);
    }

    private void dibujarFondoImagen() {
        GL20.glUniform1i(uUseTextureLocation, 1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturaFondo);
        GL20.glUniform1i(uTextureLocation, 0);

        GL20.glUniform2f(uOffsetLocation, 0.0f, 0.0f);
        GL20.glUniform2f(uScaleLocation, 2.0f, 2.0f);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL20.glUniform1i(uUseTextureLocation, 0);
    }

    // =========================================================
    // Dibujado de pantallas
    // =========================================================

    private void dibujarPantallaInicio() {
        dibujarRect(0.0f, 0.05f, 1.26f, 0.62f, 0.10f, 0.12f, 0.18f);

        dibujarRect(0.0f, 0.32f, 1.30f, 0.06f, 1.0f, 0.85f, 0.25f);
        dibujarRect(0.0f, -0.30f, 1.30f, 0.06f, 1.0f, 0.85f, 0.25f);

        dibujarPajaroPixelArt(-0.32f, 0.10f, 0.5f, 1.0f);
    }

    private void dibujarPantallaSeleccionarJugadores() {
        dibujarRect(0.0f, 0.0f, 1.45f, 0.48f, 0.18f, 0.18f, 0.28f);

        dibujarRect(-0.60f, 0.18f, 0.28f, 0.16f, 0.15f, 0.55f, 0.20f);
        dibujarRect(0.0f, 0.18f, 0.28f, 0.16f, 0.15f, 0.55f, 0.20f);
        dibujarRect(0.60f, 0.18f, 0.28f, 0.16f, 0.15f, 0.55f, 0.20f);

        dibujarPajaroPixelArt(-0.60f, 0.18f, 0.5f, 1.0f);
        dibujarPajaroPixelArt(0.0f, 0.18f, 0.5f, 1.0f);
        dibujarPajaroPixelArt(0.60f, 0.18f, 0.5f, 1.0f);
    }

    private void dibujarPantallaGameOver() {
        dibujarRect(0.0f, 0.0f, 1.30f, 0.52f, 0.12f, 0.10f, 0.12f);
        dibujarRect(0.0f, 0.24f, 1.20f, 0.06f, 0.88f, 0.18f, 0.16f);

        dibujarPajaroPixelArt(-0.55f, 0.0f, -1.0f, 1.0f);
        dibujarPajaroPixelArt(0.0f, 0.0f, -1.0f, 1.0f);
        dibujarPajaroPixelArt(0.55f, 0.0f, -1.0f, 1.0f);
    }

    // =========================================================
    // Tuberias
    // =========================================================

    private void dibujarTuberias(List<Tuberia> tuberias) {
        for (Tuberia t : tuberias) {
            float gapTop = t.gapCentroY + (Config.GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (Config.GAP_ALTO * 0.5f);

            float altoSuperior = 1.0f - gapTop;

            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                dibujarTuberia(t.x, yCentroSup, Config.TUBERIA_ANCHO, altoSuperior, true);
            }

            float altoInferior = gapBottom + 1.0f;

            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                dibujarTuberia(t.x, yCentroInf, Config.TUBERIA_ANCHO, altoInferior, false);
            }
        }
    }

    private void dibujarTuberia(float x, float y, float ancho, float alto, boolean superior) {
        // Cuerpo principal
        dibujarRect(x, y, ancho, alto, 0.18f, 0.70f, 0.25f);

        // Brillo izquierdo
        dibujarRect(
                x - ancho * 0.22f,
                y,
                ancho * 0.12f,
                alto,
                0.45f,
                0.95f,
                0.35f
        );

        // Sombra derecha
        dibujarRect(
                x + ancho * 0.30f,
                y,
                ancho * 0.10f,
                alto,
                0.08f,
                0.45f,
                0.16f
        );

        float anchoCabeza = ancho * Config.TUBERIA_CABEZA_FACTOR_ANCHO;
        float altoCabeza = Config.TUBERIA_CABEZA_ALTO;

        float yCabeza;

        if (superior) {
            yCabeza = y - alto * 0.5f;
        } else {
            yCabeza = y + alto * 0.5f;
        }

        // Cabeza de la tubería
        dibujarRect(x, yCabeza, anchoCabeza, altoCabeza, 0.22f, 0.82f, 0.28f);

        // Borde oscuro de la cabeza
        dibujarRect(
                x,
                yCabeza,
                anchoCabeza,
                altoCabeza * 0.25f,
                0.08f,
                0.45f,
                0.16f
        );

        // Brillo de la cabeza
        dibujarRect(
                x - anchoCabeza * 0.22f,
                yCabeza,
                anchoCabeza * 0.10f,
                altoCabeza,
                0.55f,
                1.0f,
                0.40f
        );
    }

    // =========================================================
    // Pajaro clase
    // =========================================================

    private void dibujarPajaro(Pajaro pajaro) {
        float angulo = -pajaro.velY * 0.6f;
        if (angulo > 0.8f) {
            angulo = 0.8f;
        } else if (angulo < -0.5f) {
            angulo = -0.5f;
        }

        dibujarPajaroPixelArt(
                pajaro.x,
                pajaro.y,
                pajaro.velY,
                1.0f,
                pajaro.r,
                pajaro.g,
                pajaro.b,
                angulo
        );
    }

    private void dibujarPajaroPixelArt(float x, float y, float velocidadY, float escala) {
        dibujarPajaroPixelArt(x, y, velocidadY, escala, 0.95f, 0.92f, 0.18f, 0.0f);
    }

    private void dibujarPajaroPixelArt(float x, float y, float velocidadY, float escala, float r, float g, float b, float rotacion) {
        float pixel = 0.012f * escala;

        int wingFrame = velocidadY > 0 ? 1 : 2;

        float bodyR = r;
        float bodyG = g;
        float bodyB = b;
        float bodyDarkR = Math.max(0.0f, r * 0.72f);
        float bodyDarkG = Math.max(0.0f, g * 0.72f);
        float bodyDarkB = Math.max(0.0f, b * 0.56f);

        float[][] colores = {
            {0f, 0f, 0f},          // 0 transparente
            {0.02f, 0.02f, 0.02f}, // 1 negro
            {bodyR, bodyG, bodyB}, // 2 color principal del pájaro
            {bodyDarkR, bodyDarkG, bodyDarkB}, // 3 color sombra del pájaro
            {1.00f, 1.00f, 1.00f}, // 4 blanco
            {0.82f, 0.82f, 0.82f}, // 5 gris
            {1.00f, 0.55f, 0.10f}, // 6 naranja
            {0.95f, 0.28f, 0.05f}  // 7 rojo/naranja
        };

        int[][] spriteAlaArriba = {
            {0,0,0,1,1,1,1,1,1,0,0,0},
            {0,0,1,2,2,2,2,4,4,1,0,0},
            {0,1,2,2,2,2,2,4,1,4,1,0},
            {1,2,2,2,2,2,2,4,4,4,1,0},
            {1,4,4,4,2,2,2,4,1,1,6,1},
            {1,4,4,4,2,2,2,1,6,6,6,1},
            {1,2,2,2,2,2,2,4,1,1,6,1},
            {0,1,2,2,2,2,2,1,6,6,6,1},
            {0,1,4,4,4,2,1,0,1,1,1,0},
            {0,0,1,1,1,1,0,0,0,0,0,0}
        };

        int[][] spriteAlaAbajo = {
            {0,0,0,1,1,1,1,1,1,0,0,0},
            {0,0,1,2,2,2,2,4,4,1,0,0},
            {0,1,2,2,2,2,2,4,1,4,1,0},
            {1,2,2,2,2,2,2,4,4,4,1,0},
            {1,2,2,2,2,2,2,4,1,1,6,1},
            {1,2,2,2,2,2,2,1,6,6,6,1},
            {1,4,4,4,2,2,2,4,1,1,6,1},
            {0,1,4,4,4,2,2,1,6,6,6,1},
            {0,0,1,4,4,4,1,0,1,1,1,0},
            {0,0,0,1,1,1,0,0,0,0,0,0}
        };

        int[][] sprite;

        if (wingFrame == 1) {
            sprite = spriteAlaArriba;
        } else {
            sprite = spriteAlaAbajo;
        }

        dibujarSpritePixelArt(x, y, pixel, sprite, colores, rotacion);
    }

    private void dibujarSpritePixelArt(
            float xCentro,
            float yCentro,
            float pixel,
            int[][] sprite,
            float[][] colores,
            float rotacion
    ) {
        int filas = sprite.length;
        int columnas = sprite[0].length;

        float cosRot = (float) Math.cos(rotacion);
        float sinRot = (float) Math.sin(rotacion);

        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                int colorIndex = sprite[fila][col];

                if (colorIndex == 0) {
                    continue;
                }

                float[] c = colores[colorIndex];

                float dx = (col - (columnas - 1) * 0.5f) * pixel;
                float dy = ((filas - 1) * 0.5f - fila) * pixel;

                float px = xCentro + dx * cosRot - dy * sinRot;
                float py = yCentro + dx * sinRot + dy * cosRot;

                dibujarRect(px, py, pixel, pixel, c[0], c[1], c[2], 0.0f);
            }
        }
    }

    // =========================================================
    // RECTÁNGULO BASE
    // =========================================================

    private void dibujarRect(float x, float y, float ancho, float alto, float r, float g, float b) {
        dibujarRect(x, y, ancho, alto, r, g, b, 0.0f);
    }

    private void dibujarRect(float x, float y, float ancho, float alto, float r, float g, float b, float rotacion) {
        GL20.glUniform1i(uUseTextureLocation, 0);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform1f(uRotationLocation, rotacion);
        GL20.glUniform3f(uColorLocation, r, g, b);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    // =========================================================
    // LIMPIEZA
    // =========================================================

    public void cleanup() {
        if (texturaFondo != 0) {
            GL11.glDeleteTextures(texturaFondo);
        }

        textRenderer.cleanup();

        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
        }

        if (vbo != 0) {
            GL15.glDeleteBuffers(vbo);
        }

        if (programa != 0) {
            GL20.glDeleteProgram(programa);
        }
    }
}