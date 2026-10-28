package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private static final float LARGURA_MUNDO = 8.0f;
    private static final float ALTURA_MUNDO = 4.5f;   // 16:9, igual ao cenário
    private static final float LARGURA_BARRA = 0.5f;
    private static final float ALTURA_BARRA = 3.5f;

    // ---- Fada ----
    private static final float ALTURA_FADA = 1.3f;       // altura da fada no mundo
    private static final float CHAO_Y = 0.9f;            // onde ficam os pés da fada
    private static final float VELOCIDADE_FADA = 2.0f;   // unidades do mundo por segundo

    private final ForceInput entradaForca;
    private FitViewport viewport;
    private ShapeRenderer desenhador;
    private SpriteBatch lote;

    private Texture texCenario;
    private Texture texFadaParada;
    private Texture texFadaAndando;
    private Texture texFadaVoando1;
    private Texture texFadaVoando2;

    private float fadaX = LARGURA_MUNDO / 2f;  // centro dos pés da fada
    private float fadaY = CHAO_Y;
    private boolean andando = false;
    private boolean subindo = false;
    private boolean descendo = false;
    private boolean viradaParaDireita = true;
    private float tempoAndando = 0f;

    public Main(ForceInput entradaForca) {
        this.entradaForca = entradaForca;
    }

    @Override
    public void create() {
        viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO);
        desenhador = new ShapeRenderer();
        lote = new SpriteBatch();

        texCenario = carregar("CenárioPrincipal.jpg");
        texFadaParada = carregar("Fada Estatica.png");
        texFadaAndando = carregar("Fada Andando.png");
        texFadaVoando1 = carregar("Fada Voandinho1.png");
        texFadaVoando2 = carregar("Fada Voandinho2.png");
    }

    private Texture carregar(String arquivo) {
        Texture t = new Texture(Gdx.files.internal(arquivo));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }

    private void atualizar(float delta) {
        boolean direita = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean esquerda = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean teclaCima = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean teclaBaixo = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        andando = direita != esquerda; // só anda se apertar UMA das setas
        subindo = teclaCima != teclaBaixo && teclaCima;
        descendo = teclaCima != teclaBaixo && teclaBaixo;

        if (andando) {
            viradaParaDireita = direita;
            float direcao = direita ? 1f : -1f;
            fadaX += direcao * VELOCIDADE_FADA * delta;
            tempoAndando += delta;
        } else {
            tempoAndando = 0f;
        }

        if (subindo) {
            fadaY += VELOCIDADE_FADA * delta;
        } else if (descendo) {
            fadaY -= VELOCIDADE_FADA * delta;
        }

        float alturaMaximaFada = Math.max(alturaDe(texFadaVoando1), alturaDe(texFadaVoando2));
        fadaY = MathUtils.clamp(fadaY, 0f, ALTURA_MUNDO - alturaMaximaFada);

        float meiaLargura = larguraDe(texFadaAndando) / 2f;
        fadaX = MathUtils.clamp(fadaX, meiaLargura, LARGURA_MUNDO - meiaLargura);
    }

    // As duas imagens da fada usam a MESMA escala, para ela não mudar de tamanho ao trocar.
    private float escala() {
        return ALTURA_FADA / texFadaParada.getHeight();
    }

    private float larguraDe(Texture t) {
        return t.getWidth() * escala();
    }

    private float alturaDe(Texture t) {
        return t.getHeight() * escala();
    }

    private void desenharFada() {
        Texture tex;
        if (subindo) {
            tex = texFadaVoando1;
        } else if (descendo) {
            tex = texFadaVoando2;
        } else if (andando) {
            tex = texFadaAndando;
        } else {
            tex = texFadaParada;
        }
        float w = larguraDe(tex);
        float h = alturaDe(tex);

        // Pulinho a cada passo (só enquanto anda)
        float pulo = andando ? Math.abs(MathUtils.sin(tempoAndando * 10f)) * 0.06f : 0f;

        // A imagem "Andando" original olha para a direita; flip quando vai para a esquerda.
        boolean flip = andando && !viradaParaDireita;

        lote.draw(tex,
                fadaX - w / 2f, fadaY + pulo, w, h,
                0, 0, tex.getWidth(), tex.getHeight(),
                flip, false);
    }

    @Override
    public void render() {
        atualizar(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        lote.setProjectionMatrix(viewport.getCamera().combined);
        desenhador.setProjectionMatrix(viewport.getCamera().combined);

        lote.begin();
        lote.draw(texCenario, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        desenharFada();
        lote.end();

        // Barra de força (teste do sensor), por cima do cenário
        desenhador.begin(ShapeRenderer.ShapeType.Filled);
        desenhador.setColor(0.25f, 0.25f, 0.3f, 1.0f);
        desenhador.rect(0.3f, 0.5f, LARGURA_BARRA, ALTURA_BARRA);
        desenhador.setColor(0.4f, 0.85f, 0.55f, 1.0f);
        desenhador.rect(0.3f, 0.5f, LARGURA_BARRA,
                ALTURA_BARRA * entradaForca.getForce());
        desenhador.end();
    }

    @Override
    public void dispose() {
        desenhador.dispose();
        lote.dispose();
        texCenario.dispose();
        texFadaParada.dispose();
        texFadaAndando.dispose();
        texFadaVoando1.dispose();
        texFadaVoando2.dispose();
    }
}