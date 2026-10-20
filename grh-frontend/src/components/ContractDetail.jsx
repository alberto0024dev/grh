import { useState, useEffect } from 'react'
import { getContractByEmployee } from '../services/contractService'
import ContractEditForm from './ContractEditForm'
import {hour, money} from "../utils/format.js";


function ContractDetail({ employeeId, editable = false, onClose }) {
    const [contract, setContract] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [editing, setEditing] = useState(false)

    useEffect(() => {
        const load = async () => {
            try {
                setContract(await getContractByEmployee(employeeId))
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        load()
    }, [employeeId])

    if (loading) return <p>Cargando contrato...</p>
    if (error) return <p className="msg-error">{error}</p>
    if (!contract) return <p>Este empleado no tiene contrato.</p>

    if (editing) {
        return (
            <ContractEditForm
                contract={contract}
                onCancel={() => setEditing(false)}
                onSaved={(updated) => {
                    setContract(updated)
                    setEditing(false)
                }}
            />
        )
    }

    return (
        <div className="panel data-card">
            <h3>Contrato</h3>
            <p><span className="label">Tipo:</span> {contract.contractType}</p>
            <p><span className="label">Inicio:</span> {contract.commencementDate}</p>
            <p><span className="label">Fin:</span> {contract.expiryDate ?? 'Indefinido'}</p>
            <p><span className="label">Salario anual:</span> {money(contract.salary)}</p>
            <p><span className="label">Vacaciones:</span> {contract.vacationDays} días</p>
            <p><span className="label">Lugar de trabajo:</span> {contract.workPlace}</p>
            <p>
                <span className="label">Horario:</span> {hour(contract.workSchedule.startTime)} - {hour(contract.workSchedule.endTime)},
                {' '}{contract.workSchedule.weeklyHours} h/semana, {contract.workSchedule.daysOff} días libres
            </p>

            {(onClose || editable) && (
                <div className="card-actions">
                    {onClose && (
                        <button className="btn-secondary" onClick={onClose}>Cerrar</button>
                    )}
                    {editable && <button onClick={() => setEditing(true)}>Editar contrato</button>}
                </div>
            )}
        </div>
    )
}

export default ContractDetail