package br.mackenzie.metricas;

/**
 * O desempenho em UMA fase (métricas da seção 3.4 do artigo) e as estrelas ganhas.
 * Objeto imutável: depois de criado, não muda.
 */
public class ResultadoFase {
    /** Firmeza mínima para ganhar a 3ª estrela. */
    public static final float FIRMEZA_PARA_ESTRELA = 0.70f;

    private final int fase;
    private final float tempo;      // segundos jogados
    private final float chegada;    // média do tempo até a Lumi chegar na altura de cada flor
    private final float firmeza;    // média da firmeza (0..1)
    private final int voltas;       // quantas vezes alguma flor precisou voltar
    private final int estrelas;

    public ResultadoFase(int fase, float tempo, float chegada, float firmeza, int voltas) {
        this.fase = fase;
        this.tempo = tempo;
        this.chegada = chegada;
        this.firmeza = firmeza;
        this.voltas = voltas;
        this.estrelas = calcularEstrelas();
    }

    // 1 por terminar + 1 se nenhuma flor voltou + 1 se a força ficou firme
    private int calcularEstrelas() {
        int total = 1;
        if (voltas == 0) total++;
        if (firmeza >= FIRMEZA_PARA_ESTRELA) total++;
        return total;
    }

    public int getFase() {
        return fase;
    }

    public float getTempo() {
        return tempo;
    }

    public float getChegada() {
        return chegada;
    }

    public float getFirmeza() {
        return firmeza;
    }

    public int getVoltas() {
        return voltas;
    }

    public int getEstrelas() {
        return estrelas;
    }

    @Override
    public String toString() {
        return "Fase " + fase + ": tempo " + Formatador.tempo(tempo)
                + ", chegada " + Formatador.segundos(chegada)
                + ", firmeza " + Formatador.porcentagem(firmeza)
                + ", voltas " + voltas + ", estrelas " + estrelas;
    }
}
