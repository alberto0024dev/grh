import { apiFetch } from './api'

const API_URL = '/api/departments'

export const getAllDepartments = () =>
    apiFetch(API_URL, { fallback: 'Error al obtener departamentos' })

export const getDepartmentById = (id) =>
    apiFetch(`${API_URL}/${id}`, { fallback: 'Departamento no encontrado' })

export const getDepartmentEmployees = (id) =>
    apiFetch(`${API_URL}/${id}/employees`, {
        fallback: 'No se pudieron cargar los empleados del departamento'
    })

export const createDepartment = (department) =>
    apiFetch(API_URL, { method: 'POST', body: department, fallback: 'Error al crear departamento' })

export const updateDepartment = (id, department) =>
    apiFetch(`${API_URL}/${id}`, { method: 'PUT', body: department, fallback: 'Error al actualizar departamento' })

export const deleteDepartment = (id) =>
    apiFetch(`${API_URL}/${id}`, { method: 'DELETE', fallback: 'Error al eliminar departamento' })