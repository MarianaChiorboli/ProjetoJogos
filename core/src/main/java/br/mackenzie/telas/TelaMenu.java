package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.ui.AreaClicavel;
import br.mackenzie.ui.BotaoCircular;
import br.mackenzie.ui.BotaoRetangular;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Menu principal: iniciar o jogo (botão de play ou texto "Iniciar jogo") e ligar/desligar o som. */
public class TelaMenu extends TelaBase {
    private final BotaoCircular play = new BotaoCircular(6.02f, 0.95f, 0.28f);
    private final AreaClicavel textoIniciar = new BotaoRetangular(4.27f, 1.00f, 1.40f, 0.36f);
    private final BotaoRetangular som = new BotaoRetangular(7.06f, 3.68f, 0.56f, 0.48f);

    public TelaMenu(Main jogo) {
        super(jogo);
    }

    @Override
    public void render(float delta) {
        lerMouse();
        boolean sobrePlay = mouseSobre(play) || mouseSobre(textoIniciar);
        boolean sobreSom = mouseSobre(som);
        mostrarMaozinha(sobrePlay || sobreSom);

        if (clicou() && sobreSom) {
            jogo.getAudio().alternarMudo();
        } else if (clicou() && sobrePlay) {
            jogo.iniciarJogo();
            return;
        }

        lote.begin();
        desenharTelaCheia(Imagem.MENU);
        lote.draw(recursos.textura(jogo.getAudio().isMutado() ? Imagem.SOM_DESLIGADO : Imagem.SOM_LIGADO),
                som.getX(), som.getY(), som.getLargura(), som.getAltura());
        lote.end();

        if (sobrePlay) {
            desenharBrilhoNoPlay();
        }
        desenharContornos(play, textoIniciar, som);
    }

    private void desenharBrilhoNoPlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        desenhador.begin(ShapeRenderer.ShapeType.Filled);
        desenhador.setColor(1f, 0.84f, 0f, 0.25f);
        desenhador.circle(play.getCentroX(), play.getCentroY(), play.getRaio());
        desenhador.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public boolean tocaMusicaMenu() {
        return true;
    }
}
