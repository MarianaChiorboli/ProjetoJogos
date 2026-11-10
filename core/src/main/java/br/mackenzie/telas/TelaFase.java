package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.Mundo;
import br.mackenzie.entidades.CenarioRolante;
import br.mackenzie.entidades.Flor;
import br.mackenzie.entidades.Lumi;
import br.mackenzie.fases.ConfiguracaoFase;
import br.mackenzie.fases.Fase;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.ui.AreaClicavel;
import br.mackenzie.ui.BotaoRetangular;
import br.mackenzie.ui.Hud;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Tela das fases 1, 2 e 3: desenha a {@link Fase}, trata a pausa e os botões.
 * A lógica do jogo fica na classe Fase; aqui fica só o que é de tela (entrada, desenho, pausa).
 */
public class TelaFase extends TelaBase {
    private static final boolean ESCURECER_NA_PAUSA = true;
    private static final float PAUSA_ESCURECIMENTO = 0.45f;

    private final AreaClicavel pausar = new BotaoRetangular(7.04f, 3.81f, 0.56f, 0.48f);
    private final AreaClicavel continuar = new BotaoRetangular(3.05f, 2.42f, 1.90f, 0.78f);
    private final AreaClicavel reiniciar = new BotaoRetangular(3.10f, 1.70f, 1.75f, 0.68f);
    private final AreaClicavel menu = new BotaoRetangular(3.10f, 1.00f, 1.78f, 0.65f);

    private final Hud hud;
    private Fase fase;
    private boolean pausado = false;

    public TelaFase(Main jogo) {
        super(jogo);
        this.hud = jogo.getHud();
    }

    /** Prepara uma fase nova (ou reiniciada) do zero. */
    public void prepararFase(ConfiguracaoFase configuracao) {
        fase = new Fase(configuracao);
        pausado = false;
        jogo.getEntradaForca().reset();
    }

    public Fase getFase() {
        return fase;
    }

    @Override
    public void hide() {
        pausado = false;
    }

    @Override
    public void render(float delta) {
        boolean estavaPausado = pausado;
        tratarBotoes();
        if (jogo.getScreen() != this) {
            return;     // foi para outra tela (ex.: menu)
        }
        if (!estavaPausado && !pausado) {
            // A força é lida UMA vez por quadro (o KeyboardForceInput muda a cada chamada)
            fase.atualizar(jogo.getEntradaForca().getForce(), delta);
            if (fase.isConcluida()) {
                jogo.concluirFase(fase);
                return;
            }
        }
        desenhar();
    }

    // ------------------------------------------------------------------
    // Entrada (pausa e botões da pausa)
    // ------------------------------------------------------------------
    private AreaClicavel botaoSobMouse() {
        if (pausado && mouseSobre(continuar)) {
            return continuar;
        } else if (pausado && mouseSobre(reiniciar)) {
            return reiniciar;
        } else if (pausado && mouseSobre(menu)) {
            return menu;
        } else if (!pausado && mouseSobre(pausar)) {
            return pausar;
        }
        return null;
    }

    private void tratarBotoes() {
        lerMouse();
        AreaClicavel botao = botaoSobMouse();
        mostrarMaozinha(botao != null);

        if (clicou() && botao != null) {
            if (botao == pausar) {
                pausado = true;
            } else if (botao == continuar) {
                pausado = false;
            } else if (botao == reiniciar) {
                jogo.iniciarJogo();
            } else if (botao == menu) {
                jogo.irParaMenu();
            }
        }
    }

    // ------------------------------------------------------------------
    // Desenho
    // ------------------------------------------------------------------
    private void desenhar() {
        ConfiguracaoFase configuracao = fase.getConfiguracao();
        lote.begin();
        fase.getCenario().desenhar(lote, recursos.textura(configuracao.cenario()));
        for (Flor flor : fase.getFlores()) {
            desenharFlor(flor);
        }
        desenharLuzes();
        fase.getLumi().desenhar(lote, recursos, fase.isTocandoLuz());
        desenharTelaCheia(Imagem.BOTAO_PAUSAR);
        hud.desenharContador(lote, fase.getFlores().size(), fase.floresRecuperadas());
        hud.desenharTempo(lote, fase.getTempoFase());
        lote.end();

        desenharProgressoRega();
        hud.desenharBarraForca(desenhador, fase.getForcaAtual());
        desenharDebug();
        desenharPausa();
    }

    private void desenharFlor(Flor flor) {
        ConfiguracaoFase configuracao = fase.getConfiguracao();
        int indice = flor.getIndice();
        Texture seca = recursos.textura(configuracao.florSeca(indice));
        if (!flor.isRecuperada()) {
            desenharImagemDeFlor(seca, flor.getX(), 1f);
            return;
        }
        float alpha = flor.opacidadeViva();
        if (alpha < 1f) {
            // a flor seca some por baixo enquanto a viva aparece
            desenharImagemDeFlor(seca, flor.getX(), 1f - alpha);
        }
        desenharImagemDeFlor(recursos.textura(configuracao.florViva(indice)), flor.getX(), alpha);
    }

