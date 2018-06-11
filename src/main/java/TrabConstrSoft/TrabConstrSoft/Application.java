package TrabConstrSoft.TrabConstrSoft;

import java.net.URISyntaxException;
import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws URISyntaxException, SQLException {
        SpringApplication.run(Application.class, args);
    }
}