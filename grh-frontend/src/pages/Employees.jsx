import { useState, useEffect } from 'react'
import {
    getAllEmployees,
    createEmployee,
    updateEmployee,
    deactivateEmployee,
    activateEmployee
} from '../services/employeeService'
import { getAllDepartments } from '../services/departmentService'
import { getAllPositions } from '../services/positionService'
import ContractDetail from '../components/ContractDetail'
import { useConfirm } from '../context/ConfirmContext'

const emptyForm = {
    name: '',
    firstSurname: '',
    secondSurname: '',
    idNumber: '',
    address: '',
    emergencyContact: '',
    phoneNumber: '',
    email: '',
    socialSecurityNumber: '',
    birthDate: '',
    role: 'EMPLOYEE',
    departmentId: '',
    positionIds: []
}
const EMAIL_DOMAIN = 'grh.com'


// Validación de la letra del DNI/NIE (misma regla que el backend)
const DNI_LETTERS = 'TRWAGMYFPDXBNJZSQVHLCKE'
const isValidDni = (value) => {
    const dni = value.trim().toUpperCase()
    if (!/^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$/.test(dni)) return false
    const digits = dni.slice(0, -1).replace('X', '0').replace('Y', '1').replace('Z', '2')
    return DNI_LETTERS[Number(digits) % 23] === dni.slice(-1)
}

// Fecha de nacimiento: entre 16 y 100 años
const today = new Date()
const maxBirth = new Date(today.getFullYear() - 16, today.getMonth(), today.getDate()).toISOString().slice(0, 10)
const minBirth = new Date(today.getFullYear() - 100, today.getMonth(), today.getDate()).toISOString().slice(0, 10)

