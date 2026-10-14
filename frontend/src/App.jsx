import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import AuthForm from './components/AuthForm';
import Sidebar from './components/Sidebar';
import ChatWindow from './components/ChatWindow';
import CreateGroupModal from './components/CreateGroupModal';
import MembersModal from './components/MembersModal';
import AddMemberModal from './components/AddMemberModal';
import { groupApi } from './api/groupApi';
import { MessageSquare, Users, Shield } from 'lucide-react';

function ChatDashboard() {
  const { user, token, logout } = useAuth();
  const [myGroups, setMyGroups] = useState([]);
  const [allGroups, setAllGroups] = useState([]);
  const [activeGroup, setActiveGroup] = useState(null);
  const [groupMembers, setGroupMembers] = useState([]);

  // Modal controls
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);
  const [isAddMemberModalOpen, setIsAddMemberModalOpen] = useState(false);

  // Fetch groups
  const loadGroups = async () => {
    try {
      const [mine, all] = await Promise.all([
        groupApi.getMyGroups(),
        groupApi.getAllGroups(),
      ]);
      setMyGroups(mine);
      setAllGroups(all);

      // Keep activeGroup in sync
      if (activeGroup) {
        const updated = all.find((g) => g.id === activeGroup.id);
        if (updated) setActiveGroup(updated);
      }
    } catch (err) {
      console.error('Error fetching groups:', err);
    }
  };

  useEffect(() => {
    loadGroups();
  }, []);

  // Fetch members when members modal opens
  const loadMembers = async (groupId) => {
    try {
      const members = await groupApi.getGroupMembers(groupId);
      setGroupMembers(members);
    } catch (err) {
      console.error('Error loading group members:', err);
    }
  };

  const handleSelectGroup = (group) => {
    setActiveGroup(group);
  };

  const handleCreateGroup = async (groupData) => {
    const created = await groupApi.createGroup(groupData);
    await loadGroups();
    setActiveGroup(created);
  };

  const handleJoinGroup = async (groupId) => {
    const joined = await groupApi.joinGroup(groupId);
    await loadGroups();
    setActiveGroup(joined);
  };

  const handleLeaveGroup = async (groupId) => {
    if (window.confirm('Are you sure you want to leave this group?')) {
      await groupApi.leaveGroup(groupId);
      setActiveGroup(null);
      await loadGroups();
    }
  };

  const handleAddMember = async (memberData) => {
    if (!activeGroup) return;
    await groupApi.addMember(activeGroup.id, memberData);
    await loadMembers(activeGroup.id);
    await loadGroups();
  };

  const handleRemoveMember = async (memberUserId) => {
    if (!activeGroup) return;
    if (window.confirm('Remove this member from the group?')) {
      await groupApi.removeMember(activeGroup.id, memberUserId);
      await loadMembers(activeGroup.id);
      await loadGroups();
    }
  };

  const handleOpenMembersModal = () => {
    if (activeGroup) {
      loadMembers(activeGroup.id);
      setIsMembersModalOpen(true);
    }
  };

  return (
    <div className="chat-layout">
      <Sidebar
        user={user}
        onLogout={logout}
        myGroups={myGroups}
        allGroups={allGroups}
        activeGroup={activeGroup}
        onSelectGroup={handleSelectGroup}
        onOpenCreateGroup={() => setIsCreateModalOpen(true)}
        onJoinGroup={handleJoinGroup}
      />

      <main className="chat-main">
        {activeGroup ? (
          <ChatWindow
            group={activeGroup}
            currentUser={user}
            token={token}
            onOpenMembersModal={handleOpenMembersModal}
            onLeaveGroup={handleLeaveGroup}
          />
        ) : (
          <div className="no-chat-selected">
            <div className="welcome-card">
              <div className="welcome-icon">
                <MessageSquare size={48} />
              </div>
              <h2>Select a Group or Start a New One</h2>
              <p>
                Join an existing team discussion from the sidebar or click "Create New Group"
                to set up a channel for your team.
              </p>
              <div className="welcome-badges">
                <div className="welcome-badge">
                  <Users size={16} />
                  <span>Real-Time Group STOMP</span>
                </div>
                <div className="welcome-badge">
                  <Shield size={16} />
                  <span>Secure JWT Authentication</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Modals */}
      <CreateGroupModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onCreateGroup={handleCreateGroup}
      />

      <MembersModal
        isOpen={isMembersModalOpen}
        onClose={() => setIsMembersModalOpen(false)}
        members={groupMembers}
        currentUserId={user.id}
        isAdmin={activeGroup?.isAdmin}
        onRemoveMember={handleRemoveMember}
        onOpenAddModal={() => {
          setIsMembersModalOpen(false);
          setIsAddMemberModalOpen(true);
        }}
      />

      <AddMemberModal
        isOpen={isAddMemberModalOpen}
        onClose={() => setIsAddMemberModalOpen(false)}
        onAddMember={handleAddMember}
        groupName={activeGroup?.name || ''}
      />
    </div>
  );
}

function MainApp() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Loading GroupConnect...</p>
      </div>
    );
  }

  return isAuthenticated ? <ChatDashboard /> : <AuthForm />;
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
