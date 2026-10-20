import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getDepartmentById, getDepartmentEmployees } from '../services/departmentService'

function DepartmentDetail() {
    const { id } = useParams()   // lee el :id de la URL
    const [department, setDepartment] = useState(null)
    const [employees, setEmployees] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        const load = async () => {
            setLoading(true)
            setError('')
            try {
                const [dept, emps] = await Promise.all([
                    getDepartmentById(id),
                    getDepartmentEmployees(id)
                ])
                setDepartment(dept)
                setEmployees(emps)
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        load()
    }, [id])

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>

    return (
        <div>
            <Link to="/departments">← Volver a departamentos</Link>
            <h1>{department.name}</h1>

            <div className="panel data-card">
                <h3>Datos del departamento</h3>
                <p><span className="label">Edificio:</span> {department.building ?? '-'}</p>
                <p><span className="label">Planta:</span> {department.floor ?? '-'}</p>
                <p><span className="label">Dirección:</span> {department.address ?? '-'}</p>
                <p><span className="label">Teléfono:</span> {department.contactPhone ?? '-'}</p>
                <p>
                    <span className="label">Estado:</span>{' '}
                    <span className={`badge badge-${department.status.toLowerCase()}`}>
                    {department.status}
                </span>
                </p>
            </div>

            <h2>Empleados ({employees.length})</h2>
            <table>
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Apellido</th>
                    <th>Email</th>
                    <th>Cargos</th>
                    <th>Estado</th>
                </tr>
                </thead>
                <tbody>
                {employees.length === 0 && (
                    <tr><td colSpan="5">Este departamento no tiene empleados.</td></tr>
                )}
                {employees.map(emp => (
                    <tr key={emp.id}>
                        <td>{emp.name}</td>
                        <td>{emp.firstSurname}</td>
                        <td>{emp.email}</td>
                        <td>{emp.positions.map(p => p.name).join(', ') || '-'}</td>
                        <td>
                            <span className={`badge ${emp.active ? 'badge-available' : 'badge-occupied'}`}>
                                {emp.active ? 'Activo' : 'Inactivo'}
                            </span>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}

export default DepartmentDetail