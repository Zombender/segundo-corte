import { useState } from 'react';
import useEquipos from '../hooks/useEquipos.js';
import EquipoTable from './EquipoTable.jsx';
import Icon from './Icon.jsx';

export default function Inventory({ session, onSessionExpired }) {
  const { equipos, loading, error, updatedAt, refresh } = useEquipos(session.token, onSessionExpired);
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('TODOS');
  const normalize = (text) => String(text ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  const visible = equipos.filter((equipo) => (status === 'TODOS' || equipo.estado === status)
    && normalize(`${equipo.codigoPatrimonial} ${equipo.nombre} ${equipo.tipo}`).includes(normalize(query.trim())));
  const available = equipos.filter((equipo) => equipo.estado === 'DISPONIBLE').length;
  const borrowed = equipos.filter((equipo) => equipo.estado === 'PRESTADO').length;
  const hasFilters = Boolean(query.trim()) || status !== 'TODOS';
  const count = (value) => loading || error ? '—' : String(value).padStart(2, '0');

  function clearFilters() {
    setQuery('');
    setStatus('TODOS');
  }

  return (
    <main className="inventory-main">
      <div className="breadcrumb">Centro de capacitación <span>/</span> Inventario</div>
      <section className="page-heading">
        <div><span className="eyebrow">RECURSOS PARA SEGUIR APRENDIENDO</span><h1>Equipos tecnológicos<span>.</span></h1><p>Consulta el inventario y encuentra el equipo para tu próxima clase.</p></div>
        <button className="button primary refresh-button" onClick={refresh} disabled={loading}><Icon name="refresh" size={18} className={loading ? 'spin' : ''} />{loading ? 'Actualizando…' : 'Actualizar equipos'}</button>
      </section>
      <section className="stats-grid" aria-label="Resumen del inventario">
        <article className="stat-card"><div><span>Total de equipos</span><strong>{count(equipos.length)}</strong><small>En el inventario</small></div><span className="stat-icon"><Icon name="equipment" size={25} /></span></article>
        <article className="stat-card"><div><span>Disponibles</span><strong>{count(available)}</strong><small>Listos para acompañarte</small></div><span className="stat-icon green"><Icon name="check" size={25} /></span></article>
        <article className="stat-card"><div><span>En préstamo</span><strong>{count(borrowed)}</strong><small>Actualmente en uso</small></div><span className="stat-icon amber"><Icon name="clock" size={25} /></span></article>
      </section>
      <section className="inventory-card" aria-labelledby="inventory-title" aria-busy={loading}>
        <div className="inventory-card-heading"><div><h2 id="inventory-title">Inventario de equipos <span className="count-badge">{loading || error ? '—' : equipos.length}</span></h2><p>La disponibilidad que necesitas, de un vistazo.</p></div><span className="readonly-label"><Icon name="lock" size={14} /> Solo consulta</span></div>
        <div className="table-toolbar">
          <label className="search-field"><Icon name="search" size={19} /><span className="sr-only">Buscar equipos</span><input type="search" placeholder="Buscar por nombre, código o tipo…" value={query} onChange={(event) => setQuery(event.target.value)} /></label>
          <label className="filter-field"><span>Estado</span><select value={status} onChange={(event) => setStatus(event.target.value)} aria-label="Filtrar por estado"><option value="TODOS">Todos los estados</option><option value="DISPONIBLE">Disponible</option><option value="PRESTADO">Prestado</option></select></label>
        </div>
        <EquipoTable equipos={visible} loading={loading} error={error} hasFilters={hasFilters} onRetry={refresh} onClear={clearFilters} />
        <div className="table-footer" aria-live="polite"><span>{loading ? 'Consultando inventario…' : error ? 'Consulta sin completar' : `${visible.length} de ${equipos.length} equipos`}</span><span>{updatedAt ? `Actualizado a las ${updatedAt.toLocaleTimeString('es-NI', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}` : 'Esperando respuesta del servidor'}</span></div>
      </section>
      <aside className="inventory-tip"><span className="tip-icon"><Icon name="refresh" size={20} /></span><div><strong>El inventario se mantiene en movimiento.</strong><p>Después de registrar un equipo, préstamo o devolución, pulsa «Actualizar equipos» para consultar los cambios.</p></div></aside>
    </main>
  );
}
