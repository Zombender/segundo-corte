import { useEffect, useState } from 'react';
import { consultarEquipos, mensajeError } from '../api/client.js';

export default function useEquipos(token, onSessionExpired) {
  const [revision, setRevision] = useState(0);
  const [state, setState] = useState({ equipos: [], loading: true, error: '', updatedAt: null });

  useEffect(() => {
    const controller = new AbortController();
    setState({ equipos: [], loading: true, error: '', updatedAt: null });
    consultarEquipos(token, controller.signal)
      .then((equipos) => {
        if (!controller.signal.aborted) {
          setState({ equipos, loading: false, error: '', updatedAt: new Date() });
        }
      })
      .catch((error) => {
        if (controller.signal.aborted) return;
        if (error.response?.status === 401) {
          onSessionExpired();
          return;
        }
        setState({ equipos: [], loading: false, error: mensajeError(error), updatedAt: null });
      });
    return () => controller.abort();
  }, [token, revision, onSessionExpired]);

  return { ...state, refresh: () => setRevision((value) => value + 1) };
}
