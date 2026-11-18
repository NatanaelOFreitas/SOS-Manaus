import { Perfil } from '../../entidade/perfil';

export interface DadosToken {
  sub: string;
  perfil: Perfil;
}

/** Porta: o controle precisa emitir um token de sessão, mas não sabe que é JWT. */
export abstract class EmissorToken {
  abstract emitir(dados: DadosToken): Promise<string>;
}
