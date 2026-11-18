import { useStatusApi } from '../controle/useStatusApi';

/**
 * FRONTEIRA (esqueleto) do painel da prefeitura.
 * Etapa futura: login do gestor (canal PAINEL_PREFEITURA), dashboard e lista de ocorrências.
 */
export default function PainelProvisorio() {
  const status = useStatusApi();
  const cor = status === 'online' ? '#155E4B' : status === 'offline' ? '#C62828' : '#5C6B65';

  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', background: '#F1F4F2', minHeight: '100vh', display: 'grid', placeItems: 'center', margin: 0 }}>
      <section style={{ background: '#fff', borderRadius: 24, padding: 32, maxWidth: 440, textAlign: 'center', boxShadow: '0 12px 32px rgba(0,0,0,.08)' }}>
        <h1 style={{ margin: 0, color: '#13201B' }}>SOS Manaus</h1>
        <p style={{ color: '#5C6B65' }}>Painel da Prefeitura: em construção</p>
        <p>
          API: <strong style={{ color: cor }}>{status}</strong>
        </p>
      </section>
    </main>
  );
}
