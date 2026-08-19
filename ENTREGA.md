# Banho & Tosa API

API REST para gerenciamento de clientes, pets e agendamentos de serviços de banho e tosa, desenvolvida com Java e Spring Boot.

A aplicação permite cadastrar clientes e seus pets, criar agendamentos, calcular automaticamente o preço do serviço escolhido e controlar o ciclo de vida de cada atendimento.

Além das operações REST, o projeto aplica padrões de projeto para separar responsabilidades e evitar que regras de negócio fiquem concentradas nos controllers.

O fluxo principal da aplicação é:

```text
Requisição HTTP
      ↓
Controller
      ↓
Facade
      ↓
Regras de negócio
      ↓
Repository
      ↓
PostgreSQL
```

Para o cálculo dos serviços, a aplicação utiliza Strategy e Factory, permitindo que cada tipo de serviço possua sua própria regra de preço.

---

## Desafio DIO

Projeto desenvolvido como parte de um desafio prático da Digital Innovation One (DIO), com o objetivo de aplicar conceitos de desenvolvimento back-end com Java, Spring Boot e padrões de projeto.

A implementação evolui o exercício para uma API REST persistida em PostgreSQL, com validação de dados, relacionamentos entre entidades, tratamento global de erros e configuração de credenciais por variáveis de ambiente.

---

## Arquitetura

O projeto organiza as responsabilidades nos seguintes componentes:

```text
Controller
    ↓
Facade
    ↓
Repository
    ↓
PostgreSQL
```

O cálculo de preços segue um fluxo separado:

```text
AgendamentoFacade
        ↓
PrecoServicoFactory
        ↓
PrecoServicoStrategy
        ↓
┌─────────────┬─────────────┬──────────────────────┐
│    BANHO    │    TOSA     │    BANHO_E_TOSA     │
│             │             │                      │
│ BanhoPreco  │ TosaPreco   │ BanhoETosaPreco     │
│ Strategy    │ Strategy    │ Strategy             │
└─────────────┴─────────────┴──────────────────────┘
```

### Facade

`AgendamentoFacade` centraliza o fluxo principal da aplicação.

Controllers recebem as requisições HTTP e delegam as operações para a facade, que coordena acesso aos repositories, validações e cálculo de preços.

Ao criar um agendamento, por exemplo, o fluxo é:

```text
clienteId
    ↓
Buscar cliente

petId
    ↓
Buscar pet

    ↓
Verificar se o pet pertence ao cliente

    ↓
Selecionar estratégia do serviço

    ↓
Calcular preço

    ↓
Criar agendamento com status AGENDADO

    ↓
Persistir no PostgreSQL
```

### Strategy

O cálculo de preço foi separado através do padrão Strategy.

O contrato comum é definido por:

```text
PrecoServicoStrategy
```

e possui implementações específicas:

```text
BanhoPrecoStrategy
TosaPrecoStrategy
BanhoETosaPrecoStrategy
```

Cada estratégia conhece somente a regra de cálculo correspondente ao seu serviço.

Isso evita estruturas condicionais extensas dentro da regra de agendamento e permite adicionar novas formas de cálculo sem alterar as estratégias existentes.

### Factory

`PrecoServicoFactory` é responsável por localizar a Strategy correspondente ao `TipoServico` solicitado.

As implementações de `PrecoServicoStrategy` são gerenciadas pelo Spring e organizadas pela Factory utilizando um `EnumMap`.

Assim, a facade não precisa conhecer qual classe concreta realiza cada cálculo.

### Repository

A persistência utiliza Spring Data JPA.

Existem repositories específicos para as principais entidades:

```text
ClienteRepository
PetRepository
AgendamentoRepository
```

O Hibernate realiza o mapeamento objeto-relacional e o PostgreSQL é utilizado como banco de dados.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1.0 |
| API | Spring Web |
| Validação | Bean Validation |
| Persistência | Spring Data JPA |
| ORM | Hibernate |
| Banco de dados | PostgreSQL |
| Pool de conexões | HikariCP |
| Build | Maven |
| Testes | JUnit |
| Teste manual da API | Postman |

---

## Funcionalidades

### Clientes

- Cadastrar cliente
- Listar clientes

