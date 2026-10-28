import { useEffect, useRef, useState } from 'react';
import { iniciarSesion, mensajeError } from '../api/client.js';
import Icon from './Icon.jsx';

export default function LoginForm({ onLogin, notice }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const request = useRef(null);

  useEffect(() => () => request.current?.abort(), []);

  async function handleSubmit(event) {
    event.preventDefault();
    if (loading) return;
    setError('');
    setLoading(true);
    const controller = new AbortController();
    request.current = controller;
    try {
      const session = await iniciarSesion(username.trim(), password, controller.signal);
      onLogin(session);
    } catch (failure) {
      if (!controller.signal.aborted) setError(mensajeError(failure, true));
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }

  return (
    <main className="login-layout">
      <section className="login-story">
        <span className="eyebrow light">CENTRO DE CAPACITACIÓN</span>
        <h1>La tecnología<br />lista para tu<br /><span>próxima clase.</span></h1>
        <p>Consulta los equipos de TecnoUAM y encuentra lo que necesitas para seguir enseñando.</p>
        <div className="device-scene" aria-hidden="true">
          <div className="scene-orbit" />
          <div className="scene-laptop"><Icon name="laptop" size={140} /></div>
          <div className="scene-card"><span className="status-dot" /> Todo empieza con una buena herramienta.</div>
        </div>
        <span className="story-caption">TECNOLOGÍA QUE ACOMPAÑA EL APRENDIZAJE</span>
      </section>
      <section className="login-panel" aria-labelledby="login-title">
        <div className="login-card">
          <div className="login-lock"><Icon name="lock" size={25} /></div>
          <span className="eyebrow">BIENVENIDO A TECNOUAM</span>
          <h2 id="login-title">Tu inventario,<br />en un solo lugar.</h2>
          <p className="muted">Inicia sesión para consultar la disponibilidad de los equipos tecnológicos.</p>
          {notice && <div className="notice" role="status">{notice}</div>}
          <form onSubmit={handleSubmit}>
            <label htmlFor="username">Usuario</label>
            <input id="username" name="username" autoComplete="username" required maxLength={100}
              value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Tu nombre de usuario" disabled={loading} />
            <label htmlFor="password">Contraseña</label>
            <input id="password" name="password" type="password" autoComplete="current-password" required
              value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Ingresa tu contraseña" disabled={loading} />
            {error && <div className="error-message" role="alert"><Icon name="alert" /><span>{error}</span></div>}
            <button className="button primary login-submit" type="submit" disabled={loading || !username.trim() || !password}>
              {loading ? 'Iniciando sesión…' : 'Iniciar sesión'}<Icon name={loading ? 'refresh' : 'arrow'} className={loading ? 'spin' : ''} />
            </button>
          </form>
          <p className="login-note"><Icon name="lock" size={15} /> Acceso exclusivo para usuarios autorizados.</p>
        </div>
      </section>
    </main>
  );
}
