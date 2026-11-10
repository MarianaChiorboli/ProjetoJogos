package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.Mundo;
import br.mackenzie.recursos.EstadoDeAudio;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.recursos.Recursos;
import br.mackenzie.ui.AreaClicavel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Base de todas as telas do jogo (herda de {@link ScreenAdapter} do libGDX).
 * Reúne o que toda tela usa: acesso ao jogo ({@link Main}), posição do mouse, cursor,
 * desenho de fundo e contorno dos botões no modo debug.
 * Por padrão, uma tela não pede nenhum som; cada tela sobrescreve o que precisar.
 */
public abstract class TelaBase extends ScreenAdapter implements EstadoDeAudio {
    protected final Main jogo;
    protected final SpriteBatch lote;
    protected final ShapeRenderer desenhador;
    protected final Recursos recursos;
    /** Posição do mouse em coordenadas do mundo (atualizada por {@link #lerMouse()}). */
    protected final Vector2 mouse = new Vector2();

    protected TelaBase(Main jogo) {
        this.jogo = jogo;
        this.lote = jogo.getLote();
        this.desenhador = jogo.getDesenhador();
        this.recursos = jogo.getRecursos();
    }

    @Override
    public void show() {
        jogo.atualizarCursor(Cursor.SystemCursor.Arrow);
    }

    /** Lê a posição do mouse e converte para coordenadas do mundo (considerando as faixas pretas). */
    protected void lerMouse() {
        jogo.getViewport().unproject(mouse.set(Gdx.input.getX(), Gdx.input.getY()));
    }

    protected boolean clicou() {
        return Gdx.input.justTouched();
    }

    protected boolean mouseSobre(AreaClicavel area) {
        return area.contem(mouse);
    }

    /** Mãozinha quando o mouse está sobre algo clicável. */
    protected void mostrarMaozinha(boolean sobreBotao) {
        jogo.atualizarCursor(sobreBotao ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);
    }

    /** Desenha uma imagem ocupando a tela toda. Chamar dentro de lote.begin()/end(). */
    protected void desenharTelaCheia(Imagem imagem) {
        Texture textura = recursos.textura(imagem);
        lote.draw(textura, 0, 0, Mundo.LARGURA, Mundo.ALTURA);
    }

    /** Contorno das áreas de clique, só quando Mundo.DEBUG_HITBOX = true. */
    protected void desenharContornos(AreaClicavel... areas) {
        if (!Mundo.DEBUG_HITBOX) {
            return;
        }
        desenhador.begin(ShapeRenderer.ShapeType.Line);
        desenhador.setColor(1f, 1f, 1f, 1f);
        for (AreaClicavel area : areas) {
            area.desenharContorno(desenhador);
        }
        desenhador.end();
    }

    // ---- Som pedido pela tela (padrão: nenhum) ----
    @Override
    public boolean tocaMusicaMenu() {
        return false;
    }

    @Override
    public boolean tocaMusicaJardim() {
        return false;
    }

    @Override
    public boolean ehTelaDeConclusao() {
        return false;
    }

    @Override
    public boolean lumiEstaRegando() {
        return false;
    }
}
