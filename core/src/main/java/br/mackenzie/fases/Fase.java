package br.mackenzie.fases;

import br.mackenzie.Mundo;
import br.mackenzie.entidades.CenarioRolante;
import br.mackenzie.entidades.Flor;
import br.mackenzie.entidades.Lumi;
import br.mackenzie.metricas.ResultadoFase;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A lógica de uma fase em andamento (sem desenho): Lumi, cenário, flores, rega, flores que voltam e métricas.
 * É igual para as 3 fases; o que muda vem da {@link ConfiguracaoFase}.
 */
public class Fase {
    // ---- Flores e luzes mágicas ----
    public static final float CHAO_FLORES = 1.65f;              // chão das flores (coordenada do cenário)
    private static final float PRIMEIRA_FLOR_X_TELA = 5.0f;     // onde a 1ª flor aparece ao começar a fase
    private static final float ESPACO_ENTRE_FLORES = 3.5f;      // distância entre uma flor e a próxima
    private static final float TAMANHO_LUZ = 1.0f;              // tamanho da imagem da luz (escala 1)
    private static final float TOLERANCIA_ALTURA = 0.30f;       // quão perto (na vertical) a Lumi precisa estar da luz
    public static final float ALCANCE_X = 0.6f;                 // janela horizontal em que a Lumi alcança a luz
    public static final float ALCANCE_X_DESLOCAMENTO = 0.25f;   // a mão da Lumi fica um pouco à direita do corpo
    private static final float FATOR_VELOCIDADE_REGANDO = 0.2f; // o cenário desacelera enquanto ela rega
    private static final float DISTANCIA_SAIDA_TELA = 0.7f;     // flor passou deste ponto à esquerda = "volta mais tarde"
    private static final float MARGEM_RETORNO = 0.8f;           // a flor que volta reaparece logo depois da borda direita

    private final ConfiguracaoFase configuracao;
    private final Lumi lumi = new Lumi();
    private final CenarioRolante cenario = new CenarioRolante();
    private final List<Flor> flores = new ArrayList<>();

    private float forcaAtual = 0f;
    private float tempoFase = 0f;          // segundos jogados (para na pausa)
    private boolean tocandoLuz = false;    // Lumi está regando
    private boolean conclusaoPendente = false;
    private boolean concluida = false;

    public Fase(ConfiguracaoFase configuracao) {
        this.configuracao = configuracao;
        for (int i = 0; i < configuracao.quantidadeDeFlores(); i++) {
            flores.add(new Flor(i, PRIMEIRA_FLOR_X_TELA + i * ESPACO_ENTRE_FLORES, configuracao.alturaDaLuz(i)));
        }
    }

    // ------------------------------------------------------------------
    // Atualização (1x por quadro, só quando não está pausado)
    // ------------------------------------------------------------------
    public void atualizar(float forca, float delta) {
        for (Flor flor : flores) {
            flor.atualizarAnimacao(delta);
        }

        forcaAtual = forca;
        lumi.atualizar(forcaAtual, delta);

        if (conclusaoPendente) {
            tocandoLuz = false;
            cenario.avancar(configuracao.velocidadeDoCenario() * delta);
            // espera a animação da última flor antes de encerrar a fase
            if (animacoesTerminaram()) {
                concluida = true;
            }
            return;
        }

        tempoFase += delta;   // para quando a última flor desperta (conclusaoPendente)

        medirChegada();

        // Rega: a Lumi está tocando a luz de alguma flor seca?
        tocandoLuz = false;
        for (Flor flor : flores) {
            if (flor.isRecuperada()) {
                continue;
            }
            if (lumiTocaLuz(flor)) {
                tocandoLuz = true;
                if (flor.regar(forcaAtual, delta, configuracao.tempoParaDespertar())) {
                    Gdx.app.log("Fase" + numero(), "Flor " + (flor.getIndice() + 1) + " despertou (tentativas: "
                            + flor.getTentativas() + ", chegada: " + flor.getChegada() + " s, firmeza: "
                            + Math.round(flor.getFirmeza() * 100) + "%)");
                }
            } else {
                flor.decairProgresso(delta, configuracao.tempoParaDespertar());
            }
        }

        // O cenário anda sozinho; desacelera enquanto a Lumi rega
        float velocidade = configuracao.velocidadeDoCenario() * (tocandoLuz ? FATOR_VELOCIDADE_REGANDO : 1f);
        cenario.avancar(velocidade * delta);

        // Flor que passou sem despertar "volta mais tarde", entrando de novo pela direita
        for (Flor flor : flores) {
            if (!flor.isRecuperada() && luzXNaTela(flor) < -DISTANCIA_SAIDA_TELA) {
                flor.voltarMaisTarde(posicaoDeRetorno(flor));
                Gdx.app.log("Fase" + numero(), "Flor " + (flor.getIndice() + 1) + " passou; volta mais tarde");
            }
        }

        if (todasRecuperadas()) {
            conclusaoPendente = true;
            tocandoLuz = false;
        }
    }

