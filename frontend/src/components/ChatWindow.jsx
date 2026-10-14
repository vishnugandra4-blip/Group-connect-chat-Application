import React, { useState, useEffect, useRef } from 'react';
import { messageApi } from '../api/messageApi';
import { websocketService } from '../api/websocketService';
import { Send, Users, LogOut, Radio, Clock } from 'lucide-react';

export default function ChatWindow({
  group,
  currentUser,
  token,
  onOpenMembersModal,
  onLeaveGroup,
}) {
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [wsConnected, setWsConnected] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // Fetch message history and setup STOMP subscription
  useEffect(() => {
    if (!group) return;

    let isMounted = true;
    setLoading(true);

    // 1. Fetch message history
    messageApi
      .getGroupMessages(group.id)
      .then((history) => {
        if (isMounted) {
          setMessages(history);
          setLoading(false);
          setTimeout(scrollToBottom, 50);
        }
      })
      .catch((err) => {
        console.error('Failed to load message history:', err);
        if (isMounted) setLoading(false);
      });

    // 2. Connect/ensure STOMP client is active
    websocketService.connect(
      token,
      () => {
        if (isMounted) {
          setWsConnected(true);
          // Subscribe to group broadcast destination
          websocketService.subscribeToGroup(group.id, (incomingMsg) => {
            setMessages((prev) => {
              // Avoid duplicates if already present
              if (incomingMsg.id && prev.some((m) => m.id === incomingMsg.id)) {
                return prev;
              }
              return [...prev, incomingMsg];
            });
            setTimeout(scrollToBottom, 50);
          });
        }
      },
      (error) => {
        if (isMounted) setWsConnected(false);
      }
    );

    return () => {
      isMounted = false;
      websocketService.unsubscribeFromGroup(group.id);
    };
  }, [group?.id, token]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    const content = inputText.trim();
    if (!content) return;

    setInputText('');

    try {
      if (websocketService.isConnected()) {
        websocketService.sendMessage(group.id, content);
      } else {
        // Fallback to REST API
        const savedMsg = await messageApi.sendMessage(group.id, content);
        setMessages((prev) => [...prev, savedMsg]);
      }
    } catch (err) {
      console.error('Failed to send message:', err);
      // Fallback to REST API
      try {
        const savedMsg = await messageApi.sendMessage(group.id, content);
        setMessages((prev) => [...prev, savedMsg]);
      } catch (restErr) {
        alert('Failed to send message. Please ensure you are a member of this group.');
      }
    }
  };

  const formatTime = (isoString) => {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '';
    }
  };

  return (
    <div className="chat-window">
      {/* Group Header */}
      <div className="chat-header">
        <div className="chat-header-info">
          <h2>{group.name}</h2>
          <p className="chat-header-desc">
            {group.description || 'No description provided'}
          </p>
        </div>

        <div className="chat-header-actions">
          <div className={`ws-badge ${wsConnected ? 'ws-connected' : 'ws-disconnected'}`}>
            <Radio size={14} className="pulse-icon" />
            <span>{wsConnected ? 'Live STOMP' : 'Connecting'}</span>
          </div>

          <button className="btn-secondary btn-sm" onClick={onOpenMembersModal}>
            <Users size={16} />
            <span>{group.memberCount} Members</span>
          </button>

          <button className="btn-danger-outline btn-sm" onClick={() => onLeaveGroup(group.id)} title="Leave Group">
            <LogOut size={16} />
            <span>Leave</span>
          </button>
        </div>
      </div>

      {/* Messages Scroll Area */}
      <div className="messages-area">
        {loading ? (
          <div className="chat-empty-state">
            <p>Loading messages...</p>
          </div>
        ) : messages.length === 0 ? (
          <div className="chat-empty-state">
            <p className="empty-title">Welcome to #{group.name}!</p>
            <p className="empty-subtitle">This is the start of the group conversation. Send a message to say hello!</p>
          </div>
        ) : (
          messages.map((msg, index) => {
            const isSelf = msg.senderId === currentUser.id;
            const isSystem = msg.type === 'JOIN' || msg.type === 'LEAVE';

            if (isSystem) {
              return (
                <div key={msg.id || index} className="system-message">
                  <span>{msg.content}</span>
                </div>
              );
            }

            return (
              <div
                key={msg.id || index}
                className={`message-row ${isSelf ? 'message-self' : 'message-other'}`}
              >
                {!isSelf && (
                  <div className="message-avatar" title={msg.senderFullName || msg.senderUsername}>
                    {(msg.senderFullName || msg.senderUsername || 'U')[0].toUpperCase()}
                  </div>
                )}
                <div className="message-bubble-wrapper">
                  {!isSelf && (
                    <div className="message-sender-name">
                      {msg.senderFullName || msg.senderUsername}
                    </div>
                  )}
                  <div className="message-bubble">
                    <p className="message-text">{msg.content}</p>
                    <span className="message-time">
                      <Clock size={11} />
                      {formatTime(msg.createdAt)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Message Input Bar */}
      <form onSubmit={handleSendMessage} className="message-input-form">
        <input
          type="text"
          placeholder={`Message #${group.name}...`}
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          className="chat-input"
          maxLength={4000}
        />
        <button
          type="submit"
          className="btn-primary btn-send"
          disabled={!inputText.trim()}
          aria-label="Send Message"
        >
          <Send size={18} />
        </button>
      </form>
    </div>
  );
}
