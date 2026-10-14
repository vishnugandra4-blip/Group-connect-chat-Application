import React, { useState } from 'react';
import {
  MessageSquare,
  Plus,
  Search,
  Users,
  LogOut,
  Hash,
  Check,
} from 'lucide-react';

export default function Sidebar({
  user,
  onLogout,
  myGroups,
  allGroups,
  activeGroup,
  onSelectGroup,
  onOpenCreateGroup,
  onJoinGroup,
}) {
  const [activeTab, setActiveTab] = useState('my'); // 'my' or 'all'
  const [searchQuery, setSearchQuery] = useState('');

  const displayedGroups = (activeTab === 'my' ? myGroups : allGroups).filter((g) =>
    g.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (g.description && g.description.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <aside className="sidebar">
      {/* Brand Header */}
      <div className="sidebar-brand">
        <div className="brand-badge-small">
          <MessageSquare size={20} />
        </div>
        <div className="brand-text">
          <h2>GroupConnect</h2>
          <span className="live-pill">Live STOMP</span>
        </div>
      </div>

      {/* User Card */}
      <div className="sidebar-user-card">
        <div className="user-avatar-small">
          {(user.fullName || user.username || 'U')[0].toUpperCase()}
        </div>
        <div className="user-details">
          <span className="user-name">{user.fullName || user.username}</span>
          <span className="user-handle">@{user.username}</span>
        </div>
        <button className="icon-btn logout-btn" onClick={onLogout} title="Log Out">
          <LogOut size={16} />
        </button>
      </div>

      {/* Action Button */}
      <button className="btn-primary full-width create-btn" onClick={onOpenCreateGroup}>
        <Plus size={18} />
        <span>Create New Group</span>
      </button>

      {/* Tabs */}
      <div className="sidebar-tabs">
        <button
          className={`sidebar-tab ${activeTab === 'my' ? 'active' : ''}`}
          onClick={() => setActiveTab('my')}
        >
          My Groups ({myGroups.length})
        </button>
        <button
          className={`sidebar-tab ${activeTab === 'all' ? 'active' : ''}`}
          onClick={() => setActiveTab('all')}
        >
          Explore All ({allGroups.length})
        </button>
      </div>

      {/* Search Input */}
      <div className="sidebar-search">
        <Search size={16} className="search-icon" />
        <input
          type="text"
          placeholder="Search groups..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {/* Groups List */}
      <div className="groups-list">
        {displayedGroups.length === 0 ? (
          <div className="sidebar-empty">
            <Users size={28} className="empty-icon" />
            <p>{searchQuery ? 'No matching groups found' : (activeTab === 'my' ? 'You have not joined any groups yet' : 'No groups available')}</p>
          </div>
        ) : (
          displayedGroups.map((group) => {
            const isSelected = activeGroup?.id === group.id;
            const isMember = group.isMember || myGroups.some((mg) => mg.id === group.id);

            return (
              <div
                key={group.id}
                className={`group-item ${isSelected ? 'selected' : ''}`}
                onClick={() => {
                  if (isMember) {
                    onSelectGroup(group);
                  }
                }}
              >
                <div className="group-item-icon">
                  <Hash size={18} />
                </div>
                <div className="group-item-content">
                  <div className="group-item-title-row">
                    <span className="group-item-name">{group.name}</span>
                    {group.isAdmin && <span className="mini-admin-pill">Admin</span>}
                  </div>
                  <span className="group-item-desc">
                    {group.description || `${group.memberCount} members`}
                  </span>
                </div>

                {!isMember ? (
                  <button
                    className="btn-join"
                    onClick={(e) => {
                      e.stopPropagation();
                      onJoinGroup(group.id);
                    }}
                  >
                    Join
                  </button>
                ) : (
                  <span className="joined-check" title="Joined">
                    <Check size={14} />
                  </span>
                )}
              </div>
            );
          })
        )}
      </div>
    </aside>
  );
}
