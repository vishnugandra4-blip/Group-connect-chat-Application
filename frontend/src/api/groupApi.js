import axiosClient from './axiosClient';

export const groupApi = {
  getAllGroups: async () => {
    const response = await axiosClient.get('/groups');
    return response.data;
  },

  getMyGroups: async () => {
    const response = await axiosClient.get('/groups/my');
    return response.data;
  },

  getGroupById: async (id) => {
    const response = await axiosClient.get(`/groups/${id}`);
    return response.data;
  },

  createGroup: async (groupData) => {
    const response = await axiosClient.post('/groups', groupData);
    return response.data;
  },

  joinGroup: async (id) => {
    const response = await axiosClient.post(`/groups/${id}/join`);
    return response.data;
  },

  leaveGroup: async (id) => {
    const response = await axiosClient.post(`/groups/${id}/leave`);
    return response.data;
  },

  getGroupMembers: async (id) => {
    const response = await axiosClient.get(`/groups/${id}/members`);
    return response.data;
  },

  addMember: async (groupId, memberData) => {
    const response = await axiosClient.post(`/groups/${groupId}/members`, memberData);
    return response.data;
  },

  removeMember: async (groupId, userId) => {
    const response = await axiosClient.delete(`/groups/${groupId}/members/${userId}`);
    return response.data;
  },
};
