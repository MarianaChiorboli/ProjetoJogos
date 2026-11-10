package br.mackenzie.fases;

import br.mackenzie.recursos.Imagem;

/** Fase 3: alturas diferentes, cenário mais rápido e luz menor -> treina precisão. */
public class ConfiguracaoFase3 extends ConfiguracaoFase {
    private static final float[] ALTURAS = {0.60f, 0.25f, 0.80f, 0.40f, 0.70f};
    // imagens 4..7 (se houver mais flores que imagens, elas se repetem)
    private static final Imagem[] SECAS = {
            Imagem.FLOR_FASE3_4_MORTA, Imagem.FLOR_FASE3_5_MORTA, Imagem.FLOR_FASE3_6_MORTA, Imagem.FLOR_FASE3_7_MORTA,
    };
    private static final Imagem[] VIVAS = {
            Imagem.FLOR_FASE3_4_VIVA, Imagem.FLOR_FASE3_5_VIVA, Imagem.FLOR_FASE3_6_VIVA, Imagem.FLOR_FASE3_7_VIVA,
    };

    @Override
    public int numero() {
        return 3;
    }

    @Override
    protected float[] alturasDasLuzes() {
        return ALTURAS;
    }

    @Override
    public float tempoParaDespertar() {
        return 2.0f;
    }

    @Override
    public float velocidadeDoCenario() {
        return 0.75f;
    }

    @Override
    public float escalaDaLuz() {
        return 0.7f;   // luz 30% menor
    }

    @Override
    public Imagem cenario() {
        return Imagem.CENARIO_PRINCIPAL;
    }

    @Override
    public Imagem florSeca(int indice) {
        return SECAS[indice % SECAS.length];
    }

    @Override
    public Imagem florViva(int indice) {
        return VIVAS[indice % VIVAS.length];
    }

    @Override
    public float larguraImagemFlor() {
        return 4.8f;
    }

    @Override
    public float ancoraXFlor() {
        return 0.825f;
    }

    @Override
    public float ancoraYFlor() {
        return 0.08f;
    }

    @Override
    public ConfiguracaoFase proxima() {
        return null;   // última fase
    }
}
