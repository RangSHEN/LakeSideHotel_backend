package com.rang.lakesidehotel.controller;

import com.rang.lakesidehotel.exception.RoleAlreadyExistException;
import com.rang.lakesidehotel.model.Role;
import com.rang.lakesidehotel.model.User;
import com.rang.lakesidehotel.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@AllArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/all-roles")
    public ResponseEntity<List<Role>> getAllRoles(){
        return new ResponseEntity<>(roleService.getRoles(), HttpStatus.FOUND);
    }

    @PostMapping("/create-new-role")
    public ResponseEntity<String> createRole(@RequestBody Role theRole){
        try {
            roleService.createRole(theRole);
            return new ResponseEntity<>("New role created successfully!", HttpStatus.OK);
        }catch (RoleAlreadyExistException re){
            return new ResponseEntity<>(re.getMessage(),HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/delete/{roleId}")
    public void deleteRole(@PathVariable("roleId") Long roleId){
        roleService.deleteRole(roleId);
    }

    @PostMapping("/remove-all-users-from-role/{roleId}")
    public Role removeAllUsersFromRole(@PathVariable("roleId") Long roleId){
        return roleService.removeAllUsersFromRole(roleId);
    }

    @PostMapping("/remove-user-from-role")
    public User removeUserFromRole(@RequestParam("userId") Long userId,
                                   @RequestParam("roleId") Long roleId){
        return roleService.removeUserFromRole(userId,roleId);
    }

    @PostMapping("/assign-user-to-role")
    public User assignRoleToUser(@RequestParam("userId") Long userId,
                                 @RequestParam("roleId") Long roleId){
        return roleService.assignRoleToUser(userId,roleId);
    }
}
