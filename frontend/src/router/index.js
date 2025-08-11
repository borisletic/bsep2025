import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

// Import views
import HomeView from '../views/HomeView.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import Certificates from '../views/Certificates.vue'
import Passwords from '../views/Passwords.vue'
import Templates from '../views/Templates.vue'
import Admin from '../views/Admin.vue'
import Profile from '../views/Profile.vue'
import Sessions from '../views/Sessions.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresAuth: false, redirectIfAuth: true }
  },
  {
    path: '/register',
    name: 'register',
    component: Register,
    meta: { requiresAuth: false, redirectIfAuth: true }
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/certificates',
    name: 'certificates',
    component: Certificates,
    meta: { requiresAuth: true }
  },
  {
    path: '/passwords',
    name: 'passwords',
    component: Passwords,
    meta: { requiresAuth: true }
  },
  {
    path: '/templates',
    name: 'templates',
    component: Templates,
    meta: { requiresAuth: true, requiresRole: ['ADMIN', 'CA_USER'] }
  },
  {
    path: '/admin',
    name: 'admin',
    component: Admin,
    meta: { requiresAuth: true, requiresRole: ['ADMIN'] }
  },
  {
    path: '/profile',
    name: 'profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/sessions',
    name: 'sessions',
    component: Sessions,
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated
  const userRole = store.getters.userRole

  // Check if route requires authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  // Redirect authenticated users away from auth pages
  if (to.meta.redirectIfAuth && isAuthenticated) {
    next('/dashboard')
    return
  }

  // Check role-based access
  if (to.meta.requiresRole && isAuthenticated) {
    if (!to.meta.requiresRole.includes(userRole)) {
      next('/dashboard')
      return
    }
  }

  // Redirect root path to appropriate page
  if (to.path === '/') {
    if (isAuthenticated) {
      next('/dashboard')
    } else {
      next('/login')
    }
    return
  }

  next()
})

export default router