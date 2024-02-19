package com.rang.lakesidehotel.service;

import com.rang.lakesidehotel.model.Role;
import com.rang.lakesidehotel.model.User;

import java.util.List;

public interface RoleService {

    List<Role> getRoles();

    Role createRole(Role theRole);

    void deleteRole(Long roleId);

    Role findByName(String name);

    User removeUserFromRole(Long userId, Long roleId);

    User assignRoleToUser(Long userId, Long roleId);

    Role removeAllUsersFromRole(Long roleId);
}
