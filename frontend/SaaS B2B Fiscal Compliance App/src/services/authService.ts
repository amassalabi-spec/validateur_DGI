import api from './api'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  companyName?: string
}

export interface AuthResponse {
  token: string
  user: {
    id: number
    email: string
    companyId: number
    companyName: string
    role: string
  }
}

export const authService = {
  /**
   * Enregistrer une nouvelle entreprise et son premier admin
   */
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/register', data)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
    }
    return response.data
  },

  /**
   * Connexion avec email/password
   */
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', credentials)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
    }
    return response.data
  },

  /**
   * Déconnexion
   */
  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    window.dispatchEvent(new CustomEvent('auth:logout'))
  },

  /**
   * Récupérer l'utilisateur actuellement connecté
   */
  getCurrentUser: () => {
    const user = localStorage.getItem('user')
    return user ? JSON.parse(user) : null
  },

  /**
   * Récupérer le token JWT
   */
  getToken: () => localStorage.getItem('token'),

  /**
   * Vérifier si l'utilisateur est authentifié
   */
  isAuthenticated: () => !!localStorage.getItem('token'),
}

