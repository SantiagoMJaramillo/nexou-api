package com.cesde.nexou.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.model.entity.ImagenArticulo;

public interface ImagenArticuloRepository extends JpaRepository<ImagenArticulo, Long> {

    Optional<ImagenArticulo> findByTipoAndArticuloId(String tipo, Long articuloId);

    @Transactional
    void deleteByTipoAndArticuloId(String tipo, Long articuloId);
}
