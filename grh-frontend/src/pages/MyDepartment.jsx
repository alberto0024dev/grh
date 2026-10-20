import { useState, useEffect } from 'react'
import { getOwnProfile } from '../services/employeeService'
import { getDepartmentById, getDepartmentEmployees } from '../services/departmentService'
import ContractDetail from '../components/ContractDetail'

function MyDepartment() {
    const [department, setDepartment] = useState(null)
    const [employees, setEmployees] = useState([])
    const [selectedId, setSelectedId] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        const load = async () => {
            try {
                // 1º: averiguar quién soy y en qué departamento estoy
                const profile = await getOwnProfile()
                if (!profile.department) {
                    setError('No tienes departamento asignado')
                    return
                }
                const id = profile.department.id

                // 2º: con el id, pedimos departamento y empleados a la vez
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
    }, [])

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>



    return (
        <div>
            <h1>Mi departamento: {department.name}</h1>

            <div className="panel data-card">
                <h3>Datos del departamento</h3>
                <p><span className="label">Edificio:</span> {department.building ?? '-'}</p>
                <p><span className="label">Planta:</span> {department.floor ?? '-'}</p>
                <p><span className="label">Teléfono:</span> {department.contactPhone ?? '-'}</p>
            </div>

            <h2>Empleados ({employees.length})</h2>

            {selectedId && (
                <ContractDetail key={selectedId} employeeId={selectedId}
                                onClose={() => setSelectedId(null)} />
            )}

            <table>
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Apellido</th>
                    <th>Email</th>
                    <th>Cargos</th>
                    <th>Estado</th>
                    <th>Contrato</th>
                </tr>
                </thead>
                <tbody>
                {employees.length === 0 && (
                    <tr><td colSpan="6">Este departamento no tiene empleados.</td></tr>
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
                        <td>
                            <button className="btn-secondary" onClick={() => setSelectedId(emp.id)}>Ver contrato</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}

export default MyDepartment