    // Velocidade (artigo, 3.4): tempo que a criança leva para levar a Lumi até a ALTURA da próxima flor.
    // O relógio começa quando a luz dessa flor aparece na tela (e ela é a próxima a ser regada)
    // e para quando a Lumi entra na faixa de altura da luz.
    private void medirChegada() {
        float limiteEsquerdo = Lumi.X_NA_TELA + ALCANCE_X_DESLOCAMENTO - ALCANCE_X;
        Flor proxima = null;
        for (Flor flor : flores) {
            if (flor.isRecuperada() || luzXNaTela(flor) < limiteEsquerdo) {
                continue;
            }
            if (proxima == null || luzXNaTela(flor) < luzXNaTela(proxima)) {
                proxima = flor;
            }
        }
        if (proxima == null || proxima.chegadaRegistrada()) {
            return;
        }
        if (!proxima.relogioIniciado()) {
            if (luzXNaTela(proxima) <= Mundo.LARGURA) {
                proxima.iniciarRelogio(tempoFase);
            }
            return;
        }
        if (Math.abs(luzYNaTela(proxima) - lumi.getY()) <= toleranciaAltura()) {
            proxima.registrarChegada(tempoFase);
        }
    }

    // Onde a flor reaparece: logo depois da borda direita da tela,
    // mas sempre a uma distância segura das outras flores que ainda estão chegando
    private float posicaoDeRetorno(Flor flor) {
        float posicao = cenario.getDeslocamento() + Mundo.LARGURA + MARGEM_RETORNO;
        for (Flor outra : flores) {
            if (outra != flor) {
                posicao = Math.max(posicao, outra.getX() + ESPACO_ENTRE_FLORES);
            }
        }
        return posicao;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    /** A Lumi alcança a luz da flor? (perto na horizontal E na altura certa) */
    public boolean lumiTocaLuz(Flor flor) {
        float dx = Math.abs(luzXNaTela(flor) - (Lumi.X_NA_TELA + ALCANCE_X_DESLOCAMENTO));
        float dy = Math.abs(luzYNaTela(flor) - lumi.getY());
        return dx <= ALCANCE_X && dy <= toleranciaAltura();
    }

    public float luzXNaTela(Flor flor) {
        return flor.getX() - cenario.getDeslocamento();
    }

    public float luzYNaTela(Flor flor) {
        return Lumi.alturaNaTela(flor.getAlturaDaLuz());
    }

    public float tamanhoLuz() {
        return TAMANHO_LUZ * configuracao.escalaDaLuz();
    }

    public float toleranciaAltura() {
        return TOLERANCIA_ALTURA * configuracao.escalaDaLuz();
    }

    public boolean todasRecuperadas() {
        for (Flor flor : flores) {
            if (!flor.isRecuperada()) {
                return false;
            }
        }
        return true;
    }

    public int floresRecuperadas() {
        int total = 0;
        for (Flor flor : flores) {
            if (flor.isRecuperada()) total++;
        }
        return total;
    }

    private boolean animacoesTerminaram() {
        for (Flor flor : flores) {
            if (!flor.animacaoTerminou()) {
                return false;
            }
        }
        return true;
    }

    /** Resultado da fase (chamar quando ela terminar). */
    public ResultadoFase gerarResultado() {
        float somaChegada = 0f;
        float somaFirmeza = 0f;
        int voltas = 0;
        for (Flor flor : flores) {
            somaChegada += flor.getChegada();
            somaFirmeza += flor.getFirmeza();
            voltas += flor.getTentativas() - 1;
        }
        int quantidade = flores.size();
        return new ResultadoFase(numero(), tempoFase, somaChegada / quantidade, somaFirmeza / quantidade, voltas);
    }

    public int numero() {
        return configuracao.numero();
    }

    public ConfiguracaoFase getConfiguracao() {
        return configuracao;
    }

    public Lumi getLumi() {
        return lumi;
    }

    public CenarioRolante getCenario() {
        return cenario;
    }

    public List<Flor> getFlores() {
        return Collections.unmodifiableList(flores);
    }

    public float getForcaAtual() {
        return forcaAtual;
    }

    public float getTempoFase() {
        return tempoFase;
    }

    public boolean isTocandoLuz() {
        return tocandoLuz;
    }

    public boolean isConclusaoPendente() {
        return conclusaoPendente;
    }

    /** A última flor despertou e a animação terminou: hora de mostrar a tela de conclusão. */
    public boolean isConcluida() {
        return concluida;
    }
}
