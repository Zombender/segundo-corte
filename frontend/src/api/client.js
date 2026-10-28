import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { Accept: 'application/json' },
});

export async function iniciarSesion(username, password, signal) {
  const { data } = await api.post('/auth/login', { username, password }, { signal });
  if (typeof data?.token !== 'string' || !data.token.trim()) {
    throw new Error('El servidor no devolvió un token válido.');
  }
  return { token: data.token, username };
}

export async function consultarEquipos(token, signal) {
  const { data } = await api.get('/equipos', {
    headers: { Authorization: `Bearer ${token}` },
    signal,
  });
  if (!Array.isArray(data)) {
    throw new Error('La respuesta del servidor no contiene una lista de equipos.');
  }
  return data;
}

export function mensajeError(error, login = false) {
  const status = error.response?.status;
  if (status === 401) {
    return login ? 'Usuario o contraseña incorrectos.' : 'Tu sesión venció. Inicia sesión nuevamente.';
  }
  if (status === 403) {
    return login
      ? 'El servidor rechazó el inicio de sesión. Revisa la configuración de seguridad de la API.'
      : 'Tu cuenta no tiene permiso para consultar los equipos.';
  }
  if (error.code === 'ECONNABORTED') return 'El servidor tardó demasiado. Intenta nuevamente.';
  if (!error.response && axios.isAxiosError(error)) {
    return 'No pudimos conectar con el servidor. Comprueba que el backend esté en ejecución.';
  }
  if (status >= 500) return 'El servidor no pudo completar la consulta. Intenta nuevamente en unos momentos.';
  return error.response?.data?.detail || error.message || 'Ocurrió un error inesperado.';
}
