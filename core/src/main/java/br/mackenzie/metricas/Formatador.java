package br.mackenzie.metricas;

/** Formata números para mostrar na tela. */
public final class Formatador {

    private Formatador() {
        // só métodos estáticos
    }

    /** 75 s -> "1:15" */
    public static String tempo(float segundosTotais) {
        int segundos = (int) segundosTotais;
        return (segundos / 60) + ":" + (segundos % 60 < 10 ? "0" : "") + (segundos % 60);
    }

    /** 2.14 s -> "2,1s" */
    public static String segundos(float segundos) {
        int decimos = Math.round(segundos * 10f);
        return (decimos / 10) + "," + (decimos % 10) + "s";
    }

    /** 0.82 -> "82%" */
    public static String porcentagem(float valor) {
        return Math.round(valor * 100) + "%";
    }
}
