import api from './api'

export interface AnalyticsCompanyResponse {
  vatCollectedByRate: {
    rate: number
    amount: number
  }[]
  revenueHt: number
  revenueTtc: number
  complianceRate: number
  topErrors: {
    errorCode: string
    errorMessage: string
    count: number
  }[]
  invoicesCount: number
  invoicesCompliant: number
  invoicesNonCompliant: number
}

export interface AnalyticsAdminResponse {
  totalInvoicesAudited: number
  totalCompliant: number
  totalNonCompliant: number
  topGlobalErrors: {
    errorCode: string
    errorMessage: string
    count: number
  }[]
  platformComplianceRate: number
  tokenConsumption: {
    companyName: string
    tokensUsed: number
  }[]
}

export const analyticsService = {
  /**
   * Récupérer les KPI de l'entreprise
   */
  getCompanyKpis: async (): Promise<AnalyticsCompanyResponse> => {
    const response = await api.get<AnalyticsCompanyResponse>('/analytics/company')
    return response.data
  },

  /**
   * Récupérer les KPI globaux (admin uniquement)
   */
  getAdminKpis: async (): Promise<AnalyticsAdminResponse> => {
    const response = await api.get<AnalyticsAdminResponse>('/analytics/admin')
    return response.data
  },
}

