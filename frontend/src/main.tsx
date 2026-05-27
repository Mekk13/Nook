import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.css'
import App from './App.tsx'
import { startNetworkMonitor } from './stores/useNetworkStore.ts';
import '@tabler/icons-webfont/dist/tabler-icons.min.css';

startNetworkMonitor();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)