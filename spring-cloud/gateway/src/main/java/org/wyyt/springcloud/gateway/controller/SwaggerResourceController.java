package org.wyyt.springcloud.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.springcloud.gateway.config.GatewaySwaggerResourceProvider;
import springfox.documentation.swagger.web.*;

import java.util.List;

/**
 * the controller of Swagger
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@RestController
@RequestMapping("/swagger-resources")
public class SwaggerResourceController {
    private final GatewaySwaggerResourceProvider gatewaySwaggerResourceProvider;

    public SwaggerResourceController(final GatewaySwaggerResourceProvider gatewaySwaggerResourceProvider) {
        this.gatewaySwaggerResourceProvider = gatewaySwaggerResourceProvider;
    }

    @RequestMapping(value = "/configuration/security")
    public ResponseEntity<SecurityConfiguration> securityConfiguration() {
        return new ResponseEntity<>(SecurityConfigurationBuilder.builder().build(), HttpStatus.OK);
    }

    @RequestMapping(value = "/configuration/ui")
    public ResponseEntity<UiConfiguration> uiConfiguration() {
        return new ResponseEntity<>(UiConfigurationBuilder.builder().build(), HttpStatus.OK);
    }

    @RequestMapping
    public ResponseEntity<List<SwaggerResource>> swaggerResources() {
        return new ResponseEntity<>(gatewaySwaggerResourceProvider.get(), HttpStatus.OK);
    }
}