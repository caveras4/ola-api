package com.caioveras.ola_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OlaController {

    @GetMapping("/ola")
    public String ola(){
        return "Olá, Mundo!";
    }

    @GetMapping("/ola/{nome}")
    public String olaComNome(@PathVariable String nome){
        return "Olá, " + nome;
    }

}
