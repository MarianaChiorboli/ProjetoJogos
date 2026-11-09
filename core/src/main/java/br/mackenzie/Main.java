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
    private enum Estado { MENU, FASE1, FASE2, NIVEL_CONCLUIDO }

    private static final float LARGURA_MUNDO = 8.0f;
    private static final float ALTURA_MUNDO = 4.5f;   // 16:9, igual ao cenário
    // Largura do cenário: IGUAL para fase 1 e fase 2 (mesmo zoom)
    private static final float LARGURA_CENARIO = 12f;
    private static final float SEGUIMENTO_CAMERA = 0.5f;
    private static final float LARGURA_BARRA = 0.5f;
    private static final float ALTURA_BARRA = 3.5f;

    // ---- Fada (compartilhado entre as fases) ----
    private static final float ALTURA_FADA = 1.3f;       // altura da fada no mundo
    private static final float CHAO_Y = 1.65f;           // posição inicial da fada no cenário
    private static final float VELOCIDADE_FADA = 2.0f;   // unidades do mundo por segundo
    private static final float FADA_X_INICIAL = 2.0f;
    private static final float CAMERA_X_INICIAL = 0.5f;
    private static final float CAMERA_Y_INICIAL = 0.75f;
    private static final float DISTANCIA_REGAR = 1.3f;

    // ---- Fase 1 ----
    private static final float FLOR_X = 11.15f;
    private static final float ESCALA_FLOR = 0.8f;

    // ---- Fase 2 ----
    // Altura (no cenário) do chão onde as flores ficam. Se as flores parecerem
    // flutuando ou afundadas na imagem, ajuste SÓ este valor.
    private static final float CHAO_FLORES_FASE2 = CHAO_Y;
    private static final float FLOR1_X_FASE2 = 4.3f;
    private static final float FLOR2_X_FASE2 = 10.7f;
    private static final float DURACAO_TRANSFORMACAO_FLOR = 0.4f;
    // Largura da imagem da flor (proporcional ao cenário de 12 de largura)
    private static final float LARGURA_IMAGEM_FLOR_FASE2 = 6.9f;
    // Ponto da imagem (0..1) onde fica a base da flor
    private static final float ANCORA_X_IMAGEM_FLOR_FASE2 = 0.83f;
    private static final float ANCORA_Y_IMAGEM_FLOR_FASE2 = 0.12f;

    // ---- Menu / botões ----
    private static final float PLAY_X = 6.02f;
    private static final float PLAY_Y = 0.95f;
    private static final float PLAY_RAIO = 0.28f;
    private static final Rectangle MENU_TEXTO_INICIAR = new Rectangle(4.27f, 1.00f, 1.40f, 0.36f);
    private static final boolean DEBUG_HITBOX = false;
    private static final Rectangle BTN_PAUSAR = new Rectangle(7.04f, 3.81f, 0.56f, 0.48f);
    private static final Rectangle BTN_PAUSA_CONTINUAR = new Rectangle(3.05f, 2.42f, 1.90f, 0.78f);
    private static final Rectangle BTN_PAUSA_REINICIAR = new Rectangle(3.10f, 1.70f, 1.75f, 0.68f);
    private static final Rectangle BTN_PAUSA_MENU = new Rectangle(3.10f, 1.00f, 1.78f, 0.65f);
    private static final Rectangle BTN_PROXIMA_FASE = new Rectangle(2.90f, 1.45f, 2.25f, 0.50f);
    private static final Rectangle BTN_JOGAR_NOVAMENTE = new Rectangle(3.08f, 1.02f, 1.88f, 0.40f);
    private static final boolean ESCURECER_NA_PAUSA = true;
    private static final float PAUSA_ESCURECIMENTO = 0.45f;

    private final ForceInput entradaForca;
    private Estado estado = Estado.MENU;
    private boolean pausado = false;
    private boolean nivelConcluido = false;
    private float forcaAtual;
    private Cursor.SystemCursor cursorAtual = Cursor.SystemCursor.Arrow;
    private FitViewport viewport;
    private ShapeRenderer desenhador;
    private SpriteBatch lote;
    private final Vector2 toque = new Vector2();

    private Texture texMenu;
    private Texture texCenario;
    private Texture texCenarioFase2;
    private Texture texNivelConcluido;
    private Texture texFlorMorta;
    private Texture texFlorViva;
    private Texture texFlorMortaFase2;
    private Texture texFlorVivaFase2;
    private Texture texFlorMortaYFase2;
    private Texture texFlorVivaYFase2;
    private Texture texPausa;
    private Texture texBotaoPausar;
    private Texture texFadaParada;
    private Texture texFadaAndando;
    private Texture texFadaRegando;
    private Texture texFadaVoando1;
    private Texture texFadaVoando2;

    // Estado da fada e da câmera (usado pelas duas fases)
    private float fadaX = FADA_X_INICIAL;  // centro dos pés da fada no cenário
    private float fadaY = CHAO_Y;
    private float cameraX = CAMERA_X_INICIAL;
    private float cameraY = CAMERA_Y_INICIAL;
    private boolean andando = false;
    private boolean subindo = false;
    private boolean descendo = false;
    private boolean viradaParaDireita = true;
    private float tempoAndando = 0f;

    // Estado específico da fase 2
    private boolean fase2Ativa = false;
    private boolean flor1Fase2Recuperada;
    private boolean flor2Fase2Recuperada;
    private boolean aguardandoNovaMedicaoFase2;
    private boolean conclusaoFase2Pendente;
    private float animacaoFlor1Fase2;
    private float animacaoFlor2Fase2;

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
        texCenarioFase2 = carregar("Fase2-jardim em recuperação.jpg");
        texNivelConcluido = carregar("Nível concluído.png");
        texFlorMorta = carregar("FlorMortaLvl1.png");
        texFlorViva = carregar("FlorRegadaLvl1.png");
        texFlorMortaFase2 = carregar("FlorMortaLvl2.png");
        texFlorVivaFase2 = carregar("FlorVivaLvl2.png");
        texFlorMortaYFase2 = carregar("FlorMortaYLvl2.png");
        texFlorVivaYFase2 = carregar("FlorVivaYLvl2.png");
        texPausa = carregar("MenuImplementado.png");
        texBotaoPausar = carregar("Pause.png");
        texFadaParada = carregar("Fada Estatica.png");
        texFadaAndando = carregar("Fada Andando.png");
        texFadaRegando = carregar("Fada Regando.png");
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

    // ------------------------------------------------------------------
    // Movimento da fada + câmera (idêntico nas duas fases)
    // ------------------------------------------------------------------
    private void atualizarMovimento(float delta) {
        boolean direita = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean esquerda = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean teclaCima = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean teclaBaixo = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        andando = direita != esquerda; // só anda se apertar UMA das setas
        subindo = teclaCima != teclaBaixo && teclaCima;
        descendo = teclaCima != teclaBaixo && teclaBaixo;

        float xAnterior = fadaX;
        float yAnterior = fadaY;

        if (andando) {
            viradaParaDireita = direita;
            fadaX += (direita ? 1f : -1f) * VELOCIDADE_FADA * delta;
            tempoAndando += delta;
        } else {
            tempoAndando = 0f;
        }

        if (subindo) {
            fadaY += VELOCIDADE_FADA * delta;
        } else if (descendo) {
            fadaY -= VELOCIDADE_FADA * delta;
        }

        float alturaMapa = alturaCenario();
        float alturaMaximaFada = Math.max(alturaDe(texFadaVoando1), alturaDe(texFadaVoando2));
        fadaY = MathUtils.clamp(fadaY, 0f, Math.max(0f, alturaMapa - alturaMaximaFada));

        float meiaLargura = larguraDe(texFadaAndando) / 2f;
        fadaX = MathUtils.clamp(fadaX, meiaLargura, LARGURA_CENARIO - meiaLargura);

        cameraX = MathUtils.clamp(cameraX + (fadaX - xAnterior) * SEGUIMENTO_CAMERA,
                0f, LARGURA_CENARIO - LARGURA_MUNDO);
        cameraY = MathUtils.clamp(cameraY + (fadaY - yAnterior) * SEGUIMENTO_CAMERA,
                0f, Math.max(0f, alturaMapa - ALTURA_MUNDO));
    }

    private void pararFada() {
        andando = false;
        subindo = false;
        descendo = false;
        tempoAndando = 0f;
    }

    private void resetarFada() {
        fadaX = FADA_X_INICIAL;
        fadaY = CHAO_Y;
        cameraX = CAMERA_X_INICIAL;
        cameraY = CAMERA_Y_INICIAL;
        viradaParaDireita = true;
        pararFada();
    }

    private void atualizarFase1(float delta) {
        atualizarMovimento(delta);
    }

    private void atualizarFase2(float delta) {
        // Avança as animações de transformação das flores
        if (animacaoFlor1Fase2 < DURACAO_TRANSFORMACAO_FLOR) {
            animacaoFlor1Fase2 = Math.min(DURACAO_TRANSFORMACAO_FLOR, animacaoFlor1Fase2 + delta);
        }
        if (animacaoFlor2Fase2 < DURACAO_TRANSFORMACAO_FLOR) {
            animacaoFlor2Fase2 = Math.min(DURACAO_TRANSFORMACAO_FLOR, animacaoFlor2Fase2 + delta);
        }

        if (conclusaoFase2Pendente) {
            // espera a animação da(s) flor(es) terminar antes de mostrar "Nível concluído"
            if (animacaoFlor1Fase2 >= DURACAO_TRANSFORMACAO_FLOR
                    && animacaoFlor2Fase2 >= DURACAO_TRANSFORMACAO_FLOR) {
                nivelConcluido = true;
                estado = Estado.NIVEL_CONCLUIDO;
                Gdx.app.log("Fase2", "Nível 2 concluído");
            }
            return;
        }

        atualizarMovimento(delta);

        // Mesma regra da fase 1: perto da flor + força máxima = rega
        forcaAtual = entradaForca.getForce();

        // Depois de regar uma flor, é preciso soltar a força antes de regar a próxima
        if (aguardandoNovaMedicaoFase2 && forcaAtual <= 0.01f) {
            aguardandoNovaMedicaoFase2 = false;
        }

        int florAlvo = florAlvoFase2();
        if (!aguardandoNovaMedicaoFase2 && florAlvo >= 0 && forcaAtual >= 0.999f) {
            if (florAlvo == 0) {
                flor1Fase2Recuperada = true;
                animacaoFlor1Fase2 = 0f;
            } else {
                flor2Fase2Recuperada = true;
                animacaoFlor2Fase2 = 0f;
            }
            aguardandoNovaMedicaoFase2 = true;
        }

        if (flor1Fase2Recuperada && flor2Fase2Recuperada) {
            conclusaoFase2Pendente = true;
            pararFada();
        }
    }

    private int florAlvoFase2() {
        boolean pertoFlor1 = !flor1Fase2Recuperada
                && Math.abs(fadaX - FLOR1_X_FASE2) <= DISTANCIA_REGAR
                && Math.abs(fadaY - CHAO_FLORES_FASE2) <= DISTANCIA_REGAR;
        boolean pertoFlor2 = !flor2Fase2Recuperada
                && Math.abs(fadaX - FLOR2_X_FASE2) <= DISTANCIA_REGAR
                && Math.abs(fadaY - CHAO_FLORES_FASE2) <= DISTANCIA_REGAR;
        if (!pertoFlor1 && !pertoFlor2) {
            return -1;
        }
        if (pertoFlor1 && pertoFlor2) {
            return Math.abs(fadaX - FLOR1_X_FASE2)
                    <= Math.abs(fadaX - FLOR2_X_FASE2) ? 0 : 1;
        }
        return pertoFlor1 ? 0 : 1;
    }

    // ------------------------------------------------------------------
    // Fada
    // ------------------------------------------------------------------
    // As imagens da fada usam a MESMA escala, para ela não mudar de tamanho ao trocar.
    private float escala() {
        return ALTURA_FADA / texFadaParada.getHeight();
    }

    private float larguraDe(Texture t) {
        return t.getWidth() * escala();
    }

    private float alturaDe(Texture t) {
        return t.getHeight() * escala();
    }

    // Usada pelas duas fases
    private void desenharFada() {
        Texture tex;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            tex = texFadaRegando;
        } else if (subindo) {
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
                fadaX - cameraX - w / 2f, fadaY - cameraY + pulo, w, h,
                0, 0, tex.getWidth(), tex.getHeight(),
                flip, false);
    }

    // ------------------------------------------------------------------
    // Desenho
    // ------------------------------------------------------------------
    private void desenharBarraForca() {
        desenhador.begin(ShapeRenderer.ShapeType.Filled);
        desenhador.setColor(0.25f, 0.25f, 0.3f, 1.0f);
        desenhador.rect(0.3f, 0.5f, LARGURA_BARRA, ALTURA_BARRA);
        desenhador.setColor(0.4f, 0.85f, 0.55f, 1.0f);
        desenhador.rect(0.3f, 0.5f, LARGURA_BARRA, ALTURA_BARRA * forcaAtual);
        desenhador.end();
    }

    private void desenharPausa() {
        if (!pausado) {
            return;
        }
        if (ESCURECER_NA_PAUSA) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            desenhador.begin(ShapeRenderer.ShapeType.Filled);
            desenhador.setColor(0f, 0f, 0f, PAUSA_ESCURECIMENTO);
            desenhador.rect(0f, 0f, LARGURA_MUNDO, ALTURA_MUNDO);
            desenhador.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        lote.begin();
        lote.draw(texPausa, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        lote.end();
    }

    private void desenharHitboxesPausa() {
        desenhador.rect(BTN_PAUSAR.x, BTN_PAUSAR.y, BTN_PAUSAR.width, BTN_PAUSAR.height);
        if (pausado) {
            desenhador.rect(BTN_PAUSA_CONTINUAR.x, BTN_PAUSA_CONTINUAR.y,
                    BTN_PAUSA_CONTINUAR.width, BTN_PAUSA_CONTINUAR.height);
            desenhador.rect(BTN_PAUSA_REINICIAR.x, BTN_PAUSA_REINICIAR.y,
                    BTN_PAUSA_REINICIAR.width, BTN_PAUSA_REINICIAR.height);
            desenhador.rect(BTN_PAUSA_MENU.x, BTN_PAUSA_MENU.y,
                    BTN_PAUSA_MENU.width, BTN_PAUSA_MENU.height);
        }
    }

    private void desenharFlorFase2(Texture tex, float x, float alpha) {
        float altura = LARGURA_IMAGEM_FLOR_FASE2 * tex.getHeight() / tex.getWidth();
        // A base da flor (âncora) fica sempre sobre o chão do cenário
        float desenhoX = x - ANCORA_X_IMAGEM_FLOR_FASE2 * LARGURA_IMAGEM_FLOR_FASE2 - cameraX;
        float desenhoY = CHAO_FLORES_FASE2 - ANCORA_Y_IMAGEM_FLOR_FASE2 * altura - cameraY;
        lote.setColor(1f, 1f, 1f, alpha);
        lote.draw(tex, desenhoX, desenhoY, LARGURA_IMAGEM_FLOR_FASE2, altura);
        lote.setColor(1f, 1f, 1f, 1f);
    }

    private void desenharFase2() {
        lote.begin();
        lote.draw(texCenarioFase2, -cameraX, -cameraY, LARGURA_CENARIO, alturaCenario());
        desenharFlorFase2(flor1Fase2Recuperada ? texFlorVivaFase2 : texFlorMortaFase2,
                FLOR1_X_FASE2,
                flor1Fase2Recuperada
                        ? Math.min(1f, animacaoFlor1Fase2 / DURACAO_TRANSFORMACAO_FLOR) : 1f);
        desenharFlorFase2(flor2Fase2Recuperada ? texFlorVivaYFase2 : texFlorMortaYFase2,
                FLOR2_X_FASE2,
                flor2Fase2Recuperada
                        ? Math.min(1f, animacaoFlor2Fase2 / DURACAO_TRANSFORMACAO_FLOR) : 1f);
        desenharFada();
        lote.draw(texBotaoPausar, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        lote.end();

        desenharBarraForca();

        if (DEBUG_HITBOX) {
            desenhador.begin(ShapeRenderer.ShapeType.Line);
            desenhador.setColor(1f, 1f, 1f, 1f);
            desenharHitboxesPausa();
            // linha do chão das flores e pontos de âncora
            desenhador.line(0f, CHAO_FLORES_FASE2 - cameraY,
                    LARGURA_MUNDO, CHAO_FLORES_FASE2 - cameraY);
            desenhador.circle(FLOR1_X_FASE2 - cameraX, CHAO_FLORES_FASE2 - cameraY, 0.08f);
            desenhador.circle(FLOR2_X_FASE2 - cameraX, CHAO_FLORES_FASE2 - cameraY, 0.08f);
            desenhador.end();
        }

        desenharPausa();
    }

    private void desenharFase1() {
        float alturaCenario = alturaCenario();
        float larguraFlor = LARGURA_MUNDO * ESCALA_FLOR;
        float alturaFlor = ALTURA_MUNDO * ESCALA_FLOR;
        float xFlor = FLOR_X - larguraFlor / 2f - cameraX;
        float yFlor = CAMERA_Y_INICIAL + CHAO_Y * (1f - ESCALA_FLOR) - cameraY;

        lote.begin();
        lote.draw(texCenario, -cameraX, -cameraY, LARGURA_CENARIO, alturaCenario);
        lote.draw(nivelConcluido ? texFlorViva : texFlorMorta,
                xFlor, yFlor, larguraFlor, alturaFlor);
        desenharFada();
        lote.draw(texBotaoPausar, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        lote.end();

        // Barra de força (teste do sensor), por cima do cenário
        desenharBarraForca();

        if (DEBUG_HITBOX) {
            desenhador.begin(ShapeRenderer.ShapeType.Line);
            desenhador.setColor(1f, 1f, 1f, 1f);
            desenharHitboxesPausa();
            desenhador.line(FLOR_X - DISTANCIA_REGAR - cameraX, 0f,
                    FLOR_X - DISTANCIA_REGAR - cameraX, ALTURA_MUNDO);
            desenhador.line(FLOR_X + DISTANCIA_REGAR - cameraX, 0f,
                    FLOR_X + DISTANCIA_REGAR - cameraX, ALTURA_MUNDO);
            desenhador.end();
        }

        desenharPausa();
    }

    private boolean fadaPertoDaFlor() {
        return Math.abs(fadaX - FLOR_X) <= DISTANCIA_REGAR;
    }

    // Altura do cenário da fase atual (as duas fases usam a mesma largura)
    private float alturaCenario() {
        Texture t = fase2Ativa ? texCenarioFase2 : texCenario;
        return LARGURA_CENARIO * t.getHeight() / t.getWidth();
    }

    private void verificarRega() {
        if (!nivelConcluido && forcaAtual >= 0.999f && fadaPertoDaFlor()) {
            nivelConcluido = true;
            estado = Estado.NIVEL_CONCLUIDO;
            pararFada();
            Gdx.app.log("Fase1", "Nivel 1 concluido");
        }
    }

    private void desenharNivelConcluido() {
        viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreProximaFase = BTN_PROXIMA_FASE.contains(toque);
        boolean sobreJogarNovamente = BTN_JOGAR_NOVAMENTE.contains(toque);
        atualizarCursor(sobreProximaFase || sobreJogarNovamente
                ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

        if (Gdx.input.justTouched()) {
            if (sobreJogarNovamente) {
                reiniciarFaseAtual();
                return;
            } else if (sobreProximaFase) {
                if (fase2Ativa) {
                    // não existe fase 3: volta ao menu em vez de reiniciar a fase 2
                    voltarAoMenu();
                } else {
                    iniciarFase2();
                }
                return;
            }
        }

        lote.begin();
        lote.draw(texNivelConcluido, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        lote.end();
    }

    private void reiniciarFaseAtual() {
        if (fase2Ativa) {
            iniciarFase2();
        } else {
            iniciarFase1();
        }
    }

    private void iniciarFase1() {
        estado = Estado.FASE1;
        fase2Ativa = false;
        atualizarCursor(Cursor.SystemCursor.Arrow);
        pausado = false;
        entradaForca.reset();
        nivelConcluido = false;
        forcaAtual = 0f;
        resetarFada();
    }

    private void iniciarFase2() {
        estado = Estado.FASE2;
        fase2Ativa = true;
        atualizarCursor(Cursor.SystemCursor.Arrow);
        pausado = false;
        entradaForca.reset();
        nivelConcluido = false;
        forcaAtual = 0f;
        resetarFada();
        flor1Fase2Recuperada = false;
        flor2Fase2Recuperada = false;
        aguardandoNovaMedicaoFase2 = false;
        conclusaoFase2Pendente = false;
        animacaoFlor1Fase2 = DURACAO_TRANSFORMACAO_FLOR;
        animacaoFlor2Fase2 = DURACAO_TRANSFORMACAO_FLOR;
    }

    private Rectangle botaoSobMouse() {
        if (pausado && BTN_PAUSA_CONTINUAR.contains(toque)) {
            return BTN_PAUSA_CONTINUAR;
        } else if (pausado && BTN_PAUSA_REINICIAR.contains(toque)) {
            return BTN_PAUSA_REINICIAR;
        } else if (pausado && BTN_PAUSA_MENU.contains(toque)) {
            return BTN_PAUSA_MENU;
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

    private void atualizarEntradaFase() {
        viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
        Rectangle botao = botaoSobMouse();
        atualizarCursor(botao != null ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

        if (Gdx.input.justTouched() && botao != null) {
            if (botao == BTN_PAUSAR) {
                pausado = true;
            } else if (botao == BTN_PAUSA_CONTINUAR) {
                pausado = false;
            } else if (botao == BTN_PAUSA_REINICIAR) {
                reiniciarFaseAtual();
            } else if (botao == BTN_PAUSA_MENU) {
                voltarAoMenu();
            }
        }
    }

    private void desenharMenu() {
        viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobrePlay = toque.dst(PLAY_X, PLAY_Y) <= PLAY_RAIO || MENU_TEXTO_INICIAR.contains(toque);
        atualizarCursor(sobrePlay ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

        if (Gdx.input.justTouched() && sobrePlay) {
            iniciarFase1();
            return;
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
            desenhador.rect(MENU_TEXTO_INICIAR.x, MENU_TEXTO_INICIAR.y,
                    MENU_TEXTO_INICIAR.width, MENU_TEXTO_INICIAR.height);
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
                atualizarEntradaFase();
                if (estado == Estado.MENU) {
                    break;
                }
                if (!estavaPausado && !pausado && !nivelConcluido) {
                    atualizarFase1(Gdx.graphics.getDeltaTime());
                    forcaAtual = entradaForca.getForce();
                    verificarRega();
                }
                if (estado == Estado.NIVEL_CONCLUIDO) {
                    desenharNivelConcluido();
                } else {
                    desenharFase1();
                }
                break;
            }
            case FASE2: {
                boolean estavaPausado = pausado;
                atualizarEntradaFase();
                if (estado == Estado.MENU) {
                    break;
                }
                if (!estavaPausado && !pausado) {
                    atualizarFase2(Gdx.graphics.getDeltaTime());
                }
                if (estado == Estado.NIVEL_CONCLUIDO) {
                    desenharNivelConcluido();
                } else {
                    desenharFase2();
                }
                break;
            }
            case NIVEL_CONCLUIDO:
                desenharNivelConcluido();
                break;
        }
    }

    @Override
    public void dispose() {
        desenhador.dispose();
        lote.dispose();
        texMenu.dispose();
        texCenario.dispose();
        texCenarioFase2.dispose();
        texNivelConcluido.dispose();
        texFlorMorta.dispose();
        texFlorViva.dispose();
        texFlorMortaFase2.dispose();
        texFlorVivaFase2.dispose();
        texFlorMortaYFase2.dispose();
        texFlorVivaYFase2.dispose();
        texPausa.dispose();
        texBotaoPausar.dispose();
        texFadaParada.dispose();
        texFadaAndando.dispose();
        texFadaRegando.dispose();
        texFadaVoando1.dispose();
        texFadaVoando2.dispose();
    }
}