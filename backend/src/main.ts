import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { configurarApp } from './configurar-app';

async function bootstrap() {
  const app = configurarApp(await NestFactory.create(AppModule));

  const porta = Number(process.env.PORT ?? 3000);
  // 0.0.0.0: aceita conexões de fora do container (app Android, painel web)
  await app.listen(porta, '0.0.0.0');
  console.log(`API SOS Manaus rodando na porta ${porta}`);
}
bootstrap();
