package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ReservaLibro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaLibro extends BaseEntity {

}