import { Controller, Get } from '@nestjs/common';
import { SkipThrottle } from '@nestjs/throttler';

/** Endpoint técnico usado pelo healthcheck do Docker Compose. */
@Controller('health')
@SkipThrottle()
export class SaudeController {
  @Get()
  verificar() {
    return { status: 'ok', servico: 'sos-manaus-api', horario: new Date().toISOString() };
  }
}
