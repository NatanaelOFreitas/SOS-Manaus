import { Injectable } from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { CredenciaisInvalidasErro } from '../entidade/erros';
import { Canal } from '../entidade/perfil';
import { UsuarioPublico } from '../entidade/usuario';
import { UsuarioRepositorio } from '../entidade/usuario.repositorio';
import { CifradorSenha } from './portas/cifrador-senha';
import { EmissorToken } from './portas/emissor-token';

export interface ComandoAutenticar {
  email: string;
  senha: string;
  canal: Canal;
}

export interface ResultadoAutenticacao {
  accessToken: string;
  usuario: UsuarioPublico;
}

/**
 * CONTROLE do caso de uso "Fazer login" (RFA_02).
 *
 * Coordena o fluxo: busca a entidade, confere a senha, aplica as regras da
 * entidade e emite o token. Não conhece HTTP (isso é da fronteira) nem
 * Prisma/bcrypt/JWT (isso é da infraestrutura, acessada pelas portas).
 */
@Injectable()
export class AutenticarUsuarioControle {
  /** Hash usado quando o e-mail não existe, para o tempo de resposta não denunciar isso. */
  private readonly hashFicticio: Promise<string>;

  constructor(
    private readonly usuarios: UsuarioRepositorio,
    private readonly cifrador: CifradorSenha,
    private readonly emissor: EmissorToken,
  ) {
    this.hashFicticio = this.cifrador.cifrar(randomUUID());
  }

  async executar(comando: ComandoAutenticar): Promise<ResultadoAutenticacao> {
    const email = comando.email.trim().toLowerCase();
    const usuario = await this.usuarios.buscarPorEmail(email);

    const hash = usuario?.senhaHash ?? (await this.hashFicticio);
    const senhaConfere = await this.cifrador.comparar(comando.senha, hash);
    if (!usuario || !senhaConfere) {
      throw new CredenciaisInvalidasErro();
    }

    usuario.garantirAcessoAo(comando.canal); // regra da entidade

    const accessToken = await this.emissor.emitir({ sub: usuario.id, perfil: usuario.perfil });
    return { accessToken, usuario: usuario.paraPublico() };
  }
}
