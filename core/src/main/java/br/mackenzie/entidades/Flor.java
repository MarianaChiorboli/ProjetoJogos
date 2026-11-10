package br.mackenzie.entidades;

import com.badlogic.gdx.math.MathUtils;

/**
 * Uma flor seca da fase, com a sua luz mágica na altura que ela pede.
 * Também guarda as métricas da passada em que ela despertou (chegada e firmeza).
 */
public class Flor {
    public static final float DURACAO_TRANSFORMACAO = 0.4f;
    // Firmeza = 1 - (desvio padrão da força enquanto rega / SIGMA_REFERENCIA_FIRMEZA), entre 0 e 1.
    // Desvio igual ou maior que a referência = firmeza 0%.
    private static final float SIGMA_REFERENCIA_FIRMEZA = 0.10f;
    private static final float DECAIMENTO_PROGRESSO = 0.5f;   // ao sair da luz, o progresso cai devagar

    private final int indice;            // posição na fase (0 = primeira)
    private final float alturaDaLuz;     // em "força" (0..1)
    private float x;                     // posição no cenário
    private float progresso = 0f;        // 0..1 enquanto a Lumi rega
    private int tentativas = 1;          // quantas vezes precisou passar
    private boolean recuperada = false;
    private float animacao = DURACAO_TRANSFORMACAO;

    // ---- métricas da passada atual ----
    private float inicioPassada = -1f;   // tempo da fase quando a luz entrou na tela (-1 = ainda não)
    private float chegadaPassada = -1f;  // segundos até a Lumi chegar na altura (-1 = ainda não)
    private double somaForca = 0;
    private double somaForcaQuadrado = 0;
    private int amostrasForca = 0;
    // ---- métricas finais (da passada em que despertou) ----
    private float chegada;
    private float firmeza;

    public Flor(int indice, float x, float alturaDaLuz) {
        this.indice = indice;
        this.x = x;
        this.alturaDaLuz = alturaDaLuz;
    }

    // ------------------------------------------------------------------
    // Rega
    // ------------------------------------------------------------------
    /** Lumi está tocando a luz: guarda a força e avança o progresso. Retorna true se despertou agora. */
    public boolean regar(float forca, float delta, float tempoParaDespertar) {
        somaForca += forca;
        somaForcaQuadrado += (double) forca * forca;
        amostrasForca++;
        progresso += delta / tempoParaDespertar;
        if (progresso >= 1f) {
            progresso = 1f;
            despertar();
            return true;
        }
        return false;
    }

    /** Lumi saiu da luz: o progresso cai devagar. */
    public void decairProgresso(float delta, float tempoParaDespertar) {
        if (progresso > 0f) {
            progresso = Math.max(0f, progresso - delta * DECAIMENTO_PROGRESSO / tempoParaDespertar);
        }
    }

    private void despertar() {
        recuperada = true;
        animacao = 0f;
        chegada = Math.max(0f, chegadaPassada);
        firmeza = calcularFirmeza();
    }

    /** Firmeza: 1 = força totalmente estável enquanto regava. */
    private float calcularFirmeza() {
        int n = amostrasForca;
        if (n < 2) {
            return 1f;
        }
        double media = somaForca / n;
        double variancia = Math.max(0.0, somaForcaQuadrado / n - media * media);
        float desvio = (float) Math.sqrt(variancia);
        return MathUtils.clamp(1f - desvio / SIGMA_REFERENCIA_FIRMEZA, 0f, 1f);
    }

    /** A flor passou sem despertar: volta para a posição {@code novoX} e as métricas recomeçam. */
    public void voltarMaisTarde(float novoX) {
        x = novoX;
        progresso = 0f;
        tentativas++;
        inicioPassada = -1f;
        chegadaPassada = -1f;
        somaForca = 0;
        somaForcaQuadrado = 0;
        amostrasForca = 0;
    }

    public void atualizarAnimacao(float delta) {
        if (animacao < DURACAO_TRANSFORMACAO) {
            animacao = Math.min(DURACAO_TRANSFORMACAO, animacao + delta);
        }
    }

    public boolean animacaoTerminou() {
        return animacao >= DURACAO_TRANSFORMACAO;
    }

    /** Opacidade da flor viva durante a transformação (0..1). */
    public float opacidadeViva() {
        return Math.min(1f, animacao / DURACAO_TRANSFORMACAO);
    }

    // ------------------------------------------------------------------
    // Chegada (velocidade)
    // ------------------------------------------------------------------
    public boolean relogioIniciado() {
        return inicioPassada >= 0f;
    }

    public void iniciarRelogio(float tempoFase) {
        inicioPassada = tempoFase;
    }

    public boolean chegadaRegistrada() {
        return chegadaPassada >= 0f;
    }

    public void registrarChegada(float tempoFase) {
        chegadaPassada = tempoFase - inicioPassada;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------
    public int getIndice() {
        return indice;
    }

    public float getX() {
        return x;
    }

    public float getAlturaDaLuz() {
        return alturaDaLuz;
    }

    public float getProgresso() {
        return progresso;
    }

    public int getTentativas() {
        return tentativas;
    }

    public boolean isRecuperada() {
        return recuperada;
    }

    public float getChegada() {
        return chegada;
    }

    public float getFirmeza() {
        return firmeza;
    }
}
