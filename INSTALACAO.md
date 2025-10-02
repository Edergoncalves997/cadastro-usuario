# 📚 Sistema de Biblioteca Digital - Guia de Instalação

## 🚀 Pré-requisitos

- **Java 17+** (recomendado Java 21)
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Git**

## 📋 Passos de Instalação

### 1. Clone o Repositório
```bash
git clone <URL_DO_REPOSITORIO>
cd cadastro-usuario
```

### 2. Configure o Banco de Dados

#### 2.1 Instale o PostgreSQL
- **Windows**: Baixe do site oficial do PostgreSQL
- **macOS**: `brew install postgresql`
- **Linux**: `sudo apt-get install postgresql postgresql-contrib`

#### 2.2 Crie o Banco de Dados
```bash
# Conecte ao PostgreSQL
psql -U postgres

# Execute o script de configuração
\i database_setup.sql
```

#### 2.3 Configure as Credenciais
Edite o arquivo `src/main/resources/application.properties`:
```properties
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### 3. Execute a Aplicação

#### 3.1 Compile o Projeto
```bash
./mvnw clean compile
```

#### 3.2 Execute a Aplicação
```bash
./mvnw spring-boot:run
```

#### 3.3 Acesse a Interface
Abra seu navegador em: `http://localhost:8080`

## 🔧 Configurações Avançadas

### Porta Personalizada
Para alterar a porta, edite `application.properties`:
```properties
server.port=8081
```

### Banco de Dados Personalizado
Para usar outro banco, edite `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/seu_banco
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

## 🐛 Solução de Problemas

### Erro de Conexão com Banco
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `application.properties`
- Teste a conexão: `psql -U postgres -d biblioteca_digital`

### Porta em Uso
- Mude a porta no `application.properties`
- Ou mate o processo: `lsof -i :8080` e `kill <PID>`

### Problemas de Compilação
- Verifique a versão do Java: `java -version`
- Limpe o cache: `./mvnw clean`
- Recompile: `./mvnw clean compile`

## 📱 Funcionalidades

- ✅ Gestão completa de livros
- ✅ Sistema de empréstimos
- ✅ Integração com APIs de livros (Google Books, Open Library)
- ✅ Exibição de capas de livros
- ✅ Interface responsiva
- ✅ Busca e filtros
- ✅ Relatórios de empréstimos

## 🆘 Suporte

Se encontrar problemas:
1. Verifique os logs da aplicação
2. Confirme se todos os pré-requisitos estão instalados
3. Teste a conexão com o banco de dados
4. Consulte a documentação do Spring Boot

## 📄 Licença

Este projeto é de código aberto e está disponível sob a licença MIT.
