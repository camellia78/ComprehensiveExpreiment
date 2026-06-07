import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login/LoginView.vue') },
  { path: '/', component: () => import('../views/layout/MainLayout.vue'), redirect: '/dashboard', meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/DashboardView.vue') },
      { path: 'dorm/buildings', name: 'Buildings', meta: { role: [0] }, component: () => import('../views/dorm/BuildingView.vue') },
      { path: 'dorm/rooms', name: 'Rooms', meta: { role: [0] }, component: () => import('../views/dorm/RoomView.vue') },
      { path: 'accom/checkin', name: 'Checkin', meta: { role: [0, 2] }, component: () => import('../views/accom/CheckinView.vue') },
      { path: 'accom/transfer', name: 'Transfer', meta: { role: [0, 2] }, component: () => import('../views/accom/TransferView.vue') },
      { path: 'accom/checkout', name: 'Checkout', meta: { role: [0, 2] }, component: () => import('../views/accom/CheckoutView.vue') },
      { path: 'repair/my', name: 'MyRepair', meta: { role: [1] }, component: () => import('../views/repair/MyRepairView.vue') },
      { path: 'repair/manage', name: 'RepairManage', meta: { role: [0, 2] }, component: () => import('../views/repair/RepairManageView.vue') },
      { path: 'system/users', name: 'Users', meta: { role: [0, 2] }, component: () => import('../views/system/UserView.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/profile/ProfileView.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = parseInt(localStorage.getItem('role') || '-1')
  if (to.path === '/login') { next(token ? '/dashboard' : undefined) }
  else if (!token) { next('/login') }
  else if (to.meta.role) {
    const allowed = Array.isArray(to.meta.role) ? to.meta.role.includes(role) : to.meta.role === role
    if (!allowed) { next('/dashboard') } else { next() }
  }
  else { next() }
})

export default router