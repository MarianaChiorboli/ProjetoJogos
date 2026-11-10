package br.mackenzie.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/** Botão redondo (o botão de play do menu). */
public class BotaoCircular extends AreaClicavel {
    private final float centroX;
    private final float centroY;
    private final float raio;

    public BotaoCircular(float centroX, float centroY, float raio) {
        this.centroX = centroX;
        this.centroY = centroY;
        this.raio = raio;
    }

    @Override
    public boolean contem(Vector2 ponto) {
        return ponto.dst(centroX, centroY) <= raio;
    }

    @Override
    public void desenharContorno(ShapeRenderer desenhador) {
        desenhador.circle(centroX, centroY, raio);
    }

    public float getCentroX() {
        return centroX;
    }

    public float getCentroY() {
        return centroY;
    }

    public float getRaio() {
        return raio;
    }
}
