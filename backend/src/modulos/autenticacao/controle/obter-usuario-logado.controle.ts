import { Injectable } from '@nestjs/common';
import { SessaoInvalidaErro } from '../entidade/erros';
import { UsuarioPublico } from '../entidade/usuario';
import { UsuarioRepositorio } from '../entidade/usuario.repositorio';

/**
 * CONTROLE "Obter usuário logado".
 *
 * Usado a cada requisição autenticada: confirma que o dono do token ainda
 * existe e está ativo. Assim, desativar uma conta derruba as sessões dela
 * sem esperar o token expirar.
 */
@Injectable()
export class ObterUsuarioLogadoControle {
  constructor(private readonly usuarios: UsuarioRepositorio) {}

  async executar(idUsuario: string): Promise<UsuarioPublico> {
    const usuario = await this.usuarios.buscarPorId(idUsuario);
    if (!usuario || !usuario.podeManterSessao()) {
      throw new SessaoInvalidaErro();
    }
    return usuario.paraPublico();
  }
}
