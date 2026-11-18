import { Body, Controller, Get, HttpCode, HttpStatus, Post, Req, UseGuards } from '@nestjs/common';
import { Throttle } from '@nestjs/throttler';
import { Request } from 'express';
import { AutenticarUsuarioControle } from '../controle/autenticar-usuario.controle';
import { UsuarioPublico } from '../entidade/usuario';
import { LoginDto } from './dto/login.dto';
import { JwtAuthGuard } from './jwt-auth.guard';

/**
 * FRONTEIRA HTTP da autenticação.
 *
 * Recebe a requisição, valida o formato (LoginDto) e repassa ao controle.
 * Não contém regra de negócio e não acessa entidades nem o banco diretamente.
 */
@Controller('auth')
export class AutenticacaoController {
  constructor(private readonly autenticarUsuario: AutenticarUsuarioControle) {}

  /** RFA_02: login de cidadão (app) e gestor (painel). */
  @Post('login')
  @HttpCode(HttpStatus.OK)
  // Proteção contra força bruta: no máximo 5 tentativas por minuto por IP (RNF03)
  @Throttle({ default: { limit: 5, ttl: 60_000 } })
  login(@Body() dto: LoginDto) {
    return this.autenticarUsuario.executar(dto);
  }

  /** Devolve o dono do token. O app usa para restaurar a sessão. */
  @Get('me')
  @UseGuards(JwtAuthGuard)
  me(@Req() req: Request & { user: UsuarioPublico }): UsuarioPublico {
    return req.user;
  }
}
