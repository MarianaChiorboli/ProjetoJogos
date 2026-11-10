package br.mackenzie.metricas;

import br.mackenzie.recursos.Imagem;

/**
 * Os resultados das 3 fases de uma partida, usados na tela de resultados.
 */
public class ResultadosDoJogo {
    public static final int QUANTIDADE_DE_FASES = 3;
    // Frases de incentivo: melhora mínima (em relação à fase 1) para a frase aparecer
    private static final float MELHORA_MINIMA_FIRMEZA = 0.05f;   // 5 pontos percentuais
    private static final float MELHORA_MINIMA_CHEGADA = 0.3f;    // segundos

    private final ResultadoFase[] fases = new ResultadoFase[QUANTIDADE_DE_FASES];

    /** Jogo novo: apaga os resultados anteriores. */
    public void limpar() {
        for (int i = 0; i < fases.length; i++) {
            fases[i] = null;
        }
    }

    /** Guarda (ou substitui, se a fase foi jogada de novo) o resultado de uma fase. */
    public void registrar(ResultadoFase resultado) {
        fases[resultado.getFase() - 1] = resultado;
    }

    /** Resultado da fase (1, 2 ou 3), ou null se ainda não foi jogada. */
    public ResultadoFase daFase(int numero) {
        return fases[numero - 1];
    }

    private boolean todasJogadas() {
        for (ResultadoFase resultado : fases) {
            if (resultado == null) {
                return false;
            }
        }
        return true;
    }

    /** Frase de incentivo escolhida a partir da evolução em relação à fase 1. */
    public Imagem escolherFrase() {
        if (!todasJogadas()) {
            return Imagem.FRASE_GERAL;
        }
        ResultadoFase f1 = fases[0];
        ResultadoFase f2 = fases[1];
        ResultadoFase f3 = fases[2];
        if (f1.getVoltas() + f2.getVoltas() + f3.getVoltas() == 0) {
            return Imagem.FRASE_PRIMEIRA;
        }
        float melhoraFirmeza2 = f2.getFirmeza() - f1.getFirmeza();
        float melhoraFirmeza3 = f3.getFirmeza() - f1.getFirmeza();
        if (Math.max(melhoraFirmeza2, melhoraFirmeza3) >= MELHORA_MINIMA_FIRMEZA) {
            return melhoraFirmeza3 >= melhoraFirmeza2 ? Imagem.FRASE_FIRMEZA_FASE3 : Imagem.FRASE_FIRMEZA_FASE2;
        }
        float melhoraChegada2 = f1.getChegada() - f2.getChegada();
        float melhoraChegada3 = f1.getChegada() - f3.getChegada();
        if (Math.max(melhoraChegada2, melhoraChegada3) >= MELHORA_MINIMA_CHEGADA) {
            return melhoraChegada3 >= melhoraChegada2 ? Imagem.FRASE_CHEGADA_FASE3 : Imagem.FRASE_CHEGADA_FASE2;
        }
        return Imagem.FRASE_GERAL;
    }
}
