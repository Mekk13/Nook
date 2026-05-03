import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.css'
import App from './App.tsx'
import { startNetworkMonitor } from './stores/useNetworkStore.ts';

startNetworkMonitor();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
