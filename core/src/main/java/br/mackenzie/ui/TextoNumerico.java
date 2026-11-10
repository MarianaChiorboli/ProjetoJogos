package br.mackenzie.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Escreve textos usando os números desenhados da imagem NumerosContador.png
 * (15 células do mesmo tamanho: 0 1 2 3 4 5 6 7 8 9 / : , % s).
 */
public class TextoNumerico {
    private static final String CARACTERES = "0123456789/:,%s";

    private final TextureRegion[] regioes = new TextureRegion[CARACTERES.length()];

    public TextoNumerico(Texture numeros) {
        int larguraCelula = numeros.getWidth() / regioes.length;
        for (int i = 0; i < regioes.length; i++) {
            regioes[i] = new TextureRegion(numeros, i * larguraCelula, 0, larguraCelula, numeros.getHeight());
        }
    }

    private float larguraCaractere(float altura) {
        TextureRegion modelo = regioes[0];
        return altura * modelo.getRegionWidth() / modelo.getRegionHeight();
    }

    /** Largura total do texto no mundo. */
    public float largura(String texto, float altura, float espacamento) {
        float larguraNumero = larguraCaractere(altura);
        return larguraNumero * espacamento * (texto.length() - 1) + larguraNumero;
    }

    /** Desenha o texto centralizado em (centroX, centroY). Chamar dentro de lote.begin()/end(). */
    public void desenharCentralizado(SpriteBatch lote, String texto, float centroX, float centroY,
                                     float altura, float espacamento) {
        float larguraNumero = larguraCaractere(altura);
        float avanco = larguraNumero * espacamento;
        float larguraTexto = avanco * (texto.length() - 1) + larguraNumero;
        float x = centroX - larguraTexto / 2f;
        float y = centroY - altura / 2f;
        for (int i = 0; i < texto.length(); i++) {
            int indice = CARACTERES.indexOf(texto.charAt(i));
            if (indice >= 0) {
                lote.draw(regioes[indice], x + i * avanco, y, larguraNumero, altura);
            }
        }
    }

    /** Desenha o texto com a borda direita em {@code direita}. */
    public void desenharAlinhadoADireita(SpriteBatch lote, String texto, float direita, float centroY,
                                         float altura, float espacamento) {
        float larguraTexto = largura(texto, altura, espacamento);
        desenharCentralizado(lote, texto, direita - larguraTexto / 2f, centroY, altura, espacamento);
    }
}
