package com.graphics.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.graphics.model.Pajaro;
import com.graphics.model.Tuberia;
import com.graphics.render.Renderer;
/**
 * Controla la lógica principal del juego:
 * ventana, input, estados, física, colisiones, puntajes y loop.
 */
public class Game {
/* Color de pajaros */
    private long window;

    private final Pajaro pajaro1 = new Pajaro(
            Config.BIRD_X,
            0.0f,
            Config.BIRD_ANCHO,
            Config.BIRD_ALTO,
            0.95f,
            0.92f,
            0.18f
    );

    private final Pajaro pajaro2 = new Pajaro(
            Config.BIRD2_X,
            0.0f,
            Config.BIRD_ANCHO,
            Config.BIRD_ALTO,
            0.12f,
            0.45f,
            0.95f
    );

    private final Pajaro pajaro3 = new Pajaro(
            Config.BIRD3_X,
            0.0f,
            Config.BIRD_ANCHO,
            Config.BIRD_ALTO,
            0.95f,
            0.18f,
            0.18f
    );

    private final List<Tuberia> tuberias = new ArrayList<>();
    private final Random random = new Random();

    private Renderer renderer;

    private EstadoJuego estadoJuego;

    private float timerSpawn;
    private int numJugadores;

    private boolean prevSpace;
    private boolean prevUp;
    private boolean prevW;
    private boolean prevR;
    private boolean prevEnter;
    private boolean prev1;
    private boolean prev2;
    private boolean prev3;

public void run() {

    init();
    resetGame();
    loop();
    cleanup();
}
    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);

        window = GLFW.glfwCreateWindow(
                Config.ANCHO,
                Config.ALTO,
                "Flappy Bird OpenGL",
                0,
                0
        );

        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        GLFW.glfwMakeContextCurrent(window);

        GLFW.glfwSetFramebufferSizeCallback(window, (windowHandle, width, height) -> {
            GL11.glViewport(0, 0, width, height);
        });

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();

        renderer = new Renderer(window);
        renderer.init();
    }

    private void resetGame() {
        pajaro1.reset(
                Config.BIRD_X,
                0.0f,
                0.0f,
                Config.BIRD_ANCHO,
                Config.BIRD_ALTO,
                true
        );
        pajaro2.reset(
                Config.BIRD2_X,
                0.0f,
                0.0f,
                Config.BIRD_ANCHO,
                Config.BIRD_ALTO,
                false
        );
        pajaro3.reset(
                Config.BIRD3_X,
                0.0f,
                0.0f,
                Config.BIRD_ANCHO,
                Config.BIRD_ALTO,
                false
        );

        timerSpawn = 0.0f;
        numJugadores = 1;

        prevSpace = false;
        prevUp = false;
        prevW = false;
        prevR = false;
        prevEnter = false;
        prev1 = false;
        prev2 = false;
        prev3 = false;

        tuberias.clear();

        estadoJuego = EstadoJuego.MENU;

        actualizarTitulo();
    }

    private void iniciarJuego() {
        pajaro1.reset(
                Config.BIRD_X,
                0.0f,
                Config.getBirdInicialVelY(),
                Config.BIRD_ANCHO,
                Config.BIRD_ALTO,
                true
        );
        pajaro2.reset(
                Config.BIRD2_X,
                0.0f,
                Config.getBirdInicialVelY(),
                Config.BIRD_ANCHO,
                Config.BIRD_ALTO,
                numJugadores >= 2
        );
        pajaro3.reset(
                Config.BIRD3_X,
                0.0f,
                Config.getBirdInicialVelY(),
                Config.BIRD_ANCHO,
                Config.BIRD_ALTO,
                numJugadores == 3
        );

        timerSpawn = 0.0f;
        tuberias.clear();

        estadoJuego = EstadoJuego.JUGANDO;

        actualizarTitulo();
    }

    private void procesarInput() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        boolean enterAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS;

        if (enterAhora && !prevEnter) {
            if (estadoJuego == EstadoJuego.MENU) {
                estadoJuego = EstadoJuego.SELECCIONAR_JUGADORES;
            }
        }

        prevEnter = enterAhora;

        if (estadoJuego == EstadoJuego.SELECCIONAR_JUGADORES || estadoJuego == EstadoJuego.MENU) {
            boolean uno = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_1) == GLFW.GLFW_PRESS;
            boolean dos = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_2) == GLFW.GLFW_PRESS;
            boolean tres = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_3) == GLFW.GLFW_PRESS;

            if (uno && !prev1) {
                numJugadores = 1;
                iniciarJuego();
            }

            if (dos && !prev2) {
                numJugadores = 2;
                iniciarJuego();
            }

            if (tres && !prev3) {
                numJugadores = 3;
                iniciarJuego();
            }

            prev1 = uno;
            prev2 = dos;
            prev3 = tres;
        }

        if (estadoJuego == EstadoJuego.JUGANDO) {
            boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;

            if (spaceAhora && !prevSpace) {
                pajaro1.saltar(Config.IMPULSO_SALTO);
            }

            prevSpace = spaceAhora;

            boolean upAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;

            if (numJugadores >= 2 && upAhora && !prevUp) {
                pajaro2.saltar(Config.IMPULSO_SALTO);
            }

            prevUp = upAhora;

            boolean wAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;

            if (numJugadores == 3 && wAhora && !prevW) {
                pajaro3.saltar(Config.IMPULSO_SALTO);
            }

            prevW = wAhora;
        }

        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;

        if (rAhora && !prevR) {
            if (estadoJuego == EstadoJuego.GAME_OVER) {
                resetGame();
            }
        }

        prevR = rAhora;
    }

    private void actualizar(float dt) {
        if (estadoJuego != EstadoJuego.JUGANDO) {
            return;
        }

        actualizarPajaro(pajaro1, dt);

        if (numJugadores >= 2) {
            actualizarPajaro(pajaro2, dt);
        }

        if (numJugadores == 3) {
            actualizarPajaro(pajaro3, dt);
        }

        if (!pajaro1.vivo) {
            if (numJugadores == 1
                    || (numJugadores == 2 && !pajaro2.vivo)
                    || (numJugadores == 3 && !pajaro2.vivo && !pajaro3.vivo)) {
                estadoJuego = EstadoJuego.GAME_OVER;
                actualizarTitulo();
                return;
            }
        }

        timerSpawn += dt;

        if (timerSpawn >= Config.TIEMPO_ENTRE_TUBERIAS) {
            timerSpawn = 0.0f;
            spawnTuberia();
        }

        float velocidadActual = getVelocidadTuberiasActual();

        Iterator<Tuberia> it = tuberias.iterator();

        while (it.hasNext()) {
            Tuberia t = it.next();

            t.x -= velocidadActual * dt;

            actualizarPuntaje(t);
            revisarColisiones(t);

            if (t.x + (Config.TUBERIA_ANCHO * Config.TUBERIA_CABEZA_FACTOR_ANCHO * 0.5f) < -1.3f) {
                it.remove();
            }
        }
    }

    private void actualizarPajaro(Pajaro pajaro, float dt) {
        if (pajaro.puntaje >= Config.PUNTOS_PARA_SUBIR_TECHO) {
            if (pajaro.temporizadorAutoAscenso <= 0.0f) {
                pajaro.temporizadorAutoAscenso = Config.TIEMPO_AUTO_ASCENSO;
            } else {
                pajaro.temporizadorAutoAscenso -= dt;
            }

            if (pajaro.temporizadorAutoAscenso <= 0.0f) {
                pajaro.velY = Math.max(pajaro.velY, Config.VELOCIDAD_AUTO_ASCENSO);
            }
        } else {
            pajaro.temporizadorAutoAscenso = 0.0f;
        }

        pajaro.aplicarFisica(
                dt,
                Config.GRAVEDAD,
                Config.VELOCIDAD_MAX_CAIDA
        );

        if (pajaro.tocaTechoOSuelo()) {
            pajaro.vivo = false;
        }
    }

    private void actualizarPuntaje(Tuberia t) {
        if (t.x + (Config.TUBERIA_ANCHO * 0.5f) < pajaro1.x && !t.puntuadoP1 && pajaro1.vivo) {
            t.puntuadoP1 = true;
            pajaro1.puntaje++;
        }

        if (numJugadores >= 2 &&
                t.x + (Config.TUBERIA_ANCHO * 0.5f) < pajaro2.x &&
                !t.puntuadoP2 &&
                pajaro2.vivo) {
            t.puntuadoP2 = true;
            pajaro2.puntaje++;
        }

        if (numJugadores == 3 &&
                t.x + (Config.TUBERIA_ANCHO * 0.5f) < pajaro3.x &&
                !t.puntuadoP3 &&
                pajaro3.vivo) {
            t.puntuadoP3 = true;
            pajaro3.puntaje++;
        }
    }

    private void revisarColisiones(Tuberia t) {
        if (pajaro1.vivo && colisionaConTuberia(pajaro1, t)) {
            pajaro1.vivo = false;
        }

        if (numJugadores >= 2 && pajaro2.vivo && colisionaConTuberia(pajaro2, t)) {
            pajaro2.vivo = false;
        }

        if (numJugadores == 3 && pajaro3.vivo && colisionaConTuberia(pajaro3, t)) {
            pajaro3.vivo = false;
        }
    }

    private void spawnTuberia() {
        float gapCentro = Config.GAP_MIN_CENTRO
                + random.nextFloat() * (Config.GAP_MAX_CENTRO - Config.GAP_MIN_CENTRO);

        tuberias.add(new Tuberia(1.2f, gapCentro));
    }

    private boolean colisionaConTuberia(Pajaro pajaro, Tuberia t) {
        float birdLeft = pajaro.x - (pajaro.ancho * 0.5f);
        float birdRight = pajaro.x + (pajaro.ancho * 0.5f);
        float birdBottom = pajaro.y - (pajaro.alto * 0.5f);
        float birdTop = pajaro.y + (pajaro.alto * 0.5f);

        float anchoColisionTubo = Config.TUBERIA_ANCHO * Config.TUBERIA_CABEZA_FACTOR_ANCHO;

        float pipeLeft = t.x - (anchoColisionTubo * 0.5f);
        float pipeRight = t.x + (anchoColisionTubo * 0.5f);

        boolean overlapX = birdRight > pipeLeft && birdLeft < pipeRight;

        if (!overlapX) {
            return false;
        }

        float gapTop = t.gapCentroY + (Config.GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (Config.GAP_ALTO * 0.5f);

        float limiteSuperiorVisual = gapTop - (Config.TUBERIA_CABEZA_ALTO * 0.5f);
        float limiteInferiorVisual = gapBottom + (Config.TUBERIA_CABEZA_ALTO * 0.5f);

        return birdTop > limiteSuperiorVisual || birdBottom < limiteInferiorVisual;
    }

    private int getNivelActual() {
        int mejorPuntaje = pajaro1.puntaje;

        if (numJugadores == 2) {
            mejorPuntaje = Math.max(pajaro1.puntaje, pajaro2.puntaje);
        } else if (numJugadores == 3) {
            mejorPuntaje = Math.max(pajaro1.puntaje, Math.max(pajaro2.puntaje, pajaro3.puntaje));
        }

        return 1 + (mejorPuntaje / Config.NIVELES_POR_AUMENTO);
    }

    private float getVelocidadTuberiasActual() {
        int nivel = getNivelActual();

        return Config.VELOCIDAD_TUBERIAS
                * (1.0f + ((nivel - 1) * (Config.NIVEL_INCREMENTO_VELOCIDAD - 1.0f)));
    }

    private void render() {
        renderer.render(
                estadoJuego,
                tuberias,
                pajaro1,
                pajaro2,
                pajaro3,
                numJugadores,
                getNivelActual()
        );
    }

    private void actualizarTitulo() {
        GLFW.glfwSetWindowTitle(window, "Flappy Bird OpenGL");
    }

    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();

        while (!GLFW.glfwWindowShouldClose(window)) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;

            if (dt > 0.0167f) {
                dt = 0.0167f;
            }

            procesarInput();
            actualizar(dt);
            render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void cleanup() {
        if (renderer != null) {
            renderer.cleanup();
        }

        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}