package djnd.project.SoundCloud.controllers.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import djnd.project.SoundCloud.domain.request.CategoryDTO;
import djnd.project.SoundCloud.services.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategoryDTO dto) {
        this.categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Create new category success!");
    }

    @PatchMapping
    public ResponseEntity<?> update(@RequestBody CategoryDTO dto) {
        this.categoryService.update(dto);
        return ResponseEntity.ok("Update category success");
    }
}