### Pets

- Cadastrar pet associado a um cliente
- Listar pets
- Excluir pet

### Agendamentos

- Criar agendamento
- Associar cliente e pet
- Escolher o tipo de serviço
- Calcular o valor automaticamente
- Listar agendamentos
- Concluir agendamento
- Cancelar agendamento

---

## API REST

### Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/clientes` | Cadastra um cliente |
| `GET` | `/clientes` | Lista os clientes |

### Pets

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/pets` | Cadastra um pet |
| `GET` | `/pets` | Lista os pets |
| `DELETE` | `/pets/{id}` | Exclui um pet |

### Agendamentos

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/agendamentos` | Cria um agendamento |
| `GET` | `/agendamentos` | Lista os agendamentos |
| `PATCH` | `/agendamentos/{id}/concluir` | Marca um agendamento como concluído |
| `PATCH` | `/agendamentos/{id}/cancelar` | Cancela um agendamento |

---

## Regras de negócio

A aplicação implementa regras para preservar a consistência dos agendamentos.

### Associação entre cliente e pet

Um agendamento só pode ser criado quando o pet informado pertence ao cliente selecionado.

Caso contrário, a operação é rejeitada.

### Data do agendamento

A data e hora do agendamento devem estar no futuro.

A validação é realizada no DTO de entrada utilizando Bean Validation.

### Cálculo do preço

O cliente da API não informa diretamente o valor final do serviço.

O preço é calculado pela aplicação através da Strategy correspondente ao tipo de serviço selecionado.

```text
TipoServico
     ↓
PrecoServicoFactory
     ↓
Strategy correspondente
     ↓
Valor calculado
```

### Status inicial

Todo novo agendamento é criado com:

```text
AGENDADO
```

### Conclusão

Somente um agendamento com status `AGENDADO` pode ser concluído.

A operação altera o status para:

```text
CONCLUIDO
```

### Cancelamento

Somente um agendamento com status `AGENDADO` pode ser cancelado.

A operação altera o status para:

```text
CANCELADO
```

---

## Modelo de dados

A aplicação possui três entidades principais:

```text
Cliente
   │
   └── Pet

Cliente ─────────┐
                 │
Pet ─────────────┴── Agendamento
```

### Cliente

Representa o tutor responsável pelos pets.

Principais informações:

```text
id
nome
telefone
email
```

### Pet

Representa um animal associado a um cliente.

Principais informações:

```text
id
nome
especie
raca
pesoKg
cliente
```

### Agendamento

Representa a contratação de um serviço para determinado pet.

Principais informações:

```text
id
cliente
pet
dataHora
tipoServico
status
valor
```

Os relacionamentos são persistidos através de chaves estrangeiras no PostgreSQL.

---

## Tipos de serviço

Os serviços disponíveis são representados pelo enum `TipoServico`:

```text
BANHO
TOSA
BANHO_E_TOSA
```

Cada opção possui uma Strategy responsável pelo cálculo do preço.

---

## Status do agendamento

O ciclo de vida é representado pelo enum `StatusAgendamento`:

```text
AGENDADO
CONCLUIDO
CANCELADO
```

O fluxo permitido é:

```text
                  ┌──▶ CONCLUIDO
                  │
AGENDADO ─────────┤
                  │
                  └──▶ CANCELADO
```

Depois de concluído ou cancelado, o agendamento não pode passar novamente pelas operações de conclusão ou cancelamento.

---

## Tratamento de erros

A API utiliza um `GlobalExceptionHandler` com `@RestControllerAdvice` para centralizar o tratamento das exceções.

Entre os cenários tratados estão:

| Situação | HTTP |
|---|---:|
| Recurso não encontrado | `404 Not Found` |
| Violação de regra de negócio | `400 Bad Request` |
| Argumento inválido | `400 Bad Request` |
| Falha de validação dos dados | `400 Bad Request` |

Os erros são devolvidos utilizando uma estrutura padronizada:

```json
{
  "timestamp": "2026-08-19T12:00:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Descrição do problema"
}
```

Isso evita retornar stack traces diretamente para quem consome a API e mantém o formato das respostas de erro consistente.

