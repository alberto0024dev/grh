import { apiFetch } from './api'

const API_URL = '/api/reports'

// Una sola función para todos los reportes: solo cambia la ruta
export const getReport = (path) =>
    apiFetch(`${API_URL}/${path}`, { fallback: 'Error al cargar el reporte' })