/** Perfis de usuário definidos no documento de requisitos (seção 2). */
export enum Perfil {
  CIDADAO = 'CIDADAO',
  GESTOR = 'GESTOR',
}

/** Canais (clientes) pelos quais o sistema pode ser acessado. */
export enum Canal {
  APP_CIDADAO = 'APP_CIDADAO',
  PAINEL_PREFEITURA = 'PAINEL_PREFEITURA',
}

/** Regra de negócio: quais perfis podem entrar em cada canal. */
export const PERFIS_PERMITIDOS_POR_CANAL: Readonly<Record<Canal, readonly Perfil[]>> = {
  [Canal.APP_CIDADAO]: [Perfil.CIDADAO],
  [Canal.PAINEL_PREFEITURA]: [Perfil.GESTOR],
};
