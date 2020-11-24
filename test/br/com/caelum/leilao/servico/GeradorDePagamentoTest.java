package br.com.caelum.leilao.servico;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.relogio.Relogio;

public class GeradorDePagamentoTest {

	@Test
	public void deveGerarPagamentoParaUmLeilaoEncerrado() {
		RepositorioDeLeiloes daoLeilao = mock(RepositorioDeLeiloes.class);
		Avaliador avaliador = mock(Avaliador.class);
		RepositorioDePagamentos daoPagamento = mock(RepositorioDePagamentos.class);

		Leilao leilao = new CriadorDeLeilao().para("TV Plasma").lance(new Usuario("Marcelo"), 2000)
				.lance(new Usuario("Fernanda"), 2500).constroi();

		when(daoLeilao.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(2500.0);

		GeradorDePagamento gerador = new GeradorDePagamento(daoLeilao, avaliador, daoPagamento);
		gerador.gera();

		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(daoPagamento).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);

	}

	@Test
	public void deveGerarPagamentoParaMaiorLanceDoLeilaoEncerrado() {
		RepositorioDeLeiloes daoLeilao = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos daoPagamento = mock(RepositorioDePagamentos.class);

		Leilao leilao = new CriadorDeLeilao().para("TV Plasma").lance(new Usuario("Marcelo"), 2000)
				.lance(new Usuario("Fernanda"), 2500).constroi();

		when(daoLeilao.encerrados()).thenReturn(Arrays.asList(leilao));

		Avaliador avaliador = new Avaliador();
		GeradorDePagamento gerador = new GeradorDePagamento(daoLeilao, avaliador, daoPagamento);
		gerador.gera();

		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(daoPagamento).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);

	}

	@Test
	public void deveEmpurrarParaOProximoDiaUtil() {
		RepositorioDeLeiloes daoLeilao = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos daoPagamento = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);

		// dia 7/abril/2012 eh um sabado
		Calendar sabado = Calendar.getInstance();
		sabado.set(2012, Calendar.APRIL, 7);

		// ensinamos o mock a dizer que "hoje" é sabado!
		when(relogio.hoje()).thenReturn(sabado);

		Leilao leilao = new CriadorDeLeilao().para("TV Plasma").lance(new Usuario("Marcelo"), 2000)
				.lance(new Usuario("Fernanda"), 2500).constroi();

		when(daoLeilao.encerrados()).thenReturn(Arrays.asList(leilao));

		Avaliador avaliador = new Avaliador();
		GeradorDePagamento gerador = new GeradorDePagamento(daoLeilao, avaliador, daoPagamento, relogio);
		gerador.gera();

		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(daoPagamento).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}
	
	@Test
	public void deveEmpurrarParaOProximoDiaUtilQuandoForDomingo() {
		RepositorioDeLeiloes daoLeilao = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos daoPagamento = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);

		// dia 8/abril/2012 eh um domingo
		Calendar domingo = Calendar.getInstance();
		domingo.set(2012, Calendar.APRIL, 8);

		// ensinamos o mock a dizer que "hoje" é domingo!
		when(relogio.hoje()).thenReturn(domingo);

		Leilao leilao = new CriadorDeLeilao().para("TV Plasma").lance(new Usuario("Marcelo"), 2000)
				.lance(new Usuario("Fernanda"), 2500).constroi();

		when(daoLeilao.encerrados()).thenReturn(Arrays.asList(leilao));

		Avaliador avaliador = new Avaliador();
		GeradorDePagamento gerador = new GeradorDePagamento(daoLeilao, avaliador, daoPagamento, relogio);
		gerador.gera();

		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(daoPagamento).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}
}
