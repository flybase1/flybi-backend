package com.fly.springbootinit.model.dto.aimodel;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteAiModelRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}