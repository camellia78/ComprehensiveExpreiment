import request from './request'

export const getCheckins = (params) => request.get('/checkins', { params })
export const doCheckin = (data) => request.post('/checkins', data)
export const doTransfer = (data) => request.post('/transfers', data)
export const doCheckout = (data) => request.post('/checkouts', data)