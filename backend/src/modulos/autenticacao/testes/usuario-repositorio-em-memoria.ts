import { Perfil } from '../entidade/perfil';
import { DadosUsuario, Usuario } from '../entidade/usuario';
import { UsuarioRepositorio } from '../entidade/usuario.repositorio';

/**
 * Implementação do repositório em memória, usada nos testes.
 * Só é possível porque o controle depende da abstração, não do Prisma.
 */
export class UsuarioRepositorioEmMemoria extends UsuarioRepositorio {
  private readonly usuarios = new Map<string, Usuario>();

  adicionar(dados: Partial<DadosUsuario> & Pick<DadosUsuario, 'email' | 'senhaHash'>): Usuario {
    const usuario = new Usuario({
      id: dados.id ?? `u${this.usuarios.size + 1}`,
      nome: dados.nome ?? 'Maria Silva',
      perfil: dados.perfil ?? Perfil.CIDADAO,
      ativo: dados.ativo ?? true,
      criadoEm: dados.criadoEm ?? new Date(),
      email: dados.email,
      senhaHash: dados.senhaHash,
    });
    this.usuarios.set(usuario.id, usuario);
    return usuario;
  }

  async buscarPorEmail(email: string): Promise<Usuario | null> {
    return [...this.usuarios.values()].find((u) => u.email === email.toLowerCase()) ?? null;
  }

  async buscarPorId(id: string): Promise<Usuario | null> {
    return this.usuarios.get(id) ?? null;
  }
}
