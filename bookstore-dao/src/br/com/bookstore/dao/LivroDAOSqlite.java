package br.com.bookstore.dao;

import br.com.bookstore.factory.ConexaoFactory;
import br.com.bookstore.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do contrato LivroDAO usando JDBC e SQLite (infraestrutura).
 *
 * Padrões aplicados:
 * - try-with-resources em todo método: Connection, PreparedStatement e ResultSet são fechados
 *   automaticamente, sem vazamento de recursos.
 * - PreparedStatement com parâmetros (?): sem risco de SQL Injection.
 * - mapearLinha centraliza a conversão ResultSet -> Livro (sem duplicação de código).
 */
public class LivroDAOSqlite implements LivroDAO {

    /**
     * Inicialização determinística: ao criar o DAO, garante que a tabela livros exista.
     * O IF NOT EXISTS torna a operação segura de repetir a cada execução.
     */
    public LivroDAOSqlite() {
        String sql = "CREATE TABLE IF NOT EXISTS livros ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "titulo TEXT NOT NULL, "
                + "autor TEXT NOT NULL, "
                + "preco REAL NOT NULL);";

        try (Connection conn = ConexaoFactory.obterConexao();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a tabela livros: " + e.getMessage(), e);
        }
    }

    /**
     * CREATE: grava o livro e resgata a chave primária gerada pelo SQLite (AUTOINCREMENT).
     * Statement.RETURN_GENERATED_KEYS pede ao driver o id criado, que vai para o objeto via setId.
     */
    @Override
    public void salvar(Livro livro) {
        String sql = "INSERT INTO livros (titulo, autor, preco) VALUES (?, ?, ?);";
        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Parâmetros tipados; o índice do JDBC começa em 1.
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setDouble(3, livro.getPreco());

            // Executa o INSERT no banco.
            stmt.executeUpdate();

            // Resgata o id gerado pelo banco e sincroniza com o objeto em memória.
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    livro.setId(chaves.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar livro: " + e.getMessage(), e);
        }
    }

    /**
     * READ pontual: devolve Optional.of(livro) se achou, ou Optional.empty() se o id não existe.
     * O Optional obriga quem chama a tratar a ausência e elimina o NullPointerException acidental.
     */
    @Override
    public Optional<Livro> buscarPorId(Long id) {
        // Guarda: setLong(1, null) estouraria NullPointerException; id nulo simplesmente não existe.
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, titulo, autor, preco FROM livros WHERE id = ?;";
        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                // O id é chave primária: no máximo uma linha, então basta um if (e não while).
                if (rs.next()) {
                    return Optional.of(mapearLinha(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar livro por ID: " + e.getMessage(), e);
        }
    }

    /**
     * READ geral: percorre o ResultSet com while (rs.next()) e monta a lista de livros.
     * A ordem por id mantém a sequência de cadastro.
     */
    @Override
    public List<Livro> listarTodos() {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT id, titulo, autor, preco FROM livros ORDER BY id;";

        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                livros.add(mapearLinha(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros: " + e.getMessage(), e);
        }

        return livros;
    }

    /**
     * Mapeamento Objeto-Relacional centralizado: transforma a linha atual do ResultSet
     * em um objeto Livro, usando o construtor completo (hidratação).
     */
    private Livro mapearLinha(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String titulo = rs.getString("titulo");
        String autor = rs.getString("autor");
        double preco = rs.getDouble("preco");
        return new Livro(id, titulo, autor, preco);
    }
}
