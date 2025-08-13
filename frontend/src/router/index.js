import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

// Import views
import HomeView from '../views/HomeView.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
// Instead of direct imports, use:
const Dashboard = () => import('../views/Dashboard.vue')
const Certificates = () => import('../views/Certificates.vue')
const Passwords = () => import('../views/Passwords.vue')
const Templates = () => import('../views/Templates.vue')
const Admin = () => import('../views/Admin.vue')
const Profile = () => import('../views/Profile.vue')
const Sessions = () => import('../views/Sessions.vue')

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
  // ISPRAVKA: Koristi namespace-ovane getters
  const isAuthenticated = store.getters['auth/isAuthenticated']
  const userRole = store.getters['auth/userRole']

  console.log('Router guard:', { 
    to: to.path, 
    isAuthenticated, 
    userRole,
    token: localStorage.getItem('token')
  })

  // Check if route requires authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    console.log('Not authenticated, redirecting to login')
    next('/login')
    return
  }

  // Redirect authenticated users away from auth pages
  if (to.meta.redirectIfAuth && isAuthenticated) {
    console.log('Already authenticated, redirecting to dashboard')
    next('/dashboard')
    return
  }

  // Check role-based access
  if (to.meta.requiresRole && isAuthenticated) {
    if (!to.meta.requiresRole.includes(userRole)) {
      console.log('Insufficient role, redirecting to dashboard')
      next('/dashboard')
      return
    }
  }

  // Redirect root path to appropriate page
  if (to.path === '/') {
    if (isAuthenticated) {
      console.log('Root + authenticated -> dashboard')
      next('/dashboard')
    } else {
      console.log('Root + not authenticated -> login')
      next('/login')
    }
    return
  }

  console.log('Navigation allowed')
  next()
})

export default router