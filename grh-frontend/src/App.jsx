import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Employees from './pages/Employees'
import Departments from './pages/Departments'
import Positions from './pages/Positions'
import Profile from './pages/Profile'
import Layout from './components/Layout'
import { useAuth } from './context/AuthContext'
import MyDepartment from './pages/MyDepartment'
import Contracts from './pages/Contracts'
import Reports from './pages/Reports'
import DepartmentDetail from './pages/DepartmentDetail'
import ChangePassword from './pages/ChangePassword'
import NotFound from './pages/NotFound'
import {homeFor} from "./utils/roles.js";



// roles: lista opcional de roles que pueden entrar. Si no se pasa, entra cualquiera logado.
function RutaProtegida({ children, roles }) {
  const { token, role } = useAuth()
  if (!token) return <Navigate to="/login" />
  if (roles && !roles.includes(role)) return <Navigate to={homeFor(role)} /> // qye hace esto????
  return <Layout>{children}</Layout>
}

function Inicio() {
  const { token, role } = useAuth()
  return <Navigate to={token ? homeFor(role) : '/login'} />
}

function App() {
  return (
      <BrowserRouter>
        <Routes>

          <Route path="/login" element={<Login />} />

          <Route path="/employees" element={
            <RutaProtegida roles={['ADMIN']}><Employees /></RutaProtegida>
          } />
          <Route path="/departments" element={
            <RutaProtegida roles={['ADMIN']}><Departments /></RutaProtegida>
          } />
          <Route path="/positions" element={
            <RutaProtegida roles={['ADMIN']}><Positions /></RutaProtegida>
          } />
          <Route path="/profile" element={
            <RutaProtegida roles={['MANAGER', 'EMPLOYEE']}><Profile /></RutaProtegida>
          } />
          <Route path="/my-department" element={
            <RutaProtegida roles={['MANAGER']}><MyDepartment /></RutaProtegida>
          } />

          <Route path="/contracts" element={
            <RutaProtegida roles={['ADMIN']}><Contracts /></RutaProtegida>
          } />
          <Route path="/reports" element={
            <RutaProtegida roles={['ADMIN', 'MANAGER']}><Reports /></RutaProtegida>
          } />
          <Route path="/departments/:id" element={
            <RutaProtegida roles={['ADMIN']}><DepartmentDetail /></RutaProtegida>
          } />
          <Route path="/change-password" element={
            <RutaProtegida><ChangePassword /></RutaProtegida>
          } />

          <Route path="/" element={<Inicio />} />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
  )
}

export default App