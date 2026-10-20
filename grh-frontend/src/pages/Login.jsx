import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../services/authService'
import { useAuth } from '../context/AuthContext'
import {homeFor} from "../utils/roles.js";

function Login() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const { loginUser } = useAuth()
    const navigate = useNavigate() // ← hook para navegar

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setLoading(true)

        try {
            const data = await login(email, password)
            loginUser(data.token, data.role)
            navigate(homeFor(data.role))
        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-brand">
                    <strong>GestiónRRHH</strong>
                    <span>Gestión de empleados, contratos y departamentos</span>
                </div>

                <form onSubmit={handleSubmit} className="login-form">
                    <h1>Iniciar sesión</h1>

                    <div className="field">
                        <label>Email</label>
                        <input type="email" maxLength={200} value={email}
                               onChange={(e) => setEmail(e.target.value)}
                               placeholder="nombre@empresa.com" required />
                    </div>
                    <div className="field">
                        <label>Contraseña</label>
                        <input type="password" maxLength={72} value={password}
                               onChange={(e) => setPassword(e.target.value)} required />
                    </div>

                    {error && <p className="msg-error">{error}</p>}

                    <button type="submit" disabled={loading}>
                        {loading ? 'Entrando...' : 'Entrar'}
                    </button>
                </form>
            </div>
        </div>
    )
}

export default Login