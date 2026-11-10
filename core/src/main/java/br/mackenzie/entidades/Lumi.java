package br.mackenzie.entidades;

import br.mackenzie.recursos.Imagem;
import br.mackenzie.recursos.Recursos;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * A personagem principal. Fica sempre na mesma posição horizontal; a ALTURA vem da força do sensor.
 */
public class Lumi {
    private static final float ALTURA_IMAGEM = 1.3f;          // altura da imagem da fada no mundo
    public static final float X_NA_TELA = 1.9f;               // Lumi fica sempre nesta posição horizontal
    // Altura do CORPO na tela: força 0 = Y_MIN (perto do chão), força 1 = Y_MAX
    public static final float Y_MIN = 1.2f;
    public static final float Y_MAX = 3.4f;
    private static final float SUAVIZACAO = 8f;               // maior = Lumi segue a força mais rápido
    private static final float LIMIAR_SUBINDO = 0.15f;        // velocidade vertical para trocar o sprite

    /** Os 4 sprites da Lumi, com a posição do corpo dentro de cada imagem (0..1 a partir da esquerda/baixo). */
    private enum Sprite {
        PARADA(Imagem.FADA_PARADA, 0.494f, 0.521f),
        SUBINDO(Imagem.FADA_VOANDO_SUBINDO, 0.298f, 0.493f),
        DESCENDO(Imagem.FADA_VOANDO_DESCENDO, 0.452f, 0.494f),
        REGANDO(Imagem.FADA_REGANDO, 0.423f, 0.452f);

        final Imagem imagem;
        final float centroX;
        final float centroY;

        Sprite(Imagem imagem, float centroX, float centroY) {
            this.imagem = imagem;
            this.centroX = centroX;
            this.centroY = centroY;
        }
    }

    private float y = Y_MIN;           // altura do corpo na tela
    private boolean subindo = false;
    private boolean descendo = false;
    private float tempoVoo = 0f;       // usado para a Lumi "flutuar" de leve

    /** Converte uma força (0..1) na altura do corpo na tela. */
    public static float alturaNaTela(float forca) {
        return Y_MIN + forca * (Y_MAX - Y_MIN);
    }

    public void reiniciar() {
        y = Y_MIN;
        subindo = false;
        descendo = false;
        tempoVoo = 0f;
    }

    public void atualizar(float forca, float delta) {
        float alvo = alturaNaTela(forca);
        float anterior = y;
        y += (alvo - y) * Math.min(1f, SUAVIZACAO * delta);
        float velocidade = delta > 0f ? (y - anterior) / delta : 0f;
        subindo = velocidade > LIMIAR_SUBINDO;
        descendo = velocidade < -LIMIAR_SUBINDO;
        tempoVoo += delta;
    }

    public float getY() {
        return y;
    }

    public float getTempoVoo() {
        return tempoVoo;
    }

    /** Chamar dentro de lote.begin()/end(). */
    public void desenhar(SpriteBatch lote, Recursos recursos, boolean regando) {
        Sprite sprite;
        if (regando) {
            sprite = Sprite.REGANDO;
        } else if (subindo) {
            sprite = Sprite.SUBINDO;
        } else if (descendo) {
            sprite = Sprite.DESCENDO;
        } else {
            sprite = Sprite.PARADA;
        }
        Texture tex = recursos.textura(sprite.imagem);
        // todas as imagens usam a MESMA escala, para a Lumi não mudar de tamanho ao trocar
        float escala = ALTURA_IMAGEM / recursos.textura(Imagem.FADA_PARADA).getHeight();
        float w = tex.getWidth() * escala;
        float h = tex.getHeight() * escala;
        // flutua de leve, para parecer que está voando
        float flutuar = MathUtils.sin(tempoVoo * 3f) * 0.03f;
        // posiciona pelo CORPO (e não pelo canto da imagem), para não "pular" ao trocar de sprite
        float x = X_NA_TELA - sprite.centroX * w;
        float yDesenho = y + flutuar - sprite.centroY * h;
        lote.draw(tex, x, yDesenho, w, h);
    }
}
