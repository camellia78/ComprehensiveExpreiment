import request from './request'
export const submitRepair = (data) => request.post('/repairs', data)
export const getMyRepairs = (params) => request.get('/repairs', { params })
export const getAllRepairs = (params) => request.get('/repairs/all', { params })
export const getProcessHistory = (id) => request.get('/repairs/' + id + '/history')
export const processRepair = (id, data) => request.put('/repairs/' + id + '/process', data)
export const cancelRepair = (id) => request.put('/repairs/' + id + '/cancel')

export const getRepairStats = () => request.get('/repairs/stats')
