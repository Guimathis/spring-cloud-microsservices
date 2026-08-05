# Spring Cloud Microsservices

Este repositório contém um ecossistema de microsserviços desenvolvido com **Spring Boot** e **Spring Cloud**. O projeto demonstra a implementação de padrões comuns em arquiteturas distribuídas, como Service Discovery, API Gateway, Load Balancing e Resiliência.

## 🏗️ Arquitetura

O ecossistema é composto pelos seguintes serviços:

- **Spring Eureka Naming Server (Porta 8761):** 
  - Servidor de registro e descoberta de serviços.
- **Spring Cloud API Gateway (Porta 8765):** 
  - Ponto único de entrada da aplicação, responsável pelo roteamento para os microsserviços.
- **Book Service (Porta 8100+):** 
  - Microsserviço responsável pela gestão de livros e cálculo de preços convertidos.
- **Exchange Service (Porta 8000+):**
  - Microsserviço responsável pela conversão de moedas.

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Cloud 2025.1.2**
  - Eureka (Naming Server)
  - Spring Cloud Gateway
  - OpenFeign (Comunicação Síncrona)
  - Resilience4j (Circuit Breaker, Retry, Rate Limiter, Bulkhead)
- **Banco de Dados & Migração**
  - PostgreSQL
  - Flyway
- **Ferramentas Auxiliares**
  - Lombok
  - Maven

## 🚀 Como Executar

### Pré-requisitos
1. Java 21 instalado.
2. PostgreSQL rodando localmente.
3. Crie dois bancos de dados: `booking-db` e `exchange-db`.
   - As credenciais padrão configuradas são: Usuário `postgres` e Senha `admin`.

### Passo a Passo
1. Clone o repositório.
2. Compile o projeto:
   ```bash
   mvn clean install
   ```
3. Inicie os serviços:
   1. **Naming Server:** `spring-eureka-naming-server`
   2. **API Gateway:** `spring-cloud-api-gateway`
   3. **Exchange Service:** `exchange-service`
   4. **Book Service:** `book-service`

## 📡 Endpoints Principais

Acesse preferencialmente através do API Gateway (Porta **8765**):

### Book Service
Busca informações de um livro e converte o seu preço para a moeda desejada.
- **Endpoint:** `GET /book/{id}/{currency}`
- **Exemplo:** `http://localhost:8765/book/1/BRL`
  - Onde você pode "derrubar" o exchange-service para visualizar o Circuit Breaker funcionando.

### Exchange Service
Realiza a conversão de um valor entre duas moedas.
- **Endpoint:** `GET /exchange-service/{value}/{from}/{to}`
- **Exemplo:** `http://localhost:8765/exchange-service/100/USD/BRL`

### Eureka Dashboard
Visualize os serviços registrados:
- `http://spring-eureka-naming-server:8761`

## 🛡️ Resiliência
O **Book Service** está configurado com **Resilience4j** para lidar com falhas no **Exchange Service**:
- **Circuit Breaker:** Abre o circuito após 50% de falha em uma janela de 10 chamadas.
- **Retry:** Tenta até 3 vezes em caso de falha.
- **Rate Limiter:** Limita a 10 requisições por segundo.
