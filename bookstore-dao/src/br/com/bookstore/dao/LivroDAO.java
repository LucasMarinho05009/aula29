package br.com.bookstore.dao;

import br.com.bookstore.model.Livro;

import java.util.List;
import java.util.Optional;

/**
 * Contrato (interface) de acesso a dados de Livro.
 *
 * A camada de negócio depende apenas DESTA interface (Inversão de Dependência, o "D" do SOLID)
 * e não conhece JDBC: dá para trocar SQLite por PostgreSQL ou por um Mock em memória
 * sem alterar nenhuma linha da regra de negócio.
 */
public interface LivroDAO {

    /** Grava o livro e preenche o id gerado no próprio objeto. */
    void salvar(Livro livro);

    /** Busca pelo id; devolve Optional vazio (e não null) quando o livro não existe. */
    Optional<Livro> buscarPorId(Long id);

    /** Lista todos os livros gravados. */
    List<Livro> listarTodos();
}
