import { useState, useEffect } from 'react'
import {
    getAllPositions,
    createPosition,
    updatePosition,
    deletePosition
} from '../services/positionService'
import { useConfirm } from '../context/ConfirmContext'

const emptyForm = { name: '', jobCategory: 'JUNIOR' }

function Positions() {
    const [positions, setPositions] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [form, setForm] = useState(emptyForm)
    const [editingId, setEditingId] = useState(null)   // null = crear, número = editar
    const [formError, setFormError] = useState('')
    const [actionError, setActionError] = useState('')

    const confirm = useConfirm()

    const fetchPositions = async () => {
        try {
            setPositions(await getAllPositions())
        } catch (err) {
            setActionError(err.message)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchPositions()
    }, [])

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, [name]: value })
    }

    const startEditing = (pos) => {
        setEditingId(pos.id)
        setForm({ name: pos.name, jobCategory: pos.jobCategory })
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
        const message = editingId ? 'Cargo actualizado correctamente' : 'Cargo creado correctamente'
        try {
            if (editingId) await updatePosition(editingId, form)
            else await createPosition(form)
            cancelEditing()
            setSuccess(message)
            await fetchPositions()
        } catch (err) {
            setFormError(err.message)
        }
    }

    const handleDelete = async (pos) => {
        const ok = await confirm({
            title: 'Eliminar cargo',
            message: `¿Seguro que quieres eliminar el cargo "${pos.name}"?`,
            confirmText: 'Eliminar',
            danger: true
        })
        if (!ok) return
        setActionError('')
        setSuccess('')
        try {
            await deletePosition(pos.id)
            await fetchPositions()
        } catch (err) {
            setActionError(err.message)   // aquí sale el 409 si el cargo está en uso
        }
    }

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>

    return (
        <div>
            <h1>Cargos</h1>

            <div className="split-layout">
                <div className="split-main">
                    {success && <p className="msg-success">{success}</p>}
                    {actionError && <p className="msg-error">{actionError}</p>}

                    <table>
                        <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Categoría</th>
                            <th className="actions">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        {positions.length === 0 && (
                            <tr><td colSpan="3">Todavía no hay cargos. Crea el primero con el formulario.</td></tr>
                        )}
                        {positions.map(pos => (
                            <tr key={pos.id} className={pos.id === editingId ? 'editing-row' : ''}>
                                <td>{pos.name}</td>
                                <td>
                                    <span className={`badge badge-${pos.jobCategory.toLowerCase()}`}>
                                        {pos.jobCategory}
                                    </span>
                                </td>
                                <td className="actions">
                                    <button className="btn-secondary" onClick={() => startEditing(pos)}>Editar</button>
                                    <button className="btn-danger" onClick={() => handleDelete(pos)}>Eliminar</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                <form onSubmit={handleSubmit} className="split-side">
                    <h3>{editingId ? 'Editar cargo' : 'Nuevo cargo'}</h3>
                    <p className="required-note"><span>*</span> Campo obligatorio</p>
                    <div className="field">
                        <label className="required">Nombre</label>
                        <input name="name" maxLength={100} value={form.name} onChange={handleChange}
                               placeholder="Ej. Ingeniero de software" required />
                    </div>
                    <div className="field">
                        <label className="required">Categoría</label>
                        <select name="jobCategory" value={form.jobCategory} onChange={handleChange}>
                            <option value="JUNIOR">JUNIOR</option>
                            <option value="SENIOR">SENIOR</option>
                            <option value="LEAD">LEAD</option>
                            <option value="MANAGER">MANAGER</option>
                        </select>
                    </div>
                    <button type="submit">{editingId ? 'Guardar cambios' : 'Crear cargo'}</button>
                    {editingId && (
                        <button type="button" className="btn-secondary" onClick={cancelEditing}>Cancelar</button>
                    )}
                    {formError && <p className="msg-error">{formError}</p>}
                </form>
            </div>
        </div>
    )
}

export default Positions