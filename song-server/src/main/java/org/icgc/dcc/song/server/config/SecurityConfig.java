/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.config;

import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.jwt.JWTAuthorizationFilter;
import org.icgc.dcc.song.server.jwt.JWTTokenConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
@Profile("jwt")
@EnableWebSecurity
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

    @Value("${auth.server.suffix}")
    private String uploadScope;

    @Autowired
    private SwaggerConfig swaggerConfig;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${auth.jwt.publicKeyUrl}")
    private String publicKeyUrl;

    @Override
    @SneakyThrows
    public void configure(HttpSecurity http) {
        http
                .addFilterBefore(new JWTAuthorizationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/health").permitAll()
                .antMatchers("/isAlive").permitAll()
                .antMatchers("/studies/**").permitAll()
                .antMatchers("/upload/**").permitAll()
                .antMatchers("/entities/**").permitAll()
                .antMatchers(swaggerConfig.getAlternateSwaggerUrl()).permitAll()
                .antMatchers("/swagger**", "/swagger-resources/**", "/v2/api**", "/webjars/**").permitAll()
                .and()
                .authorizeRequests()
                .anyRequest().authenticated();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenServices());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    @SneakyThrows
    public JwtAccessTokenConverter accessTokenConverter() {
        return new JWTTokenConverter(fetchJWTPublicKey());
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        val defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

    /**
     * Call EGO server for public key to use when verifying JWTs
     * Pass this value to the JWTTokenConverter
     */
    @SneakyThrows
    private String fetchJWTPublicKey() {
        val publicKeyResource = resourceLoader.getResource(publicKeyUrl);

        val stringBuilder = new StringBuilder();
        val reader = new BufferedReader(
                new InputStreamReader(publicKeyResource.getInputStream()));

        reader.lines().forEach(stringBuilder::append);
        return stringBuilder.toString();
    }
}
