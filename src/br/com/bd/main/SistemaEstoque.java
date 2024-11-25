package br.com.bd.main;

import java.sql.*; //importa classes para conexão e manipulação do banco de dados
import java.util.Scanner; //importa a classe Scanner para capturar entrada do usuário

//classe principal do sistema
public class SistemaEstoque {
    //constantes para conexão ao banco de dados.
    private static final String URL = "jdbc:mysql://localhost:3306/gerenciadordeestoque";
    private static final String USER = "root"; //usuário do banco
    private static final String PASSWORD = ""; //senha do banco

    public static void main(String[] args) {
        //abre uma conexão com o banco de dados usando try-with-resources
        //garante que a conexão será fechada automaticamente ao final
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);  //instancia o Scanner para capturar entradas do usuário

            while (true) { //loop infinito para manter o sistema ativo até o usuário optar por sair
                exibirMenu(); //exibe o menu de opções
                int opcao = scanner.nextInt();  //captura a opção escolhida pelo usuário
                scanner.nextLine(); //consome a nova linha (necessário após nextInt)

                switch (opcao) { //avalia a opção escolhida pelo usuário
                    case 1 -> cadastrarCategoria(conn, scanner); //chama o método para cadastrar uma categoria
                    case 2 -> cadastrarProduto(conn, scanner); //chama o método para cadastrar um produto
                    case 3 -> registrarMovimentacao(conn, scanner); //chama o método para registrar uma movimentação no estoque
                    case 4 -> consultarProdutos(conn, scanner); //chama o método para consultar produtos
                    case 5 -> consultarCategorias(conn, scanner); //chama o método para consultar categorias
                    case 6 -> relatorioProdutosCadastrados(conn); //gera relatório de produtos cadastrados
                    case 7 -> relatorioMovimentacoes(conn); //gera relatório de movimentações no estoque
                    case 8 -> {
                        System.out.println("Encerrando..."); //encerra o programa
                        return;
                    }
                    default -> System.out.println("Opção inválida!");
                }
            }
        } catch (SQLException e) { //trata erros relacionados ao banco de dados
            e.printStackTrace(); //exibe o stack trace do erro
        }
    }

    private static void exibirMenu() { //exibe o menu de opções para o usuário
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
    //cadastra uma nova categoria no banco de dados
    private static void cadastrarCategoria(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Nome da Categoria: ");
        String nome = scanner.nextLine(); //captura o nome da categoria
        System.out.print("Descrição da Categoria: ");
        String descricao = scanner.nextLine(); //captura a descrição da categoria


        String sql = "CALL CadastroCategoria(?, ?)"; //chama a stored procedure CadastroCategoria para inserir dados
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, nome); //define o primeiro parâmetro da procedure
            stmt.setString(2, descricao); //define o segundo parâmetro da procedure
            stmt.execute(); //executa a procedure no banco de dados
            System.out.println("Categoria cadastrada com sucesso!");
        }
    }
    //cadastra um novo produto no banco de dados
    private static void cadastrarProduto(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Nome do Produto: ");
        String nome = scanner.nextLine(); //captura o nome do produto
        System.out.print("Descrição do Produto: ");
        String descricao = scanner.nextLine(); //captura a descrição do produto
        System.out.print("Quantidade em Estoque: ");
        int quantidade = scanner.nextInt(); //captura a quantidade inicial do produto
        System.out.print("Preço de Compra: ");
        float precoCompra = scanner.nextFloat(); //captura o preço de compra
        System.out.print("Preço de Venda: ");
        float precoVenda = scanner.nextFloat(); //captura o preço de venda
        System.out.print("ID da Categoria: ");
        int categoria = scanner.nextInt(); //captura o ID da categoria associada

        String sql = "CALL CadastroProduto(?, ?, ?, ?, ?, ?)"; //chama a stored procedure CadastroProduto
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, descricao);
            stmt.setInt(3, quantidade);
            stmt.setFloat(4, precoCompra);
            stmt.setFloat(5, precoVenda);
            stmt.setInt(6, categoria); //define os parâmetros da procedure
            stmt.execute(); //executa a procedure no banco de dados
            System.out.println("Produto cadastrado com sucesso!");
        }
    }
    //registra uma movimentação no estoque
    private static void registrarMovimentacao(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("ID do Produto: ");
        int idProduto = scanner.nextInt(); //captura o ID do produto
        scanner.nextLine(); //consumir nova linha
        System.out.print("Tipo de Movimentação (Entrada/Saída): ");
        String tipo = scanner.nextLine(); //captura o tipo de movimentação
        System.out.print("Quantidade: ");
        int quantidade = scanner.nextInt(); //captura a quantidade da movimentação

        String sql = "CALL RegistrarMovimentacaoEstoque(?, ?, ?)"; //chama a stored procedure RegistrarMovimentacaoEstoque
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idProduto);
            stmt.setString(2, tipo);
            stmt.setInt(3, quantidade); //define os parâmetros da procedure
            stmt.execute(); //executa a procedure no banco de dados
            System.out.println("Movimentação registrada com sucesso!");
        }
    }
    //método para consultar produtos com base em critérios fornecidos pelo usuário
    private static void consultarProdutos(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Nome do Produto (ou deixe vazio): ");
        String nome = scanner.nextLine(); //captura o nome do produto, permitindo consultas parciais. Se vazio, será ignorado
        System.out.print("ID da Categoria (ou 0 para ignorar): ");
        int idCategoria = scanner.nextInt(); //captura o ID da categoria. Se 0, será ignorado
        System.out.print("Preço Mínimo (ou 0 para ignorar): ");
        float precoMinimo = scanner.nextFloat(); //captura o preço mínimo desejado para os produtos
        System.out.print("Preço Máximo (ou 0 para ignorar): ");
        float precoMaximo = scanner.nextFloat(); //captura o preço máximo desejado para os produtos

        String sql = "CALL ConsultarProdutos(?, ?, ?, ?)"; //prepara uma chamada para a stored procedure ConsultarProdutos no banco
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, nome.isEmpty() ? null : nome); //se o nome estiver vazio, passa null, indicando que este filtro será ignorado
            stmt.setObject(2, idCategoria == 0 ? null : idCategoria); //se o ID da categoria for 0, passa null, ignorando este filtro
            stmt.setObject(3, precoMinimo == 0 ? null : precoMinimo); //se o preço mínimo for 0, passa null, ignorando este filtro
            stmt.setObject(4, precoMaximo == 0 ? null : precoMaximo); //se o preço máximo for 0, passa null, ignorando este filtro

            try (ResultSet rs = stmt.executeQuery()) { //executa a query e processa o conjunto de resultados
                System.out.println("\nProdutos Encontrados:");
                while (rs.next()) { //itera pelos resultados e exibe informações dos produtos encontrados
                    System.out.printf("ID: %d | Nome: %s | Quantidade: %d | Preço de Venda: %.2f | Categoria: %s%n",
                            rs.getInt("IDProduto"), rs.getString("NomeProduto"), rs.getInt("QuantidadeEstoque"),
                            rs.getFloat("PrecoVenda"), rs.getString("NomeCategoria"));
                } // ID do produto. /nome do produto. /quantidade disponível no estoque. /preço de venda do produto. /nome da categoria associada.
            }
        }
    }

    private static void consultarCategorias(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("ID da Categoria (ou 0 para ignorar): ");
        int idCategoria = scanner.nextInt();
        scanner.nextLine(); //consumir nova linha para evitar conflitos com próximas entradas. /captura o ID da categoria. Se for 0, este critério será ignorado
        System.out.print("Nome da Categoria (ou deixe vazio): ");
        String nome = scanner.nextLine(); //captura o nome da categoria. se vazio, este critério será ignorado

        String sql = "CALL ConsultarCategorias(?, ?)"; //chama a stored procedure ConsultarCategorias no banco
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setObject(1, idCategoria == 0 ? null : idCategoria); //define o parâmetro ID da categoria como null se for 0, ignorando o critério
            stmt.setString(2, nome.isEmpty() ? null : nome); //define o parâmetro Nome da categoria como null se estiver vazio, ignorando o critério

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nCategorias Encontradas:");
                while (rs.next()) { //itera pelos resultados e exibe informações das categorias encontradas
                    System.out.printf("ID: %d | Nome: %s | Total de Produtos: %d%n",
                            rs.getInt("IDCategoria"), rs.getString("NomeCategoria"), rs.getInt("TotalProdutos"));
                } //ID da categoria. /nome da categoria. /total de produtos associados à categoria
            }
        }
    }

    private static void relatorioProdutosCadastrados(Connection conn) throws SQLException {
        String sql = "CALL RelatorioProdutosCadastrados()"; // Chama a stored procedure RelatorioProdutosCadastrados.
        try (CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) { // Executa a query e processa os resultados.
            System.out.println("\nRelatório de Produtos:");
            while (rs.next()) { // Itera pelos resultados e exibe informações dos produtos cadastrados.
                System.out.printf("ID: %d | Nome: %s | Estoque: %d | Preço: %.2f%n",
                        rs.getInt("IDProduto"), rs.getString("NomeProduto"), rs.getInt("QuantidadeEstoque"),
                        rs.getFloat("PrecoVenda"));
            } // ID do produto  /nome do produto /quantidade em estoque /preço de venda do produto
        }
    }

    private static void relatorioMovimentacoes(Connection conn) throws SQLException {
        String sql = "CALL RelatorioMovimentacoesEstoque()"; // Chama a stored procedure RelatorioMovimentacoesEstoque.
        try (CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) { // Executa a query e processa os resultados.
            System.out.println("\nRelatório de Movimentações:");
            while (rs.next()) {  // Itera pelos resultados e exibe informações das movimentações registradas.
                System.out.printf("Movimentação: %d | Produto: %s | Tipo: %s | Quantidade: %d | Data: %s%n",
                        rs.getInt("IDMovimentacao"), rs.getString("NomeProduto"), rs.getString("TipoMovimentacao"),
                        rs.getInt("Quantidade"), rs.getTimestamp("DataMovimentacao"));
            } // ID da movimentação /nome do produto associado /tipo da movimentação (Entrada ou Saída) /quantidade movimentada /data e hora da movimentação
        }
    }
}