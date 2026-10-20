import { useState } from 'react'
import { changePassword } from '../services/authService'

function ChangePassword() {
    const [currentPassword, setCurrentPassword] = useState('')
    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')

    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setSuccess('')
        if (newPassword !== confirmPassword) {
            setError('Las contraseñas nuevas no coinciden')
            return
        }
        try {
            await changePassword(currentPassword, newPassword)
            setSuccess('Contraseña actualizada correctamente')
            setCurrentPassword('')
            setNewPassword('')
            setConfirmPassword('')
        } catch (err) {
            setError(err.message)
        }
    }

    return (
        <div className="page-narrow">
            <h1>Cambiar contraseña</h1>

            <form onSubmit={handleSubmit} className="form-wide">
                <p className="hint">
                    Mínimo 8 caracteres, con al menos una letra y un número.

                </p>

                <div className="form-grid">
                    <div className="field">
                        <label>Contraseña actual</label>
                        <input type="password" maxLength={72} value={currentPassword} autoComplete="current-password"
                               onChange={(e) => setCurrentPassword(e.target.value)} required />
                    </div>
                    <div className="field">
                        <label>Nueva contraseña</label>
                        <input type="password" maxLength={72} value={newPassword} minLength={8} autoComplete="new-password"
                               pattern="(?=.*[A-Za-z])(?=.*\d).+"
                               title="Al menos una letra y un número"
                               onChange={(e) => setNewPassword(e.target.value)} required />
                    </div>
                    <div className="field">
                        <label>Repite la nueva contraseña</label>
                        <input type="password" maxLength={72} value={confirmPassword} minLength={8} autoComplete="new-password"
                               pattern="(?=.*[A-Za-z])(?=.*\d).+"
                               title="Al menos una letra y un número"
                               onChange={(e) => setConfirmPassword(e.target.value)} required />
                    </div>
                </div>

                {error && <p className="msg-error">{error}</p>}
                {success && <p className="msg-success">{success}</p>}

                <div className="form-actions">
                    <button type="submit">Cambiar contraseña</button>
                </div>
            </form>
        </div>
    )
}

export default ChangePassword