import { apiFetch } from './api'

const API_URL = '/api/auth'

// Sin apiFetch: todavía no hay token y no queremos que un 401 cierre sesión
export const login = async (email, password) => {
    let response
    try {
        response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        })
    } catch {
        throw new Error('No se pudo conectar con el servidor')
    }
    if (!response.ok) throw new Error('Credenciales incorrectas')
    return response.json()   // { token: "...", role: "ADMIN" }
}

export const changePassword = (currentPassword, newPassword) =>
    apiFetch(`${API_URL}/change-password`, {
        method: 'PATCH',
        body: { currentPassword, newPassword },
        fallback: 'No se pudo cambiar la contraseña'
    })