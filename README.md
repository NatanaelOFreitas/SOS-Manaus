# SOS Manaus

Plataforma para cidadãos de Manaus registrarem problemas urbanos com foto e localização, e para gestores públicos acompanharem e resolverem essas ocorrências.

**Etapa atual:** login do cidadão (RFA_02) de ponta a ponta, com a arquitetura organizada em camadas e no padrão Fronteira → Controle → Entidade.

| Documento | Conteúdo |
|---|---|
| [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md) | Camadas, containers, redes, padrão BCE na API e no app, diagramas de robustez e de sequência |
| [`docs/AVALIACAO-DECISOES.md`](docs/AVALIACAO-DECISOES.md) | 11 ADRs, matrizes de decisão ponderadas, cenários de qualidade, trade-offs, riscos e atendimento aos RNFs |

## Stack

| Camada | Container | Tecnologia |
|---|---|---|
| Mobile (cidadão) | `mobile` | Kotlin + Jetpack Compose, testes JUnit |
| Front (prefeitura) | `front` | React + Vite (TypeScript), por enquanto só esqueleto |
| API | `api` | Node.js + NestJS + JWT, Prisma ORM, testes Jest + Supertest |
| Banco | `db` | PostgreSQL + PostGIS |

## Rodar tudo com Docker Compose

Pré-requisito: Docker Desktop.

```powershell
copy .env.example .env      # depois edite JWT_SECRET e, se for usar celular físico, SOS_API_URL
docker compose up -d --build
docker compose ps           # os 4 serviços devem ficar "healthy"/"running"
```

| Serviço | Endereço |
|---|---|
| API | http://localhost:3000/health |
| Painel da prefeitura | http://localhost:5173 |
| Download do APK | http://localhost:8081 |

> O **primeiro** build do container `mobile` baixa o Android SDK e as dependências do Gradle (alguns GB) e pode levar bastante tempo. Ele também roda os testes JUnit: se algum falhar, o build para.
> Para subir só banco + API + painel: `docker compose up -d --build db api front`.

**Instalar o app:** no emulador, arraste o APK baixado para a janela. No celular físico, defina `SOS_API_URL=http://IP_DO_PC:3000/` no `.env`, rode `docker compose up -d --build mobile` e abra `http://IP_DO_PC:8081` no navegador do celular.

## Desenvolvimento sem Docker

**API**
```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d db   # banco com a porta 5432 aberta
cd backend
copy .env.example .env
npm install
npx prisma migrate deploy
npx prisma db seed
npm run start:dev
npm test                    # testes de entidade, controle e fronteira (Supertest)
```

**App Android:** abra a pasta `android/` no Android Studio, ajuste `SOS_API_URL` em `android/gradle.properties` se precisar e clique em **Run ▶**. Para os testes, clique com o botão direito em `app/src/test` → **Run Tests**.

## Usuários de teste (seed)

| Perfil | E-mail | Senha | App Android | Painel |
|---|---|---|---|---|
| Cidadão | cidadao@sosmanaus.com | sos12345 | ✅ entra | ❌ 403 |
| Gestor | gestor@sosmanaus.com | sos12345 | ❌ 403 | ✅ entra |

## API

| Método | Rota | Corpo / cabeçalho | Respostas |
|---|---|---|---|
| POST | `/auth/login` | `{ "email", "senha", "canal": "APP_CIDADAO" \| "PAINEL_PREFEITURA" }` | 200 token + usuário · 400 formato · 401 credenciais · 403 canal/conta inativa · 429 tentativas |
| GET | `/auth/me` | `Authorization: Bearer <token>` | 200 usuário · 401 sessão inválida |
| GET | `/health` | — | 200 |

```powershell
curl -X POST http://localhost:3000/auth/login -H "Content-Type: application/json" -d "{\"email\":\"cidadao@sosmanaus.com\",\"senha\":\"sos12345\",\"canal\":\"APP_CIDADAO\"}"
```
