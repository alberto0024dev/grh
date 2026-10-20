import { useState, useEffect } from 'react'
import { getAllEmployees } from '../services/employeeService'
import { createContract } from '../services/contractService'
import {toContractPayload} from "../utils/contractPayload.js";

const emptyForm = {
    employeeId: '',
    contractType: 'PERMANENT',
    commencementDate: '',
    expiryDate: '',
    vacationDays: 22,
    salary: '',
    workPlace: 'OFFICE',
    workSchedule: {
        weeklyHours: 40,
        startTime: '09:00',
        endTime: '18:00',
        daysOff: 2
    }
}

// Etiqueta + campo en un bloque. Con required, la etiqueta lleva el asterisco rojo
function Field({ label, required = false, children }) {
    return (
        <div className="field">
            <label className={required ? 'required' : ''}>{label}</label>
            {children}
        </div>
    )
}

function Contracts() {
    const [employees, setEmployees] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [form, setForm] = useState(emptyForm)
    const [formError, setFormError] = useState('')
    const [success, setSuccess] = useState('')

    const fetchEmployees = async () => {
        setEmployees(await getAllEmployees())
    }

    useEffect(() => {
        const load = async () => {
            try {
                await fetchEmployees()
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

    const handleScheduleChange = (e) => {
        const { name, value } = e.target
        setForm({ ...form, workSchedule: { ...form.workSchedule, [name]: value } })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setFormError('')
        setSuccess('')
        try {
            await createContract({
                ...toContractPayload(form),
                employeeId: Number(form.employeeId)

            })
            setSuccess('Contrato creado correctamente')
            setForm(emptyForm)
            await fetchEmployees()      // el empleado ya no aparece en el desplegable
        } catch (err) {
            setFormError(err.message)
        }
    }

    if (loading) return <p>Cargando...</p>
    if (error) return <p className="msg-error">{error}</p>

    const availableEmployees = employees.filter(emp => !emp.contract)

    return (
        <div className="page-narrow">
            <h1>Nuevo contrato</h1>

            {success && <p className="msg-success">{success}</p>}

            {availableEmployees.length === 0 ? (
                <p>Todos los empleados tienen ya un contrato.</p>
            ) : (
                <form onSubmit={handleSubmit} className="form-wide">
                    <h3>Datos del contrato</h3>
                    <p className="required-note"><span>*</span> Campo obligatorio</p>

                    <div className="form-grid">
                        <Field label="Empleado" required>
                            <select name="employeeId" value={form.employeeId} onChange={handleChange} required>
                                <option value="">Selecciona un empleado</option>
                                {availableEmployees.map(emp => (
                                    <option key={emp.id} value={emp.id}>
                                        {emp.name} {emp.firstSurname}
                                    </option>
                                ))}
                            </select>
                        </Field>

                        <Field label="Tipo de contrato" required>
                            <select name="contractType" value={form.contractType} onChange={handleChange}>
                                <option value="PERMANENT">PERMANENT</option>
                                <option value="TEMPORARY">TEMPORARY</option>
                                <option value="INTERNSHIP">INTERNSHIP</option>
                            </select>
                        </Field>

                        <Field label="Lugar de trabajo" required>
                            <select name="workPlace" value={form.workPlace} onChange={handleChange}>
                                <option value="OFFICE">OFFICE</option>
                                <option value="REMOTE">REMOTE</option>
                                <option value="HYBRID">HYBRID</option>
                            </select>
                        </Field>

                        <Field label="Fecha de inicio" required>
                            <input name="commencementDate" type="date" value={form.commencementDate}
                                   onChange={handleChange} required />
                        </Field>

                        {form.contractType !== 'PERMANENT' && (
                            <Field label="Fecha de fin" required>
                                <input name="expiryDate" type="date" value={form.expiryDate}
                                       onChange={handleChange} required />
                            </Field>
                        )}

                        <Field label="Salario anual (€)" required>
                            <input name="salary" type="number" step="0.01" min="0" value={form.salary}
                                   onChange={handleChange} placeholder="Ej. 30000" required />
                        </Field>

                        <Field label="Días de vacaciones" required>
                            <input name="vacationDays" type="number" min="0" max="60" value={form.vacationDays}
                                   onChange={handleChange} required />
                        </Field>
                    </div>

                    <h3>Horario</h3>
                    <div className="form-grid">
                        <Field label="Horas semanales" required>
                            <input name="weeklyHours" type="number" min="0" max="60" value={form.workSchedule.weeklyHours}
                                   onChange={handleScheduleChange} required />
                        </Field>
                        <Field label="Días libres" required>
                            <input name="daysOff" type="number" min="0" max="7" value={form.workSchedule.daysOff}
                                   onChange={handleScheduleChange} required />
                        </Field>
                        <Field label="Hora de inicio" required>
                            <input name="startTime" type="time" value={form.workSchedule.startTime}
                                   onChange={handleScheduleChange} required />
                        </Field>
                        <Field label="Hora de fin" required>
                            <input name="endTime" type="time" value={form.workSchedule.endTime}
                                   onChange={handleScheduleChange} required />
                        </Field>
                    </div>

                    <div className="form-actions">
                        <button type="submit">Crear contrato</button>
                    </div>
                    {formError && <p className="msg-error">{formError}</p>}
                </form>
            )}
        </div>
    )
}

export default Contracts