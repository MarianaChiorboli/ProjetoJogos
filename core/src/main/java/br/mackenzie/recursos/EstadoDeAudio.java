package br.mackenzie.recursos;

/**
 * O que cada tela "pede" de som. Toda tela implementa esta interface (pela TelaBase),
 * e o {@link GerenciadorAudio} decide o que tocar sem precisar conhecer as telas.
 */
public interface EstadoDeAudio {
    /** Música do menu: menu, pausa e resultados. */
    boolean tocaMusicaMenu();

    /** Música do jardim: durante as fases (sem pausa). */
    boolean tocaMusicaJardim();

    /** Telas "Fase concluída!" e "Você restaurou o jardim!": o som de passar de fase toca 1x ao entrar. */
    boolean ehTelaDeConclusao();

    /** A Lumi está regando (toca o som de rega). */
    boolean lumiEstaRegando();
}
