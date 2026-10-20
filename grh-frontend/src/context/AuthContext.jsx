import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem('token'))
    const [role, setRole] = useState(localStorage.getItem('role'))

    const loginUser = (newToken, newRole) => {
        localStorage.setItem('token', newToken)
        localStorage.setItem('role', newRole)
        setToken(newToken)
        setRole(newRole)
    }

    const logoutUser = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        setToken(null)
        setRole(null)
    }

    return (
        <AuthContext.Provider value={{ token, role, loginUser, logoutUser }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    return useContext(AuthContext)
}