import { ErroDominio } from '../../../compartilhado/entidade/erro-dominio';
import { Canal } from './perfil';

export class CredenciaisInvalidasErro extends ErroDominio {
  readonly codigo = 'CREDENCIAIS_INVALIDAS';
  constructor() {
    // Mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem)
    super('E-mail ou senha incorretos.');
  }
}

export class UsuarioInativoErro extends ErroDominio {
  readonly codigo = 'USUARIO_INATIVO';
  constructor() {
    super('Esta conta está desativada.');
  }
}

export class CanalNaoPermitidoErro extends ErroDominio {
  readonly codigo = 'CANAL_NAO_PERMITIDO';
  constructor(canal: Canal) {
    super(
      canal === Canal.APP_CIDADAO
        ? 'Esta conta é de gestor. Acesse pelo painel da prefeitura.'
        : 'Esta conta não tem acesso ao painel da prefeitura.',
    );
  }
}

export class SessaoInvalidaErro extends ErroDominio {
  readonly codigo = 'SESSAO_INVALIDA';
  constructor() {
    super('Sessão inválida. Entre novamente.');
  }
}
