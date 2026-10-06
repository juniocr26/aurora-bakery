# Instalação e execução

## Desenvolvimento com Docker

Instale Docker com Compose v2. Em um clone novo:

```sh
if [ ! -e .env ]; then cp .env.example .env; fi
# Edite .env: use uma senha local de desenvolvimento.
docker compose config --quiet
docker compose up --build -d --wait --wait-timeout 180
```

Em um checkout existente, atualize `.env` conforme as variáveis documentadas, sem sobrescrevê-lo. Aplicação: http://localhost:4200/products. API: http://localhost:8080/api/v1/products. Swagger local: http://localhost:8080/swagger-ui/index.html. Readiness: http://localhost:8080/actuator/health/readiness.

Flyway cria/atualiza o schema na inicialização do backend; Hibernate o valida. `DEV_SEED_ENABLED=true` insere três produtos fictícios, de forma idempotente, apenas no perfil `dev`. Não há seed de contas ou credenciais administrativas. Desabilite a opção para iniciar um catálogo vazio; isso não remove registros existentes.

```sh
docker compose ps
docker compose logs --tail=100
docker compose stop
```



[Testes](testing.md).


[Dependências persistidas no host e recuperação](docker-development-setup.md). A inicialização base executa migrações e seed opcional; use o override de desenvolvimento existente para desabilitar ambos ao preservar um banco inicializado. Nunca execute testes de escrita contra dados da aplicação.
