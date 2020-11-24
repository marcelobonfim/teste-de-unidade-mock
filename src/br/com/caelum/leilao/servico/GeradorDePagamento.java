package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.relogio.Relogio;

public class GeradorDePagamento {

	private RepositorioDeLeiloes daoLeilao;
	private Avaliador avaliador;
	private RepositorioDePagamentos daoPagamento;
	private Relogio relogio;

	public GeradorDePagamento(RepositorioDeLeiloes daoLeilao, Avaliador avaliador, RepositorioDePagamentos daoPagamento,
			Relogio relogio) {
		this.daoLeilao = daoLeilao;
		this.avaliador = avaliador;
		this.daoPagamento = daoPagamento;
		this.relogio = relogio;
	}

	public GeradorDePagamento(RepositorioDeLeiloes daoLeilao, Avaliador avaliador,
			RepositorioDePagamentos daoPagamento) {
		this(daoLeilao, avaliador, daoPagamento, new RelogioDoSistema());
	}

	public void gera() {
		List<Leilao> encerrados = daoLeilao.encerrados();

		for (Leilao leilao : encerrados) {
			avaliador.avalia(leilao);

			Pagamento pagamento = new Pagamento(avaliador.getMaiorLance(), primeiroDiaUtil());
			daoPagamento.salva(pagamento);
		}
	}

	private Calendar primeiroDiaUtil() {
		Calendar dataAtual = relogio.hoje();
		int diaDaSemana = dataAtual.get(Calendar.DAY_OF_WEEK);

		if (diaDaSemana == Calendar.SATURDAY) {
			dataAtual.add(Calendar.DAY_OF_MONTH, 2);
		} else if (diaDaSemana == Calendar.SUNDAY) {
			dataAtual.add(Calendar.DAY_OF_MONTH, 1);
		}

		return dataAtual;
	}
}
