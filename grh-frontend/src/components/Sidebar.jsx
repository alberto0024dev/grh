import { NavLink, useNavigate } from 'react-router-dom'
import {
    Users, Building2, Briefcase, FileText, BarChart3,
    UserCircle, KeyRound, LogOut
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'

const roleLabels = {
    ADMIN: 'Administrador',
    MANAGER: 'Manager',
    EMPLOYEE: 'Empleado'
}

function Sidebar() {
    const { role, logoutUser } = useAuth()
    const navigate = useNavigate()

    const handleLogout = () => {
        logoutUser()
        navigate('/login')
    }

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <small>{roleLabels[role]}</small>
                <strong>GestiónRRHH</strong>
            </div>

            <nav className="sidebar-links">
                {role === 'ADMIN' && (
                    <>
                        <NavLink to="/employees"><Users size={18} />Empleados</NavLink>
                        <NavLink to="/departments"><Building2 size={18} />Departamentos</NavLink>
                        <NavLink to="/positions"><Briefcase size={18} />Cargos</NavLink>
                        <NavLink to="/contracts"><FileText size={18} />Contratos</NavLink>
                        <NavLink to="/reports"><BarChart3 size={18} />Reportes</NavLink>
                    </>
                )}
                {role === 'MANAGER' && (
                    <>
                        <NavLink to="/my-department"><Building2 size={18} />Mi departamento</NavLink>
                        <NavLink to="/reports"><BarChart3 size={18} />Reportes</NavLink>
                    </>
                )}
                {role !== 'ADMIN' && <NavLink to="/profile"><UserCircle size={18} />Mi perfil</NavLink>}
            </nav>

            <div className="sidebar-bottom">
                <NavLink to="/change-password"><KeyRound size={18}/>Cambiar contraseña</NavLink>
                <button className="sidebar-logout" onClick={handleLogout}>
                    <LogOut size={16} />Cerrar sesión
                </button>
            </div>
        </aside>
    )
}

export default Sidebar