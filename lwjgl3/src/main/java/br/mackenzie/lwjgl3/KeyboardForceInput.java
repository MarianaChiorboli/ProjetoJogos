package br.mackenzie.lwjgl3;

import br.mackenzie.ForceInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class KeyboardForceInput implements ForceInput {
    // TODO: calibrar estas velocidades para a resposta desejada no jogo.
    private static final float VELOCIDADE_SUBIDA = 0.5f;
    private static final float VELOCIDADE_DESCIDA = 0.5f;

    private float forca;

    @Override
    public float getForce() {
        float variacao = Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            forca = Math.min(1.0f, forca + VELOCIDADE_SUBIDA * variacao);
        } else {
            forca = Math.max(0.0f, forca - VELOCIDADE_DESCIDA * variacao);
        }
        return forca;
    }
}
