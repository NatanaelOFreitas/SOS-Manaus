import { Injectable } from '@nestjs/common';
import * as bcrypt from 'bcryptjs';
import { CifradorSenha } from '../controle/portas/cifrador-senha';

/** INFRAESTRUTURA: implementa a porta CifradorSenha com bcrypt (custo 10). */
@Injectable()
export class BcryptCifradorSenha extends CifradorSenha {
  private static readonly CUSTO = 10;

  cifrar(senha: string): Promise<string> {
    return bcrypt.hash(senha, BcryptCifradorSenha.CUSTO);
  }

  comparar(senha: string, hash: string): Promise<boolean> {
    return bcrypt.compare(senha, hash);
  }
}
