package com.caioveras.ola_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository repository;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TaskResponse> listarTasks() {
        return repository.findAll().stream().map(TaskResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> buscaTaskId(@PathVariable Long id) {
        return repository.findById(id)
                .map(TaskResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> salvarTask(@RequestBody TaskRequest request) {
        var taskSalva = repository.save(request.toEntity());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()   // pega a URL atual: /tasks
                .path("/{id}")          // acrescenta /{id}
                .buildAndExpand(taskSalva.getId())  // troca {id} pelo id real
                .toUri();

        // Converte a entidade salva para o DTO de resposta
        TaskResponse response = TaskResponse.fromEntity(taskSalva);

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> atualizaTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        return repository.findById(id)
                .map(taskExistente -> {
                    taskExistente.setTitulo(request.titulo());
                    taskExistente.setDescricao(request.descricao());
                    taskExistente.setConcluido(request.concluido());
                    var taskSalva = repository.save(taskExistente);

                    TaskResponse response = TaskResponse.fromEntity(taskSalva);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());// Status 404 caso não exista
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletaTask(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();// Status 404 caso não exista
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();// Status 204 No Content (Sucesso sem corpo de resposta)
    }

}
