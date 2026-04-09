package djnd.project.SoundCloud.services;

import org.springframework.stereotype.Service;

import djnd.project.SoundCloud.domain.entity.Category;
import djnd.project.SoundCloud.domain.request.CategoryDTO;
import djnd.project.SoundCloud.repositories.CategoryRepository;
import djnd.project.SoundCloud.utils.error.DuplicateResourceException;
import djnd.project.SoundCloud.utils.error.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryService {
    CategoryRepository categoryRepository;

    public void create(CategoryDTO dto) {
        var category = new Category();
        if (this.categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Category Name", dto.getName());
        }
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        this.categoryRepository.save(category);
    }

    public void update(CategoryDTO dto) throws ResourceNotFoundException {
        var category = this.categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", "#" + dto.getId()));
        if (this.categoryRepository.existsByNameAndIdNot(dto.getName(), dto.getId())) {
            throw new DuplicateResourceException("Category Name", dto.getName());
        }
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        this.categoryRepository.save(category);
    }

}
