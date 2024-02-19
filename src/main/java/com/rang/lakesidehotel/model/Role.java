package com.rang.lakesidehotel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Collection<User> users = new HashSet<>();

    public void assignRoleToUser(User user){
        user.getRoles().add(this);
        this.getUsers().add(user);
    }

    public void removeUserFromRole(User user){
        user.getRoles().remove(this);
        this.getUsers().remove(user);
    }

    //remove all users from the role so we can actually delete the role
    public void removeAllUsersFromRole(){
        if(this.getUsers() != null){
            List<User> roleUsers = this.getUsers().stream().collect(Collectors.toList());
            roleUsers.forEach(this::removeUserFromRole);
        }
    }

    public Role(String name) {
        this.name = name;
    }

    public String getName(){
        return name != null? name: "";
    }

}
