package br.mackenzie.recursos;

/**
 * Todas as imagens do jogo. Cada constante sabe o nome do seu arquivo em assets/.
 * Para usar: {@code recursos.textura(Imagem.MENU)}.
 */
public enum Imagem {
    // Telas
    MENU("Menu.jpg"),
    NIVEL_CONCLUIDO("Nível concluído.png"),
    JARDIM_RESTAURADO("Jardim Encantado Restaurado.png"),
    PAUSA("MenuImplementado.png"),
    BOTAO_PAUSAR("Pause.png"),
    SOM_LIGADO("SomLigado.png"),
    SOM_DESLIGADO("SomDesligado.png"),

    // Cenários
    CENARIO_PRINCIPAL("CenárioPrincipal.jpg"),
    CENARIO_FASE2("Fase2-jardim em recuperação.jpg"),

    // Flores
    FLOR_FASE1_MORTA("FlorMortaLvl1.png"),
    FLOR_FASE1_VIVA("FlorRegadaLvl1.png"),
    FLOR_FASE2_MORTA("FlorMortaLvl2.png"),
    FLOR_FASE2_VIVA("FlorVivaLvl2.png"),
    FLOR_FASE2_AMARELA_MORTA("FlorMortaYLvl2.png"),
    FLOR_FASE2_AMARELA_VIVA("FlorVivaYLvl2.png"),
    FLOR_FASE3_4_MORTA("4-morta.png"),
    FLOR_FASE3_4_VIVA("4-viva.png"),
    FLOR_FASE3_5_MORTA("5-morta.png"),
    FLOR_FASE3_5_VIVA("5-viva.png"),
    FLOR_FASE3_6_MORTA("6-morta.png"),
    FLOR_FASE3_6_VIVA("6-viva.png"),
    FLOR_FASE3_7_MORTA("7-morta.png"),
    FLOR_FASE3_7_VIVA("7-viva.png"),
    LUZ_MAGICA("LuzMagica.png"),

    // Lumi (a fada)
    FADA_PARADA("Fada Estatica.png"),
    FADA_REGANDO("Fada Regando.png"),
    FADA_VOANDO_SUBINDO("Fada Voandinho1.png"),
    FADA_VOANDO_DESCENDO("Fada Voandinho2.png"),

    // HUD (placar, tempo e números)
    PAINEL_CONTADOR("PainelContador.png"),
    PAINEL_TEMPO("PainelTempo.png"),
    NUMEROS("NumerosContador.png"),

    // Tela de resultados
    RESULTADOS_FUNDO("resultados/ResultadosFundo.jpg"),
    ESTRELA_CHEIA("resultados/EstrelaCheia.png"),
    ESTRELA_VAZIA("resultados/EstrelaVazia.png"),
    FRASE_FIRMEZA_FASE2("resultados/FraseFirmeza2.png"),
    FRASE_FIRMEZA_FASE3("resultados/FraseFirmeza3.png"),
    FRASE_CHEGADA_FASE2("resultados/FraseChegada2.png"),
    FRASE_CHEGADA_FASE3("resultados/FraseChegada3.png"),
    FRASE_PRIMEIRA("resultados/FrasePrimeira.png"),
    FRASE_GERAL("resultados/FraseGeral.png");

    private final String arquivo;

    Imagem(String arquivo) {
        this.arquivo = arquivo;
    }

    public String arquivo() {
        return arquivo;
    }
}
