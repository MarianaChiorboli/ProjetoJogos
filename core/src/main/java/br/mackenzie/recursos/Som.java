package br.mackenzie.recursos;

/** Todos os sons do jogo, com o arquivo e se tocam em loop. */
public enum Som {
    MUSICA_MENU("menu.mp3", true),
    MUSICA_JARDIM("jardim.mp3", true),
    PASSOU_DE_FASE("passou de fase.mp3", false),
    REGAR("regar.mp3", true);

    private final String arquivo;
    private final boolean repetir;

    Som(String arquivo, boolean repetir) {
        this.arquivo = arquivo;
        this.repetir = repetir;
    }

    public String arquivo() {
        return arquivo;
    }

    public boolean repetir() {
        return repetir;
    }
}
