package com.bancohoras.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class SchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE registros_ponto ALTER COLUMN horario_sugeridoia TYPE varchar(50)");
            log.info("SchemaMigration: coluna horario_sugeridoia ampliada para varchar(50).");
        } catch (Exception e) {
            log.debug("SchemaMigration: ALTER TABLE ignorado — coluna já compatível.");
        }
    }
}
