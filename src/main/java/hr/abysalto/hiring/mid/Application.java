package hr.abysalto.hiring.mid;

import hr.abysalto.hiring.mid.components.DatabaseInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase(DatabaseInitializer databaseInitializer) {
		return args -> {
			if (!databaseInitializer.isDataInitialized()) {
				databaseInitializer.initialize();
				System.out.println("Database initialized successfully!");
			}
		};
	}
}
