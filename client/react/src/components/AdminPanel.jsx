import { useEffect, useState } from 'react';
import { listUsers, assignRole } from '../api';
import { useAuth } from '../context/AuthContext';

const ALL_ROLES = ['USER', 'REVIEWER', 'ADMIN', 'OWNER'];

function assignableRoles(callerRole) {
  if (callerRole === 'OWNER') return ALL_ROLES;
  if (callerRole === 'ADMIN') return ['USER', 'REVIEWER'];
  return [];
}

export default function AdminPanel() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(null); // userId being saved

  useEffect(() => {
    listUsers()
      .then(setUsers)
      .catch(() => setError('ব্যবহারকারী তালিকা লোড করা যায়নি'))
      .finally(() => setLoading(false));
  }, []);

  async function handleRoleChange(userId, newRole) {
    setSaving(userId);
    try {
      const updated = await assignRole(userId, newRole);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: updated.role } : u));
    } catch {
      setError('ভূমিকা পরিবর্তন করা যায়নি');
    } finally {
      setSaving(null);
    }
  }

  const callerRole = user?.role || 'USER';
  const allowed = assignableRoles(callerRole);

  return (
    <div className="admin-panel">
      <h2 className="admin-panel-title">অ্যাডমিন — ব্যবহারকারী ব্যবস্থাপনা</h2>
      {error && <p className="admin-error">{error}</p>}
      {loading ? (
        <p className="admin-loading">লোড হচ্ছে…</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>নাম</th>
              <th>ইমেইল</th>
              <th>ভূমিকা</th>
            </tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.id}>
                <td>{u.name || '—'}</td>
                <td>{u.email}</td>
                <td>
                  {allowed.length > 0 ? (
                    <select
                      value={u.role}
                      disabled={saving === u.id}
                      onChange={e => handleRoleChange(u.id, e.target.value)}
                    >
                      {ALL_ROLES.map(r => (
                        <option key={r} value={r} disabled={!allowed.includes(r)}>
                          {r}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <span>{u.role}</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
