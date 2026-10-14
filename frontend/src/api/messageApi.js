import axiosClient from './axiosClient';

export const messageApi = {
  getGroupMessages: async (groupId) => {
    const response = await axiosClient.get(`/groups/${groupId}/messages`);
    return response.data;
  },

  sendMessage: async (groupId, content) => {
    const response = await axiosClient.post(`/groups/${groupId}/messages`, { content });
    return response.data;
  },
};
