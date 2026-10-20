import { useState, useEffect } from 'react'
import { getOwnProfile, updateOwnProfile } from '../services/employeeService'
import ContractDetail from '../components/ContractDetail'

function Profile() {
    const [profile, setProfile] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [editing, setEditing] = useState(false)
    const [form, setForm] = useState({ phoneNumber: '', address: '', emergencyContact: '' })
    const [formError, setFormError] = useState('')
    const [success, setSuccess] = useState('')

    useEffect(() => {
        const load = async () => {
            try {
                setProfile(await getOwnProfile())
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        load()
    }, [])

    const startEditing = () => {
        setForm({
            phoneNumber: profile.phoneNumber ?? '',
            address: profile.address ?? '',
            emergencyContact: profile.emergencyContact ?? ''
        })
        setFormError('')
        setSuccess('')
        setEditing(true)
    }

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, [name]: value })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setFormError('')
        try {
            const updated = await updateOwnProfile(form)
            setProfile(updated)
            setEditing(false)
            setSuccess('Datos de contacto actualizados correctamente')
        } catch (err) {
            setFormError(err.message)
        }
    }

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>

    return (
        <div className="page-narrow page-medium">
            <h1>Mi perfil</h1>

            <div className="panel data-card">
                <h3>Datos personales</h3>
                <p><span className="label">Nombre:</span> {profile.name} {profile.firstSurname} {profile.secondSurname}</p>
                <p><span className="label">Email:</span> {profile.email}</p>
                <p><span className="label">Departamento:</span> {profile.department?.name ?? 'Sin departamento'}</p>
                <p><span className="label">Cargos:</span> {profile.positions.map(p => p.name).join(', ') || '-'}</p>
            </div>

            <div className="panel data-card">
                <h3>Datos de contacto</h3>

                {success && <p className="msg-success">{success}</p>}

                {editing ? (
                    <form onSubmit={handleSubmit} className="form-inline">
                        <p className="required-note"><span>*</span> Campo obligatorio</p>

                        <div className="form-grid">
                            <div className="field">
                                <label className="required">Teléfono (9 dígitos)</label>
                                <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange}
                                       inputMode="numeric" pattern="\d{9}" maxLength={9}
                                       title="9 dígitos" required />
                            </div>
                            <div className="field">
                                <label className="required">Dirección</label>
                                <input name="address" maxLength={200} value={form.address} onChange={handleChange} required />
                            </div>
                            <div className="field">
                                <label>Contacto de emergencia</label>
                                <input name="emergencyContact" maxLength={100} value={form.emergencyContact}
                                       onChange={handleChange} />
                            </div>
                        </div>

                        <div className="form-actions">
                            <button type="button" className="btn-secondary"
                                    onClick={() => setEditing(false)}>Cancelar</button>
                            <button type="submit">Guardar</button>
                        </div>
                        {formError && <p className="msg-error">{formError}</p>}
                    </form>
                ) : (
                    <>
                        <p><span className="label">Teléfono:</span> {profile.phoneNumber}</p>
                        <p><span className="label">Dirección:</span> {profile.address || '-'}</p>
                        <p><span className="label">Contacto de emergencia:</span> {profile.emergencyContact || '-'}</p>
                        <div className="card-actions">
                            <button onClick={startEditing}>Editar datos de contacto</button>
                        </div>
                    </>
                )}
            </div>
            <ContractDetail employeeId={profile.id} />
        </div>
    )
}

export default Profile