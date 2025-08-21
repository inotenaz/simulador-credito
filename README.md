# Simulador de Crédito API - Desafio Backend Hackathon 2025 - Sergio Zanetoni Junior (c149496)

API para simulação de crédito, permitindo consultar produtos de crédito, calcular parcelas e persistir chamadas de simulação.

Swagger: http://localhost:8080/swagger-ui/index.html#/ (necessário que o projeto esteja sendo executado em máquina local)
Repositório no GIT: https://github.com/inotenaz/simulador-credito.git

---

## Tecnologias

* **Linguagem:** Java 17
* **Framework:** Spring Boot
* **Banco de dados:** PostgreSQL (local, persistência) e SQL Server (externo, somente leitura)
* **Persistência:** JPA / Hibernate
* **Conexão com banco:** HikariCP
* **Build:** Maven
* **Containerização:** Docker e Docker Compose

---

## Funcionalidades

* Listar produtos de crédito disponíveis no sistema.
* Criar simulações de crédito com cálculo de parcelas.
* Persistir histórico de simulações no banco local (PostgreSQL).
* Consultar produtos de crédito de um banco externo (SQL Server) apenas para leitura.

---

## Arquitetura do Projeto

```
src/main/java/com/hackathon2025/simulador_credito/
│
├── config          # Configurações de banco e beans da aplicação
├── controller      # Endpoints REST
├── model           # Entidades do banco e DTOs
├── repository      # Repositórios JPA / JDBC
└── service         # Lógica de negócio e cálculos
```


## Passos rápidos

1. Build do projeto:
   mvn clean package

2. Subir containers:
   docker compose up

