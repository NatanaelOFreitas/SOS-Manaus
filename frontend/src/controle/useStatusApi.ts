import { useEffect, useState } from 'react';
import { StatusApi } from '../entidade/StatusApi';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:3000';

/** CONTROLE (esqueleto): consulta /health da API e expõe o status para a fronteira. */
export function useStatusApi(): StatusApi {
  const [status, setStatus] = useState<StatusApi>('verificando');

  useEffect(() => {
    fetch(`${API_URL}/health`)
      .then((r) => setStatus(r.ok ? 'online' : 'offline'))
      .catch(() => setStatus('offline'));
  }, []);

  return status;
}
