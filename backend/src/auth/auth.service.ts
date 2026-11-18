import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcryptjs';
import { UsuarioPublico, UsuariosService } from '../usuarios/usuarios.service';
import { LoginDto } from './dto/login.dto';

export interface JwtPayload {
  sub: string;
  email: string;
  perfil: string;
}

export interface LoginResposta {
  accessToken: string;
  usuario: UsuarioPublico;
}

@Injectable()
export class AuthService {
  constructor(
    private readonly usuarios: UsuariosService,
    private readonly jwt: JwtService,
  ) {}

  async login({ email, senha }: LoginDto): Promise<LoginResposta> {
    const usuario = await this.usuarios.buscarPorEmail(email);

    // Mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem)
    const senhaConfere = usuario ? await bcrypt.compare(senha, usuario.senhaHash) : false;
    if (!usuario || !senhaConfere) {
      throw new UnauthorizedException('E-mail ou senha incorretos.');
    }
    if (!usuario.ativo) {
      throw new UnauthorizedException('Esta conta está desativada.');
    }

    const payload: JwtPayload = { sub: usuario.id, email: usuario.email, perfil: usuario.perfil };
    return {
      accessToken: await this.jwt.signAsync(payload),
      usuario: UsuariosService.paraPublico(usuario),
    };
  }
}
