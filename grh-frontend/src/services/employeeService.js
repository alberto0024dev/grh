import { apiFetch } from './api'

const API_URL = '/api/employees'

export const getAllEmployees = () =>
    apiFetch(API_URL, { fallback: 'Error al obtener empleados' })

export const getEmployeeById = (id) =>
    apiFetch(`${API_URL}/${id}`, { fallback: 'Empleado no encontrado' })

export const createEmployee = (employee) =>
    apiFetch(API_URL, { method: 'POST', body: employee, fallback: 'Error al crear empleado' })

export const updateEmployee = (id, employee) =>
    apiFetch(`${API_URL}/${id}`, { method: 'PUT', body: employee, fallback: 'Error al actualizar empleado' })

export const deactivateEmployee = (id) =>
    apiFetch(`${API_URL}/${id}/deactivate`, { method: 'PATCH', fallback: 'Error al desactivar empleado' })

export const activateEmployee = (id) =>
    apiFetch(`${API_URL}/${id}/activate`, { method: 'PATCH', fallback: 'Error al activar empleado' })

export const getOwnProfile = () =>
    apiFetch(`${API_URL}/me`, { fallback: 'No se pudo cargar tu perfil' })

export const updateOwnProfile = (data) =>
    apiFetch(`${API_URL}/me`, { method: 'PATCH', body: data, fallback: 'No se pudo actualizar tu perfil' })