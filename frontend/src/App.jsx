import { useCallback, useState } from 'react';
import { guardarSesion, leerSesion } from './api/session.js';
import Inventory from './components/Inventory.jsx';
import LoginForm from './components/LoginForm.jsx';
import Icon from './components/Icon.jsx';

export default function App() {
  const [session, setSession] = useState(leerSesion);
  const [notice, setNotice] = useState('');

  const expireSession = useCallback(() => {
    guardarSesion(null);
    setSession(null);
    setNotice('Tu sesión venció o dejó de ser válida. Inicia sesión para continuar.');
  }, []);

  function handleLogin(nextSession) {
    guardarSesion(nextSession);
    setSession(nextSession);
    setNotice('');
  }

  function handleLogout() {
    guardarSesion(null);
    setSession(null);
    setNotice('');
  }

  return (
    <div className="app-shell">
      <header className="site-header"><div className="header-inner">
        <a href="/" className="brand" aria-label="TecnoUAM, inicio"><span className="brand-icon"><Icon name="equipment" size={23} /></span><span>Tecno<span className="brand-uam">UAM</span><small>CENTRO DE CAPACITACIÓN</small></span></a>
        {session ? <><nav aria-label="Navegación principal"><span className="active-nav" aria-current="page"><Icon name="equipment" size={17} /> Equipos</span></nav><div className="user-menu"><span className="avatar" aria-hidden="true">{session.username.slice(0, 1).toUpperCase()}</span><span className="username">{session.username}</span><button className="logout-button" onClick={handleLogout} title="Cerrar sesión"><Icon name="logout" size={18} /><span>Cerrar sesión</span></button></div></> : <span className="header-label"><span className="status-dot" /> Gestión de equipos</span>}
      </div></header>
      {session ? <Inventory session={session} onSessionExpired={expireSession} /> : <LoginForm onLogin={handleLogin} notice={notice} />}
      <footer className="site-footer"><span>TecnoUAM <span className="footer-separator">/</span> Tecnología al servicio de la educación.</span><span>Centro de Capacitación · UAM</span></footer>
    </div>
  );
}
