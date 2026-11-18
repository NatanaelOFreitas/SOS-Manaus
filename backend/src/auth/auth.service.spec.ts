import { UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcryptjs';
import { UsuariosService } from '../usuarios/usuarios.service';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  const usuarioBase = {
    id: 'u1',
    nome: 'Maria Silva',
    email: 'cidadao@sosmanaus.com',
    senhaHash: bcrypt.hashSync('sos12345', 4),
    perfil: 'CIDADAO' as const,
    ativo: true,
    criadoEm: new Date(),
    atualizadoEm: new Date(),
  };

  let usuarios: { buscarPorEmail: jest.Mock };
  let service: AuthService;

  beforeEach(() => {
    usuarios = { buscarPorEmail: jest.fn() };
    const jwt = { signAsync: jest.fn().mockResolvedValue('token-falso') } as unknown as JwtService;
    service = new AuthService(usuarios as unknown as UsuariosService, jwt);
  });

  it('retorna token e usuário sem a senha quando as credenciais estão corretas', async () => {
    usuarios.buscarPorEmail.mockResolvedValue(usuarioBase);
    const resp = await service.login({ email: usuarioBase.email, senha: 'sos12345' });
    expect(resp.accessToken).toBe('token-falso');
    expect(resp.usuario).not.toHaveProperty('senhaHash');
    expect(resp.usuario.nome).toBe('Maria Silva');
  });

  it('rejeita senha incorreta', async () => {
    usuarios.buscarPorEmail.mockResolvedValue(usuarioBase);
    await expect(service.login({ email: usuarioBase.email, senha: 'errada' })).rejects.toBeInstanceOf(
      UnauthorizedException,
    );
  });

  it('rejeita e-mail inexistente com a mesma mensagem', async () => {
    usuarios.buscarPorEmail.mockResolvedValue(null);
    await expect(service.login({ email: 'x@y.com', senha: 'qualquer' })).rejects.toThrow(
      'E-mail ou senha incorretos.',
    );
  });

  it('rejeita conta desativada', async () => {
    usuarios.buscarPorEmail.mockResolvedValue({ ...usuarioBase, ativo: false });
    await expect(service.login({ email: usuarioBase.email, senha: 'sos12345' })).rejects.toThrow(
      'Esta conta está desativada.',
    );
  });
});
