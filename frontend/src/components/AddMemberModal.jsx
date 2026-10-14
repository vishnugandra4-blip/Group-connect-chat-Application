import React, { useState } from 'react';
import { X, UserPlus, AlertCircle } from 'lucide-react';

export default function AddMemberModal({ isOpen, onClose, onAddMember, groupName }) {
  const [identifier, setIdentifier] = useState('');
  const [role, setRole] = useState('MEMBER');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!identifier.trim()) {
      setError('Username or email is required');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await onAddMember({ usernameOrEmail: identifier.trim(), role });
      setIdentifier('');
      setRole('MEMBER');
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add member to group');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <UserPlus size={20} className="text-primary" />
            <h3>Add Member to {groupName}</h3>
          </div>
          <button className="icon-btn" onClick={onClose} aria-label="Close">
            <X size={20} />
          </button>
        </div>

        {error && (
          <div className="alert-error">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Username or Email Address *</label>
            <input
              type="text"
              placeholder="e.g. alex or alex@example.com"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Membership Role</label>
            <select value={role} onChange={(e) => setRole(e.target.value)}>
              <option value="MEMBER">Member (Read & Post messages)</option>
              <option value="ADMIN">Admin (Manage members & group)</option>
            </select>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Adding...' : 'Add Member'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
