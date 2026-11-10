package br.mackenzie.ui;

import br.mackenzie.Mundo;
import br.mackenzie.metricas.Formatador;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.recursos.Recursos;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Informações desenhadas por cima das fases: placar de flores, tempo da fase e barra de força.
 * Tudo em coordenadas de tela (não rola com o cenário).
 */
public class Hud {
    // ---- Placar de flores "regadas / total" (topo central) ----
    private static final float PAINEL_CONTADOR_W = 1.5625f;
    private static final float PAINEL_CONTADOR_H = 0.6375f;
    private static final float PAINEL_CONTADOR_X = (Mundo.LARGURA - PAINEL_CONTADOR_W) / 2f;
    private static final float PAINEL_CONTADOR_Y = 3.84f;
    private static final float TEXTO_CONTADOR_CENTRO_X = 1.0f;  // centro do texto, medido da borda esquerda do painel
    private static final float ALTURA_NUMERO = 0.3625f;
    private static final float ESPACAMENTO_NUMERO = 0.9f;       // 1 = células encostadas; menor = mais juntas

    // ---- Tempo da fase (canto superior esquerdo) ----
    private static final float PAINEL_TEMPO_X = 0.0625f;
    private static final float PAINEL_TEMPO_Y = 3.9f;
    private static final float PAINEL_TEMPO_W = 1.3125f;
    private static final float PAINEL_TEMPO_H = 0.575f;
    private static final float TEXTO_TEMPO_CENTRO_X = 0.85f;
    private static final float ALTURA_NUMERO_TEMPO = 0.30f;
    public static final float ESPACAMENTO_NUMERO_TEMPO = 0.75f;

    // ---- Barra de força (teste do sensor) ----
    private static final float BARRA_X = 0.3f;
    private static final float BARRA_Y = 0.5f;
    private static final float LARGURA_BARRA = 0.5f;
    private static final float ALTURA_BARRA = 3.3f;   // um pouco mais baixa, para não encostar no tempo

    private final Texture painelContador;
    private final Texture painelTempo;
    private final TextoNumerico texto;

    public Hud(Recursos recursos, TextoNumerico texto) {
        this.painelContador = recursos.textura(Imagem.PAINEL_CONTADOR);
        this.painelTempo = recursos.textura(Imagem.PAINEL_TEMPO);
        this.texto = texto;
    }

    /** Plaquinha com a flor e o placar. Chamar dentro de lote.begin()/end(). */
    public void desenharContador(SpriteBatch lote, int total, int regadas) {
        lote.draw(painelContador, PAINEL_CONTADOR_X, PAINEL_CONTADOR_Y, PAINEL_CONTADOR_W, PAINEL_CONTADOR_H);
        texto.desenharCentralizado(lote, regadas + "/" + total,
                PAINEL_CONTADOR_X + TEXTO_CONTADOR_CENTRO_X,
                PAINEL_CONTADOR_Y + PAINEL_CONTADOR_H / 2f,
                ALTURA_NUMERO, ESPACAMENTO_NUMERO);
    }

    /** Plaquinha com relógio e o tempo da fase (m:ss). Chamar dentro de lote.begin()/end(). */
    public void desenharTempo(SpriteBatch lote, float tempoFase) {
        lote.draw(painelTempo, PAINEL_TEMPO_X, PAINEL_TEMPO_Y, PAINEL_TEMPO_W, PAINEL_TEMPO_H);
        texto.desenharCentralizado(lote, Formatador.tempo(tempoFase),
                PAINEL_TEMPO_X + TEXTO_TEMPO_CENTRO_X,
                PAINEL_TEMPO_Y + PAINEL_TEMPO_H / 2f,
                ALTURA_NUMERO_TEMPO, ESPACAMENTO_NUMERO_TEMPO);
    }

    /** Barra de força à esquerda. Faz o próprio begin()/end() do desenhador. */
    public void desenharBarraForca(ShapeRenderer desenhador, float forca) {
        desenhador.begin(ShapeRenderer.ShapeType.Filled);
        desenhador.setColor(0.25f, 0.25f, 0.3f, 1.0f);
        desenhador.rect(BARRA_X, BARRA_Y, LARGURA_BARRA, ALTURA_BARRA);
        desenhador.setColor(0.4f, 0.85f, 0.55f, 1.0f);
        desenhador.rect(BARRA_X, BARRA_Y, LARGURA_BARRA, ALTURA_BARRA * forca);
        desenhador.end();
    }
}
