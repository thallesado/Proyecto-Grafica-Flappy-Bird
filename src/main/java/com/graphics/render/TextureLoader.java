package com.graphics.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import com.graphics.AppFlappyBird;

/**
 * Carga texturas desde resources o desde una ruta absoluta de respaldo.
 */
public class TextureLoader {

    public int cargarTextura(String rutaRecurso, String rutaAbsolutaRespaldo) {
        String rutaFinal = resolverRutaTextura(rutaRecurso, rutaAbsolutaRespaldo);

        int texturaId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturaId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ancho = stack.mallocInt(1);
            IntBuffer alto = stack.mallocInt(1);
            IntBuffer canales = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);

            ByteBuffer imagen = STBImage.stbi_load(rutaFinal, ancho, alto, canales, 4);

            if (imagen == null) {
                throw new RuntimeException(
                        "No se pudo cargar la textura.\nRuta usada: " + rutaFinal
                                + "\nMotivo: " + STBImage.stbi_failure_reason()
                );
            }

            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL11.GL_RGBA,
                    ancho.get(0),
                    alto.get(0),
                    0,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    imagen
            );

            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            STBImage.stbi_image_free(imagen);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return texturaId;
    }

    private String resolverRutaTextura(String rutaRecurso, String rutaAbsolutaRespaldo) {
        try {
            java.net.URL recurso = AppFlappyBird.class.getClassLoader().getResource(rutaRecurso);

            if (recurso != null) {
                return Paths.get(recurso.toURI()).toString();
            }
        } catch (Exception e) {
            // Si falla la conversión del recurso, se intenta la ruta absoluta.
        }

        Path respaldo = Paths.get(rutaAbsolutaRespaldo);

        if (Files.exists(respaldo)) {
            return respaldo.toString();
        }

        throw new RuntimeException(
                "No se encontró la textura.\n"
                        + "Busqué primero en resources: " + rutaRecurso + "\n"
                        + "Luego intenté ruta absoluta: " + rutaAbsolutaRespaldo
        );
    }
}