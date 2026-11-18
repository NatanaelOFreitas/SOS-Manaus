import { CanalNaoPermitidoErro, CredenciaisInvalidasErro, UsuarioInativoErro } from '../entidade/erros';
import { Canal, Perfil } from '../entidade/perfil';
import { UsuarioRepositorioEmMemoria } from '../testes/usuario-repositorio-em-memoria';
import { AutenticarUsuarioControle } from './autenticar-usuario.controle';
import { CifradorSenha } from './portas/cifrador-senha';
import { DadosToken, EmissorToken } from './portas/emissor-token';

/** Cifrador falso e determinístico: o "hash" de X é "hash:X". */
class CifradorFalso extends CifradorSenha {
  comparacoes = 0;
  async cifrar(senha: string) {
    return `hash:${senha}`;
  }
  async comparar(senha: string, hash: string) {
    this.comparacoes++;
    return hash === `hash:${senha}`;
  }
}

class EmissorFalso extends EmissorToken {
  ultimo?: DadosToken;
  async emitir(dados: DadosToken) {
    this.ultimo = dados;
    return `token-de-${dados.sub}`;
  }
}

/** Testes do CONTROLE: fluxo do caso de uso com portas substituídas por implementações falsas. */
describe('AutenticarUsuarioControle', () => {
  let repo: UsuarioRepositorioEmMemoria;
  let cifrador: CifradorFalso;
  let emissor: EmissorFalso;
  let controle: AutenticarUsuarioControle;

  beforeEach(() => {
    repo = new UsuarioRepositorioEmMemoria();
    cifrador = new CifradorFalso();
    emissor = new EmissorFalso();
    controle = new AutenticarUsuarioControle(repo, cifrador, emissor);
  });

  it('autentica o cidadão no app e devolve token + usuário público', async () => {
    repo.adicionar({ id: 'c1', email: 'maria@sosmanaus.com', senhaHash: 'hash:sos12345' });

    const resultado = await controle.executar({
      email: '  Maria@SosManaus.com ',
      senha: 'sos12345',
      canal: Canal.APP_CIDADAO,
    });

    expect(resultado.accessToken).toBe('token-de-c1');
    expect(resultado.usuario).not.toHaveProperty('senhaHash');
    expect(emissor.ultimo).toEqual({ sub: 'c1', perfil: Perfil.CIDADAO });
  });

  it('recusa senha errada', async () => {
    repo.adicionar({ email: 'maria@sosmanaus.com', senhaHash: 'hash:sos12345' });
    await expect(
      controle.executar({ email: 'maria@sosmanaus.com', senha: 'errada', canal: Canal.APP_CIDADAO }),
    ).rejects.toBeInstanceOf(CredenciaisInvalidasErro);
  });

  it('recusa e-mail inexistente com o mesmo erro e ainda compara a senha', async () => {
    await expect(
      controle.executar({ email: 'ninguem@x.com', senha: 'qualquer', canal: Canal.APP_CIDADAO }),
    ).rejects.toBeInstanceOf(CredenciaisInvalidasErro);
    expect(cifrador.comparacoes).toBe(1); // evita diferença de tempo de resposta
  });

  it('aplica a regra da entidade: gestor não entra pelo app', async () => {
    repo.adicionar({ email: 'gestor@sosmanaus.com', senhaHash: 'hash:sos12345', perfil: Perfil.GESTOR });
    await expect(
      controle.executar({ email: 'gestor@sosmanaus.com', senha: 'sos12345', canal: Canal.APP_CIDADAO }),
    ).rejects.toBeInstanceOf(CanalNaoPermitidoErro);
    expect(emissor.ultimo).toBeUndefined();
  });

  it('aplica a regra da entidade: conta desativada não entra', async () => {
    repo.adicionar({ email: 'maria@sosmanaus.com', senhaHash: 'hash:sos12345', ativo: false });
    await expect(
      controle.executar({ email: 'maria@sosmanaus.com', senha: 'sos12345', canal: Canal.APP_CIDADAO }),
    ).rejects.toBeInstanceOf(UsuarioInativoErro);
  });
});
