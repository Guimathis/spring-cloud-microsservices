# Microservices Ecosystem - Spring Cloud to Kubernetes

Este repositório contém um ecossistema de microsserviços que evoluiu de uma stack baseada em **Spring Cloud Netflix** para uma infraestrutura moderna orquestrada por **Kubernetes**.

## 🔄 Evolução da Stack

Originalmente (versão de release), o projeto utilizava os seguintes componentes do Spring Cloud Netflix:
- **Eureka**: Service Discovery.
- **Spring Cloud Gateway**: API Gateway.
- **Config Server**: Centralização de configurações.
- **OpenFeign & Resilience4j**: Comunicação e resiliência.

Atualmente, o projeto foi migrado para **Kubernetes**, utilizando recursos nativos para Service Discovery, Balanceamento de Carga e Gerenciamento de Configurações.

## 🏗️ Arquitetura Atual (Kubernetes)

O ecossistema é composto pelos seguintes serviços principais:

- **Book Service (Porta 8100):** 
  - Microsserviço responsável pela gestão de livros e cálculo de preços convertidos.
- **Exchange Service (Porta 8000):**
  - Microsserviço responsável pela conversão de moedas.

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.4.x**
- **Spring Cloud**
  - OpenFeign (Comunicação Síncrona)
  - Resilience4j (Circuit Breaker, Retry, Rate Limiter, Bulkhead)
- **Kubernetes (Orquestração)**
  - Services (LoadBalancer / ClusterIP)
  - Deployments & Namespaces
  - ConfigMaps & Secrets
- **Banco de Dados & Migração**
  - PostgreSQL
  - Flyway
- **Infraestrutura & CI/CD**
  - Docker / Docker Compose (Dev Local)
  - GitHub Actions
  - Google Kubernetes Engine (GKE)
- **Ferramentas Auxiliares**
  - Lombok
  - Maven

## 🚀 Como Executar

### Pré-requisitos
1. Docker e Docker Compose (opcional para rodar sem K8s).
2. Cluster Kubernetes (Minikube, Kind ou GKE).
3. `kubectl` configurado.

### Executando no Kubernetes
1. Aplique os manifestos do `exchange-service`:
   ```bash
   kubectl apply -f exchange-service/k8s/
   ```
2. Aplique os manifestos do `book-service`:
   ```bash
   kubectl apply -f book-service/k8s/
   ```

### Executando com Docker Compose (Local)
1. Clone o repositório.
2. Inicie os serviços básicos:
   ```bash
   docker-compose up -d
   ```
3. Os serviços estarão disponíveis nas portas **8100** (Book) e **8000** (Exchange).

## 📡 Endpoints Principais

No Kubernetes, os serviços são expostos via LoadBalancer. Acesse através do IP externo ou encaminhe as portas localmente:

### Book Service
Busca informações de um livro e converte o seu preço para a moeda desejada.
- **Endpoint:** `GET /book/{id}/{currency}`
- **Exemplo:** `http://localhost:8100/book/1/BRL`

### Exchange Service
Realiza a conversão de um valor entre duas moedas.
- **Endpoint:** `GET /exchange-service/{value}/{from}/{to}`
- **Exemplo:** `http://localhost:8000/exchange-service/100/USD/BRL`

## 🛡️ Resiliência
O **Book Service** está configurado com **Resilience4j** para lidar com falhas no **Exchange Service**:
- **Circuit Breaker:** Abre o circuito após 50% de falha em uma janela de 10 chamadas.
- **Retry:** Tenta até 3 vezes em caso de falha.
- **Rate Limiter:** Limita a 10 requisições por segundo.
