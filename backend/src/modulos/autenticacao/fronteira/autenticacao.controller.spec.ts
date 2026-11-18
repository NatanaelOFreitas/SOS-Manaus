import { INestApplication } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { Test } from '@nestjs/testing';
import * as bcrypt from 'bcryptjs';
import request from 'supertest';
import { configurarApp } from '../../../configurar-app';
import { AutenticacaoModule } from '../autenticacao.module';
import { Perfil } from '../entidade/perfil';
import { UsuarioRepositorio } from '../entidade/usuario.repositorio';
import { UsuarioRepositorioEmMemoria } from '../testes/usuario-repositorio-em-memoria';

/**
 * Testes da FRONTEIRA (Jest + Supertest): requisições HTTP reais passando por
 * fronteira → controle → entidade. Só o banco é trocado por memória.
 */
describe('Fronteira HTTP /auth', () => {
  let app: INestApplication;
  const repo = new UsuarioRepositorioEmMemoria();

  beforeAll(async () => {
    const senhaHash = bcrypt.hashSync('sos12345', 4);
    repo.adicionar({ id: 'c1', nome: 'Maria Silva', email: 'cidadao@sosmanaus.com', senhaHash });
    repo.adicionar({ id: 'g1', email: 'gestor@sosmanaus.com', senhaHash, perfil: Perfil.GESTOR });
    repo.adicionar({ id: 'i1', email: 'inativo@sosmanaus.com', senhaHash, ativo: false });

    const moduloTeste = await Test.createTestingModule({
      imports: [
        ConfigModule.forRoot({
          isGlobal: true,
          ignoreEnvFile: true,
          load: [() => ({ JWT_SECRET: 'segredo-de-teste' })],
        }),
        AutenticacaoModule,
      ],
    })
      .overrideProvider(UsuarioRepositorio)
      .useValue(repo)
      .compile();

    app = configurarApp(moduloTeste.createNestApplication());
    await app.init();
  });

  afterAll(() => app.close());

  const login = (corpo: object) => request(app.getHttpServer()).post('/auth/login').send(corpo);

  it('200: cidadão entra pelo app e recebe token', async () => {
    const res = await login({ email: 'Cidadao@SosManaus.com', senha: 'sos12345', canal: 'APP_CIDADAO' }).expect(200);
    expect(res.body.accessToken).toEqual(expect.any(String));
    expect(res.body.usuario).toEqual({ id: 'c1', nome: 'Maria Silva', email: 'cidadao@sosmanaus.com', perfil: 'CIDADAO' });
  });

  it('401: senha errada', async () => {
    const res = await login({ email: 'cidadao@sosmanaus.com', senha: 'errada123', canal: 'APP_CIDADAO' }).expect(401);
    expect(res.body).toMatchObject({ codigo: 'CREDENCIAIS_INVALIDAS', message: 'E-mail ou senha incorretos.' });
  });

  it('403: gestor tentando entrar pelo app do cidadão', async () => {
    const res = await login({ email: 'gestor@sosmanaus.com', senha: 'sos12345', canal: 'APP_CIDADAO' }).expect(403);
    expect(res.body.codigo).toBe('CANAL_NAO_PERMITIDO');
  });

  it('200: gestor entra pelo painel da prefeitura', async () => {
    await login({ email: 'gestor@sosmanaus.com', senha: 'sos12345', canal: 'PAINEL_PREFEITURA' }).expect(200);
  });

  it('403: conta desativada', async () => {
    const res = await login({ email: 'inativo@sosmanaus.com', senha: 'sos12345', canal: 'APP_CIDADAO' }).expect(403);
    expect(res.body.codigo).toBe('USUARIO_INATIVO');
  });

  it('400: formato inválido é barrado na fronteira (e-mail ruim, canal ausente)', async () => {
    await login({ email: 'nao-e-email', senha: 'sos12345', canal: 'APP_CIDADAO' }).expect(400);
    await login({ email: 'cidadao@sosmanaus.com', senha: 'sos12345' }).expect(400);
  });

  it('GET /auth/me: 401 sem token, 200 com token válido', async () => {
    await request(app.getHttpServer()).get('/auth/me').expect(401);

    const { body } = await login({ email: 'cidadao@sosmanaus.com', senha: 'sos12345', canal: 'APP_CIDADAO' });
    const res = await request(app.getHttpServer())
      .get('/auth/me')
      .set('Authorization', `Bearer ${body.accessToken}`)
      .expect(200);
    expect(res.body.nome).toBe('Maria Silva');
  });
});
