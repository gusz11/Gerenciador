package br.com.bd.main;

import java.sql.*;
import java.util.Scanner;

public class SistemaEstoque {
    private static final String URL = "jdbc:mysql://localhost:3306/gerenciadordeestoque";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                exibirMenu();
                int opcao = scanner.nextInt();
                scanner.nextLine();

                switch (opcao) {
                    case 1 -> cadastrarCategoria(conn, scanner);
                    case 2 -> cadastrarProduto(conn, scanner);
                    case 3 -> registrarMovimentacao(conn, scanner);
                    case 4 -> consultarProdutos(conn, scanner);
                    case 5 -> consultarCategorias(conn, scanner);
                    case 6 -> relatorioProdutosCadastrados(conn);
                    case 7 -> relatorioMovimentacoes(conn);
                    case 8 -> {
                        System.out.println("Encerrando...");
                        return;
                    }
                    default -> System.out.println("Opção inválida!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void exibirMenu() {
        System.out.println("\nGerenciador de Estoque");
        System.out.println("1. Cadastrar Categoria");
        System.out.println("2. Cadastrar Produto");
        System.out.println("3. Registrar Movimentação");
        System.out.println("4. Consultar Produtos");
        System.out.println("5. Consultar Categorias");
        System.out.println("6. Relatório de Produtos Cadastrados");
        System.out.println("7. Relatório de Movimentações");
        System.out.println("8. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void cadastrarCategoria(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Nome da Categoria: ");
        String nome = scanner.nextLine();
        System.out.print("Descrição da Categoria: ");
        String descricao = scanner.nextLine();

        String sql = "CALL CadastroCategoria(?, ?)";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, descricao);
            stmt.execute();
            System.out.println("Categoria cadastrada com sucesso!");
        }
    }

    private static void cadastrarProduto(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Nome do Produto: ");
        String nome = scanner.nextLine();
        System.out.print("Descrição do Produto: ");
        String descricao = scanner.nextLine();
        System.out.print("Quantidade em Estoque: ");
        int quantidade = scanner.nextInt();
        System.out.print("Preço de Compra: ");
        float precoCompra = scanner.nextFloat();
        System.out.print("Preço de Venda: ");
        float precoVenda = scanner.nextFloat();
        System.out.print("ID da Categoria: ");
        int categoria = scanner.nextInt();

        String sql = "CALL CadastroProduto(?, ?, ?, ?, ?, ?)";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, descricao);
            stmt.setInt(3, quantidade);
            stmt.setFloat(4, precoCompra);
            stmt.setFloat(5, precoVenda);
            stmt.setInt(6, categoria);
            stmt.execute();
            System.out.println("Produto cadastrado com sucesso!");
        }
    }

    private static void registrarMovimentacao(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("ID do Produto: ");
        int idProduto = scanner.nextInt();
        scanner.nextLine(); // Consumir nova linha
        System.out.print("Tipo de Movimentação (Entrada/Saída): ");
        String tipo = scanner.nextLine();
        System.out.print("Quantidade: ");
        int quantidade = scanner.nextInt();

        String sql = "CALL RegistrarMovimentacaoEstoque(?, ?, ?)";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idProduto);
            stmt.setString(2, tipo);
            stmt.setInt(3, quantidade);
            stmt.execute();
            System.out.println("Movimentação registrada com sucesso!");
        }
    }

    private static void consultarProdutos(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Nome do Produto (ou deixe vazio): ");
        String nome = scanner.nextLine();
        System.out.print("ID da Categoria (ou 0 para ignorar): ");
        int idCategoria = scanner.nextInt();
        System.out.print("Preço Mínimo (ou 0 para ignorar): ");
        float precoMinimo = scanner.nextFloat();
        System.out.print("Preço Máximo (ou 0 para ignorar): ");
        float precoMaximo = scanner.nextFloat();

        String sql = "CALL ConsultarProdutos(?, ?, ?, ?)";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, nome.isEmpty() ? null : nome);
            stmt.setObject(2, idCategoria == 0 ? null : idCategoria);
            stmt.setObject(3, precoMinimo == 0 ? null : precoMinimo);
            stmt.setObject(4, precoMaximo == 0 ? null : precoMaximo);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nProdutos Encontrados:");
                while (rs.next()) {
                    System.out.printf("ID: %d | Nome: %s | Quantidade: %d | Preço de Venda: %.2f | Categoria: %s%n",
                            rs.getInt("IDProduto"), rs.getString("NomeProduto"), rs.getInt("QuantidadeEstoque"),
                            rs.getFloat("PrecoVenda"), rs.getString("NomeCategoria"));
                }
            }
        }
    }

    private static void consultarCategorias(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("ID da Categoria (ou 0 para ignorar): ");
        int idCategoria = scanner.nextInt();
        scanner.nextLine(); // Consumir nova linha
        System.out.print("Nome da Categoria (ou deixe vazio): ");
        String nome = scanner.nextLine();

        String sql = "CALL ConsultarCategorias(?, ?)";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setObject(1, idCategoria == 0 ? null : idCategoria);
            stmt.setString(2, nome.isEmpty() ? null : nome);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nCategorias Encontradas:");
                while (rs.next()) {
                    System.out.printf("ID: %d | Nome: %s | Total de Produtos: %d%n",
                            rs.getInt("IDCategoria"), rs.getString("NomeCategoria"), rs.getInt("TotalProdutos"));
                }
            }
        }
    }

    private static void relatorioProdutosCadastrados(Connection conn) throws SQLException {
        String sql = "CALL RelatorioProdutosCadastrados()";
        try (CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nRelatório de Produtos:");
            while (rs.next()) {
                System.out.printf("ID: %d | Nome: %s | Estoque: %d | Preço: %.2f%n",
                        rs.getInt("IDProduto"), rs.getString("NomeProduto"), rs.getInt("QuantidadeEstoque"),
                        rs.getFloat("PrecoVenda"));
            }
        }
    }

    private static void relatorioMovimentacoes(Connection conn) throws SQLException {
        String sql = "CALL RelatorioMovimentacoesEstoque()";
        try (CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nRelatório de Movimentações:");
            while (rs.next()) {
                System.out.printf("Movimentação: %d | Produto: %s | Tipo: %s | Quantidade: %d | Data: %s%n",
                        rs.getInt("IDMovimentacao"), rs.getString("NomeProduto"), rs.getString("TipoMovimentacao"),
                        rs.getInt("Quantidade"), rs.getTimestamp("DataMovimentacao"));
            }
        }
    }
}