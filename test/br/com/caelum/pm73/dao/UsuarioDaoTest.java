package br.com.caelum.pm73.dao;

import static org.junit.Assert.*;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dao.CriadorDeSessao;
import br.com.caelum.pm73.dao.UsuarioDao;
import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;

	@Before
	public void antes() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);

		session.beginTransaction();
	}

	@After
	public void depois() {
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveEncontrarPeloNomeEEmail() {
		UsuarioDao dao = new UsuarioDao(session);

		Usuario novoUsuario = new Usuario("João da Silva", "joao@dasilva.com.br");
		dao.salvar(novoUsuario);

		Usuario usuarioDoBanco = dao.porNomeEEmail("João da Silva", "joao@dasilva.com.br");

		assertEquals("João da Silva", usuarioDoBanco.getNome());
		assertEquals("joao@dasilva.com.br", usuarioDoBanco.getEmail());

	}

	@Test
	public void deveRetornarUsuarioNulo_seNaoEstiverNoBanco() {
		UsuarioDao dao = new UsuarioDao(session);

		Usuario usuarioDoBanco = dao.porNomeEEmail("Usuário Inexistente", "");
		assertNull(usuarioDoBanco);
	}

	@Test
	public void deveDeletarUmUsuario() {
		Usuario usuario = new Usuario("Bezerra", "bezerra@email");

		usuarioDao.salvar(usuario);
		usuarioDao.deletar(usuario);

		session.flush();

		Usuario usuarioNoBanco = usuarioDao.porNomeEEmail("Bezerra", "bezerra@email");

		assertNull(usuarioNoBanco);
	}

	@Test
	public void deveAlterarUmUsuario() {
		Usuario usuario = new Usuario("Bezerra", "bezerra@email");

		usuarioDao.salvar(usuario);

		usuario.setNome("João da Silva");
		usuario.setEmail("joao@silva.com.br");

		usuarioDao.atualizar(usuario);

		session.flush();

		Usuario novoUsuario = usuarioDao.porNomeEEmail("João da Silva", "joao@silva.com.br");
		assertNotNull(novoUsuario);
		System.out.println(novoUsuario);

		Usuario usuarioInexistente = usuarioDao.porNomeEEmail("Bezerra", "bezerra@email");
		assertNull(usuarioInexistente);
	}

}
