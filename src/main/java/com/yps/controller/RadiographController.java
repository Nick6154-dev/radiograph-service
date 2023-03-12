package com.yps.controller;

import com.yps.service.RadiographService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
public class RadiographController {

    @Autowired
    private RadiographService service;

    @PostMapping("/validate")
    public Integer validate(@RequestBody byte[] imageData) throws IOException {
        return service.validateImage(ImageIO.read(new ByteArrayInputStream(imageData)));
    }

}
