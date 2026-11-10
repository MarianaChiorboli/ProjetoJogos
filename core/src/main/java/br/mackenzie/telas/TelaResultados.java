package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.Mundo;
import br.mackenzie.metricas.Formatador;
import br.mackenzie.metricas.ResultadoFase;
import br.mackenzie.metricas.ResultadosDoJogo;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.ui.AreaClicavel;
import br.mackenzie.ui.BotaoRetangular;
import br.mackenzie.ui.Hud;
import br.mackenzie.ui.TextoNumerico;
import com.badlogic.gdx.graphics.Texture;

/** Tela de resultados: compara as 3 fases (métricas da seção 3.4 do artigo). */
public class TelaResultados extends TelaComBotoes {
    // ---- Layout (coordenadas do mundo, medidas na arte) ----
    private static final float COLUNA_X = 1.40f;        // borda esquerda da coluna da fase 1
    private static final float COLUNA_PASSO = 1.775f;   // distância entre colunas
    private static final float COLUNA_W = 1.65f;
    private static final float ESTRELAS_Y = 2.725f;     // centro das estrelas
    private static final float ESTRELA_TAM = 0.33f;
    private static final float ESTRELA_PASSO = 0.35f;
    private static final float LINHA1_Y = 2.35f;        // centro da 1ª linha (Tempo)
    private static final float LINHA_PASSO = 0.31f;
    private static final float VALOR_MARGEM = 0.14f;    // distância do valor até a borda direita
    private static final float ALTURA_NUMERO = 0.24f;
    private static final float FRASE_Y = 0.89f;
    private static final float FRASE_W = 5.0f;
    private static final float FRASE_H = 0.30f;

    private final AreaClicavel menu = new BotaoRetangular(2.40f, 0.37f, 1.50f, 0.36f);
    private final AreaClicavel jogarDeNovo = new BotaoRetangular(4.10f, 0.37f, 1.50f, 0.36f);
    private final AreaClicavel[] botoes = {menu, jogarDeNovo};

    private final ResultadosDoJogo resultados;
    private final TextoNumerico texto;

    public TelaResultados(Main jogo) {
        super(jogo, Imagem.RESULTADOS_FUNDO);
        this.resultados = jogo.getResultados();
        this.texto = jogo.getTextoNumerico();
    }

    @Override
    protected AreaClicavel[] botoes() {
        return botoes;
    }

    @Override
    protected void aoClicar(AreaClicavel botao) {
        if (botao == menu) {
            jogo.irParaMenu();
        } else if (botao == jogarDeNovo) {
            jogo.iniciarJogo();
        }
    }

    @Override
    protected void desenharConteudo() {
        for (int numero = 1; numero <= ResultadosDoJogo.QUANTIDADE_DE_FASES; numero++) {
            ResultadoFase resultado = resultados.daFase(numero);
            if (resultado != null) {
                desenharColuna(resultado, COLUNA_X + (numero - 1) * COLUNA_PASSO);
            }
        }
        Texture frase = recursos.textura(resultados.escolherFrase());
        lote.draw(frase, (Mundo.LARGURA - FRASE_W) / 2f, FRASE_Y - FRASE_H / 2f, FRASE_W, FRASE_H);
    }

    private void desenharColuna(ResultadoFase resultado, float colunaX) {
        float centroX = colunaX + COLUNA_W / 2f;
        // estrelas
        for (int k = 0; k < 3; k++) {
            Texture estrela = recursos.textura(k < resultado.getEstrelas() ? Imagem.ESTRELA_CHEIA : Imagem.ESTRELA_VAZIA);
            float ex = centroX + (k - 1) * ESTRELA_PASSO;
            lote.draw(estrela, ex - ESTRELA_TAM / 2f, ESTRELAS_Y - ESTRELA_TAM / 2f, ESTRELA_TAM, ESTRELA_TAM);
        }
        // valores, alinhados à direita: tempo, chegada, firmeza, voltas
        String[] valores = {
                Formatador.tempo(resultado.getTempo()),
                Formatador.segundos(resultado.getChegada()),
                Formatador.porcentagem(resultado.getFirmeza()),
                String.valueOf(resultado.getVoltas()),
        };
        float direita = colunaX + COLUNA_W - VALOR_MARGEM;
        for (int j = 0; j < valores.length; j++) {
            float y = LINHA1_Y - j * LINHA_PASSO;
            texto.desenharAlinhadoADireita(lote, valores[j], direita, y, ALTURA_NUMERO, Hud.ESPACAMENTO_NUMERO_TEMPO);
        }
    }

    @Override
    public boolean tocaMusicaMenu() {
        return true;
    }
}
