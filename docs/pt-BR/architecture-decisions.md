# Decisões de Arquitetura e Trade-offs

[English](../architecture-decisions.md) | [Português](architecture-decisions.md)

Este documento explica o catálogo atual de uma única padaria e sua evolução pretendida para e-commerce. Afirmações sobre implementação vêm do código, migrações, configuração e testes. Quando não há intenção registrada, o raciocínio avalia a arquitetura atual, sem presumir motivação histórica. Alternativas são opções de revisão, não prova de uma avaliação anterior.

## Estado da implementação

- **Implementado:** catálogo Spring Boot, schema PostgreSQL, storefront Angular, segurança HTTP que nega por padrão, Docker de desenvolvimento e testes de catálogo/pricing.
- **Projetado / preparado arquiteturalmente:** vocabulário CUSTOMER/ADMIN e política de compra executável. Isso não equivale a identidade persistida, fidelidade ou checkout.
- **Planejado / trabalho futuro:** login, provisionamento ADMIN controlado, escritas administrativas, pedidos, uploads, promoções, fidelidade por pedidos pagos, Stripe de teste e notificações. Veja o [roadmap](roadmap.md).

## Decisão: Uma padaria em um monólito modular

**Contexto.** O produto explora uma vitrine de padaria, não onboarding de comerciantes ou gestão de franquias.

**Decisão e justificativa.** Uma aplicação Spring Boot contém catálogo, vocabulário de identidade e pricing. Produtos não têm propriedade por tenant. Consultas e transações futuras de pedidos ficam na mesma fronteira de aplicação/banco; microsserviços adicionariam falhas de rede e coordenação antes de haver fluxos independentes para implantar.

**Alternativas.** SaaS multi-tenant exigiria chaves, autorização e testes de isolamento por tenant. Serviços independentes de catálogo/pedidos exigiriam contratos e consistência entre serviços.

**Trade-offs e consequências.** Deploy/debug ficam mais simples, mas fronteiras de pacotes são convenções, não serviços isolados. Catálogo separa controller, aplicação, entidade e repositório JPA; o serviço importa diretamente o repositório de infraestrutura, sem inversão de dependência estrita. Folha, ERP e contabilidade estão excluídos. Não há broker ou processamento em background.

**Reavaliar quando.** Módulos com responsáveis independentes precisarem de releases separados ou carga medida justificar escala independente. Multi-loja exige mudança explícita de escopo e isolamento de dados.

**Evidências:** [CatalogService](../../backend/src/main/java/com/aurorabakery/catalog/application/CatalogService.java), [schema V2](../../backend/src/main/resources/db/migration/V2__single_bakery_catalog.sql), [visão de domínio](architecture.md).

## Decisão: Java e Spring Boot para fronteiras tipadas

**Contexto.** Dados do catálogo e regras monetárias futuras precisam de contratos explícitos e comportamento testável no servidor.

**Decisão e justificativa.** Records Java expressam DTOs, enums restringem disponibilidade/papéis e `BigDecimal` torna arredondamento explícito. MVC, injeção de dependências, Security, JPA, Flyway e Actuator integram HTTP, persistência e operação. O benefício atual aparece no controller fino e serviço transacional, sem alegação de desempenho medido ou motivação original da linguagem.

**Alternativas.** Python/FastAPI ou Go poderiam atender o catálogo com outra integração de persistência/segurança. Spring JDBC tornaria SQL mais explícito que JPA.

**Trade-offs e consequências.** Configuração de framework e Java 25 são necessários mesmo para uma API pequena. Anotações ORM acoplam Product à persistência; anotações Spring aparecem na política de preços. Fronteiras lógicas existem, mas não são independentes do framework. DTOs explícitos evitam expor entidades e permitem evoluir o contrato público separadamente.

**Reavaliar quando.** Custo do framework dificultar implantação ou consultas justificarem SQL explícito. Primeiro medir a carga real.

**Evidências:** [pom.xml](../../backend/pom.xml), [ProductSummary](../../backend/src/main/java/com/aurorabakery/catalog/application/ProductSummary.java), [PurchasePolicy](../../backend/src/main/java/com/aurorabakery/pricing/domain/PurchasePolicy.java).

## Decisão: PostgreSQL com evolução versionada de schema

**Contexto.** O catálogo precisa de slugs únicos, estados válidos e preços decimais positivos. Clientes/pedidos futuros são relacionais, mas suas tabelas ainda não existem.

**Decisão e justificativa.** PostgreSQL impõe checks/unicidade; Flyway controla evolução e Hibernate valida em vez de gerar tabelas. `open-in-view=false` e mapeamento dentro da transação de leitura mantêm persistência fora da serialização HTTP.

