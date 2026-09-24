# ola-api

API REST de tarefas (**Todo List**) em Spring Boot com PostgreSQL, Projeto 1 do meu estudo de
Java/Spring. `/tasks` é o CRUD; `/ola` ficou da Semana 1.

## Tecnologias

Java 21 · Spring Boot 4.1.0 (Web MVC, Data JPA, Bean Validation) · PostgreSQL 18 · Maven (wrapper `./mvnw`)

## Como executar

Pré-requisito: PostgreSQL em `localhost:5432` com a base `tasksdb` criada.

A senha do banco vem da variável de ambiente `DB_PASSWORD`. O `application.properties` só declara
`${DB_PASSWORD:postgres}`, então a senha real nunca entra no repositório. No PowerShell:

```powershell
$env:DB_PASSWORD='SUA_SENHA_REAL'; .\mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints

| Método | Rota          | Sucesso             | Erro      |
|--------|---------------|---------------------|-----------|
| GET    | `/ola`        | `Olá, Mundo!`       | —         |
| GET    | `/ola/{nome}` | `Olá, {nome}`       | —         |
| GET    | `/tasks`      | 200 (lista)         | —         |
| GET    | `/tasks/{id}` | 200                 | 404       |
| POST   | `/tasks`      | 201 + `Location`    | 400       |
| PUT    | `/tasks/{id}` | 200 (recurso salvo) | 400 / 404 |
| DELETE | `/tasks/{id}` | 204 (sem corpo)     | 404       |

Corpo do `POST` e do `PUT`:

- `titulo`: obrigatório, não pode ser em branco, até 100 caracteres
- `descricao`: opcional, até 255 caracteres
- `concluido`: obrigatório

O `PUT` substitui o recurso inteiro, então envie o objeto completo. Erro de validação volta **400** com
um mapa `{"campo": "mensagem"}`. A validação roda antes do controller, por isso um `PUT /tasks/999` com
título em branco dá 400, não 404.

```bash
# Criar — 201 com o header Location apontando para o recurso novo
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"titulo":"estudar spring","descricao":"semana 3","concluido":false}'

# Título em branco — 400 com {"titulo":"não deve estar em branco"}
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"titulo":"   ","descricao":"x","concluido":false}'
```

## Decisões

### 1. A resposta é `TaskResponse`, não a entidade `Task`

O formato de `Task` é ditado pelo banco (nome do campo = nome da coluna). O de `TaskResponse` é o
contrato com quem consome a API. Testei: renomear `concluido` para `feito` no `TaskResponse` mudou só o
JSON; na entidade, exigiu `@Column(name = "concluido")` e quebrou a compilação do `fromEntity`. E com o
DTO só sai o que eu escolho: um campo como senha nunca vaza por acidente.

### 2. A entrada é `TaskRequest`, não a entidade `Task`

Recebendo `Task` no `@RequestBody`, um `POST` com `"id": 6` no corpo sobrescreveu a linha 6 e ainda
respondeu `201 Created`. Isso é **mass assignment**. O `TaskRequest` é um record sem `id`: o Jackson
monta o objeto pelo construtor, a chave `id` não tem onde caber e é descartada. Quem decide `INSERT` ou
`UPDATE` é o `save()`, olhando o `id` da entidade: nulo vira `INSERT`. O `201` aparecia nos dois casos
porque está fixo no controller, que nunca perguntou ao banco o que aconteceu.

### 3. O `PUT` não usa `toEntity()`

Testei: com `toEntity()`, o `PUT /tasks/3` criou uma linha nova e respondeu 200 com outro `id`. O
`toEntity()` não conhece a URL, então a entidade sai com `id` nulo e o `save()` faz `INSERT`. No código
final, o `PUT` parte do `findById(id)`: a entidade já vem do banco com `id = 3`, os setters trocam os
outros três campos e o `save()` faz `UPDATE`. É o mesmo `findById` que permite devolver 404.

### 4. `concluido` é `boolean`, não `Boolean`

O erro não some nem aparece: ele anda de camada. Com `boolean`, um corpo sem `concluido` é recusado pelo
Jackson antes do controller, com **400**. Com `Boolean`, o record nasce com `null` dentro, e o erro só
estoura no `setConcluido(boolean)` da entidade, ao desempacotar o `null`: `NullPointerException` e
**500**. A culpa é do cliente nos dois casos, mas só o primitivo responde isso.

### 5. Validar na entrada, e não deixar para o banco

Antes, um título de 300 caracteres era recusado pelo Postgres (`varchar(255)`) e a API respondia
**500**: culpa do cliente virando culpa do servidor. Um título `"     "` o banco aceitava: **201** e lixo
gravado. Esse é o pior, porque o 201 esconde o erro.

- `@NotBlank` e `@Size(max = 100)` no `titulo`: 100 é regra de negócio minha, não limite do banco.
  `@Size(max = 255)` na `descricao`: é o teto da coluna. O limite do DTO nunca pode passar o da coluna,
  senão volta o 500.
- `@Valid` no `POST` e no `PUT`. Sem ele as anotações não são conferidas: testei, e continuou 201.
- `GlobalExceptionHandler`: o `@RestControllerAdvice` vigia todos os controllers, e o
  `@ExceptionHandler(MethodArgumentNotValidException.class)` monta o mapa campo → mensagem. A mensagem
  sai do console da aplicação e volta ao cliente.

Limite conhecido: uma mensagem por campo. Um título de 101 espaços quebra duas regras, e só uma aparece.
A ordem entre elas não é garantida.

## Autor

Caio Veras
