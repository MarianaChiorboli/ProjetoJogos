package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private static final float LARGURA_MUNDO = 8.0f;
    private static final float ALTURA_MUNDO = 5.0f;
    private static final float LARGURA_BARRA = 0.5f;
    private static final float ALTURA_BARRA = 4.0f;

    private final ForceInput entradaForca;
    private FitViewport viewport;
    private ShapeRenderer desenhador;

    public Main(ForceInput entradaForca) {
        this.entradaForca = entradaForca;
    }

    @Override
    public void create() {
        viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO);
        desenhador = new ShapeRenderer();
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        desenhador.setProjectionMatrix(viewport.getCamera().combined);
        desenhador.begin(ShapeRenderer.ShapeType.Filled);
        desenhador.setColor(0.25f, 0.25f, 0.3f, 1.0f);
        desenhador.rect(0.5f, 0.5f, LARGURA_BARRA, ALTURA_BARRA);
        desenhador.setColor(0.4f, 0.85f, 0.55f, 1.0f);
        desenhador.rect(0.5f, 0.5f, LARGURA_BARRA,
                ALTURA_BARRA * entradaForca.getForce());
        desenhador.end();
    }

    @Override
    public void dispose() {
        desenhador.dispose();
    }
}
