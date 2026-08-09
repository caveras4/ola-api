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
    public List<Task> listarTasks() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> buscaTaskId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> salvarTask(@RequestBody Task task) {
        var taskSalva = repository.save(task);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()   // pega a URL atual: /tasks
                .path("/{id}")          // acrescenta /{id}
                .buildAndExpand(taskSalva.getId())  // troca {id} pelo id real
                .toUri();

        return ResponseEntity.created(uri).body(taskSalva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> atualizaTask(@PathVariable Long id, @RequestBody Task task) {
        return repository.findById(id)
                .map(taskExistente -> {
                    taskExistente.setTitulo(task.getTitulo());
                    taskExistente.setDescricao(task.getDescricao());
                    taskExistente.setConcluido(task.isConcluido());
                    var taskSalva = repository.save(taskExistente);
                    return ResponseEntity.ok(taskSalva);
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
