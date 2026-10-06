# Aurora Bakery

[English](README.md) | [Português](README.pt-BR.md)

Aurora Bakery é um projeto de portfólio de e-commerce para uma única padaria, desenvolvido com Java e Angular. O escopo pretendido inclui catálogo, pedidos, promoções, fidelidade, autenticação, autorização, pagamentos de teste Stripe e administração, sem complexidade desnecessária de multi-tenancy ou microsserviços.

**Implementado hoje:** catálogo de produtos somente para leitura, disponibilidade explícita, ordenação de destaques, preços em BRL, estados Angular de carregamento/vazio/erro/tentativa, migrações Flyway, produtos fictícios opcionais de desenvolvimento, segurança restritiva no backend, probes de saúde e OpenAPI local. Uma política de compra testada define descontos ADMIN configuráveis e exclusão de benefícios/marketing de clientes; ainda não está conectada ao checkout.

**Planejado:** identidade persistida compartilhada CUSTOMER/ADMIN, cadastro/login de clientes, provisionamento controlado de administradores, escritas administrativas, upload de imagens, carrinho, pedidos, configuração de promoções, progressão de fidelidade por pedidos pagos, checkout/webhooks Stripe de teste e notificações. Ainda não existe fluxo de compra ou login.

## Desenvolvimento com Docker

Instale Docker com Compose v2. Em um clone novo:

```sh
cp .env.example .env
# Edite .env: use uma senha local de desenvolvimento.
docker compose config --quiet
docker compose up --build -d --wait --wait-timeout 180
```

Em um checkout existente, atualize `.env` conforme as variáveis documentadas, sem sobrescrevê-lo. Aplicação: http://localhost:4200/products. API: http://localhost:8080/api/v1/products. Swagger local: http://localhost:8080/swagger-ui/index.html. Readiness: http://localhost:8080/actuator/health/readiness.

Flyway cria/atualiza o schema na inicialização do backend; Hibernate o valida. `DEV_SEED_ENABLED=true` insere três produtos fictícios, de forma idempotente, apenas no perfil `dev`. Não há seed de contas ou credenciais administrativas. Desabilite a opção para iniciar um catálogo vazio; isso não remove registros existentes.

```sh
docker compose ps
docker compose logs --tail=100
docker compose down
```

O volume nomeado persiste após `down`. Para um reset **intencional** dos dados locais: `docker compose down --volumes`, seguido do comando de inicialização. Isso destrói o banco desse projeto Compose. O diretório legado `.dockerized-postgres` não é mais montado.

## Testes

O fluxo Docker não exige Java ou Node no host:

```sh
docker build --target build -t aurora-bakery-backend-tests ./backend
docker run --rm aurora-bakery-backend-tests ./mvnw -B -ntp verify
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal aurora-bakery-backend-tests ./mvnw -B -ntp verify -Pintegration
docker compose run --rm --no-deps frontend npm run build
docker compose run --rm --no-deps frontend npm test
```

O executor de integração precisa do daemon Docker local e usa um PostgreSQL descartável separado; não altera o banco da aplicação. Com JDK 25 e Node 22.23.3 no host, execute `./mvnw verify -Pintegration` em `backend` e `npm ci && npm run build && npm test` em `frontend`.

## Arquitetura e documentação

Uma aplicação Spring Boot 3.5.16 (Java 25, Maven Wrapper), uma aplicação Angular 21 (lockfile npm) e PostgreSQL 17.11. As versões existentes e digests imutáveis das imagens-base foram preservados. Não havia broker, provedor de identidade, proxy reverso, aplicação adicional ou pipeline CI no código-base.

- [Arquitetura e domínio](docs/pt-BR/architecture.md)
- [Decisões de arquitetura e trade-offs](docs/pt-BR/architecture-decisions.md)
- [Docker e variáveis de ambiente](docs/pt-BR/docker.md)
- [Escopo e próximos passos](docs/pt-BR/roadmap.md)
- [Verificação](docs/pt-BR/verification.md)
- [Relatório da reestruturação e limitações](docs/pt-BR/refactor-report.md)

O próximo incremento é identidade persistida com cadastro exclusivamente CUSTOMER, hashing de senhas, provisionamento interno ADMIN e autorização no backend antes de habilitar mutações administrativas. SSO, WhatsApp, arquivamento automático, object storage e implantação de produção estão adiados. Folha de pagamento, ERP, contabilidade, franquias e isolamento de tenants estão excluídos.

## Licença

Júnio Rosa · [MIT](LICENSE)