**Alternativas.** Banco embarcado reduz infraestrutura mas não exercita constraints PostgreSQL. Document storage transfere mais validação à aplicação. Atualização automática ORM obscurece histórico de migrações.

**Trade-offs e consequências.** Mudanças exigem migrações explícitas. V1 é imutável; V2 renomeia `stores` para `legacy_stores`, sem excluir dados antigos. Bancos novos também mantêm essa tabela não usada. O catálogo carrega todos os produtos visíveis numa lista, sem paginação/cache; resposta e trabalho crescem com o catálogo.

**Reavaliar quando.** Tamanho justificar paginação/análise de índices ou política de retenção aprovada permitir remover legado. Pedidos precisam de regras de transação/snapshot antes da implementação.

**Evidências:** [migrações](../../backend/src/main/resources/db/migration/), [application.yml](../../backend/src/main/resources/application.yml), [testes PostgreSQL](../../backend/src/test/java/com/aurorabakery/catalog/ProductPostgresIT.java).

## Decisão: Disponibilidade e histórico em vez de inventário

**Contexto.** O domínio documentado modela produção sob demanda, não contabilidade de ingredientes/estoque.

**Decisão e justificativa.** AVAILABLE e TEMPORARILY_UNAVAILABLE ficam públicos; apenas AVAILABLE é marcado comprável. DISCONTINUED e ARCHIVED são excluídos da consulta pública. Disponibilidade diz se o produto pode ser oferecido sem fingir medir estoque físico. Destaque independe de desconto.

**Alternativas.** Reserva de quantidade exige concorrência, liberação/expiração e regras contra overselling. Exclusão física simplifica armazenamento mas perde registros úteis ao histórico futuro.

**Trade-offs e consequências.** Indisponibilidade temporária permanece compreensível ao cliente. Linhas são preservadas, mas ainda não há API de mutação, agendador de arquivamento ou referências de pedidos. `purchasable` é informação do catálogo, não enforcement de checkout. A UI não oferece compras.

**Reavaliar quando.** Lotes finitos exigirem reservas ou retenção/lifecycle administrativo estiverem definidos.

**Evidências:** [Product](../../backend/src/main/java/com/aurorabakery/catalog/domain/Product.java), [CatalogService](../../backend/src/main/java/com/aurorabakery/catalog/application/CatalogService.java), [ProductPostgresIT](../../backend/src/test/java/com/aurorabakery/catalog/ProductPostgresIT.java).

## Decisão: Uma aplicação Angular com estados explícitos

**Contexto.** O cliente precisa distinguir lentidão, catálogo vazio e falha backend.

**Decisão e justificativa.** Catálogo lazy em uma aplicação Angular; `ProductApi` cuida de HTTP e um estado discriminado em signal dirige loading/error/ready. Formatação BRL, roles status/alert, skip link e botão de tentativa transformam estado em feedback. `/api` relativo usa o proxy dev sem expor hostnames Docker ao navegador.

**Alternativas.** HTML no servidor reduz ferramentas cliente num catálogo de leitura. Admin separado permite releases independentes mas duplica ferramentas/contratos.

**Trade-offs e consequências.** Angular fornece componentes tipados/testes ao custo de toolchain. Retry faz nova requisição HTTP síncrona; não há cache offline ou retry automático. API retorna metadados de imagem, mas frontend não renderiza imagens. Compartilhar ferramentas storefront/admin é direção planejada; não há rotas/guards admin atuais.

**Reavaliar quando.** Indexação/renderização no servidor for necessária ou admin exigir fronteira independente de release/segurança.

**Evidências:** [UI/testes](../../frontend/src/app/catalog/), [rotas](../../frontend/src/main.ts), [proxy](../../frontend/proxy.conf.cjs).

## Decisão: Negar escritas até implementar identidade

**Contexto.** Enum de papel não autentica usuários nem autoriza administração.

**Decisão e justificativa.** Segurança permite GET de catálogo, saúde não sensível e OpenAPI dev; nega o restante. Origens CORS explícitas e Problem Details genéricos limitam exposição acidental enquanto identidade está incompleta.

**Alternativas.** Escritas públicas temporárias expõem privilégios. Provedor de identidade fornece federação mas adiciona infraestrutura antes de existir login.

**Trade-offs e consequências.** Login e acesso ADMIN reais não existem. Stateless e CSRF desabilitado descrevem a API de leitura, não uma estratégia futura definida. Cookies exigem rever CSRF; bearer exige validação/ciclo de tokens. Exceções são ocultadas do cliente mas registradas no servidor.

**Decisão planejada.** Identidade única persistida CUSTOMER/ADMIN; cadastro público cria apenas CUSTOMER. Provisionamento interno ADMIN deve gerar hash sem credenciais em migrações/seeds. Backend segue autoritativo mesmo com guards frontend.

