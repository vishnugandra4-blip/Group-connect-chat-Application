import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.connected = false;
  }

  connect(token, onConnected, onError) {
    if (this.client && this.connected) {
      if (onConnected) onConnected();
      return;
    }

    const socketUrl = '/ws-chat';

    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        // console.log('[STOMP]', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      this.connected = true;
      if (onConnected) onConnected(frame);
    };

    this.client.onStompError = (frame) => {
      this.connected = false;
      console.error('STOMP protocol error:', frame.headers['message'], frame.body);
      if (onError) onError(frame);
    };

    this.client.onWebSocketClose = () => {
      this.connected = false;
    };

    this.client.activate();
  }

  subscribeToGroup(groupId, onMessageReceived) {
    if (!this.client || !this.connected) {
      console.warn('STOMP client not yet connected, subscription queued');
    }

    const topic = `/topic/group/${groupId}`;

    // Unsubscribe from previous if exists
    if (this.subscriptions.has(groupId)) {
      this.subscriptions.get(groupId).unsubscribe();
    }

    if (this.client && this.client.connected) {
      const subscription = this.client.subscribe(topic, (message) => {
        try {
          const payload = JSON.parse(message.body);
          onMessageReceived(payload);
        } catch (err) {
          console.error('Error parsing STOMP message payload:', err);
        }
      });

      this.subscriptions.set(groupId, subscription);
      return subscription;
    }

    return null;
  }

  unsubscribeFromGroup(groupId) {
    if (this.subscriptions.has(groupId)) {
      this.subscriptions.get(groupId).unsubscribe();
      this.subscriptions.delete(groupId);
    }
  }

  sendMessage(groupId, content) {
    if (!this.client || !this.client.connected) {
      throw new Error('WebSocket is not connected');
    }

    this.client.publish({
      destination: `/app/chat.send/${groupId}`,
      body: JSON.stringify({ content }),
    });
  }

  disconnect() {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions.clear();

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connected = false;
  }

  isConnected() {
    return this.connected;
  }
}

export const websocketService = new WebSocketService();
