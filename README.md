# 🐾 Banho & Tosa API

API REST simples para gerenciamento de clientes, pets e agendamentos de um pet shop, desenvolvida em Java com Spring Boot para praticar **Padrões de Projeto (Design Patterns)**.

## Objetivo do desafio

O projeto foi criado como entrega prática de um desafio sobre Design Patterns. A proposta é demonstrar a aplicação dos padrões em um cenário pequeno, compreensível e útil para portfólio.

## Tecnologias

- Java 25
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 Database
- Maven

## Padrões de projeto aplicados

### Strategy

O cálculo do preço muda de acordo com o serviço escolhido. Para evitar vários `if/else`, cada serviço possui sua própria estratégia:

- `BanhoPrecoStrategy`
- `TosaPrecoStrategy`
- `BanhoETosaPrecoStrategy`

Todas implementam a interface `PrecoServicoStrategy`.

### Factory

A classe `PrecoServicoFactory` recebe as estratégias disponíveis e devolve a implementação correta de acordo com o `TipoServico`.

### Facade

A classe `AgendamentoFacade` fornece uma interface simples para operações que envolvem várias partes do sistema. Ao criar um agendamento, ela:

1. Busca o cliente.
2. Busca o pet.
3. Confere se o pet pertence ao cliente.
4. Escolhe a estratégia de preço.
5. Calcula o valor.
6. Salva o agendamento.

O controller não precisa conhecer esses detalhes.

### Singleton com Spring

Por padrão, componentes gerenciados pelo Spring, como classes anotadas com `@Service` e `@Component`, utilizam escopo singleton. Assim, `AgendamentoFacade` e as estratégias são instanciadas e gerenciadas pelo container do Spring.

### Repository

As interfaces que estendem `JpaRepository` isolam o acesso aos dados e deixam as regras de negócio desacopladas da persistência.

## Estrutura principal

```text
src/main/java/com/portfolio/banhoetosa
├── controller
│   ├── AgendamentoController.java
│   ├── ClienteController.java
│   └── PetController.java
├── dto
│   ├── AgendamentoRequest.java
│   └── PetRequest.java
├── exception
├── facade
│   └── AgendamentoFacade.java
├── model
├── repository
└── strategy
```

## Regras implementadas

- Cliente deve ser cadastrado antes do pet.
- Pet deve estar associado a um cliente existente.
- Só é possível agendar um serviço para o verdadeiro tutor do pet.
- A data do agendamento deve estar no futuro.
- O preço é calculado automaticamente conforme serviço e peso do pet.
- Um agendamento só pode ser concluído ou cancelado enquanto estiver com status `AGENDADO`.

## Preços utilizados

| Serviço | Pet até 10 kg | Pet acima de 10 kg |
|---|---:|---:|
| Banho | R$ 45,00 | R$ 60,00 |
| Tosa | R$ 55,00 | R$ 75,00 |
| Banho + Tosa | R$ 85,00 | R$ 115,00 |

## Como executar

Pré-requisitos:

- JDK 17 ou superior
- Maven 3.6.3 ou superior

Na raiz do projeto:

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

Console do H2:

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:mem:banhoetosa
User Name: sa
Password: deixe em branco
```

## Endpoints

### Cadastrar cliente

`POST /clientes`

```json
{
  "nome": "Ana Souza",
  "telefone": "11999999999",
  "email": "ana@email.com"
}
```

### Listar clientes

`GET /clientes`

### Cadastrar pet

`POST /pets`

```json
{
  "nome": "Thor",
  "especie": "Cachorro",
  "raca": "Shih-tzu",
  "pesoKg": 7.5,
  "clienteId": 1
}
```

### Listar pets

`GET /pets`

### Criar agendamento

`POST /agendamentos`

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

### Listar agendamentos

`GET /agendamentos`

### Concluir agendamento

`PATCH /agendamentos/{id}/concluir`

### Cancelar agendamento

`PATCH /agendamentos/{id}/cancelar`

## Testes rápidos com Postman

O diretório `postman/` contém uma collection pronta para importar e testar as operações principais.

## Possíveis evoluções

- Banco PostgreSQL ou MySQL.
- Autenticação de usuários.
- Cadastro de funcionários.
- Controle de horários disponíveis.
- Histórico de atendimentos.
- Swagger/OpenAPI.
- Testes unitários para cada Strategy.

## Autoria

Projeto desenvolvido para fins educacionais e de portfólio durante o desafio de Padrões de Projeto com Java e Spring.
