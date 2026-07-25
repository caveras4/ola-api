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
    public List<Task> listar(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> buscaTarefaId(@PathVariable Long id){
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> salvarTask(@RequestBody Task task){
        var taskSalva = repository.save(task);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()   // pega a URL atual: /tasks
                .path("/{id}")          // acrescenta /{id}
                .buildAndExpand(taskSalva.getId())  // troca {id} pelo id real
                .toUri();

        return ResponseEntity.created(uri).body(taskSalva);
    }
}
