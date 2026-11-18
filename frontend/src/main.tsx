import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import PainelProvisorio from './fronteira/PainelProvisorio';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <PainelProvisorio />
  </StrictMode>,
);
