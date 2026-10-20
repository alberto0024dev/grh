import { apiFetch } from './api'

const API_URL = '/api/positions'

export const getAllPositions = () =>
    apiFetch(API_URL, { fallback: 'Error al obtener cargos' })

export const createPosition = (position) =>
    apiFetch(API_URL, { method: 'POST', body: position, fallback: 'Error al crear cargo' })

export const updatePosition = (id, position) =>
    apiFetch(`${API_URL}/${id}`, { method: 'PUT', body: position, fallback: 'Error al actualizar cargo' })

export const deletePosition = (id) =>
    apiFetch(`${API_URL}/${id}`, { method: 'DELETE', fallback: 'Error al eliminar cargo' })