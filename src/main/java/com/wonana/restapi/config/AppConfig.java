package com.wonana.restapi.config;

import com.wonana.restapi.accounts.Account;
import com.wonana.restapi.accounts.AccountRole;
import com.wonana.restapi.accounts.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Set<AccountRole> roles = new HashSet<>();
                roles.add(AccountRole.ADMIN);
                roles.add(AccountRole.USER);

                Account account = Account.builder()
                        .email("jayden@email.com")
                        .password("wonawona")
                        .roles(roles)
                        .build();
                accountService.saveAccount(account);
            }
        };
    }
}
