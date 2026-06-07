import request from './request'
export const login = (data) => request.post('/auth/login', data)
export const changePassword = (data) => request.put('/auth/change-password', data)
export const getProfile = () => request.get('/profile')
export const getUsers = (params) => request.get('/users', { params })
export const createUser = (data) => request.post('/users', data)
export const updateUser = (id, data) => request.put('/users/' + id, data)
export const deleteUser = (id) => request.delete('/users/' + id)
export const resetPassword = (id, data) => request.put('/users/' + id + '/reset-password', data)