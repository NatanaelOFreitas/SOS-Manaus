import { Injectable } from '@nestjs/common';
import { Usuario as UsuarioPrisma } from '@prisma/client';
import { PrismaService } from '../../../compartilhado/infraestrutura/prisma/prisma.service';
import { Perfil } from '../entidade/perfil';
import { Usuario } from '../entidade/usuario';
import { UsuarioRepositorio } from '../entidade/usuario.repositorio';

/** INFRAESTRUTURA: implementa o repositório da entidade usando Prisma + PostgreSQL. */
@Injectable()
export class PrismaUsuarioRepositorio extends UsuarioRepositorio {
  constructor(private readonly prisma: PrismaService) {
    super();
  }

  async buscarPorEmail(email: string): Promise<Usuario | null> {
    const linha = await this.prisma.usuario.findUnique({ where: { email: email.toLowerCase() } });
    return linha ? this.paraEntidade(linha) : null;
  }

  async buscarPorId(id: string): Promise<Usuario | null> {
    const linha = await this.prisma.usuario.findUnique({ where: { id } });
    return linha ? this.paraEntidade(linha) : null;
  }

  /** Converte a linha do banco na entidade de domínio. */
  private paraEntidade(linha: UsuarioPrisma): Usuario {
    return new Usuario({
      id: linha.id,
      nome: linha.nome,
      email: linha.email,
      senhaHash: linha.senhaHash,
      perfil: linha.perfil as Perfil,
      ativo: linha.ativo,
      criadoEm: linha.criadoEm,
    });
  }
}
