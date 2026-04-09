package br.com.projetoApi.Common.Security;

import java.util.Arrays;
import java.util.List;

public enum SystemPermission {
    USER_CREATE,
    USER_LIST,
    USER_UPDATE,
    USER_TOGGLE_ACTIVE,
    ROLE_CREATE,
    ROLE_LIST,
    ROLE_ASSIGN,
    ROLE_REMOVE,
    PERMISSION_CREATE,
    PERMISSION_LIST,
    PERMISSION_ASSIGN,
    UNIDADE_CREATE,
    UNIDADE_LIST,
    UNIDADE_UPDATE,
    UNIDADE_TOGGLE_ACTIVE,
    USER_UNIDADE_ASSIGN,
    USER_UNIDADE_REMOVE,
    AUDIT_LIST,
    BLOCO_READ,
    BLOCO_WRITE,
    SALA_READ,
    SALA_WRITE,
    LUZ_READ,
    LUZ_WRITE;

    public static List<String> names() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
