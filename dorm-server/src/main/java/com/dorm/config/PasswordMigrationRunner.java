package com.dorm.config;

import com.dorm.config.SecurityConfig.PasswordEncoder;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class PasswordMigrationRunner implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS login_fail_count INT DEFAULT 0");
            stmt.execute("ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS lock_until TIMESTAMP");
        } catch (Exception e) {
            log.debug("Column migration skipped: {}", e.getMessage());
        }

        List<SysUser> users = userMapper.selectList(null);
        int migrated = 0;
        for (SysUser user : users) {
            if (user.getPassword() != null && !user.getPassword().startsWith("{SHA256}")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userMapper.updateById(user);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("Migrated {} plaintext passwords to SHA-256", migrated);
        }
    }
}
