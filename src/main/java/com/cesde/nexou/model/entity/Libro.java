package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Libro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Libro extends BaseEntity {

}