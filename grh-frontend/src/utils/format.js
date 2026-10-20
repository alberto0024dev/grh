export const money = (n) => Number(n).toLocaleString('es-ES', { style: 'currency', currency: 'EUR' })
export const hour = (t) => t?.slice(0, 5)