package org.delcom._1s25021springbootcashflow.config;

import org.delcom._1s25021springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom._1s25021springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom._1s25021springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom._1s25021springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.usecase.AuthUseCase;
import org.delcom._1s25021springbootcashflow.usecase.FinanceUseCase;
import org.delcom._1s25021springbootcashflow.usecase.UserUseCase;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Composition root: seluruh wiring bean ada di sini. */
@Configuration
public class AppConfig implements WebMvcConfigurer {
    private final String uploadDir;

    public AppConfig(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource, IUserRepository userRepository) {
        // userRepository dijadikan parameter agar schema (users) dibuat lebih dulu
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage() {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
