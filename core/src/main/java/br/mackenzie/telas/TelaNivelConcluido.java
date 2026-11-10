package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.ui.AreaClicavel;
import br.mackenzie.ui.BotaoRetangular;

/** Tela "Fase concluída!": próxima fase ou jogar a mesma fase de novo. */
public class TelaNivelConcluido extends TelaComBotoes {
    private final AreaClicavel jogarNovamente = new BotaoRetangular(3.05f, 0.52f, 1.90f, 0.46f);
    private final AreaClicavel proximaFase = new BotaoRetangular(2.85f, 1.00f, 2.40f, 0.62f);
    private final AreaClicavel[] botoes = {jogarNovamente, proximaFase};

    public TelaNivelConcluido(Main jogo) {
        super(jogo, Imagem.NIVEL_CONCLUIDO);
    }

    @Override
    protected AreaClicavel[] botoes() {
        return botoes;
    }

    @Override
    protected void aoClicar(AreaClicavel botao) {
        if (botao == jogarNovamente) {
            jogo.reiniciarFaseAtual();
        } else if (botao == proximaFase) {
            jogo.irParaProximaFase();
        }
    }

    @Override
    public boolean ehTelaDeConclusao() {
        return true;
    }
}
