package br.mackenzie.recursos;

import com.badlogic.gdx.audio.Music;

/**
 * Decide, a cada quadro, quais sons tocam, a partir do que a tela atual pede ({@link EstadoDeAudio}).
 * Também guarda o botão de mudo do menu.
 */
public class GerenciadorAudio {
    private final Music musicaMenu;
    private final Music musicaJardim;
    private final Music somPassouFase;
    private final Music somRegar;

    private boolean mutado = false;
    private EstadoDeAudio estadoAnterior = null;

    public GerenciadorAudio(Recursos recursos) {
        musicaMenu = recursos.som(Som.MUSICA_MENU);
        musicaJardim = recursos.som(Som.MUSICA_JARDIM);
        somPassouFase = recursos.som(Som.PASSOU_DE_FASE);
        somRegar = recursos.som(Som.REGAR);
    }

    public boolean isMutado() {
        return mutado;
    }

    public void alternarMudo() {
        mutado = !mutado;
    }

    /** Fase nova ou reiniciada: a música do jardim recomeça do início. */
    public void reiniciarMusicaDaFase() {
        musicaJardim.stop();
        somRegar.stop();
    }

    /** Chamado 1x por quadro, no fim do render(). */
    public void atualizar(EstadoDeAudio estado) {
        float volume = mutado ? 0f : 1f;
        musicaMenu.setVolume(volume);
        musicaJardim.setVolume(volume);
        somPassouFase.setVolume(volume);
        somRegar.setVolume(volume);

        boolean tocarMenu = estado.tocaMusicaMenu();
        boolean tocarJardim = estado.tocaMusicaJardim();
        boolean telaConclusao = estado.ehTelaDeConclusao();

        // Menu: toca no menu, na pausa e nos resultados; ao continuar, para.
        if (tocarMenu && !musicaMenu.isPlaying()) {
            musicaMenu.play();
        } else if (!tocarMenu && musicaMenu.isPlaying()) {
            musicaMenu.stop();
        }

        // Jardim: pausa (guarda a posição) e retoma de onde parou ao continuar.
        if (tocarJardim && !musicaJardim.isPlaying()) {
            musicaJardim.play();
        } else if (!tocarJardim && musicaJardim.isPlaying()) {
            musicaJardim.pause();
        }

        // Passou de fase: toca 1x ao entrar nas telas de conclusão.
        if (telaConclusao && estado != estadoAnterior) {
            somPassouFase.stop();
            somPassouFase.play();
        } else if (!telaConclusao && somPassouFase.isPlaying()) {
            somPassouFase.stop();
        }

        // Regar: só enquanto rega; ao soltar para, e ao voltar a regar recomeça do início.
        boolean regando = tocarJardim && estado.lumiEstaRegando();
        if (regando && !somRegar.isPlaying()) {
            somRegar.play();
        } else if (!regando && somRegar.isPlaying()) {
            somRegar.stop();
        }

        estadoAnterior = estado;
    }
}
