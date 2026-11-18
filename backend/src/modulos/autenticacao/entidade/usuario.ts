import { CanalNaoPermitidoErro, UsuarioInativoErro } from './erros';
import { Canal, Perfil, PERFIS_PERMITIDOS_POR_CANAL } from './perfil';

export interface DadosUsuario {
  id: string;
  nome: string;
  email: string;
  senhaHash: string;
  perfil: Perfil;
  ativo: boolean;
  criadoEm: Date;
}

/** Visão do usuário que pode sair do sistema (nunca inclui o hash da senha). */
export interface UsuarioPublico {
  id: string;
  nome: string;
  email: string;
  perfil: Perfil;
}

/**
 * ENTIDADE Usuario.
 *
 * Guarda os dados e as regras de negócio do usuário. Não conhece banco,
 * HTTP, NestJS, bcrypt nem JWT.
 */
export class Usuario {
  constructor(private readonly dados: DadosUsuario) {}

  get id(): string {
    return this.dados.id;
  }

  get email(): string {
    return this.dados.email;
  }

  get perfil(): Perfil {
    return this.dados.perfil;
  }

  get senhaHash(): string {
    return this.dados.senhaHash;
  }

  get ativo(): boolean {
    return this.dados.ativo;
  }

  /**
   * Regra RFA_02 + RNF03: o usuário só entra se estiver ativo e se o seu
   * perfil for permitido no canal usado (cidadão no app, gestor no painel).
   */
  garantirAcessoAo(canal: Canal): void {
    if (!this.dados.ativo) {
      throw new UsuarioInativoErro();
    }
    if (!PERFIS_PERMITIDOS_POR_CANAL[canal].includes(this.dados.perfil)) {
      throw new CanalNaoPermitidoErro(canal);
    }
  }

  /** Uma sessão já aberta continua válida enquanto a conta estiver ativa. */
  podeManterSessao(): boolean {
    return this.dados.ativo;
  }

  paraPublico(): UsuarioPublico {
    const { id, nome, email, perfil } = this.dados;
    return { id, nome, email, perfil };
  }
}
