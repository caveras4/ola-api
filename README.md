# ola-api

API REST construída com Spring Boot como Projeto 1 do meu estudo de Java/Spring: uma **Todo List API**
com persistência em PostgreSQL. Os endpoints `/ola` são mantidos como exercício da Semana 1 (criação
do projeto do zero e mapeamento de rotas); os endpoints `/tasks` são o CRUD em construção.

## Tecnologias

- Java 21
- Spring Boot 4.1.0 (Spring Web MVC + Spring Data JPA)
- PostgreSQL 18
- Maven (via wrapper `./mvnw`, não precisa de instalação)

## Pré-requisitos

- Um PostgreSQL rodando em `localhost:5432` com a base `tasksdb` criada.
- As credenciais de conexão estão em `src/main/resources/application.properties`. A senha versionada
  ali é apenas um **placeholder** — **nunca** commite a senha real. Passe a sua por variável de
  ambiente na hora de executar (ver abaixo).

## Como executar

Na raiz do projeto, sobrescrevendo a senha por fora (exemplo no PowerShell):

```powershell
$env:SPRING_DATASOURCE_PASSWORD='SUA_SENHA_REAL'; .\mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. O Spring Boot dá prioridade à variável de ambiente
sobre o valor do `application.properties`, então a senha real nunca precisa entrar no arquivo.

## Endpoints

### Saudação (Semana 1)

| Método | Rota          | Resposta      |
|--------|---------------|---------------|
| GET    | `/ola`        | `Olá, Mundo!` |
| GET    | `/ola/{nome}` | `Olá, {nome}` |

### Tarefas (Semana 2–3)

| Método | Rota            | Sucesso            | Erro |
|--------|-----------------|--------------------|------|
| GET    | `/tasks`        | 200 (lista)        | —    |
| GET    | `/tasks/{id}`   | 200                | 404  |
| POST   | `/tasks`        | 201 + `Location`   | —    |
| PUT    | `/tasks/{id}`   | *(em desenvolvimento)* |  |
| DELETE | `/tasks/{id}`   | *(em desenvolvimento)* |  |

### Exemplos

```bash
# Criar uma tarefa — retorna 201 com o header Location apontando para o recurso novo
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"titulo":"estudar spring","descricao":"semana 3","concluido":false}'

# Buscar por id — 200 se existir, 404 se não
curl -i http://localhost:8080/tasks/1

# Listar todas
curl http://localhost:8080/tasks
```

## Autor

Caio Veras
