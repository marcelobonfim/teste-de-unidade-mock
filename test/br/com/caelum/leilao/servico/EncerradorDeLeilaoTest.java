package br.com.caelum.leilao.servico;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.email.EnviadorDeEmail;

public class EncerradorDeLeilaoTest {
	
	private RepositorioDeLeiloes daoFalso;
	private EnviadorDeEmail carteiroFalso;
	private Calendar dataAntiga;

	@Before
	public void setUp() {
		dataAntiga = Calendar.getInstance();
		dataAntiga.set(1999, 1, 20);
		daoFalso = mock(RepositorioDeLeiloes.class);
		carteiroFalso = mock(EnviadorDeEmail.class);
	}
	
	@Test
	public void deveEncerrarLeiloesQueComecaramAMaisDeUmaSemana() {
		Leilao leilao1 = new CriadorDeLeilao().para("Note Book").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Home").naData(dataAntiga).constroi();
		
		List<Leilao> leiloes = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloes);
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
		assertEquals(2, encerradorDeLeilao.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		
	}
	
	@Test
	public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
		Calendar ontem = Calendar.getInstance();
		 ontem.add(Calendar.DAY_OF_MONTH, -1);
		
		Leilao leilao1 = new CriadorDeLeilao().para("Note Book").naData(ontem).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Home").naData(ontem).constroi();
		
		List<Leilao> leiloes = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloes);
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
		assertEquals(0, encerradorDeLeilao.getTotalEncerrados());
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());
		
		verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
		
	}
	
	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {
		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
		assertEquals(0, encerradorDeLeilao.getTotalEncerrados());		
	}
	
	@Test
	public void deveVerificarSeAtualizouLeilao() {
		Leilao leilao1 = new CriadorDeLeilao().para("Note Book").naData(dataAntiga).constroi();
		
		List<Leilao> leiloes = Arrays.asList(leilao1);
		
		when(daoFalso.correntes()).thenReturn(leiloes);
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
		verify(daoFalso,times(1)).atualiza(leilao1);
		
	}
	
	@Test
    public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(dataAntiga).constroi();

        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerrador = 
            new EncerradorDeLeilao(daoFalso, carteiroFalso);

        encerrador.encerra();

        InOrder inOrder = inOrder(daoFalso, carteiroFalso);
        inOrder.verify(daoFalso, times(1)).atualiza(leilao1);    
        inOrder.verify(carteiroFalso, times(1)).envia(leilao1);    
    }
	
	
	@Test
	public void deveContinuarMesmoQuandoExcecaoForLancada() {
		Leilao leilao1 = new CriadorDeLeilao().para("Note Book").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Home").naData(dataAntiga).constroi();
		
		List<Leilao> leiloes = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloes);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
		verify(daoFalso).atualiza(leilao2);
        verify(carteiroFalso).envia(leilao2);
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoEnviadorDeEmailFalha() {
		Leilao leilao1 = new CriadorDeLeilao().para("Note Book").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Home").naData(dataAntiga).constroi();
		
		List<Leilao> leiloes = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloes);
		doThrow(new RuntimeException()).when(carteiroFalso).envia(any(Leilao.class));
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
		verify(daoFalso).atualiza(leilao2);
        verify(carteiroFalso).envia(leilao2);
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoEnviadorDeEmailNaoForChamado() {
		Leilao leilao1 = new CriadorDeLeilao().para("Note Book").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Home").naData(dataAntiga).constroi();
		
		List<Leilao> leiloes = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloes);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();
		
        verify(carteiroFalso, never()).envia(any(Leilao.class));
	}

}
