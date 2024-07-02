/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.avn.mobilita.anag;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
@ComponentScan({"it.mds.sdk.flusso.avn.mobilita.anag.controller", "it.mds.sdk.flusso.avn.mobilita.anag",
		"it.mds.sdk.rest.persistence.entity",
		"it.mds.sdk.libreriaregole.validator",
		"it.mds.sdk.flusso.avn.mobilita.anag.service", "it.mds.sdk.flusso.avn.mobilita.anag.tracciato",
		"it.mds.sdk.gestoreesiti", "it.mds.sdk.flusso.avn.mobilita.anag.parser.regole",
		"it.mds.sdk.flusso.avn.mobilita.anag.parser.regole.conf",
		"it.mds.sdk.connettoremds"})
@OpenAPIDefinition(info=@Info(title = "SDK Ministero Della Salute - Flusso AVX", version = "0.0.1-SNAPSHOT", description = "Flusso Vaccinazioni Residenti Anagrafica"))
public class FlussoAvnMobilitaAnag {

	public static void main(String[] args) {
		SpringApplication.run(FlussoAvnMobilitaAnag.class, args);
	}

}
