package br.mackenzie.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Cenário sem fim: a mesma imagem repetida, alternando normal/espelhada (as emendas ficam invisíveis).
 * Ele anda sozinho da direita para a esquerda; o quanto já andou é o "deslocamento".
 */
public class CenarioRolante {
    /** Largura de UMA imagem de cenário (mesmo zoom nas 3 fases). */
    public static final float LARGURA_IMAGEM = 12f;
    /** A câmera fica parada na vertical; só o cenário anda na horizontal. */
    public static final float CAMERA_Y = 0.75f;

    private float deslocamento = 0f;

    public void reiniciar() {
        deslocamento = 0f;
    }

    public void avancar(float distancia) {
        deslocamento += distancia;
    }

    public float getDeslocamento() {
        return deslocamento;
    }

    /** Chamar dentro de lote.begin()/end(). */
    public void desenhar(SpriteBatch lote, Texture imagem) {
        float altura = LARGURA_IMAGEM * imagem.getHeight() / imagem.getWidth();
        int primeira = MathUtils.floor(deslocamento / LARGURA_IMAGEM);
        for (int k = primeira; k <= primeira + 1; k++) {
            float x = k * LARGURA_IMAGEM - deslocamento;
            boolean espelhar = (k % 2) != 0;
            // +0.01 de sobra na largura para não aparecer fresta entre as repetições
            lote.draw(imagem, x, -CAMERA_Y, LARGURA_IMAGEM + 0.01f, altura,
                    0, 0, imagem.getWidth(), imagem.getHeight(), espelhar, false);
        }
    }
}
