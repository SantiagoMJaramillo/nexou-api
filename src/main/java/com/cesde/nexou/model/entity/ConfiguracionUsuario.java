package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ConfiguracionUsuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionUsuario extends BaseEntity {

}