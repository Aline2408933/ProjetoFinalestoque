package estoque;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class MainApp extends Application {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final TableView<Produto> tableView = new TableView<>();
    private final ObservableList<Produto> produtoData = FXCollections.observableArrayList();

    // Campos de entrada
    private final TextField idField = new TextField();
    private final TextField nomeField = new TextField();
    private final TextField precoField = new TextField();
    private final TextField quantidadeField = new TextField();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gerenciamento de Estoque - Eletrônicos");

        // Configuração da TableView
        setupTableView();

        // Configuração do formulário de entrada
        GridPane formGrid = setupFormGrid();

        // Configuração dos botões
        HBox buttonBox = setupButtons();

        // Layout principal
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(formGrid, buttonBox, tableView);

        // Carregar dados iniciais
        loadProdutos();

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupTableView() {
        // Coluna ID
        TableColumn<Produto, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        // Coluna Nome
        TableColumn<Produto, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        nomeCol.setPrefWidth(200);

        // Coluna Preço
        TableColumn<Produto, Double> precoCol = new TableColumn<>("Preço");
        precoCol.setCellValueFactory(new PropertyValueFactory<>("preco"));
        precoCol.setPrefWidth(100);

        // Coluna Quantidade
        TableColumn<Produto, Integer> quantidadeCol = new TableColumn<>("Quantidade");
        quantidadeCol.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        quantidadeCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, nomeCol, precoCol, quantidadeCol);
        tableView.setItems(produtoData);

        // Listener para preencher os campos ao selecionar um item
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProdutoDetails(newValue));
    }

    private GridPane setupFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        // ID (apenas leitura, usado para atualização/exclusão)
        idField.setDisable(true);
        idField.setPromptText("ID (Automático)");
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);

        // Nome
        nomeField.setPromptText("Nome do Produto");
        grid.add(new Label("Nome:"), 0, 1);
        grid.add(nomeField, 1, 1);

        // Preço
        precoField.setPromptText("Preço (ex: 1200.50)");
        grid.add(new Label("Preço:"), 2, 1);
        grid.add(precoField, 3, 1);

        // Quantidade
        quantidadeField.setPromptText("Quantidade em Estoque");
        grid.add(new Label("Quantidade:"), 0, 2);
        grid.add(quantidadeField, 1, 2);

        return grid;
    }

    private HBox setupButtons() {
        Button addButton = new Button("Adicionar Produto");
        addButton.setOnAction(e -> handleAddProduto());

        Button updateButton = new Button("Atualizar Produto");
        updateButton.setOnAction(e -> handleUpdateProduto());

        Button deleteButton = new Button("Excluir Produto");
        deleteButton.setOnAction(e -> handleDeleteProduto());

        Button clearButton = new Button("Limpar Campos");
        clearButton.setOnAction(e -> clearFields());

        HBox box = new HBox(10);
        box.getChildren().addAll(addButton, updateButton, deleteButton, clearButton);
        return box;
    }

    private void loadProdutos() {
        try {
            List<Produto> produtos = produtoDAO.listarProdutos();
            produtoData.clear();
            produtoData.addAll(produtos);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar os produtos: " + e.getMessage());
        }
    }

    private void handleAddProduto() {
        try {
            Produto novoProduto = getProdutoFromFields(true); // true para novo produto (ID é ignorado)
            if (novoProduto == null) return;

            produtoDAO.salvarProduto(novoProduto);
            loadProdutos();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Produto adicionado com ID: " + novoProduto.getId());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Preço e Quantidade devem ser números válidos.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private void handleUpdateProduto() {
        if (idField.getText().isEmpty() || idField.getText().equals("ID (Automático)")) {
            showAlert(Alert.AlertType.WARNING, "Seleção Necessária", "Selecione um produto na tabela para atualizar.");
            return;
        }

        try {
            Produto produtoAtualizado = getProdutoFromFields(false); // false para produto existente (ID é necessário)
            if (produtoAtualizado == null) return;

            boolean sucesso = produtoDAO.atualizarProduto(produtoAtualizado);
            if (sucesso) {
                loadProdutos();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Produto atualizado com sucesso.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Produto com ID " + produtoAtualizado.getId() + " não encontrado para atualização.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Preço e Quantidade devem ser números válidos.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Erro ao atualizar produto: " + e.getMessage());
        }
    }

    private void handleDeleteProduto() {
        if (idField.getText().isEmpty() || idField.getText().equals("ID (Automático)")) {
            showAlert(Alert.AlertType.WARNING, "Seleção Necessária", "Selecione um produto na tabela para excluir.");
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            boolean sucesso = produtoDAO.excluirProduto(id);

            if (sucesso) {
                loadProdutos();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Produto excluído com sucesso.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Produto com ID " + id + " não encontrado para exclusão.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "ID inválido.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Erro ao excluir produto: " + e.getMessage());
        }
    }

    private Produto getProdutoFromFields(boolean isNew) throws NumberFormatException {
        String nome = nomeField.getText().trim();
        String precoText = precoField.getText().trim();
        String quantidadeText = quantidadeField.getText().trim();

        if (nome.isEmpty() || precoText.isEmpty() || quantidadeText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Todos os campos (Nome, Preço, Quantidade) são obrigatórios.");
            return null;
        }

        double preco = Double.parseDouble(precoText);
        int quantidade = Integer.parseInt(quantidadeText);

        if (isNew) {
            // ID será gerado pelo banco de dados (passamos 0 ou -1, mas o construtor exige int)
            return new Produto(0, nome, preco, quantidade);
        } else {
            int id = Integer.parseInt(idField.getText());
            return new Produto(id, nome, preco, quantidade);
        }
    }

    private void showProdutoDetails(Produto produto) {
        if (produto != null) {
            idField.setText(String.valueOf(produto.getId()));
            nomeField.setText(produto.getNome());
            precoField.setText(String.format("%.2f", produto.getPreco()));
            quantidadeField.setText(String.valueOf(produto.getQuantidade()));
        } else {
            clearFields();
        }
    }

    private void clearFields() {
        idField.setText("ID (Automático)");
        nomeField.clear();
        precoField.clear();
        quantidadeField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
