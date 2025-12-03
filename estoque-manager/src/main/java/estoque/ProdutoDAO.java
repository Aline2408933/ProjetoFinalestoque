package estoque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    /**
     * Adiciona um novo produto ao banco de dados.
     * @param produto O produto a ser salvo. O ID será gerado pelo banco de dados.
     * @return O produto com o ID gerado.
     * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
     */
    public Produto salvarProduto(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (nome, preco, quantidade) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            // Retorna as chaves geradas (o ID)
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, produto.getNome());
            stmt.setDouble(2, produto.getPreco());
            stmt.setInt(3, produto.getQuantidade());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao salvar produto, nenhuma linha afetada.");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                // Define o ID gerado no objeto Produto
                produto.setId(rs.getInt(1));
            } else {
                throw new SQLException("Falha ao salvar produto, nenhum ID gerado.");
            }
            return produto;
        } finally {
            // Fechamento seguro dos recursos
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Retorna uma lista de todos os produtos do banco de dados.
     * @return Lista de objetos Produto.
     * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
     */
    public List<Produto> listarProdutos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT id, nome, preco, quantidade FROM produto ORDER BY id";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");
                int quantidade = rs.getInt("quantidade");
                produtos.add(new Produto(id, nome, preco, quantidade));
            }
        } finally {
            // Fechamento seguro dos recursos
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            DBConnection.closeConnection(conn);
        }
        return produtos;
    }

    /**
     * Atualiza as informações de um produto existente no banco de dados com base no ID.
     * @param produto O produto com as informações atualizadas.
     * @return true se a atualização foi bem-sucedida, false caso contrário.
     * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
     */
    public boolean atualizarProduto(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, preco = ?, quantidade = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, produto.getNome());
            stmt.setDouble(2, produto.getPreco());
            stmt.setInt(3, produto.getQuantidade());
            stmt.setInt(4, produto.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } finally {
            // Fechamento seguro dos recursos
            if (stmt != null) stmt.close();
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Remove um produto do banco de dados com base no ID.
     * @param id O ID do produto a ser excluído.
     * @return true se a exclusão foi bem-sucedida, false caso contrário.
     * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
     */
    public boolean excluirProduto(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } finally {
            // Fechamento seguro dos recursos
            if (stmt != null) stmt.close();
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Retorna o próximo ID disponível para um novo produto.
     * Como o ID é SERIAL no PostgreSQL, o banco de dados gerencia isso.
     * Este método é mais para fins de demonstração ou para obter o ID máximo atual.
     * @return O próximo ID (ID máximo atual + 1).
     * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
     */
    public int carregarProximoId() throws SQLException {
        String sql = "SELECT MAX(id) FROM produto";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                // Se não houver produtos, MAX(id) será 0, então retorna 1.
                return rs.getInt(1) + 1;
            }
            return 1; // Começa em 1 se a tabela estiver vazia
        } finally {
            // Fechamento seguro dos recursos
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            DBConnection.closeConnection(conn);
        }
    }
}
