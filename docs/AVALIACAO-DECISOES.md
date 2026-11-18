# SOS Manaus: Avaliação das Decisões Arquiteturais

> Documento complementar ao [`ARQUITETURA.md`](ARQUITETURA.md).
> Escopo avaliado: arquitetura geral (4 camadas em containers), organização interna da **API** e do **app Android**, e decisões transversais (autenticação, banco, implantação).

**Sumário**

1. [Método de avaliação](#1-método-de-avaliação)
2. [Direcionadores arquiteturais](#2-direcionadores-arquiteturais)
3. [Árvore de utilidade e cenários de qualidade](#3-árvore-de-utilidade-e-cenários-de-qualidade)
4. [Registros de decisão (ADRs)](#4-registros-de-decisão-adrs)
5. [Matrizes de decisão ponderadas](#5-matrizes-de-decisão-ponderadas)
6. [Análise dos cenários contra a arquitetura](#6-análise-dos-cenários-contra-a-arquitetura)
7. [Pontos de sensibilidade e trade-offs](#7-pontos-de-sensibilidade-e-trade-offs)
8. [Riscos, não-riscos e temas de risco](#8-riscos-não-riscos-e-temas-de-risco)
9. [Atendimento aos requisitos não funcionais](#9-atendimento-aos-requisitos-não-funcionais)
10. [Dívida técnica e próximos passos](#10-dívida-técnica-e-próximos-passos)
11. [Conclusão](#11-conclusão)
12. [Referências](#12-referências)

---

## 1. Método de avaliação

A avaliação combina três técnicas reconhecidas na literatura de arquitetura de software:

| Técnica | Origem | Uso neste documento |
|---|---|---|
| **ADR** (*Architecture Decision Record*) | Michael Nygard (2011) | Registrar cada decisão com contexto, alternativas, decisão e consequências (seção 4) |
| **Avaliação baseada em cenários**, inspirada no **ATAM** | SEI/Carnegie Mellon (Kazman, Klein, Clements) | Árvore de utilidade, cenários de qualidade, pontos de sensibilidade, trade-offs e riscos (seções 3, 6, 7, 8) |
| **Matriz de decisão ponderada** | Análise multicritério | Comparar alternativas de forma explícita e verificável (seção 5) |

**Limitações honestas da avaliação:**

- É uma versão **reduzida** do ATAM: não houve a reunião formal com todos os *stakeholders* (prefeitura, órgãos públicos). Os cenários foram derivados do documento de requisitos e do Design Sprint.
- As notas das matrizes (1 a 5) são **julgamento da equipe**, justificadas linha a linha. Os pesos foram definidos **antes** de dar as notas, para evitar favorecer a opção já escolhida. Quando o resultado contrariou a escolha (autenticação), isso foi mantido e discutido.
- Os atributos de desempenho (RNF01) e disponibilidade (RNF04) ainda **não foram medidos**. A seção 9 indica como serão.

---

## 2. Direcionadores arquiteturais

### 2.1 Requisitos funcionais que mais influenciam a arquitetura

| ID | Requisito | Impacto arquitetural |
|---|---|---|
| RFA_01 / RFA_02 | Cadastro e login de **cidadão e gestor** | Uma API de autenticação única para dois clientes com perfis diferentes → conceito de **canal** |
| RFC_01 / RFC_02 | Ocorrência com **foto** e **localização** (GPS/mapa) | Armazenamento de objetos (S3) + banco **geoespacial** (PostGIS) |
| RFA_04 | Protocolo único por ocorrência | Geração centralizada no servidor (não no cliente) |
| RFA_05 | Notificação de mudança de status | Integração com Firebase Cloud Messaging |
| RFP_01..04 | Gestor visualiza, filtra e atualiza ocorrências | Cliente web separado consumindo a **mesma** API |

### 2.2 Requisitos não funcionais

| ID | Atributo | Exigência |
|---|---|---|
| RNF01 | Desempenho | ≤ 2 s para 95% das requisições, mesmo em picos |
| RNF02 | Usabilidade | Taxa de sucesso ≥ 90% sem treinamento |
| RNF03 | Segurança | 100% das tentativas não autorizadas testadas devem ser bloqueadas |
| RNF04 | Disponibilidade | ≥ 99% ao mês |

### 2.3 Restrições

| Restrição | Tipo | Efeito |
|---|---|---|
| Stack definida pela equipe (Kotlin/Compose, React/Vite, NestJS, PostgreSQL/PostGIS, Prisma, JWT, Docker) | Tecnológica | As decisões avaliam e justificam a stack; não partem do zero |
| Projeto acadêmico com prazo de semestre e custo zero | Prazo/orçamento | Penaliza soluções com alta complexidade operacional |
| RES01: a prefeitura pode não integrar todos os setores | Legal/organizacional | Setores e encaminhamento devem ser **dados configuráveis**, não código fixo |
| LGPD: dados pessoais de cidadãos | Legal | Dados sob controle da própria infraestrutura e acesso restrito por perfil |
| Disciplina de Modelagem e Projeto de Sistemas | Acadêmica | O código deve ser **rastreável** para casos de uso e diagramas UML (robustez, sequência) |

---

## 3. Árvore de utilidade e cenários de qualidade

Prioridade no formato **(Importância para o negócio, Dificuldade técnica)**, com A = alta, M = média e B = baixa.

```
Utilidade
├── Segurança
│   ├── S1 (A,M) Gestor tenta entrar pelo app do cidadão → acesso negado pelo servidor
│   ├── S2 (A,B) Robô testa senhas em massa → bloqueado após 5 tentativas/min por IP
│   ├── S3 (A,M) Conta desativada com token ainda válido → próxima requisição recusada
│   └── S4 (A,B) Cliente comprometido tenta acessar o banco → não há rota de rede
├── Manutenibilidade / Modificabilidade
│   ├── M1 (A,M) Novo caso de uso (Registrar ocorrência) → só adiciona módulo, sem mexer nos existentes
│   └── M2 (M,M) Trocar ORM ou cliente HTTP → só a classe de infraestrutura/fronteira muda
├── Testabilidade
│   └── T1 (A,B) Regras de login testadas sem banco, sem rede e sem emulador
├── Desempenho
│   └── D1 (A,M) 200 cidadãos registrando ocorrências ao mesmo tempo após temporal → p95 ≤ 2 s
├── Disponibilidade
│   └── A1 (A,A) API reiniciada → app mantém a sessão e o cidadão não precisa logar de novo
├── Implantabilidade / Portabilidade
│   └── I1 (M,B) Novo integrante da equipe sobe o sistema inteiro com um comando
└── Usabilidade
    └── U1 (A,B) Cidadão erra o e-mail → recebe o erro na hora, sem esperar o servidor
```

**Cenários detalhados (formato estímulo → resposta → medida):**

| ID | Fonte e estímulo | Ambiente | Resposta esperada | Medida |
|---|---|---|---|---|
| S1 | Gestor faz login pelo app | Operação normal | API recusa com 403 `CANAL_NAO_PERMITIDO` | 100% das tentativas bloqueadas (teste automatizado) |
| S2 | Script envia 100 logins/min de um IP | Ataque | A partir da 6ª tentativa, HTTP 429 | 0 tentativas além do limite processadas |
| S3 | Gestor desativa conta; dono usa o token antigo | Token não expirado | `GET /auth/me` → 401 `SESSAO_INVALIDA` | Bloqueio na 1ª requisição após desativar |
| S4 | Container `front` comprometido tenta `db:5432` | Produção | Conexão impossível (redes separadas) | Sem rota de rede |
| M1 | Equipe implementa UC01 | Desenvolvimento | Novo módulo em fronteira/controle/entidade | 0 arquivos alterados em `autenticacao/` |
| M2 | Trocar Prisma por TypeORM | Manutenção | Só `infraestrutura/` muda | 0 alterações em controle e entidade |
| T1 | Rodar os testes de regra de login | CI | Testes sem banco e sem rede | < 5 s, sem dependência externa |
| D1 | Pico de 200 usuários simultâneos | Após chuva forte | p95 de resposta ≤ 2 s | Teste de carga (k6), **a medir** |
| A1 | API reinicia durante o uso | Implantação | Token salvo continua válido (JWT stateless) | 0 relogins forçados |
| I1 | `docker compose up` em máquina nova | Desenvolvimento | 4 containers saudáveis | 1 comando, sem instalar Node/SDK na máquina |
| U1 | Cidadão digita "maria@" | Uso normal | Mensagem "E-mail inválido." no campo | Imediato, sem chamada de rede |

---

## 4. Registros de decisão (ADRs)

Cada ADR segue: **Contexto → Alternativas → Decisão → Justificativa → Consequências → Riscos e mitigação**.

### ADR-01: Arquitetura cliente-servidor em camadas com API única

- **Status:** Aceita
- **Contexto:** Dois públicos (cidadão e gestor) usam clientes diferentes (Android e web), mas manipulam os **mesmos** dados e regras: ocorrência, status, protocolo.

| Alternativa | Prós | Contras |
|---|---|---|
| **A. Camadas: 2 clientes + 1 API + 1 banco** | Regra de negócio em um só lugar; clientes "magros"; segurança centralizada | API é ponto único de falha |
| B. Cada cliente com seu backend (BFF) | Endpoints sob medida para cada cliente | Duplicação de regras; dois serviços para manter |
| C. BaaS (Firebase/Supabase) acessado direto pelos clientes | Rápido de começar | Regras espalhadas em *security rules*; dados fora do controle da prefeitura (LGPD); dependência de fornecedor; PostGIS limitado |
| D. Microsserviços | Escala independente por domínio | Complexidade operacional desproporcional ao MVP e à equipe |

- **Decisão:** A.
- **Justificativa:** Evita duplicar a regra de negócio (cenários S1 e M1). Uma única API facilita aplicar RNF03 de forma consistente. Microsserviços não se pagam no tamanho atual.
- **Consequências:** (+) consistência das regras; (+) o painel web reaproveita 100% da API do login. (−) API é ponto único de falha (ver ADR-09 e R4); (−) contratos da API precisam ser versionados quando mudarem.
- **Riscos e mitigação:** Se a API cair, nenhum cliente funciona. A mitigação é rodar mais de uma réplica no futuro, o que é viável porque a API é stateless (ADR-07).

### ADR-02: Padrão Fronteira–Controle–Entidade (BCE) com portas

- **Status:** Aceita
- **Contexto:** A disciplina exige rastreabilidade entre casos de uso, diagramas de robustez e código. O sistema vai crescer (10 funcionalidades no MVP), e as regras precisam ser testáveis sem infraestrutura.

| Alternativa | Prós | Contras |
|---|---|---|
| **A. BCE + portas (inversão de dependência)** | Mapeia 1:1 com o diagrama de robustez; regras testáveis isoladamente | Mais arquivos por caso de uso |
| B. MVC tradicional (controller → model ORM) | Simples; é o padrão "de tutorial" | Regras vazam para controllers; entidades acopladas ao ORM; teste exige banco |
| C. Clean/Hexagonal "completa" (use cases, presenters, gateways, mappers em todas as bordas) | Isolamento máximo | Muita cerimônia para o MVP; vocabulário diferente do usado em aula |
| D. Sem padrão definido | Zero custo inicial | Vira "código espaguete" com 10 funcionalidades |

- **Decisão:** A, nos dois projetos (API e app), com a **mesma nomenclatura de pastas**: `fronteira/`, `controle/`, `entidade/`.
- **Justificativa:** Maior nota na matriz 5.2. Cada classe do diagrama de robustez vira uma classe no código com o mesmo estereótipo. As portas trazem o principal benefício da arquitetura hexagonal (testabilidade, troca de tecnologia) sem toda a cerimônia.
- **Consequências:**
  - (+) Testes de entidade e controle rodam sem banco, rede ou emulador (cenário T1). Os testes de entidade e controle da API foram executados e passaram.
  - (+) Trocar Prisma, bcrypt, JWT, Retrofit ou DataStore afeta 1 arquivo cada (M2).
  - (+) A equipe inteira usa o mesmo vocabulário no código, nos diagramas e na apresentação.
  - (−) Um caso de uso simples exige de 4 a 6 arquivos.
  - (−) **Relaxamento consciente:** a fronteira pode *ler* tipos de valor da entidade (exibir `Usuario`, validar com o enum `Canal`), mas nunca chamar regras de negócio nem persistir. A alternativa seria criar classes espelho para cada tipo, dobrando o código sem ganho real.
- **Riscos e mitigação:** Membros da equipe podem colocar regra na fronteira "por pressa". A mitigação é a regra de revisão "fronteira não importa repositório", e no futuro uma checagem automática de dependências (`dependency-cruiser` na API, módulos Gradle separados no app).

### ADR-03: App nativo em Kotlin + Jetpack Compose

- **Status:** Aceita (substituiu React Native + Expo, avaliado na primeira versão)
- **Contexto:** O app depende de câmera, GPS, notificações push e mapa, e o projeto é definido como "aplicativo Android".

| Alternativa | Prós | Contras |
|---|---|---|
| **A. Kotlin + Jetpack Compose** | Linguagem e toolkit oficiais do Android; acesso direto a câmera/GPS/FCM; JUnit; Android Studio | Sem reuso de código com o painel React; só Android |
| B. React Native + Expo | Mesma linguagem do painel e da API (TypeScript) | Camada de ponte; dependência do ciclo de versões do Expo; depuração nativa mais difícil |
| C. Flutter | Bom desempenho; multiplataforma | Dart é uma 4ª linguagem; nenhum reuso com o resto |
| D. PWA | Um só código para web e mobile | Acesso limitado a câmera/GPS em segundo plano; push restrito; experiência inferior |

- **Decisão:** A.
- **Justificativa:** Maior nota na matriz 5.1. O escopo é Android, e os recursos nativos são centrais para o caso de uso principal (foto + GPS).
- **Consequências:** (+) desempenho e acesso nativo; (+) `ViewModel` e `StateFlow` encaixam naturalmente no papel de **controle**. (−) Duas linguagens no projeto (Kotlin + TypeScript): DTOs são declarados nos dois lados; (−) iOS fica fora de escopo.
- **Riscos e mitigação:** Divergência entre DTOs do app e da API. Mitigar com testes de contrato ou com a geração de cliente a partir de uma especificação OpenAPI da API (dívida D3).

### ADR-04: API em Node.js + NestJS (TypeScript)

- **Status:** Aceita
- **Contexto:** É preciso uma API REST com autenticação, validação, injeção de dependência e testes, que a equipe consiga manter.

| Alternativa | Prós | Contras |
|---|---|---|
| **A. NestJS** | Módulos, injeção de dependência nativa (essencial para as portas), validação por decorators, Passport/JWT, testes com Jest | Curva maior que Express; uso intenso de decorators |
| B. Express puro | Mínimo e flexível | Sem estrutura nem DI: a arquitetura dependeria de disciplina manual |
| C. Spring Boot (Java/Kotlin) | Robusto; mesma linguagem do app se for Kotlin | Mais pesado; menor familiaridade estimada da equipe |
| D. Django REST | Produtivo, admin pronto | Python seria a 3ª linguagem; ORM próprio concorre com Prisma |

- **Decisão:** A.
- **Justificativa:** Maior nota na matriz 5.4. O fator decisivo é a **injeção de dependência nativa**: o `autenticacao.module.ts` liga cada porta à sua implementação de forma declarativa, e os testes trocam o repositório com `overrideProvider`.
- **Consequências:** (+) a estrutura modular do Nest casa com "um módulo por caso de uso/domínio"; (+) a API e o painel usam a mesma linguagem. (−) Os decorators do Nest aparecem nas classes de controle (`@Injectable`). É um acoplamento leve e aceito, e os testes de controle instanciam as classes com `new`, sem o Nest.

### ADR-05: PostgreSQL + PostGIS

- **Status:** Aceita
- **Contexto:** Ocorrências têm localização, e o sistema precisa de "ocorrências próximas", mapa de Manaus e filtro por bairro (RFP_03).

| Alternativa | Prós | Contras |
|---|---|---|
| **A. PostgreSQL + PostGIS** | Consultas espaciais nativas (raio, polígono do bairro, índice GiST); relacional e transacional | Exige SQL espacial fora do ORM |
| B. MongoDB (índices geo) | Esquema flexível; geo básico | Sem integridade relacional (usuário ↔ ocorrência ↔ confirmação); geo menos completo |
| C. MySQL (spatial) | Popular | Funções espaciais mais limitadas que PostGIS |
| D. PostgreSQL sem PostGIS (lat/long em colunas numéricas) | Simples | "Próximas de mim" vira cálculo manual, lento e sem índice espacial |

- **Decisão:** A. A extensão já é criada na migração inicial.
- **Consequências:** (+) "Ocorrências num raio de 500 m" vira uma consulta indexada; (+) integridade referencial. (−) O Prisma não tem suporte completo a tipos geométricos (ADR-06).

### ADR-06: Prisma ORM, com SQL espacial isolado na infraestrutura

- **Status:** Aceita
- **Contexto:** Acesso a dados tipado, com migrações versionadas.

| Alternativa | Prós | Contras |
|---|---|---|
| **A. Prisma** | Tipagem gerada do schema; migrações versionadas; legível | Suporte limitado a PostGIS (`Unsupported("geometry")` + `$queryRaw`) |
| B. TypeORM | Suporte a tipos espaciais via decorators | Mais propenso a erros em tempo de execução; entidades do ORM tendem a virar as entidades do domínio (anula o BCE) |
| C. SQL puro (`pg`) | Controle total | Sem tipagem; migrações manuais |

- **Decisão:** A, com a regra: **SQL espacial só dentro de `infraestrutura/`**.
- **Consequências:** (+) O modelo do Prisma **não** é a entidade de domínio. O `PrismaUsuarioRepositorio` converte a linha do banco em `Usuario`, e isso protege o BCE. (−) Consultas espaciais em SQL cru exigem testes de integração específicos.
- **Risco R2:** As consultas `$queryRaw` precisam ser parametrizadas para evitar SQL injection. Mitigação: usar sempre template tag (`Prisma.sql`) e revisar.

### ADR-07: Autenticação com JWT stateless + verificação do usuário a cada requisição

- **Status:** Aceita para o MVP (com caminho de evolução definido)
- **Contexto:** Dois clientes (app e web) precisam se autenticar na mesma API. O app precisa manter a sessão entre aberturas.

| Alternativa | Prós | Contras |
|---|---|---|
| **A. JWT próprio (HS256, 7 dias) + `ObterUsuarioLogadoControle` checando "ativo" a cada requisição** | Simples; stateless (permite réplicas da API); funciona igual em mobile e web | Revogação imediata depende da checagem no banco; sem refresh token |
| B. Sessão no servidor (cookie) | Revogação trivial | Cookies são desconfortáveis no app nativo; estado no servidor dificulta réplicas |
| C. Keycloak / OAuth2 + OIDC | Padrão de mercado; MFA, recuperação de senha e SSO prontos; futura integração com gov.br | Mais um container pesado; curva de aprendizado alta; configuração complexa para o prazo |
| D. Firebase Authentication | Rápido; recuperação de senha pronta | Dados de identidade fora da infraestrutura própria (LGPD); dependência de fornecedor; contradiz o ADR-01 |

- **Decisão:** A para o MVP.
- **Justificativa (e o que a matriz mostrou):** Na matriz 5.3, **Keycloak (4,40) e Firebase (4,05) pontuaram acima do JWT próprio (3,90)**. Mesmo assim, a equipe escolheu o JWT porque:
  1. O critério "simplicidade" teve peso 20%, mas, num projeto de um semestre, a complexidade do Keycloak é quase **eliminatória**: configurar realm, clients e temas consome um tempo que faria falta nas ocorrências, que são o núcleo do produto.
  2. Firebase contraria o ADR-01 (dados sob controle próprio) e o LGPD.
  3. O ponto fraco do JWT (revogação) foi **mitigado no desenho**: o `JwtStrategy` chama `ObterUsuarioLogadoControle` em toda requisição, então desativar a conta derruba a sessão imediatamente (cenário S3), pagando o custo de uma consulta por requisição.
  4. A autenticação está atrás de portas (`EmissorToken`, `CifradorSenha`). Migrar para OIDC no futuro troca a fronteira e a infraestrutura, sem reescrever as regras.
- **Consequências:** (+) API stateless; (+) mesmo fluxo para app e painel. (−) Token de 7 dias guardado no aparelho; (−) a consulta ao banco por requisição reduz parte do ganho do "stateless" (ponto de sensibilidade PS2).
- **Riscos e mitigação:** Vazamento do `JWT_SECRET` permitiria forjar tokens. Mitigação: o segredo só existe no `.env`, e o compose recusa subir sem ele. Evolução: refresh token com expiração curta do access token (dívida D2) e avaliação de Keycloak/gov.br na fase pós-MVP.

### ADR-08: Regra "perfil × canal" na entidade do servidor

- **Status:** Aceita (substituiu uma checagem que existia só no app)
- **Contexto:** Gestor não deve usar o app do cidadão e cidadão não deve usar o painel. Na primeira versão essa checagem estava no app Android.

| Alternativa | Prós | Contras |
|---|---|---|
| A. Checar no cliente | Simples | **Inseguro:** qualquer cliente HTTP (curl, Postman) ignora a regra |
| **B. Cliente informa o `canal`; a entidade `Usuario` decide (`garantirAcessoAo`)** | Regra única, testável, impossível de contornar pelo cliente | O cliente precisa enviar o canal |
| C. Uma rota de login por cliente | Explícito | Duplica o fluxo; a regra fica espalhada nas rotas |

- **Decisão:** B. A tabela `PERFIS_PERMITIDOS_POR_CANAL` fica na camada de entidade.
- **Consequências:** (+) RNF03 atendido no lugar certo, com 3 testes automatizados cobrindo a regra; (+) adicionar um novo canal (ex.: app do órgão público) é uma linha na tabela. (−) Um cliente malicioso pode *mentir* o canal, mas só conseguiria entrar no canal do próprio perfil. A permissão em cada rota futura deve checar o **perfil do token**, não o canal (registrado como R5).

### ADR-09: Um container por camada no mesmo Docker Compose, com redes segregadas

- **Status:** Aceita
- **Contexto:** A equipe precisa de um ambiente igual em todas as máquinas, e a disciplina pede as camadas separadas e executáveis.

| Alternativa | Prós | Contras |
|---|---|---|
| **A. Docker Compose, 4 serviços, 2 redes** | Um comando sobe tudo; camadas isoladas; reprodutível | Build do container mobile é pesado (Android SDK) |
| B. Instalar tudo localmente | Nada de Docker | "Na minha máquina funciona"; versões diferentes |
| C. Kubernetes | Escala e alta disponibilidade | Complexidade sem necessidade no MVP |
| D. Rodar o emulador Android dentro do container | "App rodando" no container | Exige virtualização aninhada (KVM), que normalmente não existe no Docker Desktop com WSL2 do Windows; muito pesado |

- **Decisão:** A, com o container `mobile` como **ambiente de build, teste e distribuição do APK** (não D).
- **Justificativa:** Atende ao cenário I1 (um comando). A separação de redes atende ao S4. O build do APK em container garante que qualquer pessoa gere o **mesmo** APK, com os testes rodando antes.
- **Consequências:** (+) `db` sem porta exposta e fora da rede dos clientes; (+) healthchecks garantem a ordem de subida. (−) O primeiro build do container `mobile` baixa vários GB (SDK + dependências Gradle) e pode levar dezenas de minutos; (−) o endereço da API vai **embutido no APK** no build (`SOS_API_URL`), então mudar de rede exige rebuild.
- **Riscos e mitigação:** O celular não resolve o nome `api` do Docker. Por isso o `.env` define o IP do PC (documentado no README).

### ADR-10: Fotos em armazenamento S3-compatible (decisão futura, já registrada)

- **Status:** Proposta (próxima etapa, UC01)
- **Contexto:** Fotos não devem ir para o banco (tamanho e desempenho, RNF01).
- **Decisão proposta:** Container **MinIO** (compatível com S3) no compose, atrás de uma porta `ArmazenadorFotos` no controle. O banco guarda só a chave do objeto. Em produção, troca para um S3 real sem mudar o controle.
- **Consequência:** O padrão de portas do ADR-02 é o que torna essa troca barata.

### ADR-11: Notificações por Firebase Cloud Messaging (decisão futura, já registrada)

- **Status:** Proposta (RFA_05)
- **Decisão proposta:** A API envia pelo FCM através de uma porta `Notificador`, e o app registra o *device token*. É a única dependência de serviço externo aceita, porque não existe alternativa viável para push no Android sem os serviços do Google.
- **Risco:** Dependência de fornecedor. Como fica isolada atrás da porta, o impacto de uma troca se limita à infraestrutura.

---

## 5. Matrizes de decisão ponderadas

Notas de 1 (péssimo) a 5 (excelente). **Total = Σ (peso × nota)**. Os pesos foram definidos antes das notas.

### 5.1 Tecnologia do app mobile (ADR-03)

| Critério | Peso | Kotlin + Compose | React Native | Flutter | PWA |
|---|---|---|---|---|---|
| Desempenho e acesso nativo (câmera, GPS, FCM) | 25% | 5 | 4 | 4 | 2 |
| Alinhamento com "aplicativo Android" | 15% | 5 | 4 | 4 | 2 |
| Curva de aprendizado da equipe | 20% | 3 | 4 | 2 | 5 |
| Reuso de código com o painel web | 10% | 1 | 4 | 2 | 5 |
| Ferramentas e testes (Android Studio, JUnit) | 15% | 5 | 3 | 4 | 3 |
| Longevidade e suporte oficial | 15% | 5 | 4 | 4 | 3 |
| **Total** | 100% | **4,20** | 3,85 | 3,40 | 3,20 |

### 5.2 Padrão de organização do código (ADR-02)

| Critério | Peso | BCE + portas | MVC tradicional | Clean completa | Sem padrão |
|---|---|---|---|---|---|
| Separação de responsabilidades | 25% | 5 | 3 | 5 | 1 |
| Testabilidade sem infraestrutura | 25% | 5 | 2 | 5 | 1 |
| Rastreabilidade com casos de uso/UML | 20% | 5 | 3 | 3 | 1 |
| Simplicidade / curva | 15% | 3 | 5 | 2 | 5 |
| Pouco código extra | 15% | 3 | 5 | 2 | 5 |
| **Total** | 100% | **4,40** | 3,35 | 3,70 | 2,20 |

### 5.3 Mecanismo de autenticação (ADR-07)

| Critério | Peso | JWT + checagem | Sessão no servidor | Keycloak / OIDC | Firebase Auth |
|---|---|---|---|---|---|
| Segurança adequada ao contexto | 30% | 4 | 4 | 5 | 4 |
| Stateless / vários clientes | 20% | 5 | 3 | 5 | 5 |
| Simplicidade de implementação | 20% | 4 | 4 | 2 | 4 |
| Revogação de sessão | 15% | 3 | 5 | 5 | 4 |
| Integração futura (SSO / gov.br) | 15% | 3 | 2 | 5 | 3 |
| **Total** | 100% | 3,90 | 3,65 | **4,40** | 4,05 |

> **Leitura crítica:** o resultado **não** favorece a opção escolhida. Isso foi mantido de propósito: a matriz não captura duas restrições que são eliminatórias no contexto (prazo do semestre e dados de identidade sob controle próprio, por causa da LGPD). A justificativa completa está no ADR-07, e o Keycloak fica registrado como evolução.

### 5.4 Framework da API (ADR-04)

| Critério | Peso | NestJS | Express puro | Spring Boot | Django REST |
|---|---|---|---|---|---|
| Produtividade | 25% | 4 | 4 | 3 | 4 |
| Estrutura e injeção de dependência | 25% | 5 | 2 | 5 | 4 |
| Tipagem estática | 15% | 5 | 3 | 5 | 2 |
| Ecossistema (JWT, validação, testes) | 15% | 4 | 5 | 5 | 4 |
| Familiaridade da equipe | 20% | 4 | 4 | 2 | 3 |
| **Total** | 100% | **4,40** | 3,50 | 3,90 | 3,50 |

---

## 6. Análise dos cenários contra a arquitetura

| Cenário | Decisões envolvidas | Como a arquitetura responde | Situação |
|---|---|---|---|
| **S1** Gestor no app | ADR-08, ADR-02 | `Usuario.garantirAcessoAo(APP_CIDADAO)` lança `CanalNaoPermitidoErro`, e o filtro responde 403 | ✅ Implementado e testado (entidade, controle e HTTP) |
| **S2** Força bruta | ADR-04 | `@Throttle` de 5/min no login + limite global de 60/min | ✅ Implementado (teste de carga pendente) |
| **S3** Conta desativada com token válido | ADR-07 | `JwtStrategy → ObterUsuarioLogadoControle → podeManterSessao()` | ✅ Implementado e testado no controle |
| **S4** Cliente comprometido | ADR-09 | `db` só na `rede_dados`, sem porta publicada | ✅ Configurado e validado com `docker compose config` |
| **M1** Novo caso de uso | ADR-02 | Novo módulo com as mesmas 3 pastas; nada em `autenticacao/` muda | ✅ Guia na seção 8 do ARQUITETURA.md |
| **M2** Trocar ORM / cliente HTTP | ADR-02, ADR-06 | Só `PrismaUsuarioRepositorio` ou `AutenticacaoApiGateway` mudam | ✅ Garantido pelas portas |
| **T1** Testes sem infraestrutura | ADR-02 | Repositório em memória e portas falsas (API e app) | ✅ Testes de entidade e controle da API executados: 10/10 passaram |
| **D1** Pico de 200 usuários | ADR-01, ADR-05, ADR-07 | API stateless permite réplicas; PostGIS com índice espacial; fotos fora do banco | ⚠️ **Não medido.** Risco R1 |
| **A1** API reinicia | ADR-07 | JWT continua válido após o restart; o app só apaga o token se receber 401 | ✅ Pelo desenho; teste manual pendente |
| **I1** Subir tudo com 1 comando | ADR-09 | `docker compose up -d --build` | ✅ Compose validado; build completo a confirmar na máquina da equipe |
| **U1** Erro imediato no e-mail | ADR-02 | `Credenciais.validar()` no app, antes da rede | ✅ Implementado e testado |

---

## 7. Pontos de sensibilidade e trade-offs

**Ponto de sensibilidade** é um parâmetro da arquitetura que afeta fortemente um atributo de qualidade. **Trade-off** é um ponto que afeta dois atributos em direções opostas.

| ID | Ponto | Afeta | Tipo |
|---|---|---|---|
| PS1 | Limite de tentativas de login (5/min por IP) | Segurança ↑ × Usabilidade ↓ (vários cidadãos atrás do mesmo IP, como Wi-Fi público ou rede 4G com NAT, podem ser bloqueados juntos) | **Trade-off** |
| PS2 | Consultar o usuário no banco a cada requisição autenticada | Segurança ↑ (revogação imediata) × Desempenho ↓ (1 consulta extra por requisição) | **Trade-off** |
| PS3 | Validade do JWT (7 dias) | Usabilidade ↑ (não pede login toda hora) × Segurança ↓ (token roubado vale mais tempo) | **Trade-off** |
| PS4 | Custo do bcrypt (10) | Segurança ↑ × Desempenho ↓ (~50–100 ms por login) | **Trade-off** |
| PS5 | Número de arquivos por caso de uso (BCE + portas) | Manutenibilidade/testabilidade ↑ × Produtividade inicial ↓ | **Trade-off** |
| PS6 | URL da API embutida no APK no build | Simplicidade ↑ × Flexibilidade ↓ (trocar de rede exige rebuild) | **Trade-off** |
| PS7 | Índice espacial GiST nas ocorrências | Desempenho das consultas por proximidade | Sensibilidade |
| PS8 | Healthchecks e `depends_on` | Disponibilidade na subida do ambiente | Sensibilidade |

---

## 8. Riscos, não-riscos e temas de risco

### 8.1 Riscos

| ID | Risco | Probabilidade | Impacto | Mitigação |
|---|---|---|---|---|
| R1 | RNF01 (p95 ≤ 2 s) não verificado; upload de fotos pode estourar | Média | Alto | Teste de carga com k6 antes da entrega; fotos via S3 (ADR-10); paginação nas listas |
| R2 | SQL espacial cru (`$queryRaw`) sem parametrização | Baixa | Alto | Usar sempre `Prisma.sql`; revisão de código |
| R3 | API é ponto único de falha (RNF04 99%) | Média | Alto | Política `restart: unless-stopped` (já configurada); réplicas no futuro, viáveis porque a API é stateless |
| R4 | Ninguém roda o build Android em container por causa do tamanho | Média | Médio | O Android Studio continua sendo o caminho do dia a dia; o container é o build oficial |
| R5 | Rotas futuras checarem só o canal e não o perfil do token | Média | Alto | Criar um guard `@Perfis(GESTOR)` baseado no perfil do token já no módulo de ocorrências |
| R6 | DTOs divergentes entre Kotlin e TypeScript | Média | Médio | Contrato OpenAPI gerado pela API + testes de contrato (dívida D3) |
| R7 | Token guardado no DataStore sem criptografia | Baixa | Médio | A área é privada do app; `allowBackup=false`. Evolução: chave no Android Keystore |
| R8 | Tráfego HTTP sem TLS em desenvolvimento | Alta (em dev) | Alto (se for para produção) | `network_security_config` documentado como "só desenvolvimento"; HTTPS obrigatório em produção |

### 8.2 Não-riscos (decisões avaliadas como seguras)

- Senhas com bcrypt e nunca devolvidas pela API (`paraPublico()` testado).
- Mesma mensagem para e-mail inexistente e senha errada, inclusive com o mesmo custo de tempo (hash fictício).
- Banco inacessível pelos clientes (redes separadas).
- Regras de negócio concentradas na camada de entidade da API, sem duplicação nos clientes. O app só valida formato.

### 8.3 Temas de risco

1. **Desempenho e disponibilidade ainda não são medidos** (R1, R3). A arquitetura *permite* atender (stateless, PostGIS, S3), mas não há evidência. Esse é o principal ponto a fechar.
2. **Disciplina da equipe com as camadas** (R5, R6, ADR-02). O padrão só funciona se for seguido. Recomenda-se a checagem automática de dependências.

---

## 9. Atendimento aos requisitos não funcionais

| RNF | Situação | Como a arquitetura atende | Evidência hoje | Como será verificado |
|---|---|---|---|---|
| **RNF01** Desempenho | ⚠️ Parcial | API stateless (escala horizontal), PostGIS indexado, fotos fora do banco, timeouts no app | Nenhuma medição | Teste de carga k6: 200 usuários, p95 ≤ 2 s |
| **RNF02** Usabilidade | ✅ Login | Validação imediata no app; mensagens em português; sessão mantida; layout do protótipo | Testes de `Credenciais` e `LoginControle` | Teste de usabilidade com 5–10 cidadãos (taxa de sucesso ≥ 90%) |
| **RNF03** Segurança | ✅ Login | bcrypt, JWT, limite de tentativas, regra perfil × canal no servidor, revogação por conta inativa, banco isolado, segredo no `.env` | Testes de entidade, controle e HTTP cobrindo 401/403/400 | Checklist OWASP ASVS nível 1 + testes de carga no endpoint de login |
| **RNF04** Disponibilidade | ⚠️ Parcial | Healthchecks, `restart: unless-stopped`, API stateless | Healthcheck configurado | Monitoramento de uptime (ex.: Uptime Kuma) quando houver servidor |

---

## 10. Dívida técnica e próximos passos

| ID | Item | Prioridade | Relacionado a |
|---|---|---|---|
| D1 | Teste de carga (k6) do login e, depois, do registro de ocorrência | Alta | R1, RNF01 |
| D2 | Refresh token + access token curto (15 min) | Média | PS3, ADR-07 |
| D3 | Documentação OpenAPI (Swagger) gerada pela API + testes de contrato | Média | R6 |
| D4 | Guard de autorização por **perfil** (`@Perfis(...)`) | Alta (antes do módulo de ocorrências) | R5 |
| D5 | Checagem automática de dependências entre camadas | Média | ADR-02 |
| D6 | Pipeline GitLab CI rodando `npm test`, `gradlew test` e `docker compose build` | Alta | Stack definida |
| D7 | Token no Android Keystore | Baixa | R7 |
| D8 | Container MinIO + porta `ArmazenadorFotos` | Alta (UC01) | ADR-10 |

---

## 11. Conclusão

A arquitetura proposta, com **quatro camadas em containers e organização Fronteira–Controle–Entidade com portas na API e no app**, atende bem aos atributos que mais importam neste estágio: **segurança** (regras no servidor, testadas em três níveis), **manutenibilidade** e **testabilidade** (as regras de negócio não dependem de framework). Também deixa o caminho preparado para os requisitos ainda não implementados (fotos, mapa, notificações), porque cada nova tecnologia entra por trás de uma porta.

Os pontos fracos são conhecidos e estão registrados: **desempenho e disponibilidade ainda não medidos** e **autenticação própria** em vez de um provedor de identidade. O primeiro é o próximo passo obrigatório (D1). O segundo é um trade-off aceito conscientemente para o prazo do MVP, com plano de migração viável justamente graças às portas.

---

## 12. Referências

- JACOBSON, I. et al. *Object-Oriented Software Engineering: A Use Case Driven Approach*. Addison-Wesley, 1992. Origem do padrão Boundary–Control–Entity.
- ROSENBERG, D.; STEPHENS, M. *Use Case Driven Object Modeling with UML: Theory and Practice*. Apress, 2007. Diagramas de robustez (ICONIX).
- BASS, L.; CLEMENTS, P.; KAZMAN, R. *Software Architecture in Practice*. 4. ed. Addison-Wesley, 2021. Atributos de qualidade, cenários, árvore de utilidade.
- KAZMAN, R.; KLEIN, M.; CLEMENTS, P. *ATAM: Method for Architecture Evaluation*. Technical Report CMU/SEI-2000-TR-004, Software Engineering Institute, 2000.
- NYGARD, M. *Documenting Architecture Decisions*. 2011. Formato ADR.
- MARTIN, R. C. *Clean Architecture*. Prentice Hall, 2017. Princípio da Inversão de Dependência e regra de dependência.
- COCKBURN, A. *Hexagonal Architecture (Ports and Adapters)*. 2005.
- OWASP. *Application Security Verification Standard (ASVS)*.
