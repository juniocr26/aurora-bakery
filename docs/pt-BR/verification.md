# Verificação da reestruturação — 2026-10-05

[English](../en/verification.md) | [Português](verification.md)

Este é o registro da verificação anterior da reestruturação, não da revisão documental atual. Foi usado Docker Desktop com Compose v2 em macOS ARM64. Java 17 do host e ausência de Node foram contornados com imagens Java 25/Node 22 do repositório. Dependências fixadas foram preservadas.

| Verificação | Resultado registrado |
| --- | --- |
| `docker compose config --quiet` | PASS |
| `docker compose build` novo | PASS, ambas as imagens |
| `docker compose up --build -d --wait --wait-timeout 180` | PASS, db/backend/frontend saudáveis |
| Volume/rede novos | `aurora-bakery_db_data` e `aurora-bakery_default` |
| Flyway/Hibernate | PASS, V1/V2 aplicadas, schema validado |
| Consulta ao banco | PASS, migrações bem-sucedidas; dois produtos disponíveis e um temporariamente indisponível |
| Readiness com banco | PASS, UP |
| Frontend `/products` | HTTP 200 |
| Proxy Angular `/api/v1/products` | PASS, resposta igual à API com três produtos demo |
| Build produção Angular | PASS, bundle lazy do catálogo |
| Testes Angular | PASS, 4 testes |
| Testes backend | PASS, 9 unidade/API e 6 integração PostgreSQL, sem falhas/skips |
| Logs | Sem erros de startup; restart normal de inicialização PostgreSQL e avisos dev/toolchain |

Não se afirma verificação de renderização completa no navegador, checkout autenticado, pagamento, provisionamento ADMIN ou deploy de produção. Esses fluxos não existem. Catálogo público de leitura não precisa de bootstrap ADMIN. V1 mantém terminologia histórica para integridade da migração.

## Comandos executados na verificação anterior

```sh
docker compose config --quiet
docker compose build
docker compose up --build -d --wait --wait-timeout 180
docker compose ps
docker compose logs --tail=60
docker build --target build -t aurora-bakery-backend-tests ./backend
docker compose run --rm --no-deps frontend npm test
docker compose run --rm --no-deps frontend npm run build
```

Testes backend usaram socket Docker e `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal`, executando `./mvnw -B -ntp verify -Pintegration` na imagem build-stage com fonte final montada somente para leitura. O README documenta reconstrução da imagem para clones novos. Smoke checks SQL/HTTP usaram `docker compose exec -T db` (psql) e `docker compose exec -T frontend` (Node fetch), conferindo migrações, igualdade API/proxy e readiness.

## Revisão documental de arquitetura — 2026-10-05

Esta revisão separada executou a fonte atual dos testes unidade/API na imagem build-stage Java 25 existente, com mount somente leitura: `docker run --rm -v "$PWD/backend/src:/workspace/src:ro" aurora-bakery-backend-tests ./mvnw -B -ntp verify`. Os 9 testes passaram. A tentativa inicial sem rede falhou porque a distribuição Maven Wrapper não estava disponível na imagem; a repetição com downloads habilitados passou. Nesta revisão não foram executados perfil de integração, suíte frontend, browser ou stack Compose. Container descartável removido; banco/volume da aplicação e ambiente real não foram montados. Os resultados completos anteriores acima são históricos.
