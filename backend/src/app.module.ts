import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { APP_GUARD } from '@nestjs/core';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { PrismaModule } from './compartilhado/infraestrutura/prisma/prisma.module';
import { AutenticacaoModule } from './modulos/autenticacao/autenticacao.module';
import { SaudeModule } from './modulos/saude/saude.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    // Limite global: 60 requisições por minuto por IP (RNF03)
    ThrottlerModule.forRoot([{ ttl: 60_000, limit: 60 }]),
    PrismaModule,
    SaudeModule,
    AutenticacaoModule,
    // Próximos módulos seguem o mesmo molde: OcorrenciasModule, NotificacoesModule...
  ],
  providers: [{ provide: APP_GUARD, useClass: ThrottlerGuard }],
})
export class AppModule {}
