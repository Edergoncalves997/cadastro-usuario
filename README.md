# 📚 Biblioteca Digital

Sistema de gerenciamento de biblioteca digital desenvolvido com Spring Boot, oferecendo funcionalidades completas de CRUD para livros e gerenciamento de empréstimos.

## 🚀 Funcionalidades

### 📖 Gestão de Livros
- ✅ Cadastrar novos livros
- ✅ Buscar livros por título, autor, ISBN ou termo geral
- ✅ Listar todos os livros
- ✅ Listar livros disponíveis/indisponíveis
- ✅ Atualizar informações dos livros
- ✅ Excluir livros
- ✅ Marcar livros como disponível/indisponível

### 📋 Gestão de Empréstimos
- ✅ Realizar empréstimos de livros
- ✅ Devolver livros (por ID do empréstimo ou ID do livro)
- ✅ Buscar empréstimos por pessoa (email)
- ✅ Listar empréstimos ativos, devolvidos e atrasados
- ✅ Atualizar informações do empréstimo
- ✅ Excluir empréstimos
- ✅ Controle automático de disponibilidade dos livros

### 📊 Estatísticas
- ✅ Total de livros cadastrados
- ✅ Livros disponíveis/indisponíveis
- ✅ Empréstimos ativos
- ✅ Empréstimos atrasados

### 🌐 Integração com APIs de Livros
- ✅ Busca automática de capas via Open Library API
- ✅ Busca automática de informações via Google Books API
- ✅ Preenchimento automático de dados do livro
- ✅ Exibição de capas na interface
- ✅ Atualização de livros existentes com dados da API

## 🛠️ Tecnologias Utilizadas

- **Java 24**
- **Spring Boot 3.5.5**
- **Spring Data JPA**
- **PostgreSQL** (banco de dados principal)
- **Jackson** (processamento JSON)
- **RestTemplate** (integração com APIs)
- **Lombok**
- **Maven**

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

```
├── Controller (Camada de Apresentação)
│   ├── LivroController
│   ├── EmprestimoController
│   └── BibliotecaController
├── Business (Camada de Negócio)
│   ├── LivroService
│   └── EmprestimoService
└── Infrastructure (Camada de Infraestrutura)
    ├── Entitys
    │   ├── Livro
    │   └── Emprestimo
    └── Repository
        ├── LivroRepository
        └── EmprestimoRepository
```

## 🚀 Como Executar

1. **Pré-requisitos:**
   - Java 24 ou superior
   - Maven 3.6 ou superior

2. **Executar a aplicação:**
   ```bash
   mvn spring-boot:run
   ```

3. **Acessar a aplicação:**
   - API: `http://localhost:8080`
   - Console H2: `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:biblioteca`
     - Username: `sa`
     - Password: (vazio)

## 📡 Endpoints da API

### Livros
- `GET /livros` - Listar todos os livros
- `POST /livros` - Cadastrar novo livro
- `GET /livros/{id}` - Buscar livro por ID
- `GET /livros/isbn/{isbn}` - Buscar livro por ISBN
- `GET /livros/titulo?titulo={titulo}` - Buscar livros por título
- `GET /livros/autor?autor={autor}` - Buscar livros por autor
- `GET /livros/disponiveis` - Listar livros disponíveis
- `GET /livros/indisponiveis` - Listar livros indisponíveis
- `GET /livros/buscar?termo={termo}` - Busca geral
- `PUT /livros/{id}` - Atualizar livro
- `DELETE /livros/{id}` - Excluir livro
- `PUT /livros/{id}/marcar-disponivel` - Marcar como disponível
- `PUT /livros/{id}/marcar-indisponivel` - Marcar como indisponível
- `GET /livros/buscar-por-isbn/{isbn}` - Buscar informações por ISBN via API
- `PUT /livros/{id}/buscar-informacoes` - Atualizar livro com dados da API

### Empréstimos
- `POST /emprestimos/emprestar` - Realizar empréstimo
- `PUT /emprestimos/{id}/devolver` - Devolver livro por ID do empréstimo
- `PUT /emprestimos/devolver-por-livro/{livroId}` - Devolver por ID do livro
- `GET /emprestimos` - Listar todos os empréstimos
- `GET /emprestimos/{id}` - Buscar empréstimo por ID
- `GET /emprestimos/por-email?email={email}` - Buscar por email da pessoa
- `GET /emprestimos/ativos` - Listar empréstimos ativos
- `GET /emprestimos/devolvidos` - Listar empréstimos devolvidos
- `GET /emprestimos/atrasados` - Listar empréstimos atrasados
- `GET /emprestimos/por-livro/{livroId}` - Buscar empréstimos de um livro
- `PUT /emprestimos/{id}` - Atualizar empréstimo
- `DELETE /emprestimos/{id}` - Excluir empréstimo

### Biblioteca
- `GET /biblioteca/estatisticas` - Obter estatísticas gerais
- `GET /biblioteca/status` - Status da aplicação

## 📝 Exemplos de Uso

### Cadastrar um livro:
```bash
curl -X POST http://localhost:8080/livros \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "O Senhor dos Anéis",
    "autor": "J.R.R. Tolkien",
    "isbn": "978-85-359-0277-8",
    "anoPublicacao": 1954,
    "editora": "Martins Fontes",
    "descricao": "Uma das obras mais importantes da literatura fantástica"
  }'
```

### Realizar empréstimo:
```bash
curl -X POST "http://localhost:8080/emprestimos/emprestar" \
  -d "livroId=1" \
  -d "nomePessoa=João Silva" \
  -d "emailPessoa=joao@email.com" \
  -d "telefonePessoa=11999999999" \
  -d "diasEmprestimo=14"
```

