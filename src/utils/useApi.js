import { useMemo } from 'react';
import { apiFetch } from './apiClient';

export function useApi(baseUrl, token) {
  return useMemo(() => ({
    get: (p) => apiFetch(`${baseUrl}${p}`, { token }),
    post: (p, body) => apiFetch(`${baseUrl}${p}`, { method: 'POST', token, body }),
    put: (p, body) => apiFetch(`${baseUrl}${p}`, { method: 'PUT', token, body }),
    del: (p) => apiFetch(`${baseUrl}${p}`, { method: 'DELETE', token }),
  }), [baseUrl, token]);
}
