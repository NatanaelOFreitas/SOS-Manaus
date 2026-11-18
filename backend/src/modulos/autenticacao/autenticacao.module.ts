import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtModule, JwtSignOptions } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { AutenticarUsuarioControle } from './controle/autenticar-usuario.controle';
import { ObterUsuarioLogadoControle } from './controle/obter-usuario-logado.controle';
import { CifradorSenha } from './controle/portas/cifrador-senha';
import { EmissorToken } from './controle/portas/emissor-token';
import { UsuarioRepositorio } from './entidade/usuario.repositorio';
import { AutenticacaoController } from './fronteira/autenticacao.controller';
import { JwtStrategy } from './fronteira/jwt.strategy';
import { BcryptCifradorSenha } from './infraestrutura/bcrypt-cifrador-senha';
import { JwtEmissorToken } from './infraestrutura/jwt-emissor-token';
import { PrismaUsuarioRepositorio } from './infraestrutura/prisma-usuario.repositorio';

/**
 * Módulo de autenticação, organizado em Fronteira → Controle → Entidade.
 * Aqui só se faz a "ligação": cada porta (abstração) recebe sua implementação.
 */
@Module({
  imports: [
    PassportModule,
    JwtModule.registerAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        secret: config.getOrThrow<string>('JWT_SECRET'),
        signOptions: {
          expiresIn: (config.get<string>('JWT_EXPIRES_IN') ?? '7d') as JwtSignOptions['expiresIn'],
        },
      }),
    }),
  ],
  controllers: [AutenticacaoController],
  providers: [
    // Fronteira
    JwtStrategy,
    // Controle
    AutenticarUsuarioControle,
    ObterUsuarioLogadoControle,
    // Portas → implementações de infraestrutura
    { provide: UsuarioRepositorio, useClass: PrismaUsuarioRepositorio },
    { provide: CifradorSenha, useClass: BcryptCifradorSenha },
    { provide: EmissorToken, useClass: JwtEmissorToken },
  ],
})
export class AutenticacaoModule {}
