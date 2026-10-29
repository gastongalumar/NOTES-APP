package com.notasapp.mapper;

import com.notasapp.dto.tag.TagResponse;
import com.notasapp.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getNombre());
    }
}
