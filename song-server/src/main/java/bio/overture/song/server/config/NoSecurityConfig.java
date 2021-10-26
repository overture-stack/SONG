package bio.overture.song.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("noSecurityDev")
@Configuration
@EnableWebSecurity
public class NoSecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Disable cors and csrf to avoid 403 forbidden for local development.
    http.cors().and().csrf().disable().authorizeRequests().antMatchers("/").permitAll();
  }
}
