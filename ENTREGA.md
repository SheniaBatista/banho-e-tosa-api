# 🐾 Banho & Tosa API

API REST para gerenciamento de clientes, pets e agendamentos de um pet shop, desenvolvida com Java e Spring Boot como aplicação prática de **Padrões de Projeto (Design Patterns)**.

O projeto utiliza Strategy, Factory, Facade e Repository para organizar responsabilidades, reduzir acoplamento e manter as regras de negócio separadas da camada HTTP.

## Objetivo do desafio

O projeto foi desenvolvido como entrega prática de um desafio sobre Padrões de Projeto com Java e Spring.

A proposta é aplicar padrões de projeto em um cenário realista e de fácil compreensão, demonstrando como diferentes responsabilidades podem ser distribuídas entre os componentes da aplicação.

O fluxo principal permite cadastrar clientes e seus pets, criar agendamentos e calcular automaticamente o preço do atendimento de acordo com o serviço escolhido e o peso do animal.

## Tecnologias

- Java 25
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Hibernate
- Maven

## Padrões de projeto aplicados

### Strategy

O cálculo do preço varia de acordo com o serviço escolhido.

Para evitar concentrar essa lógica em uma sequência de condicionais, cada tipo de serviço possui sua própria estratégia:

- `BanhoPrecoStrategy`
- `TosaPrecoStrategy`
- `BanhoETosaPrecoStrategy`

Todas implementam a interface:

```java
PrecoServicoStrategy
```

Dessa forma, novas formas de cálculo podem ser adicionadas sem concentrar todas as regras em uma única classe.

### Factory

A classe `PrecoServicoFactory` é responsável por selecionar a estratégia adequada de acordo com o `TipoServico`.

Assim, quem solicita o cálculo não precisa conhecer diretamente qual implementação de `PrecoServicoStrategy` deve ser utilizada.

### Facade

A classe `AgendamentoFacade` centraliza o fluxo necessário para realizar as operações relacionadas aos agendamentos.

Ao criar um novo agendamento, ela coordena etapas como:

1. Buscar o cliente.
2. Buscar o pet.
3. Verificar se o pet pertence ao cliente informado.
4. Selecionar a estratégia correspondente ao serviço.
5. Calcular o valor do atendimento.
6. Persistir o agendamento.

Com isso, o controller permanece responsável pela camada HTTP sem precisar conhecer todos os detalhes das regras de negócio.

### Singleton no contexto do Spring

Os componentes gerenciados pelo Spring utilizam, por padrão, escopo singleton.

Dessa forma, classes como `AgendamentoFacade` e as implementações de `PrecoServicoStrategy` têm suas instâncias criadas e administradas pelo container de Inversão de Controle do Spring.

Neste projeto, portanto, o padrão não é implementado manualmente com construtores privados ou métodos estáticos. O ciclo de vida das instâncias fica sob responsabilidade do framework.

### Repository

Além dos padrões trabalhados no desafio, o projeto utiliza Repository para abstrair o acesso aos dados.

As interfaces:

- `ClienteRepository`
- `PetRepository`
- `AgendamentoRepository`

estendem `JpaRepository`, permitindo utilizar a abstração fornecida pelo Spring Data JPA para persistência e consulta das entidades.

## Fluxo da aplicação

```text
Requisição HTTP
      │
      ▼
  Controller
      │
      ▼
    Facade
   ┌──┴───────────────┐
   ▼                  ▼
Repository     Factory / Strategy
   │                  │
   ▼                  ▼
PostgreSQL      Regra de preço
```

Os controllers recebem as requisições HTTP, enquanto a `AgendamentoFacade` coordena as regras relacionadas aos agendamentos.

A escolha e o cálculo do preço são delegados à Factory e às Strategies, enquanto os repositories concentram o acesso aos dados persistidos no PostgreSQL.

## Estrutura principal

```text
src/main/java/com/portfolio/banhoetosa/
│
├── BanhoETosaApiApplication.java
│
├── controller/
│   ├── AgendamentoController.java
│   ├── ClienteController.java
│   └── PetController.java
│
├── dto/
│   ├── AgendamentoRequest.java
│   └── PetRequest.java
│
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── RecursoNaoEncontradoException.java
│   └── RegraNegocioException.java
│
├── facade/
│   └── AgendamentoFacade.java
│
├── model/
│   ├── Agendamento.java
│   ├── Cliente.java
│   ├── Pet.java
│   ├── StatusAgendamento.java
│   └── TipoServico.java
│
├── repository/
│   ├── AgendamentoRepository.java
│   ├── ClienteRepository.java
│   └── PetRepository.java
│
└── strategy/
    ├── BanhoETosaPrecoStrategy.java
    ├── BanhoPrecoStrategy.java
    ├── PrecoServicoFactory.java
    ├── PrecoServicoStrategy.java
    └── TosaPrecoStrategy.java
```

