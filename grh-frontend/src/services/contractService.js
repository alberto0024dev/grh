import { apiFetch } from './api'

const API_URL = '/api/contracts'

export const createContract = (contract) =>
    apiFetch(API_URL, { method: 'POST', body: contract, fallback: 'Error al crear contrato' })

export const updateContract = (id, contract) =>
    apiFetch(`${API_URL}/${id}`, { method: 'PUT', body: contract, fallback: 'Error al actualizar contrato' })

// Un empleado sin contrato no es un error: devolvemos null en vez de lanzar
export const getContractByEmployee = async (employeeId) => {
    try {
        return await apiFetch(`${API_URL}/${employeeId}/employee`, {
            fallback: 'No se pudo cargar el contrato'
        })
    } catch (err) {
        if (err.status === 404) return null
        throw err
    }
}