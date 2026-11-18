import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { UsuarioPublico, UsuariosService } from '../../usuarios/usuarios.service';
import { JwtPayload } from '../auth.service';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(
    config: ConfigService,
    private readonly usuarios: UsuariosService,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: config.getOrThrow<string>('JWT_SECRET'),
    });
  }

  async validate(payload: JwtPayload): Promise<UsuarioPublico> {
    const usuario = await this.usuarios.buscarPorId(payload.sub);
    if (!usuario || !usuario.ativo) {
      throw new UnauthorizedException('Sessão inválida.');
    }
    return UsuariosService.paraPublico(usuario);
  }
}