function Employees() {
    const [employees, setEmployees] = useState([])
    const [departments, setDepartments] = useState([])
    const [positions, setPositions] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [form, setForm] = useState(emptyForm)
    const [editing, setEditing] = useState(null)
    const [formError, setFormError] = useState('')
    const [actionError, setActionError] = useState('')
    const [success, setSuccess] = useState('')
    const [tempPassword, setTempPassword] = useState('')
    const [contractOf, setContractOf] = useState(null)

    const confirm = useConfirm()

    const fetchEmployees = async () => {
        setEmployees(await getAllEmployees())
    }

    useEffect(() => {
        const loadAll = async () => {
            try {
                const [emps, depts, poss] = await Promise.all([
                    getAllEmployees(),
                    getAllDepartments(),
                    getAllPositions()
                ])
                setEmployees(emps)
                setDepartments(depts)
                setPositions(poss)
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        loadAll()
    }, [])

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, [name]: value })
    }

    const handlePositionsChange = (e) => {
        const selected = Array.from(e.target.selectedOptions, option => Number(option.value))
        setForm({ ...form, positionIds: selected })
    }

    const startEditing = (emp) => {
        setEditing(emp)
        setForm({
            ...emptyForm,
            name: emp.name,
            firstSurname: emp.firstSurname,
            secondSurname: emp.secondSurname ?? '',
            address: emp.address ?? '',
            emergencyContact: emp.emergencyContact ?? '',
            phoneNumber: emp.phoneNumber ?? '',
            departmentId: emp.department?.id ?? '',
            positionIds: emp.positions.map(p => p.id)
        })
        setFormError('')
        setActionError('')
        setSuccess('')
        setTempPassword('')
    }

    const cancelEditing = () => {
        setEditing(null)
        setForm(emptyForm)
        setFormError('')
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setFormError('')
        setSuccess('')
        setTempPassword('')

        if (!editing && !isValidDni(form.idNumber)) {
            setFormError('El DNI o NIE no es válido (revisa la letra)')
            return
        }

        const departmentId = form.departmentId === '' ? null : Number(form.departmentId)
        try {
            if (editing) {
                await updateEmployee(editing.id, {
                    name: form.name,
                    firstSurname: form.firstSurname,
                    secondSurname: form.secondSurname,
                    address: form.address,
                    emergencyContact: form.emergencyContact,
                    phoneNumber: form.phoneNumber,
                    departmentId,
                    positionIds: form.positionIds
                })
                cancelEditing()
                setSuccess('Empleado actualizado correctamente')
            } else {
                const created = await createEmployee({ ...form, departmentId })
                setTempPassword(created.temporaryPassword)
                setForm(emptyForm)
                setSuccess(`Empleado ${created.name} ${created.firstSurname} creado correctamente`)
            }
            await fetchEmployees()
        } catch (err) {
            setFormError(err.message)
        }
    }

    const handleToggleActive = async (emp) => {
        const action = emp.active ? 'desactivar' : 'activar'
        const ok = await confirm({
            title: emp.active ? 'Desactivar empleado' : 'Activar empleado',
            message: `¿Seguro que quieres ${action} a ${emp.name} ${emp.firstSurname}?`,
            confirmText: emp.active ? 'Desactivar' : 'Activar',
            danger: emp.active
        })
        if (!ok) return
        setActionError('')
        setSuccess('')
        try {
            if (emp.active) await deactivateEmployee(emp.id)
            else await activateEmployee(emp.id)
            await fetchEmployees()
        } catch (err) {
            setActionError(err.message)
        }
    }

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>

    return (
        <div>
            <h1>Empleados</h1>

            <div className="split-layout">
                <div className="split-main">
                    {tempPassword && (
                        <div className="notice">
                            Contraseña temporal: <strong>{tempPassword}</strong>
                            {' '}(anótala, no se vuelve a mostrar)
                        </div>
                    )}
                    {success && <p className="msg-success">{success}</p>}
                    {actionError && <p className="msg-error">{actionError}</p>}

                    {contractOf && (
                        <ContractDetail key={contractOf} employeeId={contractOf} editable
                                        onClose={() => setContractOf(null)} />
                    )}

                    <table>
                        <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Email</th>
                            <th>Departamento</th>
                            <th>Estado</th>
                            <th className="actions">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        {employees.length === 0 && (
                            <tr><td colSpan="5">Todavía no hay empleados.</td></tr>
                        )}
                        {employees.map(emp => (
                            <tr key={emp.id} className={emp.id === editing?.id ? 'editing-row' : ''}>
                                <td>{emp.name} {emp.firstSurname}</td>
                                <td>{emp.email}</td>
                                <td>{emp.department?.name ?? 'Sin departamento'}</td>
                                <td>
                                        <span className={`badge ${emp.active ? 'badge-available' : 'badge-occupied'}`}>
                                            {emp.active ? 'Activo' : 'Inactivo'}
                                        </span>
                                </td>
                                <td className="actions">
                                    <button className="btn-secondary" onClick={() => startEditing(emp)}>Editar</button>
                                    <button className="btn-secondary" onClick={() => setContractOf(emp.id)}>Contrato</button>
                                    <button className={emp.active ? 'btn-danger' : ''}
                                            onClick={() => handleToggleActive(emp)}>
                                        {emp.active ? 'Desactivar' : 'Activar'}
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                <form onSubmit={handleSubmit} className="split-side">
                    <h3>{editing ? `Editar a ${editing.name}` : 'Nuevo empleado'}</h3>
                    <p className="required-note"><span>*</span> Campo obligatorio</p>

                    <div className="field">
                        <label className="required">Nombre</label>
                        <input name="name" value={form.name} onChange={handleChange} maxLength={50} required />
                    </div>
                    <div className="field">
                        <label className="required">Primer apellido</label>
                        <input name="firstSurname" value={form.firstSurname} onChange={handleChange}
                               maxLength={50} required />
                    </div>
                    <div className="field">
                        <label>Segundo apellido</label>
                        <input name="secondSurname" value={form.secondSurname} onChange={handleChange}
                               maxLength={50} />
                    </div>

                    {editing ? (
                        <div className="field">
                            <label>Email (usuario de acceso)</label>
                            <input value={editing.email} disabled />
                        </div>
                    ) : (
                        <>
                            <div className="field">
                                <label className="required">DNI o NIE</label>
                                <input name="idNumber" value={form.idNumber} onChange={handleChange}
                                       maxLength={9} placeholder="12345678Z"
                                       pattern="([0-9]{8}|[XYZxyz][0-9]{7})[A-Za-z]"
                                       title="8 números y una letra, o NIE" required />
                            </div>
                            <div className="field">
                                <label className="required">Email corporativo (@{EMAIL_DOMAIN})</label>
                                <input name="email" type="email" value={form.email} onChange={handleChange}
                                       maxLength={200} placeholder={`nombre@${EMAIL_DOMAIN}`}
                                       pattern={`[^@\\s]+@${EMAIL_DOMAIN.replace('.', '\\.')}`}
                                       title={`Debe terminar en @${EMAIL_DOMAIN}`} required />
                            </div>
                            <div className="field">
                                <label className="required">Seguridad social (12 dígitos)</label>
                                <input name="socialSecurityNumber" value={form.socialSecurityNumber}
                                       onChange={handleChange} maxLength={12} pattern="\d{12}"
                                       inputMode="numeric" title="12 dígitos" required />
                            </div>
                            <div className="field">
                                <label className="required">Fecha de nacimiento</label>
                                <input name="birthDate" type="date" value={form.birthDate}
                                       onChange={handleChange} min={minBirth} max={maxBirth} required />
                            </div>
                            <div className="field">
                                <label className="required">Rol del usuario</label>
                                <select name="role" value={form.role} onChange={handleChange}>
                                    <option value="EMPLOYEE">EMPLOYEE</option>
                                    <option value="MANAGER">MANAGER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                            </div>
                        </>
                    )}

                    <div className="field">
                        <label className="required">Teléfono (9 dígitos)</label>
                        <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange}
                               inputMode="numeric" pattern="\d{9}" maxLength={9}
                               title="9 dígitos" placeholder="612345678" required />
                    </div>
                    <div className="field">
                        <label className="required">Dirección</label>
                        <input name="address" value={form.address} onChange={handleChange}
                               maxLength={200} required />
                    </div>
                    <div className="field">
                        <label>Contacto de emergencia</label>
                        <input name="emergencyContact" value={form.emergencyContact}
                               onChange={handleChange} maxLength={100} />
                    </div>
                    <div className="field">
                        <label className="required">Departamento</label>
                        <select name="departmentId" value={form.departmentId} onChange={handleChange} required>
                            <option value="">Selecciona un departamento</option>
                            {departments.map(dept => (
                                <option key={dept.id} value={dept.id}>{dept.name}</option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <label className="required">Cargos (Ctrl + clic para elegir varios)</label>
                        <select multiple required value={form.positionIds.map(String)}
                                onChange={handlePositionsChange}>
                            {positions.map(pos => (
                                <option key={pos.id} value={pos.id}>{pos.name} ({pos.jobCategory})</option>
                            ))}
                        </select>
                    </div>

                    <button type="submit">{editing ? 'Guardar cambios' : 'Crear empleado'}</button>
                    {editing && (
                        <button type="button" className="btn-secondary" onClick={cancelEditing}>Cancelar</button>
                    )}
                    {formError && <p className="msg-error">{formError}</p>}
                </form>
            </div>
        </div>
    )
}

export default Employees