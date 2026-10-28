const STORAGE_KEY = 'tecnouam.session';

export function leerSesion() {
  try {
    const session = JSON.parse(sessionStorage.getItem(STORAGE_KEY));
    return typeof session?.token === 'string' && session.token.trim()
      && typeof session.username === 'string' ? session : null;
  } catch {
    return null;
  }
}

export function guardarSesion(session) {
  try {
    if (session) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    else sessionStorage.removeItem(STORAGE_KEY);
  } catch {
    // La sesión sigue funcionando en memoria si el navegador bloquea el almacenamiento.
  }
}
