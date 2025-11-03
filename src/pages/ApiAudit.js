import React, { useEffect, useMemo, useState } from 'react';
import api from '../services/api';
import { Search, RefreshCw, CheckCircle2, AlertTriangle, Globe } from 'lucide-react';

const resolveOpenApiUrl = () => {
  const baseURL = (api && api.defaults && api.defaults.baseURL) || (process.env.REACT_APP_API_URL || 'http://localhost:8080/api');
  try {
    const url = new URL(baseURL);
    // common case: backend mounted under /api → OpenAPI lives at /v3/api-docs
    return `${url.origin}/v3/api-docs`;
  } catch (_) {
    // fallback
    if (baseURL.endsWith('/api')) return baseURL.replace(/\/api$/, '') + '/v3/api-docs';
    return baseURL + '/v3/api-docs';
  }
};

const KNOWN_MODULE_PREFIXES = [
  '/auth',
  '/users',
  '/vehicles/brands',
  '/vehicles/models',
  '/vehicles/variants',
  '/vehicles/colors',
  '/customers',
  '/warehouses',
  '/vehicle-inventory',
  '/quotations',
  '/orders',
  '/sales-contracts',
  '/vehicle-deliveries',
  '/customer-payments',
  '/reports'
];


const AuditRow = ({ method, path, tags }) => {
  const known = KNOWN_MODULE_PREFIXES.some((p) => path.startsWith(p));
  return (
    <tr>
      <td><span className={`badge method ${method}`}>{method}</span></td>
      <td className="mono">{path}</td>
      <td>{tags?.join(', ') || '—'}</td>
      <td>{known ? <span className="ok"><CheckCircle2 size={16} /> Known</span> : <span className="warn"><AlertTriangle size={16} /> Unknown</span>}</td>
    </tr>
  );
};

const ApiAudit = () => {
  const [spec, setSpec] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');

  const fetchSpec = async () => {
    try {
      setLoading(true);
      setError('');
      const url = resolveOpenApiUrl();
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setSpec(data);
    } catch (e) {
      setError(e.message || 'Failed to load OpenAPI spec');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSpec();
  }, []);

  const allEndpoints = useMemo(() => {
    if (!spec?.paths) return [];
    const rows = [];
    Object.entries(spec.paths).forEach(([path, methods]) => {
      Object.entries(methods).forEach(([method, op]) => {
        const m = method.toUpperCase();
        if (!['GET','POST','PUT','PATCH','DELETE'].includes(m)) return;
        rows.push({ method: m, path, tags: op.tags || [] });
      });
    });
    rows.sort((a, b) => a.path.localeCompare(b.path) || a.method.localeCompare(b.method));
    return rows;
  }, [spec]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return allEndpoints;
    return allEndpoints.filter((e) => e.path.toLowerCase().includes(q) || e.method.toLowerCase().includes(q));
  }, [allEndpoints, query]);

  const unknown = useMemo(() => filtered.filter((e) => !KNOWN_MODULE_PREFIXES.some((p) => e.path.startsWith(p))), [filtered]);

  return (
    <div className="api-audit" style={{ padding: 24 }}>
      <h1 style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <Globe size={28} /> API Audit
      </h1>
      <p>Fetches OpenAPI from backend and highlights endpoints that may need new pages/features.</p>

      <div style={{ display: 'flex', gap: 12, alignItems: 'center', margin: '16px 0' }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={18} style={{ position: 'absolute', left: 12, top: 12, color: '#6b7280' }} />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search method or path..."
            className="form-input"
            style={{ width: '100%', paddingLeft: 40 }}
          />
        </div>
        <button className="btn btn-outline" onClick={fetchSpec} disabled={loading}>
          <RefreshCw size={18} /> Refresh
        </button>
      </div>

      {error && <div className="alert" style={{ color: '#b91c1c', marginBottom: 12 }}>Error: {error}</div>}

      {loading ? (
        <div>Đang tải OpenAPI...</div>
      ) : (
        <>
          <div style={{ marginBottom: 12, display: 'flex', gap: 16, flexWrap: 'wrap' }}>
            <div>Total endpoints: <strong>{allEndpoints.length}</strong></div>
            <div>Filtered: <strong>{filtered.length}</strong></div>
            <div>Unknown endpoints: <strong style={{ color: unknown.length ? '#b45309' : '#16a34a' }}>{unknown.length}</strong></div>
          </div>

          {unknown.length > 0 && (
            <div style={{ marginBottom: 20, background: '#fff7ed', border: '1px solid #fed7aa', borderRadius: 8, padding: 12 }}>
              <div style={{ fontWeight: 600, marginBottom: 8 }}>Endpoints chưa map vào module đã có:</div>
              <table className="data-table" style={{ width: '100%' }}>
                <thead>
                  <tr>
                    <th>Method</th>
                    <th>Path</th>
                    <th>Tags</th>
                    <th>Known?</th>
                  </tr>
                </thead>
                <tbody>
                  {unknown.map((e, i) => (
                    <AuditRow key={`u-${i}-${e.method}-${e.path}`} method={e.method} path={e.path} tags={e.tags} />
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div style={{ background: 'white', border: '1px solid #e5e7eb', borderRadius: 12, overflow: 'hidden' }}>
            <table className="data-table" style={{ width: '100%' }}>
              <thead>
                <tr>
                  <th style={{ width: 100 }}>Method</th>
                  <th>Path</th>
                  <th style={{ width: 260 }}>Tags</th>
                  <th style={{ width: 120 }}>Known?</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((e, i) => (
                  <AuditRow key={`${i}-${e.method}-${e.path}`} method={e.method} path={e.path} tags={e.tags} />
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <style>{`
        .api-audit .badge.method { padding: 4px 8px; border-radius: 6px; font-weight: 600; font-size: 12px; }
        .api-audit .badge.method.GET { background: #ecfeff; color: #0369a1; }
        .api-audit .badge.method.POST { background: #ecfdf5; color: #065f46; }
        .api-audit .badge.method.PUT { background: #fefce8; color: #92400e; }
        .api-audit .badge.method.PATCH { background: #fef3c7; color: #92400e; }
        .api-audit .badge.method.DELETE { background: #fee2e2; color: #991b1b; }
        .api-audit .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; }
        .api-audit .ok { display: inline-flex; align-items: center; gap: 6px; color: #16a34a; }
        .api-audit .warn { display: inline-flex; align-items: center; gap: 6px; color: #b45309; }
      `}</style>
    </div>
  );
};

export default ApiAudit;


