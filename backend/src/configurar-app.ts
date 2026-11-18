import { INestApplication, ValidationPipe } from '@nestjs/common';
import { ErroDominioFilter } from './compartilhado/fronteira/erro-dominio.filter';

/**
 * Configuração da fronteira HTTP, compartilhada entre a aplicação real
 * (main.ts) e os testes com Supertest, para que os dois se comportem igual.
 */
export function configurarApp(app: INestApplication): INestApplication {
  app.enableCors();
  app.useGlobalPipes(
    new ValidationPipe({ whitelist: true, forbidNonWhitelisted: true, transform: true }),
  );
  app.useGlobalFilters(new ErroDominioFilter());
  return app;
}
