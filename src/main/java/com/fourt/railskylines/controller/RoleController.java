package com.fourt.railskylines.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.railskylines.domain.Permission;
import com.fourt.railskylines.domain.Role;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.service.RoleService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("https://railskylines-fe-1.onrender.com")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @APIMessage("Create Role")
    public ResponseEntity<Role> createPermission(@Valid @RequestBody Role role) throws IdInvalidException {
        if (this.roleService.existByName(role.getName())) {
            throw new IdInvalidException("Role is exist");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.handleCreateRole(role));
    }

    @PutMapping("/roles/{id}")
    @APIMessage("Update a role")
    public ResponseEntity<Role> update(@PathVariable("id") Long id, @Valid @RequestBody Role r)
            throws IdInvalidException {
        // check id
        if (this.roleService.fetchById(id) == null) {
            throw new IdInvalidException("Role với id = " + id + " không tồn tại");
        }

        // check name
        // if (this.roleService.existByName(r.getName())) {
        // throw new IdInvalidException("Role với name = " + r.getName() + " đã tồn
        // tại");
        // }

        return ResponseEntity.ok().body(this.roleService.update(id, r));
    }

    @DeleteMapping("/roles/{id}")
    @APIMessage("delete a Role")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") long id)
            throws IdInvalidException {
        if (this.roleService.fetchById(id) == null) {
            throw new IdInvalidException("Role with id = " + id + "not exist ");
        }
        this.roleService.deleteRole(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/roles")
    @APIMessage("fetch all roles")
    public ResponseEntity<ResultPaginationDTO> getAllRoles(
            @Filter Specification<Role> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roleService.fetchAllRoles(spec,
                pageable));
    }

    @GetMapping("/roles/{id}")
    @APIMessage("Fetch Role by ID")
    public ResponseEntity<Role> getStationById(@PathVariable("id") Long id) throws IdInvalidException {
        Role role = this.roleService.fetchById(id);
        if (role == null) {
            throw new IdInvalidException("Role with id = " + id + " does not exist, please check again");
        }
        return ResponseEntity.ok().body(role);
    }

}
