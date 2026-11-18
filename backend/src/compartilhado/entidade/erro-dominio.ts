/**
 * Erro de regra de negócio.
 *
 * As entidades e os controles lançam erros de domínio sem saber nada de HTTP.
 * Quem traduz o `codigo` para um status HTTP é a fronteira (ErroDominioFilter).
 */
export abstract class ErroDominio extends Error {
  abstract readonly codigo: string;

  constructor(mensagem: string) {
    super(mensagem);
    this.name = new.target.name;
  }
}
