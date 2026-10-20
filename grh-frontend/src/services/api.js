import { buildError } from './apiError'

export const apiFetch = async (path, { method = 'GET', body, fallback = 'Error en la petición' } = {}) => {
    const token = localStorage.getItem('token')
    let response
    try {
        response = await fetch(path, {
            method,
            headers: {
                'Content-Type': 'application/json',
                ...(token && { Authorization: `Bearer ${token}` })
            },
            body: body !== undefined ? JSON.stringify(body) : undefined
        })
    } catch {
        throw new Error('No se pudo conectar con el servidor')
    }

    if (response.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        window.location.href = '/login'
    }
    if (!response.ok) throw await buildError(response, fallback)
    return response.status === 204 ? null : response.json()
}