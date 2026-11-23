package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.domain.User;
import com.hlala.schemas.AccountSchemas.AccountDto;
import com.hlala.schemas.AccountSchemas.RegisterRequest;
import com.hlala.schemas.AccountSchemas.RoleRequest;
import com.hlala.service.AccountService;
import com.hlala.service.Actor;
import com.hlala.service.ProfileLookup;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accounts;
    private final ProfileLookup lookup;

    public AccountController(AccountService accounts, ProfileLookup lookup) {
        this.accounts = accounts;
        this.lookup = lookup;
    }

    /** Public. Real sign-up will add an OTP step; phoneVerified stays false until then. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto register(@Valid @RequestBody RegisterRequest req) {
        User user = accounts.register(req.phone(), req.name());
        return AccountDto.of(user, lookup.rolesOf(user.getId()));
    }

    @GetMapping("/me")
    public AccountDto me(@CurrentActor Actor actor) {
        return AccountDto.of(accounts.get(actor.userId()), lookup.rolesOf(actor.userId()));
    }

    @PostMapping("/me/roles")
    public AccountDto takeRole(@CurrentActor Actor actor, @Valid @RequestBody RoleRequest req) {
        accounts.takeRole(actor, req.role());
        return AccountDto.of(accounts.get(actor.userId()), lookup.rolesOf(actor.userId()));
    }
}
