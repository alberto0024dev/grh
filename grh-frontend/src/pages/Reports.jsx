import { useState, useEffect } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { useAuth } from '../context/AuthContext'
import { getAllDepartments } from '../services/departmentService'
import { getOwnProfile } from '../services/employeeService'
import { getReport } from '../services/reportsService'
import {money} from "../utils/format.js";

const currentYear = new Date().getFullYear()
const yearOptions = [0, 1, 2, 3].map(i => currentYear - i)   // este año y los 3 anteriores
const daysOptions = [15, 30, 60, 90]

function Card({ title, value }) {
    return (
        <div className="card">
            <div className="card-title">{title}</div>
            <div className="card-value">{value}</div>
        </div>
    )
}

function BarCard({ title, data, color }) {
    const chartData = Object.entries(data).map(([name, value]) => ({ name, value }))

    return (
        <div className="panel">
            <h3>{title}</h3>
            {chartData.length === 0 ? <p>Sin datos</p> : (
                <ResponsiveContainer width="100%" height={220}>
                    <BarChart data={chartData} layout="vertical" margin={{ left: 10, right: 20 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                        <XAxis type="number" allowDecimals={false} />
                        <YAxis type="category" dataKey="name" width={100} />
                        <Tooltip />
                        <Bar dataKey="value" fill={color} radius={[0, 4, 4, 0]} />
                    </BarChart>
                </ResponsiveContainer>
            )}
        </div>
    )
}

function Reports() {
    const { role } = useAuth()
    const [departments, setDepartments] = useState([])
    const [departmentId, setDepartmentId] = useState('')
    const [year, setYear] = useState(currentYear)
    const [days, setDays] = useState(30)
    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    // 1) Al entrar: el admin carga los departamentos; el manager averigua el suyo
    useEffect(() => {
        const init = async () => {
            try {
                if (role === 'ADMIN') {
                    setDepartments(await getAllDepartments())
                } else {
                    const profile = await getOwnProfile()
                    if (!profile.department) {
                        setError('No tienes departamento asignado')
                        return
                    }
                    setDepartmentId(profile.department.id)
                }
            } catch (err) {
                setError(err.message)
            }
        }
        init()
    }, [role])

    // 2) Recarga los reportes si cambia el departamento, el año o los días
    useEffect(() => {
        if (!departmentId) { setData(null);return }
        let cancelled = false
        const load = async () => {
            setLoading(true)
            setError('')
            try {
                const id = departmentId
                const [activeStatus, averageSalary, averageTenure, averageAge, salaryRange,
                    byContractType, byPosition, byWorkplace, budget, expiring, newHires] =
                    await Promise.all([
                        getReport(`active-employees/${id}`),
                        getReport(`average-salary/${id}`),
                        getReport(`average-tenue/${id}`),
                        getReport(`average-age/${id}`),
                        getReport(`salary-range/${id}`),
                        getReport(`employees-by-contract-type/${id}`),
                        getReport(`employees-by-position/${id}`),
                        getReport(`employees-by-workplace/${id}`),
                        getReport(`budget-situation/${id}`),
                        getReport(`expiring-contracts/${id}?days=${days}`),
                        getReport(`new-hires/${id}/${year}`)
                    ])
                if(!cancelled) {setData({ activeStatus, averageSalary, averageTenure, averageAge, salaryRange,
                    byContractType, byPosition, byWorkplace, budget, expiring, newHires })}
            } catch (err) {
                if (!cancelled) { setError(err.message); setData(null) }
            } finally {
                if (!cancelled) setLoading(false)
            }
        }
        load()
        return () => {cancelled = true}
    }, [departmentId, year, days])

    return (
        <div>
            <h1>Reportes</h1>

            <div className="toolbar">
                {role === 'ADMIN' && (
                    <div className="field">
                        <label>Departamento</label>
                        <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
                            <option value="">Selecciona un departamento</option>
                            {departments.map(dept => (
                                <option key={dept.id} value={dept.id}>{dept.name}</option>
                            ))}
                        </select>
                    </div>
                )}
                <div className="field">
                    <label>Año de altas</label>
                    <select value={year} onChange={(e) => setYear(Number(e.target.value))}>
                        {yearOptions.map(y => <option key={y} value={y}>{y}</option>)}
                    </select>
                </div>
                <div className="field">
                    <label>Vencimientos en</label>
                    <select value={days} onChange={(e) => setDays(Number(e.target.value))}>
                        {daysOptions.map(d => <option key={d} value={d}>{d} días</option>)}
                    </select>
                </div>
            </div>

            {error && <p className="msg-error">{error}</p>}
            {loading && <p>Cargando...</p>}

            {data && !loading && (
                <div>
                    <div className="cards-grid">
                        <Card title="Empleados activos" value={data.activeStatus.Activos ?? 0} />
                        <Card title="Empleados inactivos" value={data.activeStatus.Inactivos ?? 0} />
                        <Card title="Edad media" value={`${data.averageAge.toFixed(1)} años`} />
                        <Card title="Antigüedad media" value={`${data.averageTenure.toFixed(1)} meses`} />
                        <Card title={`Altas en ${year}`} value={data.newHires} />
                        <Card title="Salario mínimo" value={money(data.salaryRange.min)} />
                        <Card title="Salario medio" value={money(data.averageSalary)} />
                        <Card title="Salario máximo" value={money(data.salaryRange.max)} />
                    </div>

                    <div className="charts-grid">
                        <BarCard title="Por tipo de contrato" data={data.byContractType} color="#f97316" />
                        <BarCard title="Por cargo" data={data.byPosition} color="#eab308" />
                        <BarCard title="Por lugar de trabajo" data={data.byWorkplace} color="#3b82f6" />
                    </div>

                    <div className="bottom-grid">
                        <div className="panel">
                            <h3>Presupuesto</h3>
                            <table>
                                <tbody>
                                {Object.entries(data.budget).map(([key, value]) => (
                                    <tr key={key}>
                                        <td>{key}</td>
                                        <td className={Number(value) < 0 ? 'negative' : ''}>{money(value)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="panel">
                            <h3>Contratos que vencen en {days} días</h3>
                            {data.expiring.length === 0 ? <p>Ninguno</p> : (
                                <div className="scroll-box">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>Empleado</th>
                                            <th>Tipo</th>
                                            <th>Fecha de fin</th>
                                            <th>Quedan</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {data.expiring.map(c => (
                                            <tr key={c.contractId}>
                                                <td>{c.employeeName} {c.employeeFirstSurname}</td>
                                                <td>{c.contractType}</td>
                                                <td>{c.expiryDate}</td>
                                                <td className={c.daysLeft <= 7 ? 'negative' : ''}>{c.daysLeft} días</td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default Reports