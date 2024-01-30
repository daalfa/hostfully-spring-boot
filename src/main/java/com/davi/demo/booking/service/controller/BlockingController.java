package com.davi.demo.booking.service.controller;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.service.BlockingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/host")
public class BlockingController {

    private final BlockingService blockingService;

    @Autowired
    public BlockingController(BlockingService blockingService) {
        this.blockingService = blockingService;
    }

    @GetMapping("/blockings/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Blocking getBlockingById(@PathVariable String id) {
        return blockingService.getBlockingById(toLong(id));
    }

    @GetMapping("/blockings")
    @ResponseStatus(HttpStatus.OK)
    public List<Blocking> getAllBlockings() {
        return blockingService.getAllBlockings();
    }

    @PostMapping("/blockings")
    @ResponseStatus(HttpStatus.CREATED)
    public void createBlocking(@Valid @RequestBody Blocking blocking) {
        blockingService.createBlocking(blocking);
    }

    @DeleteMapping("/blockings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlocking(@PathVariable String id) {
        blockingService.deleteBlocking(toLong(id));
    }

    private long toLong(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Id must be a number");
        }
    }
}
