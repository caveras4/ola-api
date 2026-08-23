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

## Decisões

### 1. Por que os endpoints devolvem `TaskResponse` em vez da entidade `Task`

Hoje `Task` e `TaskResponse` têm os mesmos quatro campos, então a separação não é sobre o formato — é
sobre quem manda no formato. Quem decide o formato de `Task` é o banco: o nome do campo vira o nome da
coluna. Quem decide o formato de `TaskResponse` é o contrato que eu ofereço a quem consome a API. São
dois donos diferentes, e é por isso que são duas classes.

Testei os dois lados. Renomeando o campo `concluido` para `feito` **no `TaskResponse`**, o JSON passou
a responder `feito` e a coluna no Postgres continuou `concluido`: uma palavra trocada, mais nada.

Fazendo o mesmo rename **na entidade**, o custo foi outro. Para manter a coluna que já existia, precisei
de `@Column(name = "concluido")` em cima do campo — a alternativa seria renomear a coluna no banco. A
tabela ficou intacta, mas o rename vazou para fora de `Task`: o getter virou `isFeito()`, e o
`TaskResponse.fromEntity`, que chamava `task.isConcluido()`, parou de compilar até eu ajustar.

Por enquanto isso é barato, porque a API é pequena. O que me convence é o caso seguinte: quando eu
adicionar campos que não devem sair na resposta — senha de usuário, por exemplo — devolver a entidade
inteira significa decidir a cada campo novo se ele vaza ou não. Com o DTO eu não retorno a entidade por
completo, e sim apenas os dados que eu escolho.

### 2. Por que o `POST` recebe `TaskRequest` em vez da entidade `Task`

Enquanto o `POST` recebia a entidade `Task` direto no `@RequestBody`, o corpo da requisição mandava no
banco. Criei a task 6, mandei um `POST` com `"id": 6` no corpo e a linha 6 foi sobrescrita: nenhuma linha
nova, e a resposta veio `201 Created` com `Location: /tasks/6`. O nome disso é **mass assignment** — o
cliente preenchendo um campo que o servidor nunca pretendeu expor. Não é alarme falso: a linha existente
foi apagada de verdade, e a API ainda respondeu que tinha criado alguma coisa.

Trocando o parâmetro por `TaskRequest`, um record sem `id`, o mesmo `POST` passou a ignorar o campo e a
criar uma linha nova. O `id` deixou de ser dado de entrada e voltou a ser o que sempre foi: assunto do
banco, gerado por `GenerationType.IDENTITY`.

O que me interessa é *onde* cada coisa acontece, porque são três camadas e nenhuma sabe da outra. O
Jackson não conhece o banco — ele só transforma o JSON num objeto em memória. Com a `Task` ele tem
construtor vazio, que o Java gera por padrão já que a classe não declara nenhum, e tem setters: então
cria o objeto em branco e preenche campo a campo. Não existe `setId`, mas o `getId()` público basta para
o Jackson enxergar a propriedade e gravar direto no campo privado — confirmei tirando só a palavra
`public` do getter, e aí o mesmo `POST` com `"id": 6` criou a linha 7 e deixou a 6 intacta. Com o
`TaskRequest` o caminho é outro: record não tem construtor vazio nem setter, sobra só o construtor
canônico, e ali não existe parâmetro `id`. O record não rejeita o campo, ele não oferece lugar onde o
campo caiba; a chave fica sem destino e o Jackson a descarta em silêncio.

Quem decidiu `UPDATE` em vez de `INSERT` não foi o Jackson nem o Postgres: foi o `save()` do Spring Data,
olhando o `id` da entidade no momento da chamada. `id` nulo é entidade nova e vira `INSERT`; `id`
preenchido vira `UPDATE` na linha correspondente. É a mesma linha de código nos dois testes — o que mudou
foi só o estado do objeto que chegou nela. E o `201 Created` apareceu nas duas vezes porque está fixo no
controller, em `ResponseEntity.created(uri)`: ele nunca perguntou ao banco o que de fato aconteceu.

### 3. Por que o `PUT` não usa `toEntity()`

O `POST` converte com `request.toEntity()`, e reaproveitar esse mesmo método no `PUT` era o caminho
natural. Testei: o `PUT /tasks/3` criou uma linha nova em vez de atualizar, e a linha 3 continuou lá,
intacta, com os dados antigos.

O motivo é a regra da Decisão 2 aplicada ao contrário. `toEntity()` roda inteiro dentro do record: faz
`new Task()`, chama três setters e devolve. O quarto campo ninguém toca, e um `Long` não tocado fica
`null` — `TaskRequest` não tem componente `id`, então não existe valor para colocar ali. E o record não
teria como descobrir o 3 nem se quisesse: ele não sabe que existe uma URL. São duas portas de entrada
separadas nesse endpoint, com porteiros diferentes. O 3 está na URL e quem lê a URL é o Spring MVC,
pelo `@PathVariable`. O corpo é lido pelo Jackson, pelo `@RequestBody` — e o Jackson nunca chega perto
do 3. Nessa versão o controller recebe o 3 no parâmetro e não faz nada com ele: o número chega e é
descartado. O `save()` então olha uma entidade de `id` nulo e decide `INSERT`, pela mesma regra de
sempre.

A resposta ainda mente. O `200 OK` está fixo em `ResponseEntity.ok(...)` e o corpo é montado a partir da
entidade que o `save()` devolveu, que é a linha nova: o cliente pede para atualizar `/tasks/3` e recebe
de volta um objeto com outro `id`. A URL diz uma coisa e o corpo diz outra. É a mesma armadilha do
`201 Created` da Decisão 2 — o status foi escrito por mim no controller, não apurado no banco.

No código final eu não construo ponte nenhuma até a entidade: eu pego a entidade que já está do outro
lado. `repository.findById(id)` devolve um objeto que já tem `id = 3`, porque ele *é* a linha 3 — veio
do banco com o campo preenchido. Os três setters trocam `titulo`, `descricao` e `concluido`, o `id`
ninguém toca, e por isso ele chega no `save()` valendo 3 e vira `UPDATE`. O `findById` ainda paga um
segundo aluguel: é ele que sustenta o `orElse(ResponseEntity.notFound().build())`. Dá para preencher o
`id` na mão a partir do `@PathVariable` e o `UPDATE` funciona quando a linha existe, mas aí não sobra
ninguém para perguntar ao banco se ela existe, e um `PUT /tasks/999` passa a criar recurso em silêncio
numa URL que o cliente afirmou já existir.

Então não são duas regras. `toEntity()` serve ao `POST` e não serve ao `PUT` pela mesma razão única: o
`save()` decide `INSERT` ou `UPDATE` olhando o `id` da entidade que chega nele, e cada endpoint precisa
entregar a entidade no estado certo.

## Autor

Caio Veras
