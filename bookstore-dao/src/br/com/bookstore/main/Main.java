package br.com.bookstore.main;

import br.com.bookstore.dao.LivroDAO;
import br.com.bookstore.dao.LivroDAOSqlite;
import br.com.bookstore.model.Livro;

import java.util.List;

/**
 * Roteiro canônico de testes do catálogo BookStore.
 */
public class Main {

    public static void main(String[] args) {

        // 1. DIP: a variável é do tipo da INTERFACE; só aqui se escolhe a implementação (SQLite).
        LivroDAO dao = new LivroDAOSqlite();

        // 2. Persiste 3 obras e mostra o ID gerado pelo banco para cada uma.
        System.out.println("=== 1) Cadastrando 3 livros ===");
        Livro l1 = new Livro("Clean Code", "Robert C. Martin", 89.90);
        Livro l2 = new Livro("Design Patterns", "Erich Gamma et al.", 129.50);
        Livro l3 = new Livro("Refactoring", "Martin Fowler", 109.00);
        dao.salvar(l1);
        dao.salvar(l2);
        dao.salvar(l3);
        System.out.println("ID gerado para '" + l1.getTitulo() + "': " + l1.getId());
        System.out.println("ID gerado para '" + l2.getTitulo() + "': " + l2.getId());
        System.out.println("ID gerado para '" + l3.getTitulo() + "': " + l3.getId());

        // 3. Consulta o livro de ID 2 de forma funcional: ifPresentOrElse trata os dois caminhos.
        System.out.println("\n=== 2) Buscando o livro de ID 2 ===");
        dao.buscarPorId(2L).ifPresentOrElse(
                livro -> System.out.println("Encontrado: " + livro),
                () -> System.out.println("Aviso: nenhum livro com ID 2."));

        // 4. ID inexistente: o Optional vazio evita o NullPointerException.
        System.out.println("\n=== 3) Buscando um ID inexistente (999) ===");
        dao.buscarPorId(999L).ifPresentOrElse(
                livro -> System.out.println("Encontrado: " + livro),
                () -> System.out.println("Aviso: nenhum livro encontrado com ID 999 (sem NullPointerException)."));

        // 5. Lista todos os registros com cabeçalho tabular.
        System.out.println("\n=== 4) Listando todos os livros ===");
        List<Livro> livros = dao.listarTodos();
        System.out.println(String.format("%-4s | %-20s | %-22s | %10s", "ID", "TÍTULO", "AUTOR", "PREÇO"));
        System.out.println("-----+----------------------+------------------------+-----------");
        for (Livro livro : livros) {
            System.out.println(String.format("%-4d | %-20s | %-22s | R$ %7.2f",
                    livro.getId(), livro.getTitulo(), livro.getAutor(), livro.getPreco()));
        }
        System.out.println("Total de livros: " + livros.size());
    }
}
