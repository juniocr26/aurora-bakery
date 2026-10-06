# Ambiente Docker de desenvolvimento

[English](../en/docker.md) | [Português](docker.md)

Projeto Compose: `aurora-bakery`. Serviços: `db`, `backend`, `frontend`. Compose gera containers `aurora-bakery-db-1`, `aurora-bakery-backend-1`, `aurora-bakery-frontend-1`, rede `aurora-bakery_default`, volume `aurora-bakery_db_data` e imagens `aurora-bakery-backend` / `aurora-bakery-frontend`. Nomes explícitos de containers são desnecessários. PostgreSQL mantém a imagem oficial fixada.

O serviço anterior `postgres` foi renomeado para `db`; nenhum serviço inteiro foi removido, pois os três são necessários. Foram removidas as redes obsoletas `commerce`, `web`, `db_access`, a declaração não usada `postgres_data` e o bind mount legado configurável. Uma rede bridge padrão basta. Portas do host usam loopback. Endereços internos: `db:5432`, `backend:8080`, `frontend:4200`.

Backend espera saúde do banco; frontend espera readiness do backend, que inclui conectividade ao banco. O proxy Angular usa `API_PROXY_TARGET=http://backend:8080`; navegadores chamam `/api` relativo. Todos os serviços mantêm health checks. Backend executa como usuário não-root `aurora`, frontend como `node`. Docker ignores excluem segredos, arquivos gerados e dependências do host. O Dockerfile frontend é de desenvolvimento/build, sem promessa de hospedagem de produção.

## Configuração

| Variável | Finalidade / padrão |
| --- | --- |
| APP_ENV | `dev` no Compose; habilita OpenAPI local e permite seed |
| POSTGRES_DB / POSTGRES_USER | `aurora_bakery` |
| POSTGRES_PASSWORD | Valor local obrigatório; não use o exemplo fora de dev |
| POSTGRES_HOST_PORT | Listener loopback do banco, 5432 |
| BACKEND_HOST_PORT | Listener loopback da API, 8080 |
| FRONTEND_HOST_PORT | Listener loopback Angular, 4200 |
| ALLOWED_ORIGINS | Origens explícitas separadas por vírgula; atualizar ao mudar porta |
| DEV_SEED_ENABLED | Produtos demo opt-in; false no Compose, true no exemplo |
| ADMIN_DISCOUNT_PERCENT | Política backend, 15 por padrão, intervalo 0–100 |
| APP_UID / APP_GID | Usuário backend não-root, 10001 |
| DB_URL | Override para backend no host; Compose fornece URL JDBC com `db` |
| API_PROXY_TARGET | Override frontend no host; Compose fornece hostname backend |

`POSTGRES_DATA_SOURCE` e o identificador Maven cache anteriormente configurável não são mais usados. `.env` permanece ignorado. Não há segredos Stripe ou ADMIN na configuração versionada. Para clientes SQL, use localhost, porta configurada e banco/usuário/senha de `.env`.

Veja [instalação e execução](setup.md) para inicialização, parada e testes via Docker. Edições backend exigem `docker compose up --build -d --wait`; código frontend é montado somente para leitura para desenvolvimento ao vivo. Migrações e bootstrap demo opcional executam automaticamente no startup. Overrides de produção, proxies reversos, Makefiles, scripts Docker e pipelines CI versionados não existiam e não foram introduzidos.

Para dependências persistidas no host e recuperação segura, siga [desenvolvimento Docker](docker-development-setup.md). O override explícito difere do setup base por imagens descrito acima.
