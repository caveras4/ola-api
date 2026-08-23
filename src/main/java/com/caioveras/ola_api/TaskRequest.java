package com.caioveras.ola_api;

public record TaskRequest(String titulo, String descricao, boolean concluido) {
    public Task toEntity() {
        Task task = new Task();
        task.setTitulo(this.titulo());
        task.setDescricao(this.descricao());
        task.setConcluido(this.concluido());
        return task;
    }
}
