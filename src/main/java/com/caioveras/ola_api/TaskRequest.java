package com.caioveras.ola_api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(@NotBlank @Size(max = 100) String titulo, @Size(max = 255) String descricao, boolean concluido) {
    public Task toEntity() {
        Task task = new Task();
        task.setTitulo(this.titulo());
        task.setDescricao(this.descricao());
        task.setConcluido(this.concluido());
        return task;
    }
}
