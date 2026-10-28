import assert from 'node:assert/strict';
import { after, before, test } from 'node:test';
import http from 'node:http';
import { api, consultarEquipos, iniciarSesion, mensajeError } from './client.js';

// Servidor aislado para verificar el cliente HTTP; no es la fuente de datos de la app.
let server;
let requests = [];
let nextResponse;

before(async () => {
  server = http.createServer(async (request, response) => {
    let body = '';
    for await (const chunk of request) body += chunk;
    requests.push({ method: request.method, url: request.url, headers: request.headers, body });
    const result = nextResponse || { status: 200, body: [] };
    response.writeHead(result.status, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify(result.body));
  });
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  api.defaults.baseURL = `http://127.0.0.1:${server.address().port}/api`;
  api.defaults.proxy = false;
});

after(async () => {
  await new Promise((resolve) => server.close(resolve));
});

test('el login envía las credenciales y recoge el token del contrato Spring Boot', async () => {
  nextResponse = { status: 200, body: { token: 'jwt-de-prueba', type: 'Bearer' } };
  const session = await iniciarSesion('ana', 'contraseña');
  assert.deepEqual(session, { username: 'ana', token: 'jwt-de-prueba' });
  const request = requests.at(-1);
  assert.equal(request.method, 'POST');
  assert.equal(request.url, '/api/auth/login');
  assert.deepEqual(JSON.parse(request.body), { username: 'ana', password: 'contraseña' });
  assert.equal(request.headers.authorization, undefined);
});

test('la consulta incluye Bearer y devuelve los equipos reales de la respuesta', async () => {
  const equipos = [{ id: 4, codigoPatrimonial: 'UAM-004', nombre: 'Equipo de prueba', tipo: 'LAPTOP', estado: 'PRESTADO' }];
  nextResponse = { status: 200, body: equipos };
  assert.deepEqual(await consultarEquipos('token-usuario'), equipos);
  assert.equal(requests.at(-1).method, 'GET');
  assert.equal(requests.at(-1).url, '/api/equipos');
  assert.equal(requests.at(-1).headers.authorization, 'Bearer token-usuario');
});

test('una lista vacía permanece vacía, sin registros de sustitución', async () => {
  nextResponse = { status: 200, body: [] };
  assert.deepEqual(await consultarEquipos('token-usuario'), []);
});

test('rechaza respuestas incompatibles con el contrato', async () => {
  nextResponse = { status: 200, body: { access_token: 'otro-formato' } };
  await assert.rejects(iniciarSesion('ana', 'clave'), /token válido/);
  nextResponse = { status: 200, body: { equipos: [] } };
  await assert.rejects(consultarEquipos('token'), /lista de equipos/);
});

test('distingue credenciales incorrectas, sesión vencida y acceso prohibido', async () => {
  nextResponse = { status: 401, body: {} };
  await assert.rejects(iniciarSesion('ana', 'incorrecta'), (error) => {
    assert.equal(mensajeError(error, true), 'Usuario o contraseña incorrectos.');
    return true;
  });
  await assert.rejects(consultarEquipos('vencido'), (error) => {
    assert.equal(error.response.status, 401);
    assert.match(mensajeError(error), /sesión venció/);
    return true;
  });
  nextResponse = { status: 403, body: {} };
  await assert.rejects(consultarEquipos('sin-permiso'), (error) => {
    assert.match(mensajeError(error), /no tiene permiso/);
    return true;
  });
});

test('cancelar una consulta no genera una consulta nueva', async () => {
  const controller = new AbortController();
  controller.abort();
  const count = requests.length;
  await assert.rejects(consultarEquipos('token', controller.signal), { code: 'ERR_CANCELED' });
  assert.equal(requests.length, count);
});
