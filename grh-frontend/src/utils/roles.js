export const homeFor = (role) => {
    if (role === 'ADMIN') return '/employees'
    if (role === 'MANAGER') return '/my-department'
    return '/profile'
}