package br.mackenzie.fases;

import br.mackenzie.recursos.Imagem;

/** Fase 2: flores em alturas diferentes -> treina ajuste da força. */
public class ConfiguracaoFase2 extends ConfiguracaoFase {
    private static final float[] ALTURAS = {0.30f, 0.70f, 0.45f, 0.80f};

    @Override
    public int numero() {
        return 2;
    }

    @Override
    protected float[] alturasDasLuzes() {
        return ALTURAS;
    }

    @Override
    public float tempoParaDespertar() {
        return 1.8f;
    }

    @Override
    public float velocidadeDoCenario() {
        return 0.6f;
    }

    @Override
    public float escalaDaLuz() {
        return 1.0f;
    }

    @Override
    public Imagem cenario() {
        return Imagem.CENARIO_FASE2;
    }

    // alterna os dois tipos de flor da fase 2 (rosa e amarela)
    private boolean amarela(int indice) {
        return (indice % 2) == 1;
    }

    @Override
    public Imagem florSeca(int indice) {
        return amarela(indice) ? Imagem.FLOR_FASE2_AMARELA_MORTA : Imagem.FLOR_FASE2_MORTA;
    }

    @Override
    public Imagem florViva(int indice) {
        return amarela(indice) ? Imagem.FLOR_FASE2_AMARELA_VIVA : Imagem.FLOR_FASE2_VIVA;
    }

    @Override
    public float larguraImagemFlor() {
        return 6.9f;
    }

    @Override
    public float ancoraXFlor() {
        return 0.83f;
    }

    @Override
    public float ancoraYFlor() {
        return 0.12f;
    }

    @Override
    public ConfiguracaoFase proxima() {
        return new ConfiguracaoFase3();
    }
}
