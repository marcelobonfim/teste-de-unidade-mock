package br.com.caelum.leilao.infra.dao;

import br.com.caelum.leilao.dominio.Pagamento;

public interface RepositorioDePagamentos {
	
	public void salva(Pagamento pagamento);

}
