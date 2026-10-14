import React from 'react';
import { X, ShieldCheck, User, Trash2 } from 'lucide-react';

export default function MembersModal({
  isOpen,
  onClose,
  members,
  currentUserId,
  isAdmin,
  onRemoveMember,
  onOpenAddModal,
}) {
  if (!isOpen) return null;

  return (
    <div className="modal-backdrop">
      <div className="modal-container members-modal">
        <div className="modal-header">
          <div className="modal-title">
            <ShieldCheck size={20} className="text-primary" />
            <h3>Group Members ({members.length})</h3>
          </div>
          <button className="icon-btn" onClick={onClose} aria-label="Close">
            <X size={20} />
          </button>
        </div>

        <div className="members-modal-actions">
          <button className="btn-primary full-width" onClick={onOpenAddModal}>
            + Invite New Member
          </button>
        </div>

        <div className="members-list">
          {members.map((member) => {
            const isCurrentUser = member.userId === currentUserId;
            const canRemove = isAdmin && !isCurrentUser;

            return (
              <div key={member.id} className="member-row">
                <div className="member-info">
                  <div className="member-avatar">
                    {(member.fullName || member.username || 'U')[0].toUpperCase()}
                  </div>
                  <div className="member-names">
                    <span className="member-name">
                      {member.fullName} {isCurrentUser && <span className="you-badge">(You)</span>}
                    </span>
                    <span className="member-handle">@{member.username}</span>
                  </div>
                </div>

                <div className="member-meta">
                  <span className={`role-badge ${member.role === 'ADMIN' ? 'role-admin' : 'role-member'}`}>
                    {member.role}
                  </span>

                  {canRemove && (
                    <button
                      className="icon-btn danger"
                      onClick={() => onRemoveMember(member.userId)}
                      title="Remove member"
                    >
                      <Trash2 size={16} />
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <div className="modal-actions">
          <button type="button" className="btn-secondary full-width" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
