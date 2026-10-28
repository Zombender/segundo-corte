import Icon from './Icon.jsx';

const types = {
  LAPTOP: { label: 'Laptop', icon: 'laptop' },
  PROYECTOR: { label: 'Proyector', icon: 'projector' },
  TABLETA: { label: 'Tableta', icon: 'tablet' },
};

export default function EquipoTable({ equipos, loading, error, hasFilters, onRetry, onClear }) {
  if (loading) {
    return <div className="table-state" role="status"><span className="loading-ring" /><h3>Consultando equipos</h3><p>Estamos actualizando la disponibilidad.</p></div>;
  }
  if (error) {
    return <div className="table-state" role="alert"><span className="state-icon error-icon"><Icon name="alert" size={28} /></span><h3>No pudimos cargar el inventario</h3><p>{error}</p><button className="button secondary" onClick={onRetry}><Icon name="refresh" size={17} /> Reintentar</button></div>;
  }
  if (!equipos.length) {
    return <div className="table-state" role="status"><span className="state-icon"><Icon name={hasFilters ? 'search' : 'equipment'} size={30} /></span><h3>{hasFilters ? 'No encontramos coincidencias' : 'Todavía no hay equipos'}</h3><p>{hasFilters ? 'Prueba con otro nombre, código o estado.' : 'Los equipos registrados aparecerán aquí cuando actualices la lista.'}</p>{hasFilters && <button className="button secondary" onClick={onClear}>Limpiar filtros</button>}</div>;
  }
  return (
    <div className="table-scroll">
      <table>
        <caption className="sr-only">Equipos tecnológicos y su disponibilidad actual</caption>
        <thead><tr><th scope="col">Código patrimonial</th><th scope="col">Equipo</th><th scope="col">Tipo</th><th scope="col">Estado</th></tr></thead>
        <tbody>{equipos.map((equipo) => {
          const type = types[equipo.tipo] || { label: equipo.tipo, icon: 'equipment' };
          const available = equipo.estado === 'DISPONIBLE';
          const label = available ? 'Disponible' : equipo.estado === 'PRESTADO' ? 'Prestado' : equipo.estado;
          return <tr key={equipo.id}>
            <td><span className="equipment-code">{equipo.codigoPatrimonial}</span></td>
            <td><div className="equipment-name"><span className={`device-icon ${type.icon}`}><Icon name={type.icon} size={22} /></span><strong>{equipo.nombre}</strong></div></td>
            <td className="type-cell">{type.label}</td>
            <td><span className={`status-badge ${available ? 'available' : 'borrowed'}`}><span className="status-dot" />{label}</span></td>
          </tr>;
        })}</tbody>
      </table>
    </div>
  );
}
