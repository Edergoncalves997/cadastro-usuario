-- =============================================
-- SISTEMA DE BIBLIOTECA DIGITAL
-- Script de Configuração do Banco PostgreSQL
-- =============================================

-- 1. Criação do banco de dados
CREATE DATABASE biblioteca_digital;

-- 2. Conectar ao banco
\c biblioteca_digital;

-- 3. Criação da tabela livro
CREATE TABLE livro (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) UNIQUE NOT NULL,
    ano_publicacao INTEGER,
    editora VARCHAR(255),
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descricao VARCHAR(500),
    url_capa VARCHAR(500)
);

-- 4. Criação da tabela emprestimo
CREATE TABLE emprestimo (
    id SERIAL PRIMARY KEY,
    livro_id INTEGER NOT NULL,
    nome_pessoa VARCHAR(255) NOT NULL,
    email_pessoa VARCHAR(255) NOT NULL,
    telefone_pessoa VARCHAR(255),
    data_emprestimo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_prevista_devolucao TIMESTAMP,
    data_devolucao TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    observacoes TEXT,
    FOREIGN KEY (livro_id) REFERENCES livro(id) ON DELETE CASCADE
);

-- 5. Criação de índices para melhor performance
CREATE INDEX idx_livro_isbn ON livro(isbn);
CREATE INDEX idx_livro_titulo ON livro(titulo);
CREATE INDEX idx_livro_autor ON livro(autor);
CREATE INDEX idx_emprestimo_livro_id ON emprestimo(livro_id);
CREATE INDEX idx_emprestimo_email ON emprestimo(email_pessoa);
CREATE INDEX idx_emprestimo_status ON emprestimo(status);
CREATE INDEX idx_emprestimo_data_emprestimo ON emprestimo(data_emprestimo);

-- 6. Inserir dados de exemplo
INSERT INTO livro (titulo, autor, isbn, ano_publicacao, editora, descricao) VALUES
('O Senhor dos Anéis', 'J.R.R. Tolkien', '978-8533613377', 1954, 'Martins Fontes', 'Uma das obras mais importantes da literatura fantástica'),
('1984', 'George Orwell', '978-8535904289', 1949, 'Companhia das Letras', 'Romance distópico sobre controle totalitário'),
('Dom Casmurro', 'Machado de Assis', '978-8535923456', 1899, 'Companhia das Letras', 'Clássico da literatura brasileira');

-- 7. Verificar se as tabelas foram criadas corretamente
\dt

-- 8. Verificar dados inseridos
SELECT * FROM livro;
