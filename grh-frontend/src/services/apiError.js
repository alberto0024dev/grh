export const buildError = async (response, fallback) => {
    const body = await response.json().catch(() => null)
    const detail = body?.errors
        ? Object.values(body.errors).join(' | ')
        : body?.error
    const error = new Error(detail || `${fallback} (${response.status})`)
    error.status = response.status
    return error
}