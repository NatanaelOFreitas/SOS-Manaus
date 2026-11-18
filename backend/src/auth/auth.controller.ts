import { Body, Controller, Get, HttpCode, HttpStatus, Post, Req, UseGuards } from '@nestjs/common';
import { Throttle } from '@nestjs/throttler';
import { Request } from 'express';
import { UsuarioPublico } from '../usuarios/usuarios.service';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { JwtAuthGuard } from './guards/jwt-auth.guard';

@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  /** RFA_02 – login de cidadão e gestor */
  @Post('login')
  @HttpCode(HttpStatus.OK)
  // Proteção contra força bruta: no máximo 5 tentativas por minuto por IP
  @Throttle({ default: { limit: 5, ttl: 60_000 } })
  login(@Body() dto: LoginDto) {
    return this.auth.login(dto);
  }

  /** Retorna o usuário dono do token (usado pelo app para restaurar a sessão) */
  @Get('me')
  @UseGuards(JwtAuthGuard)
  me(@Req() req: Request & { user: UsuarioPublico }) {
    return req.user;
  }
}
