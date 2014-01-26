package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;

	@Before
	public void antes() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);

		session.beginTransaction();
	}

	@Test
	public void deveRetornarUmLeilaoAtivo() {
		Usuario bruno = new Usuario("Bruno", "bruno@email");

		Leilao ativo = new LeilaoBuilder().comDono(bruno).constroi();
		Leilao inativo = new LeilaoBuilder().comDono(bruno).encerrado().constroi();

		usuarioDao.salvar(bruno);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(inativo);

		long total = leilaoDao.total();

		assertEquals(1L, total);
	}

	@Test
	public void deveRetornarZeroLeilaoAtivo() {
		Usuario bruno = new Usuario("Bruno", "bruno@email");

		Leilao ativo = new LeilaoBuilder().comDono(bruno).encerrado().constroi();
		Leilao inativo = new LeilaoBuilder().comDono(bruno).encerrado().constroi();

		usuarioDao.salvar(bruno);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(inativo);

		long total = leilaoDao.total();

		assertEquals(0, total);
	}

	@Test
	public void deveRetornarLeilaoNaoUsado() {
		Usuario bruno = new Usuario("Bruno", "bruno@email");

		Leilao ativo = new LeilaoBuilder().comDono(bruno).usado().constroi();
		Leilao naoUsado = new LeilaoBuilder().comDono(bruno).constroi();

		usuarioDao.salvar(bruno);
		leilaoDao.salvar(naoUsado);
		leilaoDao.salvar(ativo);

		List<Leilao> novos = leilaoDao.novos();

		assertEquals(novos.get(0), naoUsado);
	}

	@Test
	public void deveRetornarLeiloesCriadosAMaisDeSeteDias() {
		Usuario bruno = new Usuario("Bruno", "bruno@email");
		usuarioDao.salvar(bruno);

		Leilao antigo = new LeilaoBuilder().comDono(bruno).diasAtras(10).constroi();

		Leilao novo = new LeilaoBuilder().comDono(bruno).constroi();

		leilaoDao.salvar(novo);
		leilaoDao.salvar(antigo);

		List<Leilao> antigos = leilaoDao.antigos();

		assertEquals(1, antigos.size());
		assertEquals(antigos.get(0), antigo);
	}

	@Test
	public void deveTrazerSomenteLeiloesDeSeteDiasAtras() {
		Usuario bruno = new Usuario("Bruno", "bruno@email");
		usuarioDao.salvar(bruno);

		Leilao noLimite = new LeilaoBuilder().comDono(bruno).diasAtras(7).constroi();

		usuarioDao.salvar(bruno);
		leilaoDao.salvar(noLimite);

		List<Leilao> antigos = leilaoDao.antigos();

		assertEquals(1, antigos.size());
	}

	@Test
	public void deveRetornarLeiloesNaoEncerradosNoPeriodo() {

		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fimDoIntervalo = Calendar.getInstance();

		Usuario bruno = new Usuario("Bruno", "bruno@email");

		Leilao leilao1 = new LeilaoBuilder().comDono(bruno).diasAtras(2).comNome("carro").constroi();

		Leilao leilao2 = new LeilaoBuilder().comDono(bruno).diasAtras(20).constroi();

		usuarioDao.salvar(bruno);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

		assertEquals(1, leiloes.size());
		assertEquals("carro", leiloes.get(0).getNome());
	}

	@Test
	public void naoDeveRetornarLeiloesEncerradosNoPeriodo() {

		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fimDoIntervalo = Calendar.getInstance();

		Usuario bruno = new Usuario("Bruno", "bruno@email");

		Leilao leilao1 = new LeilaoBuilder().comDono(bruno).diasAtras(2).encerrado().constroi();

		Leilao leilao2 = new LeilaoBuilder().comDono(bruno).diasAtras(4).encerrado().constroi();

		usuarioDao.salvar(bruno);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

		assertEquals(0, leiloes.size());
	}

	@Test
	public void deveRetornarLeiloesComPeloMenosTresLances() {
		Usuario bruno = new Usuario("Bruno", "bruno@email");
		Usuario bezerra = new Usuario("Bezerra", "bezerra@email");

		Leilao leilao1 = new LeilaoBuilder().comDono(bezerra).comValor(3000.0)
				.comLance(Calendar.getInstance(), bruno, 3000.0).comLance(Calendar.getInstance(), bezerra, 3100.0)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder().comDono(bruno).comValor(3200.0)
				.comLance(Calendar.getInstance(), bruno, 3000.0).comLance(Calendar.getInstance(), bezerra, 3100.0)
				.comLance(Calendar.getInstance(), bruno, 3200.0).comLance(Calendar.getInstance(), bezerra, 3300.0)
				.comLance(Calendar.getInstance(), bruno, 3400.0).comLance(Calendar.getInstance(), bezerra, 3500.0)
				.constroi();

		usuarioDao.salvar(bezerra);
		usuarioDao.salvar(bruno);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

		assertEquals(1, leiloes.size());
		assertEquals(3200.0, leiloes.get(0).getValorInicial(), 0.00001);
	}

	@Test
	public void listaSomenteOsLeiloesDoUsuario() throws Exception {
		Usuario dono = new Usuario("Mauricio", "m@a.com");
		Usuario comprador = new Usuario("Victor", "v@v.com");
		Usuario comprador2 = new Usuario("Guilherme", "g@g.com");
		Leilao leilao = new LeilaoBuilder().comDono(dono).comValor(50.0)
				.comLance(Calendar.getInstance(), comprador, 100.0).comLance(Calendar.getInstance(), comprador2, 200.0)
				.constroi();
		Leilao leilao2 = new LeilaoBuilder().comDono(dono).comValor(250.0)
				.comLance(Calendar.getInstance(), comprador2, 100.0).constroi();
		usuarioDao.salvar(dono);
		usuarioDao.salvar(comprador);
		usuarioDao.salvar(comprador2);
		leilaoDao.salvar(leilao);
		leilaoDao.salvar(leilao2);

		List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
		assertEquals(1, leiloes.size());
		assertEquals(leilao, leiloes.get(0));
	}
	
	@Test
	public void deveDeletarUmLeilao() {
		Usuario bruno = new Usuario("Bezerra", "bezerra@email");
		Leilao leilao = new LeilaoBuilder().comDono(bruno).constroi();

		usuarioDao.salvar(bruno);
		
		leilaoDao.salvar(leilao);
		leilaoDao.deleta(leilao);
		
		usuarioDao.deletar(bruno);

		session.flush();

		assertNull(leilaoDao.porId(leilao.getId()));
	}

	@After
	public void depois() {
		session.getTransaction().rollback();
		session.close();
	}

}
