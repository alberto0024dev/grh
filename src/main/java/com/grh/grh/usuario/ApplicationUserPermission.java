package com.grh.grh.usuario;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationUserPermission {
    UPDATE_EMPLOYEE("employee:update"),     // ADMIN + MANAGER
    VIEW_REPORTS("reports:view"),           // ADMIN + MANAGER
    READ_OWN_PROFILE("profile:read"),       // EMPLOYEE
    UPDATE_OWN_PROFILE("profile:update");  // EMPLOYEE

    private final String permission;

}
