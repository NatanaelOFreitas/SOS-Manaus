/** Porta: o controle precisa comparar senhas, mas não sabe que o algoritmo é bcrypt. */
export abstract class CifradorSenha {
  abstract cifrar(senha: string): Promise<string>;
  abstract comparar(senha: string, hash: string): Promise<boolean>;
}
