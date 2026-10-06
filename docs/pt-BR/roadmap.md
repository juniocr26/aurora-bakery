# Escopo e estado da implementação

[English](../en/roadmap.md) | [Português](roadmap.md)

## Concluído na reestruturação

Identidade de produto de uma única padaria, migração/API de leitura do catálogo, catálogo Angular, disponibilidade/destaques, política independente de preço/consentimento, testes de catálogo/segurança/pricing, Docker de desenvolvimento simplificado e documentação.

## Incrementos futuros necessários

1. Identidade compartilhada persistida, cadastro/autenticação CUSTOMER seguros, telefone opcional, preferências de comunicação, provisionamento interno ADMIN e autorização por papel no backend.
2. Mutações administrativas do catálogo, porta de storage local segura e transições de produtos; rotas administrativas lazy na mesma aplicação Angular.
3. Regras persistidas de promoção por datas e fidelidade configurável, gestão administrativa e testes de serviço.
4. Carrinho/checkout, snapshots históricos de pedidos com preços calculados pelo servidor e transições validadas.
5. Adaptador Stripe de teste, webhooks idempotentes com assinatura verificada, progressão de fidelidade por pedidos pagos e notificações transacionais.
6. Histórico de pedidos, interface de fidelidade, enforcement de consentimento e operações de pedidos autorizadas.

Esses fluxos não existiam no repositório original, que continha um diretório somente para leitura. Exigem implementação nova, não apenas remoção de tenant. Não são apresentados como implementados. Testes ADMIN exercitam somente a política; ainda não há compras reais ADMIN. Não se afirma haver testes de autenticação, fidelidade por pagamento, Stripe ou endpoints administrativos. A reestruturação estabelece a base reutilizável para evolução incremental.

## Adiado intencionalmente

Agendamento automático de DISCONTINUED para ARCHIVED (idade configurável), object storage, WhatsApp, SSO, hospedagem frontend de produção e entrega confiável em background. Serviços extras não se justificam hoje. Folha, RH, ERP, contabilidade, isolamento de tenants, onboarding de comerciantes e franquias ficam fora do escopo atual.
