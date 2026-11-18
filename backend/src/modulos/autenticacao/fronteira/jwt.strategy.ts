import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { DadosToken } from '../controle/portas/emissor-token';
import { ObterUsuarioLogadoControle } from '../controle/obter-usuario-logado.controle';
import { UsuarioPublico } from '../entidade/usuario';

/**
 * FRONTEIRA: lê e verifica o JWT que chega na requisição e pede ao controle
 * o usuário correspondente. O resultado fica disponível em `req.user`.
 */
@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(
    config: ConfigService,
    private readonly obterUsuarioLogado: ObterUsuarioLogadoControle,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: config.getOrThrow<string>('JWT_SECRET'),
    });
  }

  validate(payload: DadosToken): Promise<UsuarioPublico> {
    return this.obterUsuarioLogado.executar(payload.sub);
  }
}
