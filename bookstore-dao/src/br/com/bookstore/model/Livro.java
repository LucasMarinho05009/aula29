package br.com.bookstore.model;

/**
 * CAMADA MODEL (Modelo de Domínio): representa um livro do catálogo da BookStore.
 *
 * A classe não conhece JDBC nem SQL (SRP): só guarda dados e protege as regras do domínio.
 * As invariantes são validadas no construtor (Fail-Fast), então é impossível existir
 * um Livro inválido na memória.
 */
public class Livro {

    private Long id;
    private final String titulo;
    private final String autor;
    private final double preco;

    /**
     * Construtor Completo: usado para HIDRATAR (reconstruir) um livro a partir de uma linha do banco,
     * já com o id gerado pelo SQLite.
     */
    public Livro(Long id, String titulo, String autor, double preco) {
        // Barreira Fail-Fast: valida tudo antes de qualquer atribuição.
        if (titulo == null || titulo.trim().length() < 2) {
            throw new IllegalArgumentException("O título do livro deve ter ao menos 2 caracteres.");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("O autor do livro não pode ser nulo ou vazio.");
        }
        // "!(preco > 0)" recusa zero, negativos e também NaN (que "preco <= 0" deixaria passar).
        if (!(preco > 0.0)) {
            throw new IllegalArgumentException("O preço de venda deve ser estritamente positivo.");
        }

        this.id = id;
        this.titulo = titulo.trim();
        this.autor = autor.trim();
        this.preco = preco;
    }

    /**
     * Construtor de Negócio: usado ao criar um livro novo, que ainda não foi gravado.
     * O id começa nulo e só é preenchido depois do INSERT, com setId(...).
     * Delega ao construtor completo para reaproveitar as mesmas validações.
     */
    public Livro(String titulo, String autor, double preco) {
        this(null, titulo, autor, preco);
    }

    public Long getId() {
        return id;
    }

    /**
     * Único setter permitido: sincroniza no objeto a chave primária gerada pelo banco.
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public double getPreco() {
        return preco;
    }

    @Override
    public String toString() {
        return String.format("Livro [id=%s, titulo=%s, autor=%s, preco=R$ %.2f]",
                id, titulo, autor, preco);
    }
}