---

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/com/portfolio/banhoetosa/
│   │   │
│   │   ├── BanhoETosaApiApplication.java
│   │   │
│   │   ├── controller/
│   │   │   ├── AgendamentoController.java
│   │   │   ├── ClienteController.java
│   │   │   └── PetController.java
│   │   │
│   │   ├── dto/
│   │   │   ├── AgendamentoRequest.java
│   │   │   └── PetRequest.java
│   │   │
│   │   ├── exception/
│   │   │   ├── ApiError.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── RecursoNaoEncontradoException.java
│   │   │   └── RegraNegocioException.java
│   │   │
│   │   ├── facade/
│   │   │   └── AgendamentoFacade.java
│   │   │
│   │   ├── model/
│   │   │   ├── Agendamento.java
│   │   │   ├── Cliente.java
│   │   │   ├── Pet.java
│   │   │   ├── StatusAgendamento.java
│   │   │   └── TipoServico.java
│   │   │
│   │   ├── repository/
│   │   │   ├── AgendamentoRepository.java
│   │   │   ├── ClienteRepository.java
│   │   │   └── PetRepository.java
│   │   │
│   │   └── strategy/
│   │       ├── BanhoPrecoStrategy.java
│   │       ├── TosaPrecoStrategy.java
│   │       ├── BanhoETosaPrecoStrategy.java
│   │       ├── PrecoServicoFactory.java
│   │       └── PrecoServicoStrategy.java
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/com/portfolio/banhoetosa/
        └── BanhoETosaApiApplicationTests.java

postman/
└── Banho-e-Tosa.postman_collection.json
```

---

## Como executar

### Requisitos

Para executar o projeto localmente:

| Item | Requisito |
|---|---|
| JDK | 25 |
| PostgreSQL | instalado e em execução |
| Maven | compatível com o projeto |
| Cliente HTTP | Postman ou equivalente |

---

### 1. Criar o banco de dados

No PostgreSQL, crie o banco:

```sql
CREATE DATABASE banho_e_tosa;
```

Crie ou utilize um usuário com permissão para acessar esse banco.

As credenciais não ficam armazenadas no código-fonte.

---

### 2. Configurar as variáveis de ambiente

A aplicação espera duas variáveis:

```text
DB_USERNAME
DB_PASSWORD
```

No Windows PowerShell, por exemplo:

```powershell
$env:DB_USERNAME="seu_usuario"
$env:DB_PASSWORD="sua_senha"
```

As variáveis definidas dessa maneira são válidas para a sessão atual do terminal.

No IntelliJ IDEA, elas também podem ser configuradas nas Environment Variables da Run Configuration.

Não coloque a senha diretamente no `application.properties`.

---

### 3. Configuração da aplicação

O datasource utiliza:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banho_e_tosa
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

O Hibernate está configurado para atualizar o schema durante o desenvolvimento:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Ao iniciar a aplicação, as tabelas necessárias são criadas ou atualizadas a partir das entidades JPA.

---

### 4. Executar

Pelo IntelliJ IDEA, execute:

```text
BanhoETosaApiApplication
```

Ou utilize Maven pelo terminal conforme a configuração disponível no ambiente:

```bash
mvn spring-boot:run
```

Quando a inicialização for concluída, a API estará disponível em:

```text
http://localhost:8080
```

Um log semelhante a este indica que a aplicação iniciou corretamente:

```text
Tomcat started on port 8080
Started BanhoETosaApiApplication
```

---

## Testando com Postman

O projeto contém uma collection em:

```text
postman/Banho-e-Tosa.postman_collection.json
```

Ela pode ser importada diretamente no Postman para testar os endpoints da aplicação.

Um fluxo típico de teste é:

```text
1. Cadastrar cliente
        ↓
2. Cadastrar pet para o cliente
        ↓
3. Criar agendamento
        ↓
4. Listar agendamentos
        ↓
