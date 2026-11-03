package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private enum Estado { MENU, FASE1 }

    private static final float LARGURA_MUNDO = 8.0f;
    private static final float ALTURA_MUNDO = 4.5f;   // 16:9, igual ao cenário
    private static final float LARGURA_BARRA = 0.5f;
    private static final float ALTURA_BARRA = 3.5f;
    private static final float PLAY_X = 6.02f;
    private static final float PLAY_Y = 0.95f;
    private static final float PLAY_RAIO = 0.28f;
    private static final boolean DEBUG_HITBOX = false;
    private static final Rectangle BTN_INICIAR = new Rectangle(6.07f, 3.90f, 0.51f, 0.46f);
    private static final Rectangle BTN_PAUSAR = new Rectangle(6.68f, 3.90f, 0.52f, 0.46f);
    private static final Rectangle BTN_REINICIAR = new Rectangle(7.30f, 3.90f, 0.52f, 0.46f);

    // ---- Fada ----
    private static final float ALTURA_FADA = 1.3f;       // altura da fada no mundo
    private static final float CHAO_Y = 0.9f;            // onde ficam os pés da fada
    private static final float VELOCIDADE_FADA = 2.0f;   // unidades do mundo por segundo

    private final ForceInput entradaForca;
    private Estado estado = Estado.MENU;
    private boolean pausado = false;
    private float forcaAtual;
    private Cursor.SystemCursor cursorAtual = Cursor.SystemCursor.Arrow;
    private FitViewport viewport;
    private ShapeRenderer desenhador;
    private SpriteBatch lote;
    private final Vector2 toque = new Vector2();

    private Texture texMenu;
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

        texMenu = carregar("Menu.jpg");
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

    private void atualizarFase1(float delta) {
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

    private void desenharFase1() {
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
                ALTURA_BARRA * forcaAtual);
        desenhador.end();

        if (DEBUG_HITBOX) {
            desenhador.begin(ShapeRenderer.ShapeType.Line);
            desenhador.setColor(1f, 1f, 1f, 1f);
            desenhador.rect(BTN_INICIAR.x, BTN_INICIAR.y, BTN_INICIAR.width, BTN_INICIAR.height);
            desenhador.rect(BTN_PAUSAR.x, BTN_PAUSAR.y, BTN_PAUSAR.width, BTN_PAUSAR.height);
            desenhador.rect(BTN_REINICIAR.x, BTN_REINICIAR.y, BTN_REINICIAR.width, BTN_REINICIAR.height);
            desenhador.end();
        }
    }

    private void iniciarFase1() {
        estado = Estado.FASE1;
        atualizarCursor(Cursor.SystemCursor.Arrow);
        pausado = false;
        forcaAtual = 0f;
        fadaX = LARGURA_MUNDO / 2f;
        fadaY = CHAO_Y;
        andando = false;
        subindo = false;
        descendo = false;
        viradaParaDireita = true;
        tempoAndando = 0f;
    }

    private Rectangle botaoSobMouse() {
        if (BTN_REINICIAR.contains(toque)) {
            return BTN_REINICIAR;
        } else if (pausado && BTN_INICIAR.contains(toque)) {
            return BTN_INICIAR;
        } else if (!pausado && BTN_PAUSAR.contains(toque)) {
            return BTN_PAUSAR;
        }
        return null;
    }

    private void voltarAoMenu() {
        estado = Estado.MENU;
        pausado = false;
        atualizarCursor(Cursor.SystemCursor.Arrow);
    }

    private void atualizarCursor(Cursor.SystemCursor cursor) {
        if (cursorAtual != cursor) {
            Gdx.graphics.setSystemCursor(cursor);
            cursorAtual = cursor;
        }
    }

    private void atualizarEntradaFase1() {
        viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
        Rectangle botao = botaoSobMouse();
        atualizarCursor(botao != null ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

        if (Gdx.input.justTouched() && botao != null) {
            if (botao == BTN_REINICIAR) {
                voltarAoMenu();
                return;
            } else if (botao == BTN_INICIAR) {
                pausado = false;
            } else if (botao == BTN_PAUSAR) {
                pausado = true;
            }
        }
    }

    private void desenharMenu() {
        viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobrePlay = toque.dst(PLAY_X, PLAY_Y) <= PLAY_RAIO;
        atualizarCursor(sobrePlay ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

        if (Gdx.input.justTouched() && sobrePlay) {
            iniciarFase1();
        }

        lote.begin();
        lote.draw(texMenu, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        lote.end();

        if (sobrePlay) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            desenhador.begin(ShapeRenderer.ShapeType.Filled);
            desenhador.setColor(1f, 0.84f, 0f, 0.25f);
            desenhador.circle(PLAY_X, PLAY_Y, PLAY_RAIO);
            desenhador.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (DEBUG_HITBOX) {
            desenhador.begin(ShapeRenderer.ShapeType.Line);
            desenhador.setColor(1f, 1f, 1f, 1f);
            desenhador.circle(PLAY_X, PLAY_Y, PLAY_RAIO);
            desenhador.end();
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        lote.setProjectionMatrix(viewport.getCamera().combined);
        desenhador.setProjectionMatrix(viewport.getCamera().combined);

        switch (estado) {
            case MENU:
                desenharMenu();
                break;
            case FASE1: {
                boolean estavaPausado = pausado;
                atualizarEntradaFase1();
                if (estado == Estado.MENU) {
                    lote.begin();
                    lote.draw(texMenu, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
                    lote.end();
                    break;
                }
                if (!estavaPausado && !pausado) {
                    atualizarFase1(Gdx.graphics.getDeltaTime());
                    forcaAtual = entradaForca.getForce();
                }
                desenharFase1();
                break;
            }
        }
    }

    @Override
    public void dispose() {
        desenhador.dispose();
        lote.dispose();
        texMenu.dispose();
        texCenario.dispose();
        texFadaParada.dispose();
        texFadaAndando.dispose();
        texFadaVoando1.dispose();
        texFadaVoando2.dispose();
    }
}