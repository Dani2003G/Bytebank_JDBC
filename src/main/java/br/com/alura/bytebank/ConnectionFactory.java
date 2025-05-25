package br.com.alura.bytebank;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    public Connection recuperarConexao() {
        try {
            return createDataSource().getConnection();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HikariDataSource createDataSource() throws IOException {
        Properties props = new Properties();
        InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("config.properties");
        if (input == null) {
            throw new RuntimeException("Arquivo config.properties não encontrado no classpath.");
        }
        props.load(input);

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);

    }

}
