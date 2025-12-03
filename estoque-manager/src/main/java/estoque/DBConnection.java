package estoque;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Configurações de conexão com o banco de dados PostgreSQL
    // O banco de dados 'estoque_db' e o usuário 'user_estoque' com senha 'password_estoque'
    // foram criados na fase anterior.
    private static final String URL = "jdbc:postgresql://localhost:5432/estoque_db";
    private static final String USER = "user_estoque";
    private static final String PASSWORD = "password_estoque";

    /**
     * Estabelece e retorna uma nova conexão com o banco de dados.
     * @return Objeto Connection.
     * @throws SQLException Se ocorrer um erro de conexão ou o driver não for encontrado.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Carrega o driver JDBC do PostgreSQL
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Lança uma exceção mais informativa se o driver não for encontrado
            throw new SQLException("PostgreSQL JDBC Driver not found. Certifique-se de que a dependência está no pom.xml.", e);
        }
    }

    /**
     * Fecha a conexão com o banco de dados de forma segura.
     * @param conn A conexão a ser fechada.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }
}
