package br.mackenzie.fases;

import br.mackenzie.recursos.Imagem;

/**
 * O que muda de uma fase para outra (Tabela 2 do artigo).
 * Classe abstrata: cada fase (1, 2 e 3) é uma subclasse que define os seus valores.
 * A lógica da fase ({@link Fase}) é a mesma para todas; só a configuração muda.
 */
public abstract class ConfiguracaoFase {

    /** Número da fase (1, 2 ou 3). */
    public abstract int numero();

    /** Altura de cada luz em "força" (0 = chão, 1 = força máxima). Uma luz por flor. */
    protected abstract float[] alturasDasLuzes();

    /** Segundos que a Lumi precisa ficar tocando a luz para a flor despertar. */
    public abstract float tempoParaDespertar();

    /** Velocidade do cenário (unidades do mundo por segundo). */
    public abstract float velocidadeDoCenario();

    /** Tamanho da luz em relação ao normal (a fase 3 tem luz menor = faixa-alvo menor). */
    public abstract float escalaDaLuz();

    /** Imagem de fundo da fase. */
    public abstract Imagem cenario();

    /** Imagem da flor seca/viva de número {@code indice}. */
    public abstract Imagem florSeca(int indice);

    public abstract Imagem florViva(int indice);

    /** Como desenhar as imagens de flor (elas ocupam a tela toda; a âncora é a base da flor). */
    public abstract float larguraImagemFlor();

    public abstract float ancoraXFlor();

    public abstract float ancoraYFlor();

    /** A fase seguinte, ou null se esta for a última. */
    public abstract ConfiguracaoFase proxima();

    public final int quantidadeDeFlores() {
        return alturasDasLuzes().length;
    }

    public final float alturaDaLuz(int indice) {
        return alturasDasLuzes()[indice];
    }

    public final boolean ehUltima() {
        return proxima() == null;
    }
}