5. Concluir ou cancelar
```

---

## Exemplos de requisições

### Cadastrar cliente

```http
POST /clientes
Content-Type: application/json
```

Exemplo de corpo:

```json
{
  "nome": "Ana Souza",
  "telefone": "11999999999",
  "email": "ana@email.com"
}
```

---

### Cadastrar pet

```http
POST /pets
Content-Type: application/json
```

Exemplo:

```json
{
  "nome": "Mel",
  "especie": "Cachorro",
  "raca": "Golden Retriever",
  "pesoKg": 25.0,
  "clienteId": 1
}
```

O `clienteId` determina quem é o tutor do pet.

---

### Criar agendamento

```http
POST /agendamentos
Content-Type: application/json
```

Exemplo:

```json
{
  "clienteId": 1,
  "petId": 1,
  "dataHora": "2026-12-10T14:00:00",
  "tipoServico": "BANHO"
}
```

O valor do serviço não precisa ser enviado: ele é calculado automaticamente pela aplicação.

---

### Listar clientes

```http
GET /clientes
```

### Listar pets

```http
GET /pets
```

### Listar agendamentos

```http
GET /agendamentos
```

### Concluir agendamento

```http
PATCH /agendamentos/1/concluir
```

### Cancelar agendamento

```http
PATCH /agendamentos/1/cancelar
```

### Excluir pet

```http
DELETE /pets/1
```

---


## Testes

Atualmente o projeto possui um teste de contexto com `@SpringBootTest`, responsável por verificar a inicialização do contexto Spring.

```text
BanhoETosaApiApplicationTests
└── contextLoads()
```

Os fluxos da API também podem ser testados manualmente através da collection Postman incluída no projeto.

Uma evolução futura é ampliar a cobertura automatizada das regras de negócio, especialmente cálculo de preços, criação de agendamentos e transições de status.

---

## Decisões de projeto

### Por que Strategy para os preços?

As regras de preço variam conforme o serviço.

Colocar todos os cálculos em uma única classe exigiria condicionais para descobrir qual regra executar. Com Strategy, cada serviço possui uma implementação independente.

A aplicação trabalha contra o contrato:

```text
PrecoServicoStrategy
```

e não precisa concentrar todas as regras em um único método.

### Por que uma Factory?

Mesmo com Strategy, algum componente precisa escolher qual implementação será usada.

Essa responsabilidade fica em `PrecoServicoFactory`, que associa cada `TipoServico` à Strategy correspondente.

Assim, a facade solicita a estratégia adequada sem precisar instanciar ou selecionar manualmente classes concretas.

### Por que uma Facade?

Criar um agendamento envolve mais do que salvar uma entidade.

É necessário localizar cliente e pet, verificar a relação entre eles, selecionar uma regra de preço, calcular o valor, definir o status inicial e persistir o resultado.

A facade oferece uma entrada única para esse fluxo e impede que os controllers acumulem regras de negócio.

### Por que variáveis de ambiente?

Usuário e senha do banco são configurações do ambiente onde a aplicação executa, não características do código.

Por isso o repositório mantém apenas os nomes:

```text
DB_USERNAME
DB_PASSWORD
```

e cada ambiente fornece seus próprios valores.

---

## Aprendizados

O projeto permitiu aplicar conceitos que aparecem em conjunto em aplicações Spring reais: API REST, injeção de dependências, persistência com JPA, relacionamentos entre entidades, validação, tratamento de exceções e integração com PostgreSQL.

A aplicação de Strategy deixou mais clara a diferença entre apenas criar classes com nomes de padrões e realmente distribuir responsabilidades. Cada cálculo de serviço pode evoluir de forma independente, enquanto a Factory concentra a seleção da implementação adequada.


---

## Melhorias futuras

Algumas evoluções possíveis para o projeto:

- ampliar a suíte de testes automatizados;
- criar DTOs específicos para respostas da API;
- adicionar atualização de clientes e pets;
- permitir consulta de pets por tutor;
- adicionar filtros de agendamento por data e status;
- impedir conflitos de horário;
- adicionar documentação interativa com OpenAPI/Swagger;
- utilizar migrations com Flyway em vez de depender de `ddl-auto=update`;
- adicionar autenticação e autorização;
- containerizar aplicação e PostgreSQL com Docker Compose.

---

## Licença

Projeto educacional desenvolvido para concluir a formação no Bootcamp Santander 2026 — AI Java Back-end, oferecido pela [Digital Innovation One.](https://www.dio.me/)