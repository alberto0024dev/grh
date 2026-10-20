import { useState } from 'react'
import { updateContract } from '../services/contractService'
import {toContractPayload} from "../utils/contractPayload.js";

function ContractEditForm({ contract, onSaved, onCancel }) {
    // Precargamos con los datos actuales. La hora llega como "09:00:00"; el input quiere "09:00"
    const [form, setForm] = useState({
        contractType: contract.contractType,
        commencementDate: contract.commencementDate,
        expiryDate: contract.expiryDate ?? '',
        vacationDays: contract.vacationDays,
        salary: contract.salary,
        workPlace: contract.workPlace,
        workSchedule: {
            weeklyHours: contract.workSchedule.weeklyHours,
            startTime: contract.workSchedule.startTime.slice(0, 5),
            endTime: contract.workSchedule.endTime.slice(0, 5),
            daysOff: contract.workSchedule.daysOff
        }
    })
    const [formError, setFormError] = useState('')

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, [name]: value })
    }

    const handleScheduleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, workSchedule: { ...form.workSchedule, [name]: value } })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setFormError('')
        try {
            const updated = await updateContract(contract.id, toContractPayload(form))
            onSaved(updated)
        } catch (err) {
            setFormError(err.message)
        }
    }

    return (
        <form onSubmit={handleSubmit} style={{ marginBottom: '1.5rem' }}>
            <h3>Editar contrato</h3>
            <p className="required-note"><span>*</span> Campo obligatorio</p>

            <div className="form-grid">
                <div className="field">
                    <label className="required">Tipo de contrato</label>
                    <select name="contractType" value={form.contractType} onChange={handleChange}>
                        <option value="PERMANENT">PERMANENT</option>
                        <option value="TEMPORARY">TEMPORARY</option>
                        <option value="INTERNSHIP">INTERNSHIP</option>
                    </select>
                </div>
                <div className="field">
                    <label className="required">Fecha de inicio</label>
                    <input name="commencementDate" type="date" value={form.commencementDate}
                           onChange={handleChange} required />
                </div>
                {form.contractType !== 'PERMANENT' && (
                    <div className="field">
                        <label className="required">Fecha de fin</label>
                        <input name="expiryDate" type="date" value={form.expiryDate}
                               onChange={handleChange} required />
                    </div>
                )}
                <div className="field">
                    <label className="required">Días de vacaciones</label>
                    <input name="vacationDays" type="number" min="0" max="60" value={form.vacationDays}
                           onChange={handleChange} required />
                </div>
                <div className="field">
                    <label className="required">Salario anual (€)</label>
                    <input name="salary" type="number" step="0.01" min="0" value={form.salary}
                           onChange={handleChange} required />
                </div>
                <div className="field">
                    <label className="required">Lugar de trabajo</label>
                    <select name="workPlace" value={form.workPlace} onChange={handleChange}>
                        <option value="OFFICE">OFFICE</option>
                        <option value="REMOTE">REMOTE</option>
                        <option value="HYBRID">HYBRID</option>
                    </select>
                </div>
            </div>

            <h4>Horario</h4>
            <div className="form-grid">
                <div className="field">
                    <label className="required">Horas semanales</label>
                    <input name="weeklyHours" type="number" min="0" max="60" value={form.workSchedule.weeklyHours}
                           onChange={handleScheduleChange} required />
                </div>
                <div className="field">
                    <label className="required">Días libres</label>
                    <input name="daysOff" type="number" min="0" max="7" value={form.workSchedule.daysOff}
                           onChange={handleScheduleChange} required />
                </div>
                <div className="field">
                    <label className="required">Hora de inicio</label>
                    <input name="startTime" type="time" value={form.workSchedule.startTime}
                           onChange={handleScheduleChange} required />
                </div>
                <div className="field">
                    <label className="required">Hora de fin</label>
                    <input name="endTime" type="time" value={form.workSchedule.endTime}
                           onChange={handleScheduleChange} required />
                </div>
            </div>

            <div className="form-actions" style={{ gap: '0.5rem' }}>
                <button type="button" className="btn-secondary" onClick={onCancel}>Cancelar</button>
                <button type="submit">Guardar cambios</button>
            </div>
            {formError && <p className="msg-error">{formError}</p>}
        </form>
    )
}

export default ContractEditForm