import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { DadosToken, EmissorToken } from '../controle/portas/emissor-token';

/** INFRAESTRUTURA: implementa a porta EmissorToken com JWT assinado (HS256). */
@Injectable()
export class JwtEmissorToken extends EmissorToken {
  constructor(private readonly jwt: JwtService) {
    super();
  }

  emitir(dados: DadosToken): Promise<string> {
    return this.jwt.signAsync({ sub: dados.sub, perfil: dados.perfil });
  }
}
