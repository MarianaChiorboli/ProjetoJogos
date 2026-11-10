package br.mackenzie.fases;

import br.mackenzie.recursos.Imagem;

/** Fase 1: flores na mesma altura -> treina força constante. */
public class ConfiguracaoFase1 extends ConfiguracaoFase {
    private static final float[] ALTURAS = {0.5f, 0.5f, 0.5f};

    @Override
    public int numero() {
        return 1;
    }

    @Override
    protected float[] alturasDasLuzes() {
        return ALTURAS;
    }

    @Override
    public float tempoParaDespertar() {
        return 1.5f;
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
        return Imagem.CENARIO_PRINCIPAL;
    }

    @Override
    public Imagem florSeca(int indice) {
        return Imagem.FLOR_FASE1_MORTA;
    }

    @Override
    public Imagem florViva(int indice) {
        return Imagem.FLOR_FASE1_VIVA;
    }

    @Override
    public float larguraImagemFlor() {
        return 6.4f;
    }

    @Override
    public float ancoraXFlor() {
        return 0.4837f;
    }

    @Override
    public float ancoraYFlor() {
        return 0.16f;
    }

    @Override
    public ConfiguracaoFase proxima() {
        return new ConfiguracaoFase2();
    }
}
