	CREATE DATABASE GerenciadorDeEstoque;

	USE GerenciadorDeEstoque;

	-- Tabela Categorias
	CREATE TABLE Categorias (
		IDCategoria INT PRIMARY KEY AUTO_INCREMENT,
		NomeCategoria VARCHAR(60) NOT NULL,
		DescricaoCategoria TEXT NOT NULL
	);

	-- Tabela Produtos
	CREATE TABLE Produtos (
		IDProduto INT PRIMARY KEY AUTO_INCREMENT,
		NomeProduto VARCHAR(60) NOT NULL,
		DescricaoProduto TEXT,
		QuantidadeEstoque INT DEFAULT 0,
		PrecoCompra FLOAT NOT NULL,
		PrecoVenda FLOAT NOT NULL,
		CategoriaProduto INT,
		CONSTRAINT fk_categoria FOREIGN KEY (CategoriaProduto) REFERENCES Categorias(IDCategoria)
	);

	-- Tabela Movimentações de Estoque
	CREATE TABLE MovimentacoesEstoque (
		IDMovimentacao INT PRIMARY KEY AUTO_INCREMENT,
		IDProduto INT NOT NULL,
		TipoMovimentacao ENUM('Entrada', 'Saída') NOT NULL,
		Quantidade INT NOT NULL,
		DataMovimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		FOREIGN KEY (IDProduto) REFERENCES Produtos(IDProduto)
	);

	DELIMITER //

	-- Procedure para Cadastro de Categoria
	CREATE PROCEDURE CadastroCategoria(
		IN NomeCategoria VARCHAR(60), 
		IN DescricaoCategoria TEXT
	)
	BEGIN 
		INSERT INTO Categorias (NomeCategoria, DescricaoCategoria)
		VALUES (NomeCategoria, DescricaoCategoria);
	END //

	DELIMITER ;

	DELIMITER //

	-- Procedure para Cadastro de Produto
	CREATE PROCEDURE CadastroProduto(
		IN NomeProduto VARCHAR(60), 
		IN DescricaoProduto TEXT,
		IN QuantidadeEstoque INT,
		IN PrecoCompra FLOAT, 
		IN PrecoVenda FLOAT, 
		IN CategoriaProduto INT
	)
	BEGIN
		INSERT INTO Produtos 
		(NomeProduto, DescricaoProduto, QuantidadeEstoque, PrecoCompra, PrecoVenda, CategoriaProduto)
		VALUES 
		(NomeProduto, DescricaoProduto, QuantidadeEstoque, PrecoCompra, PrecoVenda, CategoriaProduto);
	END //

	DELIMITER ;

	DELIMITER //
	-- Procedure para Deletar Produto
	CREATE PROCEDURE DeletarProduto(
		IN IDProduto INT
	)
	BEGIN
		DELETE FROM Produtos WHERE IDProduto = IDProduto;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Deletar Categoria
	CREATE PROCEDURE DeletarCategoria(
		IN IDCategoria INT
	)
	BEGIN
		DELETE FROM Categorias WHERE IDCategoria = IDCategoria;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Editar Produto
	CREATE PROCEDURE EditarProduto(
		IN IDProduto INT,                 
		IN NovoNomeProduto VARCHAR(60),  
		IN NovaDescricaoProduto TEXT,    
		IN NovaQuantidadeEstoque INT,   
		IN NovoPrecoCompra FLOAT,        
		IN NovoPrecoVenda FLOAT,         
		IN NovaCategoriaProduto INT      
	)
	BEGIN
		UPDATE Produtos
		SET 
			NomeProduto = COALESCE(NovoNomeProduto, NomeProduto),
			DescricaoProduto = COALESCE(NovaDescricaoProduto, DescricaoProduto),
			QuantidadeEstoque = COALESCE(NovaQuantidadeEstoque, QuantidadeEstoque),
			PrecoCompra = COALESCE(NovoPrecoCompra, PrecoCompra),
			PrecoVenda = COALESCE(NovoPrecoVenda, PrecoVenda),
			CategoriaProduto = COALESCE(NovaCategoriaProduto, CategoriaProduto)
		WHERE IDProduto = IDProduto;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Editar Categoria
	CREATE PROCEDURE EditarCategoria(
		IN IDCategoria INT, 
		IN NovoNomeCategoria VARCHAR(60), 
		IN NovaDescricaoCategoria TEXT
	)
	BEGIN 
		UPDATE Categorias
		SET 
			NomeCategoria = COALESCE(NovoNomeCategoria, NomeCategoria),
			DescricaoCategoria = COALESCE(NovaDescricaoCategoria, DescricaoCategoria)
		WHERE IDCategoria = IDCategoria;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Registrar Movimentação de Estoque
	CREATE PROCEDURE RegistrarMovimentacaoEstoque(
		IN ProdutoID INT,
		IN TipoMovimentacao ENUM('Entrada', 'Saída'),
		IN Quantidade INT
	)
	BEGIN
		IF TipoMovimentacao = 'Entrada' THEN
			UPDATE Produtos
			SET QuantidadeEstoque = QuantidadeEstoque + Quantidade
			WHERE IDProduto = ProdutoID;
		ELSEIF TipoMovimentacao = 'Saída' THEN
			IF (SELECT QuantidadeEstoque FROM Produtos WHERE IDProduto = ProdutoID) < Quantidade THEN
				SIGNAL SQLSTATE '45000'
				SET MESSAGE_TEXT = 'Estoque insuficiente para a saída!';
			ELSE
				UPDATE Produtos
				SET QuantidadeEstoque = QuantidadeEstoque - Quantidade
				WHERE IDProduto = ProdutoID;
			END IF;
		END IF;

		INSERT INTO MovimentacoesEstoque (IDProduto, TipoMovimentacao, Quantidade)
		VALUES (ProdutoID, TipoMovimentacao, Quantidade);
	END //
	DELIMITER ;
	DELIMITER //
	-- Trigger para verificar estoque baixo
	CREATE TRIGGER VerificarEstoqueBaixo 
	AFTER UPDATE ON Produtos
	FOR EACH ROW
	BEGIN
		IF NEW.QuantidadeEstoque < 5 THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'Estoque abaixo do nível mínimo!';
		END IF;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Consultar Produtos
	CREATE PROCEDURE ConsultarProdutos(
		IN NomeProduto VARCHAR(60),     
		IN IDCategoria INT,             
		IN PrecoMinimo FLOAT,           
		IN PrecoMaximo FLOAT            
	)
	BEGIN
		SELECT 
			p.IDProduto,
			p.NomeProduto,
			p.DescricaoProduto,
			p.QuantidadeEstoque,
			p.PrecoCompra,
			p.PrecoVenda,
			c.NomeCategoria
		FROM Produtos p
		LEFT JOIN Categorias c ON p.CategoriaProduto = c.IDCategoria
		WHERE 
			(NomeProduto IS NULL OR p.NomeProduto LIKE CONCAT('%', NomeProduto, '%')) AND
			(IDCategoria IS NULL OR p.CategoriaProduto = IDCategoria) AND
			(PrecoMinimo IS NULL OR p.PrecoVenda >= PrecoMinimo) AND
			(PrecoMaximo IS NULL OR p.PrecoVenda <= PrecoMaximo);
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Consultar Categorias
	CREATE PROCEDURE ConsultarCategorias(
		IN IDCategoria INT,           
		IN NomeCategoria VARCHAR(60)   
	)
	BEGIN
		SELECT 
			c.IDCategoria,
			c.NomeCategoria,
			c.DescricaoCategoria,
			COUNT(p.IDProduto) AS TotalProdutos
		FROM Categorias c
		LEFT JOIN Produtos p ON p.CategoriaProduto = c.IDCategoria
		WHERE 
			(IDCategoria IS NULL OR c.IDCategoria = IDCategoria) AND
			(NomeCategoria IS NULL OR c.NomeCategoria LIKE CONCAT('%', NomeCategoria, '%'))
		GROUP BY 
			c.IDCategoria;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Relatório de Produtos Cadastrados
	CREATE PROCEDURE RelatorioProdutosCadastrados()
	BEGIN
		SELECT 
			p.IDProduto,
			p.NomeProduto,
			p.DescricaoProduto,
			p.QuantidadeEstoque,
			p.PrecoCompra,
			p.PrecoVenda,
			c.NomeCategoria
		FROM Produtos p
		LEFT JOIN Categorias c ON p.CategoriaProduto = c.IDCategoria;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Relatório de Movimentações de Estoque
	CREATE PROCEDURE RelatorioMovimentacoesEstoque()
	BEGIN
		SELECT 
			m.IDMovimentacao,
			p.NomeProduto,
			m.TipoMovimentacao,
			m.Quantidade,
			m.DataMovimentacao
		FROM MovimentacoesEstoque m
		JOIN Produtos p ON m.IDProduto = p.IDProduto
		ORDER BY m.DataMovimentacao DESC;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Relatório de Produtos com Baixo Estoque
	CREATE PROCEDURE RelatorioProdutosBaixoEstoque(IN EstoqueMinimo INT)
	BEGIN
		SELECT 
			p.IDProduto,
			p.NomeProduto,
			p.QuantidadeEstoque,
			c.NomeCategoria
		FROM Produtos p
		LEFT JOIN Categorias c ON p.CategoriaProduto = c.IDCategoria
		WHERE p.QuantidadeEstoque < EstoqueMinimo;
	END //
	DELIMITER ;
	DELIMITER //
	-- Procedure para Relatório de Vendas e Lucro
	CREATE PROCEDURE RelatorioVendasLucro()
	BEGIN
		SELECT 
			p.NomeProduto,
			SUM(m.Quantidade * p.PrecoVenda) AS TotalVendas,
			SUM(m.Quantidade * (p.PrecoVenda - p.PrecoCompra)) AS Lucro
		FROM MovimentacoesEstoque m
		JOIN Produtos p ON m.IDProduto = p.IDProduto
		WHERE m.TipoMovimentacao = 'Saída'
		GROUP BY p.IDProduto;
	END //
	DELIMITER ;
