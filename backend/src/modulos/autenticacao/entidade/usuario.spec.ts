import { CanalNaoPermitidoErro, UsuarioInativoErro } from './erros';
import { Canal, Perfil } from './perfil';
import { DadosUsuario, Usuario } from './usuario';

/** Testes da ENTIDADE: regras de negócio puras, sem banco nem HTTP. */
describe('Entidade Usuario', () => {
  const criar = (extra: Partial<DadosUsuario> = {}) =>
    new Usuario({
      id: 'u1',
      nome: 'Maria Silva',
      email: 'maria@sosmanaus.com',
      senhaHash: 'hash',
      perfil: Perfil.CIDADAO,
      ativo: true,
      criadoEm: new Date(),
      ...extra,
    });

  it('cidadão ativo pode entrar no app', () => {
    expect(() => criar().garantirAcessoAo(Canal.APP_CIDADAO)).not.toThrow();
  });

  it('cidadão não pode entrar no painel da prefeitura', () => {
    expect(() => criar().garantirAcessoAo(Canal.PAINEL_PREFEITURA)).toThrow(CanalNaoPermitidoErro);
  });

  it('gestor pode entrar no painel, mas não no app do cidadão', () => {
    const gestor = criar({ perfil: Perfil.GESTOR });
    expect(() => gestor.garantirAcessoAo(Canal.PAINEL_PREFEITURA)).not.toThrow();
    expect(() => gestor.garantirAcessoAo(Canal.APP_CIDADAO)).toThrow(
      'Esta conta é de gestor. Acesse pelo painel da prefeitura.',
    );
  });

  it('conta inativa não entra em nenhum canal e não mantém sessão', () => {
    const inativo = criar({ ativo: false });
    expect(() => inativo.garantirAcessoAo(Canal.APP_CIDADAO)).toThrow(UsuarioInativoErro);
    expect(inativo.podeManterSessao()).toBe(false);
  });

  it('a visão pública nunca expõe o hash da senha', () => {
    expect(criar().paraPublico()).toEqual({
      id: 'u1',
      nome: 'Maria Silva',
      email: 'maria@sosmanaus.com',
      perfil: Perfil.CIDADAO,
    });
  });
});
