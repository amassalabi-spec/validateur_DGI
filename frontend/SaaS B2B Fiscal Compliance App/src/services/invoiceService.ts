import api from './api'

export interface InvoiceUploadResponse {
  id: number
  invoiceNumber: string
  status: 'PENDING_AUDIT' | 'NON_COMPLIANT' | 'COMPLIANT' | 'REGENERATED'
  isCompliant: boolean
  auditReport: {
    totalRules: number
    passedRules: number
    failedRules: number
    errors: Array<{
      ruleCode: string
      fieldName: string
      severity: 'ERROR' | 'WARNING' | 'INFO'
      message: string
      actualValue?: string
      expectedValue?: string
    }>
  }
}

export interface Invoice {
  id: number
  invoiceNumber: string
  date: string
  issuerName: string
  issuerIce: string
  clientName: string
  clientIce: string
  totalHt: number
  totalTva: number
  totalTtc: number
  status: 'PENDING_AUDIT' | 'NON_COMPLIANT' | 'COMPLIANT' | 'REGENERATED'
  isCompliant: boolean
  paymentMethod: string
  createdAt: string
}

export interface InvoiceListResponse {
  content: Invoice[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

export const invoiceService = {
  /**
   * Uploader un fichier facture (docx, xlsx, pdf)
   */
  upload: async (file: File): Promise<InvoiceUploadResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await api.post<InvoiceUploadResponse>('/invoices/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Lister les factures avec pagination et filtres
   */
  list: async (page: number = 0, size: number = 10, status?: string): Promise<InvoiceListResponse> => {
    const params: Record<string, any> = { page, size }
    if (status) {
      params.status = status
    }
    const response = await api.get<InvoiceListResponse>('/invoices', { params })
    return response.data
  },

  /**
   * Récupérer une facture par ID
   */
  getById: async (id: number): Promise<Invoice> => {
    const response = await api.get<Invoice>(`/invoices/${id}`)
    return response.data
  },

  /**
   * Télécharger le PDF d'une facture
   */
  downloadPdf: async (id: number): Promise<Blob> => {
    const response = await api.get(`/invoices/${id}/pdf`, {
      responseType: 'blob',
    })
    return response.data
  },

  /**
   * Corriger une facture et relancer l'audit
   */
  correct: async (id: number, corrections: any): Promise<InvoiceUploadResponse> => {
    const response = await api.put<InvoiceUploadResponse>(`/invoices/${id}/correct`, corrections)
    return response.data
  },

  /**
   * Supprimer une facture
   */
  delete: async (id: number): Promise<void> => {
    await api.delete(`/invoices/${id}`)
  },
}