## Regras implementadas

- O cliente deve ser cadastrado antes do pet.
- O pet deve estar associado a um cliente existente.
- Um serviço só pode ser agendado pelo cliente associado ao pet.
- A data e hora do agendamento devem estar no futuro.
- O preço é calculado automaticamente de acordo com o serviço e o peso do pet.
- Um agendamento só pode ser concluído ou cancelado enquanto estiver com status `AGENDADO`.

## Preços utilizados

| Serviço | Pet até 10 kg | Pet acima de 10 kg |
|---|---:|---:|
| Banho | R$ 45,00 | R$ 60,00 |
| Tosa | R$ 55,00 | R$ 75,00 |
| Banho + Tosa | R$ 85,00 | R$ 115,00 |

## API REST

### Clientes

#### Cadastrar cliente

```http
POST /clientes
```

Exemplo:

```json
{
  "nome": "Ana Souza",
  "telefone": "11999999999",
  "email": "ana@email.com"
}
```

#### Listar clientes

```http
GET /clientes
```

### Pets

#### Cadastrar pet

```http
POST /pets
```

Exemplo:

```json
{
  "nome": "Thor",
  "especie": "Cachorro",
  "raca": "Shih-tzu",
  "pesoKg": 7.5,
  "clienteId": 1
}
```

#### Listar pets

```http
GET /pets
```

### Agendamentos

#### Criar agendamento

```http
POST /agendamentos
```

Exemplo:

```json
{
  "clienteId": 1,
  "petId": 1,
  "tipoServico": "BANHO_E_TOSA",
  "dataHora": "2027-01-20T14:00:00"
}
```

Tipos de serviço aceitos:

```text
BANHO
TOSA
BANHO_E_TOSA
```

O valor do atendimento é calculado automaticamente pela aplicação.

#### Listar agendamentos

```http
GET /agendamentos
```

#### Concluir agendamento

```http
PATCH /agendamentos/{id}/concluir
```

#### Cancelar agendamento

```http
PATCH /agendamentos/{id}/cancelar
```

## Como executar

### Pré-requisitos

- JDK 25
- Maven
- PostgreSQL

### 1. Criar o banco de dados

Com o PostgreSQL em execução, crie o banco utilizado pela aplicação:

```sql
CREATE DATABASE banho_e_tosa;
```

### 2. Configurar o acesso ao PostgreSQL

A aplicação recebe o usuário e a senha do banco através das variáveis de ambiente `DB_USERNAME` e `DB_PASSWORD`.

No Windows PowerShell:

```powershell
$env:DB_USERNAME="seu_usuario"
$env:DB_PASSWORD="sua_senha"
```

As variáveis configuradas dessa forma ficam disponíveis durante a sessão atual do terminal.

O arquivo `application.properties` utiliza essas variáveis na configuração do datasource.

### 3. Executar a aplicação

Na raiz do projeto:

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

O Hibernate cria e atualiza as tabelas necessárias através da configuração:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Testando com Postman

O diretório `postman/` contém uma collection pronta para importar no Postman e testar as principais operações da API.

Um fluxo de teste pode ser realizado nesta ordem:

```text
1. Cadastrar cliente
        ↓
2. Cadastrar pet
        ↓
3. Criar agendamento
        ↓
4. Listar agendamentos
        ↓
5. Concluir ou cancelar agendamento
```

Como o pet depende de um cliente e o agendamento depende de ambos, essa sequência também representa o fluxo básico das entidades da aplicação.

## Tratamento de erros

A aplicação possui tratamento centralizado de exceções através de `GlobalExceptionHandler`.

Entre as situações tratadas estão:

- recurso não encontrado;
- violações das regras de negócio;
- dados inválidos enviados nas requisições.

As respostas de erro são representadas pelo objeto `ApiError`, evitando que detalhes internos da aplicação sejam retornados diretamente ao cliente.

## Possíveis evoluções

- Autenticação de usuários.
- Cadastro de funcionários.
- Controle de horários disponíveis.
- Histórico de atendimentos.
- Documentação interativa com Swagger/OpenAPI.
- Ampliação da cobertura de testes.
- Migrations do banco de dados com Flyway.
- Containerização da aplicação e do PostgreSQL com Docker.

## Autoria

Projeto desenvolvido para fins educacionais e de portfólio como entrega prática de um desafio de **Padrões de Projeto com Java e Spring**.