import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
    getAllDepartments,
    createDepartment,
    updateDepartment,
    deleteDepartment
} from '../services/departmentService'
import { getAllEmployees } from '../services/employeeService'
import { useConfirm } from '../context/ConfirmContext'

const emptyForm = {
    name: '',
    building: '',
    floor: 0,
    address: '',
    status: 'AVAILABLE',
    contactPhone: '',
    annualBudget: '',
    managerId: ''
}

function Departments() {
    const [departments, setDepartments] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [employees, setEmployees] = useState([])

    const [form, setForm] = useState(emptyForm)
    const [editingId, setEditingId] = useState(null)
    const [formError, setFormError] = useState('')
    const [actionError, setActionError] = useState('')

    const confirm = useConfirm()

    const fetchDepartments = async () => {
        try {
            setDepartments(await getAllDepartments())
        } catch (err) {
            setActionError(err.message)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        const load = async () => {
            try {
                const [depts, emps] = await Promise.all([
                    getAllDepartments(),
                    getAllEmployees()
                ])
                setDepartments(depts)
                setEmployees(emps)
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        load()
    }, [])

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, [name]: value })
    }

    const startEditing = (dept) => {
        setEditingId(dept.id)
        setForm({
            name: dept.name,
            building: dept.building ?? '',
            floor: dept.floor ?? 0,
            address: dept.address ?? '',
            status: dept.status,
            contactPhone: dept.contactPhone ?? '',
            annualBudget: dept.annualBudget,
            managerId: dept.managerId ?? ''
        })
        setFormError('')
        setActionError('')
        setSuccess('')
    }

    const cancelEditing = () => {
        setEditingId(null)
        setForm(emptyForm)
        setFormError('')
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setFormError('')
        setSuccess('')
        const message = editingId ? 'Departamento actualizado correctamente' : 'Departamento creado correctamente'
        const data = {
            ...form,
            contactPhone: form.contactPhone.trim() === '' ? null : form.contactPhone.trim(),
            floor: Number(form.floor),
            annualBudget: Number(form.annualBudget),
            managerId: form.managerId === '' ? null : Number(form.managerId)
        }
        try {
            if (editingId) await updateDepartment(editingId, data)
            else await createDepartment(data)
            cancelEditing()
            setSuccess(message)
            await fetchDepartments()
        } catch (err) {
            setFormError(err.message)
        }
    }

    const handleDelete = async (dept) => {
        const ok = await confirm({
            title: 'Eliminar departamento',
            message: `¿Seguro que quieres eliminar el departamento "${dept.name}"?`,
            confirmText: 'Eliminar',
            danger: true
        })
        if (!ok) return
        setActionError('')
        setSuccess('')
        try {
            await deleteDepartment(dept.id)
            await fetchDepartments()
        } catch (err) {
            setActionError(err.message)
        }
    }

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>

    return (
        <div>
            <h1>Departamentos</h1>

            <div className="split-layout">
                <div className="split-main">
                    {success && <p className="msg-success">{success}</p>}
                    {actionError && <p className="msg-error">{actionError}</p>}

                    <table>
                        <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Edificio</th>
                            <th>Planta</th>
                            <th>Teléfono</th>
                            <th>Estado</th>
                            <th className="actions">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        {departments.length === 0 && (
                            <tr><td colSpan="6">Todavía no hay departamentos. Crea el primero con el formulario.</td></tr>
                        )}
                        {departments.map(dept => (
                            <tr key={dept.id} className={dept.id === editingId ? 'editing-row' : ''}>
                                <td><Link to={`/departments/${dept.id}`}>{dept.name}</Link></td>
                                <td>{dept.building ?? '-'}</td>
                                <td>{dept.floor ?? '-'}</td>
                                <td>{dept.contactPhone ?? '-'}</td>
                                <td>
                                    <span className={`badge badge-${dept.status.toLowerCase()}`}>
                                        {dept.status}
                                    </span>
                                </td>
                                <td className="actions">
                                    <button className="btn-secondary" onClick={() => startEditing(dept)}>Editar</button>
                                    <button className="btn-danger" onClick={() => handleDelete(dept)}>Eliminar</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                <form onSubmit={handleSubmit} className="split-side">
                    <h3>{editingId ? 'Editar departamento' : 'Nuevo departamento'}</h3>
                    <p className="required-note"><span>*</span> Campo obligatorio</p>

                    <div className="field">
                        <label className="required">Nombre</label>
                        <input name="name" maxLength={100} value={form.name} onChange={handleChange} required />
                    </div>
                    <div className="field">
                        <label className="required">Edificio</label>
                        <input name="building" maxLength={100} value={form.building} onChange={handleChange} required />
                    </div>
                    <div className="field">
                        <label className="required">Planta</label>
                        <input name="floor" type="number" min="0" max="100" value={form.floor} onChange={handleChange} required/>
                    </div>
                    <div className="field">
                        <label className="required">Dirección</label>
                        <input name="address" maxLength={200} value={form.address} onChange={handleChange} required />
                    </div>
                    <div className="field">
                        <label className="required">Estado</label>
                        <select name="status" value={form.status} onChange={handleChange}>
                            <option value="AVAILABLE">AVAILABLE</option>
                            <option value="OCCUPIED">OCCUPIED</option>
                            <option value="MAINTENANCE">MAINTENANCE</option>
                        </select>
                    </div>
                    <div className="field">
                        <label>Teléfono de contacto</label>
                        <input name="contactPhone" pattern="\+?\d{9,15}" maxLength={16} title="9 a 15 dígitos" value={form.contactPhone} onChange={handleChange} />
                    </div>
                    <div className="field">
                        <label className="required">Presupuesto anual (€)</label>
                        <input name="annualBudget" type="number" step="0.01" min="0" value={form.annualBudget}
                               onChange={handleChange} placeholder="Ej. 150000" required />
                    </div>
                    <div className="field">
                        <label>Manager</label>
                        <select name="managerId" value={form.managerId} onChange={handleChange}>
                            <option value="">Sin manager</option>
                            {employees
                                .filter(emp => emp.active)
                                .sort((a, b) => a.firstSurname.localeCompare(b.firstSurname))
                                .map(emp => (
                                    <option key={emp.id} value={emp.id}>
                                        {emp.firstSurname}, {emp.name} ({emp.department?.name ?? 'sin departamento'})
                                    </option>
                                ))}
                        </select>
                    </div>

                    <button type="submit">{editingId ? 'Guardar cambios' : 'Crear departamento'}</button>
                    {editingId && (
                        <button type="button" className="btn-secondary" onClick={cancelEditing}>Cancelar</button>
                    )}
                    {formError && <p className="msg-error">{formError}</p>}
                </form>
            </div>
        </div>
    )
}

export default Departments