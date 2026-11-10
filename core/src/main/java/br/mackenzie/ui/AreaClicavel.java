package br.mackenzie.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Uma área da tela que pode ser clicada (um botão).
 * Classe abstrata: cada formato (retângulo, círculo) diz como testar o clique e como se desenhar no modo debug.
 */
public abstract class AreaClicavel {

    /** O ponto (em coordenadas do mundo) está dentro da área? */
    public abstract boolean contem(Vector2 ponto);

    /** Desenha o contorno da área (usado com Mundo.DEBUG_HITBOX). */
    public abstract void desenharContorno(ShapeRenderer desenhador);
}
