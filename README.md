# ola-api

API REST construída com Spring Boot como Projeto 1 do meu estudo de Java/Spring: uma **Todo List API**
com persistência em PostgreSQL. Os endpoints `/ola` são mantidos como exercício da Semana 1 (criação
do projeto do zero e mapeamento de rotas); os endpoints `/tasks` são o CRUD completo.

## Tecnologias

- Java 21
- Spring Boot 4.1.0 (Spring Web MVC + Spring Data JPA)
- PostgreSQL 18
- Maven (via wrapper `./mvnw`, não precisa de instalação)

## Pré-requisitos

- Um PostgreSQL rodando em `localhost:5432` com a base `tasksdb` criada.
- As credenciais de conexão estão em `src/main/resources/application.properties`, que declara a senha
  como `${DB_PASSWORD:postgres}` — um placeholder com valor padrão. **Nunca** commite a senha real:
  informe a sua pela variável de ambiente `DB_PASSWORD` na hora de executar (ver abaixo).

## Como executar

Na raiz do projeto, definindo a senha por fora (exemplo no PowerShell):

```powershell
$env:DB_PASSWORD='SUA_SENHA_REAL'; .\mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Se `DB_PASSWORD` não estiver definida, o Spring cai no
valor padrão `postgres` — então a senha real nunca precisa entrar no arquivo versionado.

## Endpoints

### Saudação (Semana 1)

| Método | Rota          | Resposta      |
|--------|---------------|---------------|
| GET    | `/ola`        | `Olá, Mundo!` |
| GET    | `/ola/{nome}` | `Olá, {nome}` |

### Tarefas (Semana 2–3)

| Método | Rota          | Sucesso              | Erro |
|--------|---------------|----------------------|------|
| GET    | `/tasks`      | 200 (lista)          | —    |
| GET    | `/tasks/{id}` | 200                  | 404  |
| POST   | `/tasks`      | 201 + `Location`     | —    |
| PUT    | `/tasks/{id}` | 200 (recurso salvo)  | 404  |
| DELETE | `/tasks/{id}` | 204 (sem corpo)      | 404  |

O `PUT` é uma **substituição integral** do recurso, como manda o verbo: campo ausente no corpo da
requisição é gravado como vazio, não preservado. Para alterar um título sem perder a descrição,
envie o objeto completo.

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

# Substituir a tarefa 1 — 200 com o recurso salvo, 404 se o id não existir.
# Note que o corpo traz todos os campos: o que for omitido aqui é apagado.
curl -i -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"titulo":"estudar spring data","descricao":"semana 3","concluido":true}'

# Remover a tarefa 1 — 204 sem corpo, 404 se o id não existir
curl -i -X DELETE http://localhost:8080/tasks/1
```

## Autor

Caio Veras
