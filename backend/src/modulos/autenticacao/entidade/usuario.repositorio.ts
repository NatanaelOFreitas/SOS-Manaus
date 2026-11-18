import { Usuario } from './usuario';

/**
 * Contrato de persistência da entidade Usuario.
 *
 * Fica na camada de entidade porque descreve COMO as entidades são obtidas,
 * sem dizer com qual tecnologia. A implementação com Prisma está em
 * `infraestrutura/prisma-usuario.repositorio.ts` (inversão de dependência).
 */
export abstract class UsuarioRepositorio {
  abstract buscarPorEmail(email: string): Promise<Usuario | null>;
  abstract buscarPorId(id: string): Promise<Usuario | null>;
}
