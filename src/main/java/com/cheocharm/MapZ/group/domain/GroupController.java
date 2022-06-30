package com.cheocharm.MapZ.group.domain;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GroupController")
@RequiredArgsConstructor
@RequestMapping("/api/group")
@RestController
public class GroupController {

}
