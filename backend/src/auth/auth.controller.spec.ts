import { INestApplication, ValidationPipe } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { Test } from '@nestjs/testing';
import * as bcrypt from 'bcryptjs';
import request from 'supertest';
import { UsuariosService } from '../usuarios/usuarios.service';
import { AuthModule } from './auth.module';

/** Testes de integração HTTP da rota de login (Jest + Supertest), sem banco real. */
describe('AuthController (HTTP)', () => {
  let app: INestApplication;

  const usuario = {
    id: 'u1',
    nome: 'Maria Silva',
    email: 'cidadao@sosmanaus.com',
    senhaHash: bcrypt.hashSync('sos12345', 4),
    perfil: 'CIDADAO',
    ativo: true,
    criadoEm: new Date(),
    atualizadoEm: new Date(),
  };

  const usuariosFake = {
    buscarPorEmail: jest.fn(async (email: string) => (email === usuario.email ? usuario : null)),
    buscarPorId: jest.fn(async (id: string) => (id === usuario.id ? usuario : null)),
  };

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      imports: [
        ConfigModule.forRoot({
          isGlobal: true,
          ignoreEnvFile: true,
          load: [() => ({ JWT_SECRET: 'segredo-de-teste' })],
        }),
        AuthModule,
      ],
    })
      .overrideProvider(UsuariosService)
      .useValue(usuariosFake)
      .compile();

    app = moduleRef.createNestApplication();
    app.useGlobalPipes(new ValidationPipe({ whitelist: true, forbidNonWhitelisted: true, transform: true }));
    await app.init();
  });

  afterAll(() => app.close());

  it('POST /auth/login devolve token com credenciais corretas', async () => {
    const res = await request(app.getHttpServer())
      .post('/auth/login')
      .send({ email: 'Cidadao@SosManaus.com', senha: 'sos12345' })
      .expect(200);

    expect(res.body.accessToken).toEqual(expect.any(String));
    expect(res.body.usuario.email).toBe(usuario.email);
    expect(res.body.usuario).not.toHaveProperty('senhaHash');
  });

  it('POST /auth/login devolve 401 com senha errada', async () => {
    const res = await request(app.getHttpServer())
      .post('/auth/login')
      .send({ email: usuario.email, senha: 'errada123' })
      .expect(401);

    expect(res.body.message).toBe('E-mail ou senha incorretos.');
  });

  it('POST /auth/login devolve 400 com e-mail inválido', async () => {
    await request(app.getHttpServer())
      .post('/auth/login')
      .send({ email: 'nao-e-email', senha: 'sos12345' })
      .expect(400);
  });

  it('GET /auth/me exige token e devolve o usuário logado', async () => {
    await request(app.getHttpServer()).get('/auth/me').expect(401);

    const login = await request(app.getHttpServer())
      .post('/auth/login')
      .send({ email: usuario.email, senha: 'sos12345' });

    const res = await request(app.getHttpServer())
      .get('/auth/me')
      .set('Authorization', `Bearer ${login.body.accessToken}`)
      .expect(200);

    expect(res.body.nome).toBe('Maria Silva');
  });
});
