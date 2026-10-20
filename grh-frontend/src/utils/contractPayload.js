export const toContractPayload = (form) => ({
    ...form,
    vacationDays: Number(form.vacationDays),
    salary: Number(form.salary),
    expiryDate: form.contractType === 'PERMANENT' ? null : form.expiryDate,
    workSchedule: {
        weeklyHours: Number(form.workSchedule.weeklyHours),
        startTime: form.workSchedule.startTime + ':00',
        endTime: form.workSchedule.endTime + ':00',
        daysOff: Number(form.workSchedule.daysOff)
    }
})