    private void desenharImagemDeFlor(Texture tex, float xCenario, float alpha) {
        ConfiguracaoFase configuracao = fase.getConfiguracao();
        float largura = configuracao.larguraImagemFlor();
        float altura = largura * tex.getHeight() / tex.getWidth();
        // A base da flor (âncora) fica sempre sobre o chão do cenário
        float desenhoX = xCenario - configuracao.ancoraXFlor() * largura - fase.getCenario().getDeslocamento();
        float desenhoY = Fase.CHAO_FLORES - configuracao.ancoraYFlor() * altura - CenarioRolante.CAMERA_Y;
        if (desenhoX > Mundo.LARGURA || desenhoX + largura < 0f) {
            return;     // fora da tela
        }
        lote.setColor(1f, 1f, 1f, alpha);
        lote.draw(tex, desenhoX, desenhoY, largura, altura);
        lote.setColor(1f, 1f, 1f, 1f);
    }

    // Luz mágica de cada flor seca, na altura que ela pede
    private void desenharLuzes() {
        Texture luz = recursos.textura(Imagem.LUZ_MAGICA);
        float base = fase.tamanhoLuz();
        float tempoVoo = fase.getLumi().getTempoVoo();
        for (Flor flor : fase.getFlores()) {
            if (flor.isRecuperada()) {
                continue;
            }
            float x = fase.luzXNaTela(flor);
            if (x < -base || x > Mundo.LARGURA + base) {
                continue;
            }
            boolean tocando = !fase.isConclusaoPendente() && fase.lumiTocaLuz(flor);
            // pulsa de leve; cresce quando a Lumi está tocando
            float pulso = 1f + MathUtils.sin(tempoVoo * 4f + flor.getIndice()) * 0.06f;
            float tam = base * pulso * (tocando ? 1.25f : 1f);
            lote.draw(luz, x - tam / 2f, fase.luzYNaTela(flor) - tam / 2f, tam, tam);
        }
    }

    // Anel de progresso ao lado da luz que está sendo regada
    private void desenharProgressoRega() {
        boolean algum = false;
        for (Flor flor : fase.getFlores()) {
            if (!flor.isRecuperada() && flor.getProgresso() > 0f) {
                algum = true;
                break;
            }
        }
        if (!algum) {
            return;
        }
        float raio = 0.15f;
        float afastamento = fase.tamanhoLuz() * 0.5f;
        desenhador.begin(ShapeRenderer.ShapeType.Filled);
        for (Flor flor : fase.getFlores()) {
            if (flor.isRecuperada() || flor.getProgresso() <= 0f) {
                continue;
            }
            float cx = fase.luzXNaTela(flor) + afastamento;
            float cy = fase.luzYNaTela(flor) + afastamento;
            desenhador.setColor(0.43f, 0.29f, 0.32f, 1f);          // contorno (marrom dos botões)
            desenhador.circle(cx, cy, raio + 0.03f, 24);
            desenhador.setColor(0.91f, 0.89f, 0.80f, 1f);          // fundo creme
            desenhador.circle(cx, cy, raio, 24);
            desenhador.setColor(0.47f, 0.75f, 0.43f, 1f);          // verde do progresso
            float graus = 360f * flor.getProgresso();
            desenhador.arc(cx, cy, raio - 0.02f, 90f - graus, graus, 24);
        }
        desenhador.end();
    }

    private void desenharDebug() {
        if (!Mundo.DEBUG_HITBOX) {
            return;
        }
        desenhador.begin(ShapeRenderer.ShapeType.Line);
        desenhador.setColor(1f, 1f, 1f, 1f);
        pausar.desenharContorno(desenhador);
        if (pausado) {
            continuar.desenharContorno(desenhador);
            reiniciar.desenharContorno(desenhador);
            menu.desenharContorno(desenhador);
        }
        // área em que a Lumi alcança uma luz, e o centro do corpo da Lumi
        float tol = fase.toleranciaAltura();
        float cx = Lumi.X_NA_TELA + Fase.ALCANCE_X_DESLOCAMENTO;
        float y = fase.getLumi().getY();
        desenhador.circle(Lumi.X_NA_TELA, y, 0.06f);
        desenhador.rect(cx - Fase.ALCANCE_X, y - tol, Fase.ALCANCE_X * 2f, tol * 2f);
        float chao = Fase.CHAO_FLORES - CenarioRolante.CAMERA_Y;
        desenhador.line(0f, chao, Mundo.LARGURA, chao);
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
            desenhador.rect(0f, 0f, Mundo.LARGURA, Mundo.ALTURA);
            desenhador.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        lote.begin();
        desenharTelaCheia(Imagem.PAUSA);
        lote.end();
    }

    // ---- Som: jardim durante a fase; menu quando pausado ----
    @Override
    public boolean tocaMusicaMenu() {
        return pausado;
    }

    @Override
    public boolean tocaMusicaJardim() {
        return !pausado;
    }

    @Override
    public boolean lumiEstaRegando() {
        return fase != null && fase.isTocandoLuz();
    }
}