**Reavaliar quando.** Introduzir qualquer endpoint autenticado/de mutação; escolher sessão/tokens antes de ampliar allowlist.

**Evidências:** [SecurityConfiguration](../../backend/src/main/java/com/aurorabakery/configuration/SecurityConfiguration.java), [testes API](../../backend/src/test/java/com/aurorabakery/catalog/ProductApiTest.java), [roadmap](roadmap.md).

## Decisão: Centralizar política monetária antes do checkout

**Contexto.** Acúmulo de descontos e elegibilidade ADMIN precisam de política determinística única no servidor.

**Decisão e justificativa.** `PurchasePolicy` aplica porcentagem ADMIN configurável (15% padrão), exclui ADMIN de fidelidade/marketing e escolhe o maior desconto elegível de promoção/fidelidade para CUSTOMER. Valida porcentagens e arredonda total a duas casas com HALF_UP. Fora de controllers, regras são testáveis sem HTTP/banco.

**Alternativas.** Cálculo na UI duplica regras e permite adulteração. Acumular descontos muda comportamento comercial e complica explicar totais.

**Trade-offs e consequências.** Papéis/porcentagens recebidos pelo método não são fatos autenticados. Não há persistência de promoções, contagem de pedidos pagos, entrega de marketing ou endpoint de compra. Checkout futuro deve derivar elegibilidade/totais no servidor e salvar snapshots. Consentimento promocional deve ser separado de comunicação transacional.

**Reavaliar quando.** Precedência de promoções, reembolsos, reversão de fidelidade ou arredondamento por item forem requisitos.

**Evidências:** [política](../../backend/src/main/java/com/aurorabakery/pricing/domain/PurchasePolicy.java), [testes](../../backend/src/test/java/com/aurorabakery/pricing/PurchasePolicyTest.java).

## Decisão: Manter storage e pagamento planejados até existir o fluxo

**Contexto.** Catálogo armazena metadados opcionais de caminho de imagem e não tem checkout.

**Decisão planejada e justificativa.** Arquitetura propõe adaptador filesystem local atrás de uma porta e adaptador Stripe de teste atrás de uma porta de pagamento. Local evita cloud numa instância; Stripe test demonstra confirmação do provedor sem dinheiro real. Nenhuma porta/adaptador existe hoje.

**Alternativas.** Object storage compartilha arquivos entre instâncias mas adiciona credenciais/operação. Blobs no banco acoplam volume de arquivos a backup. Sucesso fake na UI é simples mas não confirma pagamento.

**Trade-offs e consequências.** Antes de uploads, definir tamanho/tipo, nomes gerados e proteção traversal. Antes de Stripe, implementar totais server-side, assinatura e idempotência de webhooks. Redirect de sucesso não estabelece PAID. Outbox/entrega assíncrona durável é decisão futura, não componente atual.

**Reavaliar quando.** Uploads/checkout forem implementados; deploy compartilhado exige storage compartilhado/object storage e retries de pagamento exigem recuperação/idempotência.

**Evidências:** [fronteiras planejadas](architecture.md), [roadmap](roadmap.md), [dependências](../../backend/pom.xml).

## Decisão: Docker para reprodutibilidade e verificação em camadas

**Contexto.** Versões Java/Node/PostgreSQL precisam ser reproduzíveis sem toolchains do host.

**Decisão e justificativa.** Compose separa db/backend/frontend numa bridge, usa loopback, volume PostgreSQL nomeado e startup por saúde. Digests e lockfile reduzem drift. Backend e frontend usam usuários não-root. Frontend é servidor dev, não hosting de produção.

**Alternativas.** Setup no host dispensa containers mas exige ferramentas compatíveis; orquestração/proxy de produção resolve outras necessidades.

**Trade-offs e consequências.** Startup executa migrações automaticamente; seed dev opcional insere produtos fictícios. Iniciar sobre dados existentes é operação de escrita. Seed separado/idempotente não substitui migrações. Readiness inclui banco, sem observabilidade completa. Volume persiste após desligamento comum.

**Escolha de testes.** MockMvc com serviço fake valida HTTP/segurança; política direta valida regras; Testcontainers PostgreSQL opt-in valida migrações, constraints, consultas e seed. Fakes HTTP Angular validam estados UI. Fakes não provam banco; integração real custa startup/recursos Docker. Não há testes de checkout/pagamento.

**Reavaliar quando.** Hosting de produção, automação de deploy ou recuperação forem necessários. Manter banco de testes isolado de dados dev.

**Evidências:** [Compose](../../docker-compose.yml), [Docker](docker.md), [testes backend](../../backend/src/test/java/com/aurorabakery/), [testes frontend](../../frontend/src/app/catalog/product-list.spec.ts).
