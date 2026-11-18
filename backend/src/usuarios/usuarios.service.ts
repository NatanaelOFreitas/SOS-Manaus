import { Injectable } from '@nestjs/common';
import { Usuario } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';

export type UsuarioPublico = Omit<Usuario, 'senhaHash'>;

@Injectable()
export class UsuariosService {
  constructor(private readonly prisma: PrismaService) {}

  buscarPorEmail(email: string): Promise<Usuario | null> {
    return this.prisma.usuario.findUnique({ where: { email: email.toLowerCase() } });
  }

  buscarPorId(id: string): Promise<Usuario | null> {
    return this.prisma.usuario.findUnique({ where: { id } });
  }

  static paraPublico(usuario: Usuario): UsuarioPublico {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { senhaHash, ...publico } = usuario;
    return publico;
  }
}
