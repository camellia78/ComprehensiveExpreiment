import request from './request'

export const getCheckins = () => request.get('/checkins')
export const doCheckin = (data) => request.post('/checkins', data)
export const doTransfer = (data) => request.post('/transfers', data)
export const doCheckout = (data) => request.post('/checkouts', data)
