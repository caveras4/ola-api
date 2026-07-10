# ola-api

API REST simples construída com Spring Boot como projeto da Semana 1 do meu curso de Java/Spring Boot. Expõe endpoints de saudação para praticar a criação de um projeto do zero, mapeamento de rotas e execução local.

## Tecnologias

- Java 17+
- Spring Boot 4.1.0 (Spring Web MVC)
- Maven (via wrapper `./mvnw`, não precisa de instalação)

## Como executar

Clone o repositório e, na raiz do projeto, rode:

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints

| Método | Rota          | Resposta            |
|--------|---------------|---------------------|
| GET    | `/ola`        | `Olá, Mundo!`       |
| GET    | `/ola/{nome}` | `Olá, {nome}`       |

### Exemplos

```bash
curl http://localhost:8080/ola
# Olá, Mundo!

curl http://localhost:8080/ola/Caio
# Olá, Caio
```

## Autor

Caio Veras
