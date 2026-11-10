package br.mackenzie;

import br.mackenzie.fases.ConfiguracaoFase;
import br.mackenzie.fases.ConfiguracaoFase1;
import br.mackenzie.fases.Fase;
import br.mackenzie.metricas.ResultadoFase;
import br.mackenzie.metricas.ResultadosDoJogo;
import br.mackenzie.recursos.EstadoDeAudio;
import br.mackenzie.recursos.GerenciadorAudio;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.recursos.Recursos;
import br.mackenzie.telas.TelaFase;
import br.mackenzie.telas.TelaJardimRestaurado;
import br.mackenzie.telas.TelaMenu;
import br.mackenzie.telas.TelaNivelConcluido;
import br.mackenzie.telas.TelaResultados;
import br.mackenzie.ui.Hud;
import br.mackenzie.ui.TextoNumerico;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Ponto de entrada do jogo FloraLume - Jornada da Fada Mágica.
 * <p>
 * Herda de {@link Game} do libGDX, que troca de tela com {@code setScreen()}.
 * Esta classe cuida da NAVEGAÇÃO entre as telas e guarda o que é compartilhado por todas
 * (recursos, áudio, desenho, entrada de força e resultados).
 * <pre>
 * Menu -> Fase 1 -> Fase concluída -> Fase 2 -> Fase concluída -> Fase 3 -> Jardim restaurado -> Resultados
 * </pre>
 */
public class Main extends Game {
    private final ForceInput entradaForca;

    private FitViewport viewport;
    private SpriteBatch lote;
    private ShapeRenderer desenhador;
    private Recursos recursos;
    private GerenciadorAudio audio;
    private TextoNumerico textoNumerico;
    private Hud hud;
    private final ResultadosDoJogo resultados = new ResultadosDoJogo();
    private Cursor.SystemCursor cursorAtual = Cursor.SystemCursor.Arrow;

    // As telas são criadas uma vez e reaproveitadas
    private TelaMenu telaMenu;
    private TelaFase telaFase;
    private TelaNivelConcluido telaNivelConcluido;
    private TelaJardimRestaurado telaJardimRestaurado;
    private TelaResultados telaResultados;

    public Main(ForceInput entradaForca) {
        this.entradaForca = entradaForca;
    }

    @Override
    public void create() {
        viewport = new FitViewport(Mundo.LARGURA, Mundo.ALTURA);
        desenhador = new ShapeRenderer();
        lote = new SpriteBatch();
        recursos = new Recursos();
        recursos.carregar();
        audio = new GerenciadorAudio(recursos);
        textoNumerico = new TextoNumerico(recursos.textura(Imagem.NUMEROS));
        hud = new Hud(recursos, textoNumerico);

        telaMenu = new TelaMenu(this);
        telaFase = new TelaFase(this);
        telaNivelConcluido = new TelaNivelConcluido(this);
        telaJardimRestaurado = new TelaJardimRestaurado(this);
        telaResultados = new TelaResultados(this);

        setScreen(telaMenu);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        lote.setProjectionMatrix(viewport.getCamera().combined);
        desenhador.setProjectionMatrix(viewport.getCamera().combined);

        super.render();   // desenha a tela atual

        // o som depende da tela em que o jogo terminou este quadro
        audio.atualizar((EstadoDeAudio) getScreen());
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
        super.resize(largura, altura);
    }

    // ------------------------------------------------------------------
    // Navegação entre telas
    // ------------------------------------------------------------------
    public void irParaMenu() {
        setScreen(telaMenu);
    }

    /** Jogo novo: apaga os resultados anteriores e começa pela fase 1. */
    public void iniciarJogo() {
        resultados.limpar();
        iniciarFase(new ConfiguracaoFase1());
    }

    public void iniciarFase(ConfiguracaoFase configuracao) {
        telaFase.prepararFase(configuracao);
        audio.reiniciarMusicaDaFase();
        setScreen(telaFase);
    }

    /** "Jogar novamente" na tela de fase concluída. Na fase 1 isso é um jogo novo. */
    public void reiniciarFaseAtual() {
        ConfiguracaoFase atual = telaFase.getFase().getConfiguracao();
        if (atual.numero() == 1) {
            iniciarJogo();
        } else {
            iniciarFase(atual);
        }
    }

    public void irParaProximaFase() {
        iniciarFase(telaFase.getFase().getConfiguracao().proxima());
    }

    /** A fase terminou: guarda o resultado e mostra a tela de conclusão certa. */
    public void concluirFase(Fase fase) {
        ResultadoFase resultado = fase.gerarResultado();
        resultados.registrar(resultado);
        Gdx.app.log("Resultado", resultado.toString());
        Gdx.app.log("Fase" + fase.numero(), "Nível " + fase.numero() + " concluído");
        if (fase.getConfiguracao().ehUltima()) {
            setScreen(telaJardimRestaurado);
        } else {
            setScreen(telaNivelConcluido);
        }
    }

    public void mostrarResultados() {
        setScreen(telaResultados);
    }

    // ------------------------------------------------------------------
    // Compartilhado entre as telas
    // ------------------------------------------------------------------
    public void atualizarCursor(Cursor.SystemCursor cursor) {
        if (cursorAtual != cursor) {
            Gdx.graphics.setSystemCursor(cursor);
            cursorAtual = cursor;
        }
    }

    public ForceInput getEntradaForca() {
        return entradaForca;
    }

    public FitViewport getViewport() {
        return viewport;
    }

    public SpriteBatch getLote() {
        return lote;
    }

    public ShapeRenderer getDesenhador() {
        return desenhador;
    }

    public Recursos getRecursos() {
        return recursos;
    }

    public GerenciadorAudio getAudio() {
        return audio;
    }

    public TextoNumerico getTextoNumerico() {
        return textoNumerico;
    }

    public Hud getHud() {
        return hud;
    }

    public ResultadosDoJogo getResultados() {
        return resultados;
    }

    @Override
    public void dispose() {
        super.dispose();
        recursos.dispose();
        lote.dispose();
        desenhador.dispose();
    }
}
