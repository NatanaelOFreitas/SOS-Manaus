# SOS Manaus

> Plataforma de Smart City para aproximar cidadãos de Manaus e os setores públicos responsáveis pela manutenção urbana.

## Sobre o projeto

O **SOS Manaus** é uma proposta de aplicativo para registrar, encaminhar e acompanhar ocorrências urbanas. A solução busca tornar mais simples a comunicação com a prefeitura, dando visibilidade a problemas como buracos, falhas na iluminação pública, lixo acumulado, alagamentos, água e esgoto e questões de segurança.

O cidadão registra a ocorrência com foto, categoria, descrição e localização. A plataforma gera um protocolo, encaminha a demanda ao setor responsável e permite acompanhar cada atualização até a resolução. O projeto também prevê um ambiente administrativo para triagem, priorização e gestão das solicitações.

## Status

O repositório atualmente reúne a documentação do **Design Sprint** e o protótipo navegável da proposta. Ainda não há uma implementação de frontend, backend ou banco de dados versionada aqui; portanto, as funcionalidades descritas abaixo representam o escopo planejado do MVP.

## Funcionalidades planejadas

### Para cidadãos

- Cadastro, login ou acesso inicial simplificado;
- Registro de ocorrência com foto, categoria e descrição;
- Captura de localização por GPS, com ajuste do ponto no mapa;
- Geração de protocolo para cada solicitação;
- Acompanhamento de status: `Recebido` → `Em análise` → `Em atendimento` → `Resolvido`;
- Histórico de solicitações e notificações de atualização;
- Confirmação de ocorrências próximas para reduzir duplicidades;
- Avaliação da resolução pelo cidadão.

### Para a prefeitura

- Painel com visão geral das solicitações;
- Lista de ocorrências com filtros por categoria e status;
- Visualização de foto, localização, descrição e protocolo;
- Definição do setor responsável e atualização do andamento;
- Indicadores para apoiar a priorização do atendimento.

## Fluxo principal

```text
Cidadão identifica um problema
        ↓
Registra foto, categoria, descrição e localização
        ↓
Recebe um protocolo e acompanha a solicitação
        ↓
Prefeitura analisa, encaminha e atualiza o status
        ↓
Atendimento é concluído
        ↓
Cidadão confirma se o problema foi resolvido
```

## Protótipo

O protótipo de média/alta fidelidade foi criado no Magic Patterns e cobre os ambientes do cidadão e da prefeitura:

- Cidadão: login/cadastro, início, mapa, registro, solicitações, detalhes e avaliação da resolução;
- Prefeitura: dashboard, lista de ocorrências e detalhes para atualização do atendimento.


## Documentação

O relatório completo do Design Sprint está em [Documentações/SOS_Manaus-Design-Sprint.pdf](Documentações/SOS_Manaus-Design-Sprint.pdf). Ele registra:

- Contexto do problema urbano e personas;
- Ideação, alternativas avaliadas e matriz de decisão;
- Storyboard e requisitos do protótipo;
- Validação inicial do fluxo e oportunidades de melhoria;
- Uso crítico de IA durante o processo;
- Considerações sobre privacidade e conformidade com a LGPD.

## Próximos passos

1. Finalizar os ajustes de interatividade do protótipo;
2. Realizar testes de usabilidade com moradores de Manaus;
3. Definir os requisitos técnicos e desenvolver o MVP funcional;
4. Implementar mecanismos de prevenção a registros duplicados e falsos;
5. Avaliar acessibilidade, segurança e proteção dos dados coletados;
6. Buscar integração e parceria com órgãos públicos responsáveis.

## Equipe

- Natanael de Oliveira Freitas
- Ian Mendonça Cohen
- Rafaela Salgado Matos
- Gabriel Couto Matos

## Contexto acadêmico

Projeto desenvolvido para a disciplina **Modelagem e Projeto de Sistemas** do curso de ECP, com entrega registrada em 24 de agosto de 2026.
