package br.mackenzie.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Botão retangular (a maioria dos botões do jogo). */
public class BotaoRetangular extends AreaClicavel {
    private final Rectangle area;

    public BotaoRetangular(float x, float y, float largura, float altura) {
        this.area = new Rectangle(x, y, largura, altura);
    }

    @Override
    public boolean contem(Vector2 ponto) {
        return area.contains(ponto);
    }

    @Override
    public void desenharContorno(ShapeRenderer desenhador) {
        desenhador.rect(area.x, area.y, area.width, area.height);
    }

    public float getX() {
        return area.x;
    }

    public float getY() {
        return area.y;
    }

    public float getLargura() {
        return area.width;
    }

    public float getAltura() {
        return area.height;
    }
}
