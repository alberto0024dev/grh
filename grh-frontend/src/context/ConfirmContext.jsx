import { createContext, useContext, useState, useRef, useCallback } from 'react'
import { useEffect } from 'react'

const ConfirmContext = createContext(null)

export function ConfirmProvider({ children }) {
    const [options, setOptions] = useState(null)   // null = modal cerrado
    const resolver = useRef(null)

    const confirm = useCallback((opts) => {
        setOptions(opts)
        return new Promise((resolve) => { resolver.current = resolve })
    }, [])

    const close = (result) => {
        resolver.current?.(result)
        setOptions(null)
    }
    useEffect(() => {
        if (!options) return
        const onKey = (e) => { if (e.key === 'Escape') close(false) }
        window.addEventListener('keydown', onKey)
        return () => window.removeEventListener('keydown', onKey)
    }, [options])

    return (
        <ConfirmContext.Provider value={confirm}>
            {children}
            {options && (
                <div className="modal-backdrop" onClick={() => close(false)}>
                    <div className="modal" role="dialog" aria-modal="true"
                         onClick={(e) => e.stopPropagation()}>
                        <h3>{options.title ?? 'Confirmar acción'}</h3>
                        <p>{options.message}</p>
                        <div className="form-actions" style={{ gap: '0.5rem' }}>
                            <button className="btn-secondary" autoFocus onClick={() => close(false)}>Cancelar</button>
                            <button className={options.danger ? 'btn-danger' : ''}
                                    onClick={() => close(true)}>
                                {options.confirmText ?? 'Aceptar'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </ConfirmContext.Provider>
    )
}

export function useConfirm() {
    return useContext(ConfirmContext)
}