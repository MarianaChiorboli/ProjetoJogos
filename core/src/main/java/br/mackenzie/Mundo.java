package br.mackenzie;

/**
 * Medidas do "mundo" do jogo, compartilhadas por todas as classes.
 * O mundo tem 8 x 4,5 unidades (16:9), com a origem no canto inferior esquerdo.
 */
public final class Mundo {
    public static final float LARGURA = 8.0f;
    public static final float ALTURA = 4.5f;

    /** Mude para true para ver as áreas de clique e de alcance desenhadas na tela. */
    public static final boolean DEBUG_HITBOX = false;

    private Mundo() {
        // classe só de constantes: não deve ser instanciada
    }
}
