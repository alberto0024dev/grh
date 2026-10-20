import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

function NotFound() {
    const { token } = useAuth()

    return (
        <div className="not-found">
            <h1>404</h1>
            <p>La página que buscas no existe.</p>
            <Link to={token ? '/' : '/login'}>
                {token ? 'Volver al inicio' : 'Ir a iniciar sesión'}
            </Link>
        </div>
    )
}

export default NotFound