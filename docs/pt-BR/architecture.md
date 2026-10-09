# Arquitetura e domínio

> Escopo histórico de comércio. Payment Reconciliation Lab remove a política de compra e o seed de catálogo. Reconciliação e Stripe seguem planejados; infraestrutura atual e transição em [Docker](docker.md).


[English](../en/architecture.md) | [Português](architecture.md)

Aurora Bakery representa uma única padaria. Um monólito modular Spring Boot expõe DTOs explícitos para uma aplicação Angular por `/api/v1/products`. O proxy de desenvolvimento encaminha `/api/**` ao backend; as URLs do navegador permanecem relativas. PostgreSQL armazena os dados. Não há seleção de loja ou autorização por loja.

## Módulos implementados

O catálogo separa API, aplicação, domínio e persistência. Produtos têm slug, nome, descrição, preço BRL, categoria, referência relativa opcional de imagem, destaque, disponibilidade e timestamps. Destaque independe de desconto. A consulta pública inclui AVAILABLE e TEMPORARILY_UNAVAILABLE, ordenados por destaque e nome. Apenas AVAILABLE é comprável. DISCONTINUED e ARCHIVED ficam ocultos, com registros preservados. Não há API de exclusão ou quantidade em estoque.

Identidade define apenas o vocabulário CUSTOMER/ADMIN; ainda não há contas persistidas ou login. Pricing contém uma política de compra de backend: desconto ADMIN configurável (15% por padrão), exclusão de fidelidade/marketing independentemente de consentimento, sem acumular benefícios. Clientes recebem a maior porcentagem elegível de promoção/fidelidade. A política está testada, mas não conectada a uma compra. O checkout futuro deve resolver identidade e elegibilidade no servidor; nunca confiar em papel ou descontos enviados pelo cliente.

A segurança permite leituras públicas do catálogo, saúde sem dados sensíveis e OpenAPI apenas em dev; rotas de mutação ou não previstas são negadas. Não há atribuição pública de papéis ou campos de senha. A configuração stateless/CSRF desabilitado é adequada apenas à API atual de leitura; deve ser revista ao escolher sessão ou bearer token. CORS exige origens explícitas. Falhas da API não expõem detalhes de exceção.

## Evolução do banco

V1 permanece inalterada para preservar checksums Flyway. V2 renomeia a tabela do antigo diretório para `legacy_stores`, preservando dados históricos, e cria products para uma única padaria. Linhas legadas não são expostas nem usadas pela aplicação. Um banco novo aplica ambas as migrações. Fixtures de desenvolvimento são separadas do schema. Hibernate valida, sem gerar tabelas.

## Ciclo de vida e fronteiras planejados

Persistir uma identidade com papéis CUSTOMER/ADMIN. Cadastro público cria apenas CUSTOMER e valida nome, email, senha e CPF; telefone e consentimento de marketing são opcionais. Provisionar ADMIN por comando interno explícito, com hash de senha fornecida pelo ambiente, sem alterar papéis existentes silenciosamente. Sem cadastro público ADMIN. ADMIN pode comprar segundo a política e receber mensagens transacionais sobre seus pedidos.

Pedidos futuros guardarão snapshots do nome/preço do produto e descontos aplicados. Ciclo proposto: AWAITING_PAYMENT → PAID → PREPARING → READY → COMPLETED, com CANCELLED/PAYMENT_FAILED e transições validadas. Fidelidade deve contar pedidos pagos/concluídos verificados uma única vez, nunca pagamentos cancelados/falhos. Reembolsos e reversões precisam ser definidos antes da implementação.

Pagamentos Stripe de teste ficam atrás de uma porta; webhooks com assinatura verificada, idempotência e totais definidos pelo servidor estabelecem o estado. Hoje não há dependência, chave, endpoint ou pagamento fake Stripe. Marketing exige consentimento CUSTOMER; WhatsApp exige telefone opcional e opt-in explícito. Comunicação transacional tem finalidade/preferências separadas. Notificações podem começar no nível da aplicação; confiabilidade deve determinar a necessidade futura de outbox durável.

Uploads futuros usarão porta de storage com implementação local, metadados relativos, nomes gerados, limites de tamanho/tipo e proteção contra traversal. Ainda não existe endpoint de upload. Object storage é preferível para produção horizontal. Arquivamento pode usar agendamento Spring e idade configurável sem serviços extras.

Veja [decisões e trade-offs](architecture-decisions.md).
