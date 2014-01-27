package br.com.caelum.pm73;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoBuilder {

	private Usuario donoteste;
	private double valor;
	private String nome;
	private boolean usado;
	private Calendar dataAbertura;
	private boolean encerrado;
	private List<Lance> lances;

	public LeilaoBuilder() {
		this.dono = new Usuario("Joao da Silva", "joao@silva.com.br");
		this.valor = 1500.0;
		this.nome = "XBox";
		this.usado = false;
		this.dataAbertura = Calendar.getInstance();
		this.lances = new ArrayList<Lance>();
	}

	public LeilaoBuilder comDono(Usuario dono) {
		this.dono = dono;
		return this;
	}

	public LeilaoBuilder comValor(double valor) {
		this.valor = valor;
		return this;
	}

	public LeilaoBuilder comNome(String nome) {
		this.nome = nome;
		return this;
	}

	public LeilaoBuilder usado() {
		this.usado = true;
		return this;
	}

	public LeilaoBuilder encerrado() {
		this.encerrado = true;
		return this;
	}

	public LeilaoBuilder diasAtras(int dias) {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, -dias);

		this.dataAbertura = data;

		return this;
	}


	public LeilaoBuilder comLance(Calendar data, Usuario usuario, double valor) {
		Lance lance = new Lance(data, usuario, valor, null);
		lances.add(lance);
		return this;
	}
	
	public Leilao constroi() {
		Leilao leilao = new Leilao(nome, valor, dono, usado);
		
		leilao.setDataAbertura(dataAbertura);
		if (encerrado)
			leilao.encerra();
		
		if (lances.size() != 0) {
			for (Lance lance : lances) {
				lance.setLeilao(leilao);
				leilao.adicionaLance(lance);
			}
		}

		return leilao;
	}

}