### Devolver livro:
```bash
curl -X PUT http://localhost:8080/emprestimos/1/devolver
```

### Buscar informações de livro por ISBN:
```bash
curl -X GET http://localhost:8080/livros/buscar-por-isbn/9788535904289
```

### Atualizar livro com dados da API:
```bash
curl -X PUT http://localhost:8080/livros/1/buscar-informacoes
```

## 🎯 Características Técnicas

- **Validações:** Verificação de ISBN único, disponibilidade de livros
- **Transações:** Controle automático de transações JPA
- **Relacionamentos:** Relacionamento Many-to-One entre Empréstimo e Livro
- **Queries Customizadas:** Consultas JPQL para busca avançada
- **Tratamento de Erros:** Exceções personalizadas com mensagens claras
- **Integração com APIs:** Open Library e Google Books para dados de livros
- **Banco PostgreSQL:** Banco de dados robusto para produção
- **Interface Responsiva:** Frontend moderno com exibição de capas
- **Cache de Dados:** Otimização de consultas com índices

## 🗄️ Configuração do Banco de Dados

### PostgreSQL Setup

Para funcionar corretamente, configure um banco PostgreSQL e execute os seguintes scripts:

#### 1️⃣ **Criação do Banco e Tabelas**

```sql
-- 📚 Criação do banco de dados
CREATE DATABASE biblioteca_digital;

-- 🔗 Conectar ao banco
\c biblioteca_digital;

-- 📖 Tabela de Livros
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

-- 📋 Tabela de Empréstimos
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
```

#### 2️⃣ **Índices para Performance**

```sql
-- 🚀 Criação de índices para melhor performance
CREATE INDEX idx_livro_isbn ON livro(isbn);
CREATE INDEX idx_livro_titulo ON livro(titulo);
CREATE INDEX idx_livro_autor ON livro(autor);
CREATE INDEX idx_emprestimo_livro_id ON emprestimo(livro_id);
CREATE INDEX idx_emprestimo_email ON emprestimo(email_pessoa);
CREATE INDEX idx_emprestimo_status ON emprestimo(status);
CREATE INDEX idx_emprestimo_data_emprestimo ON emprestimo(data_emprestimo);
```

#### 3️⃣ **Dados de Exemplo**

```sql
-- 📚 Inserir livros de exemplo
INSERT INTO livro (titulo, autor, isbn, ano_publicacao, editora, descricao) VALUES
('O Senhor dos Anéis', 'J.R.R. Tolkien', '978-8533613377', 1954, 'Martins Fontes', 'Uma das obras mais importantes da literatura fantástica'),
('1984', 'George Orwell', '978-8535904289', 1949, 'Companhia das Letras', 'Romance distópico sobre controle totalitário'),
('Dom Casmurro', 'Machado de Assis', '978-8535923456', 1899, 'Companhia das Letras', 'Clássico da literatura brasileira');

-- 📝 Comentários nas colunas
COMMENT ON COLUMN livro.url_capa IS 'URL da capa do livro obtida via API externa';
COMMENT ON COLUMN emprestimo.status IS 'Status do empréstimo: ATIVO, DEVOLVIDO, ATRASADO';
```

#### 4️⃣ **Verificação**

```sql
-- ✅ Verificar se as tabelas foram criadas corretamente
\dt

-- 📊 Verificar estrutura das tabelas
\d livro
\d emprestimo
```

### 🔧 **Configuração da Aplicação**

Atualize o `application.properties`:

```properties
# Configuração do banco PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/biblioteca_digital
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=postgres

# Configuração JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Configuração do servidor
server.port=8080
```

## 🌐 Como Usar as APIs de Livros

### 🔍 Busca Automática de Informações

O sistema integra com duas APIs gratuitas para enriquecer os dados dos livros:

#### **1. Open Library API** (Prioritária)
- ✅ Gratuita, sem necessidade de chave
- ✅ Informações detalhadas de livros
- ✅ Capas em alta qualidade

#### **2. Google Books API** (Fallback)
- ✅ Usada quando Open Library não encontra o livro
- ✅ Dados complementares

### 📚 Dados Buscados Automaticamente
- **Título** do livro
- **Autor(es)**
- **Ano de publicação**
- **Editora**
- **Capa do livro** (URL da imagem)
- **Descrição** (quando disponível)

### 🎯 Como Usar na Interface

1. **Buscar informações de um livro existente:**
   - Na lista de livros, clique no botão azul 🔍
   - O sistema buscará automaticamente as informações via API
   - Os dados serão atualizados apenas nos campos vazios

2. **Criar livro com busca por ISBN:**
   - Use o endpoint: `GET /livros/buscar-por-isbn/{isbn}`
   - O sistema retornará um objeto com todos os dados preenchidos

3. **Atualizar livro existente:**
   - Use o endpoint: `PUT /livros/{id}/buscar-informacoes`
   - O sistema atualizará apenas campos vazios ou nulos

### 🔧 Exemplo de Resposta da API

```json
{
  "id": 1,
  "titulo": "1984",
  "autor": "George Orwell",
  "isbn": "9788535904289",
  "anoPublicacao": 1949,
  "editora": "Companhia das Letras",
  "urlCapa": "https://covers.openlibrary.org/b/id/1234567-L.jpg",
  "descricao": "Romance distópico sobre controle totalitário...",
  "disponivel": true
}
```

## 👨‍💻 Desenvolvido por

**Eder Gustav Gonçalves Farias**

---

*Sistema desenvolvido como projeto de estudo das tecnologias Spring Boot e JPA.*
