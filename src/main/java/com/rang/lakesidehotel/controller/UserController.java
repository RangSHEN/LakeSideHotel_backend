package com.rang.lakesidehotel.controller;

import com.rang.lakesidehotel.model.User;
import com.rang.lakesidehotel.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getUsers(){
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.FOUND);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserByEmail(@PathVariable("userId") String email){
        try {
            User theUser = userService.getUser(email);
            return new ResponseEntity<>(theUser, HttpStatus.OK);
        }catch (UsernameNotFoundException e){
            //if User is not found return "User not found" (UserServiceImpl)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e){
            return new ResponseEntity<>("Error fetching user",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 使用 try-catch 块来捕获并处理可能发生的异常。这是一种在代码中处理异常的方式，以确保程序不会因为异常而崩溃。
     *  使用 throw new 语句主动抛出异常。这是一种在代码中手动引发异常的方式。
     *  try-catch 用于处理可能发生的异常，而 throw new 用于在代码中明确指定某种情况下需要引发异常。
     */
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #email == principal.username)") //USER IS YOURSELF
    public ResponseEntity<String> deleteUser(@PathVariable("userId") String email){
        try {
            userService.deleteUser(email);
            return new ResponseEntity<>("User delete successfully",HttpStatus.OK);
        }catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (Exception e){
            return new ResponseEntity<>("Error deleting user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
