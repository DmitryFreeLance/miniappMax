package com.maxminiapp.service;

import com.maxminiapp.dto.AdminCreateInfoPostRequest;
import com.maxminiapp.dto.InfoPostResponse;
import com.maxminiapp.exception.NotFoundException;
import com.maxminiapp.model.InfoPost;
import com.maxminiapp.repository.InfoPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InfoPostService {

    private final InfoPostRepository infoPostRepository;

    public InfoPostService(InfoPostRepository infoPostRepository) {
        this.infoPostRepository = infoPostRepository;
    }

    public List<InfoPostResponse> getAll() {
        return infoPostRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InfoPostResponse create(AdminCreateInfoPostRequest request, Long adminUserId) {
        InfoPost post = new InfoPost();
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setCreatedByAdminId(adminUserId);
        return toResponse(infoPostRepository.save(post));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!infoPostRepository.existsById(id)) {
            throw new NotFoundException("Пост не найден");
        }
        infoPostRepository.deleteById(id);
    }

    private InfoPostResponse toResponse(InfoPost post) {
        return new InfoPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getCreatedAt());
    }
}
