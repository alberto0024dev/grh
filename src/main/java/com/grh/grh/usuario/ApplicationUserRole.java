package com.grh.grh.usuario;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.grh.grh.usuario.ApplicationUserPermission.*;

@Getter
@RequiredArgsConstructor
public enum ApplicationUserRole {
    ADMIN(Set.of(UPDATE_EMPLOYEE,VIEW_REPORTS)),
    MANAGER(Set.of(UPDATE_EMPLOYEE,VIEW_REPORTS, UPDATE_OWN_PROFILE, READ_OWN_PROFILE)),
    EMPLOYEE(Set.of(READ_OWN_PROFILE,UPDATE_OWN_PROFILE)),;

    private final Set<ApplicationUserPermission> permissions;

